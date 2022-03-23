/*
 * MIT License
 *
 * Copyright (c) 2021-2022 zodac.me
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.zodac.folding.bean.tc.lars;

import static me.zodac.folding.api.util.NumberUtils.formatWithCommas;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.lars.LarsRetriever;
import me.zodac.folding.api.util.EnvironmentVariableUtils;
import me.zodac.folding.bean.api.FoldingRepository;
import me.zodac.folding.lars.HardwareSplitter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Retrieves the latest {@link me.zodac.folding.api.tc.Hardware} information from LARS for {@link me.zodac.folding.api.tc.HardwareType}s, then updates
 * the database with the new values.
 */
@Component
public class LarsHardwareUpdater {

    private static final Logger LARS_LOGGER = LogManager.getLogger("lars");
    private static final String LARS_URL_ROOT = EnvironmentVariableUtils.getOrDefault("LARS_URL_ROOT", "https://folding.lar.systems");

    private final FoldingRepository foldingRepository;
    private final LarsRetriever larsRetriever;

    /**
     * {@link Autowired} constructor.
     *
     * @param foldingRepository the {@link FoldingRepository}
     * @param larsRetriever     the {@link LarsRetriever}
     */
    @Autowired
    public LarsHardwareUpdater(final FoldingRepository foldingRepository, final LarsRetriever larsRetriever) {
        this.foldingRepository = foldingRepository;
        this.larsRetriever = larsRetriever;
    }

    /**
     * Retrieves {@link me.zodac.folding.api.tc.HardwareType} data from LARS, creates {@link Hardware} instances, then updates to the DB.
     *
     * <p>
     * Depending on the state of the retrieved {@link Hardware}, one of the following will occur:
     * <ul>
     *     <li>
     *         If the {@link Hardware} is retrieved from LARS and does not exist in the DB, it will be <b>created</b>.
     *     </li>
     *     <li>
     *         If the {@link Hardware} is exists in the DB but is not retrieved from LARS, it will be <b>deleted</b>.
     *     </li>
     *     <li>
     *         If the {@link Hardware} is retrieved from LARS and does exist in the DB and either the {@code multiplier} or {@code averagePpd} is
     *         different, it will be <b>updated</b>.
     *     </li>
     *     <li>
     *         If the {@link Hardware} is retrieved from LARS and does exist in the DB and neither the {@code multiplier} nor {@code averagePpd} is
     *         different, it will be <b>ignored</b>.
     *     </li>
     * </ul>
     *
     * <p>
     * Each {@link Hardware} requires a {@code multiplier}, which is calculated by the LARS PPD DB itself. The formula for the {@code multiplier} is:
     * <pre>
     *     (PPD of best {@link Hardware}) / (PPD of current {@link Hardware})
     * </pre>
     */
    public void retrieveHardwareAndPersist() {
        try {
            retrieveGpusAndPersist();
            LARS_LOGGER.info("LARS update complete");
        } catch (final Exception e) {
            LARS_LOGGER.warn("Unexpected error updating LARS hardware data", e);
        }
    }

    private void retrieveGpusAndPersist() {
        final String gpuDbUrl = LARS_URL_ROOT + "/api/gpu_ppd/gpu_rank_list.json";
        final Set<Hardware> larsGpus = larsRetriever.retrieveGpus(gpuDbUrl);

        if (larsGpus.isEmpty()) {
            LARS_LOGGER.warn("No GPUs retrieved from LARs DB");
            return;
        }

        LARS_LOGGER.debug("Retrieved GPUs from LARS DB: {}", larsGpus);
        LARS_LOGGER.info("Retrieved {} GPUs from LARS DB", larsGpus.size());

        final Collection<Hardware> existing = foldingRepository.getAllHardware();

        for (final Hardware hardware : HardwareSplitter.toDelete(larsGpus, existing)) {
            foldingRepository.deleteHardware(hardware);
            LARS_LOGGER.info("Deleted GPU hardware '{}' (ID: {})", hardware.hardwareName(), hardware.id());
        }

        for (final Map.Entry<Hardware, Hardware> entry : HardwareSplitter.toUpdate(larsGpus, existing).entrySet()) {
            try {
                updateHardware(entry);
            } catch (final Exception e) {
                LARS_LOGGER.warn("Unexpected error connecting to Folding@Home stats to verify new GPU hardware", e);
            }
        }

        for (final Hardware hardware : HardwareSplitter.toCreate(larsGpus, existing)) {
            final Hardware createdHardware = foldingRepository.createHardware(hardware);
            LARS_LOGGER.info("Created GPU hardware '{}' (ID: {})", createdHardware.hardwareName(), createdHardware.id());
        }
    }

    private void updateHardware(final Map.Entry<Hardware, Hardware> entry) {
        final Hardware updatedHardware = entry.getKey();
        final Hardware existingHardware = entry.getValue();
        final Hardware updatedHardwareWithId = Hardware.updateWithId(existingHardware.id(), updatedHardware);

        foldingRepository.updateHardware(updatedHardwareWithId, existingHardware);

        LARS_LOGGER.info("""
                LARS updated GPU hardware:
                {}
                ID: {}
                Multiplier: {} -> {}
                Average PPD: {} -> {}
                """,
            updatedHardware.hardwareName(),
            existingHardware.id(),
            existingHardware.multiplier(),
            updatedHardware.multiplier(),
            formatWithCommas(existingHardware.averagePpd()),
            formatWithCommas(updatedHardware.averagePpd()));
    }
}

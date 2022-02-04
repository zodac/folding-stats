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

import static java.util.stream.Collectors.toSet;
import static me.zodac.folding.api.util.NumberUtils.formatWithCommas;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.HardwareMake;
import me.zodac.folding.api.tc.HardwareType;
import me.zodac.folding.api.tc.lars.LarsGpu;
import me.zodac.folding.api.util.EnvironmentVariableUtils;
import me.zodac.folding.bean.FoldingRepository;
import me.zodac.folding.lars.HardwareSplitter;
import me.zodac.folding.lars.LarsGpuRetriever;
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

    private static final Logger LOGGER = LogManager.getLogger();
    private static final String LARS_URL_ROOT = EnvironmentVariableUtils.getOrDefault("LARS_URL_ROOT", "https://folding.lar.systems");

    @Autowired
    private FoldingRepository foldingRepository;

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
     * Since each {@link Hardware} requires a {@code multiplier}, we will manually calculate it based off the highest-ranked
     * {@link LarsGpu} that is retrieved. The formula for the {@code multiplier} is:
     * <pre>
     *     (PPD of best {@link LarsGpu}) / (PPD of current {@link LarsGpu})
     * </pre>
     */
    public void retrieveHardwareAndPersist() {
        final String gpuDbUrl = LARS_URL_ROOT + "/gpu_ppd/overall_ranks";
        final List<LarsGpu> larsGpus = LarsGpuRetriever.retrieveGpus(gpuDbUrl);
        LOGGER.debug("Retrieved GPUs from LARS DB: {}", larsGpus);
        LOGGER.info("Retrieved {} GPUs from LARS DB", larsGpus.size());

        if (larsGpus.isEmpty()) {
            LOGGER.warn("No GPUs retrieved from LARs DB");
            return;
        }

        final LarsGpu bestGpu = larsGpus.get(0);
        final long bestPpd = bestGpu.getAveragePpd();
        LOGGER.info("Best PPD is '{}' for '{}', will compare to this", formatWithCommas(bestPpd), bestGpu.getModelInfo());

        final Collection<Hardware> lars = larsGpus
            .stream()
            .map(larsGpu -> toHardware(larsGpu, bestPpd))
            .collect(toSet());
        final Collection<Hardware> existing = foldingRepository.getAllHardware();

        for (final Hardware hardware : HardwareSplitter.toDelete(lars, existing)) {
            foldingRepository.deleteHardware(hardware);
            LOGGER.info("Deleted hardware '{}' (ID: {})", hardware.getHardwareName(), hardware.getId());
        }

        for (final Map.Entry<Hardware, Hardware> entry : HardwareSplitter.toUpdate(lars, existing).entrySet()) {
            try {
                final Hardware updatedHardware = entry.getKey();
                final Hardware existingHardware = entry.getValue();
                final Hardware updatedHardwareWithId = Hardware.updateWithId(existingHardware.getId(), updatedHardware);

                foldingRepository.updateHardware(updatedHardwareWithId, existingHardware);

                LOGGER.info("""
                        LARS updated hardware:
                        {}
                        ID: {}
                        Multiplier: {} -> {}
                        Average PPD: {} -> {}
                        """,
                    updatedHardware.getHardwareName(),
                    existingHardware.getId(),
                    existingHardware.getMultiplier(),
                    updatedHardware.getMultiplier(),
                    formatWithCommas(existingHardware.getAveragePpd()),
                    formatWithCommas(updatedHardware.getAveragePpd()));
            } catch (final Exception e) {
                LOGGER.warn("Unexpected error connecting to Folding@Home stats to verify new hardware", e);
            }
        }

        for (final Hardware hardware : HardwareSplitter.toCreate(lars, existing)) {
            final Hardware createdHardware = foldingRepository.createHardware(hardware);
            LOGGER.info("Created hardware '{}' (ID: {})", createdHardware.getHardwareName(), createdHardware.getId());
        }

        LOGGER.info("LARS update complete");
    }

    private static Hardware toHardware(final LarsGpu larsGpu, final long bestPpd) {
        return Hardware.builder()
            .hardwareName(larsGpu.getModelInfo())
            .displayName(larsGpu.getDisplayName())
            .hardwareMake(HardwareMake.get(larsGpu.getManufacturer()))
            .hardwareType(HardwareType.GPU)
            .multiplier(getMultiplier(bestPpd, larsGpu.getAveragePpd()))
            .averagePpd(larsGpu.getAveragePpd())
            .build();
    }

    private static double getMultiplier(final long bestPpd, final long currentPpd) {
        return roundDoubleToTwoPlaces((double) bestPpd / currentPpd);
    }

    private static double roundDoubleToTwoPlaces(final double input) {
        final String doubleAsString = String.format("%.2f", input);
        return Double.parseDouble(doubleAsString);
    }
}

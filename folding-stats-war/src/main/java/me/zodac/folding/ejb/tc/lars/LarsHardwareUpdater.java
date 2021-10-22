package me.zodac.folding.ejb.tc.lars;

import static java.util.stream.Collectors.toList;
import static me.zodac.folding.api.util.NumberUtils.formatWithCommas;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.HardwareMake;
import me.zodac.folding.api.tc.HardwareType;
import me.zodac.folding.api.tc.lars.LarsGpu;
import me.zodac.folding.api.util.EnvironmentVariableUtils;
import me.zodac.folding.ejb.api.BusinessLogic;
import me.zodac.folding.lars.HardwareSplitter;
import me.zodac.folding.lars.LarsGpuRetriever;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Retrieves the latest {@link me.zodac.folding.api.tc.Hardware} information from LARS for {@link me.zodac.folding.api.tc.HardwareType}s, then updates
 * the database with the new values.
 */
@Singleton
public class LarsHardwareUpdater {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final String LARS_URL_ROOT = EnvironmentVariableUtils.get("LARS_URL_ROOT", "https://folding.lar.systems");

    @EJB
    private BusinessLogic businessLogic;

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

        final long bestPpd = larsGpus.get(0).getAveragePpd();
        LOGGER.info("Best PPD is '{}', will compare to this", formatWithCommas(bestPpd));

        final Collection<Hardware> lars = larsGpus
            .stream()
            .map(larsGpu -> toHardware(larsGpu, bestPpd))
            .collect(toList());
        final Collection<Hardware> existing = businessLogic.getAllHardware();

        for (final Hardware hardware : HardwareSplitter.toDelete(lars, existing)) {
            businessLogic.deleteHardware(hardware);
            LOGGER.info("Deleted hardware '{}' (ID: {})", hardware.getHardwareName(), hardware.getId());
        }

        for (final Map.Entry<Hardware, Hardware> entry : HardwareSplitter.toUpdate(lars, existing).entrySet()) {
            try {
                final Hardware updatedHardware = entry.getKey();
                final Hardware existingHardware = entry.getValue();
                final Hardware updatedHardwareWithId = Hardware.updateWithId(existingHardware.getId(), updatedHardware);

                businessLogic.updateHardware(updatedHardwareWithId, existingHardware);
                LOGGER.info("LARS updated hardware\n'{}' (ID: {})\nMultiplier: {} -> {}\nAverage PPD: {} -> {}\n", updatedHardware.getHardwareName(),
                    existingHardware.getId(), existingHardware.getMultiplier(), updatedHardware.getMultiplier(),
                    formatWithCommas(existingHardware.getAveragePpd()), formatWithCommas(updatedHardware.getAveragePpd()));
            } catch (final Exception e) {
                LOGGER.warn("Unexpected error connecting to Folding@Home stats to verify new hardware", e);
            }
        }

        for (final Hardware hardware : HardwareSplitter.toCreate(lars, existing)) {
            final Hardware createdHardware = businessLogic.createHardware(hardware);
            LOGGER.debug("Created hardware '{}' (ID: {})", createdHardware.getHardwareName(), createdHardware.getId());
        }
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

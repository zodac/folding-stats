package me.zodac.folding.lars;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
import me.zodac.folding.api.tc.Hardware;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Utility class used to filter {@link Collection}s of {@link Hardware}.
 *
 * <p>
 * Since we know the {@code hardwareName} of a {@link Hardware} is unique, we can use {@link HardwareNameComparator} to compare.
 */
public final class HardwareSplitter {

    private static final Logger LOGGER = LogManager.getLogger();

    private HardwareSplitter() {

    }

    /**
     * If any of the {@link Hardware} from LARS does <b>not</b> already exist in the DB, it will be returned to be created.
     *
     * @param fromLars the {@link Hardware} retrieved from LARS
     * @param inDb     the {@link Hardware} already existing in the DB
     * @return a {@link Collection} of the {@link Hardware} to be created
     */
    public static Collection<Hardware> toCreate(final Collection<Hardware> fromLars, final Collection<Hardware> inDb) {
        final TreeSet<Hardware> lars = new TreeSet<>(HardwareNameComparator.create());
        lars.addAll(fromLars);

        final TreeSet<Hardware> db = new TreeSet<>(HardwareNameComparator.create());
        db.addAll(inDb);

        lars.removeAll(db);
        LOGGER.info("{} from LARS, {} in DB, {} to create", fromLars.size(), inDb.size(), lars.size());
        return lars;
    }

    /**
     * If any of the {@link Hardware} in the DB is not also listed in the LARS data, it will be returned to be deleted.
     *
     * @param fromLars the {@link Hardware} retrieved from LARS
     * @param inDb     the {@link Hardware} already existing in the DB
     * @return a {@link Collection} of the {@link Hardware} to be deleted
     */
    public static Collection<Hardware> toDelete(final Collection<Hardware> fromLars, final Collection<Hardware> inDb) {
        final TreeSet<Hardware> lars = new TreeSet<>(HardwareNameComparator.create());
        lars.addAll(fromLars);

        final TreeSet<Hardware> db = new TreeSet<>(HardwareNameComparator.create());
        db.addAll(inDb);

        db.removeAll(lars);
        LOGGER.info("{} from LARS, {} in DB, {} to delete", fromLars.size(), inDb.size(), db.size());
        return db;
    }

    /**
     * If any of the {@link Hardware} from LARS <b>does</b> already exist in the DB, and the {@code multiplier} or {@code averagePpd} is not the same,
     * it will be returned to be updated.
     *
     * @param fromLars the {@link Hardware} retrieved from LARS
     * @param inDb     the {@link Hardware} already existing in the DB
     * @return a {@link Map} of the {@link Hardware} to be updated, where the key is the LARS {@link Hardware} and value is the DB {@link Hardware}
     */
    public static Map<Hardware, Hardware> toUpdate(final Collection<Hardware> fromLars, final Collection<Hardware> inDb) {
        final Map<Hardware, Hardware> larsHardwareAlreadyInDb = new HashMap<>();

        // TODO: [zodac] Make this less shit
        for (final Hardware lars : fromLars) {
            for (final Hardware db : inDb) {
                if (db.getHardwareName().equalsIgnoreCase(lars.getHardwareName())) {
                    larsHardwareAlreadyInDb.put(lars, db);
                    break;
                }
            }
        }

        final Map<Hardware, Hardware> toUpdate = new HashMap<>();
        for (final Map.Entry<Hardware, Hardware> entry : larsHardwareAlreadyInDb.entrySet()) {
            final Hardware updatedHardware = entry.getKey();
            final Hardware existingHardware = entry.getValue();

            // Using BigDecimal since equality checks with doubles can be imprecise
            final BigDecimal updatedMultiplier = BigDecimal.valueOf(updatedHardware.getMultiplier());
            final BigDecimal existingMultiplier = BigDecimal.valueOf(existingHardware.getMultiplier());

            // We know the name is already equal, now we check multiplier and average PPD
            // If the stats have not changed since the last update, no need to update again now
            if (!updatedMultiplier.equals(existingMultiplier) || updatedHardware.getAveragePpd() != existingHardware.getAveragePpd()) {
                toUpdate.put(updatedHardware, existingHardware);
            }
        }

        LOGGER.info("{} from LARS, {} in DB, {} to update", fromLars.size(), inDb.size(), toUpdate.size());
        return toUpdate;
    }
}

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

package me.zodac.folding.lars;

import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.util.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Utility class used to filter {@link Collection}s of {@link Hardware}.
 *
 * <p>
 * Since we know the {@code hardwareName} of a {@link Hardware} is unique, we can use {@link HardwareNameComparator} to compare.
 */
public final class HardwareSplitter {

    private static final Logger LARS_LOGGER = LogManager.getLogger("lars");

    private HardwareSplitter() {

    }

    /**
     * If any of the {@link Hardware} from LARS does <b>not</b> already exist in the DB, it will be returned to be created.
     *
     * @param fromLars the {@link Hardware} retrieved from LARS
     * @param inDb     the {@link Hardware} already existing in the DB
     * @return a {@link Collection} of the {@link Hardware} to be created, sorted according to {@link HardwareNameComparator}
     */
    public static Collection<Hardware> toCreate(final Collection<Hardware> fromLars, final Collection<Hardware> inDb) {
        // Hardware name is unique, we can use it as an identifier
        final Collection<String> fromLarsHardwareNames = getHardwareNames(fromLars);
        final Collection<String> inDbHardwareNames = getHardwareNames(inDb);

        final Set<String> hardwareNamesOnlyInLars = CollectionUtils.existsInFirstOnly(fromLarsHardwareNames, inDbHardwareNames);

        final Set<Hardware> toCreate = fromLars
            .stream()
            .filter(hardwareFromLars -> hardwareNamesOnlyInLars.contains(hardwareFromLars.hardwareName()))
            .collect(toCollection(() -> new TreeSet<>(HardwareNameComparator.create())));

        LARS_LOGGER.info("{} from LARS, {} in DB, {} to create", fromLars.size(), inDb.size(), toCreate.size());
        return toCreate;
    }

    /**
     * If any of the {@link Hardware} in the DB is not also listed in the LARS data, it will be returned to be deleted.
     *
     * @param fromLars the {@link Hardware} retrieved from LARS
     * @param inDb     the {@link Hardware} already existing in the DB
     * @return a {@link Collection} of the {@link Hardware} to be deleted, sorted according to {@link HardwareNameComparator}
     */
    public static Collection<Hardware> toDelete(final Collection<Hardware> fromLars, final Collection<Hardware> inDb) {
        // Hardware name is unique, we can use it as an identifier
        final Collection<String> fromLarsHardwareNames = getHardwareNames(fromLars);
        final Collection<String> inDbHardwareNames = getHardwareNames(inDb);

        final Set<String> hardwareNamesNoLongerInLars = CollectionUtils.existsInFirstOnly(inDbHardwareNames, fromLarsHardwareNames);

        final Set<Hardware> toDelete = inDb
            .stream()
            .filter(hardwareInDb -> hardwareNamesNoLongerInLars.contains(hardwareInDb.hardwareName()))
            .collect(toCollection(() -> new TreeSet<>(HardwareNameComparator.create())));
        LARS_LOGGER.info("{} from LARS, {} in DB, {} to delete", fromLars.size(), inDb.size(), toDelete.size());
        return toDelete;
    }

    private static Collection<String> getHardwareNames(final Collection<Hardware> hardwares) {
        return hardwares
            .stream()
            .map(Hardware::hardwareName)
            .collect(toSet());
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
        final Map<String, Hardware> fromLarsHardware = new HashMap<>();
        for (final Hardware hardwareFromLars : fromLars) {
            fromLarsHardware.putIfAbsent(hardwareFromLars.hardwareName().toLowerCase(Locale.UK), hardwareFromLars);
        }

        final Map<String, Hardware> inDbHardware = inDb
            .stream()
            .collect(toMap(hardware -> hardware.hardwareName().toLowerCase(Locale.UK), hardware -> hardware));

        fromLarsHardware.keySet().retainAll(inDbHardware.keySet());
        final Map<Hardware, Hardware> larsHardwareAlreadyInDb = fromLarsHardware.entrySet()
            .stream()
            .collect(toMap(Map.Entry::getValue, e -> inDbHardware.get(e.getKey())));

        final Map<Hardware, Hardware> toUpdate = new HashMap<>();
        for (final Map.Entry<Hardware, Hardware> entry : larsHardwareAlreadyInDb.entrySet()) {
            final Hardware updatedHardware = entry.getKey();
            final Hardware existingHardware = entry.getValue();

            // Using BigDecimal since equality checks with doubles can be imprecise
            final BigDecimal updatedMultiplier = BigDecimal.valueOf(updatedHardware.multiplier());
            final BigDecimal existingMultiplier = BigDecimal.valueOf(existingHardware.multiplier());

            // We know the name is already equal, now we check multiplier and average PPD
            // If the stats have not changed since the last update, no need to update again now
            if (updatedMultiplier.compareTo(existingMultiplier) != 0 || updatedHardware.averagePpd() != existingHardware.averagePpd()) {
                toUpdate.put(updatedHardware, existingHardware);
            }
        }

        LARS_LOGGER.info("{} from LARS, {} in DB, {} to update", fromLars.size(), inDb.size(), toUpdate.size());
        return toUpdate;
    }
}

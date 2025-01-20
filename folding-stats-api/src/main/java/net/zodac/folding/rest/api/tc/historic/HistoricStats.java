/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2025 zodac.net
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package net.zodac.folding.rest.api.tc.historic;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import net.zodac.folding.api.tc.User;

/**
 * POJO defining historic {@code Team Competition} stats (hourly, daily, monthly, etc.) for a {@link User}.
 *
 * @param dateTime         the {@link LocalDateTime} of the stats
 * @param points           the points
 * @param multipliedPoints the multiplied points
 * @param units            the units
 */
public record HistoricStats(LocalDateTime dateTime,
                            long points,
                            long multipliedPoints,
                            int units
) {

    private static final long DEFAULT_POINTS = 0L;
    private static final long DEFAULT_MULTIPLIED_POINTS = 0L;
    private static final int DEFAULT_UNITS = 0;

    /**
     * Creates an instance of {@link HistoricStats}.
     *
     * @param dateTime         the {@link LocalDateTime} of the stats
     * @param points           the points
     * @param multipliedPoints the multiplied points
     * @param units            the units
     * @return the {@link HistoricStats} instance
     */
    public static HistoricStats create(final LocalDateTime dateTime, final long points, final long multipliedPoints, final int units) {
        return new HistoricStats(dateTime.truncatedTo(ChronoUnit.HOURS), points, multipliedPoints, units);
    }

    /**
     * Combines the values of the provided {@link HistoricStats} instances.
     *
     * <p>
     * Since each {@link User} may have stats on different days (meaning one may have stats on the 4th of the month, but
     * another may not have been added until the 5th), we take the key {@link LocalDateTime} from each {@link HistoricStats} and combine them into a
     * new {@link Map} with all available {@link LocalDateTime}s as the {@link Map#keySet()}.
     *
     * <p>
     * We then iterate over all instances for all users, and whenever one matches a key, we combine the results into a new {@link HistoricStats}
     * instance that is added to the output {@link Map}.
     *
     * @param allStats the {@link HistoricStats} for multiple {@link User}s and {@link LocalDateTime}s
     * @return a {@link Collection} of combined {@link HistoricStats}
     */
    public static Collection<HistoricStats> combine(final Collection<HistoricStats> allStats) {
        final Set<LocalDateTime> keys = allStats.stream()
            .map(HistoricStats::dateTime)
            .sorted()
            .collect(Collectors.toCollection(TreeSet::new));

        final Collection<HistoricStats> combinedStats = new ArrayList<>();

        for (final LocalDateTime key : keys) {
            long combinedPoints = DEFAULT_POINTS;
            long combinedMultipliedPoints = DEFAULT_MULTIPLIED_POINTS;
            int combinedUnits = DEFAULT_UNITS;

            for (final HistoricStats stats : allStats) {
                if (stats.dateTime.equals(key)) {
                    combinedPoints += stats.points;
                    combinedMultipliedPoints += stats.multipliedPoints;
                    combinedUnits += stats.units;
                }
            }

            combinedStats.add(create(key, combinedPoints, combinedMultipliedPoints, combinedUnits));
        }

        return combinedStats;
    }
}

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

package me.zodac.folding.rest.api.tc.historic;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * POJO defining historic {@code Team Competition} stats (hourly, daily, monthly, etc.) for a {@link me.zodac.folding.api.tc.User}.
 */
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Setter
@EqualsAndHashCode
@ToString(doNotUseGetters = true)
public class HistoricStats {

    private static final long DEFAULT_POINTS = 0L;
    private static final long DEFAULT_MULTIPLIED_POINTS = 0L;
    private static final int DEFAULT_UNITS = 0;

    private LocalDateTime dateTime;
    private long points;
    private long multipliedPoints;
    private int units;

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
     * Since each {@link me.zodac.folding.api.tc.User} may have stats on different days (meaning one may have stats on the 4th of the month, but
     * another may not have been added until the 5th), we take the key {@link LocalDateTime} from each {@link HistoricStats} and combine them into a
     * new {@link Map} with all available {@link LocalDateTime}s as the {@link Map#keySet()}.
     *
     * <p>
     * We then iterate over all instances for all users, and whenever one matches a key, we combine the results into a new {@link HistoricStats}
     * instance which is added to the output {@link Map}.
     *
     * @param allStats the {@link HistoricStats} for multiple {@link me.zodac.folding.api.tc.User}s and {@link LocalDateTime}s
     * @return a {@link Collection} of combined {@link HistoricStats}
     */
    public static Collection<HistoricStats> combine(final Collection<? extends HistoricStats> allStats) {
        final Set<LocalDateTime> keys = allStats.stream()
            .map(HistoricStats::getDateTime)
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

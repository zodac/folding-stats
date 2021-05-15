package me.zodac.folding.rest.api.tc.historic;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * POJO defining historic <code>Team Competition</code> stats (hourly, daily, monthly, etc.) for a user.
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
    private static final HistoricStats EMPTY_HISTORIC_STATS = empty();

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
     * Creates an empty instance of {@link HistoricStats} with default values.
     *
     * @return {@link HistoricStats} with default values
     */
    public static HistoricStats empty() {
        return new HistoricStats(LocalDateTime.now(ZoneOffset.UTC), DEFAULT_POINTS, DEFAULT_MULTIPLIED_POINTS, DEFAULT_UNITS);
    }

    /**
     * Combines the values of the provided {@link HistoricStats} instances. Each {@link me.zodac.folding.api.tc.User}'s instance should be provided
     * in a {@link Map} keyed by the {@link LocalDateTime} of the instance.
     * <p>
     * Since each {@link me.zodac.folding.api.tc.User} may have stats on different days (meaning one may have stats on the 4th of the month, but
     * another may not have been added until the 5th), we take the key {@link LocalDateTime} from each {@link Map} and combine them into a new {@link Map}
     * with all available keys.
     * <p>
     * We then iterate over all instances for all users, and whenever one matches a key, we combine the results into a new {@link HistoricStats}
     * instance which is added to the output {@link Map}.
     *
     * @param allStats the {@link HistoricStats} for multiple {@link me.zodac.folding.api.tc.User}s, where each {@link me.zodac.folding.api.tc.User}'s
     *                 stats are in a {@link Map} keyed by the {@link LocalDateTime}
     * @return a single {@link Map} of combined {@link HistoricStats} keyed by the {@link LocalDateTime}
     */
    public static Map<LocalDateTime, HistoricStats> combine(final HistoricStats... allStats) {
        final Set<LocalDateTime> keys = Arrays.stream(allStats)
                .map(HistoricStats::getDateTime)
                .collect(Collectors.toCollection(TreeSet::new));


        final Map<LocalDateTime, HistoricStats> combinedStats = new TreeMap<>();

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

            combinedStats.put(key, create(key, combinedPoints, combinedMultipliedPoints, combinedUnits));
        }

        return combinedStats;
    }
}

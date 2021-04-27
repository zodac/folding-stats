package me.zodac.folding.rest.tc.historic;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import me.zodac.folding.api.tc.stats.UserTcStats;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Setter
@EqualsAndHashCode
@ToString(doNotUseGetters = true)
public class DailyStats {

    private LocalDate date;
    private long points;
    private long multipliedPoints;
    private int units;

    public static DailyStats createFromTcStats(final LocalDate date, final UserTcStats userTcStats) {
        return new DailyStats(date, userTcStats.getPoints(), userTcStats.getMultipliedPoints(), userTcStats.getUnits());
    }
}

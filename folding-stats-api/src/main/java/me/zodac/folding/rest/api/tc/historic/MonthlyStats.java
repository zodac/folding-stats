package me.zodac.folding.rest.api.tc.historic;

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
public class MonthlyStats {

    private int monthNumber;
    private long points;
    private long multipliedPoints;
    private int units;

    public static MonthlyStats createFromTcStats(final LocalDate date, final UserTcStats userTcStats) {
        return new MonthlyStats(date.getMonth().getValue(), userTcStats.getPoints(), userTcStats.getMultipliedPoints(), userTcStats.getUnits());
    }
}

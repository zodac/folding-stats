package me.zodac.folding.rest.api.tc.historic;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

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

    public static DailyStats create(final LocalDate date, final long points, final long multipliedPoints, final int units) {
        return new DailyStats(date, points, multipliedPoints, units);
    }
}

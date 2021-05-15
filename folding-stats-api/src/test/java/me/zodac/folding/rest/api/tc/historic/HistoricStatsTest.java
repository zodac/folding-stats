package me.zodac.folding.rest.api.tc.historic;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link HistoricStats}.
 */
public class HistoricStatsTest {

    @Test
    public void testCombine() {
        final HistoricStats zero = HistoricStats.create(LocalDateTime.of(2020, 1, 25, 1, 1), 300L, 3_000L, 30);
        final HistoricStats first = HistoricStats.create(LocalDateTime.of(2020, 1, 1, 1, 1), 500L, 5_000L, 50);
        final HistoricStats second = HistoricStats.create(LocalDateTime.of(2020, 1, 1, 1, 1), 100L, 1_000L, 10);
        final HistoricStats third = HistoricStats.create(LocalDateTime.of(2020, 1, 2, 1, 1), 200L, 2_000L, 20);

        final Map<LocalDateTime, HistoricStats> result = HistoricStats.combine(zero, first, second, third);

        final LocalDateTime zeroDateTime = zero.getDateTime().truncatedTo(ChronoUnit.HOURS);
        final LocalDateTime firstDateTime = first.getDateTime().truncatedTo(ChronoUnit.HOURS);
        final LocalDateTime thirdDateTime = third.getDateTime().truncatedTo(ChronoUnit.HOURS);

        // Verify same dates are combined
        assertThat(result)
                .hasSize(3)
                .containsKey(zeroDateTime)
                .containsKey(firstDateTime)
                .containsKey(thirdDateTime);

        // Verify values are combined in same date
        final HistoricStats expectedZero = HistoricStats.create(zeroDateTime, 300L, 3_000L, 30);
        final HistoricStats expectedFirst = HistoricStats.create(firstDateTime, 600L, 6_000L, 60);
        final HistoricStats expectedThird = HistoricStats.create(thirdDateTime, 200L, 2_000L, 20);

        assertThat(result.get(firstDateTime))
                .isEqualTo(expectedFirst);

        assertThat(result.get(thirdDateTime))
                .isEqualTo(expectedThird);

        // Verify combined dates are sorted
        final Iterator<HistoricStats> it = result.values().iterator();
        int count = 1;
        while (it.hasNext()) {
            if (count != 3) {
                it.next();
                count++;
                continue;
            }

            assertThat(it.next())
                    .isEqualTo(expectedZero);
        }

    }
}

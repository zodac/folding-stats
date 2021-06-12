package me.zodac.folding.rest.api.tc.historic;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link HistoricStats}.
 */
class HistoricStatsTest {

    @Test
    void testCombine() {
        final int year = 2020;
        final int month = Month.JANUARY.getValue();
        final int hour = 1;
        final int minute = 1;
        
        final HistoricStats zero = HistoricStats.create(LocalDateTime.of(year, month, 25, hour, minute), 300L, 3_000L, 30);
        final HistoricStats first = HistoricStats.create(LocalDateTime.of(year, month, 1, hour, minute), 500L, 5_000L, 50);
        final HistoricStats second = HistoricStats.create(LocalDateTime.of(year, month, 1, hour, minute), 100L, 1_000L, 10);
        final HistoricStats third = HistoricStats.create(LocalDateTime.of(year, month, 2, hour, minute), 200L, 2_000L, 20);

        final Collection<HistoricStats> result = HistoricStats.combine(List.of(zero, first, second, third));

        final LocalDateTime zeroDateTime = zero.getDateTime().truncatedTo(ChronoUnit.HOURS);
        final LocalDateTime firstDateTime = first.getDateTime().truncatedTo(ChronoUnit.HOURS);
        final LocalDateTime thirdDateTime = third.getDateTime().truncatedTo(ChronoUnit.HOURS);

        // Verify same dates are combined
        assertThat(result)
            .hasSize(3);

        // Verify values are combined in same date
        final HistoricStats expectedZero = HistoricStats.create(zeroDateTime, 300L, 3_000L, 30);
        final HistoricStats expectedFirst = HistoricStats.create(firstDateTime, 600L, 6_000L, 60);
        final HistoricStats expectedThird = HistoricStats.create(thirdDateTime, 200L, 2_000L, 20);

        final Iterator<HistoricStats> results = result.iterator();
        int count = 1;

        while (results.hasNext()) {
            final HistoricStats historicStats = results.next();

            // Verify combined dates are sorted
            switch (count) {
                case 1:
                    assertThat(historicStats)
                        .isEqualTo(expectedFirst);
                    break;
                case 2:
                    assertThat(historicStats)
                        .isEqualTo(expectedThird);
                    break;
                case 3:
                    assertThat(historicStats)
                        .isEqualTo(expectedZero);
                    break;
                default:
                    throw new AssertionError("Unexpected entry in results: " + count);
            }

            count++;
        }
    }
}

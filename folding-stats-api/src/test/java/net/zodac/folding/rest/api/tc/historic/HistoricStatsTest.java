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
        assertThat(zero.points())
            .isEqualTo(300L);
        assertThat(zero.multipliedPoints())
            .isEqualTo(3_000L);
        assertThat(zero.units())
            .isEqualTo(30);

        final HistoricStats first = HistoricStats.create(LocalDateTime.of(year, month, 1, hour, minute), 500L, 5_000L, 50);
        final HistoricStats second = HistoricStats.create(LocalDateTime.of(year, month, 1, hour, minute), 100L, 1_000L, 10);
        final HistoricStats third = HistoricStats.create(LocalDateTime.of(year, month, 2, hour, minute), 200L, 2_000L, 20);

        final Collection<HistoricStats> result = HistoricStats.combine(List.of(zero, first, second, third));

        final LocalDateTime zeroDateTime = zero.dateTime().truncatedTo(ChronoUnit.HOURS);
        final LocalDateTime firstDateTime = first.dateTime().truncatedTo(ChronoUnit.HOURS);
        final LocalDateTime thirdDateTime = third.dateTime().truncatedTo(ChronoUnit.HOURS);

        // Verify same dates are combined
        assertThat(result)
            .as("Expected stats of the same date to be combined")
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
            switch (count) { // NOPMD: SwitchStmtsShouldHaveDefault - false positive
                case 1 -> assertThat(historicStats)
                    .isEqualTo(expectedFirst);
                case 2 -> assertThat(historicStats)
                    .isEqualTo(expectedThird);
                case 3 -> assertThat(historicStats)
                    .isEqualTo(expectedZero);
                default -> throw new AssertionError("Unexpected entry in results: " + count);
            }

            count++;
        }
    }
}

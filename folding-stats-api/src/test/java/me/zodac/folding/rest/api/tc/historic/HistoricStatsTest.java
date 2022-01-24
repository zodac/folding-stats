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
        assertThat(zero.getPoints())
            .isEqualTo(300L);
        assertThat(zero.getMultipliedPoints())
            .isEqualTo(3_000L);
        assertThat(zero.getUnits())
            .isEqualTo(30);

        final HistoricStats first = HistoricStats.create(LocalDateTime.of(year, month, 1, hour, minute), 500L, 5_000L, 50);
        final HistoricStats second = HistoricStats.create(LocalDateTime.of(year, month, 1, hour, minute), 100L, 1_000L, 10);
        final HistoricStats third = HistoricStats.create(LocalDateTime.of(year, month, 2, hour, minute), 200L, 2_000L, 20);

        final Collection<HistoricStats> result = HistoricStats.combine(List.of(zero, first, second, third));

        final LocalDateTime zeroDateTime = zero.getDateTime().truncatedTo(ChronoUnit.HOURS);
        final LocalDateTime firstDateTime = first.getDateTime().truncatedTo(ChronoUnit.HOURS);
        final LocalDateTime thirdDateTime = third.getDateTime().truncatedTo(ChronoUnit.HOURS);

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

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

package me.zodac.folding.api.tc.stats;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import me.zodac.folding.api.util.DateTimeConverterUtils;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link UserStats}.
 */
class UserStatsTest {

    @Test
    void testCreate() {
        final Timestamp timestamp = DateTimeConverterUtils.toTimestamp(LocalDateTime.now());
        final UserStats userStats = UserStats.create(1, timestamp, 5L, 1);

        assertThat(userStats.timestamp())
            .isNotNull();

        assertThat(userStats)
            .extracting("userId", "timestamp", "points", "units")
            .containsExactly(1, timestamp, 5L, 1);
    }

    @Test
    void testCreateNow() {
        final UserStats userStats = UserStats.createNow(2, 5L, 1);

        assertThat(userStats.timestamp())
            .isNotNull();

        assertThat(userStats)
            .extracting("userId", "points", "units")
            .containsExactly(2, 5L, 1);
    }

    @Test
    void testEmpty() {
        final UserStats userStats = UserStats.createNow(1, 5L, 1);
        assertThat(userStats.isEmpty())
            .isFalse();

        final UserStats emptyUserStats = UserStats.empty();
        assertThat(emptyUserStats.isEmpty())
            .isTrue();
    }

    @Test
    void testEmptyStats() {
        final UserStats userStats = UserStats.createNow(1, 1L, 1);
        assertThat(userStats.isEmpty())
            .isFalse();
        assertThat(userStats.isEmptyStats())
            .isFalse();

        final UserStats emptyUserStats = UserStats.createNow(1, 0L, 0);
        assertThat(emptyUserStats.isEmpty())
            .isFalse();
        assertThat(emptyUserStats.isEmptyStats())
            .isTrue();
    }
}

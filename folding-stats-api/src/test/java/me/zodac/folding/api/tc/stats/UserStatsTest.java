/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2025 zodac.me
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

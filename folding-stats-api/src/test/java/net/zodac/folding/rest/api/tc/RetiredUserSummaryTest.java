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

package net.zodac.folding.rest.api.tc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import net.zodac.folding.api.tc.stats.RetiredUserTcStats;
import net.zodac.folding.api.tc.stats.UserTcStats;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link RetiredUserSummary}.
 */
class RetiredUserSummaryTest {

    @Test
    void testCreate() {
        final RetiredUserSummary retiredUserSummary = RetiredUserSummary.create(1, "user", 5L, 500L, 1, 2);

        assertThat(retiredUserSummary)
            .extracting("id", "displayName", "points", "multipliedPoints", "units", "rankInTeam")
            .containsExactly(1, "user", 5L, 500L, 1, 2);
    }

    @Test
    void testCreate_blankDisplayName() {
        assertThatThrownBy(() -> RetiredUserSummary.create(1, "", 5L, 500L, 1, 2))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("displayName");
    }

    @Test
    void testCreateWithDefaultRank() {
        final UserTcStats userTcStats = UserTcStats.createNow(3, 5L, 500L, 1);
        final RetiredUserTcStats retiredUserTcStats = RetiredUserTcStats.create(2, 2, "user1", userTcStats);
        final RetiredUserSummary retiredUserSummary = RetiredUserSummary.createWithDefaultRank(retiredUserTcStats);

        assertThat(retiredUserSummary)
            .extracting("id", "displayName", "points", "multipliedPoints", "units", "rankInTeam")
            .containsExactly(2, "user1", 5L, 500L, 1, 1);
    }

    @Test
    void testCreateWithStats() {
        final UserTcStats userTcStats = UserTcStats.createNow(1, 5L, 500L, 1);
        final RetiredUserTcStats retiredUserTcStats = RetiredUserTcStats.create(2, 2, "user", userTcStats);
        final RetiredUserSummary retiredUserSummary = RetiredUserSummary.createWithStats(retiredUserTcStats, 4);

        assertThat(retiredUserSummary)
            .extracting("id", "displayName", "points", "multipliedPoints", "units", "rankInTeam")
            .containsExactly(2, "user", 5L, 500L, 1, 4);
    }

    @Test
    void testUpdateRank() {
        final RetiredUserSummary retiredUserSummary = RetiredUserSummary.create(1, "user", 5L, 500L, 1, 1);

        assertThat(retiredUserSummary.rankInTeam())
            .isOne();

        final RetiredUserSummary updatedRetiredUserSummary = retiredUserSummary.updateWithNewRank(2);
        assertThat(updatedRetiredUserSummary.rankInTeam())
            .isEqualTo(2);
    }
}

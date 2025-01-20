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

package net.zodac.folding.api.tc.stats;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link RetiredUserTcStats}.
 */
class RetiredUserTcStatsTest {

    @Test
    void testCreate() {
        final RetiredUserTcStats retiredUserTcStats = createTestUser("user");

        assertThat(retiredUserTcStats)
            .extracting("retiredUserId", "teamId", "displayName", "points", "multipliedPoints", "units")
            .containsExactly(1, 1, "user", 5L, 500L, 1);
    }

    @Test
    void testCreate_blankDisplayName() {
        assertThatThrownBy(() -> createTestUser(""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("displayName");
    }

    @Test
    void testCreateWithoutId_andUpdateWithId() {
        final RetiredUserTcStats retiredUserTcStats = RetiredUserTcStats.createWithoutId(1, "user", UserTcStats.createNow(1, 5L, 500L, 1));
        assertThat(retiredUserTcStats.retiredUserId())
            .isEqualTo(RetiredUserTcStats.EMPTY_RETIRED_USER_ID);

        final RetiredUserTcStats updatedRetiredUserTcStats = RetiredUserTcStats.updateWithId(3, retiredUserTcStats);
        assertThat(updatedRetiredUserTcStats.retiredUserId())
            .isEqualTo(3);
    }

    private static RetiredUserTcStats createTestUser(final String displayName) {
        return RetiredUserTcStats.create(1, 1, displayName, UserTcStats.createNow(1, 5L, 500L, 1));
    }
}

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

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link RetiredUserTcStats}.
 */
class RetiredUserTcStatsTest {

    @Test
    void testCreate() {
        final RetiredUserTcStats retiredUserTcStats = RetiredUserTcStats.create(1, 1, "user", UserTcStats.createNow(1, 5L, 500L, 1));

        assertThat(retiredUserTcStats)
            .extracting("retiredUserId", "teamId", "displayName", "points", "multipliedPoints", "units")
            .containsExactly(1, 1, "user", 5L, 500L, 1);
    }

    @Test
    void testCreateWithoutId_andUpdateWithId() {
        final RetiredUserTcStats retiredUserTcStats = RetiredUserTcStats.createWithoutId(1, "user", UserTcStats.createNow(1, 5L, 500L, 1));
        assertThat(retiredUserTcStats.getRetiredUserId())
            .isEqualTo(RetiredUserTcStats.EMPTY_RETIRED_USER_ID);

        final RetiredUserTcStats updatedRetiredUserTcStats = RetiredUserTcStats.updateWithId(3, retiredUserTcStats);
        assertThat(updatedRetiredUserTcStats.getRetiredUserId())
            .isEqualTo(3);
    }
}

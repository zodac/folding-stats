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

package me.zodac.folding.rest.api.tc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import me.zodac.folding.api.tc.Category;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.HardwareMake;
import me.zodac.folding.api.tc.HardwareType;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link UserSummary}.
 */
class UserSummaryTest {

    @Test
    void testCreate_defaultRank() {
        final User user = createUser();
        final UserSummary userSummary = UserSummary.createWithDefaultRank(user, 10L, 1_000L, 2);

        assertThat(userSummary.getUser())
            .isEqualTo(user);

        assertThat(userSummary.getRankInTeam())
            .isOne();

        assertThat(userSummary)
            .extracting("points", "multipliedPoints", "units")
            .containsExactly(10L, 1_000L, 2);
    }

    @Test
    void testCreate_rank2() {
        final User user = createUser();
        final UserSummary userSummary = UserSummary.create(user, 5L, 500L, 1, 2);

        assertThat(userSummary.getUser())
            .isEqualTo(user);

        assertThat(userSummary.getRankInTeam())
            .isEqualTo(2);

        assertThat(userSummary)
            .extracting("points", "multipliedPoints", "units")
            .containsExactly(5L, 500L, 1);
    }

    @Test
    void testCreate_nullUser() {
        assertThatThrownBy(
            () -> UserSummary.createWithDefaultRank(null, 5L, 500L, 1))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testUpdateRank() {
        final User user = createUser();
        final UserSummary userSummary = UserSummary.createWithDefaultRank(user, 5L, 500L, 1);

        assertThat(userSummary.getRankInTeam())
            .isOne();

        final UserSummary updatedUserSummary = userSummary.updateWithNewRank(2);
        assertThat(updatedUserSummary.getRankInTeam())
            .isEqualTo(2);
    }

    private static User createUser() {
        final Hardware hardware = Hardware.create(1, "hardware", "hardware", HardwareMake.AMD, HardwareType.GPU, 1.0D, 1L);
        final Team team = Team.create(1, "team", "teamDescription", "https://google.com");

        return User.create(1, "user", "user", "passkey", Category.AMD_GPU, "", "", hardware, team, true);
    }
}

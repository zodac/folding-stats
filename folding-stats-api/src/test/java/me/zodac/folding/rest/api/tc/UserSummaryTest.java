/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2023 zodac.me
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

package me.zodac.folding.rest.api.tc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import me.zodac.folding.api.tc.Category;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.HardwareMake;
import me.zodac.folding.api.tc.HardwareType;
import me.zodac.folding.api.tc.Role;
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

        assertThat(userSummary.user())
            .isEqualTo(user);

        assertThat(userSummary.rankInTeam())
            .isOne();

        assertThat(userSummary)
            .extracting("points", "multipliedPoints", "units")
            .containsExactly(10L, 1_000L, 2);
    }

    @Test
    void testCreate_rank2() {
        final User user = createUser();
        final UserSummary userSummary = UserSummary.create(user, 5L, 500L, 1, 2);

        assertThat(userSummary.user())
            .isEqualTo(user);

        assertThat(userSummary.rankInTeam())
            .isEqualTo(2);

        assertThat(userSummary)
            .extracting("points", "multipliedPoints", "units")
            .containsExactly(5L, 500L, 1);
    }

    @Test
    void testCreate_nullUser() {
        assertThatThrownBy(
            () -> UserSummary.createWithDefaultRank(null, 5L, 500L, 1))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("user");
    }

    @Test
    void testUpdateRank() {
        final User user = createUser();
        final UserSummary userSummary = UserSummary.createWithDefaultRank(user, 5L, 500L, 1);

        assertThat(userSummary.rankInTeam())
            .isOne();

        final UserSummary updatedUserSummary = userSummary.updateWithNewRank(2);
        assertThat(updatedUserSummary.rankInTeam())
            .isEqualTo(2);
    }

    private static User createUser() {
        final Hardware hardware = Hardware.create(1, "hardware", "hardware", HardwareMake.AMD, HardwareType.GPU, 1.0D, 1L);
        final Team team = Team.create(1, "team", "teamDescription", "https://google.com");

        return User.create(1, "user", "user", "passkey", Category.AMD_GPU, "", "", hardware, team, Role.CAPTAIN);
    }
}

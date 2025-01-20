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

package net.zodac.folding.rest.api.tc.leaderboard;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import net.zodac.folding.api.tc.Category;
import net.zodac.folding.api.tc.Hardware;
import net.zodac.folding.api.tc.HardwareMake;
import net.zodac.folding.api.tc.HardwareType;
import net.zodac.folding.api.tc.Role;
import net.zodac.folding.api.tc.Team;
import net.zodac.folding.api.tc.User;
import net.zodac.folding.rest.api.tc.TeamSummary;
import net.zodac.folding.rest.api.tc.UserSummary;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link TeamLeaderboardEntry}.
 */
class TeamLeaderboardEntryTest {

    @Test
    void testCreate_leader() {
        final User user = createUser();
        final UserSummary userSummary1 = UserSummary.create(user, 25L, 2_500L, 5, 1);
        final UserSummary userSummary2 = UserSummary.create(user, 20L, 2_000L, 4, 2);
        final TeamSummary teamSummary =
            TeamSummary.createWithDefaultRank(user.team(), "captain", List.of(userSummary1, userSummary2), List.of());

        final TeamLeaderboardEntry teamLeaderboardEntry = TeamLeaderboardEntry.createLeader(teamSummary);

        assertThat(teamLeaderboardEntry.teamPoints())
            .isEqualTo(45L);
        assertThat(teamLeaderboardEntry.teamMultipliedPoints())
            .isEqualTo(4_500L);
        assertThat(teamLeaderboardEntry.teamUnits())
            .isEqualTo(9);

        assertThat(teamLeaderboardEntry.rank())
            .isOne();

        assertThat(teamLeaderboardEntry.diffToLeader())
            .isEqualTo(teamLeaderboardEntry.diffToNext())
            .isZero();
    }

    @Test
    void testCreate_rank2() {
        final User user = createUser();
        final UserSummary userSummary1 = UserSummary.create(user, 10L, 1_000L, 2, 1);
        final UserSummary userSummary2 = UserSummary.create(user, 5L, 500L, 1, 2);
        final TeamSummary teamSummary =
            TeamSummary.createWithPoints(user.team(), "captain", 15L, 1_500L, 3, 2, List.of(userSummary1, userSummary2), List.of());

        final TeamLeaderboardEntry teamLeaderboardEntry = TeamLeaderboardEntry.create(teamSummary, 5_000L, 5_000L);

        assertThat(teamLeaderboardEntry.teamPoints())
            .isEqualTo(15L);
        assertThat(teamLeaderboardEntry.teamMultipliedPoints())
            .isEqualTo(1_500L);
        assertThat(teamLeaderboardEntry.teamUnits())
            .isEqualTo(3);

        assertThat(teamLeaderboardEntry.rank())
            .isEqualTo(2);

        assertThat(teamLeaderboardEntry.diffToLeader())
            .isEqualTo(teamLeaderboardEntry.diffToNext())
            .isEqualTo(5_000L);
    }

    private static User createUser() {
        final Hardware hardware = Hardware.create(1, "hardware", "hardware", HardwareMake.AMD, HardwareType.GPU, 1.0D, 1L);
        final Team team = Team.create(1, "team", "teamDescription", "https://google.com");

        return User.create(1, "user", "user", "passkey", Category.AMD_GPU, "", "", hardware, team, Role.CAPTAIN);
    }
}

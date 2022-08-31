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

package me.zodac.folding.rest.api.tc.leaderboard;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.List;
import me.zodac.folding.api.tc.Category;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.HardwareMake;
import me.zodac.folding.api.tc.HardwareType;
import me.zodac.folding.api.tc.Role;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.rest.api.tc.TeamSummary;
import me.zodac.folding.rest.api.tc.UserSummary;
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
            TeamSummary.createWithDefaultRank(user.team(), "captain", List.of(userSummary1, userSummary2), Collections.emptyList());

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
            TeamSummary.createWithPoints(user.team(), "captain", 15L, 1_500L, 3, 2, List.of(userSummary1, userSummary2), Collections.emptyList());

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

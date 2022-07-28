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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import me.zodac.folding.api.tc.Category;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.HardwareMake;
import me.zodac.folding.api.tc.HardwareType;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.tc.stats.RetiredUserTcStats;
import me.zodac.folding.api.tc.stats.UserTcStats;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link TeamSummary}.
 */
class TeamSummaryTest {

    @Test
    void testCreate_manualPoints() {
        final UserSummary userSummary1 = createUserSummary(1, 10);
        final UserSummary userSummary2 = createUserSummary(2, 5);

        final RetiredUserSummary retiredUserSummary = createRetiredUserSummary(3, 0);

        final Team team = userSummary1.getUser().team();
        final TeamSummary teamSummary =
            TeamSummary.createWithPoints(team, "captain", 75L, 7_500L, 15, 1, List.of(userSummary1, userSummary2), List.of(retiredUserSummary));

        assertThat(teamSummary.getTeam())
            .isEqualTo(team);

        assertThat(teamSummary.getActiveUsers())
            .containsExactly(userSummary1, userSummary2);

        assertThat(teamSummary.getRetiredUsers())
            .containsExactly(retiredUserSummary);

        assertThat(teamSummary.getRank())
            .isOne();

        assertThat(teamSummary)
            .extracting("teamPoints", "teamMultipliedPoints", "teamUnits")
            .containsExactly(75L, 7_500L, 15);
    }

    @Test
    void testCreate_calculatedPoints() {
        final UserSummary userSummary1 = createUserSummary(1, 10);
        final UserSummary userSummary2 = createUserSummary(2, 5);

        final RetiredUserSummary retiredUserSummary = createRetiredUserSummary(3, 1);

        final Team team = userSummary1.getUser().team();
        final TeamSummary teamSummary =
            TeamSummary.createWithDefaultRank(team, "captain", List.of(userSummary1, userSummary2), List.of(retiredUserSummary));

        assertThat(teamSummary.getActiveUsers())
            .containsExactly(userSummary1, userSummary2);

        assertThat(teamSummary.getRetiredUsers())
            .containsExactly(retiredUserSummary);

        assertThat(teamSummary.getRank())
            .isOne();

        assertThat(teamSummary)
            .extracting("teamPoints", "teamMultipliedPoints", "teamUnits")
            .containsExactly(80L, 8_000L, 16);
    }

    @Test
    void testCreate_retiredUsersAlwaysRankedBelowActiveUsers() {
        final UserSummary userSummary1 = createUserSummary(2, 10);
        final UserSummary userSummary2 = createUserSummary(4, 5);

        final RetiredUserSummary retiredUserSummary1 = createRetiredUserSummary(1, 20);
        final RetiredUserSummary retiredUserSummary2 = createRetiredUserSummary(3, 6);

        final Team team = userSummary1.getUser().team();
        final TeamSummary teamSummary = TeamSummary.createWithDefaultRank(team, "captain", List.of(userSummary1, userSummary2),
            List.of(retiredUserSummary1, retiredUserSummary2));

        final Collection<Integer> activeUserRanks = teamSummary.getActiveUsers()
            .stream()
            .map(UserSummary::getRankInTeam)
            .toList();
        assertThat(activeUserRanks)
            .containsExactly(1, 2);

        final Collection<Integer> retiredUserRanks = teamSummary.getRetiredUsers()
            .stream()
            .map(RetiredUserSummary::getRankInTeam)
            .toList();
        assertThat(retiredUserRanks)
            .containsExactly(3, 4);
    }

    @Test
    void testCreate_tiedUserPoints() {
        final UserSummary userSummary1 = createUserSummary(1, 10);
        final UserSummary userSummary2 = createUserSummary(1, 10);
        final UserSummary userSummary3 = createUserSummary(2, 5);
        final UserSummary userSummary4 = createUserSummary(3, 1);
        final UserSummary userSummary5 = createUserSummary(3, 1);

        final RetiredUserSummary retiredUserSummary1 = createRetiredUserSummary(1, 20);
        final RetiredUserSummary retiredUserSummary2 = createRetiredUserSummary(1, 20);
        final RetiredUserSummary retiredUserSummary3 = createRetiredUserSummary(1, 20);
        final RetiredUserSummary retiredUserSummary4 = createRetiredUserSummary(2, 6);
        final RetiredUserSummary retiredUserSummary5 = createRetiredUserSummary(3, 0);

        final Team team = userSummary1.getUser().team();
        final TeamSummary teamSummary = TeamSummary.createWithDefaultRank(team, "captain",
            List.of(
                userSummary1,
                userSummary2,
                userSummary3,
                userSummary4,
                userSummary5
            ),
            List.of(
                retiredUserSummary1,
                retiredUserSummary2,
                retiredUserSummary3,
                retiredUserSummary4,
                retiredUserSummary5
            )
        );

        final Collection<Integer> activeUserRanks = teamSummary.getActiveUsers()
            .stream()
            .map(UserSummary::getRankInTeam)
            .toList();
        assertThat(activeUserRanks)
            .containsExactly(1, 1, 3, 4, 4);

        final Collection<Integer> retiredUserRanks = teamSummary.getRetiredUsers()
            .stream()
            .map(RetiredUserSummary::getRankInTeam)
            .toList();
        assertThat(retiredUserRanks)
            .containsExactly(6, 6, 6, 9, 10);
    }

    @Test
    void testCreate_nullTeam() {
        assertThatThrownBy(
            () -> TeamSummary.createWithDefaultRank(null, "captainName", Collections.emptyList(), Collections.emptyList()))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testUpdateRank() {
        final UserSummary userSummary1 = createUserSummary(1, 10);
        final UserSummary userSummary2 = createUserSummary(2, 5);

        final RetiredUserSummary retiredUserSummary = createRetiredUserSummary(3, 1);

        final Team team = userSummary1.getUser().team();
        final TeamSummary teamSummary =
            TeamSummary.createWithDefaultRank(team, "captain", List.of(userSummary1, userSummary2), List.of(retiredUserSummary));

        assertThat(teamSummary.getRank())
            .isOne();

        final TeamSummary updatedTeamSummary = teamSummary.updateWithNewRank(2);
        assertThat(updatedTeamSummary.getRank())
            .isEqualTo(2);
    }

    private static UserSummary createUserSummary(final int rank, final int units) {
        final Hardware hardware = Hardware.create(1, "hardware", "hardware", HardwareMake.AMD, HardwareType.GPU, 1.0D, 1L);
        final Team team = Team.create(1, "team", "teamDescription", "https://google.com");
        final User user = User.create(1, "user", "user", "passkey", Category.AMD_GPU, "", "", hardware, team, true);

        return UserSummary.create(user, units * 5L, units * 500L, units, rank);
    }

    private static RetiredUserSummary createRetiredUserSummary(final int rank, final int units) {
        final int userId = 1;
        final UserTcStats userTcStats = UserTcStats.createNow(userId, units * 5L, units * 500L, units);
        final RetiredUserTcStats retiredUserTcStats = RetiredUserTcStats.create(userId, 1, "name", userTcStats);
        return RetiredUserSummary.createWithStats(retiredUserTcStats, rank);
    }
}

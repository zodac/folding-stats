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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import me.zodac.folding.api.tc.Category;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.HardwareMake;
import me.zodac.folding.api.tc.HardwareType;
import me.zodac.folding.api.tc.Role;
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

        final Team team = userSummary1.user().team();
        final TeamSummary teamSummary =
            TeamSummary.createWithPoints(team, "captain", 75L, 7_500L, 15, 1, List.of(userSummary1, userSummary2), List.of(retiredUserSummary));

        assertThat(teamSummary.team())
            .isEqualTo(team);

        assertThat(teamSummary.activeUsers())
            .containsExactly(userSummary1, userSummary2);

        assertThat(teamSummary.retiredUsers())
            .containsExactly(retiredUserSummary);

        assertThat(teamSummary.rank())
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

        final Team team = userSummary1.user().team();
        final TeamSummary teamSummary =
            TeamSummary.createWithDefaultRank(team, "captain", List.of(userSummary1, userSummary2), List.of(retiredUserSummary));

        assertThat(teamSummary.activeUsers())
            .containsExactly(userSummary1, userSummary2);

        assertThat(teamSummary.retiredUsers())
            .containsExactly(retiredUserSummary);

        assertThat(teamSummary.rank())
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

        final Team team = userSummary1.user().team();
        final TeamSummary teamSummary = TeamSummary.createWithDefaultRank(team, "captain", List.of(userSummary1, userSummary2),
            List.of(retiredUserSummary1, retiredUserSummary2));

        final Collection<Integer> activeUserRanks = teamSummary.activeUsers()
            .stream()
            .map(UserSummary::rankInTeam)
            .toList();
        assertThat(activeUserRanks)
            .containsExactly(1, 2);

        final Collection<Integer> retiredUserRanks = teamSummary.retiredUsers()
            .stream()
            .map(RetiredUserSummary::rankInTeam)
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

        final Team team = userSummary1.user().team();
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

        final Collection<Integer> activeUserRanks = teamSummary.activeUsers()
            .stream()
            .map(UserSummary::rankInTeam)
            .toList();
        assertThat(activeUserRanks)
            .containsExactly(1, 1, 3, 4, 4);

        final Collection<Integer> retiredUserRanks = teamSummary.retiredUsers()
            .stream()
            .map(RetiredUserSummary::rankInTeam)
            .toList();
        assertThat(retiredUserRanks)
            .containsExactly(6, 6, 6, 9, 10);
    }

    @Test
    void testCreate_nullTeam() {
        assertThatThrownBy(
            () -> TeamSummary.createWithDefaultRank(null, "captainName", Collections.emptyList(), Collections.emptyList()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("team");
    }

    @Test
    void testCreate_nullCaptainName_noException() {
        final UserSummary userSummary = createUserSummary(1, 10);
        final Team team = userSummary.user().team();
        final TeamSummary teamSummary = TeamSummary.createWithDefaultRank(team, null, Collections.emptyList(), Collections.emptyList());

        assertThat(teamSummary.captainName())
            .isNull();
    }

    @Test
    void testUpdateRank() {
        final UserSummary userSummary1 = createUserSummary(1, 10);
        final UserSummary userSummary2 = createUserSummary(2, 5);

        final RetiredUserSummary retiredUserSummary = createRetiredUserSummary(3, 1);

        final Team team = userSummary1.user().team();
        final TeamSummary teamSummary =
            TeamSummary.createWithDefaultRank(team, "captain", List.of(userSummary1, userSummary2), List.of(retiredUserSummary));

        assertThat(teamSummary.rank())
            .isOne();

        final TeamSummary updatedTeamSummary = teamSummary.updateWithNewRank(2);
        assertThat(updatedTeamSummary.rank())
            .isEqualTo(2);
    }

    private static UserSummary createUserSummary(final int rank, final int units) {
        final Hardware hardware = Hardware.create(1, "hardware", "hardware", HardwareMake.AMD, HardwareType.GPU, 1.0D, 1L);
        final Team team = Team.create(1, "team", "teamDescription", "https://google.com");
        final User user = User.create(1, "user", "user", "passkey", Category.AMD_GPU, "", "", hardware, team, Role.CAPTAIN);

        return UserSummary.create(user, units * 5L, units * 500L, units, rank);
    }

    private static RetiredUserSummary createRetiredUserSummary(final int rank, final int units) {
        final int userId = 1;
        final UserTcStats userTcStats = UserTcStats.createNow(userId, units * 5L, units * 500L, units);
        final RetiredUserTcStats retiredUserTcStats = RetiredUserTcStats.create(userId, 1, "name", userTcStats);
        return RetiredUserSummary.createWithStats(retiredUserTcStats, rank);
    }
}

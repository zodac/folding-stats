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
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link AllTeamsSummary}.
 */
class AllTeamSummaryTest {

    @Test
    void testCreate() {
        final TeamSummary teamSummary1 = createTeamSummary(
            createUserSummary(10),
            createUserSummary(20)
        );

        final TeamSummary teamSummary2 = createTeamSummary(
            createUserSummary(10),
            createUserSummary(20)
        );

        final TeamSummary teamSummary3 = createTeamSummary(
            createUserSummary(1),
            createUserSummary(2),
            createUserSummary(3),
            createUserSummary(4),
            createUserSummary(5)
        );

        final AllTeamsSummary allTeamsSummary = AllTeamsSummary.create(List.of(teamSummary1, teamSummary2, teamSummary3));

        assertThat(allTeamsSummary.competitionSummary())
            .extracting("totalPoints", "totalMultipliedPoints", "totalUnits")
            .containsExactly(375L, 37_500L, 75);

        final Collection<Integer> teamRanks = allTeamsSummary.teams()
            .stream()
            .map(TeamSummary::rank)
            .toList();
        assertThat(teamRanks)
            .containsExactly(1, 1, 3);
    }

    private static TeamSummary createTeamSummary(final UserSummary... userSummaries) {
        return TeamSummary.createWithDefaultRank(userSummaries[0].user().team(), "captain", List.of(userSummaries), Collections.emptyList());
    }

    private static UserSummary createUserSummary(final int units) {
        final Hardware hardware = Hardware.create(1, "hardware", "hardware", HardwareMake.AMD, HardwareType.GPU, 1.0D, 1L);
        final Team team = Team.create(1, "team", "teamDescription", "https://google.com");
        final User user = User.create(1, "user", "user", "passkey", Category.AMD_GPU, "", "", hardware, team, Role.CAPTAIN);

        return UserSummary.create(user, units * 5L, units * 500L, units, 1);
    }
}

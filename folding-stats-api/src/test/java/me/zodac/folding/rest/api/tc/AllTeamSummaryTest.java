/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2024 zodac.me
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

import java.util.Collection;
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
        return TeamSummary.createWithDefaultRank(userSummaries[0].user().team(), "captain", List.of(userSummaries), List.of());
    }

    private static UserSummary createUserSummary(final int units) {
        final Hardware hardware = Hardware.create(1, "hardware", "hardware", HardwareMake.AMD, HardwareType.GPU, 1.0D, 1L);
        final Team team = Team.create(1, "team", "teamDescription", "https://google.com");
        final User user = User.create(1, "user", "user", "passkey", Category.AMD_GPU, "", "", hardware, team, Role.CAPTAIN);

        return UserSummary.create(user, units * 5L, units * 500L, units, 1);
    }
}

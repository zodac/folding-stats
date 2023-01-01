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

package me.zodac.folding.api.tc.result;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import me.zodac.folding.api.tc.Category;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.HardwareMake;
import me.zodac.folding.api.tc.HardwareType;
import me.zodac.folding.api.tc.Role;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.rest.api.tc.TeamSummary;
import me.zodac.folding.rest.api.tc.UserSummary;
import me.zodac.folding.rest.api.tc.leaderboard.TeamLeaderboardEntry;
import me.zodac.folding.rest.api.tc.leaderboard.UserCategoryLeaderboardEntry;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link MonthlyResult}.
 */
class MonthlyResultTest {

    @Test
    void verifyEmpty() {
        final MonthlyResult monthlyResult = MonthlyResult.empty();

        assertThat(monthlyResult.teamLeaderboard())
            .isEmpty();

        final Map<Category, List<UserCategoryLeaderboardEntry>> userCategoryLeaderboard = monthlyResult.userCategoryLeaderboard();

        Set.of(
            Category.AMD_GPU,
            Category.NVIDIA_GPU,
            Category.WILDCARD
        ).forEach(category -> {
            assertThat(userCategoryLeaderboard)
                .containsKey(category);

            assertThat(userCategoryLeaderboard.get(category))
                .isEmpty();
        });
    }

    @Test
    void verifyHasNoStats() {
        final List<TeamLeaderboardEntry> teamLeaderboardEntries = new ArrayList<>();
        teamLeaderboardEntries.add(
            TeamLeaderboardEntry.create(
                TeamSummary.createWithDefaultRank(
                    Team.create(
                        Team.EMPTY_TEAM_ID,
                        "Team",
                        "",
                        ""
                    ),
                    "captain",
                    List.of(
                        UserSummary.createWithDefaultRank(
                            User.create(
                                User.EMPTY_USER_ID,
                                "foldingUserName",
                                "displayName",
                                "passkey",
                                Category.AMD_GPU,
                                "",
                                "",
                                Hardware.create(
                                    Hardware.EMPTY_HARDWARE_ID,
                                    "hardwareName",
                                    "displayName",
                                    HardwareMake.AMD,
                                    HardwareType.GPU,
                                    1.0D,
                                    1L
                                ),
                                Team.create(
                                    Team.EMPTY_TEAM_ID,
                                    "Team",
                                    "",
                                    ""
                                ),
                                Role.CAPTAIN
                            ),
                            5L,
                            500L,
                            1
                        )
                    ),
                    Collections.emptyList()
                ),
                0L, 0L
            )
        );

        final MonthlyResult monthlyResult = MonthlyResult.createWithCurrentDateTime(
            teamLeaderboardEntries,
            Collections.emptyMap()
        );

        assertThat(monthlyResult.hasNoStats())
            .isFalse();
        long totalTeamPoints = 0L;
        long totalTeamMultipliedPoints = 0L;
        int totalTeamUnits = 0;

        for (final TeamLeaderboardEntry teamLeaderboardEntry : monthlyResult.teamLeaderboard()) {
            totalTeamPoints += teamLeaderboardEntry.teamPoints();
            totalTeamMultipliedPoints += teamLeaderboardEntry.teamMultipliedPoints();
            totalTeamUnits += teamLeaderboardEntry.teamUnits();
        }
        assertThat(totalTeamPoints)
            .isEqualTo(5L);
        assertThat(totalTeamMultipliedPoints)
            .isEqualTo(500L);
        assertThat(totalTeamUnits)
            .isEqualTo(1);

        final MonthlyResult emptyMonthlyResult = MonthlyResult.empty();
        assertThat(emptyMonthlyResult.hasNoStats())
            .isTrue();
        long totalEmptyTeamPoints = 0L;
        long totalEmptyTeamMultipliedPoints = 0L;
        int totalEmptyTeamUnits = 0;

        for (final TeamLeaderboardEntry teamLeaderboardEntry : emptyMonthlyResult.teamLeaderboard()) {
            totalEmptyTeamPoints += teamLeaderboardEntry.teamPoints();
            totalEmptyTeamMultipliedPoints += teamLeaderboardEntry.teamMultipliedPoints();
            totalEmptyTeamUnits += teamLeaderboardEntry.teamUnits();
        }
        assertThat(totalEmptyTeamPoints)
            .isEqualTo(0L);
        assertThat(totalEmptyTeamMultipliedPoints)
            .isEqualTo(0L);
        assertThat(totalEmptyTeamUnits)
            .isEqualTo(0);
    }

    @Test
    void verifyUpdateEmptyCategories() {
        final MonthlyResult monthlyResult = MonthlyResult.createWithCurrentDateTime(
            Collections.emptyList(),
            Collections.emptyMap()
        );

        Set.of(
            Category.AMD_GPU,
            Category.NVIDIA_GPU,
            Category.WILDCARD
        ).forEach(category -> assertThat(monthlyResult.userCategoryLeaderboard())
            .doesNotContainKey(category));

        final MonthlyResult updatedMonthlyResult = MonthlyResult.updateWithEmptyCategories(monthlyResult);
        Set.of(
            Category.AMD_GPU,
            Category.NVIDIA_GPU,
            Category.WILDCARD
        ).forEach(category -> assertThat(updatedMonthlyResult.userCategoryLeaderboard())
            .containsKey(category));
    }
}

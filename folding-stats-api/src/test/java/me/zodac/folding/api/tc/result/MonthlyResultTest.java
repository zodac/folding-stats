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
                    Team.createWithoutId(
                        "Team",
                        "",
                        ""
                    ),
                    "captain",
                    List.of(
                        UserSummary.createWithDefaultRank(
                            User.createWithoutId(
                                "foldingUserName",
                                "displayName",
                                "passkey",
                                Category.AMD_GPU,
                                "",
                                "",
                                Hardware.createWithoutId(
                                    "hardwareName",
                                    "displayName",
                                    HardwareMake.AMD,
                                    HardwareType.GPU,
                                    1.0D,
                                    1L
                                ),
                                Team.createWithoutId(
                                    "Team",
                                    "",
                                    ""
                                ),
                                true
                            ),
                            5L,
                            500L,
                            1
                        )
                    ),
                    Collections.emptyList()
                ),
                1, 0L, 0L
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
            totalTeamPoints += teamLeaderboardEntry.getTeamPoints();
            totalTeamMultipliedPoints += teamLeaderboardEntry.getTeamMultipliedPoints();
            totalTeamUnits += teamLeaderboardEntry.getTeamUnits();
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
            totalEmptyTeamPoints += teamLeaderboardEntry.getTeamPoints();
            totalEmptyTeamMultipliedPoints += teamLeaderboardEntry.getTeamMultipliedPoints();
            totalEmptyTeamUnits += teamLeaderboardEntry.getTeamUnits();
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

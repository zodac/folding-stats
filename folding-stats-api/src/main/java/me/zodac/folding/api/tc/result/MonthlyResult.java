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

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import me.zodac.folding.api.tc.Category;
import me.zodac.folding.api.util.DateTimeUtils;
import me.zodac.folding.rest.api.tc.leaderboard.TeamLeaderboardEntry;
import me.zodac.folding.rest.api.tc.leaderboard.UserCategoryLeaderboardEntry;

/**
 * POJO containing the overall result for a single month of the <code>Team Competition</code>.
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@EqualsAndHashCode
@ToString(doNotUseGetters = true)
public class MonthlyResult {

    private final List<TeamLeaderboardEntry> teamLeaderboard;
    private final Map<Category, List<UserCategoryLeaderboardEntry>> userCategoryLeaderboard;
    private final LocalDateTime utcTimestamp;

    /**
     * Creates a {@link MonthlyResult}.
     *
     * @param teamLeaderboard         the leaderboard for {@link me.zodac.folding.api.tc.Team}s
     * @param userCategoryLeaderboard the leaderboard for {@link me.zodac.folding.api.tc.User} {@link Category}s
     * @param utcTimestamp            the {@link java.time.ZoneOffset#UTC} {@link LocalDateTime} for the {@link MonthlyResult}
     * @return the created {@link MonthlyResult}
     */
    public static MonthlyResult create(final List<TeamLeaderboardEntry> teamLeaderboard,
                                       final Map<Category, List<UserCategoryLeaderboardEntry>> userCategoryLeaderboard,
                                       final LocalDateTime utcTimestamp) {
        return new MonthlyResult(teamLeaderboard, userCategoryLeaderboard, utcTimestamp);
    }

    /**
     * Creates a {@link MonthlyResult}.
     *
     * <p>
     * Uses the current {@link java.time.ZoneOffset#UTC} {@link LocalDateTime} from {@link DateTimeUtils#currentUtcDateTime()}.
     *
     * @param teamLeaderboard         the leaderboard for {@link me.zodac.folding.api.tc.Team}s
     * @param userCategoryLeaderboard the leaderboard for {@link me.zodac.folding.api.tc.User} {@link Category}s
     * @return the created {@link MonthlyResult}
     */
    public static MonthlyResult create(final List<TeamLeaderboardEntry> teamLeaderboard,
                                       final Map<Category, List<UserCategoryLeaderboardEntry>> userCategoryLeaderboard) {
        return create(teamLeaderboard, userCategoryLeaderboard, DateTimeUtils.currentUtcDateTime().toLocalDateTime());
    }

    /**
     * Creates an empty {@link MonthlyResult}.
     *
     * <p>
     * Will contain an {@link Collections#emptyList()} for {@code teamLeaderboard}, and {@code userCategoryLeaderboard} will be populated with an
     * entry for each instance of {@link Category#getAllValues()}, with a default value of {@link Collections#emptyList()}.
     *
     * @return the empty {@link MonthlyResult}
     */
    public static MonthlyResult empty() {
        final Map<Category, List<UserCategoryLeaderboardEntry>> emptyCategoryResult = new HashMap<>(Category.getAllValues().size());
        for (final Category category : Category.getAllValues()) {
            emptyCategoryResult.put(category, Collections.emptyList());
        }

        return create(Collections.emptyList(), emptyCategoryResult);
    }

    /**
     * Takes an existing {@link MonthlyResult} and creates a new instance with an updated {@code userCategoryLeaderboard}.
     *
     * <p>
     * Since it is possible there are no {@link me.zodac.folding.api.tc.User}s for a given {@link Category}, there will be no entry for that
     * {@link Category} in the {@code userCategoryLeaderboard}. We will instead iterate though {@link Category#getAllValues()} and add an entry for
     * each missing {@link Category}, with a default value of {@link Collections#emptyList()}.
     *
     * @param monthlyResult the {@link MonthlyResult} to update
     * @return the updated {@link MonthlyResult}
     */
    public static MonthlyResult updateWithEmptyCategories(final MonthlyResult monthlyResult) {
        final Map<Category, List<UserCategoryLeaderboardEntry>> categories = new HashMap<>(monthlyResult.getUserCategoryLeaderboard());

        for (final Category category : Category.getAllValues()) {
            if (!categories.containsKey(category)) {
                categories.put(category, Collections.emptyList());
            }
        }

        return create(monthlyResult.teamLeaderboard, categories, monthlyResult.utcTimestamp);
    }

    /**
     * Method to check if the {@link MonthlyResult} has no stats.
     *
     * <p>
     * This can occur if a month's stats have been reset before attempting to create the {@link MonthlyResult}
     *
     * @return <code>true</code> if no {@link TeamLeaderboardEntry} has any points or units.
     */
    public boolean hasNoStats() {
        long totalTeamPoints = 0L;
        int totalTeamUnits = 0;

        for (final TeamLeaderboardEntry teamLeaderboardEntry : teamLeaderboard) {
            totalTeamPoints += teamLeaderboardEntry.getTeamPoints();
            totalTeamUnits += teamLeaderboardEntry.getTeamUnits();
        }

        return totalTeamPoints == 0L && totalTeamUnits == 0;
    }
}

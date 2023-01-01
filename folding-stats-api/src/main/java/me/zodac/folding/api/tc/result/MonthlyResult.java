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

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;
import me.zodac.folding.api.tc.Category;
import me.zodac.folding.api.util.DateTimeUtils;
import me.zodac.folding.rest.api.tc.leaderboard.TeamLeaderboardEntry;
import me.zodac.folding.rest.api.tc.leaderboard.UserCategoryLeaderboardEntry;

/**
 * POJO containing the overall result for a single month of the {@code Team Competition}.
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Accessors(fluent = true)
@ToString(doNotUseGetters = true, callSuper = true)
@EqualsAndHashCode
public final class MonthlyResult {

    private static final DateTimeUtils DATE_TIME_UTILS = DateTimeUtils.create();

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
     * Uses the current {@link java.time.ZoneOffset#UTC} {@link LocalDateTime} from {@link DateTimeUtils#currentUtcLocalDateTime()}.
     *
     * @param teamLeaderboard         the leaderboard for {@link me.zodac.folding.api.tc.Team}s
     * @param userCategoryLeaderboard the leaderboard for {@link me.zodac.folding.api.tc.User} {@link Category}s
     * @return the created {@link MonthlyResult}
     */
    public static MonthlyResult createWithCurrentDateTime(final List<TeamLeaderboardEntry> teamLeaderboard,
                                                          final Map<Category, List<UserCategoryLeaderboardEntry>> userCategoryLeaderboard) {
        return create(teamLeaderboard, userCategoryLeaderboard, DATE_TIME_UTILS.currentUtcLocalDateTime());
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
        final Map<Category, List<UserCategoryLeaderboardEntry>> emptyCategoryResult = new EnumMap<>(Category.class);
        for (final Category category : Category.getAllValues()) {
            emptyCategoryResult.put(category, Collections.emptyList());
        }

        return createWithCurrentDateTime(Collections.emptyList(), emptyCategoryResult);
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
        final Map<Category, List<UserCategoryLeaderboardEntry>> userCategoryLeaderboard = monthlyResult.userCategoryLeaderboard();
        final Map<Category, List<UserCategoryLeaderboardEntry>> categories = new EnumMap<>(Category.class);

        for (final Category category : Category.getAllValues()) {
            categories.put(category, userCategoryLeaderboard.getOrDefault(category, Collections.emptyList()));
        }

        return create(monthlyResult.teamLeaderboard, categories, monthlyResult.utcTimestamp);
    }

    /**
     * Method to check if the {@link MonthlyResult} has no stats.
     *
     * <p>
     * This can occur if a month's stats have been reset before attempting to create the {@link MonthlyResult}
     *
     * @return {@code true} if no {@link TeamLeaderboardEntry} has any points, multiplied points or units.
     */
    public boolean hasNoStats() {
        long totalTeamPoints = 0L;
        long totalTeamMultipliedPoints = 0L;
        int totalTeamUnits = 0;

        for (final TeamLeaderboardEntry teamLeaderboardEntry : teamLeaderboard) {
            totalTeamPoints = incrementOrZero(totalTeamPoints, teamLeaderboardEntry.teamPoints());
            totalTeamMultipliedPoints = incrementOrZero(totalTeamMultipliedPoints, teamLeaderboardEntry.teamMultipliedPoints());
            totalTeamUnits = Math.toIntExact(incrementOrZero(totalTeamUnits, teamLeaderboardEntry.teamUnits()));
        }

        return totalTeamPoints == 0L && totalTeamMultipliedPoints == 0L && totalTeamUnits == 0;
    }

    private static long incrementOrZero(final long initial, final long incrementAmount) {
        final long newValue = initial + incrementAmount;
        return Math.max(newValue, 0L);
    }
}

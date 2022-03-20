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

package me.zodac.folding.client.java.response;

import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.net.http.HttpResponse;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import me.zodac.folding.client.java.request.TeamCompetitionStatsRequestSender;
import me.zodac.folding.rest.api.tc.AllTeamsSummary;
import me.zodac.folding.rest.api.tc.CompetitionSummary;
import me.zodac.folding.rest.api.tc.UserSummary;
import me.zodac.folding.rest.api.tc.leaderboard.TeamLeaderboardEntry;
import me.zodac.folding.rest.api.tc.leaderboard.UserCategoryLeaderboardEntry;
import me.zodac.folding.rest.util.RestUtilConstants;

/**
 * Utility class used to parse a {@link HttpResponse} returned from {@link TeamCompetitionStatsRequestSender}.
 */
public final class TeamCompetitionStatsResponseParser {

    private TeamCompetitionStatsResponseParser() {

    }

    /**
     * Returns the {@link AllTeamsSummary} retrieved by {@link TeamCompetitionStatsRequestSender#getStats()}.
     *
     * @param response the {@link HttpResponse} to parse
     * @return the retrieved {@link AllTeamsSummary}
     */
    public static AllTeamsSummary getStats(final HttpResponse<String> response) {
        return RestUtilConstants.GSON.fromJson(response.body(), AllTeamsSummary.class);
    }

    /**
     * Returns the {@link CompetitionSummary} retrieved by {@link TeamCompetitionStatsRequestSender#getOverallStats()}.
     *
     * @param response the {@link HttpResponse} to parse
     * @return the retrieved {@link CompetitionSummary}
     */
    public static CompetitionSummary getOverallStats(final HttpResponse<String> response) {
        return RestUtilConstants.GSON.fromJson(response.body(), CompetitionSummary.class);
    }

    /**
     * Returns the {@link UserSummary} retrieved by {@link TeamCompetitionStatsRequestSender#getStatsForUser(int)}.
     *
     * @param response the {@link HttpResponse} to parse
     * @return the retrieved {@link UserSummary}
     */
    public static UserSummary getStatsForUser(final HttpResponse<String> response) {
        return RestUtilConstants.GSON.fromJson(response.body(), UserSummary.class);
    }

    /**
     * Returns the {@link TeamLeaderboardEntry}s retrieved by {@link TeamCompetitionStatsRequestSender#getTeamLeaderboard()}.
     *
     * @param response the {@link HttpResponse} to parse
     * @return the retrieved {@link TeamLeaderboardEntry}s
     */
    public static Collection<TeamLeaderboardEntry> getTeamLeaderboard(final HttpResponse<String> response) {
        final Type collectionType = TeamLeaderboardEntryCollectionType.getInstance().getType();
        return RestUtilConstants.GSON.fromJson(response.body(), collectionType);
    }

    /**
     * Returns the {@link UserCategoryLeaderboardEntry}s retrieved by {@link TeamCompetitionStatsRequestSender#getCategoryLeaderboard()}.
     *
     * @param response the {@link HttpResponse} to parse
     * @return the retrieved {@link UserCategoryLeaderboardEntry}s
     */
    public static Map<String, List<UserCategoryLeaderboardEntry>> getCategoryLeaderboard(final HttpResponse<String> response) {
        final Type collectionType = UserCategoryLeaderboardEntryMapType.getInstance().getType();
        return RestUtilConstants.GSON.fromJson(response.body(), collectionType);
    }

    /**
     * Private class defining the {@link Collection} for {@link TeamLeaderboardEntry}s.
     */
    private static final class TeamLeaderboardEntryCollectionType extends TypeToken<Collection<TeamLeaderboardEntry>> {

        private static final TeamLeaderboardEntryCollectionType INSTANCE = new TeamLeaderboardEntryCollectionType();

        /**
         * Retrieve a singleton instance of {@link TeamLeaderboardEntryCollectionType}.
         *
         * @return {@link TeamLeaderboardEntryCollectionType} instance.
         */
        static TeamLeaderboardEntryCollectionType getInstance() {
            return INSTANCE;
        }
    }

    /**
     * Private class defining the {@link Map} for {@link TeamLeaderboardEntry}s.
     */
    private static final class UserCategoryLeaderboardEntryMapType extends TypeToken<Map<String, List<UserCategoryLeaderboardEntry>>> {

        private static final TeamLeaderboardEntryCollectionType INSTANCE = new TeamLeaderboardEntryCollectionType();

        /**
         * Retrieve a singleton instance of {@link TeamLeaderboardEntryCollectionType}.
         *
         * @return {@link TeamLeaderboardEntryCollectionType} instance.
         */
        static TeamLeaderboardEntryCollectionType getInstance() {
            return INSTANCE;
        }
    }
}

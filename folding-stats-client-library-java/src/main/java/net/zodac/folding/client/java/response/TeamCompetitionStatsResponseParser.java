/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2025 zodac.net
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

package net.zodac.folding.client.java.response;

import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.net.http.HttpResponse;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import net.zodac.folding.client.java.request.TeamCompetitionStatsRequestSender;
import net.zodac.folding.rest.api.tc.AllTeamsSummary;
import net.zodac.folding.rest.api.tc.CompetitionSummary;
import net.zodac.folding.rest.api.tc.UserSummary;
import net.zodac.folding.rest.api.tc.leaderboard.TeamLeaderboardEntry;
import net.zodac.folding.rest.api.tc.leaderboard.UserCategoryLeaderboardEntry;
import net.zodac.folding.rest.api.util.RestUtilConstants;

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
     * Returns the {@link CompetitionSummary} retrieved by {@link TeamCompetitionStatsRequestSender#getSummaryStats()}.
     *
     * @param response the {@link HttpResponse} to parse
     * @return the retrieved {@link CompetitionSummary}
     */
    public static CompetitionSummary getSummaryStats(final HttpResponse<String> response) {
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
     * Private class defining the {@link Map} for {@link UserCategoryLeaderboardEntry}s.
     */
    private static final class UserCategoryLeaderboardEntryMapType extends TypeToken<Map<String, List<UserCategoryLeaderboardEntry>>> {

        private static final UserCategoryLeaderboardEntryMapType INSTANCE = new UserCategoryLeaderboardEntryMapType();

        /**
         * Retrieve a singleton instance of {@link UserCategoryLeaderboardEntryMapType}.
         *
         * @return {@link UserCategoryLeaderboardEntryMapType} instance.
         */
        static UserCategoryLeaderboardEntryMapType getInstance() {
            return INSTANCE;
        }
    }
}

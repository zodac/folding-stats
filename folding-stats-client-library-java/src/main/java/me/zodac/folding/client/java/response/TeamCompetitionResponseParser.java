package me.zodac.folding.client.java.response;

import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.net.http.HttpResponse;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import me.zodac.folding.client.java.request.TeamCompetitionStatsRequestSender;
import me.zodac.folding.rest.api.tc.CompetitionSummary;
import me.zodac.folding.rest.api.tc.UserSummary;
import me.zodac.folding.rest.api.tc.leaderboard.TeamLeaderboardEntry;
import me.zodac.folding.rest.api.tc.leaderboard.UserCategoryLeaderboardEntry;
import me.zodac.folding.rest.util.RestUtilConstants;

/**
 * Utility class used to parse a {@link HttpResponse} returned from {@link TeamCompetitionStatsRequestSender}.
 */
public final class TeamCompetitionResponseParser {

    private TeamCompetitionResponseParser() {

    }

    /**
     * Returns the {@link CompetitionSummary} retrieved by {@link TeamCompetitionStatsRequestSender#getStats()}.
     *
     * @param response the {@link HttpResponse} to parse
     * @return the retrieved {@link CompetitionSummary}
     */
    public static CompetitionSummary getStats(final HttpResponse<String> response) {
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
        final Type collectionType = new TypeToken<Collection<TeamLeaderboardEntry>>() {
        }.getType();
        return RestUtilConstants.GSON.fromJson(response.body(), collectionType);
    }

    /**
     * Returns the {@link UserCategoryLeaderboardEntry}s retrieved by {@link TeamCompetitionStatsRequestSender#getCategoryLeaderboard()}.
     *
     * @param response the {@link HttpResponse} to parse
     * @return the retrieved {@link UserCategoryLeaderboardEntry}s
     */
    public static Map<String, List<UserCategoryLeaderboardEntry>> getCategoryLeaderboard(final HttpResponse<String> response) {
        final Type collectionType = new TypeToken<Map<String, List<UserCategoryLeaderboardEntry>>>() {
        }.getType();
        return RestUtilConstants.GSON.fromJson(response.body(), collectionType);
    }
}

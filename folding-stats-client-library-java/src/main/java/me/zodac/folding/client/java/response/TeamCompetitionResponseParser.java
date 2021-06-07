package me.zodac.folding.client.java.response;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import me.zodac.folding.client.java.request.TeamCompetitionStatsRequestSender;
import me.zodac.folding.rest.api.tc.CompetitionResult;
import me.zodac.folding.rest.api.tc.UserResult;
import me.zodac.folding.rest.api.tc.leaderboard.TeamSummary;
import me.zodac.folding.rest.api.tc.leaderboard.UserSummary;

import java.lang.reflect.Type;
import java.net.http.HttpResponse;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Utility class used to parse a {@link HttpResponse} returned from {@link TeamCompetitionStatsRequestSender}.
 */
public final class TeamCompetitionResponseParser {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private TeamCompetitionResponseParser() {
        
    }

    /**
     * Returns the {@link CompetitionResult} retrieved by {@link TeamCompetitionStatsRequestSender#getStats()}.
     *
     * @param response the {@link HttpResponse} to parse
     * @return the retrieved {@link CompetitionResult}
     */
    public static CompetitionResult getStats(final HttpResponse<String> response) {
        return GSON.fromJson(response.body(), CompetitionResult.class);
    }

    /**
     * Returns the {@link UserResult} retrieved by {@link TeamCompetitionStatsRequestSender#getStatsForUser(int)}.
     *
     * @param response the {@link HttpResponse} to parse
     * @return the retrieved {@link UserResult}
     */
    public static UserResult getStatsForUser(final HttpResponse<String> response) {
        return GSON.fromJson(response.body(), UserResult.class);
    }

    /**
     * Returns the {@link TeamSummary}s retrieved by {@link TeamCompetitionStatsRequestSender#getTeamLeaderboard()}.
     *
     * @param response the {@link HttpResponse} to parse
     * @return the retrieved {@link TeamSummary}s
     */
    public static Collection<TeamSummary> getTeamLeaderboard(final HttpResponse<String> response) {
        final Type collectionType = new TypeToken<Collection<TeamSummary>>() {
        }.getType();
        return GSON.fromJson(response.body(), collectionType);
    }

    /**
     * Returns the {@link UserSummary}s retrieved by {@link TeamCompetitionStatsRequestSender#getCategoryLeaderboard()}.
     *
     * @param response the {@link HttpResponse} to parse
     * @return the retrieved {@link UserSummary}s
     */
    public static Map<String, List<UserSummary>> getCategoryLeaderboard(final HttpResponse<String> response) {
        final Type collectionType = new TypeToken<Map<String, List<UserSummary>>>() {
        }.getType();
        return GSON.fromJson(response.body(), collectionType);
    }
}

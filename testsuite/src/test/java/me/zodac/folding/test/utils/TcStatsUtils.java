package me.zodac.folding.test.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.zodac.folding.rest.api.tc.CompetitionResult;
import me.zodac.folding.rest.api.tc.TeamResult;
import me.zodac.folding.rest.api.tc.UserResult;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class TcStatsUtils {

    private static final String BASE_FOLDING_URL = "http://192.168.99.100:8081/folding"; // TODO: [zodac] Use a hostname instead?
    private static final String BASE_STATS_URL = BASE_FOLDING_URL + "/tc_stats";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private TcStatsUtils() {

    }

    public static CompetitionResult get() {
        return ResponseParser.get(RequestSender.get());
    }

    public static TeamResult getTeamFromCompetition(final CompetitionResult competitionResult, final String teamName) {
        for (final TeamResult teamResult : competitionResult.getTeams()) {
            if (teamResult.getTeamName().equalsIgnoreCase(teamName)) {
                return teamResult;
            }
        }
        throw new AssertionError(String.format("Unable to find team '%s' in competition teams: %s", teamName, competitionResult.getTeams()));
    }

    public static UserResult getActiveUserFromTeam(final TeamResult teamResult, final String userName) {
        for (final UserResult userResult : teamResult.getActiveUsers()) {
            if (userResult.getUserName().equalsIgnoreCase(userName)) {
                return userResult;
            }
        }
        throw new AssertionError(String.format("Unable to find user '%s' in active users: %s", userName, teamResult.getActiveUsers()));
    }

    public static UserResult getRetiredUserFromTeam(final TeamResult teamResult, final String userName) {
        for (final UserResult userResult : teamResult.getRetiredUsers()) {
            if (userResult.getUserName().equalsIgnoreCase(userName)) {
                return userResult;
            }
        }
        throw new AssertionError(String.format("Unable to find user '%s' in retired users: %s", userName, teamResult.getRetiredUsers()));
    }

    public static class RequestSender {

        private RequestSender() {

        }

        public static HttpResponse<String> get() {
            final HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create(BASE_STATS_URL))
                    .header("Content-Type", "application/json")
                    .build();

            try {
                return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            } catch (final IOException | InterruptedException e) {
                throw new AssertionError("Error sending HTTP request to get TC stats", e);
            }
        }

        public static HttpResponse<Void> manualUpdate() {
            final HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create(BASE_STATS_URL + "/manual"))
                    .header("Content-Type", "application/json")
                    .build();

            try {
                return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.discarding());
            } catch (final IOException | InterruptedException e) {
                throw new AssertionError("Error sending HTTP request to manually trigger update of TC stats", e);
            }
        }

        public static HttpResponse<Void> manualReset() {
            final HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create(BASE_STATS_URL + "/reset"))
                    .header("Content-Type", "application/json")
                    .build();

            try {
                return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.discarding());
            } catch (final IOException | InterruptedException e) {
                throw new AssertionError("Error sending HTTP request to manually trigger monthly reset of TC stats", e);
            }
        }
    }

    public static class ResponseParser {

        private ResponseParser() {

        }

        public static CompetitionResult get(final HttpResponse<String> response) {
            return GSON.fromJson(response.body(), CompetitionResult.class);
        }
    }
}

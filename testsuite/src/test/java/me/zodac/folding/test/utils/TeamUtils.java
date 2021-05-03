package me.zodac.folding.test.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import me.zodac.folding.api.tc.Team;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Collection;
import java.util.List;

// TODO: [zodac] Should move these to a client-library module later
public class TeamUtils {

    private static final String BASE_FOLDING_URL = "http://192.168.99.100:8081/folding"; // TODO: [zodac] Use a hostname instead?
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private TeamUtils() {

    }

    public static class RequestSender {

        private RequestSender() {

        }

        public static HttpResponse<String> getAll() {
            final HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create(BASE_FOLDING_URL + "/teams"))
                    .header("Content-Type", "application/json")
                    .build();

            try {
                return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            } catch (final IOException | InterruptedException e) {
                throw new AssertionError("Error sending HTTP request to get all teams", e);
            }
        }

        public static HttpResponse<String> get(final int teamId) {
            final HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create(BASE_FOLDING_URL + "/teams/" + teamId))
                    .header("Content-Type", "application/json")
                    .build();

            try {
                return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            } catch (final IOException | InterruptedException e) {
                throw new AssertionError("Error sending HTTP request to get team", e);
            }
        }

        public static HttpResponse<String> create(final Team team) {
            final HttpRequest request = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(team)))
                    .uri(URI.create(BASE_FOLDING_URL + "/teams"))
                    .header("Content-Type", "application/json")
                    .build();

            try {
                return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            } catch (final IOException | InterruptedException e) {
                throw new AssertionError("Error sending HTTP request to create team", e);
            }
        }

        public static HttpResponse<String> createBatchOf(final List<Team> batchOfTeams) {
            final HttpRequest request = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(batchOfTeams)))
                    .uri(URI.create(BASE_FOLDING_URL + "/teams/batch"))
                    .header("Content-Type", "application/json")
                    .build();

            try {
                return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            } catch (final IOException | InterruptedException e) {
                throw new AssertionError("Error sending HTTP request to create batch of teams", e);
            }
        }

        public static HttpResponse<String> update(final Team team) {
            final HttpRequest request = HttpRequest.newBuilder()
                    .PUT(HttpRequest.BodyPublishers.ofString(GSON.toJson(team)))
                    .uri(URI.create(BASE_FOLDING_URL + "/teams/" + team.getId()))
                    .header("Content-Type", "application/json")
                    .build();

            try {
                return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            } catch (final IOException | InterruptedException e) {
                throw new AssertionError("Error sending HTTP request to update team", e);
            }
        }

        public static HttpResponse<String> delete(final int teamId) {
            final HttpRequest request = HttpRequest.newBuilder()
                    .DELETE()
                    .uri(URI.create(BASE_FOLDING_URL + "/teams/" + teamId))
                    .header("Content-Type", "application/json")
                    .build();

            try {
                return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            } catch (final IOException | InterruptedException e) {
                throw new AssertionError("Error sending HTTP request to delete team", e);
            }
        }
    }

    public static class ResponseParser {

        private ResponseParser() {

        }

        public static Collection<Team> getAll(final HttpResponse<String> response) {
            final Type collectionType = new TypeToken<Collection<Team>>() {
            }.getType();
            return GSON.fromJson(response.body(), collectionType);
        }

        public static Team get(final HttpResponse<String> response) {
            return GSON.fromJson(response.body(), Team.class);
        }

        public static Team create(final HttpResponse<String> response) {
            return GSON.fromJson(response.body(), Team.class);
        }

        public static Team update(final HttpResponse<String> response) {
            return GSON.fromJson(response.body(), Team.class);
        }
    }
}

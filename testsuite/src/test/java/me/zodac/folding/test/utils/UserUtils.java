package me.zodac.folding.test.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.tc.stats.UserStatsOffset;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Collection;
import java.util.List;

// TODO: [zodac] Should move these to a client-library module later?
public class UserUtils {

    private static final String BASE_FOLDING_URL = "http://192.168.99.100:8081/folding"; // TODO: [zodac] Use a hostname instead?
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private UserUtils() {

    }

    public static class RequestSender {

        private RequestSender() {

        }

        public static HttpResponse<String> getAll() {
            final HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create(BASE_FOLDING_URL + "/users"))
                    .header("Content-Type", "application/json")
                    .build();

            try {
                return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            } catch (final IOException | InterruptedException e) {
                throw new AssertionError("Error sending HTTP request to get all users", e);
            }
        }

        public static HttpResponse<String> get(final int userId) {
            final HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create(BASE_FOLDING_URL + "/users/" + userId))
                    .header("Content-Type", "application/json")
                    .build();

            try {
                return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            } catch (final IOException | InterruptedException e) {
                throw new AssertionError("Error sending HTTP request to get user", e);
            }
        }

        public static HttpResponse<String> create(final User user) {
            final HttpRequest request = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(user)))
                    .uri(URI.create(BASE_FOLDING_URL + "/users"))
                    .header("Content-Type", "application/json")
                    .build();

            try {
                return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            } catch (final IOException | InterruptedException e) {
                throw new AssertionError("Error sending HTTP request to create user", e);
            }
        }

        public static HttpResponse<String> createBatchOf(final List<User> batchOfUsers) {
            final HttpRequest request = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(batchOfUsers)))
                    .uri(URI.create(BASE_FOLDING_URL + "/users/batch"))
                    .header("Content-Type", "application/json")
                    .build();

            try {
                return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            } catch (final IOException | InterruptedException e) {
                throw new AssertionError("Error sending HTTP request to create batch of users", e);
            }
        }

        public static HttpResponse<String> update(final User user) {
            final HttpRequest request = HttpRequest.newBuilder()
                    .PUT(HttpRequest.BodyPublishers.ofString(GSON.toJson(user)))
                    .uri(URI.create(BASE_FOLDING_URL + "/users/" + user.getId()))
                    .header("Content-Type", "application/json")
                    .build();

            try {
                return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            } catch (final IOException | InterruptedException e) {
                throw new AssertionError("Error sending HTTP request to update user", e);
            }
        }

        public static HttpResponse<Void> delete(final int userId) {
            final HttpRequest request = HttpRequest.newBuilder()
                    .DELETE()
                    .uri(URI.create(BASE_FOLDING_URL + "/users/" + userId))
                    .header("Content-Type", "application/json")
                    .build();

            try {
                return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.discarding());
            } catch (final IOException | InterruptedException e) {
                throw new AssertionError("Error sending HTTP request to delete user", e);
            }
        }

        public static HttpResponse<Void> offset(final int userId, final long pointsOffset, final long multipliedPointsOffset, final int unitsOffset) {
            final UserStatsOffset userStatsOffset = UserStatsOffset.create(pointsOffset, multipliedPointsOffset, unitsOffset);

            final HttpRequest request = HttpRequest.newBuilder()
                    .method("PATCH", HttpRequest.BodyPublishers.ofString(GSON.toJson(userStatsOffset)))
                    .uri(URI.create(BASE_FOLDING_URL + "/users/" + userId))
                    .header("Content-Type", "application/json")
                    .build();

            try {
                return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.discarding());
            } catch (final IOException | InterruptedException e) {
                throw new AssertionError("Error sending HTTP request to offset user stats", e);
            }
        }
    }

    public static class ResponseParser {

        private ResponseParser() {

        }

        public static Collection<User> getAll(final HttpResponse<String> response) {
            final Type collectionType = new TypeToken<Collection<User>>() {
            }.getType();
            return GSON.fromJson(response.body(), collectionType);
        }

        public static User get(final HttpResponse<String> response) {
            return GSON.fromJson(response.body(), User.class);
        }

        public static User create(final HttpResponse<String> response) {
            return GSON.fromJson(response.body(), User.class);
        }

        public static User update(final HttpResponse<String> response) {
            return GSON.fromJson(response.body(), User.class);
        }
    }
}

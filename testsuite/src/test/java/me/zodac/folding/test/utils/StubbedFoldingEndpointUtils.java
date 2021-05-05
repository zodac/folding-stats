package me.zodac.folding.test.utils;

import me.zodac.folding.api.tc.User;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class StubbedFoldingEndpointUtils {

    private static final String POINTS_URL_ROOT = "http://192.168.99.100:8081/user";
    private static final String POINTS_URL_FORMAT = POINTS_URL_ROOT + "/%s/stats?passkey=%s&points=%s";
    private static final String UNIT_URL_ROOT = "http://192.168.99.100:8081/bonus";
    private static final String UNIT_URL_FORMAT = UNIT_URL_ROOT + "?user=%s&passkey=%s&units=%s";
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private StubbedFoldingEndpointUtils() {

    }

    public static void enableUser(final User user) {
        setUnits(user, 1);
    }

    public static void disableUser(final User user) {
        setUnits(user, 0);
    }

    public static HttpResponse<Void> setPoints(final User user, final long points) {
        final HttpRequest unitsRequest = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.noBody())
                .uri(URI.create(String.format(POINTS_URL_FORMAT, user.getFoldingUserName(), user.getPasskey(), points)))
                .header("Content-Type", "application/json")
                .build();

        try {
            return HTTP_CLIENT.send(unitsRequest, HttpResponse.BodyHandlers.discarding());
        } catch (final IOException | InterruptedException e) {
            throw new AssertionError(String.format("Error setting points count of user %s/%s to %s", user.getFoldingUserName(), user.getPasskey(), points), e);
        }
    }

    public static HttpResponse<Void> deletePoints() {
        final HttpRequest unitsRequest = HttpRequest.newBuilder()
                .DELETE()
                .uri(URI.create(POINTS_URL_ROOT))
                .header("Content-Type", "application/json")
                .build();

        try {
            return HTTP_CLIENT.send(unitsRequest, HttpResponse.BodyHandlers.discarding());
        } catch (final IOException | InterruptedException e) {
            throw new AssertionError("Error resetting points count of users", e);
        }
    }

    public static HttpResponse<Void> setUnits(final User user, final int units) {
        final HttpRequest unitsRequest = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.noBody())
                .uri(URI.create(String.format(UNIT_URL_FORMAT, user.getFoldingUserName(), user.getPasskey(), units)))
                .header("Content-Type", "application/json")
                .build();

        try {
            return HTTP_CLIENT.send(unitsRequest, HttpResponse.BodyHandlers.discarding());
        } catch (final IOException | InterruptedException e) {
            throw new AssertionError(String.format("Error setting unit count of user %s/%s to %s", user.getFoldingUserName(), user.getPasskey(), units), e);
        }
    }

    public static HttpResponse<Void> deleteUnits() {
        final HttpRequest unitsRequest = HttpRequest.newBuilder()
                .DELETE()
                .uri(URI.create(UNIT_URL_ROOT))
                .header("Content-Type", "application/json")
                .build();

        try {
            return HTTP_CLIENT.send(unitsRequest, HttpResponse.BodyHandlers.discarding());
        } catch (final IOException | InterruptedException e) {
            throw new AssertionError("Error resetting points count of users", e);
        }
    }
}

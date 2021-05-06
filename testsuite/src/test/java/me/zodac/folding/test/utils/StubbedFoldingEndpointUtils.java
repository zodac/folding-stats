package me.zodac.folding.test.utils;

import me.zodac.folding.api.tc.User;
import me.zodac.folding.rest.api.exception.FoldingRestException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Utility class for REST calls to the stubbed Folding@Home endpoints.
 */
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

    /**
     * Adds one unit to the {@link User} so it can be created successfully.
     *
     * @param user the {@link User} to enable
     * @throws FoldingRestException thrown if an error occurs sending the HTTP request
     */
    public static void enableUser(final User user) throws FoldingRestException {
        setUnits(user, 1);
    }

    /**
     * Removes all units from the {@link User} so it cannot be created successfully.
     *
     * @param user the {@link User} to disable
     * @throws FoldingRestException thrown if an error occurs sending the HTTP request
     */
    public static void disableUser(final User user) throws FoldingRestException {
        setUnits(user, 0);
    }

    /**
     * Sets the number of points for a {@link User}.
     *
     * @param user the {@link User} to update
     * @throws FoldingRestException thrown if an error occurs sending the HTTP request
     */
    public static void setPoints(final User user, final long points) throws FoldingRestException {
        final HttpRequest unitsRequest = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.noBody())
                .uri(URI.create(String.format(POINTS_URL_FORMAT, user.getFoldingUserName(), user.getPasskey(), points)))
                .header("Content-Type", "application/json")
                .build();

        try {
            HTTP_CLIENT.send(unitsRequest, HttpResponse.BodyHandlers.discarding());
        } catch (final IOException | InterruptedException e) {
            throw new FoldingRestException(String.format("Error setting points count of user %s/%s to %s", user.getFoldingUserName(), user.getPasskey(), points), e);
        }
    }

    /**
     * Removes all points for all {@link User}s.
     *
     * @throws FoldingRestException thrown if an error occurs sending the HTTP request
     */
    public static void deletePoints() throws FoldingRestException {
        final HttpRequest unitsRequest = HttpRequest.newBuilder()
                .DELETE()
                .uri(URI.create(POINTS_URL_ROOT))
                .header("Content-Type", "application/json")
                .build();

        try {
            HTTP_CLIENT.send(unitsRequest, HttpResponse.BodyHandlers.discarding());
        } catch (final IOException | InterruptedException e) {
            throw new FoldingRestException("Error resetting points count of users", e);
        }
    }

    /**
     * Sets the number of units for a {@link User}.
     *
     * @param user the {@link User} to update
     * @throws FoldingRestException thrown if an error occurs sending the HTTP request
     */
    public static void setUnits(final User user, final int units) throws FoldingRestException {
        final HttpRequest unitsRequest = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.noBody())
                .uri(URI.create(String.format(UNIT_URL_FORMAT, user.getFoldingUserName(), user.getPasskey(), units)))
                .header("Content-Type", "application/json")
                .build();

        try {
            HTTP_CLIENT.send(unitsRequest, HttpResponse.BodyHandlers.discarding());
        } catch (final IOException | InterruptedException e) {
            throw new FoldingRestException(String.format("Error setting unit count of user %s/%s to %s", user.getFoldingUserName(), user.getPasskey(), units), e);
        }
    }


    /**
     * Removes all units for all {@link User}s.
     *
     * @throws FoldingRestException thrown if an error occurs sending the HTTP request
     */
    public static void deleteUnits() throws FoldingRestException {
        final HttpRequest unitsRequest = HttpRequest.newBuilder()
                .DELETE()
                .uri(URI.create(UNIT_URL_ROOT))
                .header("Content-Type", "application/json")
                .build();

        try {
            HTTP_CLIENT.send(unitsRequest, HttpResponse.BodyHandlers.discarding());
        } catch (final IOException | InterruptedException e) {
            throw new FoldingRestException("Error resetting points count of users", e);
        }
    }
}

package me.zodac.folding.test.utils.rest.request;

import static me.zodac.folding.test.utils.TestConstants.HTTP_CLIENT;
import static me.zodac.folding.test.utils.TestConstants.TEST_SERVICE_URL;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.rest.api.exception.FoldingRestException;
import me.zodac.folding.rest.api.tc.request.UserRequest;

/**
 * Utility class for REST calls to the stubbed Folding@Home endpoints.
 */
public final class StubbedFoldingEndpointUtils {

    private static final String POINTS_URL_ROOT = TEST_SERVICE_URL + "/user";
    private static final String POINTS_URL_FORMAT = POINTS_URL_ROOT + "/%s/stats?passkey=%s&points=%s";
    private static final String UNIT_URL_ROOT = TEST_SERVICE_URL + "/bonus";
    private static final String UNIT_URL_FORMAT = UNIT_URL_ROOT + "?user=%s&passkey=%s&units=%s";

    private StubbedFoldingEndpointUtils() {

    }

    /**
     * Adds one unit to the {@link UserRequest} so it can be created successfully.
     *
     * @param user the {@link UserRequest} to enable
     * @throws FoldingRestException thrown if an error occurs sending the HTTP request
     */
    public static void enableUser(final UserRequest user) throws FoldingRestException {
        setUnits(user, 1);
    }

    /**
     * Removes all units from the {@link UserRequest} so it cannot be created successfully.
     *
     * @param user the {@link UserRequest} to disable
     * @throws FoldingRestException thrown if an error occurs sending the HTTP request
     */
    public static void disableUser(final UserRequest user) throws FoldingRestException {
        setUnits(user, 0);
    }

    /**
     * Sets the number of points for a {@link User}.
     *
     * @param user the {@link User} to update
     * @throws FoldingRestException thrown if an error occurs sending the HTTP request
     */
    public static void setPoints(final User user, final long points) throws FoldingRestException {
        setPoints(user.getFoldingUserName(), user.getPasskey(), points);
    }

    /**
     * Sets the number of points for a {@link UserRequest}.
     *
     * @param user the {@link UserRequest} to update
     * @throws FoldingRestException thrown if an error occurs sending the HTTP request
     */
    public static void setPoints(final UserRequest user, final long points) throws FoldingRestException {
        setPoints(user.getFoldingUserName(), user.getPasskey(), points);
    }

    private static void setPoints(final String foldingUserName, final String passkey, final long points) throws FoldingRestException {
        final HttpRequest unitsRequest = HttpRequest.newBuilder()
            .POST(HttpRequest.BodyPublishers.noBody())
            .uri(URI.create(String.format(POINTS_URL_FORMAT, foldingUserName, passkey, points)))
            .header("Content-Type", "application/json")
            .build();

        try {
            HTTP_CLIENT.send(unitsRequest, HttpResponse.BodyHandlers.discarding());
        } catch (final IOException | InterruptedException e) {
            throw new FoldingRestException(String.format("Error setting points count of user %s/%s to %s", foldingUserName, passkey, points), e);
        }
    }

    /**
     * Removes all points for all {@link UserRequest}s.
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
        setUnits(user.getFoldingUserName(), user.getPasskey(), units);
    }

    /**
     * Sets the number of units for a {@link UserRequest}.
     *
     * @param user the {@link UserRequest} to update
     * @throws FoldingRestException thrown if an error occurs sending the HTTP request
     */
    public static void setUnits(final UserRequest user, final int units) throws FoldingRestException {
        setUnits(user.getFoldingUserName(), user.getPasskey(), units);
    }

    private static void setUnits(final String foldingUserName, final String passkey, final int units) throws FoldingRestException {
        final HttpRequest unitsRequest = HttpRequest.newBuilder()
            .POST(HttpRequest.BodyPublishers.noBody())
            .uri(URI.create(String.format(UNIT_URL_FORMAT, foldingUserName, passkey, units)))
            .header("Content-Type", "application/json")
            .build();

        try {
            HTTP_CLIENT.send(unitsRequest, HttpResponse.BodyHandlers.discarding());
        } catch (final IOException | InterruptedException e) {
            throw new FoldingRestException(String.format("Error setting unit count of user %s/%s to %s", foldingUserName, passkey, units), e);
        }
    }

    /**
     * Removes all units for all {@link UserRequest}s.
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

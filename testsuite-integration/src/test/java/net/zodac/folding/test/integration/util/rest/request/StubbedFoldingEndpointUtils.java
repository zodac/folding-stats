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

package net.zodac.folding.test.integration.util.rest.request;

import static net.zodac.folding.rest.api.util.RestUtilConstants.HTTP_CLIENT;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import net.zodac.folding.api.tc.User;
import net.zodac.folding.rest.api.exception.FoldingRestException;
import net.zodac.folding.rest.api.header.ContentType;
import net.zodac.folding.rest.api.header.RestHeader;
import net.zodac.folding.rest.api.tc.request.UserRequest;
import net.zodac.folding.test.integration.util.TestConstants;

/**
 * Utility class for REST calls to the stubbed Folding@Home endpoints.
 */
public final class StubbedFoldingEndpointUtils {

    private static final String POINTS_URL_ROOT = TestConstants.FOLDING_URL + "/user";
    private static final String POINTS_URL_FORMAT = POINTS_URL_ROOT + "/%s/stats?passkey=%s&points=%s";
    private static final String UNIT_URL_ROOT = TestConstants.FOLDING_URL + "/bonus";
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
        addUnits(user, 1);
    }

    /**
     * Removes all units from the {@link UserRequest} so it cannot be created successfully.
     *
     * @param user the {@link UserRequest} to disable
     * @throws FoldingRestException thrown if an error occurs sending the HTTP request
     */
    public static void disableUser(final UserRequest user) throws FoldingRestException {
        addUnits(user, 0);
    }

    /**
     * Adds the number of points for a {@link User}.
     *
     * @param user the {@link User} to update
     * @throws FoldingRestException thrown if an error occurs sending the HTTP request
     */
    public static void addPoints(final User user, final long points) throws FoldingRestException {
        addPoints(user.foldingUserName(), user.passkey(), points);
    }

    /**
     * Adds the number of points for a {@link UserRequest}.
     *
     * @param user the {@link UserRequest} to update
     * @throws FoldingRestException thrown if an error occurs sending the HTTP request
     */
    public static void addPoints(final UserRequest user, final long points) throws FoldingRestException {
        addPoints(user.foldingUserName(), user.passkey(), points);
    }

    private static void addPoints(final String foldingUserName, final String passkey, final long points) throws FoldingRestException {
        final HttpRequest pointsRequest = HttpRequest.newBuilder()
            .POST(HttpRequest.BodyPublishers.noBody())
            .uri(URI.create(String.format(POINTS_URL_FORMAT, foldingUserName, passkey, points)))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentTypeValue())
            .build();

        try {
            final HttpResponse<Void> response = HTTP_CLIENT.send(pointsRequest, HttpResponse.BodyHandlers.discarding());
            assertThat(response.statusCode())
                .as("Expected a 201_CREATED")
                .isEqualTo(HttpURLConnection.HTTP_CREATED);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FoldingRestException(String.format("Error setting points count of user %s/%s to %s", foldingUserName, passkey, points), e);
        } catch (final IOException e) {
            throw new FoldingRestException(String.format("Error setting points count of user %s/%s to %s", foldingUserName, passkey, points), e);
        }
    }

    /**
     * Removes all points for all {@link UserRequest}s.
     *
     * @throws FoldingRestException thrown if an error occurs sending the HTTP request
     */
    public static void deletePoints() throws FoldingRestException {
        final HttpRequest pointsRequest = HttpRequest.newBuilder()
            .DELETE()
            .uri(URI.create(POINTS_URL_ROOT))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentTypeValue())
            .build();

        try {
            final HttpResponse<Void> response = HTTP_CLIENT.send(pointsRequest, HttpResponse.BodyHandlers.discarding());
            assertThat(response.statusCode())
                .as("Expected a 200_OK")
                .isEqualTo(HttpURLConnection.HTTP_OK);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FoldingRestException("Error resetting points count of users", e);
        } catch (final IOException e) {
            throw new FoldingRestException("Error resetting points count of users", e);
        }
    }

    /**
     * Adds the number of units for a {@link User}.
     *
     * @param user the {@link User} to update
     * @throws FoldingRestException thrown if an error occurs sending the HTTP request
     */
    public static void addUnits(final User user, final int units) throws FoldingRestException {
        addUnits(user.foldingUserName(), user.passkey(), units);
    }

    private static void addUnits(final UserRequest user, final int units) throws FoldingRestException {
        addUnits(user.foldingUserName(), user.passkey(), units);
    }

    private static void addUnits(final String foldingUserName, final String passkey, final int units) throws FoldingRestException {
        final HttpRequest unitsRequest = HttpRequest.newBuilder()
            .POST(HttpRequest.BodyPublishers.noBody())
            .uri(URI.create(String.format(UNIT_URL_FORMAT, foldingUserName, passkey, units)))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentTypeValue())
            .build();

        try {
            final HttpResponse<Void> response = HTTP_CLIENT.send(unitsRequest, HttpResponse.BodyHandlers.discarding());
            assertThat(response.statusCode())
                .as("Expected a 201_CREATED")
                .isEqualTo(HttpURLConnection.HTTP_CREATED);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FoldingRestException(String.format("Error setting unit count of user %s/%s to %s", foldingUserName, passkey, units), e);
        } catch (final IOException e) {
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
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentTypeValue())
            .build();

        try {
            final HttpResponse<Void> response = HTTP_CLIENT.send(unitsRequest, HttpResponse.BodyHandlers.discarding());
            assertThat(response.statusCode())
                .as("Expected a 200_OK")
                .isEqualTo(HttpURLConnection.HTTP_OK);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FoldingRestException("Error resetting points count of users", e);
        } catch (final IOException e) {
            throw new FoldingRestException("Error resetting points count of users", e);
        }
    }
}

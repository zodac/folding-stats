/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2024 zodac.me
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

package me.zodac.folding.test.integration.util.rest.request;

import static me.zodac.folding.test.integration.util.DummyAuthenticationData.ADMIN_USER;
import static me.zodac.folding.test.integration.util.rest.response.HttpResponseHeaderUtils.getTotalCount;

import java.net.HttpURLConnection;
import java.net.http.HttpResponse;
import java.util.Collection;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.client.java.request.UserRequestSender;
import me.zodac.folding.client.java.response.UserResponseParser;
import me.zodac.folding.rest.api.exception.FoldingRestException;
import me.zodac.folding.rest.api.tc.request.UserRequest;
import me.zodac.folding.test.integration.util.TestConstants;

/**
 * Utility class for {@link User}-based tests.
 */
public final class UserUtils {

    public static final UserRequestSender USER_REQUEST_SENDER = UserRequestSender.createWithUrl(TestConstants.FOLDING_URL);

    private UserUtils() {

    }

    /**
     * Creates the given {@link UserRequest}.
     *
     * <p>
     * Will also call {@link StubbedFoldingEndpointUtils#enableUser(UserRequest)}, so if you wish to test an invalid number of
     * units, you must set that explicitly.
     *
     * @param user the {@link User} to create
     * @return the created {@link User}
     * @throws FoldingRestException thrown if an error occurs creating the {@link User}
     */
    public static User create(final UserRequest user) throws FoldingRestException {
        StubbedFoldingEndpointUtils.enableUser(user);
        final HttpResponse<String> response = USER_REQUEST_SENDER.create(user, ADMIN_USER.userName(), ADMIN_USER.password());
        if (response.statusCode() == HttpURLConnection.HTTP_CREATED) {
            return UserResponseParser.create(response);
        }

        throw new FoldingRestException(String.format("Invalid response (%s) when creating user: %s", response.statusCode(), response.body()));
    }

    /**
     * Updates a {@link User} based on the given {@link UserRequest}.
     *
     * <p>
     * Will also call {@link StubbedFoldingEndpointUtils#enableUser(UserRequest)}, so if you wish to test an invalid number of
     * units, you must set that explicitly.
     *
     * @param userId the ID of the {@link User} to update
     * @param user   the {@link User} to update
     * @return the updated{@link User}
     * @throws FoldingRestException thrown if an error occurs updating the {@link User}
     */
    public static User update(final int userId, final UserRequest user) throws FoldingRestException {
        StubbedFoldingEndpointUtils.enableUser(user);
        final HttpResponse<String> response = USER_REQUEST_SENDER.update(userId, user, ADMIN_USER.userName(), ADMIN_USER.password());
        if (response.statusCode() == HttpURLConnection.HTTP_OK) {
            return UserResponseParser.create(response);
        }

        throw new FoldingRestException(String.format("Invalid response (%s) when updating user: %s", response.statusCode(), response.body()));
    }

    /**
     * Retrieves all {@link User}s.
     *
     * @return the {@link User}s
     * @throws FoldingRestException thrown if an error occurs retrieving the {@link User}s
     */
    public static Collection<User> getAll() throws FoldingRestException {
        final HttpResponse<String> response = USER_REQUEST_SENDER.getAllWithoutPasskeys();
        if (response.statusCode() == HttpURLConnection.HTTP_OK) {
            return UserResponseParser.getAll(response);
        }

        throw new FoldingRestException(
            String.format("Invalid response (%s) when getting all users with: %s", response.statusCode(), response.body()));
    }

    /**
     * Retrieves the number of {@link User}s.
     *
     * @return the number of {@link User}s
     * @throws FoldingRestException thrown if an error occurs retrieving the {@link User} count
     */
    public static int getNumberOfUsers() throws FoldingRestException {
        final HttpResponse<String> response = USER_REQUEST_SENDER.getAllWithoutPasskeys();
        return getTotalCount(response);
    }

    /**
     * Retrieves a {@link User} with the given ID.
     *
     * @param userId the ID of the {@link User} to retrieve
     * @return the {@link User}
     * @throws FoldingRestException thrown if an error occurs retrieving the {@link User}
     */
    public static User get(final int userId) throws FoldingRestException {
        final HttpResponse<String> response = USER_REQUEST_SENDER.get(userId);
        if (response.statusCode() == HttpURLConnection.HTTP_OK) {
            return UserResponseParser.get(response);
        }

        throw new FoldingRestException(
            String.format("Invalid response (%s) when getting user with ID %s: %s", response.statusCode(), userId, response.body()));
    }

    /**
     * Retrieves a {@link User} with passkey with the given ID.
     *
     * @param userId the ID of the {@link User} to retrieve
     * @return the {@link User}
     * @throws FoldingRestException thrown if an error occurs retrieving the {@link User}
     */
    public static User getWithPasskey(final int userId) throws FoldingRestException {
        final HttpResponse<String> response = USER_REQUEST_SENDER.getWithPasskey(userId, ADMIN_USER.userName(), ADMIN_USER.password());
        if (response.statusCode() == HttpURLConnection.HTTP_OK) {
            return UserResponseParser.get(response);
        }

        throw new FoldingRestException(
            String.format("Invalid response (%s) when getting user with ID %s: %s", response.statusCode(), userId, response.body()));
    }
}

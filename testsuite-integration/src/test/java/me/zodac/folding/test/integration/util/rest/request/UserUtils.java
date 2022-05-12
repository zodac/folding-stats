/*
 * MIT License
 *
 * Copyright (c) 2021-2022 zodac.me
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.zodac.folding.test.integration.util.rest.request;

import static me.zodac.folding.test.integration.util.TestAuthenticationData.ADMIN_USER;
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

package me.zodac.folding.test.utils.rest.request;

import me.zodac.folding.api.tc.User;
import me.zodac.folding.client.java.request.UserRequestSender;
import me.zodac.folding.client.java.response.UserResponseParser;
import me.zodac.folding.rest.api.exception.FoldingRestException;
import me.zodac.folding.rest.api.tc.request.UserRequest;

import java.net.HttpURLConnection;
import java.net.http.HttpResponse;
import java.util.Collection;

import static me.zodac.folding.test.utils.TestAuthenticationData.ADMIN_USER;
import static me.zodac.folding.test.utils.TestConstants.FOLDING_URL;
import static me.zodac.folding.test.utils.rest.response.HttpResponseHeaderUtils.getXTotalCount;


/**
 * Utility class for {@link User}-based tests.
 */
public final class UserUtils {

    public static final UserRequestSender USER_REQUEST_SENDER = UserRequestSender.create(FOLDING_URL);

    private UserUtils() {

    }

    /**
     * Creates the given {@link UserRequest}.
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

        if (response.statusCode() == HttpURLConnection.HTTP_CONFLICT) {
            return get(user.getId());
        }

        throw new FoldingRestException(String.format("Invalid response (%s) when creating user: %s", response.statusCode(), response.body()));
    }


    /**
     * Retrieves all {@link User}s.
     *
     * @return the {@link User}s
     * @throws FoldingRestException thrown if an error occurs retrieving the {@link User}s
     */
    public static Collection<User> getAll() throws FoldingRestException {
        final HttpResponse<String> response = USER_REQUEST_SENDER.getAll();
        if (response.statusCode() == HttpURLConnection.HTTP_OK) {
            return UserResponseParser.getAll(response);
        }

        throw new FoldingRestException(String.format("Invalid response (%s) when getting all users with: %s", response.statusCode(), response.body()));
    }

    /**
     * Retrieves the number of {@link User}s.
     *
     * @return the number of {@link User}s
     * @throws FoldingRestException thrown if an error occurs retrieving the {@link User} count
     */
    public static int getNumberOfUsers() throws FoldingRestException {
        final HttpResponse<String> response = USER_REQUEST_SENDER.getAll();
        return getXTotalCount(response);
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

        throw new FoldingRestException(String.format("Invalid response (%s) when getting user with ID %s: %s", response.statusCode(), userId, response.body()));
    }
}

package me.zodac.folding.test.utils;

import me.zodac.folding.api.tc.User;
import me.zodac.folding.client.java.request.UserRequestSender;
import me.zodac.folding.client.java.response.UserResponseParser;
import me.zodac.folding.rest.api.exception.FoldingRestException;

import java.net.HttpURLConnection;
import java.net.http.HttpResponse;
import java.util.Collection;
import java.util.List;
import java.util.Map;


/**
 * Utility class for {@link User}-based tests.
 */
public final class UserUtils {

    public static final UserRequestSender USER_REQUEST_SENDER = UserRequestSender.create("http://192.168.99.100:8081/folding");

    private UserUtils() {

    }

    /**
     * Creates the given {@link User}, or if it already exists, returns the existing one.
     *
     * @param user the {@link User} to create/retrieve
     * @return the created {@link User} or existing {@link User}
     * @throws FoldingRestException thrown if an error occurs creating/retrieving the {@link User}
     */
    public static User createOrConflict(final User user) throws FoldingRestException {
        final HttpResponse<String> response = USER_REQUEST_SENDER.create(user);
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
        final Map<String, List<String>> headers = response.headers().map();
        if (headers.containsKey("X-Total-Count")) {
            final String firstHeaderValue = headers.get("X-Total-Count").get(0);

            try {
                return Integer.parseInt(firstHeaderValue);
            } catch (final NumberFormatException e) {
                throw new FoldingRestException(String.format("Error parsing 'X-Total-Count' header %s", firstHeaderValue), e);
            }
        }
        throw new FoldingRestException(String.format("Unable to find 'X-Total-Count' header: %s", headers));
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

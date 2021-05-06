package me.zodac.folding.test.utils;

import me.zodac.folding.api.tc.User;
import me.zodac.folding.client.java.request.UserRequestSender;
import me.zodac.folding.client.java.response.UserResponseParser;
import me.zodac.folding.rest.api.exception.FoldingRestException;

import java.net.http.HttpResponse;
import java.util.Collection;
import java.util.List;


public class UserUtils {

    private static final String BASE_FOLDING_URL = "http://192.168.99.100:8081/folding"; // TODO: [zodac] Use a hostname instead?
    private static final UserRequestSender USER_REQUEST_SENDER = UserRequestSender.create(BASE_FOLDING_URL);

    private UserUtils() {

    }

    public static class RequestSender {

        private RequestSender() {

        }

        public static HttpResponse<String> getAll() {
            try {
                return USER_REQUEST_SENDER.getAll();
            } catch (final FoldingRestException e) {
                throw new AssertionError("Error sending HTTP request to get all users", e);
            }
        }

        public static HttpResponse<String> get(final int userId) {
            try {
                return USER_REQUEST_SENDER.get(userId);
            } catch (final FoldingRestException e) {
                throw new AssertionError("Error sending HTTP request to get user", e);
            }
        }

        public static HttpResponse<String> create(final User user) {
            try {
                return USER_REQUEST_SENDER.create(user);
            } catch (final FoldingRestException e) {
                throw new AssertionError("Error sending HTTP request to create user", e);
            }
        }

        public static HttpResponse<String> createBatchOf(final List<User> batchOfUsers) {
            try {
                return USER_REQUEST_SENDER.createBatchOf(batchOfUsers);
            } catch (final FoldingRestException e) {
                throw new AssertionError("Error sending HTTP request to create batch of users", e);
            }
        }

        public static HttpResponse<String> update(final User user) {
            try {
                return USER_REQUEST_SENDER.update(user);
            } catch (final FoldingRestException e) {
                throw new AssertionError("Error sending HTTP request to update user", e);
            }
        }

        public static HttpResponse<Void> delete(final int userId) {
            try {
                return USER_REQUEST_SENDER.delete(userId);
            } catch (final FoldingRestException e) {
                throw new AssertionError("Error sending HTTP request to delete user", e);
            }
        }

        public static HttpResponse<Void> offset(final int userId, final long pointsOffset, final long multipliedPointsOffset, final int unitsOffset) {
            try {
                return USER_REQUEST_SENDER.offset(userId, pointsOffset, multipliedPointsOffset, unitsOffset);
            } catch (final FoldingRestException e) {
                throw new AssertionError("Error sending HTTP request to offset user stats", e);
            }
        }
    }

    public static class ResponseParser {

        private ResponseParser() {

        }

        public static Collection<User> getAll(final HttpResponse<String> response) {
            return UserResponseParser.getAll(response);
        }

        public static User get(final HttpResponse<String> response) {
            return UserResponseParser.get(response);
        }

        public static User create(final HttpResponse<String> response) {
            return UserResponseParser.create(response);
        }

        public static User update(final HttpResponse<String> response) {
            return UserResponseParser.update(response);
        }
    }
}

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

package net.zodac.folding.client.java.response;

import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.net.http.HttpResponse;
import java.util.Collection;
import net.zodac.folding.api.tc.User;
import net.zodac.folding.client.java.request.UserRequestSender;
import net.zodac.folding.rest.api.tc.request.UserRequest;
import net.zodac.folding.rest.api.util.RestUtilConstants;

/**
 * Utility class used to parse a {@link HttpResponse} returned from {@link UserRequestSender}.
 */
public final class UserResponseParser {

    private UserResponseParser() {

    }

    /**
     * Returns the {@link User}s retrieved by {@link UserRequestSender#getAllWithoutPasskeys()},
     * {@link UserRequestSender#getAllWithoutPasskeys(String)},
     * {@link UserRequestSender#getAllWithPasskeys(String, String)} or
     * {@link UserRequestSender#getAllWithPasskeys(String, String, String)}.
     *
     * @param response the {@link HttpResponse} to parse
     * @return the retrieved {@link User}s
     */
    public static Collection<User> getAll(final HttpResponse<String> response) {
        final Type collectionType = UserCollectionType.getInstance().getType();
        return RestUtilConstants.GSON.fromJson(response.body(), collectionType);
    }

    /**
     * Returns the {@link User} retrieved by {@link UserRequestSender#get(int)} or
     * {@link UserRequestSender#get(int, String)}.
     *
     * @param response the {@link HttpResponse} to parse
     * @return the retrieved {@link User}
     */
    public static User get(final HttpResponse<String> response) {
        return RestUtilConstants.GSON.fromJson(response.body(), User.class);
    }

    /**
     * Returns the {@link User} created by
     * {@link UserRequestSender#create(UserRequest, String, String)}.
     *
     * @param response the {@link HttpResponse} to parse
     * @return the created {@link User}
     */
    public static User create(final HttpResponse<String> response) {
        return RestUtilConstants.GSON.fromJson(response.body(), User.class);
    }

    /**
     * Returns the {@link User} updated by
     * {@link UserRequestSender#update(int, UserRequest, String, String)}.
     *
     * @param response the {@link HttpResponse} to parse
     * @return the updated {@link User}
     */
    public static User update(final HttpResponse<String> response) {
        return RestUtilConstants.GSON.fromJson(response.body(), User.class);
    }

    /**
     * Private class defining the {@link Collection} for {@link User}s.
     */
    private static final class UserCollectionType extends TypeToken<Collection<User>> {

        private static final UserCollectionType INSTANCE = new UserCollectionType();

        /**
         * Retrieve a singleton instance of {@link UserCollectionType}.
         *
         * @return {@link UserCollectionType} instance.
         */
        static UserCollectionType getInstance() {
            return INSTANCE;
        }
    }
}

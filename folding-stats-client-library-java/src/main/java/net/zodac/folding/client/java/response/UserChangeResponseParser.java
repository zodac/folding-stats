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
import net.zodac.folding.api.tc.change.UserChange;
import net.zodac.folding.client.java.request.UserChangeRequestSender;
import net.zodac.folding.rest.api.tc.request.UserChangeRequest;
import net.zodac.folding.rest.api.util.RestUtilConstants;

/**
 * Utility class used to parse a {@link HttpResponse} returned from {@link UserChangeRequestSender}.
 */
public final class UserChangeResponseParser {

    private UserChangeResponseParser() {

    }

    /**
     * Returns the {@link UserChange}s retrieved by {@link UserChangeRequestSender#getAllWithoutPasskeys()},
     * {@link UserChangeRequestSender#getAllWithoutPasskeys(Collection)}, {@link UserChangeRequestSender#getAllWithPasskeys(String, String)} or
     * {@link UserChangeRequestSender#getAllWithPasskeys(Collection, String, String)}.
     *
     * @param response the {@link HttpResponse} to parse
     * @return the retrieved {@link UserChange}s
     */
    public static Collection<UserChange> getAll(final HttpResponse<String> response) {
        final Type collectionType = UserChangeCollectionType.getInstance().getType();
        return RestUtilConstants.GSON.fromJson(response.body(), collectionType);
    }

    /**
     * Returns the {@link UserChange} retrieved by {@link UserChangeRequestSender#get(int, String, String)}.
     *
     * @param response the {@link HttpResponse} to parse
     * @return the retrieved {@link UserChange}
     */
    public static UserChange get(final HttpResponse<String> response) {
        return RestUtilConstants.GSON.fromJson(response.body(), UserChange.class);
    }

    /**
     * Returns the {@link UserChange} updated by {@link UserChangeRequestSender#reject(int, String, String)},
     * {@link UserChangeRequestSender#approveImmediately(int, String, String)} or
     * {@link UserChangeRequestSender#approveNextMonth(int, String, String)}.
     *
     * @param response the {@link HttpResponse} to parse
     * @return the updated {@link UserChange}
     */
    public static UserChange update(final HttpResponse<String> response) {
        return RestUtilConstants.GSON.fromJson(response.body(), UserChange.class);
    }

    /**
     * Returns the {@link UserChange} created by {@link UserChangeRequestSender#create(UserChangeRequest)}.
     *
     * @param response the {@link HttpResponse} to parse
     * @return the created {@link UserChange}
     */
    public static UserChange create(final HttpResponse<String> response) {
        return RestUtilConstants.GSON.fromJson(response.body(), UserChange.class);
    }

    /**
     * Private class defining the {@link Collection} for {@link UserChange}s.
     */
    private static final class UserChangeCollectionType extends TypeToken<Collection<UserChange>> {

        private static final UserChangeCollectionType INSTANCE = new UserChangeCollectionType();

        /**
         * Retrieve a singleton instance of {@link UserChangeCollectionType}.
         *
         * @return {@link UserChangeCollectionType} instance.
         */
        static UserChangeCollectionType getInstance() {
            return INSTANCE;
        }
    }
}

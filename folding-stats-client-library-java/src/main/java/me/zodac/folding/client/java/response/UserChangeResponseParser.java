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

package me.zodac.folding.client.java.response;

import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.net.http.HttpResponse;
import java.util.Collection;
import me.zodac.folding.api.tc.change.UserChange;
import me.zodac.folding.client.java.request.UserChangeRequestSender;
import me.zodac.folding.rest.api.tc.request.UserChangeRequest;
import me.zodac.folding.rest.api.util.RestUtilConstants;

/**
 * Utility class used to parse a {@link HttpResponse} returned from {@link me.zodac.folding.client.java.request.UserChangeRequestSender}.
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
     * Returns the {@link UserChange} retrieved by {@link me.zodac.folding.client.java.request.UserChangeRequestSender#get(int, String, String)}.
     *
     * @param response the {@link HttpResponse} to parse
     * @return the retrieved {@link UserChange}
     */
    public static UserChange get(final HttpResponse<String> response) {
        return RestUtilConstants.GSON.fromJson(response.body(), UserChange.class);
    }

    /**
     * Returns the {@link UserChange} updated by {@link me.zodac.folding.client.java.request.UserChangeRequestSender#reject(int, String, String)},
     * {@link me.zodac.folding.client.java.request.UserChangeRequestSender#approveImmediately(int, String, String)} or
     * {@link me.zodac.folding.client.java.request.UserChangeRequestSender#approveNextMonth(int, String, String)}.
     *
     * @param response the {@link HttpResponse} to parse
     * @return the updated {@link UserChange}
     */
    public static UserChange update(final HttpResponse<String> response) {
        return RestUtilConstants.GSON.fromJson(response.body(), UserChange.class);
    }

    /**
     * Returns the {@link UserChange} created by {@link me.zodac.folding.client.java.request.UserChangeRequestSender#create(UserChangeRequest)}.
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
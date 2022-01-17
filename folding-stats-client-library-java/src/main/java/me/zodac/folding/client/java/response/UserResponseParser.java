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
import me.zodac.folding.api.tc.User;
import me.zodac.folding.rest.util.RestUtilConstants;

/**
 * Utility class used to parse a {@link HttpResponse} returned from {@link me.zodac.folding.client.java.request.UserRequestSender}.
 */
public final class UserResponseParser {

    private UserResponseParser() {

    }

    /**
     * Returns the {@link User}s retrieved by {@link me.zodac.folding.client.java.request.UserRequestSender#getAll()}.
     *
     * @param response the {@link HttpResponse} to parse
     * @return the retrieved {@link User}s
     */
    public static Collection<User> getAll(final HttpResponse<String> response) {
        final Type collectionType = new TypeToken<Collection<User>>() {
        }.getType();
        return RestUtilConstants.GSON.fromJson(response.body(), collectionType);
    }

    /**
     * Returns the {@link User} retrieved by {@link me.zodac.folding.client.java.request.UserRequestSender#get(int)}.
     *
     * @param response the {@link HttpResponse} to parse
     * @return the retrieved {@link User}
     */
    public static User get(final HttpResponse<String> response) {
        return RestUtilConstants.GSON.fromJson(response.body(), User.class);
    }

    /**
     * Returns the {@link User} created by
     * {@link me.zodac.folding.client.java.request.UserRequestSender#create(me.zodac.folding.rest.api.tc.request.UserRequest)}.
     *
     * @param response the {@link HttpResponse} to parse
     * @return the created {@link User}
     */
    public static User create(final HttpResponse<String> response) {
        return RestUtilConstants.GSON.fromJson(response.body(), User.class);
    }

    /**
     * Returns the {@link User} updated by
     * {@link me.zodac.folding.client.java.request.UserRequestSender#update(int, me.zodac.folding.rest.api.tc.request.UserRequest)}.
     *
     * @param response the {@link HttpResponse} to parse
     * @return the updated {@link User}
     */
    public static User update(final HttpResponse<String> response) {
        return RestUtilConstants.GSON.fromJson(response.body(), User.class);
    }
}
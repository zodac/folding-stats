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
import me.zodac.folding.rest.api.tc.request.UserChangeRequest;
import me.zodac.folding.rest.util.RestUtilConstants;

/**
 * Utility class used to parse a {@link HttpResponse} returned from {@link me.zodac.folding.client.java.request.UserChangeRequestSender}.
 */
public final class UserChangeResponseParser {

    private UserChangeResponseParser() {

    }

    /**
     * Returns the {@link UserChange}s retrieved by {@link me.zodac.folding.client.java.request.UserChangeRequestSender#getAll()} or
     * {@link me.zodac.folding.client.java.request.UserChangeRequestSender#getAll(Collection)}.
     *
     * @param response the {@link HttpResponse} to parse
     * @return the retrieved {@link UserChange}s
     */
    public static Collection<UserChange> getAll(final HttpResponse<String> response) {
        final Type collectionType = new TypeToken<Collection<UserChange>>() {
        }.getType();
        return RestUtilConstants.GSON.fromJson(response.body(), collectionType);
    }

    /**
     * Returns the {@link UserChange} retrieved by {@link me.zodac.folding.client.java.request.UserChangeRequestSender#get(int)}.
     *
     * @param response the {@link HttpResponse} to parse
     * @return the retrieved {@link UserChange}
     */
    public static UserChange get(final HttpResponse<String> response) {
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
}
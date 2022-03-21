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
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.rest.api.tc.request.HardwareRequest;
import me.zodac.folding.rest.api.util.RestUtilConstants;

/**
 * Utility class used to parse a {@link HttpResponse} returned from {@link me.zodac.folding.client.java.request.HardwareRequestSender}.
 */
public final class HardwareResponseParser {

    private HardwareResponseParser() {

    }

    /**
     * Returns the {@link Hardware}s retrieved by {@link me.zodac.folding.client.java.request.HardwareRequestSender#getAll()}.
     *
     * @param response the {@link HttpResponse} to parse
     * @return the retrieved {@link Hardware}s
     */
    public static Collection<Hardware> getAll(final HttpResponse<String> response) {
        final Type collectionType = HardwareCollectionType.getInstance().getType();
        return RestUtilConstants.GSON.fromJson(response.body(), collectionType);
    }

    /**
     * Returns the {@link Hardware} retrieved by {@link me.zodac.folding.client.java.request.HardwareRequestSender#get(int)} or
     * {@link me.zodac.folding.client.java.request.HardwareRequestSender#get(String)}.
     *
     * @param response the {@link HttpResponse} to parse
     * @return the retrieved {@link Hardware}
     */
    public static Hardware get(final HttpResponse<String> response) {
        return RestUtilConstants.GSON.fromJson(response.body(), Hardware.class);
    }

    /**
     * Returns the {@link Hardware} created by
     * {@link me.zodac.folding.client.java.request.HardwareRequestSender#create(HardwareRequest, String, String)}.
     *
     * @param response the {@link HttpResponse} to parse
     * @return the created {@link Hardware}
     */
    public static Hardware create(final HttpResponse<String> response) {
        return RestUtilConstants.GSON.fromJson(response.body(), Hardware.class);
    }

    /**
     * Returns the {@link Hardware} updated by
     * {@link me.zodac.folding.client.java.request.HardwareRequestSender#update(int, HardwareRequest, String, String)}.
     *
     * @param response the {@link HttpResponse} to parse
     * @return the updated {@link Hardware}
     */
    public static Hardware update(final HttpResponse<String> response) {
        return RestUtilConstants.GSON.fromJson(response.body(), Hardware.class);
    }

    /**
     * Private class defining the {@link Collection} for {@link Hardware}s.
     */
    private static final class HardwareCollectionType extends TypeToken<Collection<Hardware>> {

        private static final HardwareCollectionType INSTANCE = new HardwareCollectionType();

        /**
         * Retrieve a singleton instance of {@link HardwareCollectionType}.
         *
         * @return {@link HardwareCollectionType} instance.
         */
        static HardwareCollectionType getInstance() {
            return INSTANCE;
        }
    }
}
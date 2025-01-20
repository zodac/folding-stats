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
import net.zodac.folding.api.tc.Hardware;
import net.zodac.folding.client.java.request.HardwareRequestSender;
import net.zodac.folding.rest.api.tc.request.HardwareRequest;
import net.zodac.folding.rest.api.util.RestUtilConstants;

/**
 * Utility class used to parse a {@link HttpResponse} returned from {@link HardwareRequestSender}.
 */
public final class HardwareResponseParser {

    private HardwareResponseParser() {

    }

    /**
     * Returns the {@link Hardware}s retrieved by {@link HardwareRequestSender#getAll()}.
     *
     * @param response the {@link HttpResponse} to parse
     * @return the retrieved {@link Hardware}s
     */
    public static Collection<Hardware> getAll(final HttpResponse<String> response) {
        final Type collectionType = HardwareCollectionType.getInstance().getType();
        return RestUtilConstants.GSON.fromJson(response.body(), collectionType);
    }

    /**
     * Returns the {@link Hardware} retrieved by {@link HardwareRequestSender#get(int)} or
     * {@link HardwareRequestSender#get(String)}.
     *
     * @param response the {@link HttpResponse} to parse
     * @return the retrieved {@link Hardware}
     */
    public static Hardware get(final HttpResponse<String> response) {
        return RestUtilConstants.GSON.fromJson(response.body(), Hardware.class);
    }

    /**
     * Returns the {@link Hardware} created by
     * {@link HardwareRequestSender#create(HardwareRequest, String, String)}.
     *
     * @param response the {@link HttpResponse} to parse
     * @return the created {@link Hardware}
     */
    public static Hardware create(final HttpResponse<String> response) {
        return RestUtilConstants.GSON.fromJson(response.body(), Hardware.class);
    }

    /**
     * Returns the {@link Hardware} updated by
     * {@link HardwareRequestSender#update(int, HardwareRequest, String, String)}.
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

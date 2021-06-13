package me.zodac.folding.client.java.response;

import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.net.http.HttpResponse;
import java.util.Collection;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.client.java.util.RestUtilConstants;

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
        final Type collectionType = new TypeToken<Collection<Hardware>>() {
        }.getType();
        return RestUtilConstants.GSON.fromJson(response.body(), collectionType);
    }

    /**
     * Returns the {@link Hardware} retrieved by {@link me.zodac.folding.client.java.request.HardwareRequestSender#get(int)}.
     *
     * @param response the {@link HttpResponse} to parse
     * @return the retrieved {@link Hardware}
     */
    public static Hardware get(final HttpResponse<String> response) {
        return RestUtilConstants.GSON.fromJson(response.body(), Hardware.class);
    }

    /**
     * Returns the {@link Hardware} created by
     * {@link me.zodac.folding.client.java.request.HardwareRequestSender#create(me.zodac.folding.rest.api.tc.request.HardwareRequest)}.
     *
     * @param response the {@link HttpResponse} to parse
     * @return the created {@link Hardware}
     */
    public static Hardware create(final HttpResponse<String> response) {
        return RestUtilConstants.GSON.fromJson(response.body(), Hardware.class);
    }

    /**
     * Returns the {@link Hardware} updated by
     * {@link me.zodac.folding.client.java.request.HardwareRequestSender#update(int, me.zodac.folding.rest.api.tc.request.HardwareRequest)}.
     *
     * @param response the {@link HttpResponse} to parse
     * @return the updated {@link Hardware}
     */
    public static Hardware update(final HttpResponse<String> response) {
        return RestUtilConstants.GSON.fromJson(response.body(), Hardware.class);
    }
}
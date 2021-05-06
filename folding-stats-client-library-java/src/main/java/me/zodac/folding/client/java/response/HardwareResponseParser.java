package me.zodac.folding.client.java.response;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import lombok.experimental.UtilityClass;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.client.java.request.HardwareRequestSender;

import java.lang.reflect.Type;
import java.net.http.HttpResponse;
import java.util.Collection;

/**
 * {@link UtilityClass} used to parse a {@link HttpResponse} returned from {@link HardwareRequestSender}.
 */
@UtilityClass
public final class HardwareResponseParser {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    /**
     * Returns the {@link Hardware}s retrieved by {@link HardwareRequestSender#getAll()}.
     *
     * @param response the {@link HttpResponse} to parse
     * @return the retrieved {@link Hardware}s
     */
    public static Collection<Hardware> getAll(final HttpResponse<String> response) {
        final Type collectionType = new TypeToken<Collection<Hardware>>() {
        }.getType();
        return GSON.fromJson(response.body(), collectionType);
    }

    /**
     * Returns the {@link Hardware} retrieved by {@link HardwareRequestSender#get(int)}.
     *
     * @param response the {@link HttpResponse} to parse
     * @return the retrieved {@link Hardware}
     */
    public static Hardware get(final HttpResponse<String> response) {
        return GSON.fromJson(response.body(), Hardware.class);
    }

    /**
     * Returns the {@link Hardware} created by {@link HardwareRequestSender#create(Hardware)}.
     *
     * @param response the {@link HttpResponse} to parse
     * @return the created {@link Hardware}
     */
    public static Hardware create(final HttpResponse<String> response) {
        return GSON.fromJson(response.body(), Hardware.class);
    }

    /**
     * Returns the {@link Hardware} updated by {@link HardwareRequestSender#update(Hardware)}.
     *
     * @param response the {@link HttpResponse} to parse
     * @return the updated {@link Hardware}
     */
    public static Hardware update(final HttpResponse<String> response) {
        return GSON.fromJson(response.body(), Hardware.class);
    }
}
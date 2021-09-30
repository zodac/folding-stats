package me.zodac.folding.client.java.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.net.http.HttpClient;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Simple utility class holding some constants.
 */
public final class RestUtilConstants {

    /**
     * Instance of {@link Gson} with:
     * <ul>
     *     <li>Pretty-printing enabled</li>
     *     <li>HTML escaping disabled</li>
     *     <li>Custom {@link GsonLocalDateTimeDeserializer} for {@link LocalDateTime}</li>
     * </ul>
     */
    public static final Gson GSON = new GsonBuilder()
        .registerTypeAdapter(LocalDateTime.class, GsonLocalDateTimeDeserializer.getInstance())
        .setPrettyPrinting()
        .disableHtmlEscaping()
        .create();

    /**
     * Instance of {@link HttpClient} with:
     * <ul>
     *     <li>HTTP protocol of {@link HttpClient.Version#HTTP_2}</li>
     *     <li>A connection timeout of <b>10</b> {@link java.util.concurrent.TimeUnit#SECONDS}</li>
     * </ul>
     */
    public static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_2)
        .connectTimeout(Duration.ofSeconds(10))
        .build();

    private RestUtilConstants() {

    }
}

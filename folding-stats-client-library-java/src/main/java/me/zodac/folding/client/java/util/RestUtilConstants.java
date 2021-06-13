package me.zodac.folding.client.java.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.net.http.HttpClient;
import java.time.Duration;

/**
 * Simple utility class holding some constants.
 */
public class RestUtilConstants {

    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    public static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_1_1) // TODO: [zodac] What's wrong with HTTP_2?
        .connectTimeout(Duration.ofSeconds(10))
        .build();
}

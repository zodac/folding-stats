package me.zodac.folding.test.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.net.http.HttpClient;
import java.time.Duration;

/**
 * Constants class for test convenience.
 */
public class TestConstants {

    // URL
    public static final String TEST_SERVICE_URL = "http://192.168.99.100:8081";
    public static final String FOLDING_URL = TEST_SERVICE_URL + "/folding";

    // ID
    public static final int NON_EXISTING_ID = 9_999;
    public static final int OUT_OF_RANGE_ID = -1;
    public static final String INVALID_FORMAT_ID = "id";

    // REST
    public static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();
    public static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

}

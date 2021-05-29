package me.zodac.folding.test.utils;

import java.net.http.HttpClient;
import java.time.Duration;

/**
 * Constants class for test convenience.
 */
public class TestConstants {

    public static final String TEST_SERVICE_URL = "http://192.168.99.100:8081";
    public static final String FOLDING_URL = TEST_SERVICE_URL + "/folding";
    public static final int INVALID_ID = 9_999;
    public static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

}

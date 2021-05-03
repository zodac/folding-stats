package me.zodac.folding.test.utils;

import me.zodac.folding.api.tc.User;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class StubbedFoldingEndpointUtils {

    private static final String UNIT_URL_FORMAT = "http://192.168.99.100:8081/bonus?user=%s&passkey=%s&units=%s";
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private StubbedFoldingEndpointUtils() {

    }

    public static void enableUser(final User user) throws IOException, InterruptedException {
        setUnits(user, 1);
    }

    public static void disableUser(final User user) throws IOException, InterruptedException {
        setUnits(user, 0);
    }

    public static void setUnits(final User user, final int units) throws IOException, InterruptedException {
        final HttpRequest unitsRequest = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.noBody())
                .uri(URI.create(String.format(UNIT_URL_FORMAT, user.getFoldingUserName(), user.getPasskey(), units)))
                .header("Content-Type", "application/json")
                .build();

        HTTP_CLIENT.send(unitsRequest, HttpResponse.BodyHandlers.discarding());
    }
}

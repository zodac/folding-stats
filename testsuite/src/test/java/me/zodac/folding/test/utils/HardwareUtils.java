package me.zodac.folding.test.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import me.zodac.folding.api.tc.Hardware;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Collection;
import java.util.List;

// TODO: [zodac] Should move these to a client-library module later
public class HardwareUtils {

    private static final String BASE_FOLDING_URL = "http://192.168.99.100:8081/folding"; // TODO: [zodac] Use a hostname instead?
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private HardwareUtils() {

    }

    public static class RequestSender {

        private RequestSender() {

        }

        public static HttpResponse<String> getAll() {
            final HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create(BASE_FOLDING_URL + "/hardware"))
                    .header("Content-Type", "application/json")
                    .build();

            try {
                return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            } catch (final IOException | InterruptedException e) {
                throw new AssertionError("Error sending HTTP request to get all hardware", e);
            }
        }

        public static HttpResponse<String> get(final int hardwareId) {
            final HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create(BASE_FOLDING_URL + "/hardware/" + hardwareId))
                    .header("Content-Type", "application/json")
                    .build();

            try {
                return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            } catch (final IOException | InterruptedException e) {
                throw new AssertionError("Error sending HTTP request to get hardware", e);
            }
        }

        public static HttpResponse<String> create(final Hardware hardware) {
            final HttpRequest request = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(hardware)))
                    .uri(URI.create(BASE_FOLDING_URL + "/hardware"))
                    .header("Content-Type", "application/json")
                    .build();

            try {
                return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            } catch (final IOException | InterruptedException e) {
                throw new AssertionError("Error sending HTTP request to create hardware", e);
            }
        }

        public static HttpResponse<String> createBatchOf(final List<Hardware> batchOfHardware) {
            final HttpRequest request = HttpRequest.newBuilder()
                    .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(batchOfHardware)))
                    .uri(URI.create(BASE_FOLDING_URL + "/hardware/batch"))
                    .header("Content-Type", "application/json")
                    .build();

            try {
                return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            } catch (final IOException | InterruptedException e) {
                throw new AssertionError("Error sending HTTP request to create batch of hardware", e);
            }
        }

        public static HttpResponse<String> update(final Hardware hardware) {
            final HttpRequest request = HttpRequest.newBuilder()
                    .PUT(HttpRequest.BodyPublishers.ofString(GSON.toJson(hardware)))
                    .uri(URI.create(BASE_FOLDING_URL + "/hardware/" + hardware.getId()))
                    .header("Content-Type", "application/json")
                    .build();

            try {
                return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            } catch (final IOException | InterruptedException e) {
                throw new AssertionError("Error sending HTTP request to update hardware", e);
            }
        }

        public static HttpResponse<Void> delete(final int hardwareId) {
            final HttpRequest request = HttpRequest.newBuilder()
                    .DELETE()
                    .uri(URI.create(BASE_FOLDING_URL + "/hardware/" + hardwareId))
                    .header("Content-Type", "application/json")
                    .build();

            try {
                return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.discarding());
            } catch (final IOException | InterruptedException e) {
                throw new AssertionError("Error sending HTTP request to delete hardware", e);
            }
        }
    }

    public static class ResponseParser {

        private ResponseParser() {

        }

        public static Collection<Hardware> getAll(final HttpResponse<String> response) {
            final Type collectionType = new TypeToken<Collection<Hardware>>() {
            }.getType();
            return GSON.fromJson(response.body(), collectionType);
        }

        public static Hardware get(final HttpResponse<String> response) {
            return GSON.fromJson(response.body(), Hardware.class);
        }

        public static Hardware create(final HttpResponse<String> response) {
            return GSON.fromJson(response.body(), Hardware.class);
        }

        public static Hardware update(final HttpResponse<String> response) {
            return GSON.fromJson(response.body(), Hardware.class);
        }
    }
}

package me.zodac.folding.test;

import com.google.gson.Gson;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Arquillian.class)
public class HardwareIT {

    private static final String BASE_FOLDING_URL = "http://192.168.99.100:8081/folding"; // TODO: [zodac] Use a hostname instead?
    private static final Gson GSON = new Gson();
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Deployment(order = 1)
    public static EnterpriseArchive getTestEar() {
        return Deployments.getTestEar();
    }

    @InSequence(1)
    @RunAsClient
    @Test
    public void whenGettingHardware_andNoHardwareHasBeenCreated_thenAnEmptyJsonResponseIsReturnedWithStatusCode200() throws IOException, InterruptedException {
        final HttpRequest createRequest = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE_FOLDING_URL + "/hardware"))
                .header("Content-Type", "application/json")
                .build();

        final HttpResponse<String> response = HTTP_CLIENT.send(createRequest, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode())
                .as("Did not receive a 200_CREATED HTTP response")
                .isEqualTo(HttpURLConnection.HTTP_OK);

        assertThat(response.body())
                .as("Did not receive an empty JSON response")
                .isEqualTo(GSON.toJson(Collections.emptyList()));
    }
}

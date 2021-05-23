package me.zodac.folding.client.java.request;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.utils.EncodingUtils;
import me.zodac.folding.rest.api.exception.FoldingRestException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Convenience class to send HTTP requests to the {@link Hardware} REST endpoint.
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class LoginRequestSender {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final String foldingUrl;

    /**
     * Create an instance of {@link LoginRequestSender}.
     *
     * @param foldingUrl the root URL of the <code>/folding</code> endpoint, i.e:
     *                   <pre>http://127.0.0.1:8080/folding</pre>
     * @return the created {@link LoginRequestSender}
     */
    public static LoginRequestSender create(final String foldingUrl) {
        return new LoginRequestSender(foldingUrl);
    }

    /**
     * Send a <b>POST</b> request to login to the system as an admin.
     * <p>
     * The user name and password will be encoded using {@link EncodingUtils#encodeBasicAuthentication(String, String)}.
     *
     * @param userName the user name
     * @param password the password
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     */
    public HttpResponse<Void> loginAsAdmin(final String userName, final String password) throws FoldingRestException {
        final String encodedUserNameAndPassword = EncodingUtils.encodeBasicAuthentication(userName, password);
        return loginAsAdmin(encodedUserNameAndPassword);
    }

    /**
     * Send a <b>POST</b> request to login to the system as an admin.
     *
     * @param encodedUserNameAndPassword the encoded user name and password
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     */
    public HttpResponse<Void> loginAsAdmin(final String encodedUserNameAndPassword) throws FoldingRestException {
        final HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.noBody())
                .uri(URI.create(foldingUrl + "/login/admin"))
                .header("Content-Type", "application/json")
                .header("Authorization", encodedUserNameAndPassword)
                .build();

        try {
            return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.discarding());
        } catch (final IOException | InterruptedException e) {
            throw new FoldingRestException("Error sending HTTP request to login as admin", e);
        }
    }


}

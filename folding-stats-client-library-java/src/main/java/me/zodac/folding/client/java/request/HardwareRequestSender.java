package me.zodac.folding.client.java.request;

import static me.zodac.folding.api.utils.EncodingUtils.encodeBasicAuthentication;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collection;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.client.java.util.RestUtilConstants;
import me.zodac.folding.rest.api.exception.FoldingRestException;
import me.zodac.folding.rest.api.header.ContentType;
import me.zodac.folding.rest.api.header.RestHeader;
import me.zodac.folding.rest.api.tc.request.HardwareRequest;
import org.apache.commons.lang3.StringUtils;

/**
 * Convenience class to send HTTP requests to the {@link Hardware} REST endpoint.
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class HardwareRequestSender {

    private final String hardwareUrl;

    /**
     * Create an instance of {@link HardwareRequestSender}.
     *
     * @param foldingUrl the root URL of the <code>/folding</code> endpoint, i.e:
     *                   <pre>http://127.0.0.1:8080/folding</pre>
     * @return the created {@link HardwareRequestSender}
     */
    public static HardwareRequestSender createWithUrl(final String foldingUrl) {
        final String hardwareUrl = foldingUrl + "/hardware";
        return new HardwareRequestSender(hardwareUrl);
    }

    /**
     * Send a <b>GET</b> request to retrieve all {@link Hardware}s in the system.
     *
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     * @see #getAll(String)
     */
    public HttpResponse<String> getAll() throws FoldingRestException {
        return getAll(null);
    }

    /**
     * Send a <b>GET</b> request to retrieve all {@link Hardware}s in the system.
     *
     * <p>
     * <b>NOTE:</b> If the server has a cached {@link Hardware} based on the <code>ETag</code>, an empty {@link HttpResponse#body()} is returned.
     *
     * @param entityTag the <code>ETag</code> from a previous {@link HttpResponse}, to retrieve cached {@link Hardware}
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     * @see #getAll()
     */
    public HttpResponse<String> getAll(final String entityTag) throws FoldingRestException {
        final HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
            .GET()
            .uri(URI.create(hardwareUrl))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentType());

        if (StringUtils.isNotBlank(entityTag)) {
            requestBuilder.header(RestHeader.IF_NONE_MATCH.headerName(), entityTag);
        }

        final HttpRequest request = requestBuilder.build();

        try {
            return RestUtilConstants.HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (final IOException | InterruptedException e) {
            throw new FoldingRestException("Error sending HTTP request to get all hardware", e);
        }
    }

    /**
     * Send a <b>GET</b> request to retrieve a single {@link Hardware} with the given {@code hardwareId}.
     *
     * @param hardwareId the ID of the {@link Hardware} to be retrieved
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     * @see #get(int, String)
     */
    public HttpResponse<String> get(final int hardwareId) throws FoldingRestException {
        return get(hardwareId, null);
    }

    /**
     * Send a <b>GET</b> request to retrieve a single {@link Hardware} with the given {@code hardwareId}.
     *
     * <p>
     * <b>NOTE:</b> If the server has a cached {@link Hardware} based on the <code>ETag</code>, an empty {@link HttpResponse#body()} is returned.
     *
     * @param hardwareId the ID of the {@link Hardware} to be retrieved
     * @param entityTag  the <code>ETag</code> from a previous {@link HttpResponse}, to retrieve a cached {@link Hardware}
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     * @see #get(int)
     */
    public HttpResponse<String> get(final int hardwareId, final String entityTag) throws FoldingRestException {
        final HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
            .GET()
            .uri(URI.create(hardwareUrl + '/' + hardwareId))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentType());

        if (StringUtils.isNotBlank(entityTag)) {
            requestBuilder.header(RestHeader.IF_NONE_MATCH.headerName(), entityTag);
        }

        final HttpRequest request = requestBuilder.build();

        try {
            return RestUtilConstants.HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (final IOException | InterruptedException e) {
            throw new FoldingRestException("Error sending HTTP request to get hardware", e);
        }
    }

    /**
     * Send a <b>POST</b> request to create the given {@link HardwareRequest} in the system.
     *
     * @param hardware the {@link HardwareRequest} to create
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     */
    public HttpResponse<String> create(final HardwareRequest hardware) throws FoldingRestException {
        return create(hardware, null, null);
    }

    /**
     * Send a <b>POST</b> request to create the given {@link HardwareRequest} in the system, using the supplied {@code userName}
     * and {@code password} for authentication.
     *
     * @param hardware the {@link HardwareRequest} to create
     * @param userName the user name
     * @param password the password
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     */
    public HttpResponse<String> create(final HardwareRequest hardware, final String userName, final String password) throws FoldingRestException {
        final HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
            .POST(HttpRequest.BodyPublishers.ofString(RestUtilConstants.GSON.toJson(hardware)))
            .uri(URI.create(hardwareUrl))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentType());

        if (StringUtils.isNoneBlank(userName, password)) {
            requestBuilder.header(RestHeader.AUTHORIZATION.headerName(), encodeBasicAuthentication(userName, password));
        }

        final HttpRequest request = requestBuilder.build();

        try {
            return RestUtilConstants.HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (final IOException | InterruptedException e) {
            throw new FoldingRestException("Error sending HTTP request to create hardware", e);
        }
    }

    /**
     * Send a <b>POST</b> request to create the given {@link HardwareRequest}s in the system.
     *
     * @param batchOfHardware the {@link Collection} of {@link HardwareRequest}s to create
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     */
    public HttpResponse<String> createBatchOf(final Collection<HardwareRequest> batchOfHardware) throws FoldingRestException {
        return createBatchOf(batchOfHardware, null, null);
    }

    /**
     * Send a <b>POST</b> request to create the given {@link HardwareRequest}s in the system.
     *
     * @param batchOfHardware the {@link Collection} of {@link HardwareRequest}s to create
     * @param userName        the user name
     * @param password        the password
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     */
    public HttpResponse<String> createBatchOf(final Collection<HardwareRequest> batchOfHardware, final String userName, final String password)
        throws FoldingRestException {
        final HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
            .POST(HttpRequest.BodyPublishers.ofString(RestUtilConstants.GSON.toJson(batchOfHardware)))
            .uri(URI.create(hardwareUrl + "/batch"))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentType());

        if (StringUtils.isNoneBlank(userName, password)) {
            requestBuilder.header(RestHeader.AUTHORIZATION.headerName(), encodeBasicAuthentication(userName, password));
        }

        final HttpRequest request = requestBuilder.build();

        try {
            return RestUtilConstants.HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (final IOException | InterruptedException e) {
            throw new FoldingRestException("Error sending HTTP request to create batch of hardware", e);
        }
    }

    /**
     * Send a <b>PUT</b> request to update the given {@link HardwareRequest} in the system.
     *
     * @param hardwareId the ID of the {@link Hardware} to update
     * @param hardware   the {@link HardwareRequest} to update
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     */
    public HttpResponse<String> update(final int hardwareId, final HardwareRequest hardware) throws FoldingRestException {
        return update(hardwareId, hardware, null, null);
    }

    /**
     * Send a <b>PUT</b> request to update the given {@link HardwareRequest} in the system.
     *
     * @param hardwareId the ID of the {@link Hardware} to update
     * @param hardware   the {@link HardwareRequest} to update
     * @param userName   the user name
     * @param password   the password
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     */
    public HttpResponse<String> update(final int hardwareId, final HardwareRequest hardware, final String userName, final String password)
        throws FoldingRestException {
        final HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
            .PUT(HttpRequest.BodyPublishers.ofString(RestUtilConstants.GSON.toJson(hardware)))
            .uri(URI.create(hardwareUrl + '/' + hardwareId))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentType());

        if (StringUtils.isNoneBlank(userName, password)) {
            requestBuilder.header(RestHeader.AUTHORIZATION.headerName(), encodeBasicAuthentication(userName, password));
        }

        final HttpRequest request = requestBuilder.build();

        try {
            return RestUtilConstants.HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (final IOException | InterruptedException e) {
            throw new FoldingRestException("Error sending HTTP request to update hardware", e);
        }
    }

    /**
     * Send a <b>DELETE</b> request to remove a {@link Hardware} with the given {@code hardwareId}.
     *
     * @param hardwareId the ID of the {@link Hardware} to remove
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     */
    public HttpResponse<Void> delete(final int hardwareId) throws FoldingRestException {
        return delete(hardwareId, null, null);
    }

    /**
     * Send a <b>DELETE</b> request to remove a {@link Hardware} with the given {@code hardwareId}.
     *
     * @param hardwareId the ID of the {@link Hardware} to remove
     * @param userName   the user name
     * @param password   the password
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     */
    public HttpResponse<Void> delete(final int hardwareId, final String userName, final String password) throws FoldingRestException {
        final HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
            .DELETE()
            .uri(URI.create(hardwareUrl + '/' + hardwareId))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentType());

        if (StringUtils.isNoneBlank(userName, password)) {
            requestBuilder.header(RestHeader.AUTHORIZATION.headerName(), encodeBasicAuthentication(userName, password));
        }

        final HttpRequest request = requestBuilder.build();

        try {
            return RestUtilConstants.HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.discarding());
        } catch (final IOException | InterruptedException e) {
            throw new FoldingRestException("Error sending HTTP request to delete hardware", e);
        }
    }
}

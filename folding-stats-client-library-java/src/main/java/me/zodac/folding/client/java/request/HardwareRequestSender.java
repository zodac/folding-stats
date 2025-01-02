/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2025 zodac.me
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package me.zodac.folding.client.java.request;

import static me.zodac.folding.api.util.EncodingUtils.encodeBasicAuthentication;

import java.io.IOException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import me.zodac.folding.api.util.StringUtils;
import me.zodac.folding.rest.api.exception.FoldingRestException;
import me.zodac.folding.rest.api.header.ContentType;
import me.zodac.folding.rest.api.header.RestHeader;
import me.zodac.folding.rest.api.tc.request.HardwareRequest;
import me.zodac.folding.rest.api.util.RestUtilConstants;
import org.jspecify.annotations.Nullable;

/**
 * Convenience class to send HTTP requests to the {@link me.zodac.folding.api.tc.Hardware} REST endpoint.
 *
 * @param hardwareUrl the URL to the {@link me.zodac.folding.api.tc.Hardware} REST endpoint
 */
public record HardwareRequestSender(String hardwareUrl) {

    /**
     * Create an instance of {@link HardwareRequestSender}.
     *
     * @param foldingUrl the root URL of the {@code /folding} endpoint, i.e: <pre>http://127.0.0.1:8080/folding</pre>
     * @return the created {@link HardwareRequestSender}
     */
    public static HardwareRequestSender createWithUrl(final String foldingUrl) {
        final String hardwareUrl = foldingUrl + RestUri.REST_URI_PATH_SEPARATOR + "hardware";
        return new HardwareRequestSender(hardwareUrl);
    }

    /**
     * Send a <b>GET</b> request to retrieve all {@link me.zodac.folding.api.tc.Hardware}s in the system.
     *
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     * @see #getAll(String)
     */
    public HttpResponse<String> getAll() throws FoldingRestException {
        return getAll(null);
    }

    /**
     * Send a <b>GET</b> request to retrieve all {@link me.zodac.folding.api.tc.Hardware}s in the system.
     *
     * <p>
     * <b>NOTE:</b> If the server has a cached {@link me.zodac.folding.api.tc.Hardware} based on the {@code ETag}, an empty
     * {@link HttpResponse#body()} is returned.
     *
     * @param entityTag the {@code ETag} from a previous {@link HttpResponse}, to retrieve cached {@link me.zodac.folding.api.tc.Hardware}
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     * @see #getAll()
     */
    public HttpResponse<String> getAll(final @Nullable String entityTag) throws FoldingRestException {
        final HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
            .GET()
            .uri(RestUri.create(hardwareUrl))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentTypeValue());

        if (StringUtils.isNotBlank(entityTag)) {
            requestBuilder.header(RestHeader.IF_NONE_MATCH.headerName(), entityTag);
        }

        final HttpRequest request = requestBuilder.build();

        try {
            return RestUtilConstants.HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FoldingRestException("Error sending HTTP request to get all hardware", e);
        } catch (final IOException e) {
            throw new FoldingRestException("Error sending HTTP request to get all hardware", e);
        }
    }

    /**
     * Send a <b>GET</b> request to retrieve a single {@link me.zodac.folding.api.tc.Hardware} with the given {@code hardwareId}.
     *
     * @param hardwareId the ID of the {@link me.zodac.folding.api.tc.Hardware} to be retrieved
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     * @see #get(int, String)
     */
    public HttpResponse<String> get(final int hardwareId) throws FoldingRestException {
        return get(hardwareId, null);
    }

    /**
     * Send a <b>GET</b> request to retrieve a single {@link me.zodac.folding.api.tc.Hardware} with the given {@code hardwareId}.
     *
     * <p>
     * <b>NOTE:</b> If the server has a cached {@link me.zodac.folding.api.tc.Hardware} based on the {@code ETag}, an empty
     * {@link HttpResponse#body()} is returned.
     *
     * @param hardwareId the ID of the {@link me.zodac.folding.api.tc.Hardware} to be retrieved
     * @param entityTag  the {@code ETag} from a previous {@link HttpResponse}, to retrieve a cached {@link me.zodac.folding.api.tc.Hardware}
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     * @see #get(int)
     */
    public HttpResponse<String> get(final int hardwareId, final @Nullable String entityTag) throws FoldingRestException {
        final HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
            .GET()
            .uri(RestUri.create(hardwareUrl, hardwareId))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentTypeValue());

        if (StringUtils.isNotBlank(entityTag)) {
            requestBuilder.header(RestHeader.IF_NONE_MATCH.headerName(), entityTag);
        }

        final HttpRequest request = requestBuilder.build();

        try {
            return RestUtilConstants.HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FoldingRestException("Error sending HTTP request to get hardware", e);
        } catch (final IOException e) {
            throw new FoldingRestException("Error sending HTTP request to get hardware", e);
        }
    }

    /**
     * Send a <b>GET</b> request to retrieve a single {@link me.zodac.folding.api.tc.Hardware} with the given {@code hardwareName}.
     *
     * @param hardwareName the {@code hardwareName} of the {@link me.zodac.folding.api.tc.Hardware} to be retrieved
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     * @see #get(String, String)
     */
    public HttpResponse<String> get(final String hardwareName) throws FoldingRestException {
        return get(hardwareName, null);
    }

    /**
     * Send a <b>GET</b> request to retrieve a single {@link me.zodac.folding.api.tc.Hardware} with the given {@code hardwareName}.
     *
     * <p>
     * <b>NOTE:</b> If the server has a cached {@link me.zodac.folding.api.tc.Hardware} based on the {@code ETag}, an empty
     * {@link HttpResponse#body()} is returned.
     *
     * @param hardwareName the {@code hardwareName} of the {@link me.zodac.folding.api.tc.Hardware} to be retrieved
     * @param entityTag    the {@code ETag} from a previous {@link HttpResponse}, to retrieve a cached {@link me.zodac.folding.api.tc.Hardware}
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     * @see #get(String)
     */
    public HttpResponse<String> get(final String hardwareName, final @Nullable String entityTag) throws FoldingRestException {
        final HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
            .GET()
            .uri(RestUri.createWithFields(hardwareUrl, "hardwareName", hardwareName))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentTypeValue());

        if (StringUtils.isNotBlank(entityTag)) {
            requestBuilder.header(RestHeader.IF_NONE_MATCH.headerName(), entityTag);
        }

        final HttpRequest request = requestBuilder.build();

        try {
            return RestUtilConstants.HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FoldingRestException("Error sending HTTP request to get hardware", e);
        } catch (final IOException e) {
            throw new FoldingRestException("Error sending HTTP request to get hardware", e);
        }
    }

    /**
     * Send a <b>POST</b> request to create the given {@link HardwareRequest} in the system, using the supplied {@code userName}
     * and {@code password} for authentication.
     *
     * @param hardware the {@link HardwareRequest} to create
     * @param userName the username
     * @param password the password
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     */
    public HttpResponse<String> create(final HardwareRequest hardware, final String userName, final String password) throws FoldingRestException {
        final HttpRequest request = HttpRequest.newBuilder()
            .POST(HttpRequest.BodyPublishers.ofString(RestUtilConstants.GSON.toJson(hardware)))
            .uri(RestUri.create(hardwareUrl))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentTypeValue())
            .header(RestHeader.AUTHORIZATION.headerName(), encodeBasicAuthentication(userName, password))
            .build();

        try {
            return RestUtilConstants.HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FoldingRestException("Error sending HTTP request to create hardware", e);
        } catch (final IOException e) {
            throw new FoldingRestException("Error sending HTTP request to create hardware", e);
        }
    }

    /**
     * Send a <b>PUT</b> request to update the given {@link HardwareRequest} in the system.
     *
     * @param hardwareId the ID of the {@link me.zodac.folding.api.tc.Hardware} to update
     * @param hardware   the {@link HardwareRequest} to update
     * @param userName   the username
     * @param password   the password
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     */
    public HttpResponse<String> update(final int hardwareId, final HardwareRequest hardware, final String userName, final String password)
        throws FoldingRestException {
        final HttpRequest request = HttpRequest.newBuilder()
            .PUT(HttpRequest.BodyPublishers.ofString(RestUtilConstants.GSON.toJson(hardware)))
            .uri(RestUri.create(hardwareUrl, hardwareId))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentTypeValue())
            .header(RestHeader.AUTHORIZATION.headerName(), encodeBasicAuthentication(userName, password))
            .build();

        try {
            return RestUtilConstants.HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FoldingRestException("Error sending HTTP request to update hardware", e);
        } catch (final IOException e) {
            throw new FoldingRestException("Error sending HTTP request to update hardware", e);
        }
    }

    /**
     * Send a <b>DELETE</b> request to remove a {@link me.zodac.folding.api.tc.Hardware} with the given {@code hardwareId}.
     *
     * @param hardwareId the ID of the {@link me.zodac.folding.api.tc.Hardware} to remove
     * @param userName   the username
     * @param password   the password
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     */
    public HttpResponse<Void> delete(final int hardwareId, final String userName, final String password) throws FoldingRestException {
        final HttpRequest request = HttpRequest.newBuilder()
            .DELETE()
            .uri(RestUri.create(hardwareUrl, hardwareId))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentTypeValue())
            .header(RestHeader.AUTHORIZATION.headerName(), encodeBasicAuthentication(userName, password))
            .build();

        try {
            return RestUtilConstants.HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.discarding());
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FoldingRestException("Error sending HTTP request to delete hardware", e);
        } catch (final IOException e) {
            throw new FoldingRestException("Error sending HTTP request to delete hardware", e);
        }
    }
}

/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2024 zodac.me
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

import java.io.IOException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import me.zodac.folding.api.util.EncodingUtils;
import me.zodac.folding.rest.api.LoginCredentials;
import me.zodac.folding.rest.api.exception.FoldingRestException;
import me.zodac.folding.rest.api.header.ContentType;
import me.zodac.folding.rest.api.header.RestHeader;
import me.zodac.folding.rest.api.util.RestUtilConstants;

/**
 * Convenience class to send HTTP requests to the {@link LoginCredentials} REST endpoint.
 *
 * @param loginUrl the URL to the {@link LoginCredentials} REST endpoint
 */
public record LoginRequestSender(String loginUrl) {

    /**
     * Create an instance of {@link LoginRequestSender}.
     *
     * @param foldingUrl the root URL of the {@code /folding} endpoint, i.e:
     *                   <pre>http://127.0.0.1:8080/folding</pre>
     * @return the created {@link LoginRequestSender}
     */
    public static LoginRequestSender createWithUrl(final String foldingUrl) {
        final String loginUrl = foldingUrl + RestUri.REST_URI_PATH_SEPARATOR + "login";
        return new LoginRequestSender(loginUrl);
    }

    /**
     * Send a <b>POST</b> request to log in to the system as an admin.
     *
     * <p>
     * The username and password will be encoded using {@link EncodingUtils#encodeBasicAuthentication(String, String)}.
     *
     * @param userName the username
     * @param password the password
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     */
    public HttpResponse<Void> loginAsAdmin(final String userName, final String password) throws FoldingRestException {
        final String encodedUserNameAndPassword = EncodingUtils.encodeBasicAuthentication(userName, password);
        return loginAsAdmin(encodedUserNameAndPassword);
    }

    /**
     * Send a <b>POST</b> request to log in to the system as an admin.
     *
     * @param encodedUserNameAndPassword the encoded username and password
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     */
    public HttpResponse<Void> loginAsAdmin(final String encodedUserNameAndPassword) throws FoldingRestException {
        final LoginCredentials loginCredentials = LoginCredentials.createWithBasicAuthentication(encodedUserNameAndPassword);

        final HttpRequest request = HttpRequest.newBuilder()
            .POST(HttpRequest.BodyPublishers.ofString(RestUtilConstants.GSON.toJson(loginCredentials)))
            .uri(RestUri.create(loginUrl, "admin"))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentTypeValue())
            .build();

        try {
            return RestUtilConstants.HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.discarding());
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FoldingRestException("Error sending HTTP request to login as admin", e);
        } catch (final IOException e) {
            throw new FoldingRestException("Error sending HTTP request to login as admin", e);
        }
    }
}

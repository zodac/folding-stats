/*
 * MIT License
 *
 * Copyright (c) 2021-2022 zodac.me
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.zodac.folding.client.java.request;

import java.io.IOException;
import java.net.URI;
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
        final String loginUrl = foldingUrl + "/login";
        return new LoginRequestSender(loginUrl);
    }

    /**
     * Send a <b>POST</b> request to login to the system as an admin.
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
     * Send a <b>POST</b> request to login to the system as an admin.
     *
     * @param encodedUserNameAndPassword the encoded username and password
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     */
    public HttpResponse<Void> loginAsAdmin(final String encodedUserNameAndPassword) throws FoldingRestException {
        final LoginCredentials loginCredentials = LoginCredentials.createWithBasicAuthentication(encodedUserNameAndPassword);

        final HttpRequest request = HttpRequest.newBuilder()
            .POST(HttpRequest.BodyPublishers.ofString(RestUtilConstants.GSON.toJson(loginCredentials)))
            .uri(URI.create(loginUrl + "/admin"))
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

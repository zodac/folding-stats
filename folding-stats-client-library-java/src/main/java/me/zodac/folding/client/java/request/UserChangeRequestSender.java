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
import java.util.Collection;
import java.util.stream.Collectors;
import me.zodac.folding.api.tc.change.UserChange;
import me.zodac.folding.api.tc.change.UserChangeState;
import me.zodac.folding.rest.api.exception.FoldingRestException;
import me.zodac.folding.rest.api.header.ContentType;
import me.zodac.folding.rest.api.header.RestHeader;
import me.zodac.folding.rest.api.tc.request.UserChangeRequest;
import me.zodac.folding.rest.api.tc.request.UserRequest;
import me.zodac.folding.rest.api.util.RestUtilConstants;

/**
 * Convenience class to send HTTP requests to the {@link UserChange} REST endpoint.
 *
 * @param requestUrl the URL to the {@link UserChange} REST endpoint
 */
public record UserChangeRequestSender(String requestUrl) {

    private static final String USER_CHANGE_REQUEST_APPROVE_IMMEDIATE_URL = "approve" + RestUri.REST_URI_PATH_SEPARATOR + "immediate";
    private static final String USER_CHANGE_REQUEST_APPROVE_NEXT_URL = "approve" + RestUri.REST_URI_PATH_SEPARATOR + "next";
    private static final String USER_CHANGE_REQUEST_REJECT_URL = "reject";

    /**
     * Create an instance of {@link UserChangeRequestSender}.
     *
     * @param foldingUrl the root URL of the {@code /folding} endpoint, i.e:
     *                   <pre>http://127.0.0.1:8080/folding</pre>
     * @return the created {@link UserChangeRequestSender}
     */
    public static UserChangeRequestSender createWithUrl(final String foldingUrl) {
        final String requestUrl = foldingUrl + RestUri.REST_URI_PATH_SEPARATOR + "changes";
        return new UserChangeRequestSender(requestUrl);
    }

    /**
     * Send a <b>GET</b> request to retrieve all {@link UserChange}s in the system, with passkeys shown.
     *
     * @param userName the username
     * @param password the password
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     */
    public HttpResponse<String> getAllWithPasskeys(final String userName, final String password) throws FoldingRestException {
        final HttpRequest request = HttpRequest.newBuilder()
            .GET()
            .uri(RestUri.create(requestUrl, "passkey"))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentTypeValue())
            .header(RestHeader.AUTHORIZATION.headerName(), encodeBasicAuthentication(userName, password))
            .build();

        try {
            return RestUtilConstants.HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FoldingRestException("Error sending HTTP request to get all user changes with passkeys", e);
        } catch (final IOException e) {
            throw new FoldingRestException("Error sending HTTP request to get all user changes with passkeys", e);
        }
    }

    /**
     * Send a <b>GET</b> request to retrieve all {@link UserChange}s in the system with a specific {@link UserChangeState}, with passkeys shown.
     *
     * @param userChangeStates the {@link UserChangeState}s to look for
     * @param userName         the username
     * @param password         the password
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     */
    public HttpResponse<String> getAllWithPasskeys(final Collection<UserChangeState> userChangeStates, final String userName,
                                                   final String password) throws FoldingRestException {
        final String commaSeparatedStates = userChangeStates
            .stream()
            .map(UserChangeState::toString)
            .collect(Collectors.joining(","));

        final HttpRequest request = HttpRequest.newBuilder()
            .GET()
            .uri(RestUri.createWithFilter(requestUrl + RestUri.REST_URI_PATH_SEPARATOR + "passkey", "state", commaSeparatedStates))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentTypeValue())
            .header(RestHeader.AUTHORIZATION.headerName(), encodeBasicAuthentication(userName, password))
            .build();

        try {
            return RestUtilConstants.HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FoldingRestException("Error sending HTTP request to get all user changes with states with passkeys", e);
        } catch (final IOException e) {
            throw new FoldingRestException("Error sending HTTP request to get all user changes with states with passkeys", e);
        }
    }

    /**
     * Send a <b>GET</b> request to retrieve all {@link UserChange}s in the system, with passkeys hidden.
     *
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     */
    public HttpResponse<String> getAllWithoutPasskeys() throws FoldingRestException {
        final HttpRequest request = HttpRequest.newBuilder()
            .GET()
            .uri(RestUri.create(requestUrl))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentTypeValue())
            .build();

        try {
            return RestUtilConstants.HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FoldingRestException("Error sending HTTP request to get all user changes without passkeys", e);
        } catch (final IOException e) {
            throw new FoldingRestException("Error sending HTTP request to get all user changes without passkeys", e);
        }
    }

    /**
     * Send a <b>GET</b> request to retrieve all {@link UserChange}s in the system with a specific {@link UserChangeState}, with passkeys hidden.
     *
     * @param userChangeStates the {@link UserChangeState}s to look for
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     */
    public HttpResponse<String> getAllWithoutPasskeys(final Collection<UserChangeState> userChangeStates) throws FoldingRestException {
        final String commaSeparatedStates = userChangeStates
            .stream()
            .map(UserChangeState::toString)
            .collect(Collectors.joining(","));

        final HttpRequest request = HttpRequest.newBuilder()
            .GET()
            .uri(RestUri.createWithFilter(requestUrl, "state", commaSeparatedStates))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentTypeValue())
            .build();

        try {
            return RestUtilConstants.HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FoldingRestException("Error sending HTTP request to get all user changes with states without passkeys", e);
        } catch (final IOException e) {
            throw new FoldingRestException("Error sending HTTP request to get all user changes with states without passkeys", e);
        }
    }

    /**
     * Send a <b>GET</b> request to retrieve a {@link UserChange} with the provided {@code userChangeId} in the system.
     *
     * @param userChangeId the ID of the {@link UserChange} to be retrieved
     * @param userName     the username
     * @param password     the password
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     */
    public HttpResponse<String> get(final int userChangeId, final String userName, final String password) throws FoldingRestException {
        final HttpRequest request = HttpRequest.newBuilder()
            .GET()
            .uri(RestUri.create(requestUrl, userChangeId))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentTypeValue())
            .header(RestHeader.AUTHORIZATION.headerName(), encodeBasicAuthentication(userName, password))
            .build();

        try {
            return RestUtilConstants.HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FoldingRestException("Error sending HTTP request to get user change", e);
        } catch (final IOException e) {
            throw new FoldingRestException("Error sending HTTP request to get user change", e);
        }
    }

    /**
     * Send a <b>POST</b> request to create the given {@link UserChangeRequest} in the system.
     *
     * @param userChangeRequest the {@link UserRequest} to create
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     */
    public HttpResponse<String> create(final UserChangeRequest userChangeRequest) throws FoldingRestException {
        final HttpRequest request = HttpRequest.newBuilder()
            .POST(HttpRequest.BodyPublishers.ofString(RestUtilConstants.GSON.toJson(userChangeRequest)))
            .uri(RestUri.create(requestUrl))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentTypeValue())
            .build();

        try {
            return RestUtilConstants.HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FoldingRestException("Error sending HTTP request to create user change", e);
        } catch (final IOException e) {
            throw new FoldingRestException("Error sending HTTP request to create user change", e);
        }
    }

    /**
     * Send a <b>PUT</b> request to reject the given {@link UserChange} in the system.
     *
     * @param userChangeId the ID of the {@link UserChange} to update
     * @param userName     the username
     * @param password     the password
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     */
    public HttpResponse<String> reject(final int userChangeId, final String userName, final String password) throws FoldingRestException {
        return update(userChangeId, USER_CHANGE_REQUEST_REJECT_URL, userName, password);
    }

    /**
     * Send a <b>PUT</b> request to approve immediately the given {@link UserChange} in the system.
     *
     * @param userChangeId the ID of the {@link UserChange} to update
     * @param userName     the username
     * @param password     the password
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     */
    public HttpResponse<String> approveImmediately(final int userChangeId, final String userName, final String password)
        throws FoldingRestException {
        return update(userChangeId, USER_CHANGE_REQUEST_APPROVE_IMMEDIATE_URL, userName, password);
    }

    /**
     * Send a <b>PUT</b> request to approve next month the given {@link UserChange} in the system.
     *
     * @param userChangeId the ID of the {@link UserChange} to update
     * @param userName     the username
     * @param password     the password
     * @return the {@link HttpResponse} from the {@link HttpRequest}
     * @throws FoldingRestException thrown if an error occurs sending the {@link HttpRequest}
     */
    public HttpResponse<String> approveNextMonth(final int userChangeId, final String userName, final String password)
        throws FoldingRestException {
        return update(userChangeId, USER_CHANGE_REQUEST_APPROVE_NEXT_URL, userName, password);
    }

    private HttpResponse<String> update(final int userChangeId, final String endpoint, final String userName, final String password)
        throws FoldingRestException {
        final HttpRequest request = HttpRequest.newBuilder()
            .PUT(HttpRequest.BodyPublishers.noBody())
            .uri(RestUri.create(requestUrl, userChangeId, endpoint))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentTypeValue())
            .header(RestHeader.AUTHORIZATION.headerName(), encodeBasicAuthentication(userName, password))
            .build();

        try {
            return RestUtilConstants.HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FoldingRestException("Error sending HTTP request to update the user change", e);
        } catch (final IOException e) {
            throw new FoldingRestException("Error sending HTTP request to update the user change", e);
        }
    }
}

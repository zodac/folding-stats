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

package me.zodac.folding.test.integration.util.rest.request;

import static me.zodac.folding.test.integration.util.DummyAuthenticationData.ADMIN_USER;
import static me.zodac.folding.test.integration.util.rest.response.HttpResponseHeaderUtils.getTotalCount;

import java.net.HttpURLConnection;
import java.net.http.HttpResponse;
import java.util.Collection;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.client.java.request.TeamRequestSender;
import me.zodac.folding.client.java.response.TeamResponseParser;
import me.zodac.folding.rest.api.exception.FoldingRestException;
import me.zodac.folding.rest.api.tc.request.TeamRequest;
import me.zodac.folding.test.integration.util.TestConstants;

/**
 * Utility class for {@link Team}-based tests.
 */
public final class TeamUtils {

    public static final TeamRequestSender TEAM_REQUEST_SENDER = TeamRequestSender.createWithUrl(TestConstants.FOLDING_URL);

    private TeamUtils() {

    }

    /**
     * Creates the given {@link TeamRequest}.
     *
     * @param team the {@link TeamRequest} to create
     * @return the created {@link Team}
     * @throws FoldingRestException thrown if an error occurs creating the {@link Team}
     */
    public static Team create(final TeamRequest team) throws FoldingRestException {
        final HttpResponse<String> response = TEAM_REQUEST_SENDER.create(team, ADMIN_USER.userName(), ADMIN_USER.password());
        if (response.statusCode() == HttpURLConnection.HTTP_CREATED) {
            return TeamResponseParser.create(response);
        }

        throw new FoldingRestException(String.format("Invalid response (%s) when creating team: %s", response.statusCode(), response.body()));
    }

    /**
     * Retrieves all {@link Team}s.
     *
     * @return the {@link Team}s
     * @throws FoldingRestException thrown if an error occurs retrieving the {@link Team}s
     */
    public static Collection<Team> getAll() throws FoldingRestException {
        final HttpResponse<String> response = TEAM_REQUEST_SENDER.getAll();
        if (response.statusCode() == HttpURLConnection.HTTP_OK) {
            return TeamResponseParser.getAll(response);
        }

        throw new FoldingRestException(
            String.format("Invalid response (%s) when getting all teams with: %s", response.statusCode(), response.body()));
    }

    /**
     * Retrieves the number of {@link Team}s.
     *
     * @return the number of {@link Team}s
     * @throws FoldingRestException thrown if an error occurs retrieving the {@link Team} count
     */
    public static int getNumberOfTeams() throws FoldingRestException {
        final HttpResponse<String> response = TEAM_REQUEST_SENDER.getAll();
        return getTotalCount(response);
    }

    /**
     * Retrieves a {@link Team} with the given ID.
     *
     * @param teamId the ID of the {@link Team} to retrieve
     * @return the {@link Team}
     * @throws FoldingRestException thrown if an error occurs retrieving the {@link Team}
     */
    public static Team get(final int teamId) throws FoldingRestException {
        final HttpResponse<String> response = TEAM_REQUEST_SENDER.get(teamId);
        if (response.statusCode() == HttpURLConnection.HTTP_OK) {
            return TeamResponseParser.get(response);
        }

        throw new FoldingRestException(
            String.format("Invalid response (%s) when getting team with ID %s: %s", response.statusCode(), teamId, response.body()));
    }
}

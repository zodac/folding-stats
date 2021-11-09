/*
 * MIT License
 *
 * Copyright (c) 2021 zodac.me
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

package me.zodac.folding.test.util.rest.request;

import static me.zodac.folding.test.util.TestAuthenticationData.ADMIN_USER;
import static me.zodac.folding.test.util.TestConstants.FOLDING_URL;
import static me.zodac.folding.test.util.rest.response.HttpResponseHeaderUtils.getTotalCount;

import java.net.HttpURLConnection;
import java.net.http.HttpResponse;
import java.util.Collection;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.client.java.request.TeamRequestSender;
import me.zodac.folding.client.java.response.TeamResponseParser;
import me.zodac.folding.rest.api.exception.FoldingRestException;
import me.zodac.folding.rest.api.tc.request.TeamRequest;

/**
 * Utility class for {@link Team}-based tests.
 */
public final class TeamUtils {

    public static final TeamRequestSender TEAM_REQUEST_SENDER = TeamRequestSender.createWithUrl(FOLDING_URL);

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

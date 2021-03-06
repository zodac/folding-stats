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

package me.zodac.folding.test.integration.util.rest.request;

import static me.zodac.folding.test.integration.util.TestAuthenticationData.ADMIN_USER;
import static org.assertj.core.api.Assertions.assertThat;

import java.net.HttpURLConnection;
import java.net.http.HttpResponse;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.client.java.request.TeamCompetitionStatsRequestSender;
import me.zodac.folding.client.java.response.TeamCompetitionStatsResponseParser;
import me.zodac.folding.rest.api.exception.FoldingRestException;
import me.zodac.folding.rest.api.tc.AllTeamsSummary;
import me.zodac.folding.rest.api.tc.RetiredUserSummary;
import me.zodac.folding.rest.api.tc.TeamSummary;
import me.zodac.folding.rest.api.tc.UserSummary;
import me.zodac.folding.test.integration.util.TestConstants;

/**
 * Utility class for TC stats-based tests.
 */
public final class TeamCompetitionStatsUtils {

    public static final TeamCompetitionStatsRequestSender TEAM_COMPETITION_REQUEST_SENDER =
        TeamCompetitionStatsRequestSender.createWithUrl(TestConstants.FOLDING_URL);

    private TeamCompetitionStatsUtils() {

    }

    /**
     * Get the overall TC results.
     *
     * @return the TC {@link AllTeamsSummary}
     * @throws FoldingRestException thrown if an error occurs sending the HTTP request
     */
    public static AllTeamsSummary getStats() throws FoldingRestException {
        final HttpResponse<String> response = TEAM_COMPETITION_REQUEST_SENDER.getStats();
        if (response.statusCode() == HttpURLConnection.HTTP_OK) {
            return TeamCompetitionStatsResponseParser.getStats(response);
        }

        throw new FoldingRestException(
            String.format("Invalid response (%s) when getting all TC result with: %s", response.statusCode(), response.body()));
    }

    /**
     * Get the TC results for a single {@link me.zodac.folding.api.tc.User}.
     *
     * @param userId the ID of the {@link me.zodac.folding.api.tc.User} whose {@link UserSummary} is to be retrieved
     * @return the TC {@link UserSummary}
     * @throws FoldingRestException thrown if an error occurs sending the HTTP request
     */
    public static UserSummary getStatsForUser(final int userId) throws FoldingRestException {
        final HttpResponse<String> response = TEAM_COMPETITION_REQUEST_SENDER.getStatsForUser(userId);
        if (response.statusCode() == HttpURLConnection.HTTP_OK) {
            return TeamCompetitionStatsResponseParser.getStatsForUser(response);
        }

        throw new FoldingRestException(String
            .format("Invalid response (%s) when getting TC result for user with ID %s with: %s", response.statusCode(), userId, response.body()));
    }

    /**
     * Retrieves the {@link TeamSummary} with the given {@code teamName} from the {@link AllTeamsSummary}.
     *
     * @param allTeamsSummary the {@link AllTeamsSummary} to check
     * @param teamName        the name of the {@link TeamSummary} to find
     * @return the {@link TeamSummary}
     * @throws FoldingRestException thrown if an error occurs sending the HTTP request
     */
    public static TeamSummary getTeamFromCompetition(final AllTeamsSummary allTeamsSummary, final String teamName) throws FoldingRestException {
        for (final TeamSummary teamSummary : allTeamsSummary.getTeams()) {
            if (teamSummary.getTeam().teamName().equalsIgnoreCase(teamName)) {
                return teamSummary;
            }
        }
        throw new FoldingRestException(String.format("Unable to find team '%s' in competition teams: %s", teamName, allTeamsSummary.getTeams()));
    }

    /**
     * Retrieves the active {@link UserSummary} with the given {@code userName} from the {@link TeamSummary}.
     *
     * @param teamSummary the {@link TeamSummary} to check
     * @param userName    the name of the active {@link UserSummary} to find
     * @return the {@link UserSummary}
     * @throws FoldingRestException thrown if an error occurs sending the HTTP request
     */
    public static UserSummary getActiveUserFromTeam(final TeamSummary teamSummary, final String userName) throws FoldingRestException {
        for (final UserSummary userSummary : teamSummary.getActiveUsers()) {
            if (userSummary.getUser().displayName().equalsIgnoreCase(userName)) {
                return userSummary;
            }
        }
        throw new FoldingRestException(String.format("Unable to find user '%s' in active users: %s", userName, teamSummary.getActiveUsers()));
    }

    /**
     * Retrieves the retired {@link UserSummary} with the given {@code userName} from the {@link TeamSummary}.
     *
     * @param teamSummary the {@link TeamSummary} to check
     * @param userName    the name of the retired {@link UserSummary} to find
     * @return the {@link UserSummary}
     * @throws FoldingRestException thrown if an error occurs sending the HTTP request
     */
    public static RetiredUserSummary getRetiredUserFromTeam(final TeamSummary teamSummary, final String userName) throws FoldingRestException {
        for (final RetiredUserSummary userResult : teamSummary.getRetiredUsers()) {
            if (userResult.getDisplayName().equalsIgnoreCase(userName)) {
                return userResult;
            }
        }
        throw new FoldingRestException(String.format("Unable to find user '%s' in retired users: %s", userName, teamSummary.getRetiredUsers()));
    }

    /**
     * Executes a manual reset of the {@code Team Competition} stats.
     *
     * @throws FoldingRestException thrown if an error occurs sending the HTTP request
     */
    public static void manuallyResetStats() throws FoldingRestException {
        final HttpResponse<Void> response = TEAM_COMPETITION_REQUEST_SENDER.manualReset(ADMIN_USER.userName(), ADMIN_USER.password());
        assertThat(response.statusCode())
            .as("Expected a 200_OK")
            .isEqualTo(HttpURLConnection.HTTP_OK);
    }

    /**
     * Executes a manual update of the {@code Team Competition} stats.
     *
     * @throws FoldingRestException thrown if an error occurs sending the HTTP request
     */
    public static void manuallyUpdateStats() throws FoldingRestException {
        final HttpResponse<Void> response = TEAM_COMPETITION_REQUEST_SENDER.manualUpdate(ADMIN_USER.userName(), ADMIN_USER.password());
        assertThat(response.statusCode())
            .as("Expected a 200_OK")
            .isEqualTo(HttpURLConnection.HTTP_OK);
    }

    /**
     * Offsets a {@link User}'s points with the provided value.
     *
     * @param user   the {@link User} whose points are to be offset
     * @param points the points to offset by
     * @throws FoldingRestException thrown if an error occurs sending the HTTP request
     */
    public static void offsetUserPoints(final User user, final long points) throws FoldingRestException {
        final HttpResponse<Void> response =
            TEAM_COMPETITION_REQUEST_SENDER.offset(user.id(), points, Math.round(points * user.hardware().multiplier()), 0,
                ADMIN_USER.userName(), ADMIN_USER.password());
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);
    }
}

/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2025 zodac.net
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

package net.zodac.folding.test.integration.util.rest.request;

import static net.zodac.folding.test.integration.util.DummyAuthenticationData.ADMIN_USER;
import static org.assertj.core.api.Assertions.assertThat;

import java.net.HttpURLConnection;
import java.net.http.HttpResponse;
import net.zodac.folding.api.tc.User;
import net.zodac.folding.client.java.request.TeamCompetitionStatsRequestSender;
import net.zodac.folding.client.java.response.TeamCompetitionStatsResponseParser;
import net.zodac.folding.rest.api.exception.FoldingRestException;
import net.zodac.folding.rest.api.tc.AllTeamsSummary;
import net.zodac.folding.rest.api.tc.RetiredUserSummary;
import net.zodac.folding.rest.api.tc.TeamSummary;
import net.zodac.folding.rest.api.tc.UserSummary;
import net.zodac.folding.test.integration.util.TestConstants;

/**
 * Utility class for TC stats-based tests.
 */
public final class TeamCompetitionStatsUtils {

    public static final TeamCompetitionStatsRequestSender TEAM_COMPETITION_REQUEST_SENDER =
        TeamCompetitionStatsRequestSender.createWithUrl(TestConstants.FOLDING_URL);

    private TeamCompetitionStatsUtils() {

    }

    /**
     * Get all stats from the TC results.
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
     * Get the TC results for a single {@link User}.
     *
     * @param userId the ID of the {@link User} whose {@link UserSummary} is to be retrieved
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
        for (final TeamSummary teamSummary : allTeamsSummary.teams()) {
            if (teamSummary.team().teamName().equalsIgnoreCase(teamName)) {
                return teamSummary;
            }
        }
        throw new FoldingRestException(String.format("Unable to find team '%s' in competition teams: %s", teamName, allTeamsSummary.teams()));
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
        for (final UserSummary userSummary : teamSummary.activeUsers()) {
            if (userSummary.user().displayName().equalsIgnoreCase(userName)) {
                return userSummary;
            }
        }
        throw new FoldingRestException(String.format("Unable to find user '%s' in active users: %s", userName, teamSummary.activeUsers()));
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
        for (final RetiredUserSummary userResult : teamSummary.retiredUsers()) {
            if (userResult.displayName().equalsIgnoreCase(userName)) {
                return userResult;
            }
        }
        throw new FoldingRestException(String.format("Unable to find user '%s' in retired users: %s", userName, teamSummary.retiredUsers()));
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
            .as("Did not receive a 200_OK HTTP response: %s", response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);
    }
}

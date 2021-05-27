package me.zodac.folding.test.utils.rest.request;

import me.zodac.folding.client.java.request.TeamCompetitionRequestSender;
import me.zodac.folding.client.java.response.TeamCompetitionResponseParser;
import me.zodac.folding.rest.api.exception.FoldingRestException;
import me.zodac.folding.rest.api.tc.CompetitionResult;
import me.zodac.folding.rest.api.tc.TeamResult;
import me.zodac.folding.rest.api.tc.UserResult;

import java.net.HttpURLConnection;
import java.net.http.HttpResponse;

import static me.zodac.folding.test.utils.TestAuthenticationData.ADMIN_USER;
import static me.zodac.folding.test.utils.TestConstants.FOLDING_URL;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Utility class for TC stats-based tests.
 */
public final class TeamCompetitionStatsUtils {

    public static final TeamCompetitionRequestSender TEAM_COMPETITION_REQUEST_SENDER = TeamCompetitionRequestSender.create(FOLDING_URL);

    private TeamCompetitionStatsUtils() {

    }

    /**
     * Get the overall TC results.
     *
     * @return the TC {@link CompetitionResult}
     * @throws FoldingRestException thrown if an error occurs sending the HTTP request
     */
    public static CompetitionResult getStats() throws FoldingRestException {
        final HttpResponse<String> response = TEAM_COMPETITION_REQUEST_SENDER.getStats();
        if (response.statusCode() == HttpURLConnection.HTTP_OK) {
            return TeamCompetitionResponseParser.getStats(response);
        }

        throw new FoldingRestException(String.format("Invalid response (%s) when getting all TC result with: %s", response.statusCode(), response.body()));
    }

    /**
     * Get the TC results for a single {@link me.zodac.folding.api.tc.User}.
     *
     * @param userId the ID of the {@link me.zodac.folding.api.tc.User} whose {@link UserResult} is to be retrieved
     * @return the TC {@link UserResult}
     * @throws FoldingRestException thrown if an error occurs sending the HTTP request
     */
    public static UserResult getStatsForUser(final int userId) throws FoldingRestException {
        final HttpResponse<String> response = TEAM_COMPETITION_REQUEST_SENDER.getStatsForUser(userId);
        if (response.statusCode() == HttpURLConnection.HTTP_OK) {
            return TeamCompetitionResponseParser.getStatsForUser(response);
        }

        throw new FoldingRestException(String.format("Invalid response (%s) when getting TC result for user with ID %s with: %s", userId, response.statusCode(), response.body()));
    }

    /**
     * Retrieves the {@link TeamResult} with the given {@code teamName} from the {@link CompetitionResult}.
     *
     * @param competitionResult the {@link CompetitionResult} to check
     * @param teamName          the name of the {@link TeamResult} to find
     * @return the {@link TeamResult}
     * @throws FoldingRestException thrown if an error occurs sending the HTTP request
     */
    public static TeamResult getTeamFromCompetition(final CompetitionResult competitionResult, final String teamName) throws FoldingRestException {
        for (final TeamResult teamResult : competitionResult.getTeams()) {
            if (teamResult.getTeamName().equalsIgnoreCase(teamName)) {
                return teamResult;
            }
        }
        throw new FoldingRestException(String.format("Unable to find team '%s' in competition teams: %s", teamName, competitionResult.getTeams()));
    }

    /**
     * Retrieves the active {@link UserResult} with the given {@code userName} from the {@link TeamResult}.
     *
     * @param teamResult the {@link TeamResult} to check
     * @param userName   the name of the active {@link UserResult} to find
     * @return the {@link UserResult}
     * @throws FoldingRestException thrown if an error occurs sending the HTTP request
     */
    public static UserResult getActiveUserFromTeam(final TeamResult teamResult, final String userName) throws FoldingRestException {
        for (final UserResult userResult : teamResult.getActiveUsers()) {
            if (userResult.getDisplayName().equalsIgnoreCase(userName)) {
                return userResult;
            }
        }
        throw new FoldingRestException(String.format("Unable to find user '%s' in active users: %s", userName, teamResult.getActiveUsers()));
    }

    /**
     * Retrieves the retired {@link UserResult} with the given {@code userName} from the {@link TeamResult}.
     *
     * @param teamResult the {@link TeamResult} to check
     * @param userName   the name of the retired {@link UserResult} to find
     * @return the {@link UserResult}
     * @throws FoldingRestException thrown if an error occurs sending the HTTP request
     */
    public static UserResult getRetiredUserFromTeam(final TeamResult teamResult, final String userName) throws FoldingRestException {
        for (final UserResult userResult : teamResult.getRetiredUsers()) {
            if (userResult.getDisplayName().equalsIgnoreCase(userName)) {
                return userResult;
            }
        }
        throw new FoldingRestException(String.format("Unable to find user '%s' in retired users: %s", userName, teamResult.getRetiredUsers()));
    }

    /**
     * Executes a manual reset of the <code>Team Competition</code> stats.
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
     * Executes a manual update of the <code>Team Competition</code> stats.
     *
     * @throws FoldingRestException thrown if an error occurs sending the HTTP request
     */
    public static void manuallyUpdateStats() throws FoldingRestException {
        final HttpResponse<Void> response = TEAM_COMPETITION_REQUEST_SENDER.manualUpdate(ADMIN_USER.userName(), ADMIN_USER.password());
        assertThat(response.statusCode())
                .as("Expected a 200_OK")
                .isEqualTo(HttpURLConnection.HTTP_OK);
    }
}

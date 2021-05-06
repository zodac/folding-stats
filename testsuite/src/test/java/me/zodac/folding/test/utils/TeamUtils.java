package me.zodac.folding.test.utils;

import me.zodac.folding.api.tc.Team;
import me.zodac.folding.client.java.request.TeamRequestSender;
import me.zodac.folding.client.java.response.TeamResponseParser;
import me.zodac.folding.rest.api.exception.FoldingRestException;

import java.net.HttpURLConnection;
import java.net.http.HttpResponse;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Utility class for {@link Team}-based tests.
 */
public class TeamUtils {

    public static final TeamRequestSender TEAM_REQUEST_SENDER = TeamRequestSender.create("http://192.168.99.100:8081/folding");

    private TeamUtils() {

    }

    /**
     * Creates the given {@link Team}, or if it already exists, returns the existing one.
     *
     * @param team the {@link Team} to create/retrieve
     * @return the created {@link Team} or existing {@link Team}
     * @throws FoldingRestException thrown if an error occurs creating/retrieving the {@link Team}
     */
    public static Team createOrConflict(final Team team) throws FoldingRestException {
        final HttpResponse<String> response = TEAM_REQUEST_SENDER.create(team);
        if (response.statusCode() == HttpURLConnection.HTTP_CREATED) {
            return TeamResponseParser.create(response);
        }

        if (response.statusCode() == HttpURLConnection.HTTP_CONFLICT) {
            return get(team.getId());
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

        throw new FoldingRestException(String.format("Invalid response (%s) when getting all teams with: %s", response.statusCode(), response.body()));
    }

    /**
     * Retrieves the number of {@link Team}s.
     *
     * @return the number of {@link Team}s
     * @throws FoldingRestException thrown if an error occurs retrieving the {@link Team} count
     */
    public static int getNumberOfTeams() throws FoldingRestException {
        final HttpResponse<String> response = TEAM_REQUEST_SENDER.getAll();
        final Map<String, List<String>> headers = response.headers().map();
        if (headers.containsKey("X-Total-Count")) {
            final String firstHeaderValue = headers.get("X-Total-Count").get(0);

            try {
                return Integer.parseInt(firstHeaderValue);
            } catch (final NumberFormatException e) {
                throw new FoldingRestException(String.format("Error parsing 'X-Total-Count' header %s", firstHeaderValue), e);
            }
        }
        throw new FoldingRestException(String.format("Unable to find 'X-Total-Count' header: %s", headers));
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

        throw new FoldingRestException(String.format("Invalid response (%s) when getting team with ID %s: %s", response.statusCode(), teamId, response.body()));
    }

    /**
     * Retires a {@link me.zodac.folding.api.tc.User} from a {@link Team}.
     *
     * @param teamId the ID of the {@link Team}
     * @param userId the ID of the {@link me.zodac.folding.api.tc.User} to retire
     * @return the retired {@link me.zodac.folding.api.tc.User} ID
     * @throws FoldingRestException thrown if an error occurs retiring the {@link me.zodac.folding.api.tc.User}
     */
    public static int retireUser(final int teamId, final int userId) throws FoldingRestException {
        final HttpResponse<String> response = TEAM_REQUEST_SENDER.retireUser(teamId, userId);
        if (response.statusCode() == HttpURLConnection.HTTP_OK) {
            return TeamResponseParser.retireUser(response).getRetiredUserIds().iterator().next();
        }

        throw new FoldingRestException(String.format("Invalid response (%s) when retiring user with ID %s from team with ID %s: %s", response.statusCode(), userId, teamId, response.body()));
    }

    /**
     * Un-retires a {@link me.zodac.folding.api.tc.User}, and adds them to a {@link Team}.
     *
     * @param teamId        the ID of the {@link Team}
     * @param retiredUserId the ID of the retired{@link me.zodac.folding.api.tc.User} to un-retire
     * @return the updated {@link Team}
     * @throws FoldingRestException thrown if an error occurs un-retiring the {@link me.zodac.folding.api.tc.User}
     */
    public static Team unretireUser(final int teamId, final int retiredUserId) throws FoldingRestException {
        final HttpResponse<String> response = TEAM_REQUEST_SENDER.unretireUser(teamId, retiredUserId);
        if (response.statusCode() == HttpURLConnection.HTTP_OK) {
            return TeamResponseParser.unretireUser(response);
        }
        throw new FoldingRestException(String.format("Invalid response (%s) when un-retiring user with ID %s from team with ID %s: %s", response.statusCode(), retiredUserId, teamId, response.body()));
    }
}

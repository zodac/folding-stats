package me.zodac.folding.stats.http.request;

import me.zodac.folding.api.utils.EnvironmentVariableUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Utility class used to build the URL to request Folding points for a user from the Stanford Folding@Home API.
 */
public class PointsUrlBuilder {

    // We do not care about the team number, as we want the points for user/passkey on all teams
    // However, the API call requires a team number to be specified, so we'll stick to OCN. :)
    private static final int TEAM_NUMBER = 37_726;
    private static final String STATS_URL_ROOT = EnvironmentVariableUtils.get("STATS_URL_ROOT", "https://api2.foldingathome.org");
    private static final String POINTS_URL_ROOT_FORMAT = STATS_URL_ROOT + "/user/%s/stats";

    private String user;
    private String passkey;

    /**
     * Update the {@link PointsUrlBuilder} with the 'user' for the URL.
     *
     * @param user the user for the request
     * @return the updated {@link PointsUrlBuilder}
     */
    public PointsUrlBuilder forUser(final String user) {
        this.user = user;
        return this;
    }

    /**
     * Update the {@link PointsUrlBuilder} with the 'passkey' for the URL.
     *
     * @param passkey the passkey for the request
     * @return the updated {@link PointsUrlBuilder}
     */
    public PointsUrlBuilder withPasskey(final String passkey) {
        this.passkey = passkey;
        return this;
    }

    /**
     * Build the points REST request URL for a user/passkey.
     *
     * @return the URL to request units
     */
    public StatsRequestUrl build() {
        if (StringUtils.isBlank(user)) {
            throw new IllegalArgumentException("'user' cannot be null or empty");
        }

        final StringBuilder pointsUrl = new StringBuilder(String.format(POINTS_URL_ROOT_FORMAT, user));

        // The 'team' query must appear before the 'passkey' query, or the response will not have a valid response
        pointsUrl.append("?team=").append(TEAM_NUMBER);

        if (StringUtils.isNotBlank(passkey)) {
            pointsUrl.append("&passkey=").append(passkey);
        }

        return StatsRequestUrl.create(pointsUrl);
    }
}

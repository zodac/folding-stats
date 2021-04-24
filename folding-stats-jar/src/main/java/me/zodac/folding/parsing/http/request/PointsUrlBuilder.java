package me.zodac.folding.parsing.http.request;

import org.apache.commons.lang3.StringUtils;

/**
 * Utility class used to build the URL to request Folding points for a user from the Stanford Folding@Home API.
 */
public class PointsUrlBuilder {
    
    // We do not care about the team number, as we want the points for user+passkey on all teams
    // However, the API call requires a team number to be specified, so we'll stick to OCN. :)
    private static final int TEAM_NUMBER = 37726;
    private static final String POINTS_URL_ROOT_FORMAT = "https://api2.foldingathome.org/user/%s/stats";

    private String user;
    private String passkey;

    public PointsUrlBuilder() {

    }

    public PointsUrlBuilder forUser(final String user) {
        this.user = user;
        return this;
    }

    public PointsUrlBuilder withPasskey(final String passkey) {
        this.passkey = passkey;
        return this;
    }

    public String build() {
        if (StringUtils.isBlank(user)) {
            throw new IllegalArgumentException("'user' cannot be null or empty");
        }

        final StringBuilder pointsUrl = new StringBuilder(String.format(POINTS_URL_ROOT_FORMAT, user));

        // The 'team' query must appear before the 'passkey' query, or the response will not have a valid response
        pointsUrl.append("?team=").append(TEAM_NUMBER);

        if (StringUtils.isNotBlank(passkey)) {
            pointsUrl.append("&passkey=").append(passkey);
        }

        return pointsUrl.toString();
    }
}

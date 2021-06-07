package me.zodac.folding.stats.http.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Simple wrapper for a {@link String} to hold the URL for a points or units REST request.
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class StatsRequestUrl {

    private final String url;

    /**
     * Creates a {@link StatsRequestUrl} based on the provided URL.
     *
     * @param statsRequestUrl the URL
     * @return the created {@link StatsRequestUrl}
     */
    public static StatsRequestUrl create(final StringBuilder statsRequestUrl) {
        return new StatsRequestUrl(statsRequestUrl.toString());
    }
}

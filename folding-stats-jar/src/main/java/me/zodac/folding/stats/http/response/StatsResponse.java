package me.zodac.folding.stats.http.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.net.http.HttpResponse;

/**
 * Simple wrapper for a {@link String} to hold the status code and body for the {@link HttpResponse} for a
 * points or units REST request.
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class StatsResponse {

    private final int statusCode;
    private final String responseBody;

    /**
     * Creates a {@link StatsResponse} based on the provided {@link HttpResponse}.
     *
     * @param httpResponse the {@link HttpResponse}
     * @return the created {@link StatsResponse}
     */
    public static StatsResponse create(final HttpResponse<String> httpResponse) {
        return new StatsResponse(httpResponse.statusCode(), httpResponse.body());
    }
}

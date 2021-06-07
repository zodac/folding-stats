package me.zodac.folding.stats.http.response;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import me.zodac.folding.api.stats.FoldingStatsDetails;
import me.zodac.folding.stats.http.request.StatsRequestSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.net.http.HttpResponse;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * Utility class parsing a {@link PointsApiInstance} or {@link UnitsApiInstance} from the Stanford REST request.
 */
public final class StatsResponseParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(StatsResponseParser.class);
    private static final Gson GSON = new Gson();
    private static final int EXPECTED_NUMBER_OF_UNIT_RESPONSES = 1;

    private StatsResponseParser() {

    }

    /**
     * Extracts the points from a {@link HttpResponse} that was received by{@link me.zodac.folding.stats.http.request.PointsUrlBuilder} and {@link StatsRequestSender}.
     * Converts the {@link HttpResponse} to a {@link PointsApiInstance} then extracts the earned points.
     *
     * @param response the {@link HttpResponse} to parse
     * @return the points for a user/passkey
     */
    public static long getPointsFromResponse(final StatsResponse response) {
        final PointsApiInstance pointsApiInstance = parsePointsResponse(response);
        return pointsApiInstance.getEarned();
    }

    private static PointsApiInstance parsePointsResponse(final StatsResponse response) {
        try {
            return GSON.fromJson(response.getResponseBody(), PointsApiInstance.class);
        } catch (final JsonSyntaxException e) {
            LOGGER.warn("Error parsing the points JSON response from the API: '{}'", response.getResponseBody(), e);
            throw e;
        } catch (final Exception e) {
            LOGGER.warn("Unexpected error parsing points JSON response from the API with status code {}: '{}'", response.getStatusCode(), response.getResponseBody(), e);
            throw e;
        }
    }

    /**
     * Extracts the Work Units from a {@link HttpResponse} that was received by {@link me.zodac.folding.stats.http.request.UnitsUrlBuilder} and {@link StatsRequestSender}.
     * Converts the {@link HttpResponse} to a {@link UnitsApiInstance} then extracts the finished units.
     *
     * @param foldingStatsDetails the Folding@Home username/passkey, used for logging in case of an issue
     * @param response            the {@link HttpResponse} to parse
     * @return the Work Units for a user/passkey
     */
    public static int getUnitsFromResponse(final FoldingStatsDetails foldingStatsDetails, final StatsResponse response) {
        final List<UnitsApiInstance> unitsApiInstances = parseUnitsResponse(response);

        if (unitsApiInstances.isEmpty()) {
            LOGGER.warn("No valid units found for user/passkey: '{}/{}'", foldingStatsDetails.getFoldingUserName(), foldingStatsDetails.getPasskey());
            return 0;
        }

        // If the username/passkey has been used on multiple teams, we will get multiple responses
        // Unfortunately, there is no way to filter on team currently, but to be fair to the other users we will take
        // the result with the lowest number of finished units.
        final UnitsApiInstance firstEntry = unitsApiInstances
                .stream()
                .sorted(Collections.reverseOrder())
                .collect(toList())
                .get(0);

        if (unitsApiInstances.size() > EXPECTED_NUMBER_OF_UNIT_RESPONSES) {
            LOGGER.warn("Too many unit responses returned for user, using {} from response: {}", firstEntry, response.getResponseBody());
        }

        return firstEntry.getFinished();
    }

    private static List<UnitsApiInstance> parseUnitsResponse(final StatsResponse response) {
        try {
            final Type collectionType = new TypeToken<Collection<UnitsApiInstance>>() {
            }.getType();
            return GSON.fromJson(response.getResponseBody(), collectionType);
        } catch (final JsonSyntaxException e) {
            LOGGER.warn("Error parsing the units JSON response from the API: '{}'", response.getResponseBody(), e);
            throw e;
        } catch (final Exception e) {
            LOGGER.warn("Unexpected error parsing units JSON response from the API with status code {}: '{}'", response.getStatusCode(), response.getResponseBody(), e);
            throw e;
        }
    }
}

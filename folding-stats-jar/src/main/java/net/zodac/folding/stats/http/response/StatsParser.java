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

package net.zodac.folding.stats.http.response;

import static net.zodac.folding.rest.api.util.RestUtilConstants.GSON;

import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.net.http.HttpResponse;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import net.zodac.folding.api.stats.FoldingStatsDetails;
import net.zodac.folding.stats.http.request.PointsUrlBuilder;
import net.zodac.folding.stats.http.request.StatsSender;
import net.zodac.folding.stats.http.request.UnitsUrlBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Utility class parsing a {@link PointsApiInstance} or {@link UnitsApiInstance} from the Stanford REST request.
 */
public final class StatsParser {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final int EXPECTED_NUMBER_OF_UNIT_RESPONSES = 1;

    private StatsParser() {

    }

    /**
     * Extracts the points from a {@link HttpResponse} that was received by{@link PointsUrlBuilder} and
     * {@link StatsSender}. Converts the {@link HttpResponse} to a {@link PointsApiInstance} then extracts the earned points.
     *
     * @param httpResponse the {@link HttpResponse} to parse
     * @return the points for a user/passkey
     */
    public static long getPointsFromResponse(final HttpResponse<String> httpResponse) {
        final PointsApiInstance pointsApiInstance = parsePointsResponse(httpResponse);
        return pointsApiInstance.earned();
    }

    private static PointsApiInstance parsePointsResponse(final HttpResponse<String> httpResponse) {
        try {
            return GSON.fromJson(httpResponse.body(), PointsApiInstance.class);
        } catch (final JsonSyntaxException e) {
            LOGGER.warn("Error parsing the points JSON response from the API: '{}'", httpResponse.body(), e);
            throw e;
        } catch (final Exception e) {
            LOGGER.warn("Unexpected error parsing points JSON response from the API with status code {}: '{}'", httpResponse.statusCode(),
                httpResponse.body(), e);
            throw e;
        }
    }

    /**
     * Extracts the Work Units from a {@link HttpResponse} that was received by {@link UnitsUrlBuilder} and
     * {@link StatsSender}. Converts the {@link HttpResponse} to a {@link UnitsApiInstance} then extracts the finished units.
     *
     * @param foldingStatsDetails the Folding@Home username/passkey, used for logging in case of an issue
     * @param httpResponse        the {@link HttpResponse} to parse
     * @return the Work Units for a user/passkey
     */
    public static int getUnitsFromResponse(final FoldingStatsDetails foldingStatsDetails, final HttpResponse<String> httpResponse) {
        final List<UnitsApiInstance> unitsApiInstances = parseUnitsResponse(httpResponse);

        if (unitsApiInstances.isEmpty()) {
            LOGGER.warn("No valid units found for user/passkey: '{}/{}'", foldingStatsDetails.foldingUserName(), foldingStatsDetails.passkey());
            return 0;
        }

        // If the username/passkey has been used on multiple teams, we will get multiple responses
        // Unfortunately, there is no way to filter on team currently, but to be fair to the other users we will take
        // the result with the lowest number of finished units.
        final UnitsApiInstance firstEntry = unitsApiInstances
            .stream()
            .sorted(Collections.reverseOrder())
            .toList()
            .getFirst();

        if (unitsApiInstances.size() > EXPECTED_NUMBER_OF_UNIT_RESPONSES) {
            LOGGER.warn("Too many unit responses returned for user, using {} from response: {}", firstEntry, httpResponse.body());
        }

        return firstEntry.finished();
    }

    private static List<UnitsApiInstance> parseUnitsResponse(final HttpResponse<String> httpResponse) {
        try {
            final Type collectionType = UnitsApiInstanceCollectionType.getInstance().getType();
            return GSON.fromJson(httpResponse.body(), collectionType);
        } catch (final JsonSyntaxException e) {
            LOGGER.warn("Error parsing the units JSON response from the API: '{}'", httpResponse.body(), e);
            throw e;
        } catch (final Exception e) {
            LOGGER.warn("Unexpected error parsing units JSON response from the API with status code {}: '{}'", httpResponse.statusCode(),
                httpResponse.body(), e);
            throw e;
        }
    }

    /**
     * Private class defining the {@link Collection} for {@link UnitsApiInstance}s.
     */
    private static final class UnitsApiInstanceCollectionType extends TypeToken<Collection<UnitsApiInstance>> {

        private static final UnitsApiInstanceCollectionType INSTANCE = new UnitsApiInstanceCollectionType();

        /**
         * Retrieve a singleton instance of {@link UnitsApiInstanceCollectionType}.
         *
         * @return {@link UnitsApiInstanceCollectionType} instance.
         */
        static UnitsApiInstanceCollectionType getInstance() {
            return INSTANCE;
        }
    }
}

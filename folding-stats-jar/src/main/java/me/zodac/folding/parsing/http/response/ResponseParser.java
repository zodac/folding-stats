package me.zodac.folding.parsing.http.response;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import me.zodac.folding.api.exception.FoldingExternalServiceException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.net.http.HttpResponse;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class ResponseParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResponseParser.class);
    private static final Gson GSON = new Gson();

    private ResponseParser() {

    }

    public static long getPointsFromResponse(final HttpResponse<String> response) throws FoldingExternalServiceException {
        if (StringUtils.isBlank(response.body())) {
            throw new FoldingExternalServiceException("Empty Folding points response");
        }

        try {
            final PointsApiResponse pointsApiResponse = GSON.fromJson(response.body(), PointsApiResponse.class);
            return pointsApiResponse.getEarned();
        } catch (final JsonSyntaxException e) {
            LOGGER.warn("Error parsing the JSON response from the API: '{}'", response.body(), e);
            throw e;
        } catch (final Exception e) {
            LOGGER.warn("Unexpected error parsing JSON response from the API with status code {}: '{}'", response.statusCode(), response.body(), e);
            throw e;
        }
    }

    public static int getUnitsFromResponse(final String userName, final String passkey, final HttpResponse<String> response) throws FoldingExternalServiceException {
        if (StringUtils.isBlank(response.body())) {
            throw new FoldingExternalServiceException("Empty Folding units response");
        }

        final Type collectionType = new TypeToken<Collection<UnitsApiInstance>>() {
        }.getType();
        final List<UnitsApiInstance> unitsResponse = GSON.fromJson(response.body(), collectionType);

        if (unitsResponse.isEmpty()) {
            LOGGER.warn("No valid units found for user/passkey: '{}/{}'", userName, passkey);
            return 0;
        }

        // If the username+passkey has been used on multiple teams, we will get multiple responses
        // Unfortunately, there is no way to filter on team currently, but to be fair to the other users we will take
        // the result with the lowest number of finished units.
        final UnitsApiInstance firstEntry = unitsResponse
                .stream()
                .sorted(Collections.reverseOrder())
                .collect(toList())
                .get(0);

        if (unitsResponse.size() > 1) {
            LOGGER.warn("Too many unit responses returned for user, using {} from response: {}", firstEntry, response.body());
        }

        return firstEntry.getFinished();
    }
}

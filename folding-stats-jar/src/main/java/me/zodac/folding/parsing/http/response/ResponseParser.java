package me.zodac.folding.parsing.http.response;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.net.http.HttpResponse;
import java.util.Collection;
import java.util.List;

public class ResponseParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResponseParser.class);
    private static final Gson GSON = new Gson();

    private ResponseParser() {

    }

    public static long getPointsFromResponse(final HttpResponse<String> response) {
        try {
            final PointsApiResponse pointsApiResponse = GSON.fromJson(response.body(), PointsApiResponse.class);
            return pointsApiResponse.getContributed();
        } catch (final JsonSyntaxException e) {
            LOGGER.warn("Error parsing the JSON response from the API: '{}'", response.body(), e);
            throw e;
        } catch (final Exception e) {
            LOGGER.warn("Unexpected error parsing JSON response from the API with status code {}: '{}'", response.statusCode(), response.body(), e);
            throw e;
        }
    }

    public static int getUnitsFromResponse(final HttpResponse<String> response) {
        final Type collectionType = new TypeToken<Collection<UnitsApiInstance>>() {
        }.getType();
        final List<UnitsApiInstance> unitsResponse = GSON.fromJson(response.body(), collectionType);

        if (unitsResponse.isEmpty()) {
            LOGGER.warn("No valid units found for user: {}", response.body());
            return 0;
        }

        if (unitsResponse.size() > 1) {
            LOGGER.warn("Too many unit responses returned for user: {}", response.body());
            return 0;
        }

        return unitsResponse.get(0).getFinished();
    }
}

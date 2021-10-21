package me.zodac.folding.rest.endpoint.util;

import static me.zodac.folding.rest.response.Responses.badRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Utility class used to parse {@link Integer}s for the REST endpoints.
 */
public final class IntegerParser {

    private static final Logger LOGGER = LogManager.getLogger();

    private IntegerParser() {

    }

    /**
     * Attempts to parse the input {@link String} into a valid non-negative {@link Integer} ID.
     *
     * <p>
     * Logs any error and returns either the valid {@link Integer} ID, or an error {@link javax.ws.rs.core.Response}.
     *
     * @param integer the {@link String} to parse into an {@link Integer}
     * @return the {@link IdResult}
     */
    public static IdResult parsePositive(final String integer) {
        try {
            final int parsedId = Integer.parseInt(integer);
            if (parsedId < 0) {
                final String errorMessage = String.format("The ID '%s' is out of range", parsedId);
                LOGGER.error(errorMessage);
                return IdResult.failure(badRequest(errorMessage));
            }

            return IdResult.success(parsedId);
        } catch (final NumberFormatException e) {
            final String errorMessage = String.format("The ID '%s' is not a valid format", integer);
            LOGGER.error(errorMessage);
            return IdResult.failure(badRequest(errorMessage));
        }
    }
}

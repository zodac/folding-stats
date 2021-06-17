package me.zodac.folding.rest.parse;

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
     * Attempts to parse the input {@link String} into a valid non-negative {@link Integer}.
     *
     * @param integer the {@link String} to parse into an {@link Integer}
     * @return the {@link ParseResult}
     */
    public static ParseResult parsePositive(final String integer) {
        try {
            final int parsedId = Integer.parseInt(integer);
            if (parsedId < 0) {
                LOGGER.debug("ID '{}' is less than 0", integer);
                return ParseResult.outOfRange();
            }
            return ParseResult.valid(parsedId);
        } catch (final NumberFormatException e) {
            LOGGER.debug("Unable to parse ID '{}'", integer, e);
            return ParseResult.badFormat();
        }
    }
}

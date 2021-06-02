package me.zodac.folding.rest.util;

import me.zodac.folding.api.tc.exception.FoldingIdInvalidException;
import me.zodac.folding.api.tc.exception.FoldingIdOutOfRangeException;

/**
 * Utility class used to parse values for the REST endpoints.
 */
public final class IdentityParser {

    private IdentityParser() {

    }

    /**
     * Attempts to parse the input {@link String} into a valid ID
     *
     * @param id the {@link String} to parse
     * @return the ID
     * @throws FoldingIdInvalidException    thrown if the input is not a valid {@link Integer}
     * @throws FoldingIdOutOfRangeException thrown if the input is less than <b>0</b>
     */
    public static int parse(final String id) throws FoldingIdInvalidException, FoldingIdOutOfRangeException {
        try {
            final int parsedId = Integer.parseInt(id);
            if (parsedId < 0) {
                throw new FoldingIdOutOfRangeException(parsedId);
            }
            return parsedId;
        } catch (final NumberFormatException e) {
            throw new FoldingIdInvalidException(id, e);
        }
    }
}

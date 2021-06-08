package me.zodac.folding.rest.util;

import me.zodac.folding.api.exception.IdOutOfRangeException;
import me.zodac.folding.api.exception.InvalidIdException;

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
     * @throws InvalidIdException    thrown if the input is not a valid {@link Integer}
     * @throws IdOutOfRangeException thrown if the input is less than <b>0</b>
     */
    public static int parse(final String id) throws InvalidIdException, IdOutOfRangeException {
        try {
            final int parsedId = Integer.parseInt(id);
            if (parsedId < 0) {
                throw new IdOutOfRangeException(parsedId);
            }
            return parsedId;
        } catch (final NumberFormatException e) {
            throw new InvalidIdException(id, e);
        }
    }
}

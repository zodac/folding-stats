package me.zodac.folding.rest.parse;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * POJO defining the result of {@link IntegerParser#parsePositive(String)}.
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@EqualsAndHashCode
@ToString(doNotUseGetters = true)
public final class ParseResult {

    private static final int INVALID_ID = -1;

    private final int id;
    private final boolean isValid;
    private final boolean isBadFormat;
    private final boolean isOutOfRange;

    /**
     * Creates an {@link ParseResult} for a successfully parsed {@link Integer}.
     *
     * @param integer the parsed {@link Integer}
     * @return the {@link ParseResult}
     */
    public static ParseResult valid(final int integer) {
        return new ParseResult(integer, true, false, false);
    }

    /**
     * Creates an {@link ParseResult} for an invalid parsing due the input value not being a valid
     * {@link Integer}.
     *
     * @return the {@link ParseResult}
     */
    public static ParseResult badFormat() {
        return new ParseResult(INVALID_ID, false, true, false);
    }

    /**
     * Creates an {@link ParseResult} for an invalid parsing due to the input value being out of range.
     *
     * @return the {@link ParseResult}
     */
    public static ParseResult outOfRange() {
        return new ParseResult(INVALID_ID, false, false, true);
    }
}

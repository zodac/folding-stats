package me.zodac.folding.api.utils;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Utility class used to retrieve environment variables.
 */
public class NumberUtils {

    private NumberUtils() {

    }

    /**
     * Formats the input long with commas, based on {@link Locale#UK}.
     * <p>
     * <b>NOTE:</b> This should only be used for logging. Any values making it to the front-end should be formatted there,
     * otherwise we will end up formatting with the incorrect style (commas vs periods, for example).
     *
     * @param number the number to be formatted
     * @return the formatted number
     */
    public static String formatWithCommas(final long number) {
        return NumberFormat.getInstance(Locale.UK).format(number);
    }
}

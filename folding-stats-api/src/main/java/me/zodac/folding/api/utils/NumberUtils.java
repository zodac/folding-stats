package me.zodac.folding.api.utils;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Utility class used to retrieve environment variables.
 */
public final class NumberUtils {

    private NumberUtils() {

    }

    /**
     * Formats the input long with commas, based on {@link Locale#UK}.
     *
     * <p>
     * <b>NOTE:</b> This should only be used for logging. Any values making it to the frontend should be formatted there,
     * otherwise we may end up formatting with the incorrect style.
     *
     * @param number the number to be formatted
     * @return the formatted number
     */
    public static String formatWithCommas(final long number) {
        return NumberFormat.getInstance(Locale.UK).format(number);
    }
}

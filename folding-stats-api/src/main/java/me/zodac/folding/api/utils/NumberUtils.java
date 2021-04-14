package me.zodac.folding.api.utils;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Utility class used to retrieve environment variables.
 */
public class NumberUtils {

    private NumberUtils() {

    }

    public static String formatWithCommas(final long number) {
        return NumberFormat.getInstance(Locale.UK).format(number);
    }
}

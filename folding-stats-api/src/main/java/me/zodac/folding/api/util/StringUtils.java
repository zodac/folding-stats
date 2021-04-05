package me.zodac.folding.api.util;

/**
 * Utility class for {@link String} functions.
 * <p>
 * Since this is an API module, I don't want to include any dependencies that are not essential.
 * Cleaner to make these functions than include something like Apache Commons, since they are so simple.
 */
public class StringUtils {

    public static boolean isNotBlank(final String string) {
        return !isBlank(string);
    }

    public static boolean isBlank(final String string) {
        return string == null || string.isBlank();
    }

    public static boolean areBlank(final String... strings) {
        for (final String string : strings) {
            if (isNotBlank(string)) {
                return false;
            }
        }
        return true;
    }

    public static boolean areNotBlank(final String... strings) {
        for (final String string : strings) {
            if (isBlank(string)) {
                return false;
            }
        }
        return true;
    }
}

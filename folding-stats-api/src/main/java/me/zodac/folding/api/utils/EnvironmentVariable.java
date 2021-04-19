package me.zodac.folding.api.utils;

/**
 * Utility class used to retrieve environment variables.
 */
public final class EnvironmentVariable {

    private EnvironmentVariable() {

    }

    /**
     * Returns the {@code propertyName} specified if it is set as a {@link System} property or as a {@link System} environment variable. The order of
     * the search is property first then environment variable.
     * <p>
     * If neither is set then null is returned.
     *
     * @param propertyName the property to search for
     * @return the value of the searched property, otherwise {@code null}
     */
    public static String get(final String propertyName) {
        return System.getProperty(propertyName, System.getenv(propertyName));
    }

    /**
     * Returns the {@code propertyName} specified if it is set as a {@link System} property or as a {@link System} environment variable. The order of
     * the search is property first then environment variable.
     * <p>
     * If neither is set then the default value provided is returned.
     *
     * @param propertyName the property name to search for
     * @param defaultValue the default value if no property exists
     * @return The value of the searched name, otherwise the {@code defaultValue}
     */
    public static String get(final String propertyName, final String defaultValue) {
        final String environmentVariable = System.getenv(propertyName);
        if (environmentVariable != null) {
            return environmentVariable;
        }
        return System.getProperty(propertyName, defaultValue);
    }
}

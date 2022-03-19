/*
 * MIT License
 *
 * Copyright (c) 2021-2022 zodac.me
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.zodac.folding.api.util;

/**
 * Utility class used to retrieve environment variables.
 */
public final class EnvironmentVariableUtils {

    private EnvironmentVariableUtils() {

    }

    /**
     * Returns the {@code variableName} specified if it is set as a {@link System} property or as an environment variable. The order of
     * the search is property first then environment variable.
     *
     * <p>
     * If neither is set then null is returned.
     *
     * @param variableName the property to search for
     * @return the value of the searched property, otherwise <b>null</b>
     */
    public static String get(final String variableName) {
        return System.getProperty(variableName, System.getenv(variableName));
    }

    /**
     * Returns the {@code variableName} specified if it is set as a {@link System} property or as an environment variable. The order of
     * the search is check for an environment variable first, then if none is found, check for a system property.
     *
     * <p>
     * If neither is set then the default value provided is returned.
     *
     * @param variableName the property name to search for
     * @param defaultValue the default value if no property exists
     * @return the value of the searched name, otherwise the {@code defaultValue}
     */
    public static String getOrDefault(final String variableName, final String defaultValue) {
        final String environmentVariable = System.getenv(variableName);
        if (environmentVariable != null) {
            return environmentVariable;
        }
        return System.getProperty(variableName, defaultValue);
    }

    /**
     * Returns the {@code variableName} specified if it is set as a {@link System} property or as an environment variable. The order of
     * the search is property first then environment variable.
     *
     * <p>
     * The returned value is then converted to a {@link Boolean}.
     *
     * @param variableName the property to search for
     * @return the value of the property as a {@link Boolean}, defaulting to {@code false} if the value does not exist or is not valid
     */
    public static boolean isEnabled(final String variableName) {
        final String value = System.getProperty(variableName, System.getenv(variableName));
        return Boolean.parseBoolean(value);
    }

    /**
     * Returns the {@code variableName} specified if it is set as a {@link System} property or as an environment variable. The order of
     * the search is property first then environment variable.
     *
     * <p>
     * The returned value is then converted to an {@code int}.
     *
     * @param variableName the property to search for
     * @param defaultValue the default value if no property exists
     * @return the value of the searched name, otherwise the {@code defaultValue}
     */
    public static int getIntOrDefault(final String variableName, final int defaultValue) {
        final String value = System.getProperty(variableName, System.getenv(variableName));
        try {
            return Integer.parseInt(value);
        } catch (final NumberFormatException e) {
            return defaultValue;
        }
    }
}

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

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Utility class used to retrieve environment variables.
 */
public final class NumberUtils {

    private NumberUtils() {

    }

    /**
     * Formats the input {@code long} with commas, based on {@link Locale#UK}.
     *
     * <p>
     * <b>NOTE:</b> This should only be used for logging. Any values making it to the frontend should be formatted there,
     * otherwise we may end up formatting with the incorrect style.
     *
     * @param number the {@code long} to be formatted
     * @return the formatted {@code long}
     */
    public static String formatWithCommas(final long number) {
        return getNumberFormat().format(number);
    }

    /**
     * Formats the input {@code int} with commas, based on {@link Locale#UK}.
     *
     * <p>
     * <b>NOTE:</b> This should only be used for logging. Any values making it to the frontend should be formatted there,
     * otherwise we may end up formatting with the incorrect style.
     *
     * @param number the {@code int} to be formatted
     * @return the formatted {@code int}
     */
    public static String formatWithCommas(final int number) {
        return getNumberFormat().format(number);
    }

    private static NumberFormat getNumberFormat() {
        return NumberFormat.getInstance(Locale.UK);
    }
}

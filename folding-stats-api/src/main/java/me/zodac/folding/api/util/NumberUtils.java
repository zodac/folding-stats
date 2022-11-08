/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2022 zodac.me
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
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
    public static String formatWithCommas(final Number number) {
        return getNumberFormat().format(number);
    }

    private static NumberFormat getNumberFormat() {
        return NumberFormat.getInstance(Locale.UK);
    }
}

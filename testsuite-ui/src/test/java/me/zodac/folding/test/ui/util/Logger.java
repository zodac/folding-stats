/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2024 zodac.me
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

package me.zodac.folding.test.ui.util;

/**
 * Utility class to log outputs to maven output.
 *
 * <p>
 * Rather than adding a dependency for logging, just print to console.
 */
public final class Logger {

    private Logger() {

    }

    /**
     * Log an entry to the console, ending with a line break.
     *
     * @param format a format {@link String}
     * @param args   arguments referenced by the format specifiers in the {@code format} {@link String}
     */
    public static void log(final String format, final Object... args) {
        final String logEntry = format + "%n";
        System.out.printf(logEntry, args); // NOPMD: SystemPrintln - Easier than adding a logger to test module
    }
}

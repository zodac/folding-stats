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

package me.zodac.folding.test.util;

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

    /**
     * Log an entry to the console, starting and ending with a line break.
     *
     * @param format a format {@link String}
     * @param args   arguments referenced by the format specifiers in the {@code format} {@link String}
     */
    public static void logWithBlankLine(final String format, final Object... args) {
        final String logEntry = "%n" + format;
        log(logEntry, args);
    }
}

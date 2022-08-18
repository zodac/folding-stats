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
 * Utility class used to retrieve the name of log files.
 */
public enum LoggerName {

    AUDIT("audit"),
    LARS("lars"),
    SECURITY("security"),
    SQL("sql"),
    STATS("stats");

    private static final String DEFAULT_VALUE = null;

    private final String loggerNameValue;

    LoggerName(final String loggerNameValue) {
        this.loggerNameValue = loggerNameValue;
    }

    /**
     * Retrieves the name of the logger. If environment variable <b>MULTIPLE_LOG_FILES</b> is not set to {@code true}, then {@code null} is returned,
     * which indicates the default logger should be used.
     *
     * @return the name of the logger
     */
    public String get() {
        if (EnvironmentVariableUtils.isEnabled("MULTIPLE_LOG_FILES")) {
            return loggerNameValue;
        }

        return DEFAULT_VALUE;
    }
}

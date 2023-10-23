/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2023 zodac.me
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

import org.checkerframework.nullaway.checker.nullness.qual.Nullable;

/**
 * Utility class used to retrieve the name of log files.
 */
public enum LoggerName {

    /**
     * The {@code audit.log} for REST requests.
     */
    AUDIT("audit"),

    /**
     * The {@code lars.log} for LARS hardware updates.
     */
    LARS("lars"),

    /**
     * The {@code security.log} for privileged REST requests.
     */
    SECURITY("security"),

    /**
     * The {@code sql.log} for SQL queries.
     */
    SQL("sql"),

    /**
     * The {@code stats.log} for {@code Team Competition} stats updates.
     */
    STATS("stats");

    private static final @Nullable String DEFAULT_VALUE = null;

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
    @Nullable
    public String get() {
        if (EnvironmentVariableUtils.isEnabled("MULTIPLE_LOG_FILES")) {
            return loggerNameValue;
        }

        return DEFAULT_VALUE;
    }
}

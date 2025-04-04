/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2025 zodac.net
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

package net.zodac.folding.db;

import java.util.Collection;
import java.util.stream.Stream;

/**
 * Lists the supported databases for the system. Should be set by the environment variable <b>DEPLOYED_DATABASE</b>.
 */
public enum DatabaseType {

    /**
     * The system is using a <b>PostgreSQL</b> database.
     */
    POSTGRESQL,

    /**
     * Not a valid {@link DatabaseType}.
     */
    INVALID;

    private static final Collection<DatabaseType> ALL_VALUES = Stream.of(values())
        .filter(value -> value != INVALID)
        .toList();

    /**
     * Retrieve a {@link DatabaseType} based on the input {@link String}. The search is case-insensitive.
     *
     * @param input the {@link DatabaseType} as a {@link String}
     * @return the matching {@link DatabaseType}, or {@link DatabaseType#INVALID} if none is found
     */
    public static DatabaseType get(final String input) {
        return ALL_VALUES
            .stream()
            .filter(category -> category.toString().equalsIgnoreCase(input))
            .findAny()
            .orElse(INVALID);
    }
}

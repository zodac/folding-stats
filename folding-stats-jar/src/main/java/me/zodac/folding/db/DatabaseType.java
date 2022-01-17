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

package me.zodac.folding.db;

import static java.util.stream.Collectors.toUnmodifiableList;

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
        .collect(toUnmodifiableList());

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
            .orElse(DatabaseType.INVALID);
    }
}
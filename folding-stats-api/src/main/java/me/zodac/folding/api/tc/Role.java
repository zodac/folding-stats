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

package me.zodac.folding.api.tc;

import java.util.Collection;
import java.util.List;

/**
 * Defines the possible roles for a {@link User} within a {@link Team}.
 */
public enum Role {

    /**
     * The {@link User} is the captain of the @link Team}.
     */
    CAPTAIN(true),

    /**
     * The {@link User} is a normal {@link Team} member.
     */
    MEMBER(false),

    /**
     * Not a valid {@link Role}.
     */
    INVALID(false);

    private static final Collection<Role> ALL_VALUES = List.of(values());

    private final boolean isCaptain;

    Role(final boolean isCaptain) {
        this.isCaptain = isCaptain;
    }

    /**
     * Whether the {@link Role} is a captaincy role.
     *
     * @return if the {@link Role} is a captain
     */
    public boolean isCaptain() {
        return isCaptain;
    }

    /**
     * Retrieve all available {@link Role}s (excluding {@link Role#INVALID}).
     *
     * <p>
     * Should be used instead of {@link Role#values()}, as that recalculates the array for each call,
     * while this method uses a static {@link Collection}.
     *
     * @return an unmodifiable {@link Collection} of all {@link Role}s
     */
    public static Collection<Role> getAllValues() {
        return ALL_VALUES
            .stream()
            .filter(value -> value != INVALID)
            .toList();
    }

    /**
     * Retrieve a {@link Role} based on the input {@link String}. The search is case-insensitive.
     *
     * @param input the {@link Role} as a {@link String}
     * @return the matching {@link Role}, or {@link Role#INVALID} if none is found
     */
    public static Role get(final String input) {
        return ALL_VALUES
            .stream()
            .filter(role -> role.toString().equalsIgnoreCase(input))
            .findAny()
            .orElse(INVALID);
    }
}

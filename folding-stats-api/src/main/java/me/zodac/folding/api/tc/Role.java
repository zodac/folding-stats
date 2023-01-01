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

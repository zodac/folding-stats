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

package me.zodac.folding.api.tc;

import java.util.Collection;
import java.util.List;

/**
 * Specifies the make/brand of {@link Hardware}.
 */
public enum HardwareMake {

    /**
     * The {@link Hardware} is by <b>AMD</b>.
     */
    AMD,

    /**
     * The {@link Hardware} is by <b>Intel</b>.
     */
    INTEL,

    /**
     * The {@link Hardware} is by <b>nVidia</b>.
     */
    NVIDIA,

    /**
     * Not a valid {@link HardwareMake}.
     */
    INVALID;

    private static final Collection<HardwareMake> ALL_VALUES = List.of(values());

    /**
     * Retrieve all available {@link HardwareMake}s (excluding {@link HardwareMake#INVALID}).
     *
     * <p>
     * Should be used instead of {@link HardwareMake#values()}, as that recalculates the array for each call,
     * while this method uses a static {@link Collection}.
     *
     * @return an unmodifiable {@link Collection} of all {@link HardwareMake}s
     */
    public static Collection<HardwareMake> getAllValues() {
        return ALL_VALUES
            .stream()
            .filter(value -> value != INVALID)
            .toList();
    }

    /**
     * Retrieve a {@link HardwareMake} based on the input {@link String}. The search is case-insensitive.
     *
     * @param input the {@link HardwareMake} as a {@link String}
     * @return the matching {@link HardwareMake}, or {@link HardwareMake#INVALID} if none is found
     */
    public static HardwareMake get(final String input) {
        return ALL_VALUES
            .stream()
            .filter(hardwareMake -> hardwareMake.toString().equalsIgnoreCase(input))
            .findAny()
            .orElse(INVALID);
    }
}

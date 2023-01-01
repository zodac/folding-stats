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
 * Specifies the type of {@link Hardware}.
 */
public enum HardwareType {

    /**
     * The {@link Hardware} is a CPU.
     */
    CPU,

    /**
     * The {@link Hardware} is a GPU.
     */
    GPU,

    /**
     * Not a valid {@link HardwareType}.
     */
    INVALID;

    private static final Collection<HardwareType> ALL_VALUES = List.of(values());

    /**
     * Retrieve all available {@link HardwareType}s (excluding {@link HardwareType#INVALID}).
     *
     * <p>
     * Should be used instead of {@link HardwareType#values()}, as that recalculates the array for each call,
     * while this method uses a static {@link Collection}.
     *
     * @return an unmodifiable {@link Collection} of all {@link HardwareType}s
     */
    public static Collection<HardwareType> getAllValues() {
        return ALL_VALUES
            .stream()
            .filter(value -> value != INVALID)
            .toList();
    }

    /**
     * Retrieve a {@link HardwareType} based on the input {@link String}. The search is case-insensitive.
     *
     * @param input the {@link HardwareType} as a {@link String}
     * @return the matching {@link HardwareType}, or {@link HardwareType#INVALID} if none is found
     */
    public static HardwareType get(final String input) {
        return ALL_VALUES
            .stream()
            .filter(hardwareType -> hardwareType.toString().equalsIgnoreCase(input))
            .findAny()
            .orElse(INVALID);
    }
}

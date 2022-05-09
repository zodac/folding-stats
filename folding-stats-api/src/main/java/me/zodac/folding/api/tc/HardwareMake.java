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
import java.util.Collections;
import java.util.stream.Stream;

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

    private static final Collection<HardwareMake> ALL_VALUES = Stream.of(values())
        .filter(value -> value != INVALID)
        .toList();

    /**
     * Retrieve all available {@link HardwareMake}s (excluding {@link HardwareMake#INVALID}).
     *
     * <p>
     * Should be used instead of {@link HardwareMake#values()}, as that recalculates the array for each call,
     * while this method uses a static {@link Collection}.
     *
     * @return a {@link Collection} of all {@link HardwareMake}s
     */
    public static Collection<HardwareMake> getAllValues() {
        return ALL_VALUES;
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

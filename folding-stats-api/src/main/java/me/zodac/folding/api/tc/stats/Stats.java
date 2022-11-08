/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2022 zodac.me
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

package me.zodac.folding.api.tc.stats;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * Simple POJO containing stats for a Folding@Home user: points and units.
 */
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Accessors(fluent = true)
@Getter
@EqualsAndHashCode
@ToString(doNotUseGetters = true)
public class Stats {

    /**
     * Initial value for points.
     */
    public static final long DEFAULT_POINTS = 0L;

    /**
     * Initial value for units.
     */
    public static final int DEFAULT_UNITS = 0;

    private final long points;
    private final int units;

    /**
     * Creates an instance of {@link Stats}.
     *
     * @param points the points
     * @param units  the units
     * @return the created {@link Stats}
     */
    public static Stats create(final long points, final int units) {
        return new Stats(points, units);
    }

    /**
     * Checks if the {@link Stats} instance has both {@code points} set to <b>0</b> and {@code unitsOffset} set to <b>0</b>.
     *
     * @return {@code true} if the {@link Stats} instance is empty
     */
    public boolean isEmpty() {
        return points == DEFAULT_POINTS && units == DEFAULT_UNITS;
    }
}

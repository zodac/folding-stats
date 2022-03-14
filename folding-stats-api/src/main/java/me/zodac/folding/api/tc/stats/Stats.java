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

package me.zodac.folding.api.tc.stats;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Simple POJO containing stats for a Folding@Home user: points and units.
 */
@AllArgsConstructor(access = AccessLevel.PROTECTED)
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
     * Checks if the {@link Stats} instance has both {@code points} set to <b>0L</b> and {@code unitsOffset} set to
     * <b>0</b>.
     *
     * @return <code>true</code> if the {@link Stats} instance is empty
     */
    public boolean isEmpty() {
        return points == DEFAULT_POINTS && units == DEFAULT_UNITS;
    }
}

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

package me.zodac.folding.rest.util;

import me.zodac.folding.rest.exception.InvalidIdException;
import me.zodac.folding.rest.exception.OutOfRangeIdException;

/**
 * Utility class used to parse {@link Integer}s for the REST endpoints.
 */
public final class IntegerParser {

    private IntegerParser() {

    }

    /**
     * Attempts to parse the input {@link String} into a valid non-negative {@link Integer} ID.
     *
     * @param possibleId the {@link String} ID to parse into an {@link Integer} ID
     * @return the ID as an {@link Integer}
     * @throws OutOfRangeIdException thrown if the input {@code possibleId} is a valid {@link Integer}, but is less than <b>0</b>
     * @throws InvalidIdException    thrown if the input {@code possibleId} is not a valid {@link Integer}
     */
    public static int parsePositive(final String possibleId) {
        try {
            final int parsedId = Integer.parseInt(possibleId);
            if (parsedId < 0) {
                throw new OutOfRangeIdException(parsedId);
            }

            return parsedId;
        } catch (final NumberFormatException e) {
            throw new InvalidIdException(possibleId, e);
        }
    }
}
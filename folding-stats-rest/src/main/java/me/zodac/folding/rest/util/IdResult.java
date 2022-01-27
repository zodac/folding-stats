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

import org.springframework.http.ResponseEntity;

/**
 * POJO defining the result of {@link IntegerParser#parsePositive(String)}.
 */
public record IdResult(boolean successful, int id, ResponseEntity<?> failureResponse) {

    private static final int INVALID_ID = -1;

    /**
     * Creates an {@link IdResult} for a successfully parsed {@link Integer} ID.
     *
     * @param id the parsed {@link Integer} ID
     * @return the {@link IdResult}
     */
    public static IdResult success(final int id) {
        return new IdResult(true, id, null);
    }

    /**
     * Creates an {@link IdResult} for an invalid parsing due the input value not being a valid
     * {@link Integer}.
     *
     * @param response the failure {@link ResponseEntity}
     * @return the {@link IdResult}
     */
    public static IdResult failure(final ResponseEntity<?> response) {
        return new IdResult(false, INVALID_ID, response);
    }

    /**
     * Checks if the {@link IdResult} failed and does not contain a valid {@link Integer} ID.
     *
     * @return <code>true</code> if the {@link IdResult} does not contain an ID
     */
    public boolean isFailure() {
        return !successful;
    }
}
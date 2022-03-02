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

package me.zodac.folding.api.exception;

import java.io.Serial;
import java.util.Collection;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import me.zodac.folding.api.ResponsePojo;

/**
 * Exception thrown when an object being deleted is still in use by another object.
 */
public class UsedByException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -4246815106029647106L;

    /**
     * The actual failure to be returned.
     */
    private final UsedByFailure usedByFailure;

    /**
     * Constructor with failing object and objects using it.
     *
     * @param invalidObject the {@link ResponsePojo} that failed validation
     * @param usedBy        a {@link Collection} of the {@link ResponsePojo}s using the failing {@link ResponsePojo}
     */
    public UsedByException(final ResponsePojo invalidObject, final Collection<? extends ResponsePojo> usedBy) {
        super();
        usedByFailure = new UsedByFailure(invalidObject, usedBy);
    }

    /**
     * The {@link UsedByFailure}.
     *
     * @return the {@link UsedByFailure}
     */
    public UsedByFailure getUsedByFailure() {
        return usedByFailure;
    }

    /**
     * Failure object used for REST response.
     */
    @NoArgsConstructor
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Getter
    @Setter
    public static class UsedByFailure {

        private ResponsePojo invalidObject;
        private Collection<? extends ResponsePojo> usedBy;
    }
}
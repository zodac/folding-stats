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

package me.zodac.folding.api.exception;

import java.io.Serial;
import java.util.Collection;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import me.zodac.folding.api.ResponsePojo;

/**
 * Exception thrown when a {@link ResponsePojo} being deleted is still in use by another {@link ResponsePojo}.
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
     * Failure POJO used for REST response.
     */
    @NoArgsConstructor
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Accessors(fluent = true)
    @Getter
    @Setter
    @ToString(doNotUseGetters = true)
    public static class UsedByFailure {

        private ResponsePojo invalidObject;
        private Collection<? extends ResponsePojo> usedBy;
    }
}
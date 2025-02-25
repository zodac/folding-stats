/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2025 zodac.net
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

package net.zodac.folding.rest.exception;

import java.io.Serial;
import lombok.Getter;
import net.zodac.folding.api.tc.change.UserChangeState;

/**
 * {@link Exception} thrown when a {@link UserChangeState} is updated, but an update is not allowed.
 */
@Getter
public class InvalidStateException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 8553443012028566473L;

    /**
     * The current {@link UserChangeState} being changed.
     */
    private final transient UserChangeState fromState;

    /**
     * The wanted {@link UserChangeState} to which we would like to change.
     */
    private final transient UserChangeState toState;

    /**
     * Basic constructor.
     *
     * @param fromState the current {@link UserChangeState}
     * @param toState   the {@link UserChangeState} value trying to be updated
     */
    public InvalidStateException(final UserChangeState fromState, final UserChangeState toState) {
        super();
        this.fromState = fromState;
        this.toState = toState;
    }
}

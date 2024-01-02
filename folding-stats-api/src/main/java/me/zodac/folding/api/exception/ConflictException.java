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

package me.zodac.folding.api.exception;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import lombok.Getter;
import me.zodac.folding.api.RequestPojo;
import me.zodac.folding.api.ResponsePojo;

/**
 * Exception thrown when a {@link RequestPojo} being validated conflicts with an existing {@link ResponsePojo}.
 */
@Getter
public class ConflictException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 8362273370218302973L;

    /**
     * The actual failure to be returned.
     */
    private final ConflictFailure conflictFailure;

    /**
     * Constructor with failing {@link RequestPojo}, the {@link ResponsePojo} it conflicts with and the attributes they conflict on.
     *
     * @param invalidObject         the {@link RequestPojo} that failed validation
     * @param conflictingObject     the {@link ResponsePojo} it conflicts with
     * @param conflictingAttributes the attribute field names that they conflict on
     */
    public ConflictException(final RequestPojo invalidObject, final ResponsePojo conflictingObject, final Collection<String> conflictingAttributes) {
        super();
        conflictFailure = new ConflictFailure(invalidObject, conflictingObject, conflictingAttributes);
    }

    /**
     * Constructor with failing {@link RequestPojo}, the {@link ResponsePojo} it conflicts with and the attribute they conflict on.
     *
     * @param invalidObject        the {@link RequestPojo} that failed validation
     * @param conflictingObject    the {@link ResponsePojo} it conflicts with
     * @param conflictingAttribute the attribute field name that they conflict on
     */
    public ConflictException(final RequestPojo invalidObject, final ResponsePojo conflictingObject, final String conflictingAttribute) {
        this(invalidObject, conflictingObject, List.of(conflictingAttribute));
    }

    /**
     * Failure POJO used for REST response.
     */
    public record ConflictFailure(RequestPojo invalidObject,
                                  ResponsePojo conflictingObject,
                                  Collection<String> conflictingAttributes
    ) implements Serializable {

    }
}

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

package me.zodac.folding.rest.exception;

import java.io.Serial;
import me.zodac.folding.api.ResponsePojo;

/**
 * {@link Exception} thrown when a requested object cannot be found.
 */
public class NotFoundException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -7931149448794408011L;

    /**
     * Basic constructor.
     *
     * @param type the type of object that cannot be found
     * @param id   the ID of the object that cannot be found
     */
    public NotFoundException(final Class<? extends ResponsePojo> type, final int id) {
        super(String.format("No %s found with ID '%s'", type.getSimpleName(), id));
    }

    /**
     * Basic constructor.
     *
     * @param type the type of object that cannot be found
     * @param name the name of the object that cannot be found
     */
    public NotFoundException(final Class<? extends ResponsePojo> type, final String name) {
        super(String.format("No %s found with name '%s'", type.getSimpleName(), name));
    }
}

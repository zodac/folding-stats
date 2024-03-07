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

package me.zodac.folding.api.state;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Unit tests for {@link SystemState}.
 */
class SystemStateTest {

    @ParameterizedTest
    @CsvSource({
        "AVAILABLE,false,false",
        "WRITE_EXECUTED,false,false",
        "RESETTING_STATS,true,true",
        "STARTING,true,true",
        "UPDATING_STATS,false,true",
    })
    void testSystemStates(final SystemState input, final boolean isReadBlocked, final boolean isWriteBlocked) {
        assertThat(input.isReadBlocked())
            .isEqualTo(isReadBlocked);
        assertThat(input.isWriteBlocked())
            .isEqualTo(isWriteBlocked);
    }
}

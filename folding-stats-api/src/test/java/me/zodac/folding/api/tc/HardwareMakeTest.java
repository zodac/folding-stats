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

package me.zodac.folding.api.tc;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link HardwareMake}.
 */
class HardwareMakeTest {

    @Test
    void testGetAllValues() {
        assertThat(HardwareMake.getAllValues())
            .hasSize(3)
            .doesNotContain(HardwareMake.INVALID);
    }

    @Test
    void testGetCaseInsensitive() {
        assertThat(HardwareMake.get("nVidIA"))
            .isEqualTo(HardwareMake.NVIDIA);
    }

    @Test
    void testGetInvalid() {
        assertThat(HardwareMake.get("does_not_exist"))
            .isEqualTo(HardwareMake.INVALID);
    }
}

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

package net.zodac.folding.api.tc;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link HardwareType}.
 */
class HardwareTypeTest {

    @Test
    void testGetAllValues() {
        assertThat(HardwareType.getAllValues())
            .hasSize(2)
            .doesNotContain(HardwareType.INVALID);
    }

    @Test
    void testGetCaseInsensitive() {
        assertThat(HardwareType.get("gPU"))
            .isEqualTo(HardwareType.GPU);
    }

    @Test
    void testGetInvalid() {
        assertThat(HardwareType.get("does_not_exist"))
            .isEqualTo(HardwareType.INVALID);
    }
}

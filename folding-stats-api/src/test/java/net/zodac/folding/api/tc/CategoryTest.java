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
 * Unit tests for {@link Category}.
 */
class CategoryTest {

    @Test
    void testGetAllValues() {
        assertThat(Category.getAllValues())
            .hasSize(3)
            .doesNotContain(Category.INVALID);
    }

    @Test
    void testMaximumPermittedAmountForAllCategories() {
        assertThat(Category.maximumPermittedAmountForAllCategories())
            .isEqualTo(3);
    }

    @Test
    void testGetCaseInsensitive() {
        assertThat(Category.get("AmD_gPu"))
            .isEqualTo(Category.AMD_GPU);
    }

    @Test
    void testGetInvalid() {
        assertThat(Category.get("does_not_exist"))
            .isEqualTo(Category.INVALID);
    }

    @Test
    void testIsHardwareMakeSupported() {
        assertThat(Category.AMD_GPU.isHardwareMakeSupported(HardwareMake.AMD))
            .isTrue();
        assertThat(Category.AMD_GPU.isHardwareMakeSupported(HardwareMake.INTEL))
            .isFalse();
        assertThat(Category.AMD_GPU.isHardwareMakeSupported(HardwareMake.NVIDIA))
            .isFalse();

        assertThat(Category.NVIDIA_GPU.isHardwareMakeSupported(HardwareMake.AMD))
            .isFalse();
        assertThat(Category.NVIDIA_GPU.isHardwareMakeSupported(HardwareMake.INTEL))
            .isFalse();
        assertThat(Category.NVIDIA_GPU.isHardwareMakeSupported(HardwareMake.NVIDIA))
            .isTrue();

        assertThat(Category.WILDCARD.isHardwareMakeSupported(HardwareMake.AMD))
            .isTrue();
        assertThat(Category.WILDCARD.isHardwareMakeSupported(HardwareMake.INTEL))
            .isTrue();
        assertThat(Category.WILDCARD.isHardwareMakeSupported(HardwareMake.NVIDIA))
            .isTrue();
    }

    @Test
    void testIsHardwareTypeSupported() {
        assertThat(Category.AMD_GPU.isHardwareTypeSupported(HardwareType.CPU))
            .isFalse();
        assertThat(Category.AMD_GPU.isHardwareTypeSupported(HardwareType.GPU))
            .isTrue();

        assertThat(Category.NVIDIA_GPU.isHardwareTypeSupported(HardwareType.CPU))
            .isFalse();
        assertThat(Category.NVIDIA_GPU.isHardwareTypeSupported(HardwareType.GPU))
            .isTrue();

        assertThat(Category.WILDCARD.isHardwareTypeSupported(HardwareType.CPU))
            .isTrue();
        assertThat(Category.WILDCARD.isHardwareTypeSupported(HardwareType.GPU))
            .isTrue();
    }
}

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

package me.zodac.folding.api.tc;

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

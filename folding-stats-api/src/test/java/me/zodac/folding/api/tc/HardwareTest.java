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

import me.zodac.folding.rest.api.tc.request.HardwareRequest;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link Hardware}.
 */
class HardwareTest {

    @Test
    void testCreate() {
        final Hardware hardware = Hardware.create(1, "hardware", "hardware", HardwareMake.AMD, HardwareType.GPU, 1.0D, 1L);

        assertThat(hardware)
            .extracting("id", "hardwareName", "displayName", "hardwareMake", "hardwareType", "multiplier", "averagePpd")
            .containsExactly(1, "hardware", "hardware", HardwareMake.AMD, HardwareType.GPU, 1.0D, 1L);
    }

    @Test
    void testCreate_noId() {
        final Hardware hardware = Hardware.createWithoutId("hardware", "hardware", HardwareMake.AMD, HardwareType.GPU, 1.0D, 1L);

        assertThat(hardware.id())
            .isEqualTo(Hardware.EMPTY_HARDWARE_ID);
    }

    @Test
    void testIsEqualRequest_valid() {
        final Hardware hardware = Hardware.create(1, "hardware", "hardware", HardwareMake.AMD, HardwareType.GPU, 1.0D, 1L);
        final HardwareRequest hardwareRequest = HardwareRequest.builder()
            .hardwareName("hardware")
            .displayName("hardware")
            .hardwareMake(HardwareMake.AMD.toString())
            .hardwareType(HardwareType.GPU.toString())
            .multiplier(1.0D)
            .averagePpd(1L)
            .build();

        assertThat(hardware.isEqualRequest(hardwareRequest))
            .isTrue();
    }

    @Test
    void testIsEqualRequest_invalid() {
        final Hardware hardware = Hardware.create(1, "hardware", "hardware", HardwareMake.AMD, HardwareType.GPU, 1.0D, 1L);
        final HardwareRequest hardwareRequest = HardwareRequest.builder()
            .hardwareName("hardware2")
            .displayName("hardware")
            .hardwareMake(HardwareMake.AMD.toString())
            .hardwareType(HardwareType.GPU.toString())
            .multiplier(1.0D)
            .averagePpd(1L)
            .build();

        assertThat(hardware.isEqualRequest(hardwareRequest))
            .isFalse();
    }
}

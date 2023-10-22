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

        assertThat(hardware.id())
            .isEqualTo(1);
        assertThat(hardware.hardwareName())
            .isEqualTo("hardware");
        assertThat(hardware.displayName())
            .isEqualTo("hardware");
        assertThat(hardware.hardwareMake())
            .isEqualTo(HardwareMake.AMD);
        assertThat(hardware.hardwareType())
            .isEqualTo(HardwareType.GPU);
        assertThat(hardware.multiplier())
            .isEqualTo(1.0D);
        assertThat(hardware.averagePpd())
            .isEqualTo(1L);
    }

    @Test
    void testCreate_noId() {
        final Hardware hardware = Hardware.create(Hardware.EMPTY_HARDWARE_ID, "hardware", "hardware", HardwareMake.AMD, HardwareType.GPU, 1.0D, 1L);

        assertThat(hardware.id())
            .isEqualTo(Hardware.EMPTY_HARDWARE_ID);
    }

    @Test
    void testIsEqualRequest_valid() {
        final Hardware hardware = Hardware.create(1, "hardware", "hardware", HardwareMake.AMD, HardwareType.GPU, 1.0D, 1L);
        final HardwareRequest hardwareRequest = new HardwareRequest(
            "hardware",
            "hardware",
            HardwareMake.AMD.toString(),
            HardwareType.GPU.toString(),
            1.0D,
            1L
        );

        assertThat(hardware.isEqualRequest(hardwareRequest))
            .isTrue();
    }

    @Test
    void testIsEqualRequest_invalid() {
        final Hardware hardware = Hardware.create(1, "hardware", "hardware", HardwareMake.AMD, HardwareType.GPU, 1.0D, 1L);
        final HardwareRequest hardwareRequest = new HardwareRequest(
            "hardware2",
            "hardware",
            HardwareMake.AMD.toString(),
            HardwareType.GPU.toString(),
            1.0D,
            1L
        );

        assertThat(hardware.isEqualRequest(hardwareRequest))
            .isFalse();
    }
}

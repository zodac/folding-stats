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

package me.zodac.folding.lars;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.HardwareMake;
import me.zodac.folding.api.tc.HardwareType;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link HardwareSplitter}.
 */
class HardwareSplitterTest {

    @Test
    void testToCreate() {
        final Collection<Hardware> lars = Set.of(
            createHardware("Test1"),
            createHardware("Test2"),
            createHardware("Test3"),
            createHardwareWithAveragePpd("Test4", 1L)
        );
        final Collection<Hardware> existing = Set.of(
            createHardware("Test1"),
            createHardwareWithAveragePpd("Test4", 2L)
        );

        final Collection<Hardware> toCreate = HardwareSplitter.toCreate(lars, existing);
        final Collection<String> names = toCreate.stream().map(Hardware::hardwareName).toList();

        assertThat(names)
            .hasSize(2)
            .contains(
                "Test2",
                "Test3"
            );
    }

    @Test
    void testToDelete() {
        final Collection<Hardware> lars = Set.of(
            createHardwareWithAveragePpd("Test2", 2L),
            createHardware("Test3")
        );
        final Collection<Hardware> existing = Set.of(
            createHardware("Test1"),
            createHardwareWithAveragePpd("Test2", 2L)
        );

        final Collection<Hardware> toDelete = HardwareSplitter.toDelete(lars, existing);
        final Collection<String> names = toDelete.stream().map(Hardware::hardwareName).toList();

        assertThat(names)
            .hasSize(1)
            .contains(
                "Test1"
            );
    }

    @Test
    void testToUpdate() {
        final Collection<Hardware> lars = Set.of(
            createHardwareWithMultiplier("Test1", 1.00D),
            createHardwareWithMultiplier("Test2", 1.00D),
            createHardwareWithMultiplier("Test3", 1.00D),
            createHardwareWithMultiplier("Test4", 1.00D),
            createHardwareWithMultiplier("Test4", 2.00D) // Duplicate value, should be ignored
        );
        final Collection<Hardware> existing = Set.of(
            createHardwareWithMultiplier("Test1", 2.00D),
            createHardwareWithMultiplier("Test2", 2.00D),
            createHardwareWithMultiplier("Test3", 1.00D)
        );

        final Map<Hardware, Hardware> toUpdate = HardwareSplitter.toUpdate(lars, existing);
        final Collection<String> names = toUpdate.keySet().stream().map(Hardware::hardwareName).toList();

        assertThat(names)
            .hasSize(2)
            .contains(
                "Test1",
                "Test2"
            );
    }

    private static Hardware createHardware(final String hardwareName) {
        return createHardwareWithMultiplier(hardwareName, 1.00D);
    }

    private static Hardware createHardwareWithMultiplier(final String hardwareName, final double multiplier) {
        return Hardware.create(Hardware.EMPTY_HARDWARE_ID, hardwareName, "", HardwareMake.AMD, HardwareType.GPU, multiplier, 1L);
    }

    private static Hardware createHardwareWithAveragePpd(final String hardwareName, final long averagePpd) {
        return Hardware.create(Hardware.EMPTY_HARDWARE_ID, hardwareName, "", HardwareMake.AMD, HardwareType.GPU, 1.00D, averagePpd);
    }
}

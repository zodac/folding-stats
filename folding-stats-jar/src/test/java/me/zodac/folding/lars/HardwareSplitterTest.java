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

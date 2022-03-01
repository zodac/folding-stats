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
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link HardwareSplitter}.
 */
class HardwareSplitterTest {

    @Test
    void testToCreate() {
        final Collection<Hardware> lars = Set.of(
            Hardware.builder()
                .hardwareName("Test1")
                .build(),
            Hardware.builder()
                .hardwareName("Test2")
                .build(),
            Hardware.builder()
                .hardwareName("Test3")
                .build(),
            Hardware.builder()
                .hardwareName("Test4")
                .averagePpd(1)
                .build()
        );
        final Collection<Hardware> existing = Set.of(
            Hardware.builder()
                .hardwareName("Test1")
                .build(),
            Hardware.builder()
                .hardwareName("Test4")
                .averagePpd(2)
                .build()
        );

        final Collection<Hardware> toCreate = HardwareSplitter.toCreate(lars, existing);
        final Collection<String> names = toCreate.stream().map(Hardware::getHardwareName).toList();

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
            Hardware.builder()
                .hardwareName("Test2")
                .averagePpd(1)
                .build(),
            Hardware.builder()
                .hardwareName("Test3")
                .build()
        );
        final Collection<Hardware> existing = Set.of(
            Hardware.builder()
                .hardwareName("Test1")
                .build(),
            Hardware.builder()
                .hardwareName("Test2")
                .averagePpd(2)
                .build()
        );

        final Collection<Hardware> toDelete = HardwareSplitter.toDelete(lars, existing);
        final Collection<String> names = toDelete.stream().map(Hardware::getHardwareName).toList();

        assertThat(names)
            .hasSize(1)
            .contains(
                "Test1"
            );
    }

    @Test
    void testToUpdate() {
        final Collection<Hardware> lars = Set.of(
            Hardware.builder()
                .hardwareName("Test1")
                .multiplier(1.00D)
                .build(),
            Hardware.builder()
                .hardwareName("Test2")
                .multiplier(1.00D)
                .build(),
            Hardware.builder()
                .hardwareName("Test3")
                .multiplier(1.00D)
                .build(),
            Hardware.builder()
                .hardwareName("Test4")
                .multiplier(1.00D)
                .build()
        );
        final Collection<Hardware> existing = Set.of(
            Hardware.builder()
                .hardwareName("Test1")
                .multiplier(2.00D)
                .build(),
            Hardware.builder()
                .hardwareName("Test2")
                .multiplier(2.00D)
                .build(),
            Hardware.builder()
                .hardwareName("Test3")
                .multiplier(1.00D)
                .build()
        );

        final Map<Hardware, Hardware> toUpdate = HardwareSplitter.toUpdate(lars, existing);
        final Collection<String> names = toUpdate.keySet().stream().map(Hardware::getHardwareName).toList();

        assertThat(names)
            .hasSize(2)
            .contains(
                "Test1",
                "Test2"
            );
    }
}

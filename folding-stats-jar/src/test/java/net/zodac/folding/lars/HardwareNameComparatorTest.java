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

package net.zodac.folding.lars;

import static org.assertj.core.api.Assertions.assertThat;

import net.zodac.folding.api.tc.Hardware;
import net.zodac.folding.api.tc.HardwareMake;
import net.zodac.folding.api.tc.HardwareType;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link HardwareNameComparator}.
 */
class HardwareNameComparatorTest {

    @Test
    void whenCompareLarsGpus_givenFirstGpuHasEarlierHardwareName_thenMinusOneIsReturned() {
        final Hardware first = Hardware.create(Hardware.EMPTY_HARDWARE_ID, "abc", "displayName", HardwareMake.AMD, HardwareType.GPU, 1.00D, 0L);
        final Hardware second = Hardware.create(Hardware.EMPTY_HARDWARE_ID, "cba", "displayName", HardwareMake.AMD, HardwareType.GPU, 1.00D, 0L);

        final HardwareNameComparator comparator = HardwareNameComparator.create();
        assertThat(comparator.compare(first, second))
            .isNegative();
    }

    @Test
    void whenCompareLarsGpus_givenSecondGpuHasLaterHardwareName_thenOneIsReturned() {
        final Hardware first = Hardware.create(Hardware.EMPTY_HARDWARE_ID, "cba", "displayName", HardwareMake.AMD, HardwareType.GPU, 1.00D, 0L);
        final Hardware second = Hardware.create(Hardware.EMPTY_HARDWARE_ID, "abc", "displayName", HardwareMake.AMD, HardwareType.GPU, 1.00D, 0L);

        final HardwareNameComparator comparator = HardwareNameComparator.create();
        assertThat(comparator.compare(first, second))
            .isPositive();
    }

    @Test
    void whenCompareLarsGpus_givenBothGpusHaveSameHardwareName_thenZeroIsReturned() {
        final Hardware first = Hardware.create(Hardware.EMPTY_HARDWARE_ID, "abc", "displayName", HardwareMake.AMD, HardwareType.GPU, 1.00D, 0L);
        final Hardware second = Hardware.create(Hardware.EMPTY_HARDWARE_ID, "abc", "displayName", HardwareMake.AMD, HardwareType.GPU, 1.00D, 0L);

        final HardwareNameComparator comparator = HardwareNameComparator.create();
        assertThat(comparator.compare(first, second))
            .isZero();
    }
}

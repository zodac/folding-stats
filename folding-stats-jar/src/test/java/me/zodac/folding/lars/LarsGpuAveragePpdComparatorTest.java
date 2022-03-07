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

import me.zodac.folding.api.tc.lars.LarsGpu;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link LarsGpuAveragePpdComparator}.
 */
class LarsGpuAveragePpdComparatorTest {

    @Test
    void whenCompareLarsGpus_givenFirstGpuHasHigherAveragePpd_thenMinusOneIsReturned() {
        final LarsGpu first = LarsGpu.create("larsGpu", "nVidia", "GTX 480", 1, 200_000L);
        final LarsGpu second = LarsGpu.create("larsGpu", "nVidia", "GTX 480", 1, 100_000L);

        final LarsGpuAveragePpdComparator comparator = LarsGpuAveragePpdComparator.create();
        assertThat(comparator.compare(first, second))
            .isEqualTo(-1);
    }

    @Test
    void whenCompareLarsGpus_givenSecondGpuHasHigherAveragePpd_thenOneIsReturned() {
        final LarsGpu first = LarsGpu.create("larsGpu", "nVidia", "GTX 480", 1, 100_000L);
        final LarsGpu second = LarsGpu.create("larsGpu", "nVidia", "GTX 480", 1, 200_000L);

        final LarsGpuAveragePpdComparator comparator = LarsGpuAveragePpdComparator.create();
        assertThat(comparator.compare(first, second))
            .isOne();
    }

    @Test
    void whenCompareLarsGpus_givenBothGpusHaveSameAveragePpd_thenZeroIsReturned() {
        final LarsGpu first = LarsGpu.create("larsGpu", "nVidia", "GTX 480", 1, 100_000L);
        final LarsGpu second = LarsGpu.create("larsGpu", "nVidia", "GTX 480", 1, 100_000L);

        final LarsGpuAveragePpdComparator comparator = LarsGpuAveragePpdComparator.create();
        assertThat(comparator.compare(first, second))
            .isZero();
    }
}

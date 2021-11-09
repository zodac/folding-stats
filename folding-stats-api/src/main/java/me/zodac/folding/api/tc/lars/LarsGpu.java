/*
 * MIT License
 *
 * Copyright (c) 2021 zodac.me
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

package me.zodac.folding.api.tc.lars;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Representation of a GPU entry from the LARS DB.
 *
 * @see <a href="https://folding.lar.systems/gpu_ppd/overall_ranks">LARS GPU PPD database</a>
 */
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Setter
@EqualsAndHashCode
@ToString(doNotUseGetters = true)
public class LarsGpu {

    private String displayName;
    private String manufacturer;
    private String modelInfo;
    private int rank;
    private long averagePpd;

    /**
     * Create a {@link LarsGpu}.
     *
     * @param displayName  the name
     * @param manufacturer the manufacturer
     * @param modelInfo    the model information
     * @param rank         the {@link LarsGpu}'s PPD rank compared to all other {@link LarsGpu}s
     * @param averagePpd   the average PPD for all operating systems
     * @return the created {@link LarsGpu}
     */
    public static LarsGpu create(final String displayName, final String manufacturer, final String modelInfo, final int rank, final long averagePpd) {
        return new LarsGpu(displayName, manufacturer, modelInfo, rank, averagePpd);
    }
}

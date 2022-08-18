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

import java.io.Serial;
import java.io.Serializable;
import java.util.Comparator;
import me.zodac.folding.api.tc.Hardware;

/**
 * Custom {@link Comparator} that allows for sorting of {@link Hardware}s by only comparing the value of {@link Hardware#hardwareName()}.
 */
final class HardwareNameComparator implements Comparator<Hardware>, Serializable {

    @Serial
    private static final long serialVersionUID = -7988652908218460899L;

    private HardwareNameComparator() {

    }

    /**
     * Creates an instance of {@link HardwareNameComparator}.
     *
     * @return the created {@link HardwareNameComparator}
     */
    static HardwareNameComparator create() {
        return new HardwareNameComparator();
    }

    @Override
    public int compare(final Hardware o1, final Hardware o2) {
        return String.CASE_INSENSITIVE_ORDER.compare(o1.hardwareName(), o2.hardwareName());
    }
}
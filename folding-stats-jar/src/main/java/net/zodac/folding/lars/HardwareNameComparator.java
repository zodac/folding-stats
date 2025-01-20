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

import java.io.Serial;
import java.io.Serializable;
import java.util.Comparator;
import net.zodac.folding.api.tc.Hardware;

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

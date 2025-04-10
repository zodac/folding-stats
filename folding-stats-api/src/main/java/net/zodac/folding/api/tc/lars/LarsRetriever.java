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

package net.zodac.folding.api.tc.lars;

import java.util.Set;
import net.zodac.folding.api.tc.Hardware;

/**
 * Interface defining a class that can retrieve LARS PPD data.
 */
@FunctionalInterface
public interface LarsRetriever {

    /**
     * Gets the GPU {@link Hardware} data from LARS.
     *
     * @param gpuApiUrl the URL to the LARS GPU PPD database API
     * @return the LARS GPU {@link Hardware}
     */
    Set<Hardware> retrieveGpus(String gpuApiUrl);
}

/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2022 zodac.me
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

package me.zodac.folding.bean.tc.lars;

import java.util.Set;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.lars.LarsRetriever;
import me.zodac.folding.lars.HttpLarsRetriever;
import org.springframework.stereotype.Component;

/**
 * Implementation of {@link LarsRetriever} which essentially wraps {@link HttpLarsRetriever}. Used so we can inject an instance
 * instead of creating an instance of {@link HttpLarsRetriever}.
 */
@Component
public class LarsRetrieverImpl implements LarsRetriever {

    private static final HttpLarsRetriever HTTP_LARS_RETRIEVER = HttpLarsRetriever.create();

    @Override
    public Set<Hardware> retrieveGpus(final String gpuApiUrl) {
        return HTTP_LARS_RETRIEVER.retrieveGpus(gpuApiUrl);
    }
}

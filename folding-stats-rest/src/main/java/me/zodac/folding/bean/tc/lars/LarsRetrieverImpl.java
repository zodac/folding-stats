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

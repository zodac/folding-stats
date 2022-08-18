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

package me.zodac.folding.test.proto.steps;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.http.HttpResponse;
import me.zodac.folding.test.framework.TestStep;
import me.zodac.folding.test.framework.annotation.NeedsHttpResponse;
import me.zodac.folding.test.integration.util.rest.response.HttpResponseHeaderUtils;

/**
 * Common REST-based {@link me.zodac.folding.test.framework.TestStep}s for integration tests.
 */
public final class RestSteps {

    /**
     * Verifies that the value of the 'X-Total-Count' header is matches the expected value.
     *
     * @param expectedTotalCountValue the expected value of the 'X-Total-Count' header
     * @see HttpResponseHeaderUtils#getTotalCount(HttpResponse)
     */
    @NeedsHttpResponse
    public static TestStep verifyTotalCountHeaderValue(final int expectedTotalCountValue) {
        return new TestStep(
            "Verify that 'X-Total-Counter' header value has the expected number of entries",
            (testContext) -> {
                final HttpResponse<String> response = testContext.getHttpResponse();
                final int xTotalCount = HttpResponseHeaderUtils.getTotalCount(response);

                assertThat(xTotalCount)
                    .as("Unexpected value for 'X-Total-Count' header")
                    .isEqualTo(expectedTotalCountValue);
            }
        );
    }

    private RestSteps() {

    }
}

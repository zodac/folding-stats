/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2023 zodac.me
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

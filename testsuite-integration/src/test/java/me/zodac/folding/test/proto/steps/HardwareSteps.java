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

import static me.zodac.folding.test.integration.util.rest.request.HardwareUtils.HARDWARE_REQUEST_SENDER;
import static org.assertj.core.api.Assertions.assertThat;

import java.net.HttpURLConnection;
import java.net.http.HttpResponse;
import java.util.Collection;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.client.java.response.HardwareResponseParser;
import me.zodac.folding.test.framework.TestStep;
import me.zodac.folding.test.integration.util.rest.response.HttpResponseHeaderUtils;

/**
 * Common {@link TestStep}s used to execute test logic for {@link Hardware}-based integration tests.
 */
public final class HardwareSteps {

    /**
     * Retrieves existing {@link Hardware} from the system.
     */
    public static final TestStep GET_ALL_HARDWARE = new TestStep(
        "Retrieve all hardware from the system using a GET HTTP request",
        (testContext) -> {
            final HttpResponse<String> response = HARDWARE_REQUEST_SENDER.getAll();
            assertThat(response.statusCode())
                .as("Did not receive a 200_OK HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_OK);
            testContext.addResponse(response);
        }
    );

    /**
     * Uses the existing {@link me.zodac.folding.test.framework.TestContext#getResponse()} to get a {@link Collection} of the {@link Hardware} on the
     * system. Then verifies that it is empty.
     *
     * @see HardwareResponseParser
     */
    public static final TestStep CHECK_NO_HARDWARE_ON_SYSTEM = new TestStep(
        "Verify that no hardware exists on the system",
        (testContext) -> {
            final HttpResponse<String> response = testContext.getResponse();
            final Collection<Hardware> allHardware = HardwareResponseParser.getAll(response);

            assertThat(allHardware)
                .as("Expected no hardware to be returned in the JSON response")
                .isEmpty();
        }
    );

    /**
     * Uses the existing {@link me.zodac.folding.test.framework.TestContext#getResponse()} to extract the value of 'X-Total-Count', and compares it to
     * the {@link Collection} of {@link Hardware} on the system. Verifies that the two values match.
     *
     * @see HttpResponseHeaderUtils#getTotalCount(HttpResponse)
     */
    public static final TestStep CHECK_ALL_HARDWARE_EQUALS_TOTAL_COUNT = new TestStep(
        "Verify that the returned hardware from the system has the same number of entries as the 'X-Total-Count' header value",
        (testContext) -> {
            final HttpResponse<String> response = testContext.getResponse();
            final Collection<Hardware> allHardware = HardwareResponseParser.getAll(response);
            final int xTotalCount = HttpResponseHeaderUtils.getTotalCount(response);

            assertThat(xTotalCount)
                .as("Expected number of hardware in JSON response to be the same as value of 'X-Total-Count' header")
                .isEqualTo(allHardware.size());
        }
    );

    private HardwareSteps() {

    }
}

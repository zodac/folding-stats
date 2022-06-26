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

import static me.zodac.folding.test.integration.util.TestAuthenticationData.ADMIN_USER;
import static me.zodac.folding.test.integration.util.rest.request.HardwareUtils.HARDWARE_REQUEST_SENDER;
import static org.assertj.core.api.Assertions.assertThat;

import java.net.http.HttpResponse;
import java.util.Collection;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.client.java.response.HardwareResponseParser;
import me.zodac.folding.rest.api.tc.request.HardwareRequest;
import me.zodac.folding.test.framework.TestStep;
import me.zodac.folding.test.framework.annotation.NeedsHttpResponse;
import me.zodac.folding.test.integration.util.TestGenerator;
import me.zodac.folding.test.integration.util.rest.response.HttpResponseHeaderUtils;

/**
 * Common {@link TestStep}s used to execute test logic for {@link Hardware}-based integration tests.
 */
public final class HardwareSteps {

    /**
     * Retrieves existing {@link Hardware} from the system, and verifies that a <b>200_OK</b> HTTP status code was returned.
     *
     * <p>
     * Stores the {@link HttpResponse} in the {@link me.zodac.folding.test.framework.TestContext}.
     *
     * @param expectedHttpStatusCode the expected HTTP status code
     */
    public static TestStep getAllHardware(final int expectedHttpStatusCode) {
        return new TestStep(
            "Retrieve all hardware from the system using a GET HTTP request",
            (testContext) -> {
                final HttpResponse<String> response = HARDWARE_REQUEST_SENDER.getAll();
                assertThat(response.statusCode())
                    .as(String.format("Did not receive a %d HTTP response: %s", expectedHttpStatusCode, response.body()))
                    .isEqualTo(expectedHttpStatusCode);

                testContext.putHttpResponse(response);
            }
        );
    }

    /**
     * Verifies that the number of {@link Hardware} on the system is the expected amount.
     *
     * @param expectedNumberOfHardware the expected number of {@link Hardware} to be returned
     * @return the {@link TestStep}
     */
    @NeedsHttpResponse
    public static TestStep verifyNumberOfHardwareOnSystemEquals(final int expectedNumberOfHardware) {
        return new TestStep(String.format("Verify that %d hardware exists on the system", expectedNumberOfHardware),
            (testContext) -> {
                final HttpResponse<String> response = testContext.getHttpResponse();
                final Collection<Hardware> allHardware = HardwareResponseParser.getAll(response);

                assertThat(allHardware.size())
                    .as("Unexpected number of hardware was returned in the JSON response")
                    .isEqualTo(expectedNumberOfHardware);
            }
        );
    }

    /**
     * Verifies that the value of the 'X-Total-Count' header is the same as the number of {@link Hardware} in the system.
     *
     * @see HttpResponseHeaderUtils#getTotalCount(HttpResponse)
     */
    @NeedsHttpResponse
    public static TestStep checkAllHardwareEqualsTotalCount() {
        return new TestStep(
            "Verify that the returned hardware from the system has the same number of entries as the 'X-Total-Count' header value",
            (testContext) -> {
                final HttpResponse<String> response = testContext.getHttpResponse();
                final Collection<Hardware> allHardware = HardwareResponseParser.getAll(response);
                final int xTotalCount = HttpResponseHeaderUtils.getTotalCount(response);

                assertThat(allHardware.size())
                    .as("Expected number of hardware in JSON response to be the same as value of 'X-Total-Count' header")
                    .isEqualTo(xTotalCount);
            }
        );
    }

    /**
     * Creates a new {@link Hardware} in the system. Once created, performs the following checks:
     *
     * <ol>
     *     <li>Verifies that the expected HTTP status code was returned</li>
     *     <li>Extract the created {@link Hardware} and verify the fields 'hardwareName', 'displayName' and 'multiplier' match the input JSON</li>
     * </ol>
     *
     * @param expectedHttpStatusCode the expected HTTP status code
     * @see TestGenerator#generateHardware()
     * @see me.zodac.folding.client.java.request.HardwareRequestSender#create(HardwareRequest, String, String)
     */
    public static TestStep createNewHardware(final int expectedHttpStatusCode) {
        return new TestStep(
            "Create a new hardware on the system",
            (testContext) -> {
                final HardwareRequest hardwareToCreate = TestGenerator.generateHardware();
                final HttpResponse<String> response = HARDWARE_REQUEST_SENDER.create(hardwareToCreate, ADMIN_USER.userName(), ADMIN_USER.password());

                assertThat(response.statusCode())
                    .as(String.format("Did not receive a %d HTTP response: %s", expectedHttpStatusCode, response.body()))
                    .isEqualTo(expectedHttpStatusCode);

                final Hardware actual = HardwareResponseParser.create(response);
                assertThat(actual)
                    .as("Did not receive created object as JSON response: " + response.body())
                    .extracting("hardwareName", "displayName", "multiplier")
                    .containsExactly(hardwareToCreate.getHardwareName(), hardwareToCreate.getDisplayName(), hardwareToCreate.getMultiplier());

                testContext.putHttpResponse(response);

            }
        );
    }

    private HardwareSteps() {

    }
}

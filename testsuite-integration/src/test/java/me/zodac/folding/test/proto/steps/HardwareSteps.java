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

import java.net.HttpURLConnection;
import java.net.http.HttpResponse;
import java.util.Collection;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.client.java.response.HardwareResponseParser;
import me.zodac.folding.rest.api.tc.request.HardwareRequest;
import me.zodac.folding.test.framework.TestStep;
import me.zodac.folding.test.framework.annotation.NeedsHttpResponse;
import me.zodac.folding.test.framework.annotation.SavesHttpResponse;
import me.zodac.folding.test.integration.util.TestGenerator;

/**
 * {@link TestStep}s used to execute test logic for {@link Hardware}-based integration tests.
 */
public final class HardwareSteps {

    /**
     * Retrieves existing {@link Hardware} from the system, and verifies that the expected HTTP status code was returned.
     *
     * @param expectedHttpStatusCode the expected HTTP status code
     */
    @SavesHttpResponse
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
     * Retrieves existing {@link Hardware} from the system, and verifies that the expected HTTP status code was returned. The {@link Hardware} is
     * retrieved by 'id', and the 'id' value is retrieved from the previous {@link #createNewHardware(int)} {@link HttpResponse}.
     *
     * @param expectedHttpStatusCode the expected HTTP status code
     */
    @NeedsHttpResponse
    public static TestStep getHardwareByIdFromPreviousResponse(final int expectedHttpStatusCode) {
        return new TestStep(
            "Retrieves hardware by ID from the system using a GET HTTP request",
            (testContext) -> {
                final HttpResponse<String> createResponse = testContext.getHttpResponse();
                final Hardware createdHardware = HardwareResponseParser.create(createResponse);

                final HttpResponse<String> response = HARDWARE_REQUEST_SENDER.get(createdHardware.id());
                assertThat(response.statusCode())
                    .as(String.format("Did not receive a %d HTTP response: %s", expectedHttpStatusCode, response.body()))
                    .isEqualTo(expectedHttpStatusCode);

                // If a positive result is expected, verify the returned response is valid
                if (expectedHttpStatusCode == HttpURLConnection.HTTP_OK) {
                    final Hardware retrievedHardware = HardwareResponseParser.get(response);
                    assertThat(retrievedHardware)
                        .as("Retrieved hardware was not the same as the created hardware")
                        .isEqualTo(createdHardware);
                }
            }
        );
    }

    /**
     * Retrieves existing {@link Hardware} from the system, and verifies that the expected HTTP status code was returned. The {@link Hardware} is
     * retrieved by 'hardwareName', and the 'hardwareName' value is retrieved from the previous {@link #createNewHardware(int)} {@link HttpResponse}.
     *
     * @param expectedHttpStatusCode the expected HTTP status code
     */
    @NeedsHttpResponse
    public static TestStep getHardwareByNameFromPreviousResponse(final int expectedHttpStatusCode) {
        return new TestStep(
            "Retrieves hardware by ID from the system using a GET HTTP request",
            (testContext) -> {
                final HttpResponse<String> createResponse = testContext.getHttpResponse();
                final Hardware createdHardware = HardwareResponseParser.create(createResponse);

                final HttpResponse<String> response = HARDWARE_REQUEST_SENDER.get(createdHardware.hardwareName());
                assertThat(response.statusCode())
                    .as(String.format("Did not receive a %d HTTP response: %s", expectedHttpStatusCode, response.body()))
                    .isEqualTo(expectedHttpStatusCode);

                final Hardware retrievedHardware = HardwareResponseParser.get(response);
                assertThat(retrievedHardware)
                    .as("Retrieved hardware was not the same as the created hardware")
                    .isEqualTo(createdHardware);
            }
        );
    }

    /**
     * Verifies that there are no {@link Hardware}s on the system.
     *
     * @return the {@link TestStep}
     * @see #verifyNumberOfHardwareOnSystemEquals(int)
     */
    @NeedsHttpResponse
    public static TestStep verifyNoHardwareOnSystem() {
        return verifyNumberOfHardwareOnSystemEquals(0);
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
     * Creates a new {@link Hardware} in the system. Once created, performs the following checks:
     *
     * <ol>
     *     <li>Verifies that the expected HTTP status code was returned</li>
     *     <li>Extracts the created {@link Hardware} and verifies the fields 'hardwareName', 'displayName' and 'multiplier' match the input JSON</li>
     * </ol>
     *
     * @param expectedHttpStatusCode the expected HTTP status code
     * @see TestGenerator#generateHardware()
     * @see me.zodac.folding.client.java.request.HardwareRequestSender#create(HardwareRequest, String, String)
     */
    @SavesHttpResponse
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

    /**
     * Updates an existing {@link Hardware} in the system. Once created, performs the following checks:
     *
     * <ol>
     *     <li>Verifies that the expected HTTP status code was returned</li>
     *     <li>Extracts the updated {@link Hardware} and verifies the returned payload matches the wanted {@link Hardware}</li>
     * </ol>
     *
     * @param newHardwareName        the new value for the 'hardwareName' and 'displayName' fields
     * @param expectedHttpStatusCode the expected HTTP status code
     * @see me.zodac.folding.client.java.request.HardwareRequestSender#update(int, HardwareRequest, String, String)
     */
    @NeedsHttpResponse
    @SavesHttpResponse
    public static TestStep updateHardwareFromPreviousResponse(final String newHardwareName, final int expectedHttpStatusCode) {
        return new TestStep(
            "Update the 'hardwareName' and 'displayName' of an existing hardware on the system",
            (testContext) -> {
                final HttpResponse<String> response = testContext.getHttpResponse();
                final Hardware createdHardware = HardwareResponseParser.create(response);

                final HardwareRequest updatedHardware = HardwareRequest.builder()
                    .hardwareName(newHardwareName)
                    .displayName(newHardwareName)
                    .hardwareMake(createdHardware.hardwareMake().toString())
                    .hardwareType(createdHardware.hardwareType().toString())
                    .multiplier(createdHardware.multiplier())
                    .averagePpd(createdHardware.averagePpd())
                    .build();

                final HttpResponse<String> updateResponse =
                    HARDWARE_REQUEST_SENDER.update(createdHardware.id(), updatedHardware, ADMIN_USER.userName(), ADMIN_USER.password());
                assertThat(updateResponse.statusCode())
                    .as(String.format("Did not receive a %d HTTP response: %s", expectedHttpStatusCode, response.body()))
                    .isEqualTo(expectedHttpStatusCode);

                final Hardware actual = HardwareResponseParser.update(updateResponse);
                assertThat(actual.isEqualRequest(updatedHardware))
                    .as("Did not receive created object as JSON response: " + response.body())
                    .isTrue();

                testContext.putHttpResponse(updateResponse);
            }
        );
    }

    /**
     * Deletes an existing {@link Hardware} in the system, and verifies that the expected HTTP status code was returned.
     *
     * @param expectedHttpStatusCode the expected HTTP status code
     * @see me.zodac.folding.client.java.request.HardwareRequestSender#delete(int, String, String)
     */
    @NeedsHttpResponse
    public static TestStep deleteHardwareFromPreviousResponse(final int expectedHttpStatusCode) {
        return new TestStep(
            "Delete an existing hardware on the system",
            (testContext) -> {
                final HttpResponse<String> response = testContext.getHttpResponse();
                final Hardware createdHardware = HardwareResponseParser.create(response);

                final HttpResponse<Void> deleteResponse =
                    HARDWARE_REQUEST_SENDER.delete(createdHardware.id(), ADMIN_USER.userName(), ADMIN_USER.password());

                assertThat(deleteResponse.statusCode())
                    .as(String.format("Did not receive a %d HTTP response: %s", expectedHttpStatusCode, response.body()))
                    .isEqualTo(HttpURLConnection.HTTP_OK);
            }
        );
    }

    private HardwareSteps() {

    }
}

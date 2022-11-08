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

package me.zodac.folding.test.proto;

import static me.zodac.folding.test.proto.steps.HardwareSteps.createNewHardware;
import static me.zodac.folding.test.proto.steps.HardwareSteps.deleteHardwareFromPreviousResponse;
import static me.zodac.folding.test.proto.steps.HardwareSteps.getAllHardware;
import static me.zodac.folding.test.proto.steps.HardwareSteps.getHardwareByIdFromPreviousResponse;
import static me.zodac.folding.test.proto.steps.HardwareSteps.getHardwareByNameFromPreviousResponse;
import static me.zodac.folding.test.proto.steps.HardwareSteps.updateHardwareFromPreviousResponse;
import static me.zodac.folding.test.proto.steps.HardwareSteps.verifyNoHardwareOnSystem;
import static me.zodac.folding.test.proto.steps.HardwareSteps.verifyNumberOfHardwareOnSystemEquals;
import static me.zodac.folding.test.proto.steps.RestSteps.verifyTotalCountHeaderValue;
import static me.zodac.folding.test.proto.steps.SystemSteps.cleanSystem;

import java.net.HttpURLConnection;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.test.framework.TestSuite;
import me.zodac.folding.test.framework.builder.SuiteBuilder;
import org.junit.jupiter.api.Test;

/**
 * {@link TestSuite} for the {@link Hardware} REST endpoint at {@code /folding/hardware}.
 */
class HardwareSuite {

    @Test
    void hardwareSuite() {
        final TestSuite positiveTestSuite = new SuiteBuilder()
            .testSuiteName("Hardware Test Suite (positive test cases)")
            .withCommonTestStep(cleanSystem())
            .withConfiguration()
                .continueOnFailure()
                .fullErrorStackTrace(false)
            .withTestCase()
                .when("Getting all hardware")
                .given("No hardware exists in the system")
                .then("An empty JSON response should be returned")
                    .and("The JSON response should have a 200_OK HTTP status code")
                    .and("The 'X-Total-Count' HTTP header should have the same value as the number of hardware in the system")
                .withTestStep(getAllHardware(HttpURLConnection.HTTP_OK))
                .withTestStep(verifyNoHardwareOnSystem())
                .withTestStep(verifyTotalCountHeaderValue(0))
            .withTestCase()
                .when("Creating a hardware")
                .given("The input JSON payload is a valid hardware")
                .then("The created hardware is returned in the response")
                    .and("The JSON response should have a 201_CREATED HTTP status code")
                    .and("The returned hardware should have the same 'hardwareName', 'displayName' and 'multiplier' as the input JSON")
                .withTestStep(createNewHardware(HttpURLConnection.HTTP_CREATED))
                .withTestStep(getAllHardware(HttpURLConnection.HTTP_OK))
                .withTestStep(verifyNumberOfHardwareOnSystemEquals(1))
            .withTestCase()
                .when("Getting a hardware by ID")
                .given("A hardware with that ID exists on the system")
                .then("The existing hardware is returned in the response")
                    .and("The JSON response should have a 200_OK HTTP status code")
                .withTestStep(createNewHardware(HttpURLConnection.HTTP_CREATED))
                .withTestStep(getHardwareByIdFromPreviousResponse(HttpURLConnection.HTTP_OK))
            .withTestCase()
                .when("Getting a hardware by 'hardwareName'")
                .given("A hardware with that 'hardwareName' exists on the system")
                .then("The existing hardware is returned in the response")
                    .and("The JSON response should have a 200_OK HTTP status code")
                .withTestStep(createNewHardware(HttpURLConnection.HTTP_CREATED))
                .withTestStep(getHardwareByNameFromPreviousResponse(HttpURLConnection.HTTP_OK))
            .withTestCase()
                .when("Updating a hardware")
                .given("A valid hardware ID and valid payload")
                .then("The updated harware is returned in the response")
                    .and("The JSON response should have a 200_OK HTTP status code")
                    .and("No additional hardware is created")
                .withTestStep(createNewHardware(HttpURLConnection.HTTP_CREATED))
                .withTestStep(updateHardwareFromPreviousResponse("newHardwareName", HttpURLConnection.HTTP_OK))
                .withTestStep(getAllHardware(HttpURLConnection.HTTP_OK))
                .withTestStep(verifyNumberOfHardwareOnSystemEquals(1))
            .withTestCase()
                .when("Deleting a hardware")
                .given("A valid hardware ID")
                .then("The hardware is deleted")
                    .and("The JSON response should have a 200_OK HTTP status code")
                    .and("The hardware cannot be retrieved again")
                    .and("No hardware is left on the system")
            .withTestStep(createNewHardware(HttpURLConnection.HTTP_CREATED))
            .withTestStep(deleteHardwareFromPreviousResponse(HttpURLConnection.HTTP_OK))
            .withTestStep(getHardwareByIdFromPreviousResponse(HttpURLConnection.HTTP_NOT_FOUND))
            .withTestStep(getAllHardware(HttpURLConnection.HTTP_OK))
            .withTestStep(verifyNoHardwareOnSystem())
            .build();
        positiveTestSuite.execute();
    }
}

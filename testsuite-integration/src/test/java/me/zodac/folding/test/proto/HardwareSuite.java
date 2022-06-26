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

package me.zodac.folding.test.proto;

import static me.zodac.folding.test.proto.steps.HardwareSteps.checkAllHardwareEqualsTotalCount;
import static me.zodac.folding.test.proto.steps.HardwareSteps.createNewHardware;
import static me.zodac.folding.test.proto.steps.HardwareSteps.getAllHardware;
import static me.zodac.folding.test.proto.steps.HardwareSteps.getHardwareByIdFromPreviousResponse;
import static me.zodac.folding.test.proto.steps.HardwareSteps.getHardwareByNameFromPreviousResponse;
import static me.zodac.folding.test.proto.steps.HardwareSteps.verifyNumberOfHardwareOnSystemEquals;
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
        new SuiteBuilder()
            .testSuiteName("Hardware Test Suite")
            .withTestCase()
                .when("Getting all hardware")
                .given("No hardware exists in the system")
                .then("An empty JSON response should be returned")
                    .and("The JSON response should have a 200_OK HTTP status code")
                    .and("The 'X-Total-Count' HTTP header should have the same value as the number of hardware in the system")
                .withTestStep(cleanSystem())
                .withTestStep(getAllHardware(HttpURLConnection.HTTP_OK))
                .withTestStep(verifyNumberOfHardwareOnSystemEquals(0))
                .withTestStep(checkAllHardwareEqualsTotalCount())
            .withTestCase()
                .when("Creating a hardware")
                .given("No the input JSON payload is a valid hardware")
                .then("The created hardware is returned in the response")
                    .and("The JSON response should have a 201_CREATED HTTP status code")
                    .and("The returned hardware should have the same 'hardwareName', 'displayName' and 'multiplier' as the input JSON")
                .withTestStep(cleanSystem())
                .withTestStep(createNewHardware(HttpURLConnection.HTTP_CREATED))
                .withTestStep(getAllHardware(HttpURLConnection.HTTP_OK))
                .withTestStep(verifyNumberOfHardwareOnSystemEquals(1))
            .withTestCase()
                .when("Getting a hardware by 'ID'")
                .given("A hardware with that 'ID' exists on the system")
                .then("The existing hardware is returned in the response")
                    .and("The JSON response should have a 200_OK HTTP status code")
                .withTestStep(cleanSystem())
                .withTestStep(createNewHardware(HttpURLConnection.HTTP_CREATED))
                .withTestStep(getHardwareByIdFromPreviousResponse(HttpURLConnection.HTTP_OK))
            .withTestCase()
                .when("Getting a hardware by 'hardwareName'")
                .given("A hardware with that 'hardwareName' exists on the system")
                .then("The existing hardware is returned in the response")
                    .and("The JSON response should have a 200_OK HTTP status code")
                .withTestStep(cleanSystem())
                .withTestStep(createNewHardware(HttpURLConnection.HTTP_CREATED))
                .withTestStep(getHardwareByNameFromPreviousResponse(HttpURLConnection.HTTP_OK))
            .execute();
    }
}

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

import static me.zodac.folding.test.proto.steps.HardwareSteps.CHECK_ALL_HARDWARE_EQUALS_TOTAL_COUNT;
import static me.zodac.folding.test.proto.steps.HardwareSteps.CHECK_NO_HARDWARE_ON_SYSTEM;
import static me.zodac.folding.test.proto.steps.HardwareSteps.CREATE_NEW_HARDWARE;
import static me.zodac.folding.test.proto.steps.HardwareSteps.GET_ALL_HARDWARE;
import static me.zodac.folding.test.proto.steps.HardwareSteps.VERIFY_CREATED_HARDWARE;
import static me.zodac.folding.test.proto.steps.HardwareSteps.VERIFY_GET_ALL_HARDWARE_STATUS_CODE;
import static me.zodac.folding.test.proto.steps.SystemSteps.CLEAN_SYSTEM;

import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.test.framework.TestSuite;
import me.zodac.folding.test.framework.TestSuiteExecutor;
import me.zodac.folding.test.framework.builder.SuiteBuilder;
import org.junit.jupiter.api.Test;

/**
 * {@link TestSuite} for the {@link Hardware} REST endpoint at {@code /folding/hardware}.
 */
class HardwareSuite {

    @Test
    void hardwareSuite() {
        final TestSuite hardwareTestSuite = new SuiteBuilder()
            .testSuiteName("Hardware Test Suite")
            .withTestCase()
                .when("Getting all hardware")
                .given("No hardware exists in the system")
                .then("An empty JSON response should be returned")
                    .and("The JSON response should have a 200 HTTP status code")
                    .and("The 'X-Total-Count' HTTP header should have the same value as the number of hardware in the system")
                .withTestStep(CLEAN_SYSTEM)
                .withTestStep(GET_ALL_HARDWARE)
                .withTestStep(VERIFY_GET_ALL_HARDWARE_STATUS_CODE)
                .withTestStep(CHECK_NO_HARDWARE_ON_SYSTEM)
                .withTestStep(CHECK_ALL_HARDWARE_EQUALS_TOTAL_COUNT)
            .withTestCase()
                .when("Creating a hardware")
                .given("No the input JSON payload is a valid hardware")
                .then("The created hardware is returned in the response")
                    .and("The JSON response should have a 201 HTTP status code")
                    .and("The returned hardware should have the same 'hardwareName', 'displayName' and 'multiplier' as the input JSON")
                .withTestStep(CLEAN_SYSTEM)
                .withTestStep(GET_ALL_HARDWARE)
                .withTestStep(CREATE_NEW_HARDWARE)
                .withTestStep(VERIFY_CREATED_HARDWARE)
            .build();

        TestSuiteExecutor.execute(hardwareTestSuite);
    }
}

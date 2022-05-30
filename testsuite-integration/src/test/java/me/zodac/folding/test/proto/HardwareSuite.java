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
import static me.zodac.folding.test.proto.steps.HardwareSteps.GET_ALL_HARDWARE;
import static me.zodac.folding.test.proto.steps.SystemSteps.CLEAN_SYSTEM;

import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.test.framework.CaseBuilder;
import me.zodac.folding.test.framework.SuiteBuilder;
import me.zodac.folding.test.framework.TestSuite;
import me.zodac.folding.test.framework.TestSuiteExecutor;
import org.junit.jupiter.api.Test;

/**
 * {@link TestSuite} for the {@link Hardware} REST endpoint at {@code /folding/hardware}.
 */
class HardwareSuite {

    @Test
    void hardwareSuite() {
        final TestSuite hardwareTestSuite = new SuiteBuilder()
            .testSuiteName("Hardware Test Suite")
            .withTestCase(
                new CaseBuilder()
                    .testDescription("""
                        WHEN getting all hardware
                        GIVEN no hardware exists on the system
                        THEN an empty JSON response should be returned with a 200 HTTP status code
                            AND the X-Total-Count HTTP header should match the number of hardware returned""")
                    .withTestStep(CLEAN_SYSTEM)
                    .withTestStep(GET_ALL_HARDWARE)
                    .withTestStep(CHECK_NO_HARDWARE_ON_SYSTEM)
                    .withTestStep(CHECK_ALL_HARDWARE_EQUALS_TOTAL_COUNT)
                    .build()
            )
            .build();

        TestSuiteExecutor.execute(hardwareTestSuite);
    }
}

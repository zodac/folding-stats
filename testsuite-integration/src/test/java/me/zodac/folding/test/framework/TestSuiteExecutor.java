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

package me.zodac.folding.test.framework;

import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Utility class to execute a {@link TestSuite}.
 */
public final class TestSuiteExecutor {

    private static final Logger LOGGER = LogManager.getLogger();

    private TestSuiteExecutor() {

    }

    /**
     * Execute the {@link TestSuite}.
     *
     * @param testSuite the {@link TestSuite} to execute
     */
    public static void execute(final TestSuite testSuite) {
        LOGGER.info("Executing test suite: {}", testSuite.getTestSuiteName());

        for (final TestCase testCase : testSuite.getTestCases()) {
            final List<TestStep> testSteps = testCase.getTestSteps();
            LOGGER.info("Executing test case:\n{}", testCase.getTestCaseDescription());
            int testStepNumber = 1;

            for (final TestStep testStep : testCase.getTestSteps()) {
                try {
                    LOGGER.info("Executing test step ({}/{}):\n{}", testStepNumber, testSteps.size(), testStep.getTestStepDescription());
                    testStep.execute(testCase.getTestContext());
                    LOGGER.info("PASSED");
                    testStepNumber++;
                } catch (final Exception e) {
                    LOGGER.info("Test step #{} '{}' failed", testStepNumber, testStep.getTestStepDescription());
                    throw new AssertionError(e);
                }
            }

            testCase.getTestContext().clear();
        }
    }
}

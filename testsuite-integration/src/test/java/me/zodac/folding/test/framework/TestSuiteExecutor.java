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

package me.zodac.folding.test.framework;

import java.util.List;
import java.util.Locale;
import me.zodac.folding.test.framework.annotation.NeedsHttpResponse;
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
     * <p>
     * The {@link TestCase}s will be executed in the order that they were added to the {@link TestSuite}. Similar for the {@link TestStep}s, they will
     * be executed in the order they were added to the {@link TestCase}.
     *
     * @param testSuite the {@link TestSuite} to execute
     */
    public static void execute(final TestSuite testSuite) {
        final List<TestCase> testCases = testSuite.testCases();
        final List<TestStep> commonTestSteps = testSuite.commonTestSteps();
        LOGGER.info("""
                Executing test suite '{}' with {} test cases and configuration:
                |-------------------------------------|
                | Property                    | Value |
                |-------------------------------------|
                | Continue On Failure         |   {}   |
                | Print Full Error Stacktrace |   {}   |
                |-------------------------------------|
                """,
            testSuite.testSuiteName(),
            testCases.size(),
            formatBooleanForTable(testSuite.continueOnTestFailure()),
            formatBooleanForTable(testSuite.fullErrorStackTrace()));

        int errors = 0;

        // Using a normal FOR loop instead of an enhanced-FOR loop, allowing us to initialise the TestCase using a TRY-WITH-RESOURCES block
        for (int testCaseNumber = 1; testCaseNumber <= testCases.size(); testCaseNumber++) {
            try (final TestCase testCase = testCases.get(testCaseNumber - 1)) {
                final List<TestStep> testSteps = testCase.getTestSteps();
                LOGGER.info("""
                    Executing test case ({}/{}):
                    {}""", testCaseNumber, testCases.size(), testCase.getTestCaseDescription());

                executeCommonTestSteps(testSuite, commonTestSteps, testCase);
                executeTestCaseTestSteps(testSuite, testCase, testSteps);
            } catch (final Exception | AssertionError e) {
                if (testSuite.continueOnTestFailure()) {
                    LOGGER.warn("FAILED: {}\n", e.getMessage());
                    errors++;
                } else {
                    throw e;
                }
            }
            LOGGER.info("Test case {} PASSED\n----------------------------\n", testCaseNumber);
        }

        if (errors == 0) {
            LOGGER.info("PASSED: {} test cases\n", testCases.size());
        } else {
            final String errorMessage = String.format("FAILED: %d/%d test cases", errors, testCases.size());
            LOGGER.error(errorMessage);
            throw new AssertionError(errorMessage);
        }
    }

    private static void executeCommonTestSteps(final TestSuite testSuite, final List<? extends TestStep> commonTestSteps, final TestCase testCase) {
        int commonTestStepNumber = 1;
        for (final TestStep commonTestStep : commonTestSteps) {
            LOGGER.info("""
                    --> Executing common test step ({}/{}) <--
                    {}
                    """, commonTestStepNumber, commonTestSteps.size(),
                commonTestStep.getTestStepDescription());

            executeTestStep(testSuite, commonTestStep, testCase.getTestContext());
            commonTestStepNumber++;
        }
    }

    private static void executeTestCaseTestSteps(final TestSuite testSuite, final TestCase testCase, final List<TestStep> testSteps) {
        int testStepNumber = 1;
        for (final TestStep testStep : testCase.getTestSteps()) {
            LOGGER.info("""
                --> Executing test step ({}/{}) <--
                {}""", testStepNumber, testSteps.size(), testStep.getTestStepDescription());

            // If the TestStep requires a HttpResponse but non exists in the TestContext, fail
            if (testStep.getClass().isAnnotationPresent(NeedsHttpResponse.class) && !testCase.getTestContext().hasHttpResponse()) {
                throw new AssertionError(String.format("Test step #%d '%s' failed due to no HttpResponse being saved in a previous step",
                    testStepNumber, testStep.getTestStepDescription()));
            }

            executeTestStep(testSuite, testStep, testCase.getTestContext());
            testStepNumber++;
        }
    }

    private static void executeTestStep(final TestSuite testSuite, final TestStep testStep, final TestContext testContext) {
        try {
            testStep.execute(testContext);
            LOGGER.info("PASSED\n");
        } catch (final AssertionError e) {
            if (testSuite.fullErrorStackTrace()) {
                throw e;
            }

            throw new AssertionError(e.getMessage()); // NOPMD: PreserveStackTrace - Not interested in trace
        } catch (final Exception e) { // TODO: Make a TestException or something cleaner
            LOGGER.error("Error: {}", e.getMessage());

            if (testSuite.fullErrorStackTrace()) {
                throw new AssertionError(e);
            } else {
                throw new AssertionError(e.getMessage()); // NOPMD: PreserveStackTrace - Not interested in trace
            }
        }
    }

    private static String formatBooleanForTable(final boolean input) {
        return String.valueOf(input)
            .toUpperCase(Locale.UK)
            .substring(0, 1);
    }
}

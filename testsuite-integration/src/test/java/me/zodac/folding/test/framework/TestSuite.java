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

/**
 * Container class defining a {@link TestSuite}.
 *
 * <p>
 * A {@link TestSuite} consists of a name and many {@link TestCase}s.
 *
 * <p>
 * Once created, a {@link TestSuite} can be executed using {@link TestSuiteExecutor}.
 *
 * @param testSuiteName         the name of the {@link TestSuite}
 * @param continueOnTestFailure whether to continue executing {@link TestCase}s after one fails
 * @param fullErrorStackTrace   whether to print the full error stack trace on error, or just the error message
 * @param testCases             the {@link TestCase}s for the {@link TestSuite}
 * @param commonTestSteps       the common {@link TestStep}s for the {@link TestSuite}
 */
public record TestSuite(String testSuiteName,
                        boolean continueOnTestFailure,
                        boolean fullErrorStackTrace,
                        List<TestCase> testCases,
                        List<TestStep> commonTestSteps
) {

    /**
     * Executes this {@link TestSuite}.
     *
     * @see me.zodac.folding.test.framework.TestSuiteExecutor
     */
    public void execute() {
        TestSuiteExecutor.execute(this);
    }
}

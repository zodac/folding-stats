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

import java.util.Collections;
import java.util.List;

/**
 * Container class defining a {@link TestSuite}.
 *
 * <p>
 * A {@link TestSuite} consists of a name and many {@link TestCase}s.
 *
 * <p>
 * Once created, a {@link TestSuite} can be executed using {@link TestSuiteExecutor}.
 */
public class TestSuite {

    private final String testSuiteName;
    private final List<TestCase> testCases;

    /**
     * Creates a {@link TestSuite} instance.
     *
     * @param testSuiteName the name of the {@link TestSuite}
     * @param testCases     the {@link TestCase}s for the {@link TestSuite}
     */
    public TestSuite(final String testSuiteName, final List<TestCase> testCases) {
        this.testSuiteName = testSuiteName;
        this.testCases = Collections.unmodifiableList(testCases);
    }

    public String getTestSuiteName() {
        return testSuiteName;
    }

    public List<TestCase> getTestCases() {
        return testCases;
    }
}

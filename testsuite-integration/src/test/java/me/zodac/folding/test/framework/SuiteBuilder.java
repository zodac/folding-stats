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

import java.util.ArrayList;
import java.util.List;

/**
 * Builder class to construct a {@link TestSuite} with {@link TestCase}s and {@link TestStep}s.
 */
public class SuiteBuilder {

    private String name;
    private final List<TestCase> cases = new ArrayList<>();

    /**
     * Add a name.
     *
     * @param name the name of the {@link TestSuite}
     * @return the {@link SuiteBuilder}
     */
    public SuiteBuilder testSuiteName(final String name) {
        this.name = name;
        return this;
    }

    /**
     * Add a {@link TestCase}.
     *
     * @param testCase a {@link TestCase} to add to the {@link TestSuite}
     * @return the {@link SuiteBuilder}
     */
    public SuiteBuilder withTestCase(final TestCase testCase) {
        cases.add(testCase);
        return this;
    }

    /**
     * Build the {@link TestSuite}.
     *
     * @return the {@link TestSuite}
     */
    public TestSuite build() {
        return new TestSuite(name, cases);
    }
}
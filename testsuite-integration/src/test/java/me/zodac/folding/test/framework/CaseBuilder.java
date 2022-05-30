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
 * Builder class to construct a {@link TestCase} with {@link TestStep}s.
 */
public class CaseBuilder {

    private String description;
    private final List<TestStep> steps = new ArrayList<>();

    /**
     * Add a description for the {@link TestCase}.
     *
     * @param description the {@link TestCase} description
     * @return the {@link CaseBuilder}
     */
    public CaseBuilder testDescription(final String description) {
        this.description = description;
        return this;
    }

    /**
     * Add a {@link TestStep}.
     *
     * @param testStep a {@link TestStep} to add to the {@link TestCase}
     * @return the {@link CaseBuilder}
     */
    public CaseBuilder withTestStep(final TestStep testStep) {
        steps.add(testStep);
        return this;
    }

    /**
     * Build the {@link TestCase}.
     *
     * @return the {@link TestCase}
     */
    public TestCase build() {
        return new TestCase(description, steps);
    }
}

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

import me.zodac.folding.rest.api.exception.FoldingRestException;

/**
 * Container class defining a {@link TestStep}.
 *
 * <p>
 * A {@link TestStep} consists of a description a single executable {@link CheckedConsumer}. It should be small enough to be re-usable across multiple
 * {@link TestCase}s and {@link TestSuite}s.
 *
 * <p>
 * {@link TestStep}s can share context using the {@link TestContext} passed in to each {@link TestStep#execute(TestContext)} call.
 */
public class TestStep {

    private final String testStepDescription;
    private final CheckedConsumer<? super TestContext> testConsumer;

    /**
     * Creates a {@link TestStep} instance.
     *
     * @param testStepDescription the name of the {@link TestStep}
     * @param testConsumer        the {@link CheckedConsumer} defining the test logic
     */
    public TestStep(final String testStepDescription, final CheckedConsumer<? super TestContext> testConsumer) {
        this.testStepDescription = testStepDescription;
        this.testConsumer = testConsumer;
    }

    /**
     * Executes the {@link TestStep}.
     *
     * @param testContext the {@link TestContext} for the {@link TestCase}
     * @throws FoldingRestException thrown if any error occurs
     */
    public void execute(final TestContext testContext) throws FoldingRestException {
        testConsumer.accept(testContext);
    }

    public String getTestStepDescription() {
        return testStepDescription;
    }
}

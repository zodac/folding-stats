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

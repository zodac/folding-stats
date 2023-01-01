/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2023 zodac.me
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

import java.util.Collections;
import java.util.List;

/**
 * Container class defining a {@link TestCase}.
 *
 * <p>
 * A {@link TestCase} consists of a description and many {@link TestStep}s. A {@link TestCase} is executed as part of a {@link TestSuite}.
 */
public class TestCase implements AutoCloseable {

    private final String testCaseDescription;
    private final List<TestStep> testSteps;
    private final TestContext testContext;

    /**
     * Creates a {@link TestCase} instance.
     *
     * @param given     the <b>GIVEN</b> part of the {@link TestCase} description
     * @param givenAnds the <b>GIVEN... AND</b> part of the {@link TestCase} description
     * @param givenOrs  the <b>GIVEN... OR</b> part of the {@link TestCase} description
     * @param when      the <b>WHEN</b> part of the {@link TestCase} description
     * @param whenAnds  the <b>WHEN... AND</b> part of the {@link TestCase} description
     * @param whenOrs   the <b>WHEN... OR</b> part of the {@link TestCase} description
     * @param then      the <b>THEN</b> part of the {@link TestCase} description
     * @param thenAnds  the <b>THEN... AND</b> part of the {@link TestCase} description
     * @param thenOrs   the <b>THEN... OR</b> part of the {@link TestCase} description
     * @param testSteps the {@link TestStep}s for the {@link TestCase}
     */
    public TestCase(final String given, final List<String> givenAnds, final List<String> givenOrs,
                    final String when, final List<String> whenAnds, final List<String> whenOrs,
                    final String then, final List<String> thenAnds, final List<String> thenOrs,
                    final List<TestStep> testSteps) {
        testCaseDescription = constructTestCaseDescription(given, givenAnds, givenOrs, when, whenAnds, whenOrs, then, thenAnds, thenOrs);
        this.testSteps = Collections.unmodifiableList(testSteps);
        testContext = new TestContext();
    }

    public String getTestCaseDescription() {
        return testCaseDescription;
    }

    public List<TestStep> getTestSteps() {
        return testSteps;
    }

    public TestContext getTestContext() {
        return testContext;
    }

    @Override
    public void close() {
        testContext.clear();
    }

    private static String constructTestCaseDescription(final String given, final List<String> givenAnds, final List<String> givenOrs,
                                                       final String when, final List<String> whenAnds, final List<String> whenOrs,
                                                       final String then, final List<String> thenAnds, final List<String> thenOrs) {
        final StringBuilder testCaseDescriptionBuilder = new StringBuilder(72);

        // GIVEN
        testCaseDescriptionBuilder.append("GIVEN: ")
            .append(given)
            .append('\n');

        for (final String givenAnd : givenAnds) {
            testCaseDescriptionBuilder.append("  AND: ")
                .append(givenAnd)
                .append('\n');
        }

        for (final String givenOr : givenOrs) {
            testCaseDescriptionBuilder.append("   OR: ")
                .append(givenOr)
                .append('\n');
        }

        // WHEN
        testCaseDescriptionBuilder.append("WHEN : ")
            .append(when)
            .append('\n');

        for (final String whenAnd : whenAnds) {
            testCaseDescriptionBuilder.append("  AND: ")
                .append(whenAnd)
                .append('\n');
        }

        for (final String whenOr : whenOrs) {
            testCaseDescriptionBuilder.append("   OR: ")
                .append(whenOr)
                .append('\n');
        }

        // THEN
        testCaseDescriptionBuilder.append("THEN : ")
            .append(then)
            .append('\n');

        for (final String thenAnd : thenAnds) {
            testCaseDescriptionBuilder.append("  AND: ")
                .append(thenAnd)
                .append('\n');
        }

        for (final String thenOr : thenOrs) {
            testCaseDescriptionBuilder.append("   OR: ")
                .append(thenOr)
                .append('\n');
        }

        return testCaseDescriptionBuilder.toString();
    }
}

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

package me.zodac.folding.test.framework.builder;

import java.util.ArrayList;
import java.util.List;
import me.zodac.folding.test.framework.TestCase;
import me.zodac.folding.test.framework.TestStep;
import me.zodac.folding.test.framework.TestSuite;

/**
 * Builder class to construct a {@link TestSuite} with {@link TestCase}s and {@link TestStep}s.
 *
 * <p>
 * The methods are chained together to allow the result of one builder to start the next instance, rather than needing to see a lot of calls to
 * {@code .build()}.
 *
 * <p>
 * Can be used as follows:
 *
 * <p>
 * {@snippet :
 * final TestSuite customTestSuite = new SuiteBuilder()
 *     .testSuiteName("Custom Test Suite")
 *     .withTestCase()
 *         .when("System is deployed")
 *             .and("System is cleaned of existing items")
 *         .given("A GET REST request was sent with an ID")
 *             .and("The ID is valid")
 *             .or("No ID is specified")
 *         .then("A JSON response should be returned")
 *             .and("The JSON response should have a 200 HTTP status code")
 *             .and("The correct resource(s) should be returned")
 *         .withTestStep(SystemSteps.SYSTEM_IS_ONLINE)
 *         .withTestStep(SystemSteps.CLEAN_SYSTEM)
 *         .withTestStep(RestSteps.SEND_GET_REQUEST(1))
 *         .withTestStep(RestSteps.VERIFY_STATUS_CODE(200))
 *         .withTestStep(RestSteps.VERIFY_GET_HARDWARE_ID(1))
 *     .withTestCase()
 *         .when("System is deployed")
 *             .and("System is cleaned of existing items")
 *         .given("A DELETE REST request is send with an non-existing ID")
 *         .then("The DELETE request should fail")
 *             .and("The JSON response should have a 404 HTTP status code")
 *             .and("The returned hardware should have the same 'hardwareName', 'displayName' and 'multiplier' as the input JSON")
 *         .withTestStep(SystemSteps.SYSTEM_IS_ONLINE)
 *         .withTestStep(SystemSteps.CLEAN_SYSTEM)
 *         .withTestStep(RestSteps.SEND_DELETE_REQUEST(-1))
 *         .withTestStep(RestSteps.VERIFY_STATUS_CODE(404))
 *     .build();
 *}
 */
public class SuiteBuilder {

    // TestSuite fields
    private String name;
    private final List<TestCase> cases = new ArrayList<>();

    // TestCase description fields
    private String given;
    private String when;
    private String then;
    private final List<String> whenAnds = new ArrayList<>();
    private final List<String> whenOrs = new ArrayList<>();
    private final List<String> givenAnds = new ArrayList<>();
    private final List<String> givenOrs = new ArrayList<>();
    private final List<String> thenAnds = new ArrayList<>();
    private final List<String> thenOrs = new ArrayList<>();

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
     * @return the {@link SuiteBuilder}
     */
    public CaseWhenBuilder withTestCase() {
        return new CaseWhenBuilder(this);
    }

    /**
     * Inner builder class to define the <b>WHEN</b> description for a {@link TestCase}.
     */
    public static class CaseWhenBuilder {

        private final SuiteBuilder suiteBuilder;

        /**
         * Creates a {@link CaseWhenBuilder} instance.
         *
         * @param suiteBuilder the parent {@link SuiteBuilder}
         */
        public CaseWhenBuilder(final SuiteBuilder suiteBuilder) {
            this.suiteBuilder = suiteBuilder;
        }

        /**
         * Adds a <b>WHEN</b> description for the {@link TestCase}.
         *
         * @param when <b>WHEN</b> description
         * @return a {@link CaseGivenBuilder}
         */
        public CaseGivenBuilder when(final String when) {
            suiteBuilder.when = when;
            return new CaseGivenBuilder(suiteBuilder);
        }
    }

    /**
     * Inner builder class to define the <b>GIVEN</b> description for a {@link TestCase}, and also the <b>AND</b>/<b>OR</b> conditions for the
     * <b>WHEN</b> description.
     */
    public static class CaseGivenBuilder {

        private final SuiteBuilder suiteBuilder;

        /**
         * Creates a {@link CaseGivenBuilder} instance.
         *
         * @param suiteBuilder the parent {@link SuiteBuilder}
         */
        public CaseGivenBuilder(final SuiteBuilder suiteBuilder) {
            this.suiteBuilder = suiteBuilder;
        }

        /**
         * Adds an <b>ADD</b> clause to the previous <b>WHEN</b> description for the {@link TestCase}.
         *
         * @param and <b>AND</b> description
         * @return this {@link CaseGivenBuilder} instance
         */
        public CaseGivenBuilder and(final String and) {
            suiteBuilder.whenAnds.add(and);
            return this;
        }

        /**
         * Adds an <b>OR</b> clause to the previous <b>WHEN</b> description for the {@link TestCase}.
         *
         * @param or <b>OR</b> description
         * @return this {@link CaseGivenBuilder} instance
         */
        public CaseGivenBuilder or(final String or) {
            suiteBuilder.whenOrs.add(or);
            return this;
        }

        /**
         * Adds a <b>GIVEN</b> description for the {@link TestCase}.
         *
         * @param given <b>GIVEN</b> description
         * @return a {@link CaseThenBuilder}
         */
        public CaseThenBuilder given(final String given) {
            suiteBuilder.given = given;
            return new CaseThenBuilder(suiteBuilder);
        }
    }

    /**
     * Inner builder class to define the <b>THEN</b> description for a {@link TestCase}, and also the <b>AND</b>/<b>OR</b> conditions for the
     * <b>GIVEN</b> description.
     */
    public static class CaseThenBuilder {

        private final SuiteBuilder suiteBuilder;

        /**
         * Creates a {@link CaseThenBuilder} instance.
         *
         * @param suiteBuilder the parent {@link SuiteBuilder}
         */
        public CaseThenBuilder(final SuiteBuilder suiteBuilder) {
            this.suiteBuilder = suiteBuilder;
        }

        /**
         * Adds an <b>ADD</b> clause to the previous <b>GIVEN</b> description for the {@link TestCase}.
         *
         * @param and <b>AND</b> description
         * @return this {@link CaseThenBuilder} instance
         */
        public CaseThenBuilder and(final String and) {
            suiteBuilder.givenAnds.add(and);
            return this;
        }

        /**
         * Adds an <b>OR</b> clause to the previous <b>GIVEN</b> description for the {@link TestCase}.
         *
         * @param or <b>OR</b> description
         * @return this {@link CaseThenBuilder} instance
         */
        public CaseThenBuilder or(final String or) {
            suiteBuilder.givenOrs.add(or);
            return this;
        }

        /**
         * Adds a <b>THEN</b> description for the {@link TestCase}.
         *
         * @param then <b>THEN</b> description
         * @return a {@link CaseThenAndBuilder}
         */
        public CaseThenAndBuilder then(final String then) {
            suiteBuilder.then = then;
            return new CaseThenAndBuilder(suiteBuilder);
        }
    }

    /**
     * Inner builder class to define the first {@link TestStep} for a {@link TestCase}, and also the <b>AND</b>/<b>OR</b> conditions for the
     * <b>THEN</b> description.
     */
    public static class CaseThenAndBuilder {

        private final SuiteBuilder suiteBuilder;

        /**
         * Creates a {@link CaseThenAndBuilder} instance.
         *
         * @param suiteBuilder the parent {@link SuiteBuilder}
         */
        public CaseThenAndBuilder(final SuiteBuilder suiteBuilder) {
            this.suiteBuilder = suiteBuilder;
        }

        /**
         * Adds an <b>ADD</b> clause to the previous <b>THEN</b> description for the {@link TestCase}.
         *
         * @param and <b>AND</b> description
         * @return this {@link CaseThenAndBuilder} instance
         */
        public CaseThenAndBuilder and(final String and) {
            suiteBuilder.thenAnds.add(and);
            return this;
        }

        /**
         * Adds an <b>OR</b> clause to the previous <b>THEN</b> description for the {@link TestCase}.
         *
         * @param or <b>OR</b> description
         * @return this {@link CaseThenAndBuilder} instance
         */
        public CaseThenAndBuilder or(final String or) {
            suiteBuilder.thenOrs.add(or);
            return this;
        }

        /**
         * Adds the first {@link TestStep} to the current {@link TestCase}.
         *
         * @param firstTestStep the first {@link TestStep}
         * @return this {@link CaseBuilder} instance
         */
        public CaseBuilder withTestStep(final TestStep firstTestStep) {
            return new CaseBuilder(suiteBuilder, firstTestStep);
        }
    }

    /**
     * Inner builder class to define any additional {@link TestStep}s for the {@link TestCase}. We can also continue by adding further
     * {@link TestCase}s, or finish and {@link #build()} the {@link TestSuite}.
     */
    public static class CaseBuilder {

        private final SuiteBuilder suiteBuilder;
        private final List<TestStep> testSteps = new ArrayList<>();

        /**
         * Creates a {@link CaseBuilder} instance.
         *
         * @param suiteBuilder the parent {@link SuiteBuilder}
         * @param testStep     the first {@link TestSuite} for the {@link TestCase}
         */
        public CaseBuilder(final SuiteBuilder suiteBuilder, final TestStep testStep) {
            this.suiteBuilder = suiteBuilder;
            testSteps.add(testStep);
        }

        /**
         * Adds a {@link TestStep} to the current {@link TestCase}.
         *
         * @param testStep the {@link TestStep}
         * @return this {@link CaseBuilder} instance
         */
        public CaseBuilder withTestStep(final TestStep testStep) {
            testSteps.add(testStep);
            return this;
        }

        /**
         * Begins building a new {@link TestCase} for the {@link TestSuite}.
         *
         * @return a new {@link CaseWhenBuilder}
         */
        public CaseWhenBuilder withTestCase() {
            suiteBuilder.cases.add(buildTestCase());
            return new CaseWhenBuilder(suiteBuilder);
        }

        /**
         * Uses the supplied values and builds the {@link TestSuite}.
         *
         * @return the created {@link TestSuite}
         */
        public TestSuite build() {
            suiteBuilder.cases.add(buildTestCase());
            return new TestSuite(suiteBuilder.name, suiteBuilder.cases);
        }

        private TestCase buildTestCase() {
            final TestCase testCase = new TestCase(
                suiteBuilder.given, suiteBuilder.givenAnds, suiteBuilder.givenOrs,
                suiteBuilder.when, suiteBuilder.whenAnds, suiteBuilder.whenOrs,
                suiteBuilder.then, suiteBuilder.thenAnds, suiteBuilder.thenOrs,
                testSteps
            );

            // Clear the lists for the next test case description
            // No need to unset given/when/then as they are Strings and will be overridden by default
            suiteBuilder.givenAnds.clear();
            suiteBuilder.givenOrs.clear();
            suiteBuilder.whenAnds.clear();
            suiteBuilder.whenOrs.clear();
            suiteBuilder.thenAnds.clear();
            suiteBuilder.thenOrs.clear();

            return testCase;
        }
    }

}
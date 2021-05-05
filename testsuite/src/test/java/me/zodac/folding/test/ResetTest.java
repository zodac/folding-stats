package me.zodac.folding.test;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static me.zodac.folding.test.utils.SystemCleaner.cleanSystemForComplexTests;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the monthly reset of the <code>Team Competition</code> stats.
 */
public class ResetTest {

    @BeforeAll
    public static void setUp() {
        cleanSystemForComplexTests();
    }

    @Test
    public void test() {
        assertThat(true)
                .isTrue();
    }

    // TODO: [zodac] Required tests:
    // On reset, retired users are removed from team
    // On reset, overall TC stats are reset
    // On reset, all teams have points reset
    // On reset, all users have points reset
    // On reset, if no team exists, no error occurs

    @AfterAll
    public static void tearDown() {
        cleanSystemForComplexTests();
    }
}
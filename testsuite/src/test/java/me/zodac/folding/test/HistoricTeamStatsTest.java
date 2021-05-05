package me.zodac.folding.test;

import me.zodac.folding.api.tc.Team;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static me.zodac.folding.test.utils.SystemCleaner.cleanSystemForComplexTests;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the historic <code>Team Competition</code> stats for {@link Team}s.
 */
public class HistoricTeamStatsTest {

    @BeforeClass
    public static void setUp() {
        cleanSystemForComplexTests();
    }

    @Test
    public void test() {
        assertThat(true)
                .isTrue();
    }

    // TODO: [zodac] Required tests:
    //   Implement historic stats for teams first

    @AfterClass
    public static void tearDown() {
        cleanSystemForComplexTests();
    }
}

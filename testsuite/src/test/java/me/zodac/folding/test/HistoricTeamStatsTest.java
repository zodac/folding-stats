package me.zodac.folding.test;

import me.zodac.folding.api.tc.Team;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static me.zodac.folding.test.utils.SystemCleaner.cleanSystemForComplexTests;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the historic <code>Team Competition</code> stats for {@link Team}s.
 */
public class HistoricTeamStatsTest {

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
    //   Implement historic stats for teams first

    @AfterAll
    public static void tearDown() {
        cleanSystemForComplexTests();
    }
}

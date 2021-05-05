package me.zodac.folding.test;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static me.zodac.folding.test.utils.SystemCleaner.cleanSystemForComplexTests;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the <code>Team Competition</code> stats calculation.
 */
public class TcStatsTest {

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
    // When no teams exist, response with 0 stats returned
    // When 1 team exists with 1 user:
    //  - Stats for user/team/overall start at 0
    //  - Stats for team/overall increment as user increments
    // When 1 team exists with 2 users:
    //  - Both users are rank 1 at start
    //  - When 1 user has points, ranks become 1/2
    //  - When user rank 2 increments more than user rank 1, ranks change
    // When 2 teams exist with 1 user each:
    //  - Both teams are rank 1 at start
    //  - When 1 teams has points, ranks become 1/2
    //  - When team rank 2 increments more than team rank 1, ranks change
    // When 1 team exists with 1 user with hardware multiplier that is not x1:
    //  - Points are multiplied correctly, original points still available, units are unchanged
    //  - Team is given correct multiplied/original points
    //  - If user has their hardware changed, then subsequent points use new multiplier, old points are unchanged <-- Not tested manually yet
    // When 1 team has an invalid user ID, user is given 0 stats and team gets no new stats
    // When 1 team has 2 users, and one user with stats retires:
    //  - Team does not lose their points, but no new points are added to the team
    //  - Retired user is unretired to same team, stats during retirement are not counted, old stats are retained, and new stats start being added to team again
    //  - Retired user is unretired to new team, user starts from 0 for the team, old team retains points, new stats are added to new team only
    
    @AfterClass
    public static void tearDown() {
        cleanSystemForComplexTests();
    }
}

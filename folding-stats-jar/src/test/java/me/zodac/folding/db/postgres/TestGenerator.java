package me.zodac.folding.db.postgres;

/**
 * Utility class to generate IDs/names for tests.
 */
public final class TestGenerator {

    private static int hardwareCount;
    private static int userCount;
    private static int teamCount;

    private TestGenerator() {

    }

    /**
     * Gets the next {@link me.zodac.folding.api.tc.Hardware} name.
     *
     * @return next name for {@link me.zodac.folding.api.tc.Hardware}
     */
    public static String nextHardwareName() {
        return "hardware_" + hardwareCount++;
    }

    /**
     * Gets the next {@link me.zodac.folding.api.tc.User} name.
     *
     * @return next name for {@link me.zodac.folding.api.tc.User}
     */
    public static String nextUserName() {
        return "user_" + userCount++;
    }

    /**
     * Gets the next {@link me.zodac.folding.api.tc.Team} name.
     *
     * @return next name for {@link me.zodac.folding.api.tc.Team}
     */
    public static String nextTeamName() {
        return "team_" + teamCount++;
    }
}

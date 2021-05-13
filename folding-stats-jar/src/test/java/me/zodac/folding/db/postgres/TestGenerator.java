package me.zodac.folding.db.postgres;

/**
 * Utility class to generate IDs/names for tests.
 */
public class TestGenerator {

    private static int hardwareCount = 0;
    private static int userCount = 0;
    private static int teamCount = 0;

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

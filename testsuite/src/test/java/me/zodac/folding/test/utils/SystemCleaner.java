package me.zodac.folding.test.utils;

import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;

import java.util.Collection;

/**
 * Utility class to clean the system for tests.
 */
public final class SystemCleaner {

    private SystemCleaner() {

    }

    /**
     * Utility function that cleans the system for tests to be executed.
     * <p>
     * Deletes hardware, users and teams through the REST endpoint, then clears the DB tables and resets the serial count for IDs to 0.
     * <p> We delete through the REST endpoint rather than simply the DB because we need to clear the caches in the system.
     */
    public static void cleanSystemForTests() {
        DatabaseCleaner.truncateTableAndResetId("retired_user_stats");

        final Collection<Team> allTeams = TeamUtils.ResponseParser.getAll(TeamUtils.RequestSender.getAll());
        for (final Team team : allTeams) {
            TeamUtils.RequestSender.delete(team.getId());
        }

        final Collection<User> allUsers = UserUtils.ResponseParser.getAll(UserUtils.RequestSender.getAll());
        for (final User user : allUsers) {
            UserUtils.RequestSender.delete(user.getId());
        }

        final Collection<Hardware> allHardware = HardwareUtils.ResponseParser.getAll(HardwareUtils.RequestSender.getAll());
        for (final Hardware hardware : allHardware) {
            HardwareUtils.RequestSender.delete(hardware.getId());
        }

        DatabaseCleaner.truncateTableAndResetId("hardware", "users", "teams");
    }

}

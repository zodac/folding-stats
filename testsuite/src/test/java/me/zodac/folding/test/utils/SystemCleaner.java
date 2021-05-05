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
     * Utility function that cleans the system for simple {@link Hardware}, {@link User} and {@link Team} tests to be executed.
     * <p>
     * Deletes hardware, users and teams through the REST endpoint, then clears the DB tables and resets the serial count for IDs to 0.
     * <p>
     * We delete through the REST endpoint rather than simply the DB because we need to clear the caches in the system.
     * <p>
     * The order of the cleanup is:
     * <ol>
     *     <li>Clean the <b>retired_user_stats</b> DB table, since it references a {@link Team} and a {@link User}</li>
     *     <li>Delete all {@link Team}s, since they reference {@link User}s</li>
     *     <li>Delete all {@link User}s, since they reference {@link Hardware}</li>
     *     <li>Delete all {@link Hardware}</li>
     *     <li>Clean the <b>teams</b>, <b>users</b> and <b>hardware</b> DB tables to reset the IDs</li>
     * </ol>
     *
     * @see DatabaseCleaner#truncateTableAndResetId(String...)
     */
    public static void cleanSystemForSimpleTests() {
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

    /**
     * Utility function that cleans the system for complex <code>Team Competition</code> stats-based tests to be executed.
     * <p>
     * Cleans the stats DB tables, then executes the {@link #cleanSystemForSimpleTests()}. The order of the DB cleanup is:
     * <ol>
     *     <li>user_initial_stats</li>
     *     <li>user_offset_tc_stats</li>
     *     <li>user_tc_stats_hourly</li>
     *     <li>user_total_stats</li>
     * </ol>
     *
     * @see #cleanSystemForSimpleTests()
     */
    public static void cleanSystemForComplexTests() {
        DatabaseCleaner.truncateTableAndResetId("user_initial_stats", "user_offset_tc_stats", "user_tc_stats_hourly", "user_total_stats");
        cleanSystemForSimpleTests();
    }
}

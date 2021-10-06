package me.zodac.folding.test.util;

import static me.zodac.folding.test.util.TestAuthenticationData.ADMIN_USER;
import static me.zodac.folding.test.util.rest.request.HardwareUtils.HARDWARE_REQUEST_SENDER;
import static me.zodac.folding.test.util.rest.request.TeamUtils.TEAM_REQUEST_SENDER;
import static me.zodac.folding.test.util.rest.request.UserUtils.USER_REQUEST_SENDER;

import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.rest.api.exception.FoldingRestException;
import me.zodac.folding.test.util.db.DatabaseUtils;
import me.zodac.folding.test.util.rest.request.HardwareUtils;
import me.zodac.folding.test.util.rest.request.StubbedFoldingEndpointUtils;
import me.zodac.folding.test.util.rest.request.TeamCompetitionStatsUtils;
import me.zodac.folding.test.util.rest.request.TeamUtils;
import me.zodac.folding.test.util.rest.request.UserUtils;

/**
 * Utility class to clean the system for tests.
 */
public final class SystemCleaner {

    private SystemCleaner() {

    }

    /**
     * Utility function that cleans the system for simple {@link Hardware}, {@link User} and {@link Team} tests to be executed.
     *
     * <p>
     * Deletes hardware, users and teams through the REST endpoint, then clears the DB tables and resets the serial count for IDs to 0.
     *
     * <p>
     * We delete through the REST endpoint rather than simply the DB because we need to clear the caches in the system.
     *
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
     * @throws FoldingRestException thrown if an error occurs during cleanup
     * @see DatabaseUtils#truncateTableAndResetId(String...)
     */
    public static void cleanSystemForSimpleTests() throws FoldingRestException {
        DatabaseUtils.truncateTableAndResetId("retired_user_stats");

        for (final User user : UserUtils.getAll()) {
            USER_REQUEST_SENDER.delete(user.getId(), ADMIN_USER.userName(), ADMIN_USER.password());
        }

        for (final Team team : TeamUtils.getAll()) {
            TEAM_REQUEST_SENDER.delete(team.getId(), ADMIN_USER.userName(), ADMIN_USER.password());
        }

        for (final Hardware hardware : HardwareUtils.getAll()) {
            HARDWARE_REQUEST_SENDER.delete(hardware.getId(), ADMIN_USER.userName(), ADMIN_USER.password());
        }

        DatabaseUtils.truncateTableAndResetId("hardware", "users", "teams");
    }

    /**
     * Utility function that cleans the system for complex <code>Team Competition</code> stats-based tests to be executed.
     *
     * <p>
     * Cleans the stats DB tables, resets the stubbed Folding endpoints for units and points, then executes the {@link #cleanSystemForSimpleTests()}.
     * The order of the DB cleanup is:
     * <ol>
     *     <li>user_initial_stats</li>
     *     <li>user_offset_tc_stats</li>
     *     <li>user_tc_stats_hourly</li>
     *     <li>user_total_stats</li>
     * </ol>
     *
     * @throws FoldingRestException thrown if an error occurs during cleanup
     * @see #cleanSystemForSimpleTests()
     */
    public static void cleanSystemForComplexTests() throws FoldingRestException {
        StubbedFoldingEndpointUtils.deletePoints();
        StubbedFoldingEndpointUtils.deleteUnits();
        DatabaseUtils.truncateTableAndResetId("user_initial_stats", "user_offset_tc_stats", "user_tc_stats_hourly", "user_total_stats");
        TeamCompetitionStatsUtils.manuallyResetStats();
        cleanSystemForSimpleTests();
    }
}
/*
 * MIT License
 *
 * Copyright (c) 2021 zodac.me
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

package me.zodac.folding.test.util;

import static me.zodac.folding.test.util.TestAuthenticationData.ADMIN_USER;
import static me.zodac.folding.test.util.rest.request.HardwareUtils.HARDWARE_REQUEST_SENDER;
import static me.zodac.folding.test.util.rest.request.TeamUtils.TEAM_REQUEST_SENDER;
import static me.zodac.folding.test.util.rest.request.UserUtils.USER_REQUEST_SENDER;

import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.rest.api.exception.FoldingRestException;
import me.zodac.folding.rest.api.tc.request.UserRequest;
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
            if (!user.isUserIsCaptain()) {
                USER_REQUEST_SENDER.delete(user.getId(), ADMIN_USER.userName(), ADMIN_USER.password());
                continue;
            }

            // Captains must be unset as captains before they can be deleted
            final User userWithPasskey = UserUtils.getWithPasskey(user.getId());
            final UserRequest userNoLongerCaptain = UserRequest.builder()
                .foldingUserName(userWithPasskey.getFoldingUserName())
                .displayName(userWithPasskey.getDisplayName())
                .passkey(userWithPasskey.getPasskey())
                .category(userWithPasskey.getCategory().toString())
                .profileLink(userWithPasskey.getProfileLink())
                .liveStatsLink(userWithPasskey.getLiveStatsLink())
                .hardwareId(userWithPasskey.getHardware().getId())
                .teamId(userWithPasskey.getTeam().getId())
                .userIsCaptain(false)
                .build();

            USER_REQUEST_SENDER.update(userWithPasskey.getId(), userNoLongerCaptain, ADMIN_USER.userName(), ADMIN_USER.password());
            USER_REQUEST_SENDER.delete(userWithPasskey.getId(), ADMIN_USER.userName(), ADMIN_USER.password());
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
     *     <li>monthly_results</li>
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
        DatabaseUtils.truncateTable("monthly_results");
        DatabaseUtils.truncateTableAndResetId("user_initial_stats", "user_offset_tc_stats", "user_tc_stats_hourly", "user_total_stats");
        TeamCompetitionStatsUtils.manuallyResetStats();
        cleanSystemForSimpleTests();
    }
}

/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2025 zodac.net
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package net.zodac.folding.test.integration.util;

import static net.zodac.folding.test.integration.util.DummyAuthenticationData.ADMIN_USER;
import static net.zodac.folding.test.integration.util.rest.request.HardwareUtils.HARDWARE_REQUEST_SENDER;
import static net.zodac.folding.test.integration.util.rest.request.TeamUtils.TEAM_REQUEST_SENDER;
import static net.zodac.folding.test.integration.util.rest.request.UserUtils.USER_REQUEST_SENDER;

import net.zodac.folding.api.tc.Hardware;
import net.zodac.folding.api.tc.Role;
import net.zodac.folding.api.tc.Team;
import net.zodac.folding.api.tc.User;
import net.zodac.folding.rest.api.exception.FoldingRestException;
import net.zodac.folding.rest.api.tc.request.UserRequest;
import net.zodac.folding.test.integration.util.db.DatabaseUtils;
import net.zodac.folding.test.integration.util.rest.request.HardwareUtils;
import net.zodac.folding.test.integration.util.rest.request.StubbedFoldingEndpointUtils;
import net.zodac.folding.test.integration.util.rest.request.TeamCompetitionStatsUtils;
import net.zodac.folding.test.integration.util.rest.request.TeamUtils;
import net.zodac.folding.test.integration.util.rest.request.UserUtils;

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
            if (user.role() != Role.CAPTAIN) {
                USER_REQUEST_SENDER.delete(user.id(), ADMIN_USER.userName(), ADMIN_USER.password());
                continue;
            }

            // Captains must be unset as captains before they can be deleted
            final User userWithPasskey = UserUtils.getWithPasskey(user.id());
            removeCaptaincyFromUser(userWithPasskey);
            USER_REQUEST_SENDER.delete(user.id(), ADMIN_USER.userName(), ADMIN_USER.password());
        }

        for (final Team team : TeamUtils.getAll()) {
            TEAM_REQUEST_SENDER.delete(team.id(), ADMIN_USER.userName(), ADMIN_USER.password());
        }

        for (final Hardware hardware : HardwareUtils.getAll()) {
            HARDWARE_REQUEST_SENDER.delete(hardware.id(), ADMIN_USER.userName(), ADMIN_USER.password());
        }

        DatabaseUtils.truncateTableAndResetId("hardware", "users", "teams");
    }

    private static void removeCaptaincyFromUser(final User userWithPasskey) throws FoldingRestException {
        final UserRequest userNoLongerCaptain = new UserRequest(
            userWithPasskey.foldingUserName(),
            userWithPasskey.displayName(),
            userWithPasskey.passkey(),
            userWithPasskey.category().toString(),
            userWithPasskey.profileLink(),
            userWithPasskey.liveStatsLink(),
            userWithPasskey.hardware().id(),
            userWithPasskey.team().id(),
            false
        );

        USER_REQUEST_SENDER.update(userWithPasskey.id(), userNoLongerCaptain, ADMIN_USER.userName(), ADMIN_USER.password());
    }

    /**
     * Utility function that cleans the system for complex {@code Team Competition} stats-based tests to be executed.
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
     *     <li>user_changes</li>
     * </ol>
     *
     * @throws FoldingRestException thrown if an error occurs during cleanup
     * @see #cleanSystemForSimpleTests()
     */
    public static void cleanSystemForComplexTests() throws FoldingRestException {
        StubbedFoldingEndpointUtils.deletePoints();
        StubbedFoldingEndpointUtils.deleteUnits();

        DatabaseUtils.truncateTable("monthly_results");
        DatabaseUtils.truncateTableAndResetId(
            "user_initial_stats",
            "user_offset_tc_stats",
            "user_tc_stats_hourly",
            "user_total_stats",
            "user_changes"
        );

        TeamCompetitionStatsUtils.manuallyResetStats();
        cleanSystemForSimpleTests();
    }
}

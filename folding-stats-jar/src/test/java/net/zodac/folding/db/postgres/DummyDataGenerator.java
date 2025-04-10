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

package net.zodac.folding.db.postgres;

import net.zodac.folding.api.tc.Hardware;
import net.zodac.folding.api.tc.Team;
import net.zodac.folding.api.tc.User;

/**
 * Utility class to generate IDs/names for tests.
 */
final class DummyDataGenerator {

    private static int hardwareCount = 1;
    private static int userCount = 1;
    private static int teamCount = 1;

    private DummyDataGenerator() {

    }

    /**
     * Gets the next {@link Hardware} name.
     *
     * @return next name for {@link Hardware}
     */
    static String nextHardwareName() {
        return "hardware_" + hardwareCount++;
    }

    /**
     * Gets the next {@link User} name.
     *
     * @return next name for {@link User}
     */
    static String nextUserName() {
        return "user_" + userCount++;
    }

    /**
     * Gets the next {@link Team} name.
     *
     * @return next name for {@link Team}
     */
    static String nextTeamName() {
        return "team_" + teamCount++;
    }
}

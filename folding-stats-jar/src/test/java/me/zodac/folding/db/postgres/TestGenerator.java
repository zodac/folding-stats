/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2022 zodac.me
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

package me.zodac.folding.db.postgres;

/**
 * Utility class to generate IDs/names for tests.
 */
final class TestGenerator {

    private static int hardwareCount = 1;
    private static int userCount = 1;
    private static int teamCount = 1;

    private TestGenerator() {

    }

    /**
     * Gets the next {@link me.zodac.folding.api.tc.Hardware} name.
     *
     * @return next name for {@link me.zodac.folding.api.tc.Hardware}
     */
    static String nextHardwareName() {
        return "hardware_" + hardwareCount++;
    }

    /**
     * Gets the next {@link me.zodac.folding.api.tc.User} name.
     *
     * @return next name for {@link me.zodac.folding.api.tc.User}
     */
    static String nextUserName() {
        return "user_" + userCount++;
    }

    /**
     * Gets the next {@link me.zodac.folding.api.tc.Team} name.
     *
     * @return next name for {@link me.zodac.folding.api.tc.Team}
     */
    static String nextTeamName() {
        return "team_" + teamCount++;
    }
}

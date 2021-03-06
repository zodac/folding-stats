/*
 * MIT License
 *
 * Copyright (c) 2021-2022 zodac.me
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

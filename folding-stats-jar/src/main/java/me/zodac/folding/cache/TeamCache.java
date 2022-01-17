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

package me.zodac.folding.cache;

import me.zodac.folding.api.tc.Team;

/**
 * Implementation of {@link BaseCache} for {@link Team}s.
 *
 * <p>
 * <b>key:</b> {@link Team} ID
 *
 * <p>
 * <b>value:</b> {@link Team}
 */
public final class TeamCache extends BaseCache<Team> {

    private static final TeamCache INSTANCE = new TeamCache();

    private TeamCache() {
        super();
    }

    /**
     * Returns a singleton instance of {@link TeamCache}.
     *
     * @return the {@link TeamCache}
     */
    public static TeamCache getInstance() {
        return INSTANCE;
    }
}

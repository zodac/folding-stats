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
 *
 */

package me.zodac.folding.rest.api.tc.user;

import java.util.Collection;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.tc.stats.Stats;
import org.springframework.stereotype.Service;

/**
 * Class that parses {@link Stats} for <code>Team Competition</code> {@link User}s.
 */
@Service
public interface UserStatsParserService {

    /**
     * Parses the latest TC stats for the given {@link User}s.
     *
     * <p>
     * Method blocks until the stats have been parsed, calculated and persisted.
     *
     * @param users the {@link User}s whose TC stats are to be parsed
     */
    void parseTcStatsForUserAndWait(final Collection<User> users);

    /**
     * Parses the latest TC stats for the given {@link User}s.
     *
     * @param users the {@link User}s whose TC stats are to be parsed
     */
    void parseTcStatsForUser(final Collection<User> users);
}
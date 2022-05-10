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

package me.zodac.folding.test.ui.util;

import java.util.Collection;
import java.util.stream.Stream;

/**
 * Enum defining the frontend UI's URL links.
 */
public enum FrontendLink {

    /**
     * The URL for the administrator page of the frontend UI.
     */
    ADMIN_URL("http://frontend_dev/admin"),

    /**
     * The URL for the RAML API of the frontend UI.
     */
    API_URL("http://frontend_dev/api.html"),

    /**
     * The URL for the main page of the frontend UI.
     */
    INDEX_URL("http://frontend_dev/"),

    /**
     * The URL for the "Request A Change" page of the frontend UI.
     */
    REQUESTS_URL("http://frontend_dev/requests"),

    /**
     * The URL for the past results page of the frontend UI.
     */
    RESULTS_URL("http://frontend_dev/results"),

    /**
     * The URL for the hourly team page of the frontend UI.
     */
    TEAM_HOURLY_URL("http://frontend_dev/historic_team_hourly"),

    /**
     * The URL for the daily team page of the frontend UI.
     */
    TEAM_DAILY_URL("http://frontend_dev/historic_team_daily"),

    /**
     * The URL for the monthly team page of the frontend UI.
     */
    TEAM_MONTHLY_URL("http://frontend_dev/historic_team_monthly"),

    /**
     * The URL for the hourly user page of the frontend UI.
     */
    USER_HOURLY_URL("http://frontend_dev/historic_user_hourly"),

    /**
     * The URL for the daily user page of the frontend UI.
     */
    USER_DAILY_URL("http://frontend_dev/historic_user_daily"),

    /**
     * The URL for the monthly user page of the frontend UI.
     */
    USER_MONTHLY_URL("http://frontend_dev/historic_user_monthly");

    private static final Collection<FrontendLink> ALL_VALUES = Stream.of(values())
        .toList();

    private final String url;

    FrontendLink(final String url) {
        this.url = url;
    }

    /**
     * Retrieve all available {@link FrontendLink}s.
     *
     * <p>
     * Should be used instead of {@link FrontendLink#values()}, as that recalculates the array for each call,
     * while this method uses a static {@link Collection}.
     *
     * @return a {@link Collection} of all {@link FrontendLink}s
     */
    public static Collection<FrontendLink> getAllValues() {
        return ALL_VALUES;
    }

    /**
     * The frontend URL.
     *
     * @return the URL
     */
    public String getUrl() {
        return url;
    }
}

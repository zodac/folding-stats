/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2024 zodac.me
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
    ADMIN_URL("https://frontend_dev:81/admin"),

    /**
     * The URL for the main page of the frontend UI.
     */
    INDEX_URL("https://frontend_dev:81/"),

    /**
     * The URL for the "Request A Change" page of the frontend UI.
     */
    REQUESTS_URL("https://frontend_dev:81/requests"),

    /**
     * The URL for the past results page of the frontend UI.
     */
    RESULTS_URL("https://frontend_dev:81/results"),

    /**
     * The URL for the hourly team page of the frontend UI.
     */
    TEAM_HOURLY_URL("https://frontend_dev:81/historic_team_hourly"),

    /**
     * The URL for the daily team page of the frontend UI.
     */
    TEAM_DAILY_URL("https://frontend_dev:81/historic_team_daily"),

    /**
     * The URL for the monthly team page of the frontend UI.
     */
    TEAM_MONTHLY_URL("https://frontend_dev:81/historic_team_monthly"),

    /**
     * The URL for the hourly user page of the frontend UI.
     */
    USER_HOURLY_URL("https://frontend_dev:81/historic_user_hourly"),

    /**
     * The URL for the daily user page of the frontend UI.
     */
    USER_DAILY_URL("https://frontend_dev:81/historic_user_daily"),

    /**
     * The URL for the monthly user page of the frontend UI.
     */
    USER_MONTHLY_URL("https://frontend_dev:81/historic_user_monthly");

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
    public String url() {
        return url;
    }
}

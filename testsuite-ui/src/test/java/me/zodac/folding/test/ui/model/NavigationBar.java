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

package me.zodac.folding.test.ui.model;

import java.util.Map;
import org.openqa.selenium.By;

/**
 * Constants for the navigation bar on the UI.
 */
public final class NavigationBar { // NOPMD: DataClass - Happy to break encapsulation here

    // Expected values
    public static final String EXPECTED_NAVIGATION_BAR_TITLE = "Extreme Team Folding";
    public static final String EXPECTED_ACTIVE_LINK_CLASS = "active";
    public static final int EXPECTED_NUMBER_OF_LINKS = 6;

    // Elements
    public static final String NAVIGATION_BAR_ID_VALUE = "navbar";
    public static final By NAVIGATION_BAR = By.id(NAVIGATION_BAR_ID_VALUE);

    public static final String FOOTER_CONTENT_ID_VALUE = "footer-content";
    public static final By FOOTER = By.id(FOOTER_CONTENT_ID_VALUE);

    public static final String TITLE_ID_VALUE = "navbar-title";
    public static final By TITLE = By.id(TITLE_ID_VALUE);

    public static final String LINKS_PARENT_ID_VALUE = "navbar-links-parent";
    public static final By LINKS_PARENT = By.id(LINKS_PARENT_ID_VALUE);

    public static final String LINKS_CLASS_VALUE = "nav-item";
    public static final By LINKS = By.className(LINKS_CLASS_VALUE);

    public static final Map<Integer, String> EXPECTED_TAB_NAME_BY_INDEX = Map.of(
        0, "ETF Stats",
        1, "Past Results",
        2, "User Stats",
        3, "Team Stats",
        4, "System",
        5, "API"
    );

    private NavigationBar() {

    }
}

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

package me.zodac.folding.test.ui.model;

import java.util.Map;
import org.openqa.selenium.By;

/**
 * Constants for the navigation bar on the UI.
 */
public final class NavigationBar {

    private NavigationBar() {

    }

    // Expected values
    public static final String EXPECTED_NAVIGATION_BAR_TITLE = "Extreme Team Folding";
    public static final String EXPECTED_ACTIVE_LINK_CLASS = "active";
    public static final int EXPECTED_NUMBER_OF_LINKS = 6;

    // Elements
    public static final String NAVIGATION_BAR_ID_VALUE = "navbar";
    public static final By NAVIGATION_BAR = By.id(NAVIGATION_BAR_ID_VALUE);


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
}

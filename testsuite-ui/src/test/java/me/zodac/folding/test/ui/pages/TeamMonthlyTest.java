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

package me.zodac.folding.test.ui.pages;

import static me.zodac.folding.test.ui.util.Logger.log;
import static me.zodac.folding.test.ui.util.TestExecutor.executeWithDriver;
import static org.assertj.core.api.Assertions.assertThat;

import java.net.MalformedURLException;
import java.util.List;
import me.zodac.folding.test.ui.model.Attribute;
import me.zodac.folding.test.ui.model.NavigationBar;
import me.zodac.folding.test.ui.model.Tag;
import me.zodac.folding.test.ui.util.BrowserType;
import me.zodac.folding.test.ui.util.FrontendLink;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.openqa.selenium.WebElement;

/**
 * Verifies the UI contents of the {@link FrontendLink#TEAM_MONTHLY_URL} page.
 */
class TeamMonthlyTest {

    private static final int PAGE_INDEX_IN_NAVIGATION_BAR = 3;
    private static final FrontendLink FRONTEND_LINK = FrontendLink.TEAM_MONTHLY_URL;

    @ParameterizedTest
    @EnumSource(BrowserType.class)
    void loadAdminPage(final BrowserType browserType) throws MalformedURLException {
        log("Loading '%s' browser at '%s'", browserType.displayName(), FRONTEND_LINK.url());

        executeWithDriver(browserType, driver -> {
            driver.navigate().to(FRONTEND_LINK.url());

            // Checking active navigation link, and other links are inactive
            final WebElement navigationBar = driver.findElement(NavigationBar.NAVIGATION_BAR);
            final WebElement navigationBarLinksParent = navigationBar.findElement(NavigationBar.LINKS_PARENT);
            final List<WebElement> navigationBarLinks = navigationBarLinksParent.findElements(NavigationBar.LINKS);

            for (int i = 0; i < NavigationBar.EXPECTED_NUMBER_OF_LINKS; i++) {
                final WebElement navigationBarLink = navigationBarLinks.get(i);

                // Child <a> element will have a class 'active' to indicate the link is the active one
                // Only the expected link should have this class
                final WebElement navigationBarLinkChild = navigationBarLink.findElement(Tag.A);

                if (i == PAGE_INDEX_IN_NAVIGATION_BAR) {
                    assertThat(navigationBarLinkChild.getAttribute(Attribute.CLASS))
                        .as(String.format("Expected '%s' link to be active at index %d", navigationBarLink.getText(), PAGE_INDEX_IN_NAVIGATION_BAR))
                        .contains(NavigationBar.EXPECTED_ACTIVE_LINK_CLASS);
                } else {
                    assertThat(navigationBarLinkChild.getAttribute(Attribute.CLASS))
                        .as(String.format("Did not expect '%s' link to be active at index %d", navigationBarLink.getText(), i))
                        .doesNotContain(NavigationBar.EXPECTED_ACTIVE_LINK_CLASS);
                }
            }
        });
    }
}

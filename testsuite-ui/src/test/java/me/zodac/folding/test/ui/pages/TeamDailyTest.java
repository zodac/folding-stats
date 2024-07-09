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

package me.zodac.folding.test.ui.pages;

import static me.zodac.folding.test.ui.util.Executor.executeWithDriver;
import static me.zodac.folding.test.ui.util.Logger.log;
import static org.assertj.core.api.Assertions.assertThat;

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
 * Verifies the UI contents of the {@link FrontendLink#TEAM_DAILY_URL} page.
 */
class TeamDailyTest {

    private static final int PAGE_INDEX_IN_NAVIGATION_BAR = 3;
    private static final FrontendLink FRONTEND_LINK = FrontendLink.TEAM_DAILY_URL;

    @ParameterizedTest
    @EnumSource(BrowserType.class)
    void loadTeamDailyPage(final BrowserType browserType) {
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
                        .as("Expected '%s' link to be active at index %d", navigationBarLink.getText(), PAGE_INDEX_IN_NAVIGATION_BAR)
                        .contains(NavigationBar.EXPECTED_ACTIVE_LINK_CLASS);
                } else {
                    assertThat(navigationBarLinkChild.getAttribute(Attribute.CLASS))
                        .as("Did not expect '%s' link to be active at index %d", navigationBarLink.getText(), i)
                        .doesNotContain(NavigationBar.EXPECTED_ACTIVE_LINK_CLASS);
                }
            }
        });
    }
}

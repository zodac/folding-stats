/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2025 zodac.net
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

package net.zodac.folding.test.ui.pages;

import static net.zodac.folding.test.ui.util.Executor.executeWithDriver;
import static net.zodac.folding.test.ui.util.Logger.log;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.List;
import net.zodac.folding.test.ui.model.Attribute;
import net.zodac.folding.test.ui.model.NavigationBar;
import net.zodac.folding.test.ui.model.Tag;
import net.zodac.folding.test.ui.util.BrowserType;
import net.zodac.folding.test.ui.util.FrontendLink;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * Verifies the UI contents of the {@link FrontendLink#INDEX_URL} page.
 */
class IndexTest {

    private static final int PAGE_INDEX_IN_NAVIGATION_BAR = 0;
    private static final FrontendLink FRONTEND_LINK = FrontendLink.INDEX_URL;

    @ParameterizedTest
    @EnumSource(BrowserType.class)
    void testLoadIndexPage(final BrowserType browserType) {
        log("Loading '%s' browser at '%s'", browserType.displayName(), FRONTEND_LINK.url());

        executeWithDriver(browserType, driver -> {
            driver.navigate().to(FRONTEND_LINK.url());

            // Checking active navigation link, and other links are inactive
            final WebElement navigationBar = driver.findElement(NavigationBar.NAVIGATION_BAR);
            final WebElement navigationBarLinksParent = navigationBar.findElement(NavigationBar.LINKS_PARENT);
            final List<WebElement> navigationBarLinks = navigationBarLinksParent.findElements(NavigationBar.LINKS);

            assertThat(navigationBarLinks)
                .hasSize(NavigationBar.EXPECTED_NUMBER_OF_LINKS);

            for (int i = 0; i < NavigationBar.EXPECTED_NUMBER_OF_LINKS; i++) {
                final WebElement navigationBarLink = navigationBarLinks.get(i);

                // Child <a> element will have a class 'active' to indicate the link is the active one
                // Only the expected link should have this class
                final WebElement navigationBarLinkChild = navigationBarLink.findElement(Tag.A);

                if (i == PAGE_INDEX_IN_NAVIGATION_BAR) {
                    assertThat(navigationBarLinkChild.getDomAttribute(Attribute.CLASS))
                        .as("Expected '%s' link to be active at index %d", navigationBarLink.getText(), PAGE_INDEX_IN_NAVIGATION_BAR)
                        .contains(NavigationBar.EXPECTED_ACTIVE_LINK_CLASS);
                } else {
                    assertThat(navigationBarLinkChild.getDomAttribute(Attribute.CLASS))
                        .as("Did not expect '%s' link to be active at index %d", navigationBarLink.getText(), i)
                        .doesNotContain(NavigationBar.EXPECTED_ACTIVE_LINK_CLASS);
                }
            }

            // Verifying viewing tabs exist and the leaderboard is loaded by default
            final By viewingButtonDivBy = By.id("view_buttons");
            final WebElement viewingButtonsDiv = driver.findElement(viewingButtonDivBy);

            final String buttonsTagValue = "button";
            final By buttons = By.tagName(buttonsTagValue);
            final List<WebElement> viewingButtons = viewingButtonsDiv.findElements(buttons);

            assertThat(viewingButtons)
                .hasSize(4);

            final WebElement viewingButton1 = viewingButtons.getFirst();
            assertThat(viewingButton1.getText())
                .isEqualTo("Summary");
            assertThat(viewingButton1.getDomAttribute(Attribute.CLASS))
                .contains("btn-primary")
                .doesNotContain("btn-success");

            // Active button
            final WebElement viewingButton2 = viewingButtons.get(1);
            assertThat(viewingButton2.getText())
                .isEqualTo("Leaderboard");
            assertThat(viewingButton2.getDomAttribute(Attribute.CLASS))
                .contains("btn-success")
                .doesNotContain("btn-primary");

            final WebElement viewingButton3 = viewingButtons.get(2);
            assertThat(viewingButton3.getText())
                .isEqualTo("Team Stats");
            assertThat(viewingButton3.getDomAttribute(Attribute.CLASS))
                .contains("btn-primary")
                .doesNotContain("btn-success");

            final WebElement viewingButton4 = viewingButtons.get(3);
            assertThat(viewingButton4.getText())
                .isEqualTo("Category Leaderboard");
            assertThat(viewingButton4.getDomAttribute(Attribute.CLASS))
                .contains("btn-primary")
                .doesNotContain("btn-success");

            // Verifying content divs exist and only leaderboard is shown
            final By mainDivBy = By.id("main_parent");
            final WebElement mainParent = driver.findElement(mainDivBy);

            final String divTagValue = "div";
            final By divs = By.tagName(divTagValue);
            final List<WebElement> mainDivs = mainParent.findElements(divs);

            assertThat(mainDivs)
                .hasSize(4);

            final WebElement mainDiv1 = mainDivs.getFirst();
            assertThat(mainDiv1.getDomAttribute(Attribute.ID))
                .isEqualTo("summary_div");
            assertThat(mainDiv1.getDomAttribute(Attribute.CLASS))
                .contains("collapse")
                .doesNotContain("show");

            // Active div
            final WebElement mainDiv2 = mainDivs.get(1);
            assertThat(mainDiv2.getDomAttribute(Attribute.ID))
                .isEqualTo("leaderboard_div");
            assertThat(mainDiv2.getDomAttribute(Attribute.CLASS))
                .contains(
                    "collapse",
                    "show"
                );

            final WebElement mainDiv3 = mainDivs.get(2);
            assertThat(mainDiv3.getDomAttribute(Attribute.ID))
                .isEqualTo("stats_div");
            assertThat(mainDiv3.getDomAttribute(Attribute.CLASS))
                .contains("collapse")
                .doesNotContain("show");

            final WebElement mainDiv4 = mainDivs.get(3);
            assertThat(mainDiv4.getDomAttribute(Attribute.ID))
                .isEqualTo("category_div");
            assertThat(mainDiv4.getDomAttribute(Attribute.CLASS))
                .contains("collapse")
                .doesNotContain("show");
        });
    }

    @ParameterizedTest
    @EnumSource(BrowserType.class)
    void testViewingButtons(final BrowserType browserType) {
        log("Loading '%s' browser at '%s'", browserType.displayName(), FRONTEND_LINK.url());

        executeWithDriver(browserType, driver -> {
            driver.navigate().to(FRONTEND_LINK.url());

            final By viewingButtonDivBy = By.id("view_buttons");
            final WebElement viewingButtonsDiv = driver.findElement(viewingButtonDivBy);

            final String buttonsTagValue = "button";
            final By buttons = By.tagName(buttonsTagValue);
            final List<WebElement> viewingButtons = viewingButtonsDiv.findElements(buttons);

            final By mainDivBy = By.id("main_parent");
            final WebElement mainParent = driver.findElement(mainDivBy);

            final String divTagValue = "div";
            final By divs = By.tagName(divTagValue);
            final List<WebElement> mainDivs = mainParent.findElements(divs);

            // Default state - off/on/off/off
            assertThat(viewingButtons.getFirst().getDomAttribute(Attribute.CLASS))
                .contains("btn-primary")
                .doesNotContain("btn-success");
            assertThat(mainDivs.getFirst().getDomAttribute(Attribute.CLASS))
                .contains("collapse")
                .doesNotContain("show");
            assertThat(viewingButtons.get(1).getDomAttribute(Attribute.CLASS))
                .contains("btn-success")
                .doesNotContain("btn-primary");
            assertThat(mainDivs.get(1).getDomAttribute(Attribute.CLASS))
                .contains(
                    "collapse",
                    "show"
                );
            assertThat(viewingButtons.get(2).getDomAttribute(Attribute.CLASS))
                .contains("btn-primary")
                .doesNotContain("btn-success");
            assertThat(mainDivs.get(2).getDomAttribute(Attribute.CLASS))
                .contains("collapse")
                .doesNotContain("show");
            assertThat(viewingButtons.get(3).getDomAttribute(Attribute.CLASS))
                .contains("btn-primary")
                .doesNotContain("btn-success");
            assertThat(mainDivs.get(3).getDomAttribute(Attribute.CLASS))
                .contains("collapse")
                .doesNotContain("show");

            // Click 2nd button to disable it and close its div
            viewingButtons.get(1).click();
            try {
                Thread.sleep(Duration.ofSeconds(1L));
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // New state - off/off/off/off
            assertThat(viewingButtons.getFirst().getDomAttribute(Attribute.CLASS))
                .contains("btn-primary")
                .doesNotContain("btn-success");
            assertThat(mainDivs.getFirst().getDomAttribute(Attribute.CLASS))
                .contains("collapse")
                .doesNotContain("show");
            assertThat(viewingButtons.get(1).getDomAttribute(Attribute.CLASS))
                .contains("btn-primary")
                .doesNotContain("btn-success");
            assertThat(mainDivs.get(1).getDomAttribute(Attribute.CLASS))
                .contains("collapse")
                .doesNotContain("show");
            assertThat(viewingButtons.get(2).getDomAttribute(Attribute.CLASS))
                .contains("btn-primary")
                .doesNotContain("btn-success");
            assertThat(mainDivs.get(2).getDomAttribute(Attribute.CLASS))
                .contains("collapse")
                .doesNotContain("show");
            assertThat(viewingButtons.get(3).getDomAttribute(Attribute.CLASS))
                .contains("btn-primary")
                .doesNotContain("btn-success");
            assertThat(mainDivs.get(3).getDomAttribute(Attribute.CLASS))
                .contains("collapse")
                .doesNotContain("show");

            // Click 1st button to enable it and open its div
            viewingButtons.getFirst().click();
            try {
                Thread.sleep(Duration.ofSeconds(1L));
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // New state - on/off/off/off
            assertThat(viewingButtons.getFirst().getDomAttribute(Attribute.CLASS))
                .contains("btn-success")
                .doesNotContain("btn-primary");
            assertThat(mainDivs.getFirst().getDomAttribute(Attribute.CLASS))
                .contains(
                    "collapse",
                    "show"
                );
            assertThat(viewingButtons.get(1).getDomAttribute(Attribute.CLASS))
                .contains("btn-primary")
                .doesNotContain("btn-success");
            assertThat(mainDivs.get(1).getDomAttribute(Attribute.CLASS))
                .contains("collapse")
                .doesNotContain("show");
            assertThat(viewingButtons.get(2).getDomAttribute(Attribute.CLASS))
                .contains("btn-primary")
                .doesNotContain("btn-success");
            assertThat(mainDivs.get(2).getDomAttribute(Attribute.CLASS))
                .contains("collapse")
                .doesNotContain("show");
            assertThat(viewingButtons.get(3).getDomAttribute(Attribute.CLASS))
                .contains("btn-primary")
                .doesNotContain("btn-success");
            assertThat(mainDivs.get(3).getDomAttribute(Attribute.CLASS))
                .contains("collapse")
                .doesNotContain("show");

            // Click 1st button to disable it and close its div
            viewingButtons.getFirst().click();
            try {
                Thread.sleep(Duration.ofSeconds(1L));
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // New state - off/off/off/off
            assertThat(viewingButtons.getFirst().getDomAttribute(Attribute.CLASS))
                .contains("btn-primary")
                .doesNotContain("btn-success");
            assertThat(mainDivs.getFirst().getDomAttribute(Attribute.CLASS))
                .contains("collapse")
                .doesNotContain("show");
            assertThat(viewingButtons.get(1).getDomAttribute(Attribute.CLASS))
                .contains("btn-primary")
                .doesNotContain("btn-success");
            assertThat(mainDivs.get(1).getDomAttribute(Attribute.CLASS))
                .contains("collapse")
                .doesNotContain("show");
            assertThat(viewingButtons.get(2).getDomAttribute(Attribute.CLASS))
                .contains("btn-primary")
                .doesNotContain("btn-success");
            assertThat(mainDivs.get(2).getDomAttribute(Attribute.CLASS))
                .contains("collapse")
                .doesNotContain("show");
            assertThat(viewingButtons.get(3).getDomAttribute(Attribute.CLASS))
                .contains("btn-primary")
                .doesNotContain("btn-success");
            assertThat(mainDivs.get(3).getDomAttribute(Attribute.CLASS))
                .contains("collapse")
                .doesNotContain("show");

            // Click 3rd button to enable it and open its div
            viewingButtons.get(2).click();
            try {
                Thread.sleep(Duration.ofSeconds(1L));
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // New state - off/off/on/off
            assertThat(viewingButtons.getFirst().getDomAttribute(Attribute.CLASS))
                .contains("btn-primary")
                .doesNotContain("btn-success");
            assertThat(mainDivs.getFirst().getDomAttribute(Attribute.CLASS))
                .contains("collapse")
                .doesNotContain("show");
            assertThat(viewingButtons.get(1).getDomAttribute(Attribute.CLASS))
                .contains("btn-primary")
                .doesNotContain("btn-success");
            assertThat(mainDivs.get(1).getDomAttribute(Attribute.CLASS))
                .contains("collapse")
                .doesNotContain("show");
            assertThat(viewingButtons.get(2).getDomAttribute(Attribute.CLASS))
                .contains("btn-success")
                .doesNotContain("btn-primary");
            assertThat(mainDivs.get(2).getDomAttribute(Attribute.CLASS))
                .contains(
                    "collapse",
                    "show"
                );
            assertThat(viewingButtons.get(3).getDomAttribute(Attribute.CLASS))
                .contains("btn-primary")
                .doesNotContain("btn-success");
            assertThat(mainDivs.get(3).getDomAttribute(Attribute.CLASS))
                .contains("collapse")
                .doesNotContain("show");

            // Click 3rd button to disable it and close its div
            viewingButtons.get(2).click();
            try {
                Thread.sleep(Duration.ofSeconds(1L));
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // New state - off/off/off/off
            assertThat(viewingButtons.getFirst().getDomAttribute(Attribute.CLASS))
                .contains("btn-primary")
                .doesNotContain("btn-success");
            assertThat(mainDivs.getFirst().getDomAttribute(Attribute.CLASS))
                .contains("collapse")
                .doesNotContain("show");
            assertThat(viewingButtons.get(1).getDomAttribute(Attribute.CLASS))
                .contains("btn-primary")
                .doesNotContain("btn-success");
            assertThat(mainDivs.get(1).getDomAttribute(Attribute.CLASS))
                .contains("collapse")
                .doesNotContain("show");
            assertThat(viewingButtons.get(2).getDomAttribute(Attribute.CLASS))
                .contains("btn-primary")
                .doesNotContain("btn-success");
            assertThat(mainDivs.get(2).getDomAttribute(Attribute.CLASS))
                .contains("collapse")
                .doesNotContain("show");
            assertThat(viewingButtons.get(3).getDomAttribute(Attribute.CLASS))
                .contains("btn-primary")
                .doesNotContain("btn-success");
            assertThat(mainDivs.get(3).getDomAttribute(Attribute.CLASS))
                .contains("collapse")
                .doesNotContain("show");

            // Click 4th button to enable it and open its div
            viewingButtons.get(3).click();
            try {
                Thread.sleep(Duration.ofSeconds(1L));
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // New state - off/off/off/on
            assertThat(viewingButtons.getFirst().getDomAttribute(Attribute.CLASS))
                .contains("btn-primary")
                .doesNotContain("btn-success");
            assertThat(mainDivs.getFirst().getDomAttribute(Attribute.CLASS))
                .contains("collapse")
                .doesNotContain("show");
            assertThat(viewingButtons.get(1).getDomAttribute(Attribute.CLASS))
                .contains("btn-primary")
                .doesNotContain("btn-success");
            assertThat(mainDivs.get(1).getDomAttribute(Attribute.CLASS))
                .contains("collapse")
                .doesNotContain("show");
            assertThat(viewingButtons.get(2).getDomAttribute(Attribute.CLASS))
                .contains("btn-primary")
                .doesNotContain("btn-success");
            assertThat(mainDivs.get(2).getDomAttribute(Attribute.CLASS))
                .contains("collapse")
                .doesNotContain("show");
            assertThat(viewingButtons.get(3).getDomAttribute(Attribute.CLASS))
                .contains("btn-success")
                .doesNotContain("btn-primary");
            assertThat(mainDivs.get(3).getDomAttribute(Attribute.CLASS))
                .contains(
                    "collapse",
                    "show"
                );

            // Click 4th button to disable it and close its div
            viewingButtons.get(3).click();
            try {
                Thread.sleep(Duration.ofSeconds(1L));
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // New state - off/off/off/off
            assertThat(viewingButtons.getFirst().getDomAttribute(Attribute.CLASS))
                .contains("btn-primary")
                .doesNotContain("btn-success");
            assertThat(mainDivs.getFirst().getDomAttribute(Attribute.CLASS))
                .contains("collapse")
                .doesNotContain("show");
            assertThat(viewingButtons.get(1).getDomAttribute(Attribute.CLASS))
                .contains("btn-primary")
                .doesNotContain("btn-success");
            assertThat(mainDivs.get(1).getDomAttribute(Attribute.CLASS))
                .contains("collapse")
                .doesNotContain("show");
            assertThat(viewingButtons.get(2).getDomAttribute(Attribute.CLASS))
                .contains("btn-primary")
                .doesNotContain("btn-success");
            assertThat(mainDivs.get(2).getDomAttribute(Attribute.CLASS))
                .contains("collapse")
                .doesNotContain("show");
            assertThat(viewingButtons.get(3).getDomAttribute(Attribute.CLASS))
                .contains("btn-primary")
                .doesNotContain("btn-success");
            assertThat(mainDivs.get(3).getDomAttribute(Attribute.CLASS))
                .contains("collapse")
                .doesNotContain("show");

            // Click 2nd button to enable it and open its div
            viewingButtons.get(1).click();
            try {
                Thread.sleep(Duration.ofSeconds(1L));
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // Default state - off/on/off/off
            assertThat(viewingButtons.getFirst().getDomAttribute(Attribute.CLASS))
                .contains("btn-primary")
                .doesNotContain("btn-success");
            assertThat(mainDivs.getFirst().getDomAttribute(Attribute.CLASS))
                .contains("collapse")
                .doesNotContain("show");
            assertThat(viewingButtons.get(1).getDomAttribute(Attribute.CLASS))
                .contains("btn-success")
                .doesNotContain("btn-primary");
            assertThat(mainDivs.get(1).getDomAttribute(Attribute.CLASS))
                .contains(
                    "collapse",
                    "show"
                );
            assertThat(viewingButtons.get(2).getDomAttribute(Attribute.CLASS))
                .contains("btn-primary")
                .doesNotContain("btn-success");
            assertThat(mainDivs.get(2).getDomAttribute(Attribute.CLASS))
                .contains("collapse")
                .doesNotContain("show");
            assertThat(viewingButtons.get(3).getDomAttribute(Attribute.CLASS))
                .contains("btn-primary")
                .doesNotContain("btn-success");
            assertThat(mainDivs.get(3).getDomAttribute(Attribute.CLASS))
                .contains("collapse")
                .doesNotContain("show");
        });
    }

    @ParameterizedTest
    @EnumSource(BrowserType.class)
    void testSummaryTable(final BrowserType browserType) {
        log("Loading '%s' browser at '%s', viewing summary table", browserType.displayName(), FRONTEND_LINK.url());

        executeWithDriver(browserType, driver -> {
            driver.navigate().to(FRONTEND_LINK.url());

            final WebElement summaryDiv = driver.findElement(By.id("summary_div"));
            assertThat(summaryDiv.getDomAttribute(Attribute.CLASS))
                .contains("collapse")
                .doesNotContain("show");
        });
    }

    @ParameterizedTest
    @EnumSource(BrowserType.class)
    void testLeaderboardTable(final BrowserType browserType) {
        log("Loading '%s' browser at '%s', viewing leaderboard table", browserType.displayName(), FRONTEND_LINK.url());

        executeWithDriver(browserType, driver -> {
            driver.navigate().to(FRONTEND_LINK.url());

            final WebElement leaderboardDiv = driver.findElement(By.id("leaderboard_div"));
            assertThat(leaderboardDiv.getDomAttribute(Attribute.CLASS))
                .contains(
                    "collapse",
                    "show"
                );
        });
    }

    @ParameterizedTest
    @EnumSource(BrowserType.class)
    void testTeamStatsTable(final BrowserType browserType) {
        log("Loading '%s' browser at '%s', viewing team stats table", browserType.displayName(), FRONTEND_LINK.url());

        executeWithDriver(browserType, driver -> {
            driver.navigate().to(FRONTEND_LINK.url());

            final WebElement teamStatsDiv = driver.findElement(By.id("stats_div"));
            assertThat(teamStatsDiv.getDomAttribute(Attribute.CLASS))
                .contains("collapse")
                .doesNotContain("show");
        });
    }

    @ParameterizedTest
    @EnumSource(BrowserType.class)
    void testCategoryTable(final BrowserType browserType) {
        log("Loading '%s' browser at '%s', viewing category table", browserType.displayName(), FRONTEND_LINK.url());

        executeWithDriver(browserType, driver -> {
            driver.navigate().to(FRONTEND_LINK.url());

            final WebElement categoryDiv = driver.findElement(By.id("category_div"));
            assertThat(categoryDiv.getDomAttribute(Attribute.CLASS))
                .contains("collapse")
                .doesNotContain("show");
        });
    }
}

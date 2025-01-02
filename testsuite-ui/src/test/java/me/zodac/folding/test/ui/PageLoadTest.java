/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2025 zodac.me
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

package me.zodac.folding.test.ui;

import static me.zodac.folding.test.ui.util.Executor.executeWithDriver;
import static me.zodac.folding.test.ui.util.Logger.log;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Stream;
import me.zodac.folding.test.ui.model.Attribute;
import me.zodac.folding.test.ui.model.NavigationBar;
import me.zodac.folding.test.ui.model.Tag;
import me.zodac.folding.test.ui.util.BrowserType;
import me.zodac.folding.test.ui.util.FrontendLink;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openqa.selenium.WebElement;

/**
 * Verifies that each page of the UI can be loaded.
 *
 * <p>
 * Does not do any in depth testing of the page (see related UI test), but checks the following:
 * <ul>
 *     <li>That the page can be loaded</li>
 *     <li>That the tab title is {@code "Extreme Team Folding"}</li>
 *     <li>That the navigation bar contains the correct link titles</li>
 * </ul>
 *
 * @see FrontendLink
 */
class PageLoadTest {

    private static final String EXPECTED_TAB_TITLE = "Extreme Team Folding";

    @ParameterizedTest
    @MethodSource("browserAndFrontendLinkProvider")
    void testLoadingAllPages(final BrowserType browserType, final FrontendLink frontendLink) {
        log("Loading '%s' browser at '%s'", browserType.displayName(), frontendLink.url());

        executeWithDriver(browserType, driver -> {
            driver.navigate().to(frontendLink.url());

            assertThat(driver.getTitle())
                .as("Unexpected tab title for URL '%s'", frontendLink.url())
                .isEqualTo(EXPECTED_TAB_TITLE);

            final WebElement navigationBar = driver.findElement(NavigationBar.NAVIGATION_BAR);

            // Check navigation bar title
            final WebElement navigationBarTitle = navigationBar.findElement(NavigationBar.TITLE);
            assertThat(navigationBarTitle.getText())
                .as("Unexpected navigation bar title for URL '%s'", frontendLink.url())
                .isEqualTo(NavigationBar.EXPECTED_NAVIGATION_BAR_TITLE);

            final WebElement navigationBarLinksParent = navigationBar.findElement(NavigationBar.LINKS_PARENT);
            final List<WebElement> navigationBarLinks = navigationBarLinksParent.findElements(NavigationBar.LINKS);

            assertThat(navigationBarLinks)
                .as("Expected the navigation bar to have 6 links for URL: %s", frontendLink.url())
                .hasSize(NavigationBar.EXPECTED_NUMBER_OF_LINKS);

            for (int i = 0; i < NavigationBar.EXPECTED_NUMBER_OF_LINKS; i++) {
                final WebElement navigationBarLink = navigationBarLinks.get(i);
                assertThat(navigationBarLink.findElement(Tag.A).getText())
                    .isEqualTo(NavigationBar.EXPECTED_TAB_NAME_BY_INDEX.get(i));
            }

            // Confirm footer and content
            final WebElement footer = driver.findElement(NavigationBar.FOOTER);
            assertThat(footer.getText())
                .contains("Folding@Home data provided by Folding@Home Statistics")
                .contains("Hardware PPD data provided by LARS PPD DB");

            final List<String> footerHrefAttributes = footer.findElements(Tag.A)
                .stream()
                .map(footerLink -> footerLink.getDomAttribute(Attribute.HREF))
                .toList();

            assertThat(footerHrefAttributes)
                .contains(
                    "https://stats.foldingathome.org/donor/",
                    "https://folding.lar.systems/",
                    "https://chrome.google.com/webstore/detail/folding-at-home-in-the-da/alpjkkbjnbkddolgnicglknicbgfahoe/"
                );
        });
    }

    private static Stream<Arguments> browserAndFrontendLinkProvider() {
        final Stream.Builder<Arguments> argumentBuilder = Stream.builder();
        for (final BrowserType browserType : BrowserType.getAllValues()) {
            for (final FrontendLink frontendLink : FrontendLink.getAllValues()) {
                argumentBuilder.add(Arguments.of(browserType, frontendLink));
            }
        }
        return argumentBuilder.build();
    }
}

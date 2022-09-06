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


package me.zodac.folding.test.ui;

import static me.zodac.folding.test.ui.util.Logger.log;
import static me.zodac.folding.test.ui.util.TestExecutor.executeWithDriver;
import static org.assertj.core.api.Assertions.assertThat;

import java.net.MalformedURLException;
import java.util.List;
import java.util.stream.Stream;
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
    void loadAllPages(final BrowserType browserType, final FrontendLink frontendLink) throws MalformedURLException {
        log("Loading '%s' browser at '%s'", browserType.displayName(), frontendLink.url());

        executeWithDriver(browserType, driver -> {
            driver.navigate().to(frontendLink.url());

            assertThat(driver.getTitle())
                .as(String.format("Unexpected tab title for URL '%s'", frontendLink.url()))
                .isEqualTo(EXPECTED_TAB_TITLE);

            final WebElement navigationBar = driver.findElement(NavigationBar.NAVIGATION_BAR);

            // Check navigation bar title
            final WebElement navigationBarTitle = navigationBar.findElement(NavigationBar.TITLE);
            assertThat(navigationBarTitle.getText())
                .as(String.format("Unexpected navigation bar title for URL '%s'", frontendLink.url()))
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

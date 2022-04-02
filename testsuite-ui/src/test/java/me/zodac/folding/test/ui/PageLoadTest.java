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

import static me.zodac.folding.test.util.Logger.log;
import static me.zodac.folding.test.util.Logger.logWithBlankLine;
import static me.zodac.folding.test.util.TestExecutor.executeWithDriver;
import static org.assertj.core.api.Assertions.assertThat;

import java.net.MalformedURLException;
import me.zodac.folding.test.util.BrowserType;
import me.zodac.folding.test.util.FrontendLink;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * Verifies that each page of the UI can be loaded.
 *
 * <p>
 * Does not do any in depth testing of the page (see related UI test), but checks the following:
 * <ul>
 *     <li>That the page can be loaded</li>
 *     <li>That the tab title is {@code "Extreme Team Folding"}</li>
 * </ul>
 *
 * @see FrontendLink
 */
class PageLoadTest {

    private static final String EXPECTED_TAB_TITLE = "Extreme Team Folding";

    @ParameterizedTest
    @EnumSource(BrowserType.class)
    void loadPages(final BrowserType browserType) throws MalformedURLException {
        logWithBlankLine("Loading '%s' browser", browserType);

        executeWithDriver(browserType, driver -> {
            for (final FrontendLink frontendLink : FrontendLink.getAllValues()) {
                log("Visiting '%s'", frontendLink.getUrl());
                driver.navigate().to(frontendLink.getUrl());

                assertThat(driver.getTitle())
                    .isEqualTo(EXPECTED_TAB_TITLE);
            }
        });
    }
}

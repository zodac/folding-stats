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
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

/**
 * Supported browsers to be tested.
 */
public enum BrowserType {

    /**
     * {@code Google Chrome} web browser.
     */
    CHROME("Google Chrome", RemoteWebDriverFactory.create("chromePort", "4444", new ChromeOptions())),

    /**
     * {@code Microsoft Edge} web browser.
     */
    EDGE("Microsoft Edge", RemoteWebDriverFactory.create("edgePort", "4445", new EdgeOptions())),

    /**
     * {@code Mozilla Firefox} web browser.
     */
    FIREFOX("Mozilla Firefox", RemoteWebDriverFactory.create("firefoxPort", "4446", new FirefoxOptions()));

    private static final Collection<BrowserType> ALL_VALUES = Stream.of(values())
        .toList();

    private final String displayName;
    private final RemoteWebDriver remoteWebDriver;

    BrowserType(final String displayName, final RemoteWebDriver remoteWebDriver) {
        this.displayName = displayName;
        this.remoteWebDriver = remoteWebDriver;
    }

    /**
     * Retrieve all available {@link BrowserType}s.
     *
     * <p>
     * Should be used instead of {@link BrowserType#values()}, as that recalculates the array for each call,
     * while this method uses a static {@link BrowserType}.
     *
     * @return a {@link Collection} of all {@link BrowserType}s
     */
    public static Collection<BrowserType> getAllValues() {
        return ALL_VALUES;
    }

    /**
     * The user-friendly display name for the {@link BrowserType}.
     *
     * @return the display name
     */
    public String displayName() {
        return displayName;
    }

    /**
     * Returns a {@link RemoteWebDriver} for UI testing.
     *
     * @return the {@link RemoteWebDriver}
     */
    public RemoteWebDriver remoteWebDriver() {
        return remoteWebDriver;
    }
}

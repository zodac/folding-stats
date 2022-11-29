/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2022 zodac.me
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

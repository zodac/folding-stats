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

import java.net.MalformedURLException;
import java.net.URL;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.AbstractDriverOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

/**
 * Factory class to create instances of {@link RemoteWebDriver}.
 */
final class RemoteWebDriverFactory {

    private static final String TEST_IP_ADDRESS = System.getProperty("testIpAddress", "127.0.0.1");
    private static final String WEB_DRIVER_URL_FORMAT = "http://%s:%s/wd/hub";

    private RemoteWebDriverFactory() {

    }

    /**
     * Creates an instance of {@link RemoteWebDriver}. Will use a blank instance of {@link ChromeOptions}.
     *
     * @param portPropertyName the name of the environment variable defining the selenium port
     * @param defaultPortValue the default port number if no environment variable is set
     * @param options          the default {@link AbstractDriverOptions}
     * @return the constructed {@link RemoteWebDriver}
     */
    static RemoteWebDriver create(final String portPropertyName, final String defaultPortValue, final AbstractDriverOptions<?> options) {
        try {
            final String port = System.getProperty(portPropertyName, defaultPortValue);
            final URL url = new URL(String.format(WEB_DRIVER_URL_FORMAT, TEST_IP_ADDRESS, port));
            return new RemoteWebDriver(url, options);
        } catch (final MalformedURLException e) {
            throw new AssertionError("Error initialising web driver", e);
        }
    }
}

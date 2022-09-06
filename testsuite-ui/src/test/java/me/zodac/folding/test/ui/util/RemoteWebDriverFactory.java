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

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

package me.zodac.folding.test.util;

import java.net.MalformedURLException;
import java.net.URL;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

/**
 * Supported browsers to be tested.
 */
public enum BrowserType {

    /**
     * {@code Google Chrome} web browser.
     */
    CHROME {
        @Override
        public RemoteWebDriver getDriver() throws MalformedURLException {
            final String port = System.getProperty("chromePort", "4444");
            final URL url = new URL(String.format(WEB_DRIVER_URL_FORMAT, TEST_IP_ADDRESS, port));

            return new RemoteWebDriver(url, new ChromeOptions());
        }
    },

    /**
     * {@code Mozilla Firefox} web browser.
     */
    FIREFOX {
        @Override
        public RemoteWebDriver getDriver() throws MalformedURLException {
            final String port = System.getProperty("firefoxPort", "4445");
            final URL url = new URL(String.format(WEB_DRIVER_URL_FORMAT, TEST_IP_ADDRESS, port));
            return new RemoteWebDriver(url, new FirefoxOptions());
        }
    };

    private static final String TEST_IP_ADDRESS = System.getProperty("testIpAddress", "127.0.0.1");
    private static final String WEB_DRIVER_URL_FORMAT = "http://%s:%s/wd/hub";

    /**
     * Abstract method to be implemented by {@link BrowserType} values, to return a {@link RemoteWebDriver} for UI testing.
     *
     * @return the {@link RemoteWebDriver}
     * @throws MalformedURLException thrown if the {@link URL} is invalid
     */
    public abstract RemoteWebDriver getDriver() throws MalformedURLException;
}

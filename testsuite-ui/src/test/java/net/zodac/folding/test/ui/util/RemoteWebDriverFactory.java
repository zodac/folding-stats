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

package net.zodac.folding.test.ui.util;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import org.openqa.selenium.remote.AbstractDriverOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

/**
 * Factory class to create instances of {@link RemoteWebDriver}.
 */
final class RemoteWebDriverFactory {

    private static final String TEST_IP_ADDRESS = System.getProperty("testIpAddress", "127.0.0.1"); // NOPMD: AvoidUsingHardCodedIP - Fine here
    private static final String WEB_DRIVER_URL_FORMAT = "http://%s:%s/wd/hub";

    private RemoteWebDriverFactory() {

    }

    /**
     * Creates an instance of {@link RemoteWebDriver}.
     *
     * @param browserType the {@link BrowserType} for which to create the {@link RemoteWebDriver}
     * @return the constructed {@link RemoteWebDriver}
     */
    static RemoteWebDriver create(final BrowserType browserType) {
        try {
            final AbstractDriverOptions<?> options = browserType.options();
            options.setAcceptInsecureCerts(true); // Needed as we use a self-signed certificate in the dev environment

            final URL url = URI.create(String.format(WEB_DRIVER_URL_FORMAT, TEST_IP_ADDRESS, browserType.portNumber())).toURL();
            return new RemoteWebDriver(url, options);
        } catch (final MalformedURLException e) {
            throw new AssertionError(String.format("Error initialising web driver for %s", browserType), e);
        }
    }
}

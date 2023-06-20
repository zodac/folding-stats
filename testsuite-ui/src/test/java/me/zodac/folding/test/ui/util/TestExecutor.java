/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2023 zodac.me
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

import java.util.function.Consumer;
import org.openqa.selenium.remote.RemoteWebDriver;

/**
 * Utility class used to help execute tests.
 */
public final class TestExecutor {

    private TestExecutor() {

    }

    /**
     * Executes a test with a {@link RemoteWebDriver}. Closes the {@link RemoteWebDriver} after the test is completed.
     *
     * @param browserType the {@link BrowserType} to execute the test on, which creates the {@link RemoteWebDriver}
     * @param test        the test to be executed, using the {@link RemoteWebDriver}
     */
    public static void executeWithDriver(final BrowserType browserType, final Consumer<? super RemoteWebDriver> test) {
        final RemoteWebDriver driver = RemoteWebDriverFactory.create(browserType);
        try {
            test.accept(driver);
        } finally {
            driver.quit();
        }
    }
}

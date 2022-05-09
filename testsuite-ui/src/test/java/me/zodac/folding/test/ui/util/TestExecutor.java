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
     * @throws MalformedURLException thrown if an error occurs creating the {@link RemoteWebDriver}
     */
    public static void executeWithDriver(final BrowserType browserType, final Consumer<? super RemoteWebDriver> test)
        throws MalformedURLException {

        final RemoteWebDriver driver = browserType.getDriver();
        try {
            test.accept(driver);
        } finally {
            driver.quit();
        }
    }
}

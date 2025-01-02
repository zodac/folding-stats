/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2025 zodac.me
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

package me.zodac.folding.test.ui.pages;

import static me.zodac.folding.test.ui.util.Executor.executeWithDriver;
import static me.zodac.folding.test.ui.util.Logger.log;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import me.zodac.folding.test.ui.util.BrowserType;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Verifies the UI contents of the hardware table on the new UI.
 */
class NewUiTest {

    @Disabled("Used as reference for new UI, which is not yet implemented")
    @Test
    void testNewIndex() {
        log("Loading '%s' browser at '%s'", BrowserType.CHROME.displayName(), "http://192.168.178.6:3000");

        executeWithDriver(BrowserType.CHROME, driver -> {
            driver.navigate().to("http://192.168.178.6:3000");

            final Wait<WebDriver> hardwareTableWait = new WebDriverWait(driver, Duration.ofSeconds(2L));
            hardwareTableWait.until(_ -> driver.findElement(By.id("hardwareTable")).isDisplayed());
            final WebElement hardwareTable = driver.findElement(By.id("hardwareTable"));
            assertThat(hardwareTable)
                .as("Expected hardware table was not found")
                .isNotNull();

            final Wait<WebDriver> hardwareTableContentsWait = new WebDriverWait(driver, Duration.ofSeconds(5L));
            hardwareTableContentsWait.until(_ -> hardwareTable.findElement(By.xpath("//tbody/tr[1]")).isDisplayed());
            assertThat(hardwareTable.findElements(By.tagName("tr")))
                .as("Unexpected number of hardware entries returned")
                .hasSize(358);
        });
    }
}

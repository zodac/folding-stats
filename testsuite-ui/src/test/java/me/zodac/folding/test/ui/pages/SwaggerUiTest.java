/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2024 zodac.me
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

import me.zodac.folding.test.ui.util.BrowserType;
import me.zodac.folding.test.ui.util.SwaggerLink;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.openqa.selenium.By;

/**
 * Verifies that each page of the Swagger UI can be loaded.
 *
 * @see SwaggerLink
 */
class SwaggerUiTest {

    @ParameterizedTest
    @EnumSource(BrowserType.class)
    void loadSwaggerDocsPage(final BrowserType browserType) {
        log("Loading '%s' browser at '%s'", browserType.displayName(), SwaggerLink.SWAGGER_DOCS_URL.url());

        executeWithDriver(browserType, driver -> {
            driver.navigate().to(SwaggerLink.SWAGGER_DOCS_URL.url());
            driver.findElement(By.xpath("//*[contains(text(),'OpenAPI definition')]"));
        });
    }

    @ParameterizedTest
    @EnumSource(BrowserType.class)
    void loadSwaggerDocsProjectPage(final BrowserType browserType) {
        log("Loading '%s' browser at '%s'", browserType.displayName(), SwaggerLink.SWAGGER_DOCS_PROJECT_URL.url());

        executeWithDriver(browserType, driver -> {
            driver.navigate().to(SwaggerLink.SWAGGER_DOCS_PROJECT_URL.url());
            driver.findElement(By.xpath("//*[contains(text(),\"REST API for the 'folding-stats' project\")]"));
        });
    }

    @ParameterizedTest
    @EnumSource(BrowserType.class)
    void loadSwaggerUiPage(final BrowserType browserType) {
        log("Loading '%s' browser at '%s'", browserType.displayName(), SwaggerLink.SWAGGER_UI_URL.url());

        executeWithDriver(browserType, driver -> {
            driver.navigate().to(SwaggerLink.SWAGGER_UI_URL.url());

            assertThat(driver.getTitle())
                .as("Unexpected tab title for URL '%s'", SwaggerLink.SWAGGER_UI_URL.url())
                .isEqualTo("Swagger UI");
        });
    }
}

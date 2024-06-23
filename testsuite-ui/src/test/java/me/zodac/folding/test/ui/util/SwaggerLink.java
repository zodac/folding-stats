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

package me.zodac.folding.test.ui.util;

/**
 * Enum defining the frontend API's Swagger UI URL links.
 */
public enum SwaggerLink {

    /**
     * The URL for the Swagger API JSON.
     */
    SWAGGER_DOCS_URL("http://backend-dev:8079/folding/api-docs"),

    /**
     * The URL for the Swagger API JSON for the {@code FoldingStats} project.
     */
    SWAGGER_DOCS_PROJECT_URL("http://backend-dev:8079/folding/api-docs/FoldingStats"),

    /**
     * The URL for the Swagger UI.
     */
    SWAGGER_UI_URL("http://backend-dev:8079/folding/swagger-ui/index.html");

    private final String url;

    SwaggerLink(final String url) {
        this.url = url;
    }

    /**
     * The frontend URL.
     *
     * @return the URL
     */
    public String url() {
        return url;
    }
}

/*
 * MIT License
 *
 * Copyright (c) 2021 zodac.me
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

package me.zodac.folding;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration;

/**
 * Base {@link FoldingStatsApplication}.
 *
 * <p>
 * This should be placed in the highest package possible, as the Spring application will scan all sub-packages for
 * {@link org.springframework.web.bind.annotation.RestController}s, {@link org.springframework.stereotype.Service}s,
 * {@link org.springframework.stereotype.Component}s, etc.
 *
 * <p>
 * The {@code jooq} transitive dependency {@link R2dbcAutoConfiguration} must also be disabled.
 */
@SpringBootApplication(exclude = {R2dbcAutoConfiguration.class})
public class FoldingStatsApplication { // NOPMD - SpringBootApplication must be non-final and have a public constructor

    /**
     * Main entry point to our Spring application.
     *
     * @param args arguments for the {@link FoldingStatsApplication}.
     */
    public static void main(final String[] args) {
        SpringApplication.run(FoldingStatsApplication.class, args);
    }
}
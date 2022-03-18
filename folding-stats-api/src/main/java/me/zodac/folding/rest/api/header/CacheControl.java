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

package me.zodac.folding.rest.api.header;

/**
 * Values for {@link RestHeader#CACHE_CONTROL} REST headers.
 */
public enum CacheControl {

    /**
     * The <code>no-cache</code> value.
     */
    NO_CACHE("no-cache"),

    /**
     * The <code>no-store</code> value.
     */
    NO_STORE("no-store");

    private final String cacheControlValue;

    /**
     * Constructs a {@link CacheControl} with the header value as a {@link String}.
     *
     * @param cacheControlValue the {@link CacheControl} value as a {@link String}
     */
    CacheControl(final String cacheControlValue) {
        this.cacheControlValue = cacheControlValue;
    }

    /**
     * The value of the {@link CacheControl}.
     *
     * @return the {@link CacheControl} value
     */
    public String cacheControlValue() {
        return cacheControlValue;
    }
}

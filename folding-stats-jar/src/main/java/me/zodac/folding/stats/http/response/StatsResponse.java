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

package me.zodac.folding.stats.http.response;

import java.net.http.HttpResponse;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Simple wrapper for a {@link String} to hold the status code and body for the {@link HttpResponse} for a
 * points or units REST request.
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class StatsResponse {

    private final int statusCode;
    private final String responseBody;

    /**
     * Creates a {@link StatsResponse} based on the provided {@link HttpResponse}.
     *
     * @param httpResponse the {@link HttpResponse}
     * @return the created {@link StatsResponse}
     */
    public static StatsResponse create(final HttpResponse<String> httpResponse) {
        return new StatsResponse(httpResponse.statusCode(), httpResponse.body());
    }
}

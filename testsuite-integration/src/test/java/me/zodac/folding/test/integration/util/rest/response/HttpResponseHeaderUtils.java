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

package me.zodac.folding.test.integration.util.rest.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.http.HttpHeaders;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import me.zodac.folding.rest.api.header.RestHeader;

/**
 * Utility class with convenience functions extract {@link HttpHeaders} from a {@link HttpResponse}.
 */
public final class HttpResponseHeaderUtils {

    private HttpResponseHeaderUtils() {

    }

    /**
     * Retrieves the value of the {@code ETag} header from the {@link HttpResponse}.
     *
     * @param response the {@link HttpResponse} from which to extract the {@link RestHeader#ETAG} header
     * @return the value of the {@code ETag} header
     */
    public static String getEntityTag(final HttpResponse<String> response) {
        return getHeader(response, RestHeader.ETAG.headerName());
    }

    /**
     * Retrieves the value of the {@code X-Total-Count} header from the {@link HttpResponse}.
     *
     * @param response the {@link HttpResponse} from which to extract the {@code X-Total-Count} header
     * @return the value of the {@code X-Total-Count} header
     */
    public static int getTotalCount(final HttpResponse<String> response) {
        final String headerValue = getHeader(response, RestHeader.TOTAL_COUNT.headerName());
        return Integer.parseInt(headerValue);
    }

    private static String getHeader(final HttpResponse<String> response, final String headerName) {
        final HttpHeaders headers = response.headers();
        final Map<String, List<String>> headersByName = headers.map();
        assertThat(headersByName)
            .containsKey(headerName);

        return headersByName.get(headerName).get(0);
    }
}

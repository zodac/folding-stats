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

        return headersByName.getOrDefault(headerName, List.of()).getFirst();
    }
}

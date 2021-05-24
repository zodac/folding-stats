package me.zodac.folding.test.utils.rest.response;

import java.net.http.HttpHeaders;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Utility class with convenience functions extract {@link HttpHeaders} from a {@link HttpResponse}.
 */
public final class HttpResponseHeaderUtils {

    private HttpResponseHeaderUtils() {

    }

    /**
     * Retrieves the value of the <code>ETag</code> header from the {@link HttpResponse}.
     *
     * @param response the {@link HttpResponse} from which to extract the <code>ETag</code> header
     * @return the value of the <code>ETag</code> header
     */
    public static String getETag(final HttpResponse<String> response) {
        return getHeader(response, "ETag");
    }

    /**
     * Retrieves the value of the <code>X-Total-Count</code> header from the {@link HttpResponse}.
     *
     * @param response the {@link HttpResponse} from which to extract the <code>X-Total-Count</code> header
     * @return the value of the <code>X-Total-Count</code> header
     */
    public static int getXTotalCount(final HttpResponse<String> response) {
        final String headerValue = getHeader(response, "X-Total-Count");
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

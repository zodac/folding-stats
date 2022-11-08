/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2022 zodac.me
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

package me.zodac.folding.test.framework;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.net.ssl.SSLSession;

/**
 * Wrapper class holding the context of a {@link TestCase}. An instance will be created at the start of a {@link TestCase}, and will be passed into
 * each of its {@link TestStep}s. This allows one {@link TestStep} to use the result or output of a previous one if needed.
 */
public class TestContext {

    private static final String HTTP_RESPONSE_KEY = "response";

    private final Map<String, Object> context = new HashMap<>();

    /**
     * Puts an {@link Object} in the {@link TestContext} for the specified {@link String} key.
     *
     * @param key   the {@link String} key
     * @param value the {@link Object} value
     */
    public void put(final String key, final Object value) {
        context.put(key, value);
    }

    /**
     * Retrives an {@link Object} from the {@link TestContext} for the specified {@link String} key.
     *
     * @param key the {@link String} key
     * @return the {@link Object} value
     */
    public Object get(final String key) {
        return context.get(key);
    }

    /**
     * Puts a {@link HttpResponse} in the {@link TestContext}.
     *
     * @param httpResponse the {@link HttpResponse}
     */
    public void putHttpResponse(final HttpResponse<String> httpResponse) {
        put(HTTP_RESPONSE_KEY, httpResponse);
    }

    /**
     * Retrieves the {@link HttpResponse} from the {@link TestContext}.
     *
     * @return the {@link HttpResponse}
     * @throws IllegalStateException if the stored value is an invalid type
     */
    public HttpResponse<String> getHttpResponse() {
        final Object response = get(HTTP_RESPONSE_KEY);

        if (response instanceof HttpResponse<?> r) {
            final Object body = r.body();
            if (body instanceof String) {
                return new StringHttpResponse(r);
            }

            throw new IllegalStateException("Expected 'responseBody' to be of type String, instead was: " + r.getClass().getSimpleName());
        }

        throw new IllegalStateException("Expected 'response' to be of type HttpResponse, instead was: " + response.getClass().getSimpleName());
    }

    /**
     * Checks whether the current {@link TestContext} has a saved {@link HttpResponse}.
     *
     * @return {@code true} if the {@link TestContext} has a saved {@link HttpResponse}
     */
    public boolean hasHttpResponse() {
        return context.containsKey(HTTP_RESPONSE_KEY);
    }

    /**
     * Clears the context.
     */
    public void clear() {
        context.clear();
    }

    /**
     * Since we lose the type of the {@link HttpResponse} when retrieving from the {@link TestContext}, we construct a new instance if we know that
     * the response body is a {@link String}.
     *
     * <p>
     * Note that method {@link HttpResponse#previousResponse()} will always return {@link Optional#empty()}.
     *
     * @param response the generic {@link HttpResponse} to be used to construct a {@link String} {@link HttpResponse}
     */
    private record StringHttpResponse(HttpResponse<?> response) implements HttpResponse<String> {

        @Override
        public int statusCode() {
            return response.statusCode();
        }

        @Override
        public HttpRequest request() {
            return response.request();
        }

        @Override
        public Optional<HttpResponse<String>> previousResponse() {
            return Optional.empty();
        }

        @Override
        public HttpHeaders headers() {
            return response.headers();
        }

        @Override
        public String body() {
            return String.valueOf(response.body());
        }

        @Override
        public Optional<SSLSession> sslSession() {
            return response.sslSession();
        }

        @Override
        public URI uri() {
            return response.uri();
        }

        @Override
        public HttpClient.Version version() {
            return response.version();
        }
    }
}

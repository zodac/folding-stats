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

    private static final String RESPONSE_KEY = "response";

    private final Map<String, Object> context = new HashMap<>();

    /**
     * Adds a {@link HttpResponse} to the {@link TestContext}.
     *
     * @param response the {@link HttpResponse}
     */
    public void addResponse(final HttpResponse<String> response) {
        context.put(RESPONSE_KEY, response);
    }

    /**
     * Retrieves the {@link HttpResponse} from the {@link TestContext}.
     *
     * @return the {@link HttpResponse}
     * @throws IllegalStateException if the stored value is an invalid type
     */
    public HttpResponse<String> getResponse() {
        final Object response = context.get(RESPONSE_KEY);

        if (response instanceof HttpResponse r) {
            final Object body = r.body();
            if (body instanceof String) {
                return new StringHttpResponse(r);
            }

            throw new IllegalStateException("Expected 'responseBody' to be of type String, instead was: " + r.getClass().getSimpleName());
        }

        throw new IllegalStateException("Expected 'response' to be of type HttpResponse, instead was: " + response.getClass().getSimpleName());
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

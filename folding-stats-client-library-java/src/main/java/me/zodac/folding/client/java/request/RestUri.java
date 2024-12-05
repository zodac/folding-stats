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

package me.zodac.folding.client.java.request;

import java.net.URI;

/**
 * Utility class used to create {@link URI}s for REST requests.
 */
public final class RestUri {

    /**
     * The character used to separate paths values in the URL for a REST request.
     */
    public static final char REST_URI_PATH_SEPARATOR = '/';

    private static final char REST_URI_NAME_VALUE_SEPARATOR = '=';
    private static final char REST_URI_FIELD_DELIMITER = '?';
    private static final String FIELDS_ATTRIBUTE = "fields" + REST_URI_FIELD_DELIMITER;

    private RestUri() {

    }

    /**
     * Creates a {@link URI} with just the base URL for the REST request.
     *
     * @param baseUrl the base URL
     * @return the {@link URI}
     */
    public static URI create(final String baseUrl) {
        return URI.create(baseUrl);
    }

    /**
     * Creates a {@link URI} starting with the base URL for the REST request, and then appending multiple path elements. Each path element will be
     * separated by {@value #REST_URI_PATH_SEPARATOR}. For example:
     *
     * <pre>
     *     example.com/baseUrl/resourceType/id
     * </pre>
     *
     * @param baseUrl               the base URL
     * @param firstPathElement      the first path element to define the resource path
     * @param remainingPathElements any additional path elements (can be a resource structure, or an ID, for example)
     * @return the {@link URI}
     */
    public static URI create(final String baseUrl, final Object firstPathElement, final Object... remainingPathElements) {
        final StringBuilder stringBuilder = new StringBuilder(baseUrl)
            .append(REST_URI_PATH_SEPARATOR)
            .append(firstPathElement);

        for (final Object remainingPathElement : remainingPathElements) {
            stringBuilder.append(REST_URI_PATH_SEPARATOR).append(remainingPathElement);
        }

        return URI.create(stringBuilder.toString());
    }

    /**
     * Creates a {@link URI} starting with the base URL for the REST request, and then appending a field name and value to define the objects
     * returned by the request.
     *
     * <pre>
     *     example.com/baseUrl/resourceType/fields?fieldName=fieldValue
     * </pre>
     *
     * @param baseUrl    the base URL
     * @param fieldName  the field name
     * @param fieldValue the field value
     * @return the {@link URI}
     */
    public static URI createWithFields(final String baseUrl, final String fieldName, final String fieldValue) {
        return URI.create(baseUrl + REST_URI_PATH_SEPARATOR + FIELDS_ATTRIBUTE + fieldName + REST_URI_NAME_VALUE_SEPARATOR + fieldValue);
    }

    /**
     * Creates a {@link URI} starting with the base URL for the REST request, and then appending a filter name and value to filter the objects
     * returned by the request.
     *
     * <pre>
     *     example.com/baseUrl/resourceType?filterName=filterValue
     * </pre>
     *
     * @param baseUrl     the base URL
     * @param filterName  the filter name
     * @param filterValue the filter value
     * @return the {@link URI}
     */
    public static URI createWithFilter(final String baseUrl, final String filterName, final Object filterValue) {
        return URI.create(baseUrl + REST_URI_FIELD_DELIMITER + filterName + REST_URI_NAME_VALUE_SEPARATOR + filterValue);
    }
}

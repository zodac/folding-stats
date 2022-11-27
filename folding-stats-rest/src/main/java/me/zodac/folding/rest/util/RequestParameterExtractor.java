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

package me.zodac.folding.rest.util;

import jakarta.servlet.ServletRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;

/**
 * Utility class to extract parameters from a REST request.
 */
public final class RequestParameterExtractor {

    private RequestParameterExtractor() {

    }

    /**
     * Extracts the parameters from the supplied {@link ServletRequest} and returns them as a comma-separated {@link String} in the form:
     * <pre>
     *     paramName1=paramValue1,paramName2=paramValue2
     * </pre>
     *
     * @param request the {@link ServletRequest} from which the parameters are to be extracted
     * @return the extract parameters as a comma-separated {@link String}
     */
    public static String extractParameters(final ServletRequest request) {
        final Collection<String> parameters = new ArrayList<>();

        final Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            final String parameterName = parameterNames.nextElement();
            final String parameterValue = request.getParameter(parameterName);
            parameters.add(String.format("%s=%s", parameterName, parameterValue));
        }

        return String.join(",", parameters);
    }
}

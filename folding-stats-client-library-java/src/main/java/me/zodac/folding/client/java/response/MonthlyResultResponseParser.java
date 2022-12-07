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

package me.zodac.folding.client.java.response;

import java.net.http.HttpResponse;
import java.time.Month;
import java.time.Year;
import me.zodac.folding.api.tc.result.MonthlyResult;
import me.zodac.folding.rest.api.util.RestUtilConstants;

/**
 * Utility class used to parse a {@link HttpResponse} returned from {@link me.zodac.folding.client.java.request.MonthlyResultRequestSender}.
 */
public final class MonthlyResultResponseParser {

    private MonthlyResultResponseParser() {

    }

    /**
     * Returns the {@link MonthlyResult} retrieved by
     * {@link me.zodac.folding.client.java.request.MonthlyResultRequestSender#getMonthlyResult(Year, Month, String)}.
     *
     * @param response the {@link HttpResponse} to parse
     * @return the retrieved {@link MonthlyResult}
     */
    public static MonthlyResult getMonthlyResult(final HttpResponse<String> response) {
        return RestUtilConstants.GSON.fromJson(response.body(), MonthlyResult.class);
    }
}

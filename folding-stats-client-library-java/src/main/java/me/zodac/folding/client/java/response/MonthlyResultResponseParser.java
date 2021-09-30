package me.zodac.folding.client.java.response;

import java.net.http.HttpResponse;
import java.time.Month;
import java.time.Year;
import me.zodac.folding.api.tc.result.MonthlyResult;
import me.zodac.folding.rest.util.RestUtilConstants;

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
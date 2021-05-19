package me.zodac.folding.client.java.response;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import lombok.experimental.UtilityClass;
import me.zodac.folding.rest.api.tc.historic.HistoricStats;

import java.lang.reflect.Type;
import java.net.http.HttpResponse;
import java.time.Month;
import java.time.Year;
import java.util.Collection;

/**
 * {@link UtilityClass} used to parse a {@link HttpResponse} returned from {@link me.zodac.folding.client.java.request.HistoricStatsRequestSender}.
 */
@UtilityClass
public final class HistoricStatsResponseParser {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    /**
     * Returns the {@link HistoricStats} retrieved by {@link me.zodac.folding.client.java.request.HistoricStatsRequestSender#getHourlyUserStats(int, Year, Month, int, String)}.
     *
     * @param response the {@link HttpResponse} to parse
     * @return the retrieved {@link HistoricStats}
     */
    public static Collection<HistoricStats> getHourlyUserStats(final HttpResponse<String> response) {
        final Type collectionType = new TypeToken<Collection<HistoricStats>>() {
        }.getType();
        return GSON.fromJson(response.body(), collectionType);
    }

    /**
     * Returns the {@link HistoricStats} retrieved by {@link me.zodac.folding.client.java.request.HistoricStatsRequestSender#getDailyUserStats(int, Year, Month, String)}.
     *
     * @param response the {@link HttpResponse} to parse
     * @return the retrieved {@link HistoricStats}
     */
    public static Collection<HistoricStats> getDailyUserStats(final HttpResponse<String> response) {
        final Type collectionType = new TypeToken<Collection<HistoricStats>>() {
        }.getType();
        return GSON.fromJson(response.body(), collectionType);
    }

    /**
     * Returns the {@link HistoricStats} retrieved by {@link me.zodac.folding.client.java.request.HistoricStatsRequestSender#getMonthlyUserStats(int, Year, String)}.
     *
     * @param response the {@link HttpResponse} to parse
     * @return the retrieved {@link HistoricStats}
     */
    public static Collection<HistoricStats> getMonthlyUserStats(final HttpResponse<String> response) {
        final Type collectionType = new TypeToken<Collection<HistoricStats>>() {
        }.getType();
        return GSON.fromJson(response.body(), collectionType);
    }

    /**
     * Returns the {@link HistoricStats} retrieved by {@link me.zodac.folding.client.java.request.HistoricStatsRequestSender#getHourlyTeamStats(int, Year, Month, int, String)}.
     *
     * @param response the {@link HttpResponse} to parse
     * @return the retrieved {@link HistoricStats}
     */
    public static Collection<HistoricStats> getHourlyTeamStats(final HttpResponse<String> response) {
        final Type collectionType = new TypeToken<Collection<HistoricStats>>() {
        }.getType();
        return GSON.fromJson(response.body(), collectionType);
    }

    /**
     * Returns the {@link HistoricStats} retrieved by {@link me.zodac.folding.client.java.request.HistoricStatsRequestSender#getDailyTeamStats(int, Year, Month, String)}.
     *
     * @param response the {@link HttpResponse} to parse
     * @return the retrieved {@link HistoricStats}
     */
    public static Collection<HistoricStats> getDailyTeamStats(final HttpResponse<String> response) {
        final Type collectionType = new TypeToken<Collection<HistoricStats>>() {
        }.getType();
        return GSON.fromJson(response.body(), collectionType);
    }

    /**
     * Returns the {@link HistoricStats} retrieved by {@link me.zodac.folding.client.java.request.HistoricStatsRequestSender#getMonthlyTeamStats(int, Year, String)}.
     *
     * @param response the {@link HttpResponse} to parse
     * @return the retrieved {@link HistoricStats}
     */
    public static Collection<HistoricStats> getMonthlyTeamStats(final HttpResponse<String> response) {
        final Type collectionType = new TypeToken<Collection<HistoricStats>>() {
        }.getType();
        return GSON.fromJson(response.body(), collectionType);
    }
}
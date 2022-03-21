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

package me.zodac.folding.test;

import static me.zodac.folding.rest.api.util.RestUtilConstants.HTTP_CLIENT;
import static me.zodac.folding.test.util.SystemCleaner.cleanSystemForComplexTests;
import static me.zodac.folding.test.util.TestConstants.FOLDING_URL;
import static me.zodac.folding.test.util.rest.response.HttpResponseHeaderUtils.getEntityTag;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Month;
import java.time.Year;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import me.zodac.folding.client.java.request.HistoricStatsRequestSender;
import me.zodac.folding.client.java.response.HistoricStatsResponseParser;
import me.zodac.folding.rest.api.exception.FoldingRestException;
import me.zodac.folding.rest.api.header.ContentType;
import me.zodac.folding.rest.api.header.RestHeader;
import me.zodac.folding.rest.api.tc.historic.HistoricStats;
import me.zodac.folding.test.util.TestConstants;
import me.zodac.folding.test.util.TestGenerator;
import me.zodac.folding.test.util.TestStats;
import me.zodac.folding.test.util.db.DatabaseUtils;
import me.zodac.folding.test.util.rest.request.UserUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@code Team Competition} {@link HistoricStats} for {@link me.zodac.folding.api.tc.User}s.
 */
class HistoricUserStatsTest {

    private static final HistoricStatsRequestSender HISTORIC_STATS_REQUEST_SENDER = HistoricStatsRequestSender.createWithUrl(FOLDING_URL);

    @BeforeAll
    static void setUp() throws FoldingRestException {
        cleanSystemForComplexTests();
    }

    @AfterAll
    static void tearDown() throws FoldingRestException {
        cleanSystemForComplexTests();
    }

    @Test
    void whenGettingHourlyStats_andValidUserIdIsGiven_andUserHasNoStats_thenNoStatsAreReturned_andResponseHas200Status() throws FoldingRestException {
        final int userId = UserUtils.create(TestGenerator.generateUser()).id();

        final HttpResponse<String> response = HISTORIC_STATS_REQUEST_SENDER.getHourlyUserStats(userId, Year.parse("2020"), Month.of(4), 12);
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final Collection<HistoricStats> results = HistoricStatsResponseParser.getHourlyUserStats(response);
        assertThat(results)
            .isEmpty();
    }

    @Test
    void whenGettingHourlyStats_andValidUserIdIsGiven_andUserHasSomeStats_thenStatsAreReturned_andResponseHas200Status() throws FoldingRestException {
        final int userId = UserUtils.create(TestGenerator.generateUser()).id();
        DatabaseUtils.insertStats("user_tc_stats_hourly",
            TestStats.create(userId, "2020-04-12 14:00:00", 100L, 1_000L, 10)
        );

        final HttpResponse<String> response = HISTORIC_STATS_REQUEST_SENDER.getHourlyUserStats(userId, Year.parse("2020"), Month.of(4), 12);
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final Collection<HistoricStats> results = HistoricStatsResponseParser.getHourlyUserStats(response);
        assertThat(results)
            .hasSize(1);
    }

    @Test
    void whenGettingHourlyStats_andUserHasMultipleStats_thenEachStatsEntryIsDiffedFromPreviousHour_andResponseHas200Status()
        throws FoldingRestException {
        final int userId = UserUtils.create(TestGenerator.generateUser()).id();
        DatabaseUtils.insertStats("user_tc_stats_hourly",
            TestStats.create(userId, "2020-04-11 23:00:00", 0L, 0L, 0),
            TestStats.create(userId, "2020-04-12 13:00:00", 20L, 200L, 2),
            TestStats.create(userId, "2020-04-12 14:00:00", 100L, 1_000L, 10)
        );

        final HttpResponse<String> response = HISTORIC_STATS_REQUEST_SENDER.getHourlyUserStats(userId, Year.parse("2020"), Month.of(4), 12);
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final List<HistoricStats> results = new ArrayList<>(HistoricStatsResponseParser.getHourlyUserStats(response));
        assertThat(results)
            .hasSize(2);

        final HistoricStats result = results.get(1); // Ignore first result, as it is the "base" stats for the user
        assertThat(result.getPoints())
            .isEqualTo(80L);
        assertThat(result.getMultipliedPoints())
            .isEqualTo(800L);
        assertThat(result.getUnits())
            .isEqualTo(8);
    }

    @Test
    void whenGettingHourlyStats_andUserHasMultipleStatsInSameHour_thenMaxStatsInHourAreReturned_andResponseHas200Status()
        throws FoldingRestException {
        final int userId = UserUtils.create(TestGenerator.generateUser()).id();
        DatabaseUtils.insertStats("user_tc_stats_hourly",
            TestStats.create(userId, "2020-04-11 23:00:00", 0L, 0L, 0),
            TestStats.create(userId, "2020-04-12 13:00:00", 50L, 500L, 5),
            TestStats.create(userId, "2020-04-12 14:00:00", 100L, 1_000L, 10),
            TestStats.create(userId, "2020-04-12 14:30:00", 110L, 1_100L, 11)
        );

        final HttpResponse<String> response = HISTORIC_STATS_REQUEST_SENDER.getHourlyUserStats(userId, Year.parse("2020"), Month.of(4), 12);
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final List<HistoricStats> results = new ArrayList<>(HistoricStatsResponseParser.getHourlyUserStats(response));
        assertThat(results)
            .hasSize(2);

        final HistoricStats result = results.get(1); // Ignore first result, as it is the "base" stats for the user
        assertThat(result.getPoints())
            .isEqualTo(60L);
        assertThat(result.getMultipliedPoints())
            .isEqualTo(600L);
        assertThat(result.getUnits())
            .isEqualTo(6);
    }

    @Test
    void whenGettingHourlyStats_givenRequestUsesPreviousEntityTag_andStatsHaveNotChanged_thenResponseHas304Status_andNoBody()
        throws FoldingRestException {
        final int userId = UserUtils.create(TestGenerator.generateUser()).id();
        DatabaseUtils.insertStats("user_tc_stats_hourly",
            TestStats.create(userId, "2020-04-12 14:00:00", 100L, 1_000L, 10)
        );

        final HttpResponse<String> response = HISTORIC_STATS_REQUEST_SENDER.getHourlyUserStats(userId, Year.parse("2020"), Month.of(4), 12);
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final String eTag = getEntityTag(response);

        final HttpResponse<String> cachedResponse =
            HISTORIC_STATS_REQUEST_SENDER.getHourlyUserStats(userId, Year.parse("2020"), Month.of(4), 12, eTag);
        assertThat(cachedResponse.statusCode())
            .as("Expected second request to have a 304_NOT_MODIFIED HTTP response")
            .isEqualTo(HttpURLConnection.HTTP_NOT_MODIFIED);

        assertThat(HistoricStatsResponseParser.getHourlyUserStats(cachedResponse))
            .as("Expected cached response to have the same content as the non-cached response")
            .isNull();
    }

    @Test
    void whenGettingHourlyStats_givenNonExistingUserId_thenResponseHas404Status() throws FoldingRestException {
        final HttpResponse<String> response =
            HISTORIC_STATS_REQUEST_SENDER.getHourlyUserStats(TestConstants.NON_EXISTING_ID, Year.parse("2020"), Month.of(4), 12);
        assertThat(response.statusCode())
            .as("Did not receive a 404_NOT_FOUND HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_NOT_FOUND);
    }

    @Test
    void whenGettingHourlyStats_givenInvalidUserId_thenResponseHas400Status() throws IOException, InterruptedException {
        final HttpRequest request = HttpRequest.newBuilder()
            .GET()
            .uri(URI.create(
                FOLDING_URL + "/historic/users/" + TestConstants.INVALID_FORMAT_ID + '/' + Year.parse("2020") + '/' + Month.of(4) + '/' + 12))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentTypeValue())
            .build();

        final HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode())
            .as("Did not receive a 400_BAD_REQUEST HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);

        assertThat(response.body())
            .as("Did not receive valid error message: " + response.body())
            .contains("not a valid format");
    }

    @Test
    void whenGettingHourlyStats_andInvalidDateIsGiven_thenResponseHas400Status() throws FoldingRestException {
        final int userId = UserUtils.create(TestGenerator.generateUser()).id();
        final int invalidDay = 35;

        final HttpResponse<String> response = HISTORIC_STATS_REQUEST_SENDER.getHourlyUserStats(userId, Year.parse("2020"), Month.of(4), invalidDay);
        assertThat(response.statusCode())
            .as("Did not receive a 400_BAD_REQUEST HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);
    }

    @Test
    void whenGettingDailyStats_andValidUserIdIsGiven_andUserHasNoStats_thenNoStatsAreReturned_andResponseHas200Status() throws FoldingRestException {
        final int userId = UserUtils.create(TestGenerator.generateUser()).id();

        final HttpResponse<String> response = HISTORIC_STATS_REQUEST_SENDER.getDailyUserStats(userId, Year.parse("2020"), Month.of(4));
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final Collection<HistoricStats> results = HistoricStatsResponseParser.getDailyUserStats(response);
        assertThat(results)
            .isEmpty();
    }

    @Test
    void whenGettingDailyStats_andValidUserIdIsGiven_andUserHasSomeStats_thenStatsAreReturned_andResponseHas200Status() throws FoldingRestException {
        final int userId = UserUtils.create(TestGenerator.generateUser()).id();
        DatabaseUtils.insertStats("user_tc_stats_hourly",
            TestStats.create(userId, "2020-04-12 14:00:00", 100L, 1_000L, 10)
        );

        final HttpResponse<String> response = HISTORIC_STATS_REQUEST_SENDER.getDailyUserStats(userId, Year.parse("2020"), Month.of(4));
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final Collection<HistoricStats> results = HistoricStatsResponseParser.getDailyUserStats(response);
        assertThat(results)
            .hasSize(1);
    }

    @Test
    void whenGettingDailyStats_andUserHasMultipleStats_thenEachStatsEntryIsDiffedFromPreviousDay_andResponseHas200Status()
        throws FoldingRestException {
        final int userId = UserUtils.create(TestGenerator.generateUser()).id();
        DatabaseUtils.insertStats("user_tc_stats_hourly",
            TestStats.create(userId, "2020-04-12 13:00:00", 20L, 200L, 2),
            TestStats.create(userId, "2020-04-13 13:00:00", 100L, 1_000L, 10)
        );

        final HttpResponse<String> response = HISTORIC_STATS_REQUEST_SENDER.getDailyUserStats(userId, Year.parse("2020"), Month.of(4));
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final List<HistoricStats> results = new ArrayList<>(HistoricStatsResponseParser.getDailyUserStats(response));
        assertThat(results)
            .hasSize(2);

        final HistoricStats result = results.get(1); // Ignore first result, as it is the "base" stats for the user
        assertThat(result.getPoints())
            .isEqualTo(80L);
        assertThat(result.getMultipliedPoints())
            .isEqualTo(800L);
        assertThat(result.getUnits())
            .isEqualTo(8);
    }

    @Test
    void whenGettingDailyStats_andUserHasMultipleStatsInSameDay_thenMaxStatsInDayAreReturned_andResponseHas200Status() throws FoldingRestException {
        final int userId = UserUtils.create(TestGenerator.generateUser()).id();
        DatabaseUtils.insertStats("user_tc_stats_hourly",
            TestStats.create(userId, "2020-04-12 13:00:00", 10L, 100L, 1),
            TestStats.create(userId, "2020-04-13 14:00:00", 50L, 500L, 5),
            TestStats.create(userId, "2020-04-13 15:00:00", 110L, 1_100L, 11)
        );

        final HttpResponse<String> response = HISTORIC_STATS_REQUEST_SENDER.getDailyUserStats(userId, Year.parse("2020"), Month.of(4));
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final List<HistoricStats> results = new ArrayList<>(HistoricStatsResponseParser.getDailyUserStats(response));
        assertThat(results)
            .hasSize(2);

        final HistoricStats result = results.get(1); // Ignore first result, as it is the "base" stats for the user
        assertThat(result.getPoints())
            .isEqualTo(100L);
        assertThat(result.getMultipliedPoints())
            .isEqualTo(1_000L);
        assertThat(result.getUnits())
            .isEqualTo(10);
    }

    @Test
    void whenGettingDailyStats_givenRequestUsesPreviousEntityTag_andStatsHaveNotChanged_thenResponseHas304Status_andNoBody()
        throws FoldingRestException {
        final int userId = UserUtils.create(TestGenerator.generateUser()).id();
        DatabaseUtils.insertStats("user_tc_stats_hourly",
            TestStats.create(userId, "2020-04-12 14:00:00", 100L, 1_000L, 10)
        );

        final HttpResponse<String> response = HISTORIC_STATS_REQUEST_SENDER.getDailyUserStats(userId, Year.parse("2020"), Month.of(4));
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final String eTag = getEntityTag(response);

        final HttpResponse<String> cachedResponse = HISTORIC_STATS_REQUEST_SENDER.getDailyUserStats(userId, Year.parse("2020"), Month.of(4), eTag);
        assertThat(cachedResponse.statusCode())
            .as("Expected second request to have a 304_NOT_MODIFIED HTTP response")
            .isEqualTo(HttpURLConnection.HTTP_NOT_MODIFIED);

        assertThat(HistoricStatsResponseParser.getDailyUserStats(cachedResponse))
            .as("Expected cached response to have the same content as the non-cached response")
            .isNull();
    }

    @Test
    void whenGettingDailyStats_givenNonExistingUserId_thenResponseHas404Status() throws FoldingRestException {
        final HttpResponse<String> response =
            HISTORIC_STATS_REQUEST_SENDER.getDailyUserStats(TestConstants.NON_EXISTING_ID, Year.parse("2020"), Month.of(4));
        assertThat(response.statusCode())
            .as("Did not receive a 404_NOT_FOUND HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_NOT_FOUND);
    }

    @Test
    void whenGettingDailyStats_givenInvalidUserId_thenResponseHas400Status() throws IOException, InterruptedException {
        final HttpRequest request = HttpRequest.newBuilder()
            .GET()
            .uri(URI.create(FOLDING_URL + "/historic/users/" + TestConstants.INVALID_FORMAT_ID + '/' + Year.parse("2020") + '/' + Month.of(4)))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentTypeValue())
            .build();

        final HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode())
            .as("Did not receive a 400_BAD_REQUEST HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);

        assertThat(response.body())
            .as("Did not receive valid error message: " + response.body())
            .contains("not a valid format");
    }

    @Test
    void whenGettingDailyStats_andInvalidDateIsGiven_thenResponseHas400Status() throws FoldingRestException, IOException, InterruptedException {
        final int userId = UserUtils.create(TestGenerator.generateUser()).id();
        final int invalidMonth = 25;

        final HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
            .GET()
            .uri(URI.create(FOLDING_URL + "/historic/users/" + userId + "/2020/" + invalidMonth))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentTypeValue());

        final HttpRequest request = requestBuilder.build();

        final HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode())
            .as("Did not receive a 400_BAD_REQUEST HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);
    }

    @Test
    void whenGettingMonthlyStats_andValidUserIdIsGiven_andUserHasNoStats_thenNoStatsAreReturned_andResponseHas200Status()
        throws FoldingRestException {
        final int userId = UserUtils.create(TestGenerator.generateUser()).id();

        final HttpResponse<String> response = HISTORIC_STATS_REQUEST_SENDER.getMonthlyUserStats(userId, Year.parse("2020"));
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final Collection<HistoricStats> results = HistoricStatsResponseParser.getMonthlyUserStats(response);
        assertThat(results)
            .isEmpty();
    }

    @Test
    void whenGettingMonthlyStats_andValidUserIdIsGiven_andUserHasSomeStats_thenStatsAreReturned_andResponseHas200Status()
        throws FoldingRestException {
        final int userId = UserUtils.create(TestGenerator.generateUser()).id();
        DatabaseUtils.insertStats("user_tc_stats_hourly",
            TestStats.create(userId, "2020-04-12 14:00:00", 100L, 1_000L, 10)
        );

        final HttpResponse<String> response = HISTORIC_STATS_REQUEST_SENDER.getMonthlyUserStats(userId, Year.parse("2020"));
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final Collection<HistoricStats> results = HistoricStatsResponseParser.getMonthlyUserStats(response);
        assertThat(results)
            .hasSize(1);
    }

    @Test
    void whenGettingMonthlyStats_andUserHasMultipleStats_thenEachStatsEntryIsDiffedFromPreviousMonth_andResponseHas200Status()
        throws FoldingRestException {
        final int userId = UserUtils.create(TestGenerator.generateUser()).id();
        DatabaseUtils.insertStats("user_tc_stats_hourly",
            TestStats.create(userId, "2020-03-12 13:00:00", 20L, 200L, 2), // No diff from this result, since stats are reset each month
            TestStats.create(userId, "2020-04-12 13:00:00", 100L, 1_000L, 10)
        );

        final HttpResponse<String> response = HISTORIC_STATS_REQUEST_SENDER.getMonthlyUserStats(userId, Year.parse("2020"));
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final List<HistoricStats> results = new ArrayList<>(HistoricStatsResponseParser.getMonthlyUserStats(response));
        assertThat(results)
            .hasSize(2);

        final HistoricStats result = results.get(1); // Ignore first result, as it is the "base" stats for the user
        assertThat(result.getPoints())
            .isEqualTo(100L);
        assertThat(result.getMultipliedPoints())
            .isEqualTo(1_000L);
        assertThat(result.getUnits())
            .isEqualTo(10);
    }

    @Test
    void whenGettingMonthlyStats_andUserHasMultipleStatsInSameMonth_thenMaxStatsInMonthAreReturned_andResponseHas200Status()
        throws FoldingRestException {
        final int userId = UserUtils.create(TestGenerator.generateUser()).id();
        DatabaseUtils.insertStats("user_tc_stats_hourly",
            TestStats.create(userId, "2020-03-12 13:00:00", 50L, 500L, 5), // No diff from this result, since stats are reset each month
            TestStats.create(userId, "2020-04-13 14:00:00", 100L, 1_000L, 10),
            TestStats.create(userId, "2020-04-13 15:00:00", 110L, 1_100L, 11)
        );

        final HttpResponse<String> response = HISTORIC_STATS_REQUEST_SENDER.getMonthlyUserStats(userId, Year.parse("2020"));
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final List<HistoricStats> results = new ArrayList<>(HistoricStatsResponseParser.getMonthlyUserStats(response));
        assertThat(results)
            .hasSize(2);

        final HistoricStats result = results.get(1); // Ignore first result, as it is the "base" stats for the user
        assertThat(result.getPoints())
            .isEqualTo(110L);
        assertThat(result.getMultipliedPoints())
            .isEqualTo(1_100L);
        assertThat(result.getUnits())
            .isEqualTo(11);
    }

    @Test
    void whenGettingMonthlyStats_givenRequestUsesPreviousEntityTag_andStatsHaveNotChanged_thenResponseHas304Status_andNoBody()
        throws FoldingRestException {
        final int userId = UserUtils.create(TestGenerator.generateUser()).id();
        DatabaseUtils.insertStats("user_tc_stats_hourly",
            TestStats.create(userId, "2020-04-12 14:00:00", 100L, 1_000L, 10)
        );

        final HttpResponse<String> response = HISTORIC_STATS_REQUEST_SENDER.getMonthlyUserStats(userId, Year.parse("2020"));
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final String eTag = getEntityTag(response);

        final HttpResponse<String> cachedResponse = HISTORIC_STATS_REQUEST_SENDER.getMonthlyUserStats(userId, Year.parse("2020"), eTag);
        assertThat(cachedResponse.statusCode())
            .as("Expected second request to have a 304_NOT_MODIFIED HTTP response")
            .isEqualTo(HttpURLConnection.HTTP_NOT_MODIFIED);

        assertThat(HistoricStatsResponseParser.getMonthlyUserStats(cachedResponse))
            .as("Expected cached response to have the same content as the non-cached response")
            .isNull();
    }

    @Test
    void whenGettingMonthlyStats_givenNonExistingUserId_thenResponseHas404Status() throws FoldingRestException {
        final HttpResponse<String> response = HISTORIC_STATS_REQUEST_SENDER.getMonthlyUserStats(TestConstants.NON_EXISTING_ID, Year.parse("2020"));
        assertThat(response.statusCode())
            .as("Did not receive a 404_NOT_FOUND HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_NOT_FOUND);
    }

    @Test
    void whenGettingMonthlyStats_givenInvalidUserId_thenResponseHas400Status() throws IOException, InterruptedException {
        final HttpRequest request = HttpRequest.newBuilder()
            .GET()
            .uri(URI.create(FOLDING_URL + "/historic/users/" + TestConstants.INVALID_FORMAT_ID + '/' + Year.parse("2020")))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentTypeValue())
            .build();

        final HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode())
            .as("Did not receive a 400_BAD_REQUEST HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);

        assertThat(response.body())
            .as("Did not receive valid error message: " + response.body())
            .contains("not a valid format");
    }

    @Test
    void whenGettingMonthlyStats_andInvalidDateIsGiven_thenResponseHas400Status() throws FoldingRestException, IOException, InterruptedException {
        final int userId = UserUtils.create(TestGenerator.generateUser()).id();

        final HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
            .GET()
            .uri(URI.create(FOLDING_URL + "/historic/users/" + userId + "/invalidYear"))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentTypeValue());

        final HttpRequest request = requestBuilder.build();

        final HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode())
            .as("Did not receive a 400_BAD_REQUEST HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);
    }
}

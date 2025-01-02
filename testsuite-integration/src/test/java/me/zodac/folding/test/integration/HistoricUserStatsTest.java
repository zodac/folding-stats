/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2025 zodac.me
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

package me.zodac.folding.test.integration;

import static me.zodac.folding.rest.api.util.RestUtilConstants.HTTP_CLIENT;
import static me.zodac.folding.test.integration.util.SystemCleaner.cleanSystemForComplexTests;
import static me.zodac.folding.test.integration.util.TestConstants.FOLDING_URL;
import static me.zodac.folding.test.integration.util.TestConstants.INVALID_FORMAT_ID;
import static me.zodac.folding.test.integration.util.rest.response.HttpResponseHeaderUtils.getEntityTag;
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
import me.zodac.folding.client.java.request.HistoricStatsType;
import me.zodac.folding.client.java.request.RestUri;
import me.zodac.folding.client.java.response.HistoricStatsResponseParser;
import me.zodac.folding.rest.api.exception.FoldingRestException;
import me.zodac.folding.rest.api.header.ContentType;
import me.zodac.folding.rest.api.header.RestHeader;
import me.zodac.folding.rest.api.tc.historic.HistoricStats;
import me.zodac.folding.test.integration.util.DummyDataGenerator;
import me.zodac.folding.test.integration.util.DummyStats;
import me.zodac.folding.test.integration.util.TestConstants;
import me.zodac.folding.test.integration.util.db.DatabaseUtils;
import me.zodac.folding.test.integration.util.rest.request.UserUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@code Team Competition} {@link HistoricStats} for {@link me.zodac.folding.api.tc.User}s.
 */
class HistoricUserStatsTest {

    private static final HistoricStatsRequestSender HISTORIC_STATS_REQUEST_SENDER = HistoricStatsRequestSender.createWithUrl(FOLDING_URL);
    private static final String BASE_URL = FOLDING_URL + RestUri.REST_URI_PATH_SEPARATOR + "historic";

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
        final int userId = UserUtils.create(DummyDataGenerator.generateUser()).id();

        final HttpResponse<String> response = HISTORIC_STATS_REQUEST_SENDER.getHourlyUserStats(userId, Year.parse("2020"), Month.of(4), 12);
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: %s", response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final Collection<HistoricStats> results = HistoricStatsResponseParser.getHourlyUserStats(response);
        assertThat(results)
            .isEmpty();
    }

    @Test
    void whenGettingHourlyStats_andValidUserIdIsGiven_andUserHasSomeStats_thenStatsAreReturned_andResponseHas200Status() throws FoldingRestException {
        final int userId = UserUtils.create(DummyDataGenerator.generateUser()).id();
        DatabaseUtils.insertStats("user_tc_stats_hourly",
            DummyStats.create(userId, "2020-04-12 14:00:00", 100L, 1_000L, 10)
        );

        final HttpResponse<String> response = HISTORIC_STATS_REQUEST_SENDER.getHourlyUserStats(userId, Year.parse("2020"), Month.of(4), 12);
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: %s", response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final Collection<HistoricStats> results = HistoricStatsResponseParser.getHourlyUserStats(response);
        assertThat(results)
            .hasSize(1);
    }

    @Test
    void whenGettingHourlyStats_andUserHasMultipleStats_thenEachStatsEntryIsDiffedFromPreviousHour_andResponseHas200Status()
        throws FoldingRestException {
        final int userId = UserUtils.create(DummyDataGenerator.generateUser()).id();
        DatabaseUtils.insertStats("user_tc_stats_hourly",
            DummyStats.create(userId, "2020-04-11 23:00:00", 0L, 0L, 0),
            DummyStats.create(userId, "2020-04-12 13:00:00", 20L, 200L, 2),
            DummyStats.create(userId, "2020-04-12 14:00:00", 100L, 1_000L, 10)
        );

        final HttpResponse<String> response = HISTORIC_STATS_REQUEST_SENDER.getHourlyUserStats(userId, Year.parse("2020"), Month.of(4), 12);
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: %s", response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final List<HistoricStats> results = new ArrayList<>(HistoricStatsResponseParser.getHourlyUserStats(response));
        assertThat(results)
            .hasSize(2);

        final HistoricStats result = results.get(1); // Ignore first result, as it is the "base" stats for the user
        assertThat(result.points())
            .isEqualTo(80L);
        assertThat(result.multipliedPoints())
            .isEqualTo(800L);
        assertThat(result.units())
            .isEqualTo(8);
    }

    @Test
    void whenGettingHourlyStats_andUserHasMultipleStatsInSameHour_thenMaxStatsInHourAreReturned_andResponseHas200Status()
        throws FoldingRestException {
        final int userId = UserUtils.create(DummyDataGenerator.generateUser()).id();
        DatabaseUtils.insertStats("user_tc_stats_hourly",
            DummyStats.create(userId, "2020-04-11 23:00:00", 0L, 0L, 0),
            DummyStats.create(userId, "2020-04-12 13:00:00", 50L, 500L, 5),
            DummyStats.create(userId, "2020-04-12 14:00:00", 100L, 1_000L, 10),
            DummyStats.create(userId, "2020-04-12 14:30:00", 110L, 1_100L, 11)
        );

        final HttpResponse<String> response = HISTORIC_STATS_REQUEST_SENDER.getHourlyUserStats(userId, Year.parse("2020"), Month.of(4), 12);
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: %s", response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final List<HistoricStats> results = new ArrayList<>(HistoricStatsResponseParser.getHourlyUserStats(response));
        assertThat(results)
            .hasSize(2);

        final HistoricStats result = results.get(1); // Ignore first result, as it is the "base" stats for the user
        assertThat(result.points())
            .isEqualTo(60L);
        assertThat(result.multipliedPoints())
            .isEqualTo(600L);
        assertThat(result.units())
            .isEqualTo(6);
    }

    @Test
    void whenGettingHourlyStats_givenRequestUsesPreviousEntityTag_andStatsHaveNotChanged_thenResponseHas304Status_andNoBody()
        throws FoldingRestException {
        final int userId = UserUtils.create(DummyDataGenerator.generateUser()).id();
        DatabaseUtils.insertStats("user_tc_stats_hourly",
            DummyStats.create(userId, "2020-04-12 14:00:00", 100L, 1_000L, 10)
        );

        final HttpResponse<String> response = HISTORIC_STATS_REQUEST_SENDER.getHourlyUserStats(userId, Year.parse("2020"), Month.of(4), 12);
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: %s", response.body())
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
            .as("Did not receive a 404_NOT_FOUND HTTP response: %s", response.body())
            .isEqualTo(HttpURLConnection.HTTP_NOT_FOUND);
    }

    @Test
    void whenGettingHourlyStats_givenInvalidUserId_thenResponseHas400Status() throws IOException, InterruptedException {
        final HttpRequest request = HttpRequest.newBuilder()
            .GET()
            .uri(RestUri.create(BASE_URL, HistoricStatsType.USER.endpointUrl(), INVALID_FORMAT_ID, Year.parse("2020"), Month.of(4), 12))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentTypeValue())
            .build();

        final HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode())
            .as("Did not receive a 400_BAD_REQUEST HTTP response: %s", response.body())
            .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);

        assertThat(response.body())
            .as("Did not receive valid error message: %s", response.body())
            .contains("not a valid format");
    }

    @Test
    void whenGettingHourlyStats_andInvalidDateIsGiven_thenResponseHas400Status() throws FoldingRestException {
        final int userId = UserUtils.create(DummyDataGenerator.generateUser()).id();
        final int invalidDay = 35;

        final HttpResponse<String> response = HISTORIC_STATS_REQUEST_SENDER.getHourlyUserStats(userId, Year.parse("2020"), Month.of(4), invalidDay);
        assertThat(response.statusCode())
            .as("Did not receive a 400_BAD_REQUEST HTTP response: %s", response.body())
            .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);
    }

    @Test
    void whenGettingDailyStats_andValidUserIdIsGiven_andUserHasNoStats_thenNoStatsAreReturned_andResponseHas200Status() throws FoldingRestException {
        final int userId = UserUtils.create(DummyDataGenerator.generateUser()).id();

        final HttpResponse<String> response = HISTORIC_STATS_REQUEST_SENDER.getDailyUserStats(userId, Year.parse("2020"), Month.of(4));
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: %s", response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final Collection<HistoricStats> results = HistoricStatsResponseParser.getDailyUserStats(response);
        assertThat(results)
            .isEmpty();
    }

    @Test
    void whenGettingDailyStats_andValidUserIdIsGiven_andUserHasSomeStats_thenStatsAreReturned_andResponseHas200Status() throws FoldingRestException {
        final int userId = UserUtils.create(DummyDataGenerator.generateUser()).id();
        DatabaseUtils.insertStats("user_tc_stats_hourly",
            DummyStats.create(userId, "2020-04-12 14:00:00", 100L, 1_000L, 10)
        );

        final HttpResponse<String> response = HISTORIC_STATS_REQUEST_SENDER.getDailyUserStats(userId, Year.parse("2020"), Month.of(4));
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: %s", response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final Collection<HistoricStats> results = HistoricStatsResponseParser.getDailyUserStats(response);
        assertThat(results)
            .hasSize(1);
    }

    @Test
    void whenGettingDailyStats_andUserHasMultipleStats_thenEachStatsEntryIsDiffedFromPreviousDay_andResponseHas200Status()
        throws FoldingRestException {
        final int userId = UserUtils.create(DummyDataGenerator.generateUser()).id();
        DatabaseUtils.insertStats("user_tc_stats_hourly",
            DummyStats.create(userId, "2020-04-12 13:00:00", 20L, 200L, 2),
            DummyStats.create(userId, "2020-04-13 13:00:00", 100L, 1_000L, 10)
        );

        final HttpResponse<String> response = HISTORIC_STATS_REQUEST_SENDER.getDailyUserStats(userId, Year.parse("2020"), Month.of(4));
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: %s", response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final List<HistoricStats> results = new ArrayList<>(HistoricStatsResponseParser.getDailyUserStats(response));
        assertThat(results)
            .hasSize(2);

        final HistoricStats result = results.get(1); // Ignore first result, as it is the "base" stats for the user
        assertThat(result.points())
            .isEqualTo(80L);
        assertThat(result.multipliedPoints())
            .isEqualTo(800L);
        assertThat(result.units())
            .isEqualTo(8);
    }

    @Test
    void whenGettingDailyStats_andUserHasMultipleStatsInSameDay_thenMaxStatsInDayAreReturned_andResponseHas200Status() throws FoldingRestException {
        final int userId = UserUtils.create(DummyDataGenerator.generateUser()).id();
        DatabaseUtils.insertStats("user_tc_stats_hourly",
            DummyStats.create(userId, "2020-04-12 13:00:00", 10L, 100L, 1),
            DummyStats.create(userId, "2020-04-13 14:00:00", 50L, 500L, 5),
            DummyStats.create(userId, "2020-04-13 15:00:00", 110L, 1_100L, 11)
        );

        final HttpResponse<String> response = HISTORIC_STATS_REQUEST_SENDER.getDailyUserStats(userId, Year.parse("2020"), Month.of(4));
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: %s", response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final List<HistoricStats> results = new ArrayList<>(HistoricStatsResponseParser.getDailyUserStats(response));
        assertThat(results)
            .hasSize(2);

        final HistoricStats result = results.get(1); // Ignore first result, as it is the "base" stats for the user
        assertThat(result.points())
            .isEqualTo(100L);
        assertThat(result.multipliedPoints())
            .isEqualTo(1_000L);
        assertThat(result.units())
            .isEqualTo(10);
    }

    @Test
    void whenGettingDailyStats_givenRequestUsesPreviousEntityTag_andStatsHaveNotChanged_thenResponseHas304Status_andNoBody()
        throws FoldingRestException {
        final int userId = UserUtils.create(DummyDataGenerator.generateUser()).id();
        DatabaseUtils.insertStats("user_tc_stats_hourly",
            DummyStats.create(userId, "2020-04-12 14:00:00", 100L, 1_000L, 10)
        );

        final HttpResponse<String> response = HISTORIC_STATS_REQUEST_SENDER.getDailyUserStats(userId, Year.parse("2020"), Month.of(4));
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: %s", response.body())
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
            .as("Did not receive a 404_NOT_FOUND HTTP response: %s", response.body())
            .isEqualTo(HttpURLConnection.HTTP_NOT_FOUND);
    }

    @Test
    void whenGettingDailyStats_givenInvalidUserId_thenResponseHas400Status() throws IOException, InterruptedException {
        final HttpRequest request = HttpRequest.newBuilder()
            .GET()
            .uri(RestUri.create(BASE_URL, HistoricStatsType.USER.endpointUrl(), INVALID_FORMAT_ID, Year.parse("2020"), Month.of(4)))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentTypeValue())
            .build();

        final HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode())
            .as("Did not receive a 400_BAD_REQUEST HTTP response: %s", response.body())
            .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);

        assertThat(response.body())
            .as("Did not receive valid error message: %s", response.body())
            .contains("not a valid format");
    }

    @Test
    void whenGettingDailyStats_andInvalidDateIsGiven_thenResponseHas400Status() throws FoldingRestException, IOException, InterruptedException {
        final int userId = UserUtils.create(DummyDataGenerator.generateUser()).id();
        final int invalidMonth = 25;

        final HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
            .GET()
            .uri(URI.create(FOLDING_URL + "/historic/users/" + userId + "/2020/" + invalidMonth))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentTypeValue());

        final HttpRequest request = requestBuilder.build();

        final HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode())
            .as("Did not receive a 400_BAD_REQUEST HTTP response: %s", response.body())
            .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);
    }

    @Test
    void whenGettingMonthlyStats_andValidUserIdIsGiven_andUserHasNoStats_thenNoStatsAreReturned_andResponseHas200Status()
        throws FoldingRestException {
        final int userId = UserUtils.create(DummyDataGenerator.generateUser()).id();

        final HttpResponse<String> response = HISTORIC_STATS_REQUEST_SENDER.getMonthlyUserStats(userId, Year.parse("2020"));
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: %s", response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final Collection<HistoricStats> results = HistoricStatsResponseParser.getMonthlyUserStats(response);
        assertThat(results)
            .isEmpty();
    }

    @Test
    void whenGettingMonthlyStats_andValidUserIdIsGiven_andUserHasSomeStats_thenStatsAreReturned_andResponseHas200Status()
        throws FoldingRestException {
        final int userId = UserUtils.create(DummyDataGenerator.generateUser()).id();
        DatabaseUtils.insertStats("user_tc_stats_hourly",
            DummyStats.create(userId, "2020-04-12 14:00:00", 100L, 1_000L, 10)
        );

        final HttpResponse<String> response = HISTORIC_STATS_REQUEST_SENDER.getMonthlyUserStats(userId, Year.parse("2020"));
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: %s", response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final Collection<HistoricStats> results = HistoricStatsResponseParser.getMonthlyUserStats(response);
        assertThat(results)
            .hasSize(1);
    }

    @Test
    void whenGettingMonthlyStats_andUserHasMultipleStats_thenEachStatsEntryIsDiffedFromPreviousMonth_andResponseHas200Status()
        throws FoldingRestException {
        final int userId = UserUtils.create(DummyDataGenerator.generateUser()).id();
        DatabaseUtils.insertStats("user_tc_stats_hourly",
            DummyStats.create(userId, "2020-03-12 13:00:00", 20L, 200L, 2), // No diff from this result, since stats are reset each month
            DummyStats.create(userId, "2020-04-12 13:00:00", 100L, 1_000L, 10)
        );

        final HttpResponse<String> response = HISTORIC_STATS_REQUEST_SENDER.getMonthlyUserStats(userId, Year.parse("2020"));
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: %s", response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final List<HistoricStats> results = new ArrayList<>(HistoricStatsResponseParser.getMonthlyUserStats(response));
        assertThat(results)
            .hasSize(2);

        final HistoricStats result = results.get(1); // Ignore first result, as it is the "base" stats for the user
        assertThat(result.points())
            .isEqualTo(100L);
        assertThat(result.multipliedPoints())
            .isEqualTo(1_000L);
        assertThat(result.units())
            .isEqualTo(10);
    }

    @Test
    void whenGettingMonthlyStats_andUserHasMultipleStatsInSameMonth_thenMaxStatsInMonthAreReturned_andResponseHas200Status()
        throws FoldingRestException {
        final int userId = UserUtils.create(DummyDataGenerator.generateUser()).id();
        DatabaseUtils.insertStats("user_tc_stats_hourly",
            DummyStats.create(userId, "2020-03-12 13:00:00", 50L, 500L, 5), // No diff from this result, since stats are reset each month
            DummyStats.create(userId, "2020-04-13 14:00:00", 100L, 1_000L, 10),
            DummyStats.create(userId, "2020-04-13 15:00:00", 110L, 1_100L, 11)
        );

        final HttpResponse<String> response = HISTORIC_STATS_REQUEST_SENDER.getMonthlyUserStats(userId, Year.parse("2020"));
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: %s", response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final List<HistoricStats> results = new ArrayList<>(HistoricStatsResponseParser.getMonthlyUserStats(response));
        assertThat(results)
            .hasSize(2);

        final HistoricStats result = results.get(1); // Ignore first result, as it is the "base" stats for the user
        assertThat(result.points())
            .isEqualTo(110L);
        assertThat(result.multipliedPoints())
            .isEqualTo(1_100L);
        assertThat(result.units())
            .isEqualTo(11);
    }

    @Test
    void whenGettingMonthlyStats_givenRequestUsesPreviousEntityTag_andStatsHaveNotChanged_thenResponseHas304Status_andNoBody()
        throws FoldingRestException {
        final int userId = UserUtils.create(DummyDataGenerator.generateUser()).id();
        DatabaseUtils.insertStats("user_tc_stats_hourly",
            DummyStats.create(userId, "2020-04-12 14:00:00", 100L, 1_000L, 10)
        );

        final HttpResponse<String> response = HISTORIC_STATS_REQUEST_SENDER.getMonthlyUserStats(userId, Year.parse("2020"));
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: %s", response.body())
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
            .as("Did not receive a 404_NOT_FOUND HTTP response: %s", response.body())
            .isEqualTo(HttpURLConnection.HTTP_NOT_FOUND);
    }

    @Test
    void whenGettingMonthlyStats_givenInvalidUserId_thenResponseHas400Status() throws IOException, InterruptedException {
        final HttpRequest request = HttpRequest.newBuilder()
            .GET()
            .uri(RestUri.create(BASE_URL, HistoricStatsType.USER.endpointUrl(), INVALID_FORMAT_ID, Year.parse("2020")))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentTypeValue())
            .build();

        final HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode())
            .as("Did not receive a 400_BAD_REQUEST HTTP response: %s", response.body())
            .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);

        assertThat(response.body())
            .as("Did not receive valid error message: %s", response.body())
            .contains("not a valid format");
    }

    @Test
    void whenGettingMonthlyStats_andInvalidDateIsGiven_thenResponseHas400Status() throws FoldingRestException, IOException, InterruptedException {
        final int userId = UserUtils.create(DummyDataGenerator.generateUser()).id();

        final HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
            .GET()
            .uri(URI.create(FOLDING_URL + "/historic/users/" + userId + "/invalidYear"))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentTypeValue());

        final HttpRequest request = requestBuilder.build();

        final HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode())
            .as("Did not receive a 400_BAD_REQUEST HTTP response: %s", response.body())
            .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);
    }
}

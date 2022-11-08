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

package me.zodac.folding.test.integration;

import static me.zodac.folding.rest.api.util.RestUtilConstants.HTTP_CLIENT;
import static me.zodac.folding.test.integration.util.TestConstants.FOLDING_URL;
import static me.zodac.folding.test.integration.util.TestConstants.INVALID_FORMAT_ID;
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
import me.zodac.folding.api.tc.Category;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.client.java.request.HistoricStatsRequestSender;
import me.zodac.folding.client.java.response.HistoricStatsResponseParser;
import me.zodac.folding.rest.api.exception.FoldingRestException;
import me.zodac.folding.rest.api.header.ContentType;
import me.zodac.folding.rest.api.header.RestHeader;
import me.zodac.folding.rest.api.tc.historic.HistoricStats;
import me.zodac.folding.test.integration.util.SystemCleaner;
import me.zodac.folding.test.integration.util.TestConstants;
import me.zodac.folding.test.integration.util.TestGenerator;
import me.zodac.folding.test.integration.util.TestStats;
import me.zodac.folding.test.integration.util.db.DatabaseUtils;
import me.zodac.folding.test.integration.util.rest.request.TeamUtils;
import me.zodac.folding.test.integration.util.rest.request.UserUtils;
import me.zodac.folding.test.integration.util.rest.response.HttpResponseHeaderUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@code Team Competition} {@link HistoricStats} for {@link Team}s.
 */
class HistoricTeamStatsTest {

    private static final HistoricStatsRequestSender HISTORIC_STATS_REQUEST_SENDER = HistoricStatsRequestSender.createWithUrl(FOLDING_URL);

    @BeforeAll
    static void setUp() throws FoldingRestException {
        SystemCleaner.cleanSystemForComplexTests();
    }

    @AfterAll
    static void tearDown() throws FoldingRestException {
        SystemCleaner.cleanSystemForComplexTests();
    }

    @Test
    void whenGettingHourlyStats_andValidTeamIdIsGiven_andAllUsersHaveNoStats_thenNoStatsAreReturned_andResponseHas200Status()
        throws FoldingRestException {
        final Team team = TeamUtils.create(TestGenerator.generateTeam());
        UserUtils.create(TestGenerator.generateUserWithTeamId(team.id()));

        final HttpResponse<String> response = HISTORIC_STATS_REQUEST_SENDER.getHourlyTeamStats(team.id(), Year.parse("2020"), Month.of(4), 12);
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final Collection<HistoricStats> results = HistoricStatsResponseParser.getHourlyTeamStats(response);
        assertThat(results)
            .isEmpty();
    }

    @Test
    void whenGettingHourlyStats_andValidTeamIdIsGiven_andUserHasSomeStats_thenStatsAreReturned_andResponseHas200Status() throws FoldingRestException {
        final Team team = TeamUtils.create(TestGenerator.generateTeam());
        final int userId = UserUtils.create(TestGenerator.generateUserWithTeamId(team.id())).id();
        DatabaseUtils.insertStats("user_tc_stats_hourly",
            TestStats.create(userId, "2020-04-11 23:00:00", 0L, 0L, 0),
            TestStats.create(userId, "2020-04-12 14:00:00", 100L, 1_000L, 10)
        );

        final HttpResponse<String> response = HISTORIC_STATS_REQUEST_SENDER.getHourlyTeamStats(team.id(), Year.parse("2020"), Month.of(4), 12);
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final Collection<HistoricStats> results = HistoricStatsResponseParser.getHourlyTeamStats(response);
        assertThat(results)
            .hasSize(1);
    }

    @Test
    void whenGettingHourlyStats_andTeamHasMultipleStatsForSingleUser_thenEachStatsEntryIsDiffedFromPreviousHour_andResponseHas200Status()
        throws FoldingRestException {
        final Team team = TeamUtils.create(TestGenerator.generateTeam());
        final int userId = UserUtils.create(TestGenerator.generateUserWithTeamId(team.id())).id();
        DatabaseUtils.insertStats("user_tc_stats_hourly",
            TestStats.create(userId, "2020-04-11 23:00:00", 0L, 0L, 0),
            TestStats.create(userId, "2020-04-12 13:00:00", 20L, 200L, 2),
            TestStats.create(userId, "2020-04-12 14:00:00", 100L, 1_000L, 10)
        );

        final HttpResponse<String> response = HISTORIC_STATS_REQUEST_SENDER.getHourlyTeamStats(team.id(), Year.parse("2020"), Month.of(4), 12);
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final List<HistoricStats> results = new ArrayList<>(HistoricStatsResponseParser.getHourlyTeamStats(response));
        assertThat(results)
            .hasSize(2);

        final HistoricStats result = results.get(1); // Ignore first result, as it is the "initial" stats for the team
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
        final Team team = TeamUtils.create(TestGenerator.generateTeam());
        final int userId = UserUtils.create(TestGenerator.generateUserWithTeamId(team.id())).id();
        DatabaseUtils.insertStats("user_tc_stats_hourly",
            TestStats.create(userId, "2020-04-11 23:00:00", 0L, 0L, 0),
            TestStats.create(userId, "2020-04-12 13:00:00", 50L, 500L, 5),
            TestStats.create(userId, "2020-04-12 14:00:00", 100L, 1_000L, 10),
            TestStats.create(userId, "2020-04-12 14:30:00", 110L, 1_100L, 11)
        );

        final HttpResponse<String> response = HISTORIC_STATS_REQUEST_SENDER.getHourlyTeamStats(team.id(), Year.parse("2020"), Month.of(4), 12);
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final List<HistoricStats> results = new ArrayList<>(HistoricStatsResponseParser.getHourlyTeamStats(response));
        assertThat(results)
            .hasSize(2);

        final HistoricStats result = results.get(1); // Ignore first result, as it is the "initial" stats for the team
        assertThat(result.points())
            .isEqualTo(60L);
        assertThat(result.multipliedPoints())
            .isEqualTo(600L);
        assertThat(result.units())
            .isEqualTo(6);
    }

    @Test
    void whenGettingHourlyStats_andTeamHasMultipleUsersInSameHour_thenCombinedStatsForAllUsersAreReturned_andResponseHas200Status()
        throws FoldingRestException {
        final Team team = TeamUtils.create(TestGenerator.generateTeam());
        final int firstUserId = UserUtils.create(TestGenerator.generateUserWithTeamIdAndCategory(team.id(), Category.AMD_GPU)).id();
        final int secondUserId = UserUtils.create(TestGenerator.generateUserWithTeamIdAndCategory(team.id(), Category.NVIDIA_GPU)).id();
        DatabaseUtils.insertStats("user_tc_stats_hourly",
            TestStats.create(firstUserId, "2020-04-11 23:00:00", 0L, 0L, 0),
            TestStats.create(secondUserId, "2020-04-11 23:00:00", 0L, 0L, 0),
            TestStats.create(firstUserId, "2020-04-12 13:00:00", 50L, 500L, 5),
            TestStats.create(secondUserId, "2020-04-12 13:00:00", 50L, 500L, 5),
            TestStats.create(firstUserId, "2020-04-12 14:00:00", 100L, 1_000L, 10),
            TestStats.create(firstUserId, "2020-04-12 14:30:00", 110L, 1_100L, 11),
            TestStats.create(secondUserId, "2020-04-12 14:30:00", 200L, 2_000L, 20)
        );

        final HttpResponse<String> response = HISTORIC_STATS_REQUEST_SENDER.getHourlyTeamStats(team.id(), Year.parse("2020"), Month.of(4), 12);
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final List<HistoricStats> results = new ArrayList<>(HistoricStatsResponseParser.getHourlyTeamStats(response));
        assertThat(results)
            .hasSize(2);

        final HistoricStats result = results.get(1); // Ignore first result, as it is the "initial" stats for the team
        assertThat(result.points())
            .isEqualTo(210L);
        assertThat(result.multipliedPoints())
            .isEqualTo(2_100L);
        assertThat(result.units())
            .isEqualTo(21);
    }

    @Test
    void whenGettingHourlyStats_givenRequestUsesPreviousEntityTag_andStatsHaveNotChanged_thenResponseHas304Status_andNoBody()
        throws FoldingRestException {
        final Team team = TeamUtils.create(TestGenerator.generateTeam());
        final int userId = UserUtils.create(TestGenerator.generateUserWithTeamId(team.id())).id();
        DatabaseUtils.insertStats("user_tc_stats_hourly",
            TestStats.create(userId, "2020-04-12 14:00:00", 100L, 1_000L, 10)
        );

        final HttpResponse<String> response = HISTORIC_STATS_REQUEST_SENDER.getHourlyTeamStats(team.id(), Year.parse("2020"), Month.of(4), 12);
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final String eTag = HttpResponseHeaderUtils.getEntityTag(response);

        final HttpResponse<String> cachedResponse =
            HISTORIC_STATS_REQUEST_SENDER.getHourlyTeamStats(team.id(), Year.parse("2020"), Month.of(4), 12, eTag);
        assertThat(cachedResponse.statusCode())
            .as("Expected second request to have a 304_NOT_MODIFIED HTTP response")
            .isEqualTo(HttpURLConnection.HTTP_NOT_MODIFIED);

        assertThat(HistoricStatsResponseParser.getHourlyTeamStats(cachedResponse))
            .as("Expected cached response to have the same content as the non-cached response")
            .isNull();
    }

    @Test
    void whenGettingHourlyStats_givenNonExistingTeamId_thenResponseHas404Status() throws FoldingRestException {
        final HttpResponse<String> response =
            HISTORIC_STATS_REQUEST_SENDER.getHourlyTeamStats(TestConstants.NON_EXISTING_ID, Year.parse("2020"), Month.of(4), 12);
        assertThat(response.statusCode())
            .as("Did not receive a 404_NOT_FOUND HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_NOT_FOUND);
    }

    @Test
    void whenGettingHourlyStats_givenInvalidTeamId_thenResponseHasA400Status() throws IOException, InterruptedException {
        final HttpRequest request = HttpRequest.newBuilder()
            .GET()
            .uri(URI.create(FOLDING_URL + "/historic/teams/" + INVALID_FORMAT_ID + '/' + Year.parse("2020") + '/' + Month.of(4) + '/' + 12))
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
        final Team team = TeamUtils.create(TestGenerator.generateTeam());
        UserUtils.create(TestGenerator.generateUserWithTeamId(team.id()));
        final int invalidDay = 35;

        final HttpResponse<String> response =
            HISTORIC_STATS_REQUEST_SENDER.getHourlyTeamStats(team.id(), Year.parse("2020"), Month.of(4), invalidDay);
        assertThat(response.statusCode())
            .as("Did not receive a 400_BAD_REQUEST HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);
    }

    @Test
    void whenGettingDailyStats_andValidTeamIdIsGiven_andAllUsersHaveNoStats_thenNoStatsAreReturned_andResponseHas200Status()
        throws FoldingRestException {
        final Team team = TeamUtils.create(TestGenerator.generateTeam());
        UserUtils.create(TestGenerator.generateUserWithTeamId(team.id()));

        final HttpResponse<String> response = HISTORIC_STATS_REQUEST_SENDER.getDailyTeamStats(team.id(), Year.parse("2020"), Month.of(4));
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final Collection<HistoricStats> results = HistoricStatsResponseParser.getDailyTeamStats(response);
        assertThat(results)
            .isEmpty();
    }

    @Test
    void whenGettingDailyStats_andValidTeamIdIsGiven_andUserHasSomeStats_thenStatsAreReturned_andResponseHas200Status() throws FoldingRestException {
        final Team team = TeamUtils.create(TestGenerator.generateTeam());
        final int userId = UserUtils.create(TestGenerator.generateUserWithTeamId(team.id())).id();
        DatabaseUtils.insertStats("user_tc_stats_hourly",
            TestStats.create(userId, "2020-04-12 14:00:00", 100L, 1_000L, 10)
        );

        final HttpResponse<String> response = HISTORIC_STATS_REQUEST_SENDER.getDailyTeamStats(team.id(), Year.parse("2020"), Month.of(4));
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final Collection<HistoricStats> results = HistoricStatsResponseParser.getDailyTeamStats(response);
        assertThat(results)
            .hasSize(1);
    }

    @Test
    void whenGettingDailyStats_andTeamHasMultipleStatsForSameUser_thenEachStatsEntryIsDiffedFromPreviousDay_andResponseHas200Status()
        throws FoldingRestException {
        final Team team = TeamUtils.create(TestGenerator.generateTeam());
        final int userId = UserUtils.create(TestGenerator.generateUserWithTeamId(team.id())).id();
        DatabaseUtils.insertStats("user_tc_stats_hourly",
            TestStats.create(userId, "2020-04-12 13:00:00", 20L, 200L, 2),
            TestStats.create(userId, "2020-04-13 13:00:00", 100L, 1_000L, 10)
        );

        final HttpResponse<String> response = HISTORIC_STATS_REQUEST_SENDER.getDailyTeamStats(team.id(), Year.parse("2020"), Month.of(4));
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final List<HistoricStats> results = new ArrayList<>(HistoricStatsResponseParser.getDailyTeamStats(response));
        assertThat(results)
            .hasSize(2);

        final HistoricStats result = results.get(1); // Ignore first result, as it is the "initial" stats for the team
        assertThat(result.points())
            .isEqualTo(80L);
        assertThat(result.multipliedPoints())
            .isEqualTo(800L);
        assertThat(result.units())
            .isEqualTo(8);
    }

    @Test
    void whenGettingDailyStats_andUserHasMultipleStatsInSameDay_thenMaxStatsInDayAreReturned_andResponseHas200Status() throws FoldingRestException {
        final Team team = TeamUtils.create(TestGenerator.generateTeam());
        final int userId = UserUtils.create(TestGenerator.generateUserWithTeamId(team.id())).id();
        DatabaseUtils.insertStats("user_tc_stats_hourly",
            TestStats.create(userId, "2020-04-12 13:00:00", 10L, 100L, 1),
            TestStats.create(userId, "2020-04-13 14:00:00", 50L, 500L, 5),
            TestStats.create(userId, "2020-04-13 15:00:00", 110L, 1_100L, 11)
        );

        final HttpResponse<String> response = HISTORIC_STATS_REQUEST_SENDER.getDailyTeamStats(team.id(), Year.parse("2020"), Month.of(4));
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final List<HistoricStats> results = new ArrayList<>(HistoricStatsResponseParser.getDailyTeamStats(response));
        assertThat(results)
            .hasSize(2);

        final HistoricStats result = results.get(1); // Ignore first result, as it is the "initial" stats for the team
        assertThat(result.points())
            .isEqualTo(100L);
        assertThat(result.multipliedPoints())
            .isEqualTo(1_000L);
        assertThat(result.units())
            .isEqualTo(10);
    }

    @Test
    void whenGettingDailyStats_andTeamHasMultipleUsersInSameDay_thenCombinedStatsForAllUsersAreReturned_andResponseHas200Status()
        throws FoldingRestException {
        final Team team = TeamUtils.create(TestGenerator.generateTeam());
        final int firstUserId = UserUtils.create(TestGenerator.generateUserWithTeamIdAndCategory(team.id(), Category.AMD_GPU)).id();
        final int secondUserId = UserUtils.create(TestGenerator.generateUserWithTeamIdAndCategory(team.id(), Category.NVIDIA_GPU)).id();
        DatabaseUtils.insertStats("user_tc_stats_hourly",
            TestStats.create(firstUserId, "2020-04-12 13:00:00", 50L, 500L, 5),
            TestStats.create(secondUserId, "2020-04-12 13:00:00", 50L, 500L, 5),
            TestStats.create(firstUserId, "2020-04-13 14:00:00", 100L, 1_000L, 10),
            TestStats.create(firstUserId, "2020-04-13 14:30:00", 110L, 1_100L, 11),
            TestStats.create(secondUserId, "2020-04-13 14:30:00", 200L, 2_000L, 20)
        );

        final HttpResponse<String> response = HISTORIC_STATS_REQUEST_SENDER.getDailyTeamStats(team.id(), Year.parse("2020"), Month.of(4));
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final List<HistoricStats> results = new ArrayList<>(HistoricStatsResponseParser.getDailyTeamStats(response));
        assertThat(results)
            .hasSize(2);

        final HistoricStats result = results.get(1); // Ignore first result, as it is the "initial" stats for the team
        assertThat(result.points())
            .isEqualTo(210L);
        assertThat(result.multipliedPoints())
            .isEqualTo(2_100L);
        assertThat(result.units())
            .isEqualTo(21);
    }

    @Test
    void whenGettingDailyStats_givenRequestUsesPreviousEntityTag_andStatsHaveNotChanged_thenResponseHas304Status_andNoBody()
        throws FoldingRestException {
        final Team team = TeamUtils.create(TestGenerator.generateTeam());
        final int userId = UserUtils.create(TestGenerator.generateUserWithTeamId(team.id())).id();
        DatabaseUtils.insertStats("user_tc_stats_hourly",
            TestStats.create(userId, "2020-04-12 14:00:00", 100L, 1_000L, 10)
        );

        final HttpResponse<String> response = HISTORIC_STATS_REQUEST_SENDER.getDailyTeamStats(team.id(), Year.parse("2020"), Month.of(4));
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final String eTag = HttpResponseHeaderUtils.getEntityTag(response);

        final HttpResponse<String> cachedResponse =
            HISTORIC_STATS_REQUEST_SENDER.getDailyTeamStats(team.id(), Year.parse("2020"), Month.of(4), eTag);
        assertThat(cachedResponse.statusCode())
            .as("Expected second request to have a 304_NOT_MODIFIED HTTP response")
            .isEqualTo(HttpURLConnection.HTTP_NOT_MODIFIED);

        assertThat(HistoricStatsResponseParser.getDailyTeamStats(cachedResponse))
            .as("Expected cached response to have the same content as the non-cached response")
            .isNull();
    }

    @Test
    void whenGettingDailyStats_givenNonExistingTeamId_thenResponseHas404Status() throws FoldingRestException {
        final HttpResponse<String> response =
            HISTORIC_STATS_REQUEST_SENDER.getDailyTeamStats(TestConstants.NON_EXISTING_ID, Year.parse("2020"), Month.of(4));
        assertThat(response.statusCode())
            .as("Did not receive a 404_NOT_FOUND HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_NOT_FOUND);
    }

    @Test
    void whenGettingDailyStats_givenInvalidTeamId_thenResponseHas400Status() throws IOException, InterruptedException {
        final HttpRequest request = HttpRequest.newBuilder()
            .GET()
            .uri(URI.create(FOLDING_URL + "/historic/teams/" + INVALID_FORMAT_ID + '/' + Year.parse("2020") + '/' + Month.of(4)))
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
        final Team team = TeamUtils.create(TestGenerator.generateTeam());
        UserUtils.create(TestGenerator.generateUserWithTeamId(team.id()));
        final int invalidMonth = 25;

        final HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
            .GET()
            .uri(URI.create(FOLDING_URL + "/historic/teams/" + team.id() + "/2020/" + invalidMonth))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentTypeValue());

        final HttpRequest request = requestBuilder.build();

        final HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode())
            .as("Did not receive a 400_BAD_REQUEST HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);
    }

    @Test
    void whenGettingMonthlyStats_andValidTeamIdIsGiven_andAllUsersHaveNoStats_thenNoStatsAreReturned_andResponseHas200Status()
        throws FoldingRestException {
        final Team team = TeamUtils.create(TestGenerator.generateTeam());
        UserUtils.create(TestGenerator.generateUserWithTeamId(team.id()));

        final HttpResponse<String> response = HISTORIC_STATS_REQUEST_SENDER.getMonthlyTeamStats(team.id(), Year.parse("2020"));
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final Collection<HistoricStats> results = HistoricStatsResponseParser.getMonthlyTeamStats(response);
        assertThat(results)
            .isEmpty();
    }

    @Test
    void whenGettingMonthlyStats_andValidTeamIdIsGiven_andUserHasSomeStats_thenStatsAreReturned_andResponseHas200Status()
        throws FoldingRestException {
        final Team team = TeamUtils.create(TestGenerator.generateTeam());
        final int userId = UserUtils.create(TestGenerator.generateUserWithTeamId(team.id())).id();
        DatabaseUtils.insertStats("user_tc_stats_hourly",
            TestStats.create(userId, "2020-04-12 14:00:00", 100L, 1_000L, 10)
        );

        final HttpResponse<String> response = HISTORIC_STATS_REQUEST_SENDER.getMonthlyTeamStats(team.id(), Year.parse("2020"));
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final Collection<HistoricStats> results = HistoricStatsResponseParser.getMonthlyTeamStats(response);
        assertThat(results)
            .hasSize(1);
    }

    @Test
    void whenGettingMonthlyStats_andUserHasMultipleStats_thenEachStatsEntryIsDiffedFromPreviousMonth_andResponseHas200Status()
        throws FoldingRestException {
        final Team team = TeamUtils.create(TestGenerator.generateTeam());
        final int userId = UserUtils.create(TestGenerator.generateUserWithTeamId(team.id())).id();
        DatabaseUtils.insertStats("user_tc_stats_hourly",
            TestStats.create(userId, "2020-03-12 13:00:00", 20L, 200L, 2), // No diff from this result, since stats are reset each month
            TestStats.create(userId, "2020-04-12 13:00:00", 100L, 1_000L, 10)
        );

        final HttpResponse<String> response = HISTORIC_STATS_REQUEST_SENDER.getMonthlyTeamStats(team.id(), Year.parse("2020"));
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final List<HistoricStats> results = new ArrayList<>(HistoricStatsResponseParser.getMonthlyTeamStats(response));
        assertThat(results)
            .hasSize(2);

        final HistoricStats result = results.get(1); // Ignore first result, as it is the "initial" stats for the team
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
        final Team team = TeamUtils.create(TestGenerator.generateTeam());
        final int userId = UserUtils.create(TestGenerator.generateUserWithTeamId(team.id())).id();
        DatabaseUtils.insertStats("user_tc_stats_hourly",
            TestStats.create(userId, "2020-03-12 13:00:00", 50L, 500L, 5), // No diff from this result, since stats are reset each month
            TestStats.create(userId, "2020-04-13 14:00:00", 100L, 1_000L, 10),
            TestStats.create(userId, "2020-04-13 15:00:00", 110L, 1_100L, 11)
        );

        final HttpResponse<String> response = HISTORIC_STATS_REQUEST_SENDER.getMonthlyTeamStats(team.id(), Year.parse("2020"));
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final List<HistoricStats> results = new ArrayList<>(HistoricStatsResponseParser.getMonthlyTeamStats(response));
        assertThat(results)
            .hasSize(2);

        final HistoricStats result = results.get(1); // Ignore first result, as it is the "initial" stats for the team
        assertThat(result.points())
            .isEqualTo(110L);
        assertThat(result.multipliedPoints())
            .isEqualTo(1_100L);
        assertThat(result.units())
            .isEqualTo(11);
    }

    @Test
    void whenGettingMonthlyStats_andTeamHasMultipleUsersInSameMonth_thenCombinedStatsForAllUsersAreReturned_andResponseHas200Status()
        throws FoldingRestException {
        final Team team = TeamUtils.create(TestGenerator.generateTeam());
        final int firstUserId = UserUtils.create(TestGenerator.generateUserWithTeamIdAndCategory(team.id(), Category.AMD_GPU)).id();
        final int secondUserId = UserUtils.create(TestGenerator.generateUserWithTeamIdAndCategory(team.id(), Category.NVIDIA_GPU)).id();
        DatabaseUtils.insertStats("user_tc_stats_hourly",
            TestStats.create(firstUserId, "2020-03-12 13:00:00", 50L, 500L, 5), // No diff from this result, since stats are reset each month
            TestStats.create(secondUserId, "2020-03-12 13:00:00", 50L, 500L, 5),
            TestStats.create(firstUserId, "2020-04-13 14:00:00", 100L, 1_000L, 10),
            TestStats.create(firstUserId, "2020-04-13 14:30:00", 110L, 1_100L, 11),
            TestStats.create(secondUserId, "2020-04-13 14:30:00", 200L, 2_000L, 20)
        );

        final HttpResponse<String> response = HISTORIC_STATS_REQUEST_SENDER.getMonthlyTeamStats(team.id(), Year.parse("2020"));
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final List<HistoricStats> results = new ArrayList<>(HistoricStatsResponseParser.getHourlyTeamStats(response));
        assertThat(results)
            .hasSize(2);

        final HistoricStats result = results.get(1); // Ignore first result, as it is the "initial" stats for the team
        assertThat(result.points())
            .isEqualTo(310L);
        assertThat(result.multipliedPoints())
            .isEqualTo(3_100L);
        assertThat(result.units())
            .isEqualTo(31);
    }

    @Test
    void whenGettingMonthlyStats_givenRequestUsesPreviousEntityTag_andStatsHaveNotChanged_thenResponseHas304Status_andNoBody()
        throws FoldingRestException {
        final Team team = TeamUtils.create(TestGenerator.generateTeam());
        final int userId = UserUtils.create(TestGenerator.generateUserWithTeamId(team.id())).id();
        DatabaseUtils.insertStats("user_tc_stats_hourly",
            TestStats.create(userId, "2020-04-12 14:00:00", 100L, 1_000L, 10)
        );

        final HttpResponse<String> response = HISTORIC_STATS_REQUEST_SENDER.getMonthlyTeamStats(team.id(), Year.parse("2020"));
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final String eTag = HttpResponseHeaderUtils.getEntityTag(response);

        final HttpResponse<String> cachedResponse = HISTORIC_STATS_REQUEST_SENDER.getMonthlyTeamStats(team.id(), Year.parse("2020"), eTag);
        assertThat(cachedResponse.statusCode())
            .as("Expected second request to have a 304_NOT_MODIFIED HTTP response")
            .isEqualTo(HttpURLConnection.HTTP_NOT_MODIFIED);

        assertThat(HistoricStatsResponseParser.getMonthlyTeamStats(cachedResponse))
            .as("Expected cached response to have the same content as the non-cached response")
            .isNull();
    }

    @Test
    void whenGettingMonthlyStats_givenNonExistingTeamId_thenResponseHas404Status() throws FoldingRestException {
        final HttpResponse<String> response = HISTORIC_STATS_REQUEST_SENDER.getMonthlyTeamStats(TestConstants.NON_EXISTING_ID, Year.parse("2020"));
        assertThat(response.statusCode())
            .as("Did not receive a 404_NOT_FOUND HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_NOT_FOUND);
    }

    @Test
    void whenGettingMonthlyStats_givenInvalidTeamId_thenResponseHas400Status() throws IOException, InterruptedException {
        final HttpRequest request = HttpRequest.newBuilder()
            .GET()
            .uri(URI.create(FOLDING_URL + "/historic/teams/" + INVALID_FORMAT_ID + '/' + Year.parse("2020")))
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
        final Team team = TeamUtils.create(TestGenerator.generateTeam());
        UserUtils.create(TestGenerator.generateUserWithTeamId(team.id()));

        final HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
            .GET()
            .uri(URI.create(FOLDING_URL + "/historic/teams/" + team.id() + "/invalidYear"))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentTypeValue());

        final HttpRequest request = requestBuilder.build();

        final HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode())
            .as("Did not receive a 400_BAD_REQUEST HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);
    }
}

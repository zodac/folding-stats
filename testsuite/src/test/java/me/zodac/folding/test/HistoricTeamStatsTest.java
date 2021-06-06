package me.zodac.folding.test;

import me.zodac.folding.api.tc.Category;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.client.java.request.HistoricStatsRequestSender;
import me.zodac.folding.client.java.response.HistoricStatsResponseParser;
import me.zodac.folding.rest.api.exception.FoldingRestException;
import me.zodac.folding.rest.api.tc.historic.HistoricStats;
import me.zodac.folding.test.utils.TestConstants;
import me.zodac.folding.test.utils.TestStats;
import me.zodac.folding.test.utils.db.DatabaseUtils;
import me.zodac.folding.test.utils.rest.request.TeamUtils;
import me.zodac.folding.test.utils.rest.request.UserUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

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

import static me.zodac.folding.test.utils.SystemCleaner.cleanSystemForComplexTests;
import static me.zodac.folding.test.utils.TestConstants.FOLDING_URL;
import static me.zodac.folding.test.utils.TestConstants.HTTP_CLIENT;
import static me.zodac.folding.test.utils.TestGenerator.generateTeam;
import static me.zodac.folding.test.utils.TestGenerator.generateUserWithTeamId;
import static me.zodac.folding.test.utils.TestGenerator.generateUserWithTeamIdAndCategory;
import static me.zodac.folding.test.utils.rest.response.HttpResponseHeaderUtils.getETag;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for <code>Team Competition</code> {@link HistoricStats} for {@link me.zodac.folding.api.tc.Team}s.
 */
class HistoricTeamStatsTest {

    private static final HistoricStatsRequestSender HISTORIC_STATS_REQUEST_SENDER = HistoricStatsRequestSender.create(FOLDING_URL);

    @BeforeAll
    static void setUp() throws FoldingRestException {
        cleanSystemForComplexTests();
    }

    @Test
    void whenGettingHourlyStats_andValidTeamIdIsGiven_andAllUsersHaveNoStats_thenNoStatsAreReturned_andResponseHas200Status() throws FoldingRestException {
        final Team team = TeamUtils.create(generateTeam());
        UserUtils.create(generateUserWithTeamId(team.getId()));

        final HttpResponse<String> response = HISTORIC_STATS_REQUEST_SENDER.getHourlyTeamStats(team.getId(), Year.parse("2020"), Month.of(4), 12);
        assertThat(response.statusCode())
                .as("Did not receive a 200_OK HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_OK);

        final Collection<HistoricStats> results = HistoricStatsResponseParser.getHourlyTeamStats(response);
        assertThat(results)
                .isEmpty();
    }

    @Test
    void whenGettingHourlyStats_andValidTeamIdIsGiven_andUserHasSomeStats_thenStatsAreReturned_andResponseHas200Status() throws FoldingRestException {
        final Team team = TeamUtils.create(generateTeam());
        final int userId = UserUtils.create(generateUserWithTeamId(team.getId())).getId();
        DatabaseUtils.insertStats("user_tc_stats_hourly",
                TestStats.create(userId, "2020-04-11 23:00:00", 0L, 0L, 0),
                TestStats.create(userId, "2020-04-12 14:00:00", 100L, 1_000L, 10)
        );

        final HttpResponse<String> response = HISTORIC_STATS_REQUEST_SENDER.getHourlyTeamStats(team.getId(), Year.parse("2020"), Month.of(4), 12);
        assertThat(response.statusCode())
                .as("Did not receive a 200_OK HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_OK);

        final Collection<HistoricStats> results = HistoricStatsResponseParser.getHourlyTeamStats(response);
        assertThat(results)
                .hasSize(1);
    }

    @Test
    void whenGettingHourlyStats_andTeamHasMultipleStatsForSingleUser_thenEachStatsEntryIsADiffFromPreviousHour_andResponseHas200Status() throws FoldingRestException {
        final Team team = TeamUtils.create(generateTeam());
        final int userId = UserUtils.create(generateUserWithTeamId(team.getId())).getId();
        DatabaseUtils.insertStats("user_tc_stats_hourly",
                TestStats.create(userId, "2020-04-11 23:00:00", 0L, 0L, 0),
                TestStats.create(userId, "2020-04-12 13:00:00", 20L, 200L, 2),
                TestStats.create(userId, "2020-04-12 14:00:00", 100L, 1_000L, 10)
        );

        final HttpResponse<String> response = HISTORIC_STATS_REQUEST_SENDER.getHourlyTeamStats(team.getId(), Year.parse("2020"), Month.of(4), 12);
        assertThat(response.statusCode())
                .as("Did not receive a 200_OK HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_OK);

        final List<HistoricStats> results = new ArrayList<>(HistoricStatsResponseParser.getHourlyTeamStats(response));
        assertThat(results)
                .hasSize(2);

        final HistoricStats result = results.get(1); // Ignore first result, as it is the "base" stats for the team
        assertThat(result.getPoints())
                .isEqualTo(80L);
        assertThat(result.getMultipliedPoints())
                .isEqualTo(800L);
        assertThat(result.getUnits())
                .isEqualTo(8);
    }

    @Test
    void whenGettingHourlyStats_andUserHasMultipleStatsInSameHour_thenMaxStatsInHourAreReturned_andResponseHas200Status() throws FoldingRestException {
        final Team team = TeamUtils.create(generateTeam());
        final int userId = UserUtils.create(generateUserWithTeamId(team.getId())).getId();
        DatabaseUtils.insertStats("user_tc_stats_hourly",
                TestStats.create(userId, "2020-04-11 23:00:00", 0L, 0L, 0),
                TestStats.create(userId, "2020-04-12 13:00:00", 50L, 500L, 5),
                TestStats.create(userId, "2020-04-12 14:00:00", 100L, 1_000L, 10),
                TestStats.create(userId, "2020-04-12 14:30:00", 110L, 1_100L, 11)
        );

        final HttpResponse<String> response = HISTORIC_STATS_REQUEST_SENDER.getHourlyTeamStats(team.getId(), Year.parse("2020"), Month.of(4), 12);
        assertThat(response.statusCode())
                .as("Did not receive a 200_OK HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_OK);

        final List<HistoricStats> results = new ArrayList<>(HistoricStatsResponseParser.getHourlyTeamStats(response));
        assertThat(results)
                .hasSize(2);

        final HistoricStats result = results.get(1); // Ignore first result, as it is the "base" stats for the team
        assertThat(result.getPoints())
                .isEqualTo(60L);
        assertThat(result.getMultipliedPoints())
                .isEqualTo(600L);
        assertThat(result.getUnits())
                .isEqualTo(6);
    }

    @Test
    void whenGettingHourlyStats_andTeamHasMultipleUsersInSameHour_thenCombinedStatsForAllUsersAreReturned_andResponseHas200Status() throws FoldingRestException {
        final Team team = TeamUtils.create(generateTeam());
        final int firstUserId = UserUtils.create(generateUserWithTeamIdAndCategory(team.getId(), Category.AMD_GPU)).getId();
        final int secondUserId = UserUtils.create(generateUserWithTeamIdAndCategory(team.getId(), Category.NVIDIA_GPU)).getId();
        DatabaseUtils.insertStats("user_tc_stats_hourly",
                TestStats.create(firstUserId, "2020-04-11 23:00:00", 0L, 0L, 0),
                TestStats.create(secondUserId, "2020-04-11 23:00:00", 0L, 0L, 0),
                TestStats.create(firstUserId, "2020-04-12 13:00:00", 50L, 500L, 5),
                TestStats.create(secondUserId, "2020-04-12 13:00:00", 50L, 500L, 5),
                TestStats.create(firstUserId, "2020-04-12 14:00:00", 100L, 1_000L, 10),
                TestStats.create(firstUserId, "2020-04-12 14:30:00", 110L, 1_100L, 11),
                TestStats.create(secondUserId, "2020-04-12 14:30:00", 200L, 2_000L, 20)
        );

        final HttpResponse<String> response = HISTORIC_STATS_REQUEST_SENDER.getHourlyTeamStats(team.getId(), Year.parse("2020"), Month.of(4), 12);
        assertThat(response.statusCode())
                .as("Did not receive a 200_OK HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_OK);

        final List<HistoricStats> results = new ArrayList<>(HistoricStatsResponseParser.getHourlyTeamStats(response));
        assertThat(results)
                .hasSize(2);

        final HistoricStats result = results.get(1); // Ignore first result, as it is the "base" stats for the team
        assertThat(result.getPoints())
                .isEqualTo(210L);
        assertThat(result.getMultipliedPoints())
                .isEqualTo(2_100L);
        assertThat(result.getUnits())
                .isEqualTo(21);
    }

    @Test
    void whenGettingHourlyStats_givenRequestUsesPreviousETag_andStatsHaveNotChanged_thenResponseHasA304Status_andNoBody() throws FoldingRestException {
        final Team team = TeamUtils.create(generateTeam());
        final int userId = UserUtils.create(generateUserWithTeamId(team.getId())).getId();
        DatabaseUtils.insertStats("user_tc_stats_hourly",
                TestStats.create(userId, "2020-04-12 14:00:00", 100L, 1_000L, 10)
        );

        final HttpResponse<String> response = HISTORIC_STATS_REQUEST_SENDER.getHourlyTeamStats(team.getId(), Year.parse("2020"), Month.of(4), 12);
        assertThat(response.statusCode())
                .as("Did not receive a 200_OK HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_OK);

        final String eTag = getETag(response);

        final HttpResponse<String> cachedResponse = HISTORIC_STATS_REQUEST_SENDER.getHourlyTeamStats(team.getId(), Year.parse("2020"), Month.of(4), 12, eTag);
        assertThat(cachedResponse.statusCode())
                .as("Expected second request to have a 304_NOT_MODIFIED HTTP response")
                .isEqualTo(HttpURLConnection.HTTP_NOT_MODIFIED);

        assertThat(HistoricStatsResponseParser.getHourlyTeamStats(cachedResponse))
                .as("Expected cached response to have the same content as the non-cached response")
                .isNull();
    }

    @Test
    void whenGettingHourlyStats_andInvalidTeamIdIsGiven_thenResponseHasA404Status() throws FoldingRestException {
        final HttpResponse<String> response = HISTORIC_STATS_REQUEST_SENDER.getHourlyTeamStats(TestConstants.INVALID_ID, Year.parse("2020"), Month.of(4), 12);
        assertThat(response.statusCode())
                .as("Did not receive a 404_NOT_FOUND HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_NOT_FOUND);
    }

    @Test
    void whenGettingHourlyStats_andInvalidDateIsGiven_thenResponseHasA400Status() throws FoldingRestException {
        final Team team = TeamUtils.create(generateTeam());
        UserUtils.create(generateUserWithTeamId(team.getId()));
        final int invalidDay = 35;

        final HttpResponse<String> response = HISTORIC_STATS_REQUEST_SENDER.getHourlyTeamStats(team.getId(), Year.parse("2020"), Month.of(4), invalidDay);
        assertThat(response.statusCode())
                .as("Did not receive a 400_BAD_REQUEST HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);
    }

    @Test
    void whenGettingDailyStats_andValidTeamIdIsGiven_andAllUsersHaveNoStats_thenNoStatsAreReturned_andResponseHas200Status() throws FoldingRestException {
        final Team team = TeamUtils.create(generateTeam());
        UserUtils.create(generateUserWithTeamId(team.getId()));

        final HttpResponse<String> response = HISTORIC_STATS_REQUEST_SENDER.getDailyTeamStats(team.getId(), Year.parse("2020"), Month.of(4));
        assertThat(response.statusCode())
                .as("Did not receive a 200_OK HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_OK);

        final Collection<HistoricStats> results = HistoricStatsResponseParser.getDailyTeamStats(response);
        assertThat(results)
                .isEmpty();
    }

    @Test
    void whenGettingDailyStats_andValidTeamIdIsGiven_andUserHasSomeStats_thenStatsAreReturned_andResponseHas200Status() throws FoldingRestException {
        final Team team = TeamUtils.create(generateTeam());
        final int userId = UserUtils.create(generateUserWithTeamId(team.getId())).getId();
        DatabaseUtils.insertStats("user_tc_stats_hourly",
                TestStats.create(userId, "2020-04-12 14:00:00", 100L, 1_000L, 10)
        );

        final HttpResponse<String> response = HISTORIC_STATS_REQUEST_SENDER.getDailyTeamStats(team.getId(), Year.parse("2020"), Month.of(4));
        assertThat(response.statusCode())
                .as("Did not receive a 200_OK HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_OK);

        final Collection<HistoricStats> results = HistoricStatsResponseParser.getDailyTeamStats(response);
        assertThat(results)
                .hasSize(1);
    }

    @Test
    void whenGettingDailyStats_andTeamHasMultipleStatsForSameUser_thenEachStatsEntryIsADiffFromPreviousDay_andResponseHas200Status() throws FoldingRestException {
        final Team team = TeamUtils.create(generateTeam());
        final int userId = UserUtils.create(generateUserWithTeamId(team.getId())).getId();
        DatabaseUtils.insertStats("user_tc_stats_hourly",
                TestStats.create(userId, "2020-04-12 13:00:00", 20L, 200L, 2),
                TestStats.create(userId, "2020-04-13 13:00:00", 100L, 1_000L, 10)
        );

        final HttpResponse<String> response = HISTORIC_STATS_REQUEST_SENDER.getDailyTeamStats(team.getId(), Year.parse("2020"), Month.of(4));
        assertThat(response.statusCode())
                .as("Did not receive a 200_OK HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_OK);

        final List<HistoricStats> results = new ArrayList<>(HistoricStatsResponseParser.getDailyTeamStats(response));
        assertThat(results)
                .hasSize(2);

        final HistoricStats result = results.get(1); // Ignore first result, as it is the "base" stats for the team
        assertThat(result.getPoints())
                .isEqualTo(80L);
        assertThat(result.getMultipliedPoints())
                .isEqualTo(800L);
        assertThat(result.getUnits())
                .isEqualTo(8);
    }

    @Test
    void whenGettingDailyStats_andUserHasMultipleStatsInSameDay_thenMaxStatsInDayAreReturned_andResponseHas200Status() throws FoldingRestException {
        final Team team = TeamUtils.create(generateTeam());
        final int userId = UserUtils.create(generateUserWithTeamId(team.getId())).getId();
        DatabaseUtils.insertStats("user_tc_stats_hourly",
                TestStats.create(userId, "2020-04-12 13:00:00", 10L, 100L, 1),
                TestStats.create(userId, "2020-04-13 14:00:00", 50L, 500L, 5),
                TestStats.create(userId, "2020-04-13 15:00:00", 110L, 1_100L, 11)
        );

        final HttpResponse<String> response = HISTORIC_STATS_REQUEST_SENDER.getDailyTeamStats(team.getId(), Year.parse("2020"), Month.of(4));
        assertThat(response.statusCode())
                .as("Did not receive a 200_OK HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_OK);

        final List<HistoricStats> results = new ArrayList<>(HistoricStatsResponseParser.getDailyTeamStats(response));
        assertThat(results)
                .hasSize(2);

        final HistoricStats result = results.get(1); // Ignore first result, as it is the "base" stats for the team
        assertThat(result.getPoints())
                .isEqualTo(100L);
        assertThat(result.getMultipliedPoints())
                .isEqualTo(1_000L);
        assertThat(result.getUnits())
                .isEqualTo(10);
    }

    @Test
    void whenGettingDailyStats_andTeamHasMultipleUsersInSameDay_thenCombinedStatsForAllUsersAreReturned_andResponseHas200Status() throws FoldingRestException {
        final Team team = TeamUtils.create(generateTeam());
        final int firstUserId = UserUtils.create(generateUserWithTeamIdAndCategory(team.getId(), Category.AMD_GPU)).getId();
        final int secondUserId = UserUtils.create(generateUserWithTeamIdAndCategory(team.getId(), Category.NVIDIA_GPU)).getId();
        DatabaseUtils.insertStats("user_tc_stats_hourly",
                TestStats.create(firstUserId, "2020-04-12 13:00:00", 50L, 500L, 5),
                TestStats.create(secondUserId, "2020-04-12 13:00:00", 50L, 500L, 5),
                TestStats.create(firstUserId, "2020-04-13 14:00:00", 100L, 1_000L, 10),
                TestStats.create(firstUserId, "2020-04-13 14:30:00", 110L, 1_100L, 11),
                TestStats.create(secondUserId, "2020-04-13 14:30:00", 200L, 2_000L, 20)
        );

        final HttpResponse<String> response = HISTORIC_STATS_REQUEST_SENDER.getDailyTeamStats(team.getId(), Year.parse("2020"), Month.of(4));
        assertThat(response.statusCode())
                .as("Did not receive a 200_OK HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_OK);

        final List<HistoricStats> results = new ArrayList<>(HistoricStatsResponseParser.getDailyTeamStats(response));
        assertThat(results)
                .hasSize(2);

        final HistoricStats result = results.get(1); // Ignore first result, as it is the "base" stats for the team
        assertThat(result.getPoints())
                .isEqualTo(210L);
        assertThat(result.getMultipliedPoints())
                .isEqualTo(2_100L);
        assertThat(result.getUnits())
                .isEqualTo(21);
    }

    @Test
    void whenGettingDailyStats_givenRequestUsesPreviousETag_andStatsHaveNotChanged_thenResponseHasA304Status_andNoBody() throws FoldingRestException {
        final Team team = TeamUtils.create(generateTeam());
        final int userId = UserUtils.create(generateUserWithTeamId(team.getId())).getId();
        DatabaseUtils.insertStats("user_tc_stats_hourly",
                TestStats.create(userId, "2020-04-12 14:00:00", 100L, 1_000L, 10)
        );

        final HttpResponse<String> response = HISTORIC_STATS_REQUEST_SENDER.getDailyTeamStats(team.getId(), Year.parse("2020"), Month.of(4));
        assertThat(response.statusCode())
                .as("Did not receive a 200_OK HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_OK);

        final String eTag = getETag(response);

        final HttpResponse<String> cachedResponse = HISTORIC_STATS_REQUEST_SENDER.getDailyTeamStats(team.getId(), Year.parse("2020"), Month.of(4), eTag);
        assertThat(cachedResponse.statusCode())
                .as("Expected second request to have a 304_NOT_MODIFIED HTTP response")
                .isEqualTo(HttpURLConnection.HTTP_NOT_MODIFIED);

        assertThat(HistoricStatsResponseParser.getDailyTeamStats(cachedResponse))
                .as("Expected cached response to have the same content as the non-cached response")
                .isNull();
    }

    @Test
    void whenGettingDailyStats_andInvalidTeamIdIsGiven_thenResponseHasA404Status() throws FoldingRestException {
        final HttpResponse<String> response = HISTORIC_STATS_REQUEST_SENDER.getDailyTeamStats(TestConstants.INVALID_ID, Year.parse("2020"), Month.of(4));
        assertThat(response.statusCode())
                .as("Did not receive a 404_NOT_FOUND HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_NOT_FOUND);
    }

    @Test
    void whenGettingDailyStats_andInvalidDateIsGiven_thenResponseHasA400Status() throws FoldingRestException, IOException, InterruptedException {
        final Team team = TeamUtils.create(generateTeam());
        UserUtils.create(generateUserWithTeamId(team.getId()));
        final int invalidMonth = 25;

        final HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(FOLDING_URL + "/historic/teams/" + team.getId() + '/' + "2020" + '/' + invalidMonth))
                .header("Content-Type", "application/json");

        final HttpRequest request = requestBuilder.build();

        final HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode())
                .as("Did not receive a 400_BAD_REQUEST HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);
    }


    @Test
    void whenGettingMonthlyStats_andValidTeamIdIsGiven_andAllUsersHaveNoStats_thenNoStatsAreReturned_andResponseHas200Status() throws FoldingRestException {
        final Team team = TeamUtils.create(generateTeam());
        UserUtils.create(generateUserWithTeamId(team.getId()));

        final HttpResponse<String> response = HISTORIC_STATS_REQUEST_SENDER.getMonthlyTeamStats(team.getId(), Year.parse("2020"));
        assertThat(response.statusCode())
                .as("Did not receive a 200_OK HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_OK);

        final Collection<HistoricStats> results = HistoricStatsResponseParser.getMonthlyTeamStats(response);
        assertThat(results)
                .isEmpty();
    }

    @Test
    void whenGettingMonthlyStats_andValidTeamIdIsGiven_andUserHasSomeStats_thenStatsAreReturned_andResponseHas200Status() throws FoldingRestException {
        final Team team = TeamUtils.create(generateTeam());
        final int userId = UserUtils.create(generateUserWithTeamId(team.getId())).getId();
        DatabaseUtils.insertStats("user_tc_stats_hourly",
                TestStats.create(userId, "2020-04-12 14:00:00", 100L, 1_000L, 10)
        );

        final HttpResponse<String> response = HISTORIC_STATS_REQUEST_SENDER.getMonthlyTeamStats(team.getId(), Year.parse("2020"));
        assertThat(response.statusCode())
                .as("Did not receive a 200_OK HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_OK);

        final Collection<HistoricStats> results = HistoricStatsResponseParser.getMonthlyTeamStats(response);
        assertThat(results)
                .hasSize(1);
    }

    @Test
    void whenGettingMonthlyStats_andUserHasMultipleStats_thenEachStatsEntryIsADiffFromPreviousMonth_andResponseHas200Status() throws FoldingRestException {
        final Team team = TeamUtils.create(generateTeam());
        final int userId = UserUtils.create(generateUserWithTeamId(team.getId())).getId();
        DatabaseUtils.insertStats("user_tc_stats_hourly",
                TestStats.create(userId, "2020-03-12 13:00:00", 20L, 200L, 2), // No diff from this result, since stats are reset each month
                TestStats.create(userId, "2020-04-12 13:00:00", 100L, 1_000L, 10)
        );

        final HttpResponse<String> response = HISTORIC_STATS_REQUEST_SENDER.getMonthlyTeamStats(team.getId(), Year.parse("2020"));
        assertThat(response.statusCode())
                .as("Did not receive a 200_OK HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_OK);

        final List<HistoricStats> results = new ArrayList<>(HistoricStatsResponseParser.getMonthlyTeamStats(response));
        assertThat(results)
                .hasSize(2);

        final HistoricStats result = results.get(1); // Ignore first result, as it is the "base" stats for the team
        assertThat(result.getPoints())
                .isEqualTo(100L);
        assertThat(result.getMultipliedPoints())
                .isEqualTo(1_000L);
        assertThat(result.getUnits())
                .isEqualTo(10);
    }

    @Test
    void whenGettingMonthlyStats_andUserHasMultipleStatsInSameMonth_thenMaxStatsInMonthAreReturned_andResponseHas200Status() throws FoldingRestException {
        final Team team = TeamUtils.create(generateTeam());
        final int userId = UserUtils.create(generateUserWithTeamId(team.getId())).getId();
        DatabaseUtils.insertStats("user_tc_stats_hourly",
                TestStats.create(userId, "2020-03-12 13:00:00", 50L, 500L, 5), // No diff from this result, since stats are reset each month
                TestStats.create(userId, "2020-04-13 14:00:00", 100L, 1_000L, 10),
                TestStats.create(userId, "2020-04-13 15:00:00", 110L, 1_100L, 11)
        );

        final HttpResponse<String> response = HISTORIC_STATS_REQUEST_SENDER.getMonthlyTeamStats(team.getId(), Year.parse("2020"));
        assertThat(response.statusCode())
                .as("Did not receive a 200_OK HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_OK);

        final List<HistoricStats> results = new ArrayList<>(HistoricStatsResponseParser.getMonthlyTeamStats(response));
        assertThat(results)
                .hasSize(2);

        final HistoricStats result = results.get(1); // Ignore first result, as it is the "base" stats for the team
        assertThat(result.getPoints())
                .isEqualTo(110L);
        assertThat(result.getMultipliedPoints())
                .isEqualTo(1_100L);
        assertThat(result.getUnits())
                .isEqualTo(11);
    }

    @Test
    void whenGettingMonthlyStats_andTeamHasMultipleUsersInSameMonth_thenCombinedStatsForAllUsersAreReturned_andResponseHas200Status() throws FoldingRestException {
        final Team team = TeamUtils.create(generateTeam());
        final int firstUserId = UserUtils.create(generateUserWithTeamIdAndCategory(team.getId(), Category.AMD_GPU)).getId();
        final int secondUserId = UserUtils.create(generateUserWithTeamIdAndCategory(team.getId(), Category.NVIDIA_GPU)).getId();
        DatabaseUtils.insertStats("user_tc_stats_hourly",
                TestStats.create(firstUserId, "2020-03-12 13:00:00", 50L, 500L, 5), // No diff from this result, since stats are reset each month
                TestStats.create(secondUserId, "2020-03-12 13:00:00", 50L, 500L, 5),
                TestStats.create(firstUserId, "2020-04-13 14:00:00", 100L, 1_000L, 10),
                TestStats.create(firstUserId, "2020-04-13 14:30:00", 110L, 1_100L, 11),
                TestStats.create(secondUserId, "2020-04-13 14:30:00", 200L, 2_000L, 20)
        );

        final HttpResponse<String> response = HISTORIC_STATS_REQUEST_SENDER.getMonthlyTeamStats(team.getId(), Year.parse("2020"));
        assertThat(response.statusCode())
                .as("Did not receive a 200_OK HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_OK);

        final List<HistoricStats> results = new ArrayList<>(HistoricStatsResponseParser.getHourlyTeamStats(response));
        assertThat(results)
                .hasSize(2);

        final HistoricStats result = results.get(1); // Ignore first result, as it is the "base" stats for the team
        assertThat(result.getPoints())
                .isEqualTo(310L);
        assertThat(result.getMultipliedPoints())
                .isEqualTo(3_100L);
        assertThat(result.getUnits())
                .isEqualTo(31);
    }

    @Test
    void whenGettingMonthlyStats_givenRequestUsesPreviousETag_andStatsHaveNotChanged_thenResponseHasA304Status_andNoBody() throws FoldingRestException {
        final Team team = TeamUtils.create(generateTeam());
        final int userId = UserUtils.create(generateUserWithTeamId(team.getId())).getId();
        DatabaseUtils.insertStats("user_tc_stats_hourly",
                TestStats.create(userId, "2020-04-12 14:00:00", 100L, 1_000L, 10)
        );

        final HttpResponse<String> response = HISTORIC_STATS_REQUEST_SENDER.getMonthlyTeamStats(team.getId(), Year.parse("2020"));
        assertThat(response.statusCode())
                .as("Did not receive a 200_OK HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_OK);

        final String eTag = getETag(response);

        final HttpResponse<String> cachedResponse = HISTORIC_STATS_REQUEST_SENDER.getMonthlyTeamStats(team.getId(), Year.parse("2020"), eTag);
        assertThat(cachedResponse.statusCode())
                .as("Expected second request to have a 304_NOT_MODIFIED HTTP response")
                .isEqualTo(HttpURLConnection.HTTP_NOT_MODIFIED);

        assertThat(HistoricStatsResponseParser.getMonthlyTeamStats(cachedResponse))
                .as("Expected cached response to have the same content as the non-cached response")
                .isNull();
    }

    @Test
    void whenGettingMonthlyStats_andInvalidTeamIdIsGiven_thenResponseHasA404Status() throws FoldingRestException {
        final HttpResponse<String> response = HISTORIC_STATS_REQUEST_SENDER.getMonthlyTeamStats(TestConstants.INVALID_ID, Year.parse("2020"));
        assertThat(response.statusCode())
                .as("Did not receive a 404_NOT_FOUND HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_NOT_FOUND);
    }

    @Test
    void whenGettingMonthlyStats_andInvalidDateIsGiven_thenResponseHasA400Status() throws FoldingRestException, IOException, InterruptedException {
        final Team team = TeamUtils.create(generateTeam());
        UserUtils.create(generateUserWithTeamId(team.getId()));
        final int invalidYear = -100;

        final HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(FOLDING_URL + "/historic/teams/" + team.getId() + '/' + invalidYear))
                .header("Content-Type", "application/json");

        final HttpRequest request = requestBuilder.build();

        final HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode())
                .as("Did not receive a 400_BAD_REQUEST HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);
    }

    @AfterAll
    static void tearDown() throws FoldingRestException {
        cleanSystemForComplexTests();
    }
}

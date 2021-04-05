package me.zodac.folding.parsing;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.zodac.folding.api.FoldingUser;
import me.zodac.folding.api.exception.FoldingException;
import me.zodac.folding.db.postgres.PostgresDbManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class FoldingStatsParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(PostgresDbManager.class);

    public static void parseStats(final List<FoldingUser> foldingUsers) {
        final Timestamp currentUtcTime = new Timestamp(ZonedDateTime.now(ZoneId.of("UTC")).toInstant().toEpochMilli());
        final List<FoldingStats> stats = new ArrayList<>(foldingUsers.size());

        for (final FoldingUser foldingUser : foldingUsers) {
            try {
                final long totalPointsForUser = getTotalPointsForUser(foldingUser.getFoldingUserName(), foldingUser.getPasskey());
                stats.add(new FoldingStats(foldingUser.getId(), totalPointsForUser, currentUtcTime));
            } catch (final FoldingException e) {
                LOGGER.warn("Unable to get stats for user '{}/{}'", foldingUser.getFoldingUserName(), foldingUser.getPasskey(), e.getCause());
            }
        }

        try {
            PostgresDbManager.persistStats(stats);
        } catch (final FoldingException e) {
            LOGGER.error("Error persisting stats", e.getCause());
        }
    }

    // TODO: [zodac] Move this somewhere else, keep the HTTP logic in a single place

    // TODO: [zodac] Team number hardcoded: set as env variable, set for each user?
    private static final String TEAM_NUMBER = "239902"; // EHW
    private static final String STATS_URL_FORMAT = "https://stats.foldingathome.org/api/donors?name=%s&search_type=exact&passkey=%s&team=" + TEAM_NUMBER;
    private static final Gson GSON = new Gson();
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private static long getTotalPointsForUser(final String userName, final String passkey) throws FoldingException {
        LOGGER.info("Getting points for username/passkey '{}/{}' for team {}", userName, passkey, TEAM_NUMBER);
        final String statsRequestUrl = String.format(STATS_URL_FORMAT, userName, passkey);

        final HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(statsRequestUrl))
                .header("Content-Type", "application/json")
                .build();

        try {
            final HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            // All user searches return a 200 response, even if the user/passkey is invalid, we will need to parse and check it later
            if (response.statusCode() != HttpURLConnection.HTTP_OK) {
                throw new FoldingException(String.format("Invalid response: %s", response));
            }

            final JsonObject httpResponse = JsonParser.parseString(response.body()).getAsJsonObject();
            final JsonArray results = httpResponse.getAsJsonArray("results");

            if (results.size() == 0) {
                throw new FoldingException(String.format("Unable to find any result for username/passkey '%s/%s': %s", userName, passkey, response.body()));
            }

            if (results.size() > 1) {
                // Don't believe this should happen with our current URL, there should only be one result for a user/passkey/team combo
                throw new FoldingException(String.format("Too many results found for username/passkey '%s/%s': %s", userName, passkey, response.body()));
            }

            final StatsApiResult statsApiResponse = GSON.fromJson(results.get(0), StatsApiResult.class);
            LOGGER.info("Found result: {}", statsApiResponse);
            return statsApiResponse.getCredit();
        } catch (final IOException | InterruptedException e) {
            throw new FoldingException("Unable to send HTTP request to Folding@Home API", e.getCause());
        }
    }

    /**
     * Invalid response:
     * <pre>
     *     {
     *       "description": "No results",
     *       "monthly": false,
     *       "results": [],
     *       "month": 3,
     *       "year": 2021,
     *       "query": "donor",
     *       "path": "donors"
     *     }
     * </pre>
     * <p>
     * Valid response:
     * <pre>
     *     {
     *       "description": "Name is 'zodac' -- Passkey 'fc7d6837269d86784d8bfd0b386d6bca' -- Team '37726'",
     *       "monthly": false,
     *       "results": [
     *         {
     *           "wus": 22023,
     *           "credit_cert": "https://apps.foldingathome.org/awards?user=28431&type=score",
     *           "name": "zodac",
     *           "rank": 33481,
     *           "credit": 39514566,
     *           "team": 37726,
     *           "wus_cert": "https://apps.foldingathome.org/awards?user=28431&type=wus",
     *           "id": 28431
     *         }
     *       ],
     *       "month": 3,
     *       "year": 2021,
     *       "query": "donor",
     *       "path": "donors"
     *     }
     * </pre>
     */
    private static class StatsApiResult {

        private String name;
        private int team;
        private long credit;

        public StatsApiResult() {

        }

        public String getName() {
            return name;
        }

        public void setName(final String name) {
            this.name = name;
        }

        public int getTeam() {
            return team;
        }

        public void setTeam(final int team) {
            this.team = team;
        }

        public long getCredit() {
            return credit;
        }

        public void setCredit(final long credit) {
            this.credit = credit;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            final StatsApiResult that = (StatsApiResult) o;
            return team == that.team && credit == that.credit && Objects.equals(name, that.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, team, credit);
        }

        // TODO: [zodac] toString()
        @Override
        public String toString() {
            return "StatsApiResult{" +
                    "name='" + name + '\'' +
                    ", team=" + team +
                    ", credit=" + NumberFormat.getInstance(Locale.UK).format(credit) +
                    '}';
        }
    }
}

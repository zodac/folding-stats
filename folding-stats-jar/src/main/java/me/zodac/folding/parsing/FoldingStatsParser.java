package me.zodac.folding.parsing;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import me.zodac.folding.api.FoldingStats;
import me.zodac.folding.api.FoldingUser;
import me.zodac.folding.api.UserStats;
import me.zodac.folding.api.exception.FoldingException;
import me.zodac.folding.cache.tc.TcStatsCache;
import me.zodac.folding.db.DbManagerRetriever;
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
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

// TODO: [zodac] Should not go straight to TC stats caches or PostgresDB, use StorageFacade to call parsing logic, then do persistence from caller
public class FoldingStatsParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(FoldingStatsParser.class);

    private static final TcStatsCache TC_STATS_CACHE = TcStatsCache.get();

    public static void parseStatsForAllUsers(final List<FoldingUser> foldingUsers) {
        final Timestamp currentUtcTime = new Timestamp(OffsetDateTime.now(ZoneOffset.UTC).toInstant().toEpochMilli());
        final List<FoldingStats> stats = new ArrayList<>(foldingUsers.size());

        for (final FoldingUser foldingUser : foldingUsers) {
            try {
                final UserStats totalStatsForUser = getTotalPointsForUser(foldingUser.getFoldingUserName(), foldingUser.getPasskey(), foldingUser.getFoldingTeamNumber());
                stats.add(new FoldingStats(foldingUser.getId(), totalStatsForUser, currentUtcTime));

                // If no entry exists in the cache, first time we pull stats for the user is also the initial state
                if (!TC_STATS_CACHE.haveInitialStatsForUser(foldingUser.getId())) {
                    TC_STATS_CACHE.addInitialStats(foldingUser.getId(), totalStatsForUser);
                }

                TC_STATS_CACHE.addCurrentStats(foldingUser.getId(), totalStatsForUser);
            } catch (final FoldingException e) {
                LOGGER.warn("Unable to get stats for user '{}/{}/{}'", foldingUser.getFoldingUserName(), foldingUser.getPasskey(), foldingUser.getFoldingTeamNumber(), e.getCause());
            }
        }

        try {
            DbManagerRetriever.get().persistTcStats(stats);
        } catch (final FoldingException e) {
            LOGGER.error("Error persisting stats", e.getCause());
        }
    }

    // TODO: [zodac] Move this somewhere else, keep the HTTP logic in a single place

    private static final String STATS_URL_FORMAT = "https://statsclassic.foldingathome.org/api/donors?name=%s&search_type=exact&passkey=%s&team=%s";
    private static final Gson GSON = new Gson();
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public static UserStats getTotalPointsForUser(final String userName, final String passkey, final int foldingTeamNumber) throws FoldingException {
        LOGGER.debug("Getting stats for username/passkey '{}/{}' for team {}", userName, passkey, foldingTeamNumber);
        final String statsRequestUrl = String.format(STATS_URL_FORMAT, userName, passkey, foldingTeamNumber);
        LOGGER.debug("Sending request to: {}", statsRequestUrl);

        final HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(statsRequestUrl))
                .header("Content-Type", "application/json")
                .build();

        try {
            // The Folding@Home API seems to be caching the username+passkey stats. The first request will have the same result as the previous hour
            // To get around this, we send a request, ignore it, then send another request and parse that one
            HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            final HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            // All user searches return a 200 response, even if the user/passkey is invalid, we will need to parse and check it later
            if (response.statusCode() != HttpURLConnection.HTTP_OK) {
                throw new FoldingException(String.format("Invalid response: %s", response));
            }

            final StatsApiResult statsApiResponse = parseHttpResponse(userName, passkey, response);
            LOGGER.info("Found {}", statsApiResponse);
            return new UserStats(statsApiResponse.getCredit(), statsApiResponse.getWus());
        } catch (final IOException | InterruptedException e) {
            throw new FoldingException("Unable to send HTTP request to Folding@Home API", e);
        } catch (final ClassCastException e) {
            throw new FoldingException("Unable to parse HTTP response from Folding@Home API correctly", e);
        } catch (final Exception e) {
            LOGGER.warn("Unexpected error parsing stats", e);
            throw e;
        }
    }

    private static StatsApiResult parseHttpResponse(final String userName, final String passkey, final HttpResponse<String> response) throws FoldingException {
        try {
            final JsonObject httpResponse = JsonParser.parseString(response.body()).getAsJsonObject();

            if (!httpResponse.has("results")) {
                throw new FoldingException(String.format("Unable to find any 'results' entry for username/passkey '%s/%s': %s", userName, passkey, response.body()));
            }

            final JsonArray results = httpResponse.getAsJsonArray("results");

            if (results.size() == 0) {
                throw new FoldingException(String.format("Unable to find any result for username/passkey '%s/%s': %s", userName, passkey, response.body()));
            }

            if (results.size() > 1) {
                // Don't believe this should happen, since with our current URL, there should only be one result for a user/passkey/team combo
                throw new FoldingException(String.format("Too many results found for username/passkey '%s/%s': %s", userName, passkey, response.body()));
            }

            return GSON.fromJson(results.get(0), StatsApiResult.class);
        } catch (final JsonSyntaxException e) {
            LOGGER.warn("Error parsing the JSON response from the API: '{}'", response.body(), e);
            throw e;
        } catch (final Exception e) {
            LOGGER.warn("Unexpected error parsing JSON response from the API with status code {}: '{}'", response.statusCode(), response.body(), e);
            throw e;
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
        private long credit;
        private long wus;

        public StatsApiResult() {

        }

        public String getName() {
            return name;
        }

        public void setName(final String name) {
            this.name = name;
        }

        public long getCredit() {
            return credit;
        }

        public void setCredit(final long credit) {
            this.credit = credit;
        }

        public long getWus() {
            return wus;
        }

        public void setWus(final long wus) {
            this.wus = wus;
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
            return credit == that.credit && wus == that.wus && Objects.equals(name, that.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, credit, wus);
        }

        // TODO: [zodac] toString()
        @Override
        public String toString() {
            return "StatsApiResult{" +
                    "name='" + name + '\'' +
                    ", credit=" + NumberFormat.getInstance(Locale.UK).format(credit) +
                    ", wus=" + NumberFormat.getInstance(Locale.UK).format(wus) +
                    '}';
        }
    }
}

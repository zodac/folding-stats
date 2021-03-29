package me.zodac.folding.parsing;

import com.google.gson.Gson;
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
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class FoldingStatsParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(PostgresDbManager.class);

    public static void parseStats(final List<FoldingUser> foldingUsers) {
        final long currentTime = System.currentTimeMillis();
        final List<FoldingStats> stats = new ArrayList<>(foldingUsers.size());

        for (final FoldingUser foldingUser : foldingUsers) {
            try {
                final long totalPointsForUser = getTotalPointsForUser(foldingUser.getUserName(), foldingUser.getPasskey());
                stats.add(new FoldingStats(foldingUser.getId(), totalPointsForUser, currentTime));
            } catch (final FoldingException e) {
                LOGGER.warn("Unable to get stats for user {}", foldingUser.getUserName(), e.getCause());
            }
        }

        try {
            PostgresDbManager.persistStats(stats);
        } catch (final FoldingException e) {
            LOGGER.error("Error persisting stats", e.getCause());
        }
    }

    // TODO: [zodac] Move this somewhere else, keep the HTTP logic in a single place

    private static final String STATS_URL_FORMAT = "https://stats.foldingathome.org/api/donors?name=%s&search_type=exact&passkey=%s&team=37726";
    private static final Gson GSON = new Gson();
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private static long getTotalPointsForUser(final String userName, final String passkey) throws FoldingException {
        final String statsRequestUrl = String.format(STATS_URL_FORMAT, userName, passkey);

        final HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(statsRequestUrl))
                .header("Content-Type", "application/json")
                .build();

        try {
            final HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != HttpURLConnection.HTTP_OK) {
                throw new FoldingException(String.format("Invalid response: %s", response));
            }

            final StatsApiResponse statsApiResponse = GSON.fromJson(response.body(), StatsApiResponse.class);
            return statsApiResponse.getCredit();
        } catch (final IOException | InterruptedException e) {
            throw new FoldingException("Unable to send HTTP request to F@H API", e.getCause());
        }
    }

    // TODO: [zodac] Endpoint throwing 502 atm, clarify response when it's back up

    /**
     * <pre>
     *     {
     *       "wus": 3,
     *       "credit_cert": "https://apps.foldingathome.org/awards?user=2075&type=score",
     *       "name": "zodac",
     *       "rank": 8435,
     *       "credit": 999999,
     *       "team": 37726,
     *       "wus_cert": "https://apps.foldingathome.org/awards?user=2075&type=wus",
     *       "id": -1
     *     }
     * </pre>
     */
    private static class StatsApiResponse {

        private long credit;

        public StatsApiResponse() {

        }

        public long getCredit() {
            return credit;
        }

        public void setCredit(final long credit) {
            this.credit = credit;
        }
    }
}

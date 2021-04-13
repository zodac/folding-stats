package me.zodac.folding.parsing;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import me.zodac.folding.api.FoldingStats;
import me.zodac.folding.api.FoldingUser;
import me.zodac.folding.api.UserStats;
import me.zodac.folding.api.exception.FoldingException;
import me.zodac.folding.cache.tc.TcStatsCache;
import me.zodac.folding.db.DbManagerRetriever;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Type;
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
import java.util.*;

// TODO: [zodac] Should not go straight to TC stats caches or PostgresDB, use StorageFacade to call parsing logic, then do persistence from caller
public class FoldingStatsParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(FoldingStatsParser.class);

    private static final TcStatsCache TC_STATS_CACHE = TcStatsCache.get();

    public static void parseStatsForAllUsers(final List<FoldingUser> foldingUsers) {
        final Timestamp currentUtcTime = new Timestamp(OffsetDateTime.now(ZoneOffset.UTC).toInstant().toEpochMilli());
        final List<FoldingStats> stats = new ArrayList<>(foldingUsers.size());

        for (final FoldingUser foldingUser : foldingUsers) {
            try {
                final UserStats totalStatsForUser = getStatsForUser(foldingUser.getFoldingUserName(), foldingUser.getPasskey(), foldingUser.getFoldingTeamNumber());
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

    private static final Gson GSON = new Gson();
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public static UserStats getStatsForUser(final String userName, final String passkey, final int foldingTeamNumber) throws FoldingException {
        LOGGER.debug("Getting stats for username/passkey '{}/{}' at team {}", userName, passkey, foldingTeamNumber);
        final long userPoints = getPointsForUser(userName, passkey, foldingTeamNumber);
        final int userUnits = getUnitsForUser(userName, passkey);

        final UserStats userStats = new UserStats(userPoints, userUnits);
        LOGGER.info("Found {}", userStats);
        return userStats;
    }

    public static int getUnitsForUser(final String userName, final String passkey) throws FoldingException {
        final String unitsRequestUrl = new UnitsUrlBuilder()
                .forUser(userName)
                .withPasskey(passkey)
                .build();

        LOGGER.debug("Sending units request to: {}", unitsRequestUrl);
        final HttpResponse<String> response = sendFoldingRequest(unitsRequestUrl);
        LOGGER.debug("Units response: {}", response.body());

        final Type collectionType = new TypeToken<Collection<UnitsApiInstance>>() {
        }.getType();
        final List<UnitsApiInstance> unitsResponse = GSON.fromJson(response.body(), collectionType);

        if (unitsResponse.isEmpty()) {
            LOGGER.warn("No valid units found for user: {}", response.body());
            return 0;
        }

        if (unitsResponse.size() > 1) {
            LOGGER.warn("Too many unit responses returned for user: {}", response.body());
            return 0;
        }

        return unitsResponse.get(0).getFinished();
    }

    public static long getPointsForUser(final String userName, final String passkey, final int foldingTeamNumber) throws FoldingException {
        final String pointsRequestUrl = new PointsUrlBuilder()
                .forUser(userName)
                .withPasskey(passkey)
                .atTeam(foldingTeamNumber)
                .build();

        LOGGER.debug("Sending points request to: {}", pointsRequestUrl);
        final HttpResponse<String> response = sendFoldingRequest(pointsRequestUrl);
        LOGGER.debug("Points response: {}", response.body());

        final PointsApiResponse pointsApiResponse = parsePointsResponse(response);
        return pointsApiResponse.getContributed();
    }

    // The Folding@Home API seems to be caching the username+passkey stats. The first request will have the same result as the previous hour
    // To get around this, we send a request, ignore it, then send another request and parse that one
    private static HttpResponse<String> sendFoldingRequest(final String requestUrl) throws FoldingException {
        try {
            final HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create(requestUrl))
                    .header("Content-Type", "application/json")
                    .build();

            HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            final HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            // All user searches return a 200 response, even if the user/passkey is invalid, we will need to parse and check it later
            if (response.statusCode() != HttpURLConnection.HTTP_OK) {
                throw new FoldingException(String.format("Invalid response: %s", response));
            }

            return response;
        } catch (final IOException | InterruptedException e) {
            throw new FoldingException("Unable to send HTTP request to Folding@Home API", e);
        } catch (final ClassCastException e) {
            throw new FoldingException("Unable to parse HTTP response from Folding@Home API correctly", e);
        } catch (final Exception e) {
            LOGGER.warn("Unexpected error retrieving stats for user", e);
            throw e;
        }
    }

    private static PointsApiResponse parsePointsResponse(final HttpResponse<String> response) {
        try {
            return GSON.fromJson(response.body(), PointsApiResponse.class);
        } catch (final JsonSyntaxException e) {
            LOGGER.warn("Error parsing the JSON response from the API: '{}'", response.body(), e);
            throw e;
        } catch (final Exception e) {
            LOGGER.warn("Unexpected error parsing JSON response from the API with status code {}: '{}'", response.statusCode(), response.body(), e);
            throw e;
        }
    }

    /**
     * Valid response:
     * <pre>
     *     [
     *         {
     *             "finished":21260,
     *             "expired":60,
     *             "active":1
     *         }
     *     ]
     * </pre>
     */
    private static class UnitsApiInstance {

        private int finished;
        private int expired;
        private int active;

        public UnitsApiInstance() {

        }

        public int getFinished() {
            return finished;
        }

        public void setFinished(final int finished) {
            this.finished = finished;
        }

        public int getExpired() {
            return expired;
        }

        public void setExpired(final int expired) {
            this.expired = expired;
        }

        public int getActive() {
            return active;
        }

        public void setActive(final int active) {
            this.active = active;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            final UnitsApiInstance that = (UnitsApiInstance) o;
            return finished == that.finished && expired == that.expired && active == that.active;
        }

        @Override
        public int hashCode() {
            return Objects.hash(finished, expired, active);
        }

        @Override
        public String toString() {
            return "UnitsApiInstance{" +
                    "finished=" + finished +
                    ", expired=" + expired +
                    ", active=" + active +
                    '}';
        }
    }


    /**
     * Valid response:
     * <pre>
     *     {
     *         "earned": 97802740,
     *         "contributed": 76694831,
     *         "team_total": 5526874925,
     *         "team_name": "ExtremeHW",
     *         "team_url": "https://extremehw.net/",
     *         "team_rank": 219,
     *         "team_urllogo": "https://image.extremehw.net/images/2020/03/15/LOGO-EXTREME-ON-TRANSPARENT-BACKGROUNDbd5ff1f81fff3068.png",
     *         "url": "https://stats.foldingathome.org/donor/BWG"
     *      }
     * </pre>
     */
    private static class PointsApiResponse {

        private long contributed;

        public PointsApiResponse() {

        }

        public long getContributed() {
            return contributed;
        }

        public void setContributed(final long contributed) {
            this.contributed = contributed;
        }


        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || this.getClass() != o.getClass()) {
                return false;
            }
            final PointsApiResponse that = (PointsApiResponse) o;
            return contributed == that.contributed;
        }

        @Override
        public int hashCode() {
            return Objects.hash(contributed);
        }

        // TODO: [zodac] toString()
        @Override
        public String toString() {
            return "StatsApiResult{" +
                    "points=" + NumberFormat.getInstance(Locale.UK).format(contributed) +
                    '}';
        }
    }
}

package me.zodac.folding.ejb.tc;

import com.google.gson.Gson;
import java.time.LocalDateTime;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import me.zodac.folding.api.ejb.BusinessLogic;
import me.zodac.folding.api.tc.result.MonthlyResult;
import me.zodac.folding.api.utils.DateTimeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Stores the stats results for the <code>Team Competition</code>. Takes the current stats and stores them with the current
 * {@link java.time.ZoneOffset#UTC} {@link LocalDateTime}.
 */
@Singleton
public class UserStatsStorer {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new Gson();

    @EJB
    private BusinessLogic businessLogic;

    @EJB
    private LeaderboardStatsGenerator leaderboardStatsGenerator;

    /**
     * Stores the {@link MonthlyResult} for the current {@link java.time.ZoneOffset#UTC} date-time.
     *
     * <p> The {@link MonthlyResult} will be generated using {@link LeaderboardStatsGenerator#generateTeamLeaderboards()} and
     * {@link LeaderboardStatsGenerator#generateUserCategoryLeaderboards()}.
     *
     * <p> The JSON output will be in the form:
     * <pre>
     * {
     *   "teamLeaderboard": [
     *     {
     *       "teamName": "Team1",
     *       "teamPoints": 25054094,
     *       "teamMultipliedPoints": 857328234,
     *       "teamUnits": 183,
     *       "rank": 1,
     *       "diffToLeader": 0,
     *       "diffToNext": 0
     *     },
     *     {
     *       "teamName": "Team2",
     *       "teamPoints": 139969822,
     *       "teamMultipliedPoints": 451436284,
     *       "teamUnits": 525,
     *       "rank": 2,
     *       "diffToLeader": 405891950,
     *       "diffToNext": 405891950
     *     }
     *   ],
     *   "userCategoryLeaderboard": {
     *     "AMD_GPU": [
     *       {
     *         "displayName": "User1",
     *         "foldingName": "User1",
     *         "hardware": "Hardware1",
     *         "teamName": "Team1",
     *         "points": 7707061,
     *         "multipliedPoints": 547817896,
     *         "units": 50,
     *         "rank": 1,
     *         "diffToLeader": 0,
     *         "diffToNext": 0
     *       },
     *       {
     *         "displayName": "User2",
     *         "foldingName": "User2",
     *         "hardware": "Hardware2",
     *         "teamName": "Team2",
     *         "points": 11587322,
     *         "multipliedPoints": 189800334,
     *         "units": 68,
     *         "rank": 2,
     *         "diffToLeader": 358017562,
     *         "diffToNext": 358017562
     *       }
     *     ],
     *     "NVIDIA_GPU": [
     *       {
     *         "displayName": "User3",
     *         "foldingName": "User3",
     *         "hardware": "Hardware3",
     *         "teamName": "Team1",
     *         "points": 124648353,
     *         "multipliedPoints": 181986595,
     *         "units": 349,
     *         "rank": 1,
     *         "diffToLeader": 0,
     *         "diffToNext": 0
     *       }
     *     ],
     *     "WILDCARD": [
     *       {
     *         "displayName": "User4",
     *         "foldingName": "User4",
     *         "hardware": "Hardware4",
     *         "teamName": "Team2",
     *         "points": 11633984,
     *         "multipliedPoints": 190564658,
     *         "units": 72,
     *         "rank": 1,
     *         "diffToLeader": 0,
     *         "diffToNext": 0
     *       }
     *     ]
     *   }
     * }
     * </pre>
     */
    public void storeMonthlyResult() {
        final MonthlyResult monthlyResult = MonthlyResult.create(leaderboardStatsGenerator.generateTeamLeaderboards(),
            leaderboardStatsGenerator.generateUserCategoryLeaderboards());

        final String result = GSON.toJson(monthlyResult);
        final LocalDateTime currentUtcDateTime = DateTimeUtils.currentUtcDateTime().toLocalDateTime();

        LOGGER.info("Storing TC results for {}", currentUtcDateTime);
        businessLogic.createMonthlyResult(result, currentUtcDateTime);
    }
}

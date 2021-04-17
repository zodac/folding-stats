package me.zodac.folding.bean;

import me.zodac.folding.StorageFacade;
import me.zodac.folding.api.FoldingStats;
import me.zodac.folding.api.FoldingTeam;
import me.zodac.folding.api.FoldingUser;
import me.zodac.folding.api.TeamStats;
import me.zodac.folding.api.UserStats;
import me.zodac.folding.api.utils.TimeUtils;
import me.zodac.folding.cache.tc.TcStatsCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Startup
@Singleton
public class HistoricStatsCollector {

    private static final Logger LOGGER = LoggerFactory.getLogger(HistoricStatsCollector.class);

    @EJB
    private StorageFacade storageFacade;

    @Schedule(minute = "20", info = "Daily historic stats for TC users")
    public void dailyTcUserStatsCollection() {
        LOGGER.info("Saving daily historic stats for TC users");
        final List<FoldingUser> tcUsers = storageFacade.getTcUsers();

        if (tcUsers.isEmpty()) {
            LOGGER.warn("No TC Folding users configured in system!");
            return;
        }

        final Timestamp currentUtcTimestamp = TimeUtils.getCurrentUtcTimestamp();
        final List<FoldingStats> tcFoldingStats = new ArrayList<>(tcUsers.size());

        for (final FoldingUser tcUser : tcUsers) {
            final Optional<UserStats> initialStats =  TcStatsCache.get().getInitialStatsForUser(tcUser.getId());
            final Optional<UserStats> currentStats = TcStatsCache.get().getCurrentStatsForUser(tcUser.getId());

            if (initialStats.isEmpty() || currentStats.isEmpty()) {
                LOGGER.warn("No stats found for TC Folding user {}", tcUser.getId());
                continue;
            }

            final long userDailyPoints = currentStats.get().getPoints() - initialStats.get().getPoints();
            final int userDailyUnits = currentStats.get().getUnits() -  initialStats.get().getUnits();

            tcFoldingStats.add(new FoldingStats(tcUser.getId(), new UserStats(userDailyPoints, userDailyUnits), currentUtcTimestamp));
        }

        storageFacade.persistDailyUserTcStats(tcFoldingStats);
    }

    @Schedule(minute = "20", info = "Daily historic stats for TC teams")
    public void dailyTcTeamStatsCollection() {
        LOGGER.info("Saving daily historic stats for TC users");
        final List<TeamStats> tcTeamStats = getTeamStats();

        if (!tcTeamStats.isEmpty()) {
            storageFacade.persistDailyTeamTcStats(tcTeamStats);
        }
    }

    @Schedule(dayOfMonth = "1", minute = "20", info = "Monthly historic stats for TC teams")
    public void monthlyTcTeamStatsCollection() {
        LOGGER.info("Saving monthly historic stats for TC users");
        final List<TeamStats> tcTeamStats = getTeamStats();

        if (!tcTeamStats.isEmpty()) {
            storageFacade.persistMonthlyTeamTcStats(tcTeamStats);
        }
    }

    private List<TeamStats> getTeamStats() {
        final List<FoldingTeam> tcTeams = storageFacade.getTcTeams();

        if (tcTeams.isEmpty()) {
            LOGGER.warn("No TC Folding teams configured in system!");
            return Collections.emptyList();
        }

        final Timestamp currentUtcTimestamp = TimeUtils.getCurrentUtcTimestamp();
        final List<TeamStats> tcTeamStats = new ArrayList<>(tcTeams.size());

        for (final FoldingTeam tcTeam : tcTeams) {
            final Set<Integer> userIds = tcTeam.getUserIds();

            long teamPoints = 0L;
            int teamUnits = 0;

            for (final int userId : userIds) {
                final Optional<UserStats> initialStats =  TcStatsCache.get().getInitialStatsForUser(userId);
                final Optional<UserStats> currentStats = TcStatsCache.get().getCurrentStatsForUser(userId);

                if (initialStats.isEmpty() || currentStats.isEmpty()) {
                    LOGGER.warn("No stats found for TC Folding user {}", userId);
                    continue;
                }

                teamPoints += currentStats.get().getPoints() - initialStats.get().getPoints();
                teamUnits += currentStats.get().getUnits() -  initialStats.get().getUnits();
            }

            tcTeamStats.add(new TeamStats(tcTeam.getId(), new UserStats(teamPoints, teamUnits), currentUtcTimestamp));
        }
        return tcTeamStats;
    }
}

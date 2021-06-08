package me.zodac.folding.api.db;

import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.stats.OffsetStats;
import me.zodac.folding.api.tc.stats.RetiredUserTcStats;
import me.zodac.folding.api.tc.stats.UserStats;
import me.zodac.folding.api.tc.stats.UserTcStats;
import me.zodac.folding.rest.api.tc.historic.HistoricStats;

import java.time.Month;
import java.time.Year;
import java.util.Collection;
import java.util.Optional;

/**
 * Interface used to interact with the storage backend and perform stats operations on:
 * <ul>
 *     <li>{@link HistoricStats}</li>
 *     <li>{@link OffsetStats}</li>
 *     <li>{@link RetiredUserTcStats}</li>
 *     <li>{@link UserStats}</li>
 *     <li>{@link UserTcStats}</li>
 * </ul>
 */
public interface DbStatsManager {

    // TC operations

    void persistHourlyTcStats(final UserTcStats userTcStats);

    boolean isAnyHourlyTcStats();

    // Historic TC operations

    Collection<HistoricStats> getHistoricStatsHourly(final int userId, final int day, final Month month, final Year year);

    Collection<HistoricStats> getHistoricStatsDaily(final int userId, final Month month, final Year year);

    Collection<HistoricStats> getHistoricStatsMonthly(final int userId, final Year year);

    void persistInitialStats(final UserStats userStats);

    Optional<UserStats> getInitialStats(final int userId);

    Optional<UserTcStats> getHourlyTcStats(final int userId);

    void persistTotalStats(final UserStats stats);

    Optional<UserStats> getTotalStats(final int userId);

    void addOffsetStats(int userId, OffsetStats offsetStats);

    Optional<OffsetStats> addOrUpdateOffsetStats(final int userId, final OffsetStats offsetStats);

    Optional<OffsetStats> getOffsetStats(final int userId);

    void clearAllOffsetStats();

    int persistRetiredUserStats(final int teamId, final int userId, final String displayUserName, final UserTcStats retiredUserStats);

    Collection<RetiredUserTcStats> getRetiredUserStatsForTeam(final Team team);

    void deleteRetiredUserStats();
}

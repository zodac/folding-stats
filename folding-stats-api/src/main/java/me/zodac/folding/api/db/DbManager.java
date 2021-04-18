package me.zodac.folding.api.db;

import me.zodac.folding.api.exception.FoldingConflictException;
import me.zodac.folding.api.exception.FoldingException;
import me.zodac.folding.api.exception.NotFoundException;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.tc.stats.Stats;
import me.zodac.folding.api.tc.stats.UserStats;

import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.util.List;
import java.util.Map;

/**
 * Interface used to interact with the storage backend and perform CRUD operations.
 */
public interface DbManager {

    // CRUD operations

    /**
     * Creates a {@link Hardware} instance in the DB.
     *
     * @param hardware the {@link Hardware} to persist
     * @return the {@link Hardware} updated with an ID
     * @throws FoldingException thrown on error persisting the {@link Hardware}
     */
    Hardware createHardware(final Hardware hardware) throws FoldingException;

    List<Hardware> getAllHardware() throws FoldingException;

    Hardware getHardware(final int hardwareId) throws FoldingException, NotFoundException;

    void updateHardware(final Hardware hardware) throws FoldingException, NotFoundException;

    void deleteHardware(final int hardwareId) throws FoldingException, FoldingConflictException;

    User createUser(final User user) throws FoldingException;

    List<User> getAllUsers() throws FoldingException;

    User getUser(final int userId) throws FoldingException, NotFoundException;

    void updateUser(final User user) throws FoldingException, NotFoundException;

    void deleteUser(final int userId) throws FoldingException, FoldingConflictException;

    Team createTeam(final Team team) throws FoldingException;

    List<Team> getAllTeams() throws FoldingException;

    Team getTeam(final int foldingTeamId) throws FoldingException, NotFoundException;

    void updateTeam(final Team team) throws FoldingException, NotFoundException;

    void deleteTeam(final int teamId) throws FoldingException, FoldingConflictException;

    // TC operations

    Stats getFirstStatsForUser(final int userId, final Month month, final Year year) throws FoldingException, NotFoundException;

    Stats getLatestStatsForUser(final int userId, final Month month, final Year year) throws FoldingException, NotFoundException;

    void persistHourlyUserStats(final List<UserStats> userStats) throws FoldingException;

    boolean doTcStatsExist() throws FoldingException;

    // Historic TC operations

    Map<LocalDate, Stats> getDailyUserStats(final int userId, final Month month, final Year year) throws FoldingException, NotFoundException;
}

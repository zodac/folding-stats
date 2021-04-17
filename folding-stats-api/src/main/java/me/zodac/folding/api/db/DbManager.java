package me.zodac.folding.api.db;

import me.zodac.folding.api.FoldingStats;
import me.zodac.folding.api.FoldingTeam;
import me.zodac.folding.api.FoldingUser;
import me.zodac.folding.api.Hardware;
import me.zodac.folding.api.TeamStats;
import me.zodac.folding.api.UserStats;
import me.zodac.folding.api.exception.FoldingConflictException;
import me.zodac.folding.api.exception.FoldingException;
import me.zodac.folding.api.exception.NotFoundException;

import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.util.List;
import java.util.Map;

/**
 * Interface used to interact with the storage backend and perform CRUD operations.
 */
public interface DbManager {

    public static void main(final String[] args) {
        final String year = "nope";
        final String month = "4";

        System.out.println(Year.parse(year));
        System.out.println(Month.of(Integer.parseInt(month)));
    }

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

    FoldingUser createFoldingUser(final FoldingUser foldingUser) throws FoldingException;

    List<FoldingUser> getAllFoldingUsers() throws FoldingException;

    FoldingUser getFoldingUser(final int foldingUserId) throws FoldingException, NotFoundException;

    void updateFoldingUser(final FoldingUser foldingUser) throws FoldingException, NotFoundException;

    void deleteFoldingUser(final int foldingUserId) throws FoldingException, FoldingConflictException;

    FoldingTeam createFoldingTeam(final FoldingTeam foldingTeam) throws FoldingException;

    List<FoldingTeam> getAllFoldingTeams() throws FoldingException;

    FoldingTeam getFoldingTeam(final int foldingTeamId) throws FoldingException, NotFoundException;

    void updateFoldingTeam(final FoldingTeam foldingTeam) throws FoldingException, NotFoundException;

    void deleteFoldingTeam(final int foldingTeamId) throws FoldingException, FoldingConflictException;

    // TC operations

    UserStats getFirstPointsForUserInMonth(final FoldingUser foldingUser, final Month month, final Year year) throws FoldingException, NotFoundException;

    UserStats getCurrentPointsForUserInMonth(final FoldingUser foldingUser, final Month month, final Year year) throws FoldingException, NotFoundException;

    void persistHourlyUserTcStats(final List<FoldingStats> tcUserStats) throws FoldingException;

    void persistDailyUserTcStats(final List<FoldingStats> tcUserStats) throws FoldingException;

    void persistDailyTeamTcStats(final List<TeamStats> tcTeamStats) throws FoldingException;

    void persistMonthlyTeamTcStats(final List<TeamStats> tcTeamStats) throws FoldingException;

    // TODO: [zodac] To be removed
    boolean doTcStatsExist() throws FoldingException;

    // Historic TC operations

    Map<LocalDate, UserStats> getDailyUserStats(final int foldingUserId, final Month month, final Year year) throws FoldingException, NotFoundException;
}

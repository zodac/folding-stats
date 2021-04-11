package me.zodac.folding.api.db;

import me.zodac.folding.api.FoldingStats;
import me.zodac.folding.api.FoldingTeam;
import me.zodac.folding.api.FoldingUser;
import me.zodac.folding.api.Hardware;
import me.zodac.folding.api.UserStats;
import me.zodac.folding.api.exception.FoldingException;
import me.zodac.folding.api.exception.NotFoundException;

import java.time.Month;
import java.util.List;

/**
 * Interface used to interact with the storage backend and perform CRUD operations.
 */
public interface DbManager {

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

    FoldingUser createFoldingUser(final FoldingUser foldingUser) throws FoldingException;

    List<FoldingUser> getAllFoldingUsers() throws FoldingException;

    FoldingUser getFoldingUser(final int foldingUserId) throws FoldingException, NotFoundException;

    FoldingTeam createFoldingTeam(final FoldingTeam foldingTeam) throws FoldingException;

    List<FoldingTeam> getAllFoldingTeams() throws FoldingException;

    FoldingTeam getFoldingTeam(final int foldingTeamId) throws FoldingException, NotFoundException;

    // TODO: [zodac] Needs a better name
    void persistTcStats(final List<FoldingStats> foldingStats) throws FoldingException;

    boolean doesTcStatsExist() throws FoldingException;

    UserStats getFirstPointsForUserInMonth(final FoldingUser foldingUser, final Month month) throws FoldingException, NotFoundException;

    UserStats getCurrentPointsForUserInMonth(final FoldingUser foldingUser, final Month month) throws FoldingException, NotFoundException;
}

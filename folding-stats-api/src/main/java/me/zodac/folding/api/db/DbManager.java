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

// TODO: [zodac] Use this and dynamically instantiate, rather than hardcoding Postgres everywhere, like an idiot
public interface DbManager {

    Hardware createHardware(final Hardware hardware) throws FoldingException;

    List<Hardware> getAllHardware() throws FoldingException;

    Hardware getHardware(final String hardwareId) throws FoldingException, NotFoundException;

    FoldingUser createFoldingUser(final FoldingUser foldingUser) throws FoldingException;

    List<FoldingUser> getAllFoldingUsers() throws FoldingException;

    FoldingUser getFoldingUser(final String foldingUserId) throws FoldingException, NotFoundException;

    FoldingTeam createFoldingTeam(final FoldingTeam foldingTeam) throws FoldingException;

    List<FoldingTeam> getAllFoldingTeams() throws FoldingException;

    FoldingTeam getFoldingTeam(final String foldingTeamId) throws FoldingException, NotFoundException;

    // TODO: [zodac] Needs a better name
    void persistStats(final List<FoldingStats> foldingStats) throws FoldingException;

    UserStats getFirstPointsForUserInMonth(final FoldingUser foldingUser, final Month month) throws FoldingException, NotFoundException;

    UserStats getCurrentPointsForUserInMonth(final FoldingUser foldingUser, final Month month) throws FoldingException, NotFoundException;


}

package me.zodac.folding;

import me.zodac.folding.api.FoldingTeam;
import me.zodac.folding.api.FoldingUser;
import me.zodac.folding.api.Hardware;
import me.zodac.folding.api.UserStats;
import me.zodac.folding.api.exception.FoldingException;
import me.zodac.folding.api.exception.NotFoundException;
import me.zodac.folding.cache.FoldingTeamCache;
import me.zodac.folding.cache.FoldingUserCache;
import me.zodac.folding.cache.HardwareCache;
import me.zodac.folding.cache.tc.TcStatsCache;
import me.zodac.folding.db.postgres.PostgresDbManager;
import me.zodac.folding.parsing.FoldingStatsParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Singleton;
import java.util.List;

/**
 * In order to decouple the REST layer from the storage/persistence, we use this {@link StorageFacade} instead.
 * <p>
 * This way the {@link StorageFacade} is aware of caches or any other internal implementation, while the REST layer
 * does not need to know about them or any DBs being used.
 */
@Singleton
public class StorageFacade {

    private static final Logger LOGGER = LoggerFactory.getLogger(StorageFacade.class);

    // TODO: [zodac] Dynamically inject/instantiate the PostgresDbManager

    private final FoldingTeamCache foldingTeamCache = FoldingTeamCache.getInstance();
    private final FoldingUserCache foldingUserCache = FoldingUserCache.getInstance();
    private final HardwareCache hardwareCache = HardwareCache.getInstance();
    private final TcStatsCache tcStatsCache = TcStatsCache.getInstance();

    public Hardware createHardware(final Hardware hardware) throws FoldingException {
        final Hardware hardwareWithId = PostgresDbManager.createHardware(hardware);
        hardwareCache.add(hardwareWithId);
        return hardwareWithId;
    }

    public Hardware getHardware(final int hardwareId) throws FoldingException, NotFoundException {
        return getHardware(String.valueOf(hardwareId));
    }

    public Hardware getHardware(final String hardwareId) throws FoldingException, NotFoundException {
        try {
            return hardwareCache.get(hardwareId);
        } catch (final NotFoundException e) {
            LOGGER.debug("Unable to find hardware with ID {} in cache", hardwareId, e);
        }

        // Should be no need to get anything from the DB (since it should have been added to the cache when created)
        // But adding this just in case we decide to add some cache eviction in future
        final Hardware hardwareFromDb = PostgresDbManager.getHardware(hardwareId);
        hardwareCache.add(hardwareFromDb);

        return hardwareFromDb;
    }

    public List<Hardware> getAllHardware() throws FoldingException {
        final List<Hardware> allHardware = hardwareCache.getAll();

        if (!allHardware.isEmpty()) {
            return allHardware;
        }

        // Should be no need to get anything from the DB (since it should have been added to the cache when created)
        // But adding this just in case we decide to add some cache eviction in future
        final List<Hardware> allHardwareFromDb = PostgresDbManager.getAllHardware();
        hardwareCache.addAll(allHardwareFromDb);
        return allHardwareFromDb;
    }

    public FoldingUser createFoldingUser(final FoldingUser foldingUser) throws FoldingException {
        final FoldingUser foldingUserWithId = PostgresDbManager.createFoldingUser(foldingUser);
        foldingUserCache.add(foldingUserWithId);

        // When adding a new user, we should also configure the TC stats cache
        final UserStats currentStats = FoldingStatsParser.getTotalPointsForUser(foldingUser.getFoldingUserName(), foldingUser.getPasskey(), foldingUser.getFoldingTeamNumber());
        tcStatsCache.addInitialStats(foldingUserWithId.getId(), currentStats);


        return foldingUserWithId;
    }

    public FoldingUser getFoldingUser(final int foldingUserId) throws FoldingException, NotFoundException {
        return getFoldingUser(String.valueOf(foldingUserId));
    }

    public FoldingUser getFoldingUser(final String foldingUserId) throws FoldingException, NotFoundException {
        try {
            return foldingUserCache.get(foldingUserId);
        } catch (final NotFoundException e) {
            LOGGER.debug("Unable to find Folding user with ID {} in cache", foldingUserId, e);
        }

        // Should be no need to get anything from the DB (since it should have been added to the cache when created)
        // But adding this just in case we decide to add some cache eviction in future
        final FoldingUser foldingUserFromDb = PostgresDbManager.getFoldingUser(foldingUserId);
        foldingUserCache.add(foldingUserFromDb);

        return foldingUserFromDb;
    }

    public List<FoldingUser> getAllFoldingUsers() throws FoldingException {
        final List<FoldingUser> allFoldingUsers = foldingUserCache.getAll();

        if (!allFoldingUsers.isEmpty()) {
            return allFoldingUsers;
        }

        // Should be no need to get anything from the DB (since it should have been added to the cache when created)
        // But adding this just in case we decide to add some cache eviction in future
        final List<FoldingUser> allFoldingUsersFromDb = PostgresDbManager.getAllFoldingUsers();
        foldingUserCache.addAll(allFoldingUsersFromDb);
        return allFoldingUsers;
    }

    public FoldingTeam createFoldingTeam(final FoldingTeam foldingTeam) throws FoldingException {
        final FoldingTeam foldingTeamWithId = PostgresDbManager.createFoldingTeam(foldingTeam);
        foldingTeamCache.add(foldingTeamWithId);
        return foldingTeamWithId;
    }

    public FoldingTeam getFoldingTeam(final int foldingTeamId) throws FoldingException, NotFoundException {
        return getFoldingTeam(String.valueOf(foldingTeamId));
    }

    public FoldingTeam getFoldingTeam(final String foldingTeamId) throws FoldingException, NotFoundException {
        try {
            return foldingTeamCache.get(foldingTeamId);
        } catch (final NotFoundException e) {
            LOGGER.debug("Unable to find Folding team with ID {} in cache", foldingTeamId, e);
        }

        // Should be no need to get anything from the DB (since it should have been added to the cache when created)
        // But adding this just in case we decide to add some cache eviction in future
        final FoldingTeam foldingTeamFromDb = PostgresDbManager.getFoldingTeam(foldingTeamId);
        foldingTeamCache.add(foldingTeamFromDb);

        return foldingTeamFromDb;
    }

    public List<FoldingTeam> getAllFoldingTeams() throws FoldingException {
        final List<FoldingTeam> allFoldingTeams = foldingTeamCache.getAll();

        if (!allFoldingTeams.isEmpty()) {
            return allFoldingTeams;
        }

        // Should be no need to get anything from the DB (since it should have been added to the cache when created)
        // But adding this just in case we decide to add some cache eviction in future
        final List<FoldingTeam> allFoldingTeamsFromDb = PostgresDbManager.getAllFoldingTeams();
        foldingTeamCache.addAll(allFoldingTeamsFromDb);
        return allFoldingTeamsFromDb;
    }
}

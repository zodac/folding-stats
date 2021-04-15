package me.zodac.folding;

import me.zodac.folding.api.FoldingStats;
import me.zodac.folding.api.FoldingTeam;
import me.zodac.folding.api.FoldingUser;
import me.zodac.folding.api.Hardware;
import me.zodac.folding.api.TeamStats;
import me.zodac.folding.api.UserStats;
import me.zodac.folding.api.db.DbManager;
import me.zodac.folding.api.exception.FoldingException;
import me.zodac.folding.api.exception.NotFoundException;
import me.zodac.folding.cache.FoldingTeamCache;
import me.zodac.folding.cache.FoldingUserCache;
import me.zodac.folding.cache.HardwareCache;
import me.zodac.folding.cache.tc.TcStatsCache;
import me.zodac.folding.db.DbManagerRetriever;
import me.zodac.folding.parsing.FoldingStatsParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Singleton;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.toList;

/**
 * In order to decouple the REST layer from the storage/persistence, we use this {@link StorageFacade} instead.
 * <p>
 * This way the {@link StorageFacade} is aware of caches or any other internal implementation, while the REST layer
 * does not need to know about them or any DBs being used.
 */
@Singleton
public class StorageFacade {

    private static final Logger LOGGER = LoggerFactory.getLogger(StorageFacade.class);

    private final DbManager dbManager = DbManagerRetriever.get();
    private final FoldingTeamCache foldingTeamCache = FoldingTeamCache.get();
    private final FoldingUserCache foldingUserCache = FoldingUserCache.get();
    private final HardwareCache hardwareCache = HardwareCache.get();
    private final TcStatsCache tcStatsCache = TcStatsCache.get();

    public Hardware createHardware(final Hardware hardware) throws FoldingException {
        final Hardware hardwareWithId = dbManager.createHardware(hardware);
        hardwareCache.add(hardwareWithId);
        return hardwareWithId;
    }

    public Hardware getHardware(final int hardwareId) throws FoldingException, NotFoundException {
        try {
            return hardwareCache.get(hardwareId);
        } catch (final NotFoundException e) {
            LOGGER.debug("Unable to find hardware with ID {} in cache", hardwareId, e);
        }

        // Should be no need to get anything from the DB (since it should have been added to the cache when created)
        // But adding this just in case we decide to add some cache eviction in future
        final Hardware hardwareFromDb = dbManager.getHardware(hardwareId);
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
        final List<Hardware> allHardwareFromDb = dbManager.getAllHardware();
        hardwareCache.addAll(allHardwareFromDb);
        return allHardwareFromDb;
    }

    public FoldingUser createFoldingUser(final FoldingUser foldingUser) throws FoldingException {
        final FoldingUser foldingUserWithId = dbManager.createFoldingUser(foldingUser);
        foldingUserCache.add(foldingUserWithId);

        // When adding a new user, we should also configure the TC stats cache
        final UserStats currentStats = FoldingStatsParser.getStatsForUser(foldingUser.getFoldingUserName(), foldingUser.getPasskey(), foldingUser.getFoldingTeamNumber());
        tcStatsCache.addInitialStats(foldingUserWithId.getId(), currentStats);

        return foldingUserWithId;
    }

    public FoldingUser getFoldingUser(final int foldingUserId) throws FoldingException, NotFoundException {
        try {
            return foldingUserCache.get(foldingUserId);
        } catch (final NotFoundException e) {
            LOGGER.debug("Unable to find Folding user with ID {} in cache", foldingUserId, e);
        }

        // Should be no need to get anything from the DB (since it should have been added to the cache when created)
        // But adding this just in case we decide to add some cache eviction in future
        final FoldingUser foldingUserFromDb = dbManager.getFoldingUser(foldingUserId);
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
        final List<FoldingUser> allFoldingUsersFromDb = dbManager.getAllFoldingUsers();
        foldingUserCache.addAll(allFoldingUsersFromDb);
        return allFoldingUsersFromDb;
    }

    public FoldingTeam createFoldingTeam(final FoldingTeam foldingTeam) throws FoldingException {
        final FoldingTeam foldingTeamWithId = dbManager.createFoldingTeam(foldingTeam);
        foldingTeamCache.add(foldingTeamWithId);
        return foldingTeamWithId;
    }

    public FoldingTeam getFoldingTeam(final int foldingTeamId) throws FoldingException, NotFoundException {
        try {
            return foldingTeamCache.get(foldingTeamId);
        } catch (final NotFoundException e) {
            LOGGER.debug("Unable to find Folding team with ID {} in cache", foldingTeamId, e);
        }

        // Should be no need to get anything from the DB (since it should have been added to the cache when created)
        // But adding this just in case we decide to add some cache eviction in future
        final FoldingTeam foldingTeamFromDb = dbManager.getFoldingTeam(foldingTeamId);
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
        final List<FoldingTeam> allFoldingTeamsFromDb = dbManager.getAllFoldingTeams();
        foldingTeamCache.addAll(allFoldingTeamsFromDb);
        return allFoldingTeamsFromDb;
    }

    public List<FoldingTeam> getTcTeams() {
        try {
            return getAllFoldingTeams();
        } catch (final FoldingException e) {
            LOGGER.warn("Error retrieving TC Folding teams", e.getCause());
            return Collections.emptyList();
        }
    }

    public List<FoldingUser> getTcUsers() {
        try {
            return getAllFoldingTeams()
                    .stream()
                    .map(FoldingTeam::getUserIds)
                    .flatMap(Collection::stream)
                    .map(userId -> FoldingUserCache.get().getOrNull(userId))
                    .filter(Objects::nonNull)
                    .collect(toList());
        } catch (final FoldingException e) {
            LOGGER.warn("Error retrieving TC Folding users", e.getCause());
            return Collections.emptyList();
        }
    }

    public void persistDailyUserTcStats(final List<FoldingStats> tcFoldingStats) {
        try {
            dbManager.persistDailyUserTcStats(tcFoldingStats);
        } catch (final FoldingException e) {
            LOGGER.error("Error persisting daily TC user stats", e.getCause());
        } catch (final Exception e) {
            LOGGER.error("Unexpected error persisting daily TC user stats", e);
        }
    }

    public void persistDailyTeamTcStats(final List<TeamStats> tcTeamStats) {
        try {
            dbManager.persistDailyTeamTcStats(tcTeamStats);
        } catch (final FoldingException e) {
            LOGGER.error("Error persisting daily TC team stats", e.getCause());
        } catch (final Exception e) {
            LOGGER.error("Unexpected error persisting daily team TC stats", e);
        }
    }

    public void persistMonthlyTeamTcStats(final List<TeamStats> tcTeamStats) {
        try {
            dbManager.persistMonthlyTeamTcStats(tcTeamStats);
        } catch (final FoldingException e) {
            LOGGER.error("Error persisting monthly TC team stats", e.getCause());
        } catch (final Exception e) {
            LOGGER.error("Unexpected error persisting monthly team TC stats", e);
        }
    }
}

package me.zodac.folding;

import me.zodac.folding.api.FoldingTeam;
import me.zodac.folding.api.FoldingUser;
import me.zodac.folding.api.Hardware;
import me.zodac.folding.api.exception.FoldingException;
import me.zodac.folding.cache.FoldingTeamCache;
import me.zodac.folding.cache.FoldingUserCache;
import me.zodac.folding.cache.HardwareCache;
import me.zodac.folding.db.postgres.PostgresDbManager;

import javax.ejb.Singleton;
import java.util.List;
import java.util.Optional;

/**
 * In order to decouple the REST layer from the storage/persistence, we use this {@link StorageFacade} instead.
 * <p>
 * This way the {@link StorageFacade} is aware of caches or any other internal implementation, while the REST layer
 * does not need to know about them or any DBs being used.
 */
@Singleton
public class StorageFacade {

    // TODO: [zodac] Dynamically inject/instantiate the PostgresDbManager

    private final HardwareCache hardwareCache = HardwareCache.getInstance();
    private final FoldingUserCache foldingUserCache = FoldingUserCache.getInstance();
    private final FoldingTeamCache foldingTeamCache = FoldingTeamCache.getInstance();

    public Hardware createHardware(final Hardware hardware) throws FoldingException {
        final Hardware hardwareWithId = PostgresDbManager.createHardware(hardware);
        hardwareCache.add(hardwareWithId);
        return hardwareWithId;
    }

    public Hardware getHardware(final String hardwareId) throws FoldingException {
        final Optional<Hardware> hardware = hardwareCache.get(hardwareId);

        if (hardware.isPresent()) {
            return hardware.get();
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
        return foldingUserWithId;
    }

    public FoldingUser getFoldingUser(final String foldingUserId) throws FoldingException {
        final Optional<FoldingUser> foldingUser = foldingUserCache.get(foldingUserId);

        if (foldingUser.isPresent()) {
            return foldingUser.get();
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

    public FoldingTeam getFoldingTeam(final String foldingTeamId) throws FoldingException {
        final Optional<FoldingTeam> foldingTeam = foldingTeamCache.get(foldingTeamId);

        if (foldingTeam.isPresent()) {
            return foldingTeam.get();
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

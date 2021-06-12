package me.zodac.folding.ejb.core;

import me.zodac.folding.api.db.DbManager;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.cache.HardwareCache;
import me.zodac.folding.cache.UserCache;
import me.zodac.folding.db.DbManagerRetriever;
import me.zodac.folding.ejb.OldFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Optional;

/**
 * In order to decouple both the REST layer and the {@link BusinessLogicEjb} from the persistence
 * solution we use this interface for CRUD operations.
 * <p>
 * Since some of the persisted data can be cached, we don't want any other modules of the
 * codebase to need to worry about DB vs cache access, and instead encapsulate all of that logic here.
 * <p>
 * <b>NOTE:</b> Should only be used by {@link BusinessLogicEjb}, other classes should not go use this class.
 */
// TODO: [zodac] Should replace the cache miss warnings with some metrics instead?
final class Storage {

    private static final Logger LOGGER = LoggerFactory.getLogger(OldFacade.class);
    private static final DbManager DB_MANAGER = DbManagerRetriever.get();
    private static final Storage INSTANCE = new Storage();

    private transient final HardwareCache hardwareCache = HardwareCache.getInstance();
    private transient final UserCache userCache = UserCache.getInstance();

    private Storage() {

    }

    static Storage getInstance() {
        return INSTANCE;
    }

    /**
     * Creates a {@link Hardware}.
     * <p>
     * Persists it with the {@link DbManager}, then adds it to the {@link HardwareCache}.
     *
     * @param hardware the {@link Hardware} to create
     * @return the created {@link Hardware}, with ID
     * @see DbManager#createHardware(Hardware)
     */
    public Hardware createHardware(final Hardware hardware) {
        final Hardware hardwareWithId = DB_MANAGER.createHardware(hardware);
        hardwareCache.add(hardwareWithId);
        return hardwareWithId;
    }

    /**
     * Retrieves a {@link Hardware}.
     * <p>
     * First attempts to retrieve from {@link HardwareCache}, then if none exists, attempts to retrieve from the
     * {@link DbManager}.
     *
     * @param hardwareId the ID of the {@link Hardware} to retrieve
     * @return an {@link Optional} of the retrieved {@link Hardware}
     * @see DbManager#getHardware(int)
     */
    Optional<Hardware> getHardware(final int hardwareId) {
        final Optional<Hardware> fromCache = hardwareCache.get(hardwareId);

        if (fromCache.isPresent()) {
            return fromCache;
        }

        LOGGER.trace("Cache miss! Get hardware");
        final Optional<Hardware> fromDb = DB_MANAGER.getHardware(hardwareId);
        fromDb.ifPresent(hardwareCache::add);
        return fromDb;
    }

    /**
     * Retrieves all {@link Hardware}.
     * <p>
     * First attempts to retrieve from {@link HardwareCache}, then if none exists, attempts to retrieve from the
     * {@link DbManager}.
     *
     * @return a {@link Collection} of the retrieved {@link Hardware}
     * @see DbManager#getAllHardware()
     */
    Collection<Hardware> getAllHardware() {
        final Collection<Hardware> fromCache = hardwareCache.getAll();

        if (!fromCache.isEmpty()) {
            return fromCache;
        }

        LOGGER.trace("Cache miss! Get all hardware");
        final Collection<Hardware> fromDb = DB_MANAGER.getAllHardware();
        hardwareCache.addAll(fromDb);
        return fromDb;
    }

    /**
     * Updates a {@link Hardware}.
     * <p>
     * Persists it with the {@link DbManager}, then updates it in the {@link HardwareCache}.
     *
     * @param updatedHardware the {@link Hardware} to update
     * @see DbManager#updateHardware(Hardware)
     */
    void updateHardware(final Hardware updatedHardware) {
        DB_MANAGER.updateHardware(updatedHardware);
        hardwareCache.add(updatedHardware);
    }

    /**
     * Deletes a {@link Hardware}.
     * <p>
     * Deletes it with the {@link DbManager}, then removes it to the {@link HardwareCache}.
     *
     * @param hardwareId the ID of the {@link Hardware} to delete
     * @see DbManager#deleteHardware(int)
     */
    public void deleteHardware(final int hardwareId) {
        DB_MANAGER.deleteHardware(hardwareId);
        hardwareCache.remove(hardwareId);
    }

    /**
     * Evicts a {@link User} from the {@link UserCache}.
     * <p>
     * Used in scenarios where something a {@link User} references, like a {@link Hardware} or {@link Team} has been
     * updated, but the {@link User} itself does not require an update in the DB (since we only store a reference to the
     * ID of the {@link Hardware}/{@link Team}. This will force the next retrieval to go direct to the DB and retrieve
     * the updated {@link Hardware}/{@link Team} details for the {@link User}.
     *
     * @param userId the ID of the {@link User} to evict
     */
    public void evictUserFromCache(final int userId) {
        userCache.remove(userId);
    }
}

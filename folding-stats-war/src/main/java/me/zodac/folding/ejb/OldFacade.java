package me.zodac.folding.ejb;

import java.util.Collection;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import me.zodac.folding.api.db.DbManager;
import me.zodac.folding.api.ejb.BusinessLogic;
import me.zodac.folding.api.exception.ExternalConnectionException;
import me.zodac.folding.api.stats.FoldingStatsRetriever;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.tc.stats.RetiredUserTcStats;
import me.zodac.folding.api.tc.stats.UserStats;
import me.zodac.folding.api.tc.stats.UserTcStats;
import me.zodac.folding.cache.HardwareCache;
import me.zodac.folding.cache.UserCache;
import me.zodac.folding.db.DbManagerRetriever;
import me.zodac.folding.ejb.tc.user.UserStateChangeChecker;
import me.zodac.folding.ejb.tc.user.UserStatsParser;
import me.zodac.folding.stats.HttpFoldingStatsRetriever;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Deprecated
@Singleton
public class OldFacade {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final FoldingStatsRetriever FOLDING_STATS_RETRIEVER = HttpFoldingStatsRetriever.create();

    private final transient DbManager dbManager = DbManagerRetriever.get();
    private final transient UserCache userCache = UserCache.getInstance();
    private final transient HardwareCache hardwareCache = HardwareCache.getInstance();

    @EJB
    private transient BusinessLogic businessLogic;

    @EJB
    private transient UserStatsParser userStatsParser;

    @EJB
    private transient UserStateChangeChecker userStateChangeHandler;

    public Hardware updateHardware(final Hardware updatedHardware, final Hardware existingHardware) throws ExternalConnectionException {
        dbManager.updateHardware(updatedHardware);
        hardwareCache.add(updatedHardware.getId(), updatedHardware);

        final Collection<User> usersUsingThisHardware = businessLogic.getUsersWithHardware(updatedHardware);
        for (final User user : usersUsingThisHardware) {
            if (userStateChangeHandler.isHardwareStateChange(updatedHardware, existingHardware)) {
                LOGGER.debug("User '{}' (ID: {}) had state change to hardware multiplier", user.getDisplayName(), user.getId());
                userStateChangeHandler.handleStateChange(user);
            }

            final User updatedUser = User.updateHardware(user, updatedHardware);
            userCache.add(updatedUser.getId(), updatedUser);
        }

        return updatedHardware;
    }

    public User createUser(final User user) throws ExternalConnectionException {
        final User userWithId = dbManager.createUser(user);
        userCache.add(userWithId.getId(), userWithId);

        // When adding a new user, we configure the initial stats DB/cache
        final UserStats currentUserStats = FOLDING_STATS_RETRIEVER.getTotalStats(userWithId);
        final UserStats initialStats = businessLogic.createInitialStats(currentUserStats);
        LOGGER.info("User '{}' (ID: {}) created with initial stats: {}", userWithId.getDisplayName(), userWithId.getId(), initialStats);
        userStatsParser.parseTcStatsForUser(userWithId);

        return userWithId;
    }

    public User updateUser(final User updatedUser, final User existingUser) throws ExternalConnectionException {
        dbManager.updateUser(updatedUser);
        userCache.add(updatedUser.getId(), updatedUser);

        if (userStateChangeHandler.isUserStateChange(updatedUser, existingUser)) {
            userStateChangeHandler.handleStateChange(updatedUser);
        }

        LOGGER.trace("User updated with any required state changes");
        return updatedUser;
    }

    public void deleteUser(final User user) {
        final int userId = user.getId();
        dbManager.deleteUser(userId);
        userCache.remove(userId);

        final UserTcStats userStats = businessLogic.getHourlyTcStats(user);

        if (userStats.isEmptyStats()) {
            LOGGER.warn("User '{}' (ID: {}) has no stats, not saving any retired stats", user.getDisplayName(), user.getId());
            return;
        }

        final RetiredUserTcStats retiredUserTcStats = RetiredUserTcStats.createWithoutId(user.getTeam().getId(), user.getDisplayName(), userStats);
        final RetiredUserTcStats createdRetiredUserTcStats = businessLogic.createRetiredUserStats(retiredUserTcStats);
        LOGGER.info("User '{}' (ID: {}) retired with retired stats ID: {}", user.getDisplayName(), user.getId(),
            createdRetiredUserTcStats.getRetiredUserId());
    }
}

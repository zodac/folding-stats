package me.zodac.folding.ejb;

import java.math.BigDecimal;
import java.util.Collection;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import me.zodac.folding.ParsingStateManager;
import me.zodac.folding.api.ParsingState;
import me.zodac.folding.api.db.DbManager;
import me.zodac.folding.api.ejb.BusinessLogic;
import me.zodac.folding.api.exception.ExternalConnectionException;
import me.zodac.folding.api.stats.FoldingStatsRetriever;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.tc.stats.OffsetTcStats;
import me.zodac.folding.api.tc.stats.RetiredUserTcStats;
import me.zodac.folding.api.tc.stats.UserStats;
import me.zodac.folding.api.tc.stats.UserTcStats;
import me.zodac.folding.cache.HardwareCache;
import me.zodac.folding.cache.UserCache;
import me.zodac.folding.db.DbManagerRetriever;
import me.zodac.folding.ejb.tc.UserStatsParser;
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

    public Hardware updateHardware(final Hardware updatedHardware, final Hardware existingHardware) throws ExternalConnectionException {
        dbManager.updateHardware(updatedHardware);
        hardwareCache.add(updatedHardware.getId(), updatedHardware);

        final Collection<User> usersUsingThisHardware = businessLogic.getUsersWithHardware(updatedHardware);

        // Using BigDecimal since equality checks with doubles can be imprecise
        final BigDecimal existingMultiplier = BigDecimal.valueOf(existingHardware.getMultiplier());
        final BigDecimal updatedMultiplier = BigDecimal.valueOf(updatedHardware.getMultiplier());
        final boolean isHardwareMultiplierChange = !existingMultiplier.equals(updatedMultiplier);

        for (final User user : usersUsingThisHardware) {
            if (isHardwareMultiplierChange) {
                LOGGER.debug("User '{}' (ID: {}) had state change to hardware multiplier", user.getDisplayName(), user.getId());
                handleStateChangeForUser(user);
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

        if (existingUser.getHardware().getId() != updatedUser.getHardware().getId()) {
            LOGGER.debug("User '{}' (ID: {}) had state change to hardware, {} -> {}, recalculating initial stats", existingUser.getDisplayName(),
                existingUser.getId(), existingUser.getHardware(), updatedUser.getHardware());
            handleStateChangeForUser(updatedUser);
            return updatedUser;
        }

        if (existingUser.getTeam().getId() != updatedUser.getTeam().getId()) {
            LOGGER.debug("User '{}' (ID: {}) had state change to team, {} -> {}, recalculating initial stats", existingUser.getDisplayName(),
                existingUser.getId(), existingUser.getTeam(), updatedUser.getTeam());
            handleStateChangeForUser(updatedUser);
            return updatedUser;
        }

        if (!existingUser.getFoldingUserName().equalsIgnoreCase(updatedUser.getFoldingUserName())) {
            LOGGER.debug("User '{}' (ID: {}) had state change to Folding username, {} -> {}, recalculating initial stats",
                existingUser.getDisplayName(), existingUser.getId(), existingUser.getFoldingUserName(), updatedUser.getFoldingUserName());
            handleStateChangeForUser(updatedUser);
            return updatedUser;
        }

        if (!existingUser.getPasskey().equalsIgnoreCase(updatedUser.getPasskey())) {
            LOGGER.debug("User '{}' (ID: {}) had state change to passkey, {} -> {}, recalculating initial stats", existingUser.getDisplayName(),
                existingUser.getId(), existingUser.getPasskey(), updatedUser.getPasskey());
            handleStateChangeForUser(updatedUser);
            return updatedUser;
        }

        LOGGER.trace("User updated with any required state changes");
        return updatedUser;
    }

    // If a user is updated and their Folding username, hardware ID or passkey is changed, we need to update their initial offset again
    // Also occurs if the hardware multiplier for a hardware used by a user is changed
    // We set the new initial stats to the user's current total stats, then give an offset of their current TC stats (multiplied)
    private void handleStateChangeForUser(final User userWithStateChange) throws ExternalConnectionException {
        if (ParsingStateManager.current() == ParsingState.DISABLED) {
            LOGGER.debug("Received a state change for user '{}' (ID: {}), but system is not currently parsing stats",
                userWithStateChange.getDisplayName(), userWithStateChange.getId());
            return;
        }

        final UserStats userTotalStats = FOLDING_STATS_RETRIEVER.getTotalStats(userWithStateChange);
        LOGGER.debug("Setting initial stats to: {}", userTotalStats);
        businessLogic.createInitialStats(userTotalStats);

        final UserTcStats currentUserTcStats = businessLogic.getHourlyTcStats(userWithStateChange);
        final OffsetTcStats offsetTcStats =
            OffsetTcStats.create(currentUserTcStats.getPoints(), currentUserTcStats.getMultipliedPoints(), currentUserTcStats.getUnits());
        LOGGER.debug("Adding offset stats of: {}", offsetTcStats);
        businessLogic.deleteOffsetStats(userWithStateChange);
        final OffsetTcStats createdOffsetStats = businessLogic.createOrUpdateOffsetStats(userWithStateChange, offsetTcStats);
        LOGGER.debug("User now has offset stats of: {}", createdOffsetStats);
        LOGGER.info("Handled state change for user '{}' (ID: {})", userWithStateChange.getDisplayName(), userWithStateChange.getId());
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

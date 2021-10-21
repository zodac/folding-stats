package me.zodac.folding.ejb.tc.user;

import java.math.BigDecimal;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import me.zodac.folding.ParsingStateManager;
import me.zodac.folding.api.ejb.BusinessLogic;
import me.zodac.folding.api.exception.ExternalConnectionException;
import me.zodac.folding.api.state.ParsingState;
import me.zodac.folding.api.stats.FoldingStatsRetriever;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.tc.stats.OffsetTcStats;
import me.zodac.folding.api.tc.stats.UserStats;
import me.zodac.folding.api.tc.stats.UserTcStats;
import me.zodac.folding.stats.HttpFoldingStatsRetriever;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Handles the state change for a {@link User}, when it (or something it relies on) is modified and their <code>Team Competition</code> stats need to
 * be adjusted.
 */
@Singleton
public class UserStateChangeHandler {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final FoldingStatsRetriever FOLDING_STATS_RETRIEVER = HttpFoldingStatsRetriever.create();

    @EJB
    private BusinessLogic businessLogic;

    /**
     * Checks if the updated {@link User} requires a state change for the <code>Team Competition</code>.
     *
     * <p>
     * A state change is required if the updated {@link User} has changed:
     * <ul>
     *     <li>{@link User}'s Folding@Home username</li>
     *     <li>{@link User}'s Folding@Home passkey</li>
     *     <li>{@link User}'s {@link Hardware}</li>
     *     <li>{@link User}'s {@link Team}</li>
     * </ul>
     *
     * @param updatedUser  the updated {@link User} to check
     * @param existingUser the existing {@link User} to compare against
     * @return <code>true</code> if the {@link User} requires a state change
     * @see #handleStateChange(User)
     */
    public boolean isUserStateChange(final User updatedUser, final User existingUser) {
        if (existingUser.getHardware().getId() != updatedUser.getHardware().getId()) {
            LOGGER.debug("User '{}' (ID: {}) had state change to hardware, {} -> {}", existingUser.getDisplayName(),
                existingUser.getId(), existingUser.getHardware(), updatedUser.getHardware());
            return true;
        }

        if (existingUser.getTeam().getId() != updatedUser.getTeam().getId()) {
            LOGGER.debug("User '{}' (ID: {}) had state change to team, {} -> {}", existingUser.getDisplayName(),
                existingUser.getId(), existingUser.getTeam(), updatedUser.getTeam());
            return true;
        }

        if (!existingUser.getFoldingUserName().equalsIgnoreCase(updatedUser.getFoldingUserName())) {
            LOGGER.debug("User '{}' (ID: {}) had state change to Folding username, {} -> {}",
                existingUser.getDisplayName(), existingUser.getId(), existingUser.getFoldingUserName(), updatedUser.getFoldingUserName());
            return true;
        }

        if (!existingUser.getPasskey().equalsIgnoreCase(updatedUser.getPasskey())) {
            LOGGER.debug("User '{}' (ID: {}) had state change to passkey, {} -> {}", existingUser.getDisplayName(),
                existingUser.getId(), existingUser.getPasskey(), updatedUser.getPasskey());
            return true;
        }

        LOGGER.debug("No state change required for updated user '{}' (ID: {})", updatedUser.getDisplayName(), updatedUser.getId());
        return false;
    }

    /**
     * Checks if the updated {@link Hardware} requires a state change to any {@link User}s that use it for the <code>Team Competition</code>.
     *
     * <p>
     * A state change is required if the updated {@link Hardware} has changed:
     * <ul>
     *     <li>{@link Hardware}'s multiplier</li>
     * </ul>
     *
     * @param updatedHardware  the updated {@link Hardware} to check
     * @param existingHardware the existing {@link Hardware} to compare against
     * @return <code>true</code> if any {@link User}s using the {@link Hardware} require a state change
     * @see #handleStateChange(User)
     */
    public boolean isHardwareStateChange(final Hardware updatedHardware, final Hardware existingHardware) {
        // Using BigDecimal since equality checks with doubles can be imprecise
        final BigDecimal existingMultiplier = BigDecimal.valueOf(existingHardware.getMultiplier());
        final BigDecimal updatedMultiplier = BigDecimal.valueOf(updatedHardware.getMultiplier());
        final boolean isMultiplierChange = !existingMultiplier.equals(updatedMultiplier);

        if (isMultiplierChange) {
            LOGGER.debug("Hardware '{}' (ID: {}) had state change to multiplier, {} -> {}", updatedHardware.getId(),
                updatedHardware.getHardwareName(), existingHardware.getMultiplier(), updatedHardware.getMultiplier());
        }

        return isMultiplierChange;
    }

    // If a user is updated and their Folding username, hardware ID or passkey is changed, we need to update their initial offset again
    // Also occurs if the hardware multiplier for a hardware used by a user is changed
    // We set the new initial stats to the user's current total stats, then give an offset of their current TC stats (multiplied)

    /**
     * This should be called if any of the following are changed:
     * <ul>
     *     <li>{@link User}'s Folding@Home username</li>
     *     <li>{@link User}'s Folding@Home passkey</li>
     *     <li>{@link User}'s {@link Hardware}</li>
     *     <li>{@link User}'s {@link Team}</li>
     *     <li>The {@code multiplier} of a {@link Hardware} being used by the {@link User}</li>
     * </ul>
     *
     * <p>
     * We want to restart their <code>Team Competition</code> calculation with their new information, so we do the following:
     * <ol>
     *     <li>Take their current {@link UserTcStats} and create some {@link OffsetTcStats} for the same amount</li>
     *     <li>Set their initial {@link UserStats} to their current total {@link UserStats}</li>
     * </ol>
     *
     * <p>
     * This allows all new stats to be collected based off their changed information, while retaining their existing points for the
     * <code>Team Competition</code>.
     *
     * <p>
     * <b>NOTE:</b> If the currently {@link ParsingState} is {@link ParsingState#DISABLED}, no changes will be made to the {@link User}.
     *
     * @param userWithStateChange the {@link User} which had its state change
     */
    public void handleStateChange(final User userWithStateChange) {
        if (ParsingStateManager.current() == ParsingState.DISABLED) {
            LOGGER.debug("Received a state change for user '{}' (ID: {}), but system is not currently parsing stats",
                userWithStateChange.getDisplayName(), userWithStateChange.getId());
            return;
        }

        try {
            final UserStats userTotalStats = FOLDING_STATS_RETRIEVER.getTotalStats(userWithStateChange);
            LOGGER.debug("Setting initial stats to: {}", userTotalStats);
            businessLogic.createInitialStats(userTotalStats);

            final UserTcStats currentUserTcStats = businessLogic.getHourlyTcStats(userWithStateChange);
            final OffsetTcStats offsetTcStats = OffsetTcStats.create(currentUserTcStats);
            final OffsetTcStats createdOffsetStats = businessLogic.createOffsetStats(userWithStateChange, offsetTcStats);
            LOGGER.debug("Added offset stats of: {}", createdOffsetStats);

            LOGGER.info("Handled state change for user '{}' (ID: {})", userWithStateChange.getDisplayName(), userWithStateChange.getId());
        } catch (final ExternalConnectionException e) {
            LOGGER.error("Unable to update the state of user '{}' (ID: {})", userWithStateChange.getDisplayName(), userWithStateChange.getId(), e);
        }
    }
}

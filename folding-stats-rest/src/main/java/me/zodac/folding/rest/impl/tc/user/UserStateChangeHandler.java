/*
 * MIT License
 *
 * Copyright (c) 2021 zodac.me
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package me.zodac.folding.rest.impl.tc.user;

import java.math.BigDecimal;
import me.zodac.folding.api.exception.ExternalConnectionException;
import me.zodac.folding.api.state.ParsingState;
import me.zodac.folding.api.stats.FoldingStatsRetriever;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.tc.stats.OffsetTcStats;
import me.zodac.folding.api.tc.stats.UserStats;
import me.zodac.folding.api.tc.stats.UserTcStats;
import me.zodac.folding.rest.api.FoldingStatsService;
import me.zodac.folding.rest.api.tc.user.UserStateChangeHandlerService;
import me.zodac.folding.state.ParsingStateManager;
import me.zodac.folding.stats.HttpFoldingStatsRetriever;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Handles the state change for a {@link User}, when it (or something it relies on) is modified and their <code>Team Competition</code> stats need to
 * be adjusted.
 */
@Component
public class UserStateChangeHandler implements UserStateChangeHandlerService {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final FoldingStatsRetriever FOLDING_STATS_RETRIEVER = HttpFoldingStatsRetriever.create();

    @Autowired
    private FoldingStatsService foldingStatsCore;

    @Override
    public boolean isUserStateChange(final User updatedUser, final User existingUser) {
        if (existingUser.getHardware().getId() != updatedUser.getHardware().getId()) {
            LOGGER.debug("User '{}' (ID: {}) had state change to hardware, {} -> {}", existingUser.getDisplayName(),
                existingUser.getId(), existingUser.getHardware(), updatedUser.getHardware());
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

    @Override
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

    @Override
    public void handleStateChange(final User userWithStateChange) {
        if (ParsingStateManager.current() == ParsingState.DISABLED) {
            LOGGER.debug("Received a state change for user '{}' (ID: {}), but system is not currently parsing stats",
                userWithStateChange.getDisplayName(), userWithStateChange.getId());
            return;
        }

        try {
            final UserStats userTotalStats = FOLDING_STATS_RETRIEVER.getTotalStats(userWithStateChange);
            LOGGER.debug("Setting initial stats to: {}", userTotalStats);
            foldingStatsCore.createInitialStats(userTotalStats);

            final UserTcStats currentUserTcStats = foldingStatsCore.getHourlyTcStats(userWithStateChange);
            final OffsetTcStats offsetTcStats = OffsetTcStats.create(currentUserTcStats);
            final OffsetTcStats createdOffsetStats = foldingStatsCore.createOffsetStats(userWithStateChange, offsetTcStats);
            LOGGER.debug("Added offset stats of: {}", createdOffsetStats);

            LOGGER.info("Handled state change for user '{}' (ID: {})", userWithStateChange.getDisplayName(), userWithStateChange.getId());
        } catch (final ExternalConnectionException e) {
            LOGGER.error("Unable to update the state of user '{}' (ID: {})", userWithStateChange.getDisplayName(), userWithStateChange.getId(), e);
        }
    }
}
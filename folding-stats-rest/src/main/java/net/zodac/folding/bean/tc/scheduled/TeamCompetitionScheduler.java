/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2025 zodac.net
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package net.zodac.folding.bean.tc.scheduled;

import net.zodac.folding.api.state.ParsingState;
import net.zodac.folding.api.state.SystemState;
import net.zodac.folding.api.tc.Hardware;
import net.zodac.folding.api.tc.change.UserChange;
import net.zodac.folding.api.util.EnvironmentVariableUtils;
import net.zodac.folding.bean.tc.lars.LarsHardwareUpdater;
import net.zodac.folding.bean.tc.user.UserChangeApplier;
import net.zodac.folding.bean.tc.user.UserStatsResetter;
import net.zodac.folding.bean.tc.user.UserStatsStorer;
import net.zodac.folding.state.ParsingStateManager;
import net.zodac.folding.state.SystemStateManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * {@link Scheduled} {@link Component} which schedules the beginning and end of the {@code Team Competition}.
 *
 * <p>
 * The first day of the {@code Team Competition} is set to the <b>3rd</b> of the month, and on this day it will reset the stats for all users.
 * The reset will occur at <b>00:15</b>. This time cannot be changed, but the reset can be disabled using the environment variable:
 * <ul>
 *     <li>{@code ENABLE_STATS_MONTHLY_RESET}</li>
 * </ul>
 *
 * <p>
 * The last day of the {@code Team Competition} is the final day of the month, and on this day the following actions are performed:
 * <ol>
 *     <li>Resets the stats for all users. The reset will occur at <b>00:15</b>. This time cannot be changed, but the reset can be disabled using
 *     the environment variable:
 *         <ul>
 *             <li>{@code ENABLE_STATS_MONTHLY_RESET}</li>
 *         </ul>
 *     </li>
 *     <li>Persists the result of that month. The monthly result time cannot be configured, but it can be disabled using the environment variable:
 *         <ul>
 *             <li>{@code ENABLE_MONTHLY_RESULT_STORAGE}</li>
 *         </ul>
 *     </li>
 *     <li>Updates the value of all {@link Hardware} from the LARS PPD database</li>
 *     <li>Applies any pending {@link UserChange}s for the next month with {@link UserChangeApplier}</li>
 * </ol>
 */
@Component
public class TeamCompetitionScheduler {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final boolean IS_MONTHLY_RESET_ENABLED = EnvironmentVariableUtils.isEnabled("ENABLE_STATS_MONTHLY_RESET");
    private static final boolean IS_MONTHLY_RESULT_ENABLED = EnvironmentVariableUtils.isEnabled("ENABLE_MONTHLY_RESULT_STORAGE");
    private static final boolean IS_LARS_UPDATE_ENABLED = EnvironmentVariableUtils.isEnabled("ENABLE_LARS_HARDWARE_UPDATE");

    private final LarsHardwareUpdater larsHardwareUpdater;
    private final UserChangeApplier userChangeApplier;
    private final UserStatsResetter userStatsResetter;
    private final UserStatsStorer userStatsStorer;

    /**
     * {@link Autowired} constructor.
     *
     * @param larsHardwareUpdater the {@link LarsHardwareUpdater}
     * @param userChangeApplier   the {@link UserChangeApplier}
     * @param userStatsResetter   the {@link UserStatsResetter}
     * @param userStatsStorer     the {@link UserStatsStorer}
     */
    @Autowired
    public TeamCompetitionScheduler(final LarsHardwareUpdater larsHardwareUpdater,
                                    final UserChangeApplier userChangeApplier,
                                    final UserStatsResetter userStatsResetter,
                                    final UserStatsStorer userStatsStorer) {
        this.larsHardwareUpdater = larsHardwareUpdater;
        this.userChangeApplier = userChangeApplier;
        this.userStatsResetter = userStatsResetter;
        this.userStatsStorer = userStatsStorer;
    }

    /**
     * Scheduled execution to prepare the {@code Team Competition} for the new month.
     *
     * @see UserStatsResetter#resetTeamCompetitionStatsForEndOfMonth()
     */
    @Scheduled(cron = "0 15 0 3 * *", zone = "UTC") // TC starts on the 3rd of the month
    public void startOfTeamCompetition() {
        try {
            LOGGER.trace("Method #startOfTeamCompetition() fired");
            LOGGER.warn("Starting TC stats for new month");

            SystemStateManager.next(SystemState.RESETTING_STATS);
            ParsingStateManager.next(ParsingState.DISABLED);
            userStatsResetter.resetTeamCompetitionStatsForEndOfMonth();
            SystemStateManager.next(SystemState.WRITE_EXECUTED);
        } catch (final Exception e) {
            LOGGER.error("Error with start of month schedule", e);
        }
    }

    /**
     * Scheduled execution to finalise the {@code Team Competition} at the end of a month.
     *
     * @see UserStatsStorer#storeMonthlyResult()
     * @see UserStatsResetter#resetTeamCompetitionStatsForEndOfMonth()
     * @see LarsHardwareUpdater#retrieveHardwareAndPersist()
     */
    @Scheduled(cron = "0 57 23 L * *", zone = "UTC") // 'L' refers to the last day of the month
    public void endOfTeamCompetition() {
        try {
            LOGGER.trace("Method #endOfTeamCompetition() fired");

            if (IS_MONTHLY_RESULT_ENABLED) {
                LOGGER.info("Storing TC stats for new month");
                userStatsStorer.storeMonthlyResult();
            }

            if (IS_MONTHLY_RESET_ENABLED) {
                LOGGER.warn("Resetting TC stats for end of month");
                ParsingStateManager.next(ParsingState.DISABLED);
                SystemStateManager.next(SystemState.RESETTING_STATS);
                userStatsResetter.resetTeamCompetitionStatsForEndOfMonth();
                SystemStateManager.next(SystemState.WRITE_EXECUTED);
            }

            if (IS_LARS_UPDATE_ENABLED) {
                LOGGER.info("Updating hardware from LARS DB");
                larsHardwareUpdater.retrieveHardwareAndPersist();
            }

            userChangeApplier.applyAllForNextMonth();
        } catch (final Exception e) {
            LOGGER.error("Error with end of month schedule", e);
        }
    }
}

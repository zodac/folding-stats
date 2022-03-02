/*
 * MIT License
 *
 * Copyright (c) 2021-2022 zodac.me
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
 */

package me.zodac.folding.bean.tc.scheduled;

import static java.lang.Boolean.parseBoolean;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.TimeZone;
import me.zodac.folding.api.state.ParsingState;
import me.zodac.folding.api.state.SystemState;
import me.zodac.folding.api.tc.change.UserChange;
import me.zodac.folding.api.util.EnvironmentVariableUtils;
import me.zodac.folding.bean.tc.lars.LarsHardwareUpdater;
import me.zodac.folding.bean.tc.user.UserChangeApplier;
import me.zodac.folding.bean.tc.user.UserStatsResetter;
import me.zodac.folding.bean.tc.user.UserStatsStorer;
import me.zodac.folding.state.ParsingStateManager;
import me.zodac.folding.state.SystemStateManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * {@link Scheduled} {@link Component} which schedules the beginning and end of the <code>Team Competition</code>.
 *
 * <p>
 * The first day of the <code>Team Competition</code> is set to the <b>3rd</b> of the month, and on this day it will reset the stats for all users.
 * The reset will occur at <b>00:15</b>. This time cannot be changed, but the reset can be disabled using the environment variable:
 * <ul>
 *     <li>{@code ENABLE_STATS_MONTHLY_RESET}</li>
 * </ul>
 *
 * <p>
 * The last day of the <code>Team Competition</code> is the final day of the month, and on this day the following actions are performed:
 * <ol>
 *     <li>Resets the stats for all users. The reset will occur  at <b>00:15</b>. This time cannot be changed, but the reset can be disabled using
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
 *     <li>Updates the value of all {@link me.zodac.folding.api.tc.Hardware} from the LARS PPD database</li>
 *     <li>Applies any pending {@link UserChange}s for the next month with {@link UserChangeApplier}</li>
 * </ol>
 */
@Component
public class TeamCompetitionScheduler {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final boolean IS_MONTHLY_RESET_ENABLED =
        parseBoolean(EnvironmentVariableUtils.getOrDefault("ENABLE_STATS_MONTHLY_RESET", "false"));
    private static final boolean IS_MONTHLY_RESULT_ENABLED =
        parseBoolean(EnvironmentVariableUtils.getOrDefault("ENABLE_MONTHLY_RESULT_STORAGE", "false"));
    private static final boolean IS_LARS_UPDATE_ENABLED = parseBoolean(EnvironmentVariableUtils.getOrDefault("ENABLE_LARS_HARDWARE_UPDATE", "false"));

    @Autowired
    private LarsHardwareUpdater larsHardwareUpdater;

    @Autowired
    private UserChangeApplier userChangeApplier;

    @Autowired
    private UserStatsResetter userStatsResetter;

    @Autowired
    private UserStatsStorer userStatsStorer;

    /**
     * Scheduled execution to prepare the <code>Team Competition</code> for the new month.
     *
     * @see UserStatsResetter#resetTeamCompetitionStats()
     */
    @Scheduled(cron = "0 15 0 3 * *", zone = "UTC") // TC starts on the 3rd of the month
    public void startOfTeamCompetition() {
        try {
            LOGGER.trace("Method #startOfTeamCompetition() fired");
            LOGGER.warn("Starting TC stats for new month");

            SystemStateManager.next(SystemState.RESETTING_STATS);
            ParsingStateManager.next(ParsingState.DISABLED);
            userStatsResetter.resetTeamCompetitionStats();
            SystemStateManager.next(SystemState.WRITE_EXECUTED);
        } catch (final Exception e) {
            LOGGER.error("Error with start of month schedule", e);
        }
    }

    /**
     * Scheduled execution to finalise the <code>Team Competition</code> at the end of a month.
     *
     * @see UserStatsStorer#storeMonthlyResult()
     * @see UserStatsResetter#resetTeamCompetitionStats()
     * @see LarsHardwareUpdater#retrieveHardwareAndPersist()
     */
    @Scheduled(cron = "0 57 23 28-31 * *", zone = "UTC")
    public void endOfTeamCompetition() {
        try {
            // Because we cannot set up a cron schedule with last day for each month, we use the range '28-31'.
            // We then check if the current day in the month is the last day of the month.
            // If not, we skip the reset.
            if (!isLastDayInMonth()) {
                LOGGER.warn("End of month reset triggered, but not actually end of the month, skipping");
                return;
            }

            LOGGER.trace("Method #endOfTeamCompetition() fired");

            if (IS_MONTHLY_RESULT_ENABLED) {
                LOGGER.info("Storing TC stats for new month");
                userStatsStorer.storeMonthlyResult();
            }

            if (IS_MONTHLY_RESET_ENABLED) {
                LOGGER.warn("Resetting TC stats for end of month");
                ParsingStateManager.next(ParsingState.DISABLED);
                SystemStateManager.next(SystemState.RESETTING_STATS);
                userStatsResetter.resetTeamCompetitionStats();
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

    private static boolean isLastDayInMonth() {
        final TimeZone utcTimeZone = TimeZone.getTimeZone(ZoneId.from(ZoneOffset.UTC));
        final Calendar calendar = Calendar.getInstance(utcTimeZone);
        return calendar.get(Calendar.DATE) == calendar.getActualMaximum(Calendar.DATE);
    }
}

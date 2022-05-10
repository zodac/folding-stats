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

package me.zodac.folding;

import java.util.Collection;
import me.zodac.folding.api.state.SystemState;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.tc.stats.OffsetTcStats;
import me.zodac.folding.api.tc.stats.UserStats;
import me.zodac.folding.bean.StatsRepository;
import me.zodac.folding.bean.api.FoldingRepository;
import me.zodac.folding.state.SystemStateManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Base {@link FoldingStatsApplication}.
 *
 * <p>
 * This should be placed in the highest package possible, as the Spring application will scan all sub-packages for
 * {@link org.springframework.web.bind.annotation.RestController}s, {@link org.springframework.stereotype.Service}s,
 * {@link org.springframework.stereotype.Component}s, etc.
 *
 * <p>
 * The {@code jooq} transitive dependency {@link R2dbcAutoConfiguration} must also be excluded.
 */
@EnableScheduling
@SpringBootApplication(exclude = R2dbcAutoConfiguration.class)
public class FoldingStatsApplication {

    private static final Logger LOGGER = LogManager.getLogger();

    private final FoldingRepository foldingRepository;
    private final StatsRepository statsRepository;

    /**
     * {@link Autowired} constructor.
     *
     * @param foldingRepository the {@link FoldingRepository}
     * @param statsRepository   the {@link StatsRepository}
     */
    public FoldingStatsApplication(final FoldingRepository foldingRepository, final StatsRepository statsRepository) {
        this.foldingRepository = foldingRepository;
        this.statsRepository = statsRepository;
    }

    /**
     * Main entry point to our Spring application.
     *
     * @param args arguments for the {@link FoldingStatsApplication}.
     */
    public static void main(final String[] args) {
        SpringApplication.run(FoldingStatsApplication.class, args);
    }

    /**
     * On system startup, we execute the following actions to initialise the system for requests:
     * <ol>
     *     <li>Initialise the {@link me.zodac.folding.api.tc.Hardware}, {@link User}, {@link me.zodac.folding.api.tc.Team},
     *     {@link OffsetTcStats} and initial {@link me.zodac.folding.api.tc.stats.UserStats} caches</li>
     *     <li>Sets the {@link SystemState} to {@link SystemState#AVAILABLE} when complete</li>
     * </ol>
     *
     * @param ctx the {@link ApplicationContext}
     * @return the {@link CommandLineRunner} with the execution to be run by {@link FoldingStatsApplication}
     */
    @Bean
    public CommandLineRunner initialisation(final ApplicationContext ctx) {
        return args -> {
            initCaches();

            SystemStateManager.next(SystemState.AVAILABLE);
            LOGGER.info("System ready for requests");
        };
    }

    private void initCaches() {
        foldingRepository.getAllHardware();
        foldingRepository.getAllTeams();
        final Collection<User> users = foldingRepository.getAllUsersWithoutPasskeys();

        for (final User user : users) {
            final OffsetTcStats offsetTcStatsForUser = statsRepository.getOffsetStats(user);
            LOGGER.debug("Found offset stats for user {}: {}", user, offsetTcStatsForUser);

            final UserStats initialStatsForUser = statsRepository.getInitialStats(user);
            LOGGER.debug("Found initial stats for user {}: {}", user, initialStatsForUser);
        }

        LOGGER.debug("Initialised stats caches");
    }
}

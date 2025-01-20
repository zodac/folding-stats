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

package net.zodac.folding.configuration;

import java.util.Collection;
import net.zodac.folding.api.tc.Hardware;
import net.zodac.folding.api.tc.Team;
import net.zodac.folding.api.tc.User;
import net.zodac.folding.api.tc.stats.OffsetTcStats;
import net.zodac.folding.api.tc.stats.UserStats;
import net.zodac.folding.bean.StatsRepository;
import net.zodac.folding.bean.api.FoldingRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * {@link Configuration} to initialise the system on startup.
 */
@Configuration
public class InitialisationConfiguration {

    private static final Logger LOGGER = LogManager.getLogger();

    private final FoldingRepository foldingRepository;
    private final StatsRepository statsRepository;

    /**
     * {@link Autowired} constructor.
     *
     * @param foldingRepository the {@link FoldingRepository}
     * @param statsRepository   the {@link StatsRepository}
     */
    @Autowired
    public InitialisationConfiguration(final FoldingRepository foldingRepository, final StatsRepository statsRepository) {
        this.foldingRepository = foldingRepository;
        this.statsRepository = statsRepository;
    }

    /**
     * On system startup, we execute the following actions to initialise the system for requests:
     * <ol>
     *     <li>Initialise the {@link Hardware}, {@link User}, {@link Team},
     *     {@link OffsetTcStats} and initial {@link UserStats} caches</li>
     * </ol>
     *
     * @return the {@link CommandLineRunner} with the execution to be run
     */
    @Bean
    public CommandLineRunner initialisation() {
        return _ -> initCaches();
    }

    private void initCaches() {
        foldingRepository.getAllHardware();
        foldingRepository.getAllTeams();
        final Collection<User> users = foldingRepository.getAllUsersWithoutPasskeys();

        for (final User user : users) {
            LOGGER.info("\nInitialising cache for user '{}' (ID: {})", user.foldingUserName(), user.id());

            final OffsetTcStats offsetTcStatsForUser = statsRepository.getOffsetStats(user);
            LOGGER.info("Found offset stats for user '{}': {}", user.foldingUserName(), offsetTcStatsForUser);

            final UserStats initialStatsForUser = statsRepository.getInitialStats(user);
            LOGGER.info("Found initial stats for user '{}': {}", user.foldingUserName(), initialStatsForUser);
        }

        LOGGER.debug("Initialised stats caches");
    }
}

/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2022 zodac.me
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

package me.zodac.folding;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import java.util.Collection;
import java.util.List;
import me.zodac.folding.api.state.SystemState;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.tc.stats.OffsetTcStats;
import me.zodac.folding.api.tc.stats.UserStats;
import me.zodac.folding.api.util.EnvironmentVariableUtils;
import me.zodac.folding.bean.StatsRepository;
import me.zodac.folding.bean.api.FoldingRepository;
import me.zodac.folding.state.SystemStateManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration;
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
@SecurityScheme(
    name = "basicAuthentication",
    scheme = "basic",
    type = SecuritySchemeType.HTTP
)
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
    @Autowired
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
     * Defines the Swagger documentation for the overall project.
     *
     * @param applicationName    the name of the {@link FoldingStatsApplication}
     * @param applicationVersion the version of the {@link FoldingStatsApplication}
     * @return the {@link GroupedOpenApi} documentation
     */
    @Bean
    public GroupedOpenApi projectInfo(@Value("${application.name}") final String applicationName,
                                      @Value("${application.version}") final String applicationVersion) {
        final String contactName = EnvironmentVariableUtils.get("CONTACT_NAME");
        final String forumLink = EnvironmentVariableUtils.get("FORUM_LINK");
        final String restEndpointUrl = EnvironmentVariableUtils.get("REST_ENDPOINT_URL");

        final Info projectInfo = new Info()
            .title(String.format("The '%s' REST API", applicationName))
            .description(String.format("REST API for the '%s' project", applicationName))
            .contact(new Contact().name(contactName).url(forumLink))
            .license(new License().name("MIT License").url("https://github.com/zodac/folding-stats/blob/master/LICENSE/"))
            .version(applicationVersion);

        return GroupedOpenApi.builder()
            .group("ProjectAPI")
            .addOpenApiCustomiser(openApi -> openApi.info(projectInfo))
            .addOpenApiCustomiser(openApi -> openApi.servers(List.of(new Server().description("Main Website").url(restEndpointUrl))))
            .pathsToExclude("/health/*")
            .build();
    }

    /**
     * On system startup, we execute the following actions to initialise the system for requests:
     * <ol>
     *     <li>Initialise the {@link me.zodac.folding.api.tc.Hardware}, {@link User}, {@link me.zodac.folding.api.tc.Team},
     *     {@link OffsetTcStats} and initial {@link me.zodac.folding.api.tc.stats.UserStats} caches</li>
     *     <li>Sets the {@link SystemState} to {@link SystemState#AVAILABLE} when complete</li>
     * </ol>
     *
     * @return the {@link CommandLineRunner} with the execution to be run by {@link FoldingStatsApplication}
     */
    @Bean
    public CommandLineRunner initialisation() {
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
            LOGGER.info("\nInitialising cache for user '{}' (ID: {})", user.foldingUserName(), user.id());

            final OffsetTcStats offsetTcStatsForUser = statsRepository.getOffsetStats(user);
            LOGGER.info("Found offset stats for user '{}': {}", user.foldingUserName(), offsetTcStatsForUser);

            final UserStats initialStatsForUser = statsRepository.getInitialStats(user);
            LOGGER.info("Found initial stats for user '{}': {}", user.foldingUserName(), initialStatsForUser);
        }

        LOGGER.debug("Initialised stats caches");
    }
}

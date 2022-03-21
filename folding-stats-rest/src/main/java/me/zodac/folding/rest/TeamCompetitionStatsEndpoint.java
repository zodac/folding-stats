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

package me.zodac.folding.rest;

import static me.zodac.folding.rest.response.Responses.notFound;
import static me.zodac.folding.rest.response.Responses.ok;
import static me.zodac.folding.rest.util.RequestParameterExtractor.extractParameters;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import me.zodac.folding.api.state.SystemState;
import me.zodac.folding.api.tc.Category;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.tc.stats.OffsetTcStats;
import me.zodac.folding.bean.StatsRepository;
import me.zodac.folding.bean.api.FoldingRepository;
import me.zodac.folding.bean.tc.LeaderboardStatsGenerator;
import me.zodac.folding.bean.tc.user.UserStatsParser;
import me.zodac.folding.bean.tc.user.UserStatsResetter;
import me.zodac.folding.rest.api.tc.AllTeamsSummary;
import me.zodac.folding.rest.api.tc.CompetitionSummary;
import me.zodac.folding.rest.api.tc.UserSummary;
import me.zodac.folding.rest.api.tc.leaderboard.TeamLeaderboardEntry;
import me.zodac.folding.rest.api.tc.leaderboard.UserCategoryLeaderboardEntry;
import me.zodac.folding.rest.util.ReadRequired;
import me.zodac.folding.rest.util.WriteRequired;
import me.zodac.folding.state.SystemStateManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST endpoint for {@code Team Competition} stats.
 */
@RestController
@RequestMapping("/stats")
public class TeamCompetitionStatsEndpoint {

    private static final Logger AUDIT_LOGGER = LogManager.getLogger("audit");

    private final FoldingRepository foldingRepository;
    private final LeaderboardStatsGenerator leaderboardStatsGenerator;
    private final StatsRepository statsRepository;
    private final UserStatsParser userStatsParser;
    private final UserStatsResetter userStatsResetter;

    // Prometheus counters
    private final Counter visitCounter;
    private final Counter userStatsOffsets;

    /**
     * {@link Autowired} constructor to inject {@link MeterRegistry} and configure Prometheus {@link Counter}s.
     *
     * @param foldingRepository         the {@link FoldingRepository}
     * @param leaderboardStatsGenerator the {@link LeaderboardStatsGenerator}
     * @param registry                  the Prometheus {@link MeterRegistry}
     * @param statsRepository           the {@link StatsRepository}
     * @param userStatsParser           the {@link UserStatsParser}
     * @param userStatsResetter         the {@link UserStatsResetter}
     */
    @Autowired
    public TeamCompetitionStatsEndpoint(final FoldingRepository foldingRepository,
                                        final LeaderboardStatsGenerator leaderboardStatsGenerator,
                                        final MeterRegistry registry,
                                        final StatsRepository statsRepository,
                                        final UserStatsParser userStatsParser,
                                        final UserStatsResetter userStatsResetter) {
        this.foldingRepository = foldingRepository;
        this.leaderboardStatsGenerator = leaderboardStatsGenerator;
        this.statsRepository = statsRepository;
        this.userStatsParser = userStatsParser;
        this.userStatsResetter = userStatsResetter;

        visitCounter = Counter.builder("visit_counter")
            .description("Number of visits to the site")
            .register(registry);

        userStatsOffsets = Counter.builder("user_stats_offset_counter")
            .description("Number of times a user's stats have been manually offset")
            .register(registry);
    }

    /**
     * {@link GetMapping} request to retrieve the {@code Team Competition} simple {@link CompetitionSummary}.
     *
     * @return {@link me.zodac.folding.rest.response.Responses#ok()} containing the {@link CompetitionSummary}
     */
    @ReadRequired
    @PermitAll
    @GetMapping(path = "/overall", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getCompetitionStats() {
        AUDIT_LOGGER.debug("GET request received to show TC overall stats");
        visitCounter.increment();
        final AllTeamsSummary allTeamsSummary = statsRepository.getAllTeamsSummary();
        return ok(allTeamsSummary.getCompetitionSummary());
    }

    /**
     * {@link GetMapping} request to retrieve the {@code Team Competition} overall {@link AllTeamsSummary}.
     *
     * @return {@link me.zodac.folding.rest.response.Responses#ok()} containing the {@link AllTeamsSummary}
     */
    @ReadRequired
    @PermitAll
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getTeamCompetitionStats() {
        AUDIT_LOGGER.debug("GET request received to show TC stats");
        final AllTeamsSummary allTeamsSummary = statsRepository.getAllTeamsSummary();
        return ok(allTeamsSummary);
    }

    /**
     * {@link GetMapping} request to retrieve the {@code Team Competition} {@link UserSummary} for the given {@link User}.
     *
     * @param userId  the ID of the {@link User} whose {@link UserSummary} is to be retrieved
     * @param request the {@link HttpServletRequest}
     * @return {@link me.zodac.folding.rest.response.Responses#ok()} containing the {@link UserSummary}
     */
    @ReadRequired
    @PermitAll
    @GetMapping(path = "/users/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getTeamCompetitionStatsForUser(@PathVariable("userId") final int userId, final HttpServletRequest request) {
        AUDIT_LOGGER.debug("GET request received to show TC stats for user received at '{}'", request::getRequestURI);

        final User user = foldingRepository.getUserWithoutPasskey(userId);

        final AllTeamsSummary allTeamsSummary = statsRepository.getAllTeamsSummary();
        final Collection<UserSummary> userSummaries = allTeamsSummary.getTeams()
            .stream()
            .flatMap(teamResult -> teamResult.getActiveUsers().stream())
            .toList();

        for (final UserSummary userSummary : userSummaries) {
            if (userSummary.getUser().id() == user.id()) {
                return ok(userSummary);
            }
        }

        return notFound();
    }

    /**
     * {@link PatchMapping} request to update an existing {@link UserSummary} with an {@link OffsetTcStats}.
     *
     * @param userId        the ID of the {@link UserSummary} to be updated
     * @param offsetTcStats the {@link OffsetTcStats} to be applied
     * @param request       the {@link HttpServletRequest}
     * @return {@link me.zodac.folding.rest.response.Responses#ok()}
     */
    @WriteRequired
    @RolesAllowed("admin")
    @PatchMapping(path = "/users/{userId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateUserWithOffset(@PathVariable("userId") final int userId,
                                                  @RequestBody final OffsetTcStats offsetTcStats,
                                                  final HttpServletRequest request) {
        AUDIT_LOGGER.info("PATCH request to update offset for user received at '{}' with request {}", request::getRequestURI, () -> offsetTcStats);

        final User user = foldingRepository.getUserWithPasskey(userId);
        final Hardware hardware = user.hardware();
        final OffsetTcStats offsetTcStatsToPersist = OffsetTcStats.updateWithHardwareMultiplier(offsetTcStats, hardware.multiplier());
        final OffsetTcStats createdOffsetStats = statsRepository.createOrUpdateOffsetStats(user, offsetTcStatsToPersist);

        SystemStateManager.next(SystemState.UPDATING_STATS);
        userStatsParser.parseTcStatsForUsersAndWait(Collections.singletonList(user));
        SystemStateManager.next(SystemState.WRITE_EXECUTED);
        AUDIT_LOGGER.info("Updated user with ID {} with points offset: {}", userId, createdOffsetStats);
        userStatsOffsets.increment();
        return ok();
    }

    /**
     * {@link GetMapping} request to retrieve the {@code Team Competition} {@link TeamLeaderboardEntry}s.
     *
     * @return {@link me.zodac.folding.rest.response.Responses#ok()} containing the {@link TeamLeaderboardEntry}s
     */
    @ReadRequired
    @PermitAll
    @GetMapping(path = "/leaderboard", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getTeamLeaderboard() {
        AUDIT_LOGGER.debug("GET request received to show TC leaderboard");
        final List<TeamLeaderboardEntry> teamSummaries = leaderboardStatsGenerator.generateTeamLeaderboards();
        return ok(teamSummaries);
    }

    /**
     * {@link GetMapping} request to retrieve the {@code Team Competition} {@link UserCategoryLeaderboardEntry}s by {@link Category}.
     *
     * @return {@link me.zodac.folding.rest.response.Responses#ok()} containing the {@link UserCategoryLeaderboardEntry}s
     */
    @ReadRequired
    @PermitAll
    @GetMapping(path = "/category", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getCategoryLeaderboard() {
        AUDIT_LOGGER.debug("GET request received to show TC category leaderboard");
        final Map<Category, List<UserCategoryLeaderboardEntry>> categoryLeaderboard = leaderboardStatsGenerator.generateUserCategoryLeaderboards();
        return ok(categoryLeaderboard);
    }

    /**
     * {@link PostMapping} request to manually update the {@code Team Competition} stats.
     *
     * @param async   whether the execution should be performed asynchronously or synchronously
     * @param request the {@link HttpServletRequest}
     * @return {@link me.zodac.folding.rest.response.Responses#ok()}
     */
    @WriteRequired
    @RolesAllowed("admin")
    @PostMapping(path = "/manual/update")
    public ResponseEntity<?> updateStats(@RequestParam(value = "async", required = false, defaultValue = "false") final boolean async,
                                         final HttpServletRequest request) {
        AUDIT_LOGGER.info("GET request received to manually update TC stats at '{}?{}", request::getRequestURI, () -> extractParameters(request));
        final Collection<User> users = foldingRepository.getAllUsersWithPasskeys();

        if (async) {
            userStatsParser.parseTcStatsForUsers(users);
        } else {
            userStatsParser.parseTcStatsForUsersAndWait(users);
        }
        return ok();
    }

    /**
     * {@link PostMapping} request to manually reset the {@code Team Competition} stats.
     *
     * @return {@link me.zodac.folding.rest.response.Responses#ok()}
     */
    @WriteRequired
    @RolesAllowed("admin")
    @PostMapping(path = "/manual/reset")
    public ResponseEntity<?> resetStats() {
        AUDIT_LOGGER.info("GET request received to manually reset TC stats");

        SystemStateManager.next(SystemState.RESETTING_STATS);
        userStatsResetter.resetTeamCompetitionStats();
        SystemStateManager.next(SystemState.WRITE_EXECUTED);
        return ok();
    }
}
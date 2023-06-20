/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2023 zodac.me
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

package me.zodac.folding.rest.controller;

import static me.zodac.folding.rest.response.Responses.ok;
import static me.zodac.folding.rest.util.RequestParameterExtractor.extractParameters;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import me.zodac.folding.api.state.SystemState;
import me.zodac.folding.api.tc.Category;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.tc.stats.OffsetTcStats;
import me.zodac.folding.api.util.LoggerName;
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
import me.zodac.folding.rest.api.tc.request.OffsetTcStatsRequest;
import me.zodac.folding.rest.controller.api.TeamCompetitionStatsEndpoint;
import me.zodac.folding.rest.exception.NotFoundException;
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
import org.springframework.web.bind.annotation.RestController;

/**
 * Implementation of {@link TeamCompetitionStatsEndpoint} REST endpoints.
 */
@RestController
@RequestMapping("/stats")
public class TeamCompetitionStatsController implements TeamCompetitionStatsEndpoint {

    private static final Logger AUDIT_LOGGER = LogManager.getLogger(LoggerName.AUDIT.get());

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
    public TeamCompetitionStatsController(final FoldingRepository foldingRepository,
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

    @Override
    @ReadRequired
    @PermitAll
    @GetMapping(path = "/summary", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CompetitionSummary> getCompetitionStats() {
        AUDIT_LOGGER.debug("GET request received to show TC summary stats");
        visitCounter.increment();
        final AllTeamsSummary allTeamsSummary = statsRepository.getAllTeamsSummary();
        return ok(allTeamsSummary.competitionSummary());
    }

    @Override
    @ReadRequired
    @PermitAll
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AllTeamsSummary> getTeamCompetitionStats() {
        AUDIT_LOGGER.debug("GET request received to show TC stats");
        final AllTeamsSummary allTeamsSummary = statsRepository.getAllTeamsSummary();
        return ok(allTeamsSummary);
    }

    @Override
    @ReadRequired
    @PermitAll
    @GetMapping(path = "/users/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserSummary> getTeamCompetitionStatsForUser(@PathVariable("userId") final int userId, final HttpServletRequest request) {
        AUDIT_LOGGER.debug("GET request received to show TC stats for user received at '{}'", request::getRequestURI);

        final User user = foldingRepository.getUserWithoutPasskey(userId);

        final AllTeamsSummary allTeamsSummary = statsRepository.getAllTeamsSummary();
        final Collection<UserSummary> userSummaries = allTeamsSummary.teams()
            .stream()
            .flatMap(teamResult -> teamResult.activeUsers().stream())
            .toList();

        for (final UserSummary userSummary : userSummaries) {
            if (userSummary.user().id() == user.id()) {
                return ok(userSummary);
            }
        }

        throw new NotFoundException(User.class, user.id());
    }

    @Override
    @WriteRequired
    @RolesAllowed("admin")
    @PatchMapping(path = "/users/{userId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> updateUserWithOffset(@PathVariable("userId") final int userId,
                                                     @RequestBody final OffsetTcStatsRequest offsetTcStatsRequest,
                                                     final HttpServletRequest request) {
        AUDIT_LOGGER.info("PATCH request to offset user stats received at '{}' with request {}", request::getRequestURI, () -> offsetTcStatsRequest);

        final User user = foldingRepository.getUserWithPasskey(userId);
        final Hardware hardware = user.hardware();
        final OffsetTcStats offsetTcStats = OffsetTcStats.create(offsetTcStatsRequest);

        // TODO: What happens if both points and multipliedPoints are empty?
        final OffsetTcStats offsetTcStatsToPersist = OffsetTcStats.updateWithHardwareMultiplier(offsetTcStats, hardware.multiplier());

        final OffsetTcStats createdOffsetStats = statsRepository.createOrUpdateOffsetStats(user, offsetTcStatsToPersist);

        SystemStateManager.next(SystemState.UPDATING_STATS);
        userStatsParser.parseTcStatsForUser(user);
        SystemStateManager.next(SystemState.WRITE_EXECUTED);
        AUDIT_LOGGER.info("Updated user with ID {} with points offset: {}", userId, createdOffsetStats);
        userStatsOffsets.increment();
        return ok();
    }

    @Override
    @ReadRequired
    @PermitAll
    @GetMapping(path = "/leaderboard", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Collection<TeamLeaderboardEntry>> getTeamLeaderboard() {
        AUDIT_LOGGER.debug("GET request received to show TC leaderboard");
        final Collection<TeamLeaderboardEntry> teamSummaries = leaderboardStatsGenerator.generateTeamLeaderboards();
        return ok(teamSummaries);
    }

    @Override
    @ReadRequired
    @PermitAll
    @GetMapping(path = "/category", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<Category, List<UserCategoryLeaderboardEntry>>> getCategoryLeaderboard() {
        AUDIT_LOGGER.debug("GET request received to show TC category leaderboard");
        final Map<Category, List<UserCategoryLeaderboardEntry>> categoryLeaderboard = leaderboardStatsGenerator.generateUserCategoryLeaderboards();
        return ok(categoryLeaderboard);
    }

    @Override
    @WriteRequired
    @RolesAllowed("admin")
    @PostMapping(path = "/manual/update")
    public ResponseEntity<Void> updateStats(final HttpServletRequest request) {
        AUDIT_LOGGER.info("GET request received to manually update TC stats at '{}?{}", request::getRequestURI, () -> extractParameters(request));
        final Collection<User> users = foldingRepository.getAllUsersWithPasskeys();
        userStatsParser.parseTcStatsForUsers(users);
        return ok();
    }

    @Override
    @WriteRequired
    @RolesAllowed("admin")
    @PostMapping(path = "/manual/reset")
    public ResponseEntity<Void> resetStats() {
        AUDIT_LOGGER.info("GET request received to manually reset TC stats");

        SystemStateManager.next(SystemState.RESETTING_STATS);
        userStatsResetter.resetTeamCompetitionStats(true);
        SystemStateManager.next(SystemState.WRITE_EXECUTED);
        return ok();
    }
}

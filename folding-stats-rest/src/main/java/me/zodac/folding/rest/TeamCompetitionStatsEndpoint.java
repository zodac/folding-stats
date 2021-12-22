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
 */

package me.zodac.folding.rest;

import static java.util.stream.Collectors.toList;
import static me.zodac.folding.rest.response.Responses.notFound;
import static me.zodac.folding.rest.response.Responses.nullRequest;
import static me.zodac.folding.rest.response.Responses.ok;
import static me.zodac.folding.rest.response.Responses.serverError;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import me.zodac.folding.api.state.ReadRequired;
import me.zodac.folding.api.state.SystemState;
import me.zodac.folding.api.state.WriteRequired;
import me.zodac.folding.api.tc.Category;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.tc.stats.OffsetTcStats;
import me.zodac.folding.api.util.ProcessingType;
import me.zodac.folding.bean.FoldingRepository;
import me.zodac.folding.bean.StatsRepository;
import me.zodac.folding.bean.tc.LeaderboardStatsGenerator;
import me.zodac.folding.bean.tc.user.UserStatsParser;
import me.zodac.folding.bean.tc.user.UserStatsResetter;
import me.zodac.folding.rest.api.tc.CompetitionSummary;
import me.zodac.folding.rest.api.tc.UserSummary;
import me.zodac.folding.rest.api.tc.leaderboard.TeamLeaderboardEntry;
import me.zodac.folding.rest.api.tc.leaderboard.UserCategoryLeaderboardEntry;
import me.zodac.folding.rest.util.IdResult;
import me.zodac.folding.rest.util.IntegerParser;
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
 * REST endpoint for <code>Team Competition</code> stats.
 */
@RestController
@RequestMapping("/stats")
public class TeamCompetitionStatsEndpoint {

    private static final Logger LOGGER = LogManager.getLogger();

    @Autowired
    private FoldingRepository foldingRepository;

    @Autowired
    private LeaderboardStatsGenerator leaderboardStatsGenerator;

    @Autowired
    private StatsRepository statsRepository;

    @Autowired
    private UserStatsParser userStatsParser;

    @Autowired
    private UserStatsResetter userStatsResetter;

    /**
     * {@link GetMapping} request to retrieve the <code>Team Competition</code> overall {@link CompetitionSummary}.
     *
     * @return {@link me.zodac.folding.rest.response.Responses#ok()} containing the {@link CompetitionSummary}
     */
    @ReadRequired
    @PermitAll
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getTeamCompetitionStats() {
        LOGGER.debug("GET request received to show TC stats");

        try {
            final CompetitionSummary competitionSummary = statsRepository.getCompetitionSummary();
            return ok(competitionSummary);
        } catch (final Exception e) {
            LOGGER.error("Unexpected error retrieving full TC stats", e);
            return serverError();
        }
    }

    /**
     * {@link GetMapping} request to retrieve the <code>Team Competition</code> {@link UserSummary} for the given {@link User}.
     *
     * @param userId  the ID of the {@link User} whose {@link UserSummary} is to be retrieved
     * @param request the {@link HttpServletRequest}
     * @return {@link me.zodac.folding.rest.response.Responses#ok()} containing the {@link UserSummary}
     */
    @ReadRequired
    @PermitAll
    @GetMapping(path = "/users/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getTeamCompetitionStatsForUser(@PathVariable("userId") final String userId, final HttpServletRequest request) {
        LOGGER.debug("GET request received to show TC stats for user received at '{}'", request::getRequestURI);

        try {
            final IdResult idResult = IntegerParser.parsePositive(userId);
            if (idResult.isFailure()) {
                return idResult.getFailureResponse();
            }
            final int parsedId = idResult.getId();

            final Optional<User> optionalUser = foldingRepository.getUserWithoutPasskey(parsedId);
            if (optionalUser.isEmpty()) {
                LOGGER.error("No user found with ID: {}", parsedId);
                return notFound();
            }
            final User user = optionalUser.get();

            final CompetitionSummary competitionSummary = statsRepository.getCompetitionSummary();
            final Collection<UserSummary> userSummaries = competitionSummary.getTeams()
                .stream()
                .flatMap(teamResult -> teamResult.getActiveUsers().stream())
                .collect(toList());

            for (final UserSummary userSummary : userSummaries) {
                if (userSummary.getUser().getId() == user.getId()) {
                    return ok(userSummary);
                }
            }

            return notFound();
        } catch (final Exception e) {
            LOGGER.error("Unexpected error retrieving TC stats for users", e);
            return serverError();
        }
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
    public ResponseEntity<?> updateUserWithOffset(@PathVariable("userId") final String userId,
                                                  @RequestBody final OffsetTcStats offsetTcStats,
                                                  final HttpServletRequest request) {
        LOGGER.debug("PATCH request to update offset for user received at '{}': {}", request::getRequestURI, () -> offsetTcStats);

        if (offsetTcStats == null || offsetTcStats.isEmpty()) {
            LOGGER.error("Payload is null");
            return nullRequest();
        }

        try {
            final IdResult idResult = IntegerParser.parsePositive(userId);
            if (idResult.isFailure()) {
                return idResult.getFailureResponse();
            }
            final int parsedId = idResult.getId();

            final Optional<User> optionalUser = foldingRepository.getUserWithPasskey(parsedId);
            if (optionalUser.isEmpty()) {
                LOGGER.error("No user found with ID: {}", parsedId);
                return notFound();
            }

            final User user = optionalUser.get();
            final Hardware hardware = user.getHardware();
            final OffsetTcStats offsetTcStatsToPersist = OffsetTcStats.updateWithHardwareMultiplier(offsetTcStats, hardware.getMultiplier());
            final OffsetTcStats createdOffsetStats = statsRepository.createOrUpdateOffsetStats(user, offsetTcStatsToPersist);

            SystemStateManager.next(SystemState.UPDATING_STATS);
            userStatsParser.parseTcStatsForUsersAndWait(Collections.singletonList(user));
            SystemStateManager.next(SystemState.WRITE_EXECUTED);
            LOGGER.info("Updated user with ID {} with points offset: {}", userId, createdOffsetStats);
            return ok();
        } catch (final Exception e) {
            LOGGER.error("Unexpected error updating user with ID: {}", userId, e);
            return serverError();
        }
    }

    /**
     * {@link GetMapping} request to retrieve the <code>Team Competition</code> {@link TeamLeaderboardEntry}s.
     *
     * @return {@link me.zodac.folding.rest.response.Responses#ok()} containing the {@link TeamLeaderboardEntry}s
     */
    @ReadRequired
    @PermitAll
    @GetMapping(path = "/leaderboard", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getTeamLeaderboard() {
        LOGGER.debug("GET request received to show TC leaderboard");

        try {
            final List<TeamLeaderboardEntry> teamSummaries = leaderboardStatsGenerator.generateTeamLeaderboards();
            return ok(teamSummaries);
        } catch (final Exception e) {
            LOGGER.error("Unexpected error retrieving TC leaderboard", e);
            return serverError();
        }
    }

    /**
     * {@link GetMapping} request to retrieve the <code>Team Competition</code> {@link UserCategoryLeaderboardEntry}s by {@link Category}.
     *
     * @return {@link me.zodac.folding.rest.response.Responses#ok()} containing the {@link UserCategoryLeaderboardEntry}s
     */
    @ReadRequired
    @PermitAll
    @GetMapping(path = "/category", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getCategoryLeaderboard() {
        LOGGER.debug("GET request received to show TC category leaderboard");

        try {
            final Map<Category, List<UserCategoryLeaderboardEntry>> categoryLeaderboard =
                leaderboardStatsGenerator.generateUserCategoryLeaderboards();
            return ok(categoryLeaderboard);
        } catch (final Exception e) {
            LOGGER.error("Unexpected error retrieving TC stats", e);
            return serverError();
        }
    }

    /**
     * {@link PostMapping} request to manually update the <code>Team Competition</code> stats.
     *
     * @param async whether the execution should be performed asynchronously or synchronously
     * @return {@link me.zodac.folding.rest.response.Responses#ok()}
     */
    @WriteRequired
    @RolesAllowed("admin")
    @PostMapping(path = "/manual/update")
    public ResponseEntity<?> updateStats(@RequestParam(value = "async", required = false) final boolean async) {
        LOGGER.info("GET request received to manually update TC stats");

        try {
            final Collection<User> users = foldingRepository.getAllUsersWithPasskeys();
            final ProcessingType processingType = async ? ProcessingType.ASYNCHRONOUS : ProcessingType.SYNCHRONOUS;
            if (processingType == ProcessingType.SYNCHRONOUS) {
                userStatsParser.parseTcStatsForUsersAndWait(users);
            } else {
                userStatsParser.parseTcStatsForUsers(users);
            }
            return ok();
        } catch (final Exception e) {
            LOGGER.error("Unexpected error manually parsing TC stats", e);
            return serverError();
        }
    }

    /**
     * {@link PostMapping} request to manually reset the <code>Team Competition</code> stats.
     *
     * @return {@link me.zodac.folding.rest.response.Responses#ok()}
     */
    @WriteRequired
    @RolesAllowed("admin")
    @PostMapping(path = "/manual/reset")
    public ResponseEntity<?> resetStats() {
        LOGGER.info("GET request received to manually reset TC stats");

        try {
            SystemStateManager.next(SystemState.RESETTING_STATS);
            userStatsResetter.resetTeamCompetitionStats();
            SystemStateManager.next(SystemState.WRITE_EXECUTED);
            return ok();
        } catch (final Exception e) {
            LOGGER.error("Unexpected error manually resetting TC stats", e);
            return serverError();
        }
    }
}
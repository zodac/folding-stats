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

package net.zodac.folding.rest.controller.api;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import net.zodac.folding.api.tc.Category;
import net.zodac.folding.api.tc.User;
import net.zodac.folding.rest.api.tc.AllTeamsSummary;
import net.zodac.folding.rest.api.tc.CompetitionSummary;
import net.zodac.folding.rest.api.tc.UserSummary;
import net.zodac.folding.rest.api.tc.leaderboard.TeamLeaderboardEntry;
import net.zodac.folding.rest.api.tc.leaderboard.UserCategoryLeaderboardEntry;
import net.zodac.folding.rest.api.tc.request.OffsetTcStatsRequest;
import net.zodac.folding.rest.response.Responses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * REST endpoint for {@code Team Competition} stats.
 */
public interface TeamCompetitionStatsEndpoint {

    /**
     * {@link GetMapping} request to retrieve the {@code Team Competition} simple {@link CompetitionSummary}.
     *
     * @return {@link Responses#ok(Object)} containing the {@link CompetitionSummary}
     */
    ResponseEntity<CompetitionSummary> getCompetitionStats();

    /**
     * {@link GetMapping} request to retrieve the {@code Team Competition} {@link AllTeamsSummary}.
     *
     * @return {@link Responses#ok(Object)} containing the {@link AllTeamsSummary}
     */
    ResponseEntity<AllTeamsSummary> getTeamCompetitionStats();

    /**
     * {@link GetMapping} request to retrieve the {@code Team Competition} {@link UserSummary} for the given {@link User}.
     *
     * @param userId  the ID of the {@link User} whose {@link UserSummary} is to be retrieved
     * @param request the {@link HttpServletRequest}
     * @return {@link Responses#ok(Object)} containing the {@link UserSummary}
     */
    ResponseEntity<UserSummary> getTeamCompetitionStatsForUser(@PathVariable("userId") int userId, HttpServletRequest request);

    /**
     * {@link PatchMapping} request to update an existing {@link UserSummary} with an {@link OffsetTcStatsRequest}.
     *
     * @param userId               the ID of the {@link UserSummary} to be updated
     * @param offsetTcStatsRequest the {@link OffsetTcStatsRequest} to be applied
     * @param request              the {@link HttpServletRequest}
     * @return {@link Responses#ok()}
     */
    ResponseEntity<Void> updateUserWithOffset(@PathVariable("userId") int userId,
                                              @RequestBody OffsetTcStatsRequest offsetTcStatsRequest,
                                              HttpServletRequest request);

    /**
     * {@link GetMapping} request to retrieve the {@code Team Competition} {@link TeamLeaderboardEntry}s.
     *
     * @return {@link Responses#ok(Object)} containing the {@link TeamLeaderboardEntry}s
     */
    ResponseEntity<Collection<TeamLeaderboardEntry>> getTeamLeaderboard();

    /**
     * {@link GetMapping} request to retrieve the {@code Team Competition} {@link UserCategoryLeaderboardEntry}s by {@link Category}.
     *
     * @return {@link Responses#ok(Object)} containing the {@link UserCategoryLeaderboardEntry}s
     */
    ResponseEntity<Map<Category, List<UserCategoryLeaderboardEntry>>> getCategoryLeaderboard();

    /**
     * {@link PostMapping} request to manually update the {@code Team Competition} stats.
     *
     * @param request the {@link HttpServletRequest}
     * @return {@link Responses#ok()}
     */
    ResponseEntity<Void> updateStats(HttpServletRequest request);

    /**
     * {@link PostMapping} request to manually reset the {@code Team Competition} stats.
     *
     * @return {@link Responses#ok()}
     */
    ResponseEntity<Void> resetStats();
}

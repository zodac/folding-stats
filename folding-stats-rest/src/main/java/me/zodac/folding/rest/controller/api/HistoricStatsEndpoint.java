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

package me.zodac.folding.rest.controller.api;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Month;
import java.time.Year;
import java.util.Collection;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.rest.api.tc.historic.HistoricStats;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * REST endpoints for {@code Team Competition} {@link HistoricStats}.
 */
public interface HistoricStatsEndpoint {

    /**
     * {@link GetMapping} request to retrieve a {@link User}'s hourly {@link HistoricStats} for a single {@code day}.
     *
     * @param userId  the ID of the {@link User} whose {@link HistoricStats} are to be retrieved
     * @param year    the {@link Year} of the {@link HistoricStats}
     * @param month   the {@link Month} of the {@link HistoricStats}
     * @param day     the {@code day} of the {@link HistoricStats}
     * @param request the {@link HttpServletRequest}
     * @return {@link me.zodac.folding.rest.response.Responses#cachedOk(Collection, long)} containing the {@link User}'s hourly {@link HistoricStats}
     */
    ResponseEntity<Collection<HistoricStats>> getUserHistoricStatsHourly(@PathVariable("userId") int userId,
                                                                         @PathVariable("year") String year,
                                                                         @PathVariable("month") String month,
                                                                         @PathVariable("day") String day,
                                                                         HttpServletRequest request);

    /**
     * {@link GetMapping} request to retrieve a {@link User}'s daily {@link HistoricStats} for a single {@link Month}.
     *
     * @param userId  the ID of the {@link User} whose {@link HistoricStats} are to be retrieved
     * @param year    the {@link Year} of the {@link HistoricStats}
     * @param month   the {@link Month} of the {@link HistoricStats}
     * @param request the {@link HttpServletRequest}
     * @return {@link me.zodac.folding.rest.response.Responses#cachedOk(Collection, long)} containing the {@link User}'s daily {@link HistoricStats}
     */
    ResponseEntity<Collection<HistoricStats>> getUserHistoricStatsDaily(@PathVariable("userId") int userId,
                                                                        @PathVariable("year") String year,
                                                                        @PathVariable("month") String month,
                                                                        HttpServletRequest request);

    /**
     * {@link GetMapping} request to retrieve a {@link User}'s monthly {@link HistoricStats} for a single {@link Year}.
     *
     * @param userId  the ID of the {@link User} whose {@link HistoricStats} are to be retrieved
     * @param year    the {@link Year} of the {@link HistoricStats}
     * @param request the {@link HttpServletRequest}
     * @return {@link me.zodac.folding.rest.response.Responses#cachedOk(Collection, long)} containing the {@link User}'s monthly {@link HistoricStats}
     */
    ResponseEntity<Collection<HistoricStats>> getUserHistoricStatsMonthly(@PathVariable("userId") int userId,
                                                                          @PathVariable("year") String year,
                                                                          HttpServletRequest request);

    /**
     * {@link GetMapping} request to retrieve a {@link Team}'s hourly {@link HistoricStats} for a single {@code day}.
     *
     * @param teamId  the ID of the {@link Team} whose {@link HistoricStats} are to be retrieved
     * @param year    the {@link Year} of the {@link HistoricStats}
     * @param month   the {@link Month} of the {@link HistoricStats}
     * @param day     the {@code day} of the {@link HistoricStats}
     * @param request the {@link HttpServletRequest}
     * @return {@link me.zodac.folding.rest.response.Responses#cachedOk(Collection, long)} containing the {@link Team}'s hourly {@link HistoricStats}
     */
    ResponseEntity<Collection<HistoricStats>> getTeamHistoricStatsHourly(@PathVariable("teamId") int teamId,
                                                                         @PathVariable("year") String year,
                                                                         @PathVariable("month") String month,
                                                                         @PathVariable("day") String day,
                                                                         HttpServletRequest request);

    /**
     * {@link GetMapping} request to retrieve a {@link Team}'s daily {@link HistoricStats} for a single {@link Month}.
     *
     * @param teamId  the ID of the {@link Team} whose {@link HistoricStats} are to be retrieved
     * @param year    the {@link Year} of the {@link HistoricStats}
     * @param month   the {@link Month} of the {@link HistoricStats}
     * @param request the {@link HttpServletRequest}
     * @return {@link me.zodac.folding.rest.response.Responses#cachedOk(Collection, long)} containing the {@link Team}'s daily {@link HistoricStats}
     */
    ResponseEntity<Collection<HistoricStats>> getTeamHistoricStatsDaily(@PathVariable("teamId") int teamId,
                                                                        @PathVariable("year") String year,
                                                                        @PathVariable("month") String month,
                                                                        HttpServletRequest request);

    /**
     * {@link GetMapping} request to retrieve a {@link Team}'s monthly {@link HistoricStats} for a single {@link Year}.
     *
     * @param teamId  the ID of the {@link Team} whose {@link HistoricStats} are to be retrieved
     * @param year    the {@link Year} of the {@link HistoricStats}
     * @param request the {@link HttpServletRequest}
     * @return {@link me.zodac.folding.rest.response.Responses#cachedOk(Collection, long)} containing the {@link Team}'s monthly {@link HistoricStats}
     */
    ResponseEntity<Collection<HistoricStats>> getTeamHistoricStatsMonthly(@PathVariable("teamId") int teamId,
                                                                          @PathVariable("year") String year,
                                                                          HttpServletRequest request);
}
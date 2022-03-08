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

import static me.zodac.folding.api.util.DateTimeUtils.untilNextMonthUtc;
import static me.zodac.folding.rest.response.Responses.badRequest;
import static me.zodac.folding.rest.response.Responses.cachedOk;
import static me.zodac.folding.rest.response.Responses.created;
import static me.zodac.folding.rest.response.Responses.notFound;
import static me.zodac.folding.rest.response.Responses.ok;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Optional;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import me.zodac.folding.api.state.ReadRequired;
import me.zodac.folding.api.state.SystemState;
import me.zodac.folding.api.state.WriteRequired;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.util.StringUtils;
import me.zodac.folding.bean.FoldingRepository;
import me.zodac.folding.bean.tc.validation.TeamValidator;
import me.zodac.folding.rest.api.tc.request.TeamRequest;
import me.zodac.folding.state.SystemStateManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST endpoints for <code>Team Competition</code> {@link Team}s.
 */
@RestController
@RequestMapping("/teams")
public class TeamEndpoint {

    private static final Logger LOGGER = LogManager.getLogger();

    @Autowired
    private FoldingRepository foldingRepository;

    // Prometheus counters
    private final Counter teamCreates;
    private final Counter teamUpdates;
    private final Counter teamDeletes;

    /**
     * Constructor to inject {@link MeterRegistry} and configure Prometheus {@link Counter}s.
     *
     * @param registry the Prometheus {@link MeterRegistry}
     */
    public TeamEndpoint(final MeterRegistry registry) {
        teamCreates = Counter.builder("team_create_counter")
            .description("Number of Team creations through the REST endpoint")
            .register(registry);
        teamUpdates = Counter.builder("team_update_counter")
            .description("Number of Team updates through the REST endpoint")
            .register(registry);
        teamDeletes = Counter.builder("team_delete_counter")
            .description("Number of Team deletions through the REST endpoint")
            .register(registry);
    }

    /**
     * {@link PostMapping} request to create a {@link Team} based on the input request.
     *
     * @param teamRequest the {@link TeamRequest} to create a {@link Team}
     * @param request     the {@link HttpServletRequest}
     * @return {@link me.zodac.folding.rest.response.Responses#created(Object, int)} containing the created {@link Team}
     */
    @WriteRequired
    @RolesAllowed("admin")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> create(@RequestBody final TeamRequest teamRequest, final HttpServletRequest request) {
        LOGGER.debug("POST request received to create team at '{}' with request: {}", request::getRequestURI, () -> teamRequest);

        final Team validatedTeam = validateCreate(teamRequest);
        final Team elementWithId = foldingRepository.createTeam(validatedTeam);
        SystemStateManager.next(SystemState.WRITE_EXECUTED);

        LOGGER.info("Created team with ID {}", elementWithId.getId());
        teamCreates.increment();
        return created(elementWithId, elementWithId.getId());
    }

    /**
     * {@link GetMapping} request to retrieve all {@link Team}s.
     *
     * @param request the {@link HttpServletRequest}
     * @return {@link me.zodac.folding.rest.response.Responses#cachedOk(Collection, long)} containing the {@link Team}s
     */
    @ReadRequired
    @PermitAll
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAll(final HttpServletRequest request) {
        LOGGER.debug("GET request received for all teams at '{}'", request::getRequestURI);
        final Collection<Team> elements = foldingRepository.getAllTeams();
        return cachedOk(elements, untilNextMonthUtc(ChronoUnit.SECONDS));
    }

    /**
     * {@link GetMapping} request to retrieve a {@link Team} by {@code teamId}.
     *
     * @param teamId  the ID of the {@link Team} to retrieve
     * @param request the {@link HttpServletRequest}
     * @return {@link me.zodac.folding.rest.response.Responses#cachedOk(Object, long)} containing the {@link Team}
     */
    @ReadRequired
    @PermitAll
    @GetMapping(path = "/{teamId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getById(@PathVariable("teamId") final int teamId, final HttpServletRequest request) {
        LOGGER.debug("GET request for team received at '{}'", request::getRequestURI);

        final Team element = foldingRepository.getTeam(teamId);
        return cachedOk(element, untilNextMonthUtc(ChronoUnit.SECONDS));
    }

    /**
     * {@link GetMapping} request to retrieve a {@link Team} by {@code teamName}.
     *
     * @param teamName the {@code teamName} of the {@link Team} to retrieve
     * @param request  the {@link HttpServletRequest}
     * @return {@link me.zodac.folding.rest.response.Responses#cachedOk(Object, long)} containing the {@link Team}
     */
    @ReadRequired
    @PermitAll
    @GetMapping(path = "/fields", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getByTeamName(@RequestParam("teamName") final String teamName, final HttpServletRequest request) {
        LOGGER.debug("GET request for team received at '{}'", request::getRequestURI);
        if (StringUtils.isBlank(teamName)) {
            final String errorMessage = String.format("Input 'teamName' must not be blank: '%s'", teamName);
            LOGGER.error(errorMessage);
            return badRequest(errorMessage);
        }

        final Optional<Team> optionalTeam = foldingRepository.getAllTeams()
            .stream()
            .filter(team -> team.getTeamName().equalsIgnoreCase(teamName))
            .findAny();

        if (optionalTeam.isEmpty()) {
            LOGGER.error("No team found with 'teamName' '{}'", teamName);
            return notFound();
        }

        final Team element = optionalTeam.get();
        return cachedOk(element, untilNextMonthUtc(ChronoUnit.SECONDS));
    }

    /**
     * {@link PutMapping} request to update an existing {@link Team} based on the input request.
     *
     * @param teamId      the ID of the {@link Team} to be updated
     * @param teamRequest the {@link TeamRequest} to update a {@link Team}
     * @param request     the {@link HttpServletRequest}
     * @return {@link me.zodac.folding.rest.response.Responses#ok(Object, int)} containing the updated {@link Team}
     */
    @WriteRequired
    @RolesAllowed("admin")
    @PutMapping(path = "/{teamId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> updateById(@PathVariable("teamId") final int teamId,
                                        @RequestBody final TeamRequest teamRequest,
                                        final HttpServletRequest request) {
        LOGGER.debug("PUT request for team received at '{}'", request::getRequestURI);

        final Team existingTeam = foldingRepository.getTeam(teamId);

        if (existingTeam.isEqualRequest(teamRequest)) {
            LOGGER.debug("No change necessary");
            return ok(existingTeam);
        }

        final Team validatedHardware = validateUpdate(teamRequest, existingTeam);

        // The payload 'should' have the ID, but it's not guaranteed if the correct URL is used
        final Team teamWithId = Team.updateWithId(existingTeam.getId(), validatedHardware);
        final Team updatedTeamWithId = foldingRepository.updateTeam(teamWithId);
        SystemStateManager.next(SystemState.WRITE_EXECUTED);

        LOGGER.info("Updated team with ID {}", updatedTeamWithId.getId());
        teamUpdates.increment();
        return ok(updatedTeamWithId, updatedTeamWithId.getId());
    }

    /**
     * {@link DeleteMapping} request to delete an existing {@link Team}.
     *
     * @param teamId  the ID of the {@link Team} to be deleted
     * @param request the {@link HttpServletRequest}
     * @return {@link me.zodac.folding.rest.response.Responses#ok()}
     */
    @WriteRequired
    @RolesAllowed("admin")
    @DeleteMapping(path = "/{teamId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> deleteById(@PathVariable("teamId") final int teamId, final HttpServletRequest request) {
        LOGGER.debug("DELETE request for team received at '{}'", request::getRequestURI);

        final Team team = foldingRepository.getTeam(teamId);

        final Team validatedTeam = validateDelete(team);
        foldingRepository.deleteTeam(validatedTeam);
        SystemStateManager.next(SystemState.WRITE_EXECUTED);

        LOGGER.info("Deleted team with ID {}", teamId);
        teamDeletes.increment();
        return ok();
    }

    private Team validateCreate(final TeamRequest teamRequest) {
        return TeamValidator.validateCreate(teamRequest, foldingRepository.getAllTeams());
    }

    private Team validateUpdate(final TeamRequest teamRequest, final Team existingTeam) {
        return TeamValidator.validateUpdate(teamRequest, existingTeam, foldingRepository.getAllTeams());
    }

    private Team validateDelete(final Team team) {
        return TeamValidator.validateDelete(team, foldingRepository.getAllUsersWithoutPasskeys());
    }
}
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

import static me.zodac.folding.rest.response.Responses.cachedOk;
import static me.zodac.folding.rest.response.Responses.created;
import static me.zodac.folding.rest.response.Responses.ok;
import static me.zodac.folding.rest.util.RequestParameterExtractor.extractParameters;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.Collection;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import me.zodac.folding.api.state.SystemState;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.bean.api.FoldingRepository;
import me.zodac.folding.bean.tc.validation.TeamValidator;
import me.zodac.folding.rest.api.tc.request.TeamRequest;
import me.zodac.folding.rest.exception.NotFoundException;
import me.zodac.folding.rest.util.ReadRequired;
import me.zodac.folding.rest.util.WriteRequired;
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
 * REST endpoints for {@code Team Competition} {@link Team}s.
 */
@RestController
@RequestMapping("/teams")
public class TeamEndpoint {

    private static final Logger AUDIT_LOGGER = LogManager.getLogger("audit");

    private final FoldingRepository foldingRepository;
    private final TeamValidator teamValidator;

    // Prometheus counters
    private final Counter teamCreates;
    private final Counter teamUpdates;
    private final Counter teamDeletes;

    /**
     * {@link Autowired} constructor to inject {@link MeterRegistry} and configure Prometheus {@link Counter}s.
     *
     * @param foldingRepository the {@link FoldingRepository}
     * @param registry          the Prometheus {@link MeterRegistry}
     * @param teamValidator     the {@link TeamValidator}
     */
    public TeamEndpoint(final FoldingRepository foldingRepository, final MeterRegistry registry, final TeamValidator teamValidator) {
        this.foldingRepository = foldingRepository;
        this.teamValidator = teamValidator;

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
        AUDIT_LOGGER.info("POST request received to create team at '{}' with request: {}", request::getRequestURI, () -> teamRequest);

        final Team validatedTeam = teamValidator.create(teamRequest);
        final Team elementWithId = foldingRepository.createTeam(validatedTeam);
        SystemStateManager.next(SystemState.WRITE_EXECUTED);

        AUDIT_LOGGER.info("Created team with ID {}", elementWithId.id());
        teamCreates.increment();
        return created(elementWithId, elementWithId.id());
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
        AUDIT_LOGGER.debug("GET request received for all teams at '{}'", request::getRequestURI);
        final Collection<Team> elements = foldingRepository.getAllTeams();
        return cachedOk(elements);
    }

    /**
     * {@link GetMapping} request to retrieve a {@link Team} by {@code teamId}.
     *
     * @param teamId  the ID of the {@link Team} to retrieve
     * @param request the {@link HttpServletRequest}
     * @return {@link me.zodac.folding.rest.response.Responses#cachedOk(Object)} containing the {@link Team}
     */
    @ReadRequired
    @PermitAll
    @GetMapping(path = "/{teamId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getById(@PathVariable("teamId") final int teamId, final HttpServletRequest request) {
        AUDIT_LOGGER.debug("GET request for team received at '{}'", request::getRequestURI);

        final Team element = foldingRepository.getTeam(teamId);
        return cachedOk(element);
    }

    /**
     * {@link GetMapping} request to retrieve a {@link Team} by {@code teamName}.
     *
     * @param teamName the {@code teamName} of the {@link Team} to retrieve
     * @param request  the {@link HttpServletRequest}
     * @return {@link me.zodac.folding.rest.response.Responses#cachedOk(Object)} containing the {@link Team}
     */
    @ReadRequired
    @PermitAll
    @GetMapping(path = "/fields", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getByTeamName(@RequestParam("teamName") final String teamName, final HttpServletRequest request) {
        AUDIT_LOGGER.debug("GET request for team received at '{}?{}'", request::getRequestURI, () -> extractParameters(request));

        final Team retrievedTeam = foldingRepository.getAllTeams()
            .stream()
            .filter(team -> team.teamName().equalsIgnoreCase(teamName))
            .findAny()
            .orElseThrow(() -> new NotFoundException(Team.class, teamName));

        return cachedOk(retrievedTeam);
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
        AUDIT_LOGGER.info("PUT request for team received at '{}' with request {}", request::getRequestURI, () -> teamRequest);

        final Team existingTeam = foldingRepository.getTeam(teamId);

        if (existingTeam.isEqualRequest(teamRequest)) {
            AUDIT_LOGGER.info("Request is same as existing team");
            return ok(existingTeam);
        }

        final Team validatedHardware = teamValidator.update(teamRequest, existingTeam);

        // The payload 'should' have the ID, but it's not guaranteed if the correct URL is used
        final Team teamWithId = Team.updateWithId(existingTeam.id(), validatedHardware);
        final Team updatedTeamWithId = foldingRepository.updateTeam(teamWithId);
        SystemStateManager.next(SystemState.WRITE_EXECUTED);

        AUDIT_LOGGER.info("Updated team with ID {}", updatedTeamWithId.id());
        teamUpdates.increment();
        return ok(updatedTeamWithId, updatedTeamWithId.id());
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
        AUDIT_LOGGER.info("DELETE request for team received at '{}'", request::getRequestURI);

        final Team team = foldingRepository.getTeam(teamId);

        final Team validatedTeam = teamValidator.delete(team);
        foldingRepository.deleteTeam(validatedTeam);
        SystemStateManager.next(SystemState.WRITE_EXECUTED);

        AUDIT_LOGGER.info("Deleted team with ID {}", teamId);
        teamDeletes.increment();
        return ok();
    }
}
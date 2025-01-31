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

package net.zodac.folding.rest.controller;

import static net.zodac.folding.rest.response.Responses.cachedOk;
import static net.zodac.folding.rest.response.Responses.created;
import static net.zodac.folding.rest.response.Responses.ok;
import static net.zodac.folding.rest.util.RequestParameterExtractor.extractParameters;

import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Collection;
import net.zodac.folding.api.state.SystemState;
import net.zodac.folding.api.tc.Team;
import net.zodac.folding.api.util.LoggerName;
import net.zodac.folding.api.util.StringUtils;
import net.zodac.folding.bean.api.FoldingRepository;
import net.zodac.folding.bean.tc.validation.TeamValidator;
import net.zodac.folding.rest.api.tc.request.TeamRequest;
import net.zodac.folding.rest.controller.api.TeamEndpoint;
import net.zodac.folding.rest.exception.NotFoundException;
import net.zodac.folding.rest.util.ReadRequired;
import net.zodac.folding.rest.util.WriteRequired;
import net.zodac.folding.state.SystemStateManager;
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
 * Implementation of {@link TeamEndpoint} REST endpoints.
 */
@RestController
@RequestMapping("/teams")
public class TeamController implements TeamEndpoint {

    private static final Logger AUDIT_LOGGER = LogManager.getLogger(LoggerName.AUDIT.get());

    private final FoldingRepository foldingRepository;
    private final TeamValidator teamValidator;

    /**
     * {@link Autowired} constructor.
     *
     * @param foldingRepository the {@link FoldingRepository}
     * @param teamValidator     the {@link TeamValidator}
     */
    @Autowired
    public TeamController(final FoldingRepository foldingRepository, final TeamValidator teamValidator) {
        this.foldingRepository = foldingRepository;
        this.teamValidator = teamValidator;
    }

    @Override
    @WriteRequired
    @RolesAllowed("admin")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Team> create(@RequestBody final TeamRequest teamRequest, final HttpServletRequest request) {
        AUDIT_LOGGER.info("POST request received to create team at '{}' with request: {}", request.getRequestURI(), teamRequest);

        final Team validatedTeam = teamValidator.create(teamRequest);
        final Team elementWithId = foldingRepository.createTeam(validatedTeam);
        SystemStateManager.next(SystemState.WRITE_EXECUTED);

        AUDIT_LOGGER.info("Created team with ID {}", elementWithId.id());
        return created(elementWithId, elementWithId.id());
    }

    @Override
    @ReadRequired
    @PermitAll
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Collection<Team>> getAll(final HttpServletRequest request) {
        AUDIT_LOGGER.debug("GET request received for all teams at '{}'", request.getRequestURI());
        final Collection<Team> elements = foldingRepository.getAllTeams();
        return cachedOk(elements);
    }

    @Override
    @ReadRequired
    @PermitAll
    @GetMapping(path = "/{teamId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Team> getById(@PathVariable("teamId") final int teamId, final HttpServletRequest request) {
        AUDIT_LOGGER.debug("GET request for team received at '{}'", request.getRequestURI());

        final Team element = foldingRepository.getTeam(teamId);
        return cachedOk(element);
    }

    @Override
    @ReadRequired
    @PermitAll
    @GetMapping(path = "/fields", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Team> getByTeamName(@RequestParam("teamName") final String teamName, final HttpServletRequest request) {
        AUDIT_LOGGER.debug("GET request for team received at '{}?{}'", request.getRequestURI(), extractParameters(request));
        final String unescapedTeamName = StringUtils.unescapeHtml(teamName);

        final Team retrievedTeam = foldingRepository.getAllTeams()
            .stream()
            .filter(team -> team.teamName().equalsIgnoreCase(unescapedTeamName))
            .findAny()
            .orElseThrow(() -> new NotFoundException(Team.class, unescapedTeamName));

        return cachedOk(retrievedTeam);
    }

    @Override
    @WriteRequired
    @RolesAllowed("admin")
    @PutMapping(path = "/{teamId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Team> updateById(@PathVariable("teamId") final int teamId,
                                           @RequestBody final TeamRequest teamRequest,
                                           final HttpServletRequest request) {
        AUDIT_LOGGER.info("PUT request for team received at '{}' with request {}", request.getRequestURI(), teamRequest);

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
        return ok(updatedTeamWithId, updatedTeamWithId.id());
    }

    @Override
    @WriteRequired
    @RolesAllowed("admin")
    @DeleteMapping(path = "/{teamId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteById(@PathVariable("teamId") final int teamId, final HttpServletRequest request) {
        AUDIT_LOGGER.info("DELETE request for team received at '{}'", request.getRequestURI());

        final Team team = foldingRepository.getTeam(teamId);

        final Team validatedTeam = teamValidator.delete(team);
        foldingRepository.deleteTeam(validatedTeam);
        SystemStateManager.next(SystemState.WRITE_EXECUTED);

        AUDIT_LOGGER.info("Deleted team with ID {}", teamId);
        return ok();
    }
}

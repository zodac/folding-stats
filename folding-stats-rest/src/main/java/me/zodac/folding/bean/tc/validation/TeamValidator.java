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

package me.zodac.folding.bean.tc.validation;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import me.zodac.folding.api.exception.ConflictException;
import me.zodac.folding.api.exception.UsedByException;
import me.zodac.folding.api.exception.ValidationException;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.bean.api.FoldingRepository;
import me.zodac.folding.rest.api.tc.request.TeamRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Validator class to validate a {@link Team} or {@link TeamRequest}.
 */
@Component
public class TeamValidator {

    private static final String CONFLICTING_ATTRIBUTE = "teamName";

    private final FoldingRepository foldingRepository;

    /**
     * {@link Autowired} constructor.
     *
     * @param foldingRepository the {@link FoldingRepository}
     */
    @Autowired
    public TeamValidator(final FoldingRepository foldingRepository) {
        this.foldingRepository = foldingRepository;
    }

    /**
     * Validates a {@link TeamRequest} for a {@link Team} to be created on the system.
     *
     * <p>
     * Validation checks include:
     * <ul>
     *     <li>Field 'teamName' must not be empty</li>
     *     <li>If field 'teamName' is valid, it must not be used by any other {@link Team}</li>
     *     <li>If field 'forumLink' is not empty, it must be a valid URL</li>
     * </ul>
     *
     * @param teamRequest the {@link TeamRequest} to validate
     * @return the validated {@link Team}
     * @throws ConflictException   thrown if the input conflicts with an existing {@link Hardware}
     * @throws ValidationException thrown if the input fails validation
     */
    public Team create(final TeamRequest teamRequest) {
        // The teamName must be unique
        final Optional<Team> teamWithMatchingName = getTeamWithName(teamRequest.getTeamName());
        if (teamWithMatchingName.isPresent()) {
            throw new ConflictException(teamRequest, teamWithMatchingName.get(), CONFLICTING_ATTRIBUTE);
        }

        teamRequest.validate();
        return Team.createWithoutId(teamRequest);
    }

    /**
     * Validates a {@link TeamRequest} to update an existing {@link Team} on the system.
     *
     * <p>
     * Validation checks include:
     * <ul>
     *     <li>Field 'teamName' must not be empty</li>
     *     <li>If field 'teamName' is valid, it must not be used by another {@link Team}, unless it is the {@link Team} to be updated</li>
     *     <li>If field 'forumLink' is not empty, it must be a valid URL</li>
     * </ul>
     *
     * @param teamRequest  the {@link TeamRequest} to validate
     * @param existingTeam the already existing {@link Team} in the system to be updated
     * @return the validated {@link Team}
     * @throws ConflictException   thrown if the input conflicts with an existing {@link Hardware}
     * @throws ValidationException thrown if the input fails validation
     */
    public Team update(final TeamRequest teamRequest, final Team existingTeam) {
        // The teamName must be unique, unless replacing the same team
        final Optional<Team> teamWithMatchingName = getTeamWithName(teamRequest.getTeamName());
        if (teamWithMatchingName.isPresent() && teamWithMatchingName.get().id() != existingTeam.id()) {
            throw new ConflictException(teamRequest, teamWithMatchingName.get(), CONFLICTING_ATTRIBUTE);
        }

        teamRequest.validate();
        return Team.createWithoutId(teamRequest);
    }

    /**
     * Validates a {@link Team} to be deleted from the system.
     *
     * <p>
     * If the {@link Team} is in use by a {@link User}, it cannot be deleted.
     *
     * @param team the {@link Team} to validate
     * @return the validated {@link Team}
     * @throws UsedByException thrown if the {@link Team} is in use by a {@link User}
     */
    public Team delete(final Team team) {
        final Collection<User> usersWithMatchingTeam = getUsersOnTeam(team.id());

        if (!usersWithMatchingTeam.isEmpty()) {
            throw new UsedByException(team, usersWithMatchingTeam);
        }

        return team;
    }

    private Optional<Team> getTeamWithName(final String teamName) {
        return foldingRepository.getAllTeams()
            .stream()
            .filter(team -> team.teamName().equalsIgnoreCase(teamName))
            .findAny();
    }

    private Collection<User> getUsersOnTeam(final int teamId) {
        if (teamId == Team.EMPTY_TEAM_ID) {
            return Collections.emptyList();
        }

        return foldingRepository.getAllUsersWithoutPasskeys()
            .stream()
            .filter(user -> user.team().id() == teamId)
            .toList();
    }
}

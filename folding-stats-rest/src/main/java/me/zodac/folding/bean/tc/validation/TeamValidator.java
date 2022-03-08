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

package me.zodac.folding.bean.tc.validation;

import static me.zodac.folding.api.util.StringUtils.isBlank;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import me.zodac.folding.api.exception.ConflictException;
import me.zodac.folding.api.exception.UsedByException;
import me.zodac.folding.api.exception.ValidationException;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.rest.api.tc.request.TeamRequest;

/**
 * Validator class to validate a {@link Team} or {@link TeamRequest}.
 */
public final class TeamValidator {

    private TeamValidator() {

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
     * @param allTeams    all {@link Team}s on the system
     * @return the validated {@link Team}
     * @throws ConflictException   thrown if the input conflicts with an existing {@link Hardware}
     * @throws ValidationException thrown if the input fails validation
     */
    public static Team validateCreate(final TeamRequest teamRequest, final Collection<Team> allTeams) {
        // The teamName must be unique
        final Optional<Team> teamWithMatchingName = getTeamWithName(teamRequest.getTeamName(), allTeams);
        if (teamWithMatchingName.isPresent()) {
            throw new ConflictException(teamRequest, teamWithMatchingName.get(), "teamName");
        }

        final Collection<String> failureMessages = teamRequest.validate();
        if (!failureMessages.isEmpty()) {
            throw new ValidationException(teamRequest, failureMessages);
        }

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
     *                     @param allTeams all {@link Team}s on the system
     * @return the validated {@link Team}
     * @throws ConflictException   thrown if the input conflicts with an existing {@link Hardware}
     * @throws ValidationException thrown if the input fails validation
     */
    public static Team validateUpdate(final TeamRequest teamRequest, final Team existingTeam, final Collection<Team> allTeams) {
        // The teamName must be unique, unless replacing the same team
        final Optional<Team> teamWithMatchingName = getTeamWithName(teamRequest.getTeamName(), allTeams);
        if (teamWithMatchingName.isPresent() && teamWithMatchingName.get().getId() != existingTeam.getId()) {
            throw new ConflictException(teamRequest, teamWithMatchingName.get(), "teamName");
        }

        final Collection<String> failureMessages = teamRequest.validate();
        if (!failureMessages.isEmpty()) {
            throw new ValidationException(teamRequest, failureMessages);
        }

        return Team.createWithoutId(teamRequest);
    }

    /**
     * Validates a {@link Team} to be deleted from the system.
     *
     * <p>
     * If the {@link Team} is in use by a {@link User}, it cannot be deleted.
     *
     * @param team     the {@link Team} to validate
     * @param allUsers all {@link User}s on the system
     * @return the validated {@link Team}
     * @throws UsedByException thrown if the {@link Team} is in use by a {@link User}
     */
    public static Team validateDelete(final Team team, final Collection<User> allUsers) {
        final Collection<User> usersWithMatchingTeam = getUsersOnTeam(team.getId(), allUsers);

        if (!usersWithMatchingTeam.isEmpty()) {
            throw new UsedByException(team, usersWithMatchingTeam);
        }

        return team;
    }

    private static Optional<Team> getTeamWithName(final String teamName, final Collection<Team> allTeams) {
        if (isBlank(teamName)) {
            return Optional.empty();
        }

        return allTeams
            .stream()
            .filter(team -> team.getTeamName().equalsIgnoreCase(teamName))
            .findAny();
    }

    private static Collection<User> getUsersOnTeam(final int teamId, final Collection<User> allUsers) {
        if (teamId == Team.EMPTY_TEAM_ID) {
            return Collections.emptyList();
        }

        return allUsers
            .stream()
            .filter(user -> user.getTeam().getId() == teamId)
            .map(User::hidePasskey)
            .toList();
    }
}

package me.zodac.folding.rest.validator;

import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.rest.api.tc.request.TeamRequest;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;

/**
 * Validator class to validate a {@link Team} or {@link TeamRequest}.
 */
public final class TeamValidator {

    private static final UrlValidator URL_VALIDATOR = new UrlValidator();

    private TeamValidator() {

    }

    /**
     * Validates a {@link TeamRequest} for a {@link Team} to be created on the system.
     *
     * <p>
     * Validation checks include:
     * <ul>
     *     <li>Field 'teamName' must not be empty</li>
     *     <li>If field 'teamName' is not empty, it must not be used by any other {@link Team}</li>
     *     <li>If field 'forumLink' is not empty, it must be a valid URL</li>
     * </ul>
     *
     * @param teamRequest the {@link TeamRequest} to validate
     * @param allTeams    all {@link Team}s on the system
     * @return the {@link ValidationResult}
     * @see UrlValidator#isValid(String)
     */
    public static ValidationResult<Team> validateCreate(final TeamRequest teamRequest, final Collection<Team> allTeams) {
        if (teamRequest == null) {
            return ValidationResult.nullObject();
        }

        // Team name must be unique
        final Optional<Team> teamWithMatchingName = getTeamWithName(teamRequest.getTeamName(), allTeams);
        if (teamWithMatchingName.isPresent()) {
            return ValidationResult.conflictingWith(teamRequest, teamWithMatchingName.get(), List.of("teamName"));
        }

        final List<String> failureMessages = Stream.of(
                teamName(teamRequest),
                forumLink(teamRequest)
            )
            .filter(Objects::nonNull)
            .collect(toList());

        if (!failureMessages.isEmpty()) {
            return ValidationResult.failure(teamRequest, failureMessages);
        }

        final Team convertedTeam = Team.createWithoutId(teamRequest);
        return ValidationResult.successful(convertedTeam);
    }

    /**
     * Validates a {@link TeamRequest} to update an existing {@link Team} on the system.
     *
     * <p>
     * Validation checks include:
     * <ul>
     *     <li>Field 'teamName' must not be empty</li>
     *     <li>If field 'teamName' is not empty, it must not be used by another {@link Team}, unless it is the {@link Team} to be updated</li>
     *     <li>If field 'forumLink' is not empty, it must be a valid URL</li>
     * </ul>
     *
     * @param teamRequest  the {@link TeamRequest} to validate
     * @param existingTeam the already existing {@link Team} in the system to be updated
     *                     @param allTeams all {@link Team}s on the system
     * @return the {@link ValidationResult}
     */
    public static ValidationResult<Team> validateUpdate(final TeamRequest teamRequest, final Team existingTeam, final Collection<Team> allTeams) {
        if (teamRequest == null || existingTeam == null) {
            return ValidationResult.nullObject();
        }

        // Team name must be unique
        final Optional<Team> teamWithMatchingName = getTeamWithName(teamRequest.getTeamName(), allTeams);
        if (teamWithMatchingName.isPresent() && teamWithMatchingName.get().getId() != existingTeam.getId()) {
            return ValidationResult.conflictingWith(teamRequest, teamWithMatchingName.get(), List.of("teamName"));
        }

        final List<String> failureMessages = Stream.of(
                teamName(teamRequest),
                forumLink(teamRequest)
            )
            .filter(Objects::nonNull)
            .collect(toList());

        if (!failureMessages.isEmpty()) {
            return ValidationResult.failure(teamRequest, failureMessages);
        }

        final Team convertedTeam = Team.createWithoutId(teamRequest);
        return ValidationResult.successful(convertedTeam);
    }

    /**
     * Validates a {@link Team} to be deleted from the system.
     *
     * @param team     the {@link Team} to validate
     * @param allUsers all {@link User}s on the system
     * @return the {@link ValidationResult}
     */
    public static ValidationResult<Team> validateDelete(final Team team, final Collection<User> allUsers) {
        final Collection<User> usersWithMatchingTeam = getUsersOnTeam(team.getId(), allUsers);

        if (!usersWithMatchingTeam.isEmpty()) {
            return ValidationResult.usedBy(team, usersWithMatchingTeam);
        }

        return ValidationResult.successful(team);
    }

    private static Optional<Team> getTeamWithName(final String teamName, final Collection<Team> allTeams) {
        if (StringUtils.isBlank(teamName)) {
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
            .collect(toList());
    }

    private static String teamName(final TeamRequest teamRequest) {
        return StringUtils.isNotBlank(teamRequest.getTeamName()) ? null : "Field 'teamName' must not be empty";
    }

    private static String forumLink(final TeamRequest teamRequest) {
        return (StringUtils.isBlank(teamRequest.getForumLink()) || URL_VALIDATOR.isValid(teamRequest.getForumLink())) ? null :
            String.format("Field 'forumLink' is not a valid link: '%s'", teamRequest.getForumLink());
    }
}

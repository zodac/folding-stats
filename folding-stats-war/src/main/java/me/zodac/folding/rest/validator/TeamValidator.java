package me.zodac.folding.rest.validator;

import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import me.zodac.folding.api.ejb.BusinessLogic;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.validator.ValidationResponse;
import me.zodac.folding.rest.api.tc.request.TeamRequest;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;

/**
 * Validator class to validate a {@link Team} or {@link TeamRequest}.
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class TeamValidator {

    private static final UrlValidator URL_VALIDATOR = new UrlValidator();

    private final transient BusinessLogic businessLogic;

    /**
     * Creates a {@link TeamValidator}.
     *
     * @param businessLogic the {@link BusinessLogic} used for retrieval of {@link User}s for conflict checks
     * @return the created {@link TeamValidator}
     */
    public static TeamValidator create(final BusinessLogic businessLogic) {
        return new TeamValidator(businessLogic);
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
     * @return the {@link ValidationResponse}
     * @see UrlValidator#isValid(String)
     */
    public ValidationResponse<Team> validateCreate(final TeamRequest teamRequest) {
        if (teamRequest == null) {
            return ValidationResponse.nullObject();
        }

        // Team name must be unique
        final Optional<Team> teamWithMatchingName = businessLogic.getTeamWithName(teamRequest.getTeamName());
        if (teamWithMatchingName.isPresent()) {
            return ValidationResponse.conflictingWith(teamRequest, teamWithMatchingName.get(), List.of("teamName"));
        }

        final List<String> failureMessages = Stream.of(
                teamName(teamRequest),
                forumLink(teamRequest)
            )
            .filter(Objects::nonNull)
            .collect(toList());

        if (!failureMessages.isEmpty()) {
            return ValidationResponse.failure(teamRequest, failureMessages);
        }

        final Team convertedTeam = Team.createWithoutId(teamRequest);
        return ValidationResponse.success(convertedTeam);
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
     * @return the {@link ValidationResponse}
     */
    public ValidationResponse<Team> validateUpdate(final TeamRequest teamRequest, final Team existingTeam) {
        if (teamRequest == null || existingTeam == null) {
            return ValidationResponse.nullObject();
        }

        // Team name must be unique
        final Optional<Team> teamWithMatchingName = businessLogic.getTeamWithName(teamRequest.getTeamName());
        if (teamWithMatchingName.isPresent() && teamWithMatchingName.get().getId() != existingTeam.getId()) {
            return ValidationResponse.conflictingWith(teamRequest, teamWithMatchingName.get(), List.of("teamName"));
        }

        final List<String> failureMessages = Stream.of(
                teamName(teamRequest),
                forumLink(teamRequest)
            )
            .filter(Objects::nonNull)
            .collect(toList());

        if (!failureMessages.isEmpty()) {
            return ValidationResponse.failure(teamRequest, failureMessages);
        }

        final Team convertedTeam = Team.createWithoutId(teamRequest);
        return ValidationResponse.success(convertedTeam);
    }

    /**
     * Validates a {@link Team} to be deleted from the system.
     *
     * @param team the {@link Team} to validate
     * @return the {@link ValidationResponse}
     */
    public ValidationResponse<Team> validateDelete(final Team team) {
        final Collection<User> usersWithMatchingTeam = businessLogic.getUsersOnTeam(team);

        if (!usersWithMatchingTeam.isEmpty()) {
            return ValidationResponse.usedBy(team, usersWithMatchingTeam);
        }

        return ValidationResponse.success(team);
    }

    private static String teamName(final TeamRequest teamRequest) {
        return StringUtils.isNotBlank(teamRequest.getTeamName()) ? null : "Field 'teamName' must not be empty";
    }

    private static String forumLink(final TeamRequest teamRequest) {
        return (StringUtils.isBlank(teamRequest.getForumLink()) || URL_VALIDATOR.isValid(teamRequest.getForumLink())) ? null :
            String.format("Field 'forumLink' is not a valid link: '%s'", teamRequest.getForumLink());
    }
}

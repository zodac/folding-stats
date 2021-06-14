package me.zodac.folding.rest.validator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import me.zodac.folding.api.ejb.BusinessLogic;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.validator.ValidationResponse;
import me.zodac.folding.ejb.OldFacade;
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
    private final transient OldFacade oldFacade;

    /**
     * Creates a {@link TeamValidator}.
     *
     * @param businessLogic the {@link BusinessLogic} used for retrieval of {@link User}s for conflict checks
     * @param oldFacade     the {@link OldFacade} used for retrieval of {@link User}s for conflict checks
     * @return the created {@link TeamValidator}
     */
    public static TeamValidator createValidator(final BusinessLogic businessLogic, final OldFacade oldFacade) {
        return new TeamValidator(businessLogic, oldFacade);
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

        final List<String> failureMessages = new ArrayList<>(2);

        if (StringUtils.isBlank(teamRequest.getTeamName())) {
            failureMessages.add("Field 'teamName' must not be empty");
        } else {
            final Optional<Team> teamWithMatchingName = businessLogic.getTeamWithName(teamRequest.getTeamName());

            if (teamWithMatchingName.isPresent()) {
                return ValidationResponse.conflictingWith(teamRequest, teamWithMatchingName.get(), List.of("teamName"));
            }
        }

        if (StringUtils.isNotBlank(teamRequest.getForumLink()) && !URL_VALIDATOR.isValid(teamRequest.getForumLink())) {
            failureMessages.add(String.format("Field 'forumLink' is not a valid link: '%s'", teamRequest.getForumLink()));
        }

        if (failureMessages.isEmpty()) {
            final Team convertedTeam = Team.createWithoutId(teamRequest.getTeamName(), teamRequest.getTeamDescription(), teamRequest.getForumLink());
            return ValidationResponse.success(convertedTeam);
        }

        return ValidationResponse.failure(teamRequest, failureMessages);
    }

    /**
     * Validates a {@link TeamRequest} to update an existing {@link Team} on the system.
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
     */
    public ValidationResponse<Team> validateUpdate(final TeamRequest teamRequest) {
        if (teamRequest == null) {
            return ValidationResponse.nullObject();
        }

        final List<String> failureMessages = new ArrayList<>(2);

        if (StringUtils.isBlank(teamRequest.getTeamName())) {
            failureMessages.add("Field 'teamName' must not be empty");
        }

        if (StringUtils.isNotBlank(teamRequest.getForumLink()) && !URL_VALIDATOR.isValid(teamRequest.getForumLink())) {
            failureMessages.add(String.format("Attribute 'forumLink' is not a valid link: '%s'", teamRequest.getForumLink()));
        }

        if (failureMessages.isEmpty()) {
            final Team convertedTeam = Team.createWithoutId(teamRequest.getTeamName(), teamRequest.getTeamDescription(), teamRequest.getForumLink());
            return ValidationResponse.success(convertedTeam);
        }

        return ValidationResponse.failure(teamRequest, failureMessages);
    }

    /**
     * Validates a {@link Team} to be deleted from the system.
     *
     * @param team the {@link Team} to validate
     * @return the {@link ValidationResponse}
     */
    public ValidationResponse<Team> validateDelete(final Team team) {
        final Optional<User> userWithMatchingTeam = oldFacade.getUserWithTeam(team);

        return userWithMatchingTeam
            .<ValidationResponse<Team>>map(user -> ValidationResponse.usedBy(team, user))
            .orElseGet(() -> ValidationResponse.success(team));
    }
}

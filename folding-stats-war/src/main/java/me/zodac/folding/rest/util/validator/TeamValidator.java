package me.zodac.folding.rest.util.validator;

import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.validator.ValidationResponse;
import me.zodac.folding.ejb.BusinessLogic;
import me.zodac.folding.rest.api.tc.request.TeamRequest;
import org.apache.commons.validator.routines.UrlValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class TeamValidator {

    private static final UrlValidator URL_VALIDATOR = new UrlValidator();

    private transient final BusinessLogic businessLogic;

    private TeamValidator(final BusinessLogic businessLogic) {
        this.businessLogic = businessLogic;
    }

    public static TeamValidator create(final BusinessLogic businessLogic) {
        return new TeamValidator(businessLogic);
    }

    public ValidationResponse<Team> validateCreate(final TeamRequest teamRequest) {
        if (teamRequest == null) {
            return ValidationResponse.nullObject();
        }

        final List<String> failureMessages = new ArrayList<>();

        if (isBlank(teamRequest.getTeamName())) {
            failureMessages.add("Field 'teamName' must not be empty");
        } else {
            final Optional<Team> teamWithMatchingName = businessLogic.getTeamWithName(teamRequest.getTeamName());

            if (teamWithMatchingName.isPresent()) {
                return ValidationResponse.conflictingWith(teamRequest, teamWithMatchingName.get(), List.of("teamName"));
            }
        }

        if (!isBlank(teamRequest.getForumLink()) && !URL_VALIDATOR.isValid(teamRequest.getForumLink())) {
            failureMessages.add(String.format("Field 'forumLink' is not a valid link: '%s'", teamRequest.getForumLink()));
        }

        if (failureMessages.isEmpty()) {
            final Team convertedTeam = Team.createWithoutId(teamRequest.getTeamName(), teamRequest.getTeamDescription(), teamRequest.getForumLink());
            return ValidationResponse.success(convertedTeam);
        }

        return ValidationResponse.failure(teamRequest, failureMessages);
    }

    public ValidationResponse<Team> validateUpdate(final TeamRequest teamRequest) {
        if (teamRequest == null) {
            return ValidationResponse.nullObject();
        }

        final List<String> failureMessages = new ArrayList<>();

        if (isBlank(teamRequest.getTeamName())) {
            failureMessages.add("Field 'teamName' must not be empty");
        }

        if (!isBlank(teamRequest.getForumLink()) && !URL_VALIDATOR.isValid(teamRequest.getForumLink())) {
            failureMessages.add(String.format("Attribute 'forumLink' is not a valid link: '%s'", teamRequest.getForumLink()));
        }

        if (failureMessages.isEmpty()) {
            final Team convertedTeam = Team.createWithoutId(teamRequest.getTeamName(), teamRequest.getTeamDescription(), teamRequest.getForumLink());
            return ValidationResponse.success(convertedTeam);
        }

        return ValidationResponse.failure(teamRequest, failureMessages);
    }

    public ValidationResponse<Team> validateDelete(final Team team) {
        final Optional<User> userWithMatchingTeam = businessLogic.getUserWithTeam(team);

        if (userWithMatchingTeam.isPresent()) {
            return ValidationResponse.usedBy(team, userWithMatchingTeam.get());
        }

        return ValidationResponse.success(team);
    }

    private boolean isBlank(final String input) {
        return input == null || input.isBlank();
    }
}

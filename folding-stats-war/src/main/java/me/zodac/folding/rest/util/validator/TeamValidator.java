package me.zodac.folding.rest.util.validator;

import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.validator.ValidationResponse;
import me.zodac.folding.rest.api.tc.request.TeamRequest;
import org.apache.commons.validator.routines.UrlValidator;

import java.util.ArrayList;
import java.util.List;

public final class TeamValidator {

    private static final UrlValidator URL_VALIDATOR = new UrlValidator();


    private TeamValidator() {

    }

    public static TeamValidator create() {
        return new TeamValidator();
    }


    public ValidationResponse<Team> validate(final TeamRequest teamRequest) {
        if (teamRequest == null) {
            return ValidationResponse.nullObject();
        }

        final List<String> failureMessages = new ArrayList<>();

        if (isBlank(teamRequest.getTeamName())) {
            failureMessages.add("Attribute 'teamName' must not be empty");
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

    private boolean isBlank(final String input) {
        return input == null || input.isBlank();
    }
}

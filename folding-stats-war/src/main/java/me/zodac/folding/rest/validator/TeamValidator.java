package me.zodac.folding.rest.validator;

import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.validator.ValidationResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;

import java.util.ArrayList;
import java.util.List;

public class TeamValidator {

    private static final UrlValidator URL_VALIDATOR = new UrlValidator();


    private TeamValidator() {

    }

    public static TeamValidator create() {
        return new TeamValidator();
    }


    public ValidationResponse isValid(final Team team) {
        if (team == null) {
            return ValidationResponse.nullObject();
        }

        final List<String> failureMessages = new ArrayList<>();

        if (StringUtils.isBlank(team.getTeamName())) {
            failureMessages.add("Attribute 'teamName' must not be empty");
        }

        if (StringUtils.isNotEmpty(team.getForumLink()) && !URL_VALIDATOR.isValid(team.getForumLink())) {
            failureMessages.add(String.format("Attribute 'forumLink' is not a valid link: '%s'", team.getForumLink()));
        }

        if (failureMessages.isEmpty()) {
            return ValidationResponse.success();
        }

        return ValidationResponse.failure(team, failureMessages);
    }
}

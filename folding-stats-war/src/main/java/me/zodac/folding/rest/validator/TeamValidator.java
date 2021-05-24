package me.zodac.folding.rest.validator;

import me.zodac.folding.api.tc.Category;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.tc.stats.RetiredUserTcStats;
import me.zodac.folding.api.validator.ValidationResponse;
import me.zodac.folding.ejb.BusinessLogic;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public class TeamValidator {

    private static final UrlValidator URL_VALIDATOR = new UrlValidator();

    private final BusinessLogic businessLogic;

    private TeamValidator(final BusinessLogic businessLogic) {
        this.businessLogic = businessLogic;
    }

    public static TeamValidator create(final BusinessLogic businessLogic) {
        return new TeamValidator(businessLogic);
    }


    public ValidationResponse isValid(final Team team) {
        if (team == null) {
            return ValidationResponse.nullObject();
        }

        final List<String> failureMessages = new ArrayList<>();

        if (StringUtils.isBlank(team.getTeamName())) {
            failureMessages.add("Attribute 'teamName' must not be empty");
        }

        if (StringUtils.isNotEmpty(team.getForumLink())) {
            if (!URL_VALIDATOR.isValid(team.getForumLink())) {
                failureMessages.add(String.format("Attribute 'forumLink' is not a valid link: '%s'", team.getForumLink()));
            }
        }

        if (team.getCaptainUserId() <= User.EMPTY_USER_ID || businessLogic.doesNotContainUser(team.getCaptainUserId())) {
            final List<String> availableUsers = businessLogic
                    .getAllUsersOrEmpty()
                    .stream()
                    .map(foldingUser -> String.format("%s: %s", foldingUser.getId(), foldingUser.getFoldingUserName()))
                    .collect(toList());

            failureMessages.add(String.format("Attribute 'captainUserId' must be one of: %s", availableUsers));
        }

        if (!team.getUserIds().contains(team.getCaptainUserId())) {
            failureMessages.add(String.format("Attribute 'captainUserId' must be in the team, must be one of: %s", team.getUserIds()));
        }

        if (team.getUserIds().size() > Category.maximumPermittedAmountForAllCategories()) {
            failureMessages.add(String.format("Attribute 'userIds' has %s users, maximum permitted is %s", team.getUserIds().size(), Category.maximumPermittedAmountForAllCategories()));
        }

        if (team.getUserIds().isEmpty()) {
            final List<String> availableUsers = businessLogic
                    .getAllUsersOrEmpty()
                    .stream()
                    .map(foldingUser -> String.format("%s: %s", foldingUser.getId(), foldingUser.getFoldingUserName()))
                    .collect(toList());

            failureMessages.add(String.format("Attribute 'userIds' must contain at least one of: %s", availableUsers));
        }

        final List<Integer> invalidUserIds = new ArrayList<>(team.getUserIds().size());
        for (final int userId : team.getUserIds()) {
            if (businessLogic.doesNotContainUser(userId)) {
                invalidUserIds.add(userId);
            }
        }

        if (!invalidUserIds.isEmpty()) {
            final List<String> availableUsers = businessLogic
                    .getAllUsersOrEmpty()
                    .stream()
                    .map(foldingUser -> String.format("%s: %s", foldingUser.getId(), foldingUser.getFoldingUserName()))
                    .collect(toList());

            failureMessages.add(String.format("Attribute 'userIds' contains invalid IDs %s, must be: %s", invalidUserIds, availableUsers));
        }

        for (final Team existingTeam : businessLogic.getAllTeamsOrEmpty()) {
            if (existingTeam.getTeamName().equalsIgnoreCase(team.getTeamName())) {
                continue;
            }

            final Set<Integer> existingTeamUserIds = new HashSet<>(existingTeam.getUserIds());
            final Set<Integer> newTeamUserIds = new HashSet<>(team.getUserIds());
            existingTeamUserIds.retainAll(newTeamUserIds);

            if (!existingTeamUserIds.isEmpty()) {
                failureMessages.add(String.format("User %s already exists in team %s", existingTeamUserIds, team.getTeamName()));
            }
        }

        // No point checking the category count if the team is already invalid, perhaps none of the users are correct at this point
        if (failureMessages.isEmpty()) {
            final Map<Category, Long> categoryCount = team.getUserIds()
                    .stream()
                    .map(businessLogic::getUserOrNull)
                    .filter(Objects::nonNull)
                    .map(User::getCategory)
                    .map(Category::get)
                    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

            for (final Map.Entry<Category, Long> categoryAndCount : categoryCount.entrySet()) {
                if (categoryAndCount.getValue() > categoryAndCount.getKey().permittedAmount()) {
                    failureMessages.add(String.format("Found %s users of category %s, only %s permitted", categoryAndCount.getValue(), categoryAndCount.getKey().displayName(), categoryAndCount.getKey().permittedAmount()));
                }
            }
        }

        if (failureMessages.isEmpty()) {
            return ValidationResponse.success();
        }

        return ValidationResponse.failure(team, failureMessages);
    }

    public ValidationResponse isValidRetirement(final Team team, final int userId) {
        final List<String> failureMessages = new ArrayList<>();

        if (team.getCaptainUserId() == userId) {
            failureMessages.add(String.format("Cannot retire captain (user ID: %s), update captain first", userId));
        }

        if (!team.getUserIds().contains(userId)) {
            failureMessages.add(String.format("Cannot retire user ID %s, must be: %s", userId, team.getUserIds()));
        }

        if (failureMessages.isEmpty()) {
            return ValidationResponse.success();
        }

        return ValidationResponse.failure(team, failureMessages);
    }


    public ValidationResponse isValidUnretirement(final Team team, final int retiredUserId) {
        final List<String> failureMessages = new ArrayList<>();

        if (team.getUserIds().size() == Category.maximumPermittedAmountForAllCategories()) {
            failureMessages.add(String.format("Unable to add retired user ID %s to team %s, %s users already on the team", retiredUserId, team, team.getUserIds().size()));
        }

        if (businessLogic.doesNotContainRetiredUser(retiredUserId)) {
            final List<Integer> availableRetiredUsers = businessLogic
                    .getAllRetiredUserStats()
                    .stream()
                    .mapToInt(RetiredUserTcStats::getRetiredUserId)
                    .boxed()
                    .collect(toList());

            failureMessages.add(String.format("Invalid retired user ID %s, must be one of: %s", retiredUserId, availableRetiredUsers));
        }

        // TODO: [zodac] I dunno... can't unretire a user twice, should retired users have a 'still_retired' flag? Seems stupid
        //   Maybe a retiredUserCache is better? With the new retiredUserId?
//        if (!UserCache.get().getRetired().containsKey(retiredUserId)) {
//            failureMessages.add(String.format("Attribute 'retiredUserId' must be a retired user", UserCache.get().getRetired().values()));
//        }

        // TODO: [zodac] What if I add a user that was previously retired, unretired, then retired again?
        //   With no 'still_retired' flag, it's possible to add it back to its original team by mistake

        if (failureMessages.isEmpty()) {
            return ValidationResponse.success();
        }


        return ValidationResponse.failure(team, failureMessages);
    }
}

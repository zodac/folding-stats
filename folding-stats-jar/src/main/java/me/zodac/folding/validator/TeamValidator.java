package me.zodac.folding.validator;

import me.zodac.folding.api.tc.Category;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.cache.UserCache;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public class TeamValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(TeamValidator.class);

    private TeamValidator() {

    }

    public static ValidationResponse isValid(final Team team) {
        final List<String> failureMessages = new ArrayList<>();

        if (StringUtils.isBlank(team.getTeamName())) {
            failureMessages.add("Attribute 'teamName' must not be empty");
        }

        if (team.getCaptainUserId() <= User.EMPTY_USER_ID || UserCache.get().doesNotContain(team.getCaptainUserId())) {
            final List<String> availableUsers = UserCache.get()
                    .getAll()
                    .stream()
                    .map(foldingUser -> String.format("%s: %s", foldingUser.getId(), foldingUser.getFoldingUserName()))
                    .collect(toList());

            failureMessages.add(String.format("Attribute 'captainUserId' must be one of: %s", availableUsers));
        }

        if (!team.getUserIds().contains(team.getCaptainUserId())) {
            failureMessages.add(String.format("Attribute 'captainUserId' must be in the team, must be one of: %s", team.getUserIds()));
        }

        if (team.getUserIds().size() > Category.maximumPermittedAmount()) {
            failureMessages.add(String.format("Attribute 'userIds' has %s users, maximum permitted is %s", team.getUserIds().size(), Category.maximumPermittedAmount()));
        }

        if (team.getUserIds().isEmpty()) {
            final List<String> availableUsers = UserCache.get()
                    .getAll()
                    .stream()
                    .map(foldingUser -> String.format("%s: %s", foldingUser.getId(), foldingUser.getFoldingUserName()))
                    .collect(toList());

            failureMessages.add(String.format("Attribute 'userIds' must contain at least one of: %s", availableUsers));
        }

        final List<Integer> invalidUserIds = new ArrayList<>(team.getUserIds().size());
        for (final int userId : team.getUserIds()) {
            if (UserCache.get().doesNotContain(userId)) {
                invalidUserIds.add(userId);
            }
        }

        if (!invalidUserIds.isEmpty()) {
            final List<String> availableUsers = UserCache.get()
                    .getAll()
                    .stream()
                    .map(foldingUser -> String.format("%s: %s", foldingUser.getId(), foldingUser.getFoldingUserName()))
                    .collect(toList());

            failureMessages.add(String.format("Attribute 'userIds' contains invalid IDs %s, must be: %s", invalidUserIds, availableUsers));
        }


        // No point checking the category count if the team is already invalid, perhaps none of the users are correct at this point
        if (failureMessages.isEmpty()) {
            final Map<Category, Long> categoryCount = team.getUserIds()
                    .stream()
                    .map(userId -> UserCache.get().getOrNull(userId))
                    .filter(Objects::nonNull)
                    .map(User::getCategory)
                    .map(Category::get)
                    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

            for (final Map.Entry<Category, Long> categoryAndCount : categoryCount.entrySet()) {
                if (categoryAndCount.getValue() > categoryAndCount.getKey().getPermittedAmount()) {
                    failureMessages.add(String.format("Found %s users of category %s, only %s permitted", categoryAndCount.getValue(), categoryAndCount.getKey(), categoryAndCount.getKey().getPermittedAmount()));
                }
            }
        }

        if (failureMessages.isEmpty()) {
            return ValidationResponse.success();
        }

        return ValidationResponse.failure(team, failureMessages);
    }

    public static ValidationResponse isValidRetirement(final Team team, final int userId) {
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


    public static ValidationResponse isValidUnretirement(final Team team, final int retiredUserId) {
        final List<String> failureMessages = new ArrayList<>();

        // TODO: [zodac] I dunno... can't unretire a user twice, should retired users have a 'still_retired' flag? Seems stupid
        //   Maybe a retiredUserCache is better? With the new retiredUserId?
//        if (!UserCache.get().getRetired().containsKey(retiredUserId)) {
//            failureMessages.add(String.format("Attribute 'retiredUserId' must be a retired user", UserCache.get().getRetired().values()));
//        }

        // TODO: [zodac] If adding the user exceeds max team/category size, reject it


        // TODO: [zodac] What if I add a user that was previously retired, unretired, then retired again?
        //   With no 'still_retired' flag, it's possible to add it back to its original team by mistake


        if (failureMessages.isEmpty()) {
            return ValidationResponse.success();
        }

        return ValidationResponse.failure(team, failureMessages);
    }
}

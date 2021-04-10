package me.zodac.folding.validator;

import me.zodac.folding.api.Category;
import me.zodac.folding.api.FoldingTeam;
import me.zodac.folding.api.FoldingUser;
import me.zodac.folding.cache.FoldingUserCache;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public class FoldingTeamValidator {

    private FoldingTeamValidator() {

    }

    public static ValidationResponse isValid(final FoldingTeam foldingTeam) {
        final List<String> failureMessages = new ArrayList<>();

        if (StringUtils.isBlank(foldingTeam.getTeamName())) {
            failureMessages.add("Attribute 'teamName' must not be empty");
        }

        if (foldingTeam.getCaptainUserId() <= FoldingUser.EMPTY_USER_ID || !FoldingUserCache.get().contains(foldingTeam.getCaptainUserId())) {
            final List<String> availableUsers = FoldingUserCache.get()
                    .getAll()
                    .stream()
                    .map(foldingUser -> String.format("%s: %s", foldingUser.getId(), foldingUser.getFoldingUserName()))
                    .collect(toList());

            failureMessages.add(String.format("Attribute 'captainUserId' must be one of: %s", availableUsers));
        }

        if (!foldingTeam.getUserIds().contains(foldingTeam.getCaptainUserId())) {
            failureMessages.add(String.format("Attribute 'captainUserId' must be in the team, must be one of: %s", foldingTeam.getUserIds()));
        }

        if (foldingTeam.getUserIds().isEmpty()) {
            final List<String> availableUsers = FoldingUserCache.get()
                    .getAll()
                    .stream()
                    .map(foldingUser -> String.format("%s: %s", foldingUser.getId(), foldingUser.getFoldingUserName()))
                    .collect(toList());

            failureMessages.add(String.format("Attribute 'userIds' contain at least one of: %s", availableUsers));
        }

        final List<Integer> invalidUserIds = new ArrayList<>(foldingTeam.getUserIds().size());
        for (final int userId : foldingTeam.getUserIds()) {
            if (!FoldingUserCache.get().contains(userId)) {
                invalidUserIds.add(userId);
            }
        }

        if (!invalidUserIds.isEmpty()) {
            final List<String> availableUsers = FoldingUserCache.get()
                    .getAll()
                    .stream()
                    .map(foldingUser -> String.format("%s: %s", foldingUser.getId(), foldingUser.getFoldingUserName()))
                    .collect(toList());

            failureMessages.add(String.format("Attribute 'userIds' contains invalid IDs %s, must be: %s", invalidUserIds, availableUsers));
        }


        // No point checking the category count if the team is already invalid, perhaps none of the users are correct at this point
        if (failureMessages.isEmpty()) {
            final Map<Category, Long> categoryCount = foldingTeam.getUserIds()
                    .stream()
                    .map(userId -> FoldingUserCache.get().getOrNull(userId))
                    .filter(Objects::nonNull)
                    .map(FoldingUser::getCategory)
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

        return ValidationResponse.failure(foldingTeam, failureMessages);
    }
}

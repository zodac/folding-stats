package me.zodac.folding.rest.util.validator;

import me.zodac.folding.api.exception.FoldingException;
import me.zodac.folding.api.stats.FoldingStatsDetails;
import me.zodac.folding.api.stats.FoldingStatsRetriever;
import me.zodac.folding.api.tc.Category;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.tc.exception.NotFoundException;
import me.zodac.folding.api.tc.exception.TeamNotFoundException;
import me.zodac.folding.api.validator.ValidationResponse;
import me.zodac.folding.ejb.BusinessLogic;
import me.zodac.folding.rest.api.tc.request.UserRequest;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public final class UserValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserValidator.class);
    private static final UrlValidator URL_VALIDATOR = new UrlValidator();
    private static final int EXPECTED_PASSKEY_LENGTH = 32;

    private transient final BusinessLogic businessLogic;
    private transient final FoldingStatsRetriever foldingStatsRetriever;

    private UserValidator(final BusinessLogic businessLogic, final FoldingStatsRetriever foldingStatsRetriever) {
        this.businessLogic = businessLogic;
        this.foldingStatsRetriever = foldingStatsRetriever;
    }

    public static UserValidator create(final BusinessLogic businessLogic, final FoldingStatsRetriever foldingStatsRetriever) {
        return new UserValidator(businessLogic, foldingStatsRetriever);
    }

    public ValidationResponse<User> validate(final UserRequest userRequest) {
        if (userRequest == null) {
            return ValidationResponse.nullObject();
        }

        final Category category = Category.get(userRequest.getCategory());


        final List<String> failureMessages = validateAttributes(userRequest, category);

        final List<String> hardwareValidationFailureMessages = validateHardware(userRequest);
        failureMessages.addAll(hardwareValidationFailureMessages);

        final List<String> teamValidationFailureMessages = validateTeam(userRequest);
        failureMessages.addAll(teamValidationFailureMessages);

        // Since this is a heavy validation check, only do it if the rest of the user is valid
        if (failureMessages.isEmpty()) {
            final List<String> teamFailureMessages = validateIfUserCanBeAddedToTeam(userRequest);
            failureMessages.addAll(teamFailureMessages);
        }

        // Since this is a heavy validation check, only do it if the rest of the user is valid
        if (failureMessages.isEmpty()) {
            final List<String> userUnitsFailureMessages = validateUserUnits(userRequest);
            failureMessages.addAll(userUnitsFailureMessages);
        }

        if (failureMessages.isEmpty()) {
            try {
                final Hardware hardware = businessLogic.getHardware(userRequest.getHardwareId());
                final Team team = businessLogic.getTeam(userRequest.getTeamId());

                final User user = User.createWithoutId(userRequest.getFoldingUserName(), userRequest.getDisplayName(), userRequest.getPasskey(), category, userRequest.getProfileLink(), userRequest.getLiveStatsLink(), hardware, team, userRequest.isUserIsCaptain());
                return ValidationResponse.success(user);
            } catch (final NotFoundException e) {
                LOGGER.warn("{} with ID {} was validated successfully, but could not be retrieved", e.getType(), e.getId(), e);
                failureMessages.add(String.format("Unable to find %s with ID %s", e.getType(), e.getId()));
            } catch (final FoldingException e) {
                LOGGER.warn("Unexpected error retrieving hardware/team", e);
                failureMessages.add("Unable to retrieve hardware/team");
            }
        }

        return ValidationResponse.failure(userRequest, failureMessages);
    }

    private List<String> validateTeam(final UserRequest userRequest) {
        try {
            if (userRequest.getTeamId() <= Team.EMPTY_TEAM_ID || businessLogic.doesNotContainTeam(userRequest.getTeamId())) {
                final List<String> availableTeams = businessLogic
                        .getAllTeams()
                        .stream()
                        .map(team -> String.format("%s: %s", team.getId(), team.getTeamName()))
                        .collect(toList());

                return List.of(String.format("Attribute 'teamId' must be one of: %s", availableTeams));
            }
        } catch (final FoldingException e) {
            LOGGER.warn("Unable to get team for user {}", userRequest, e);
            return List.of("Unable to check team for user");
        }

        return Collections.emptyList();
    }

    private List<String> validateHardware(final UserRequest userRequest) {
        try {
            if (userRequest.getHardwareId() <= Hardware.EMPTY_HARDWARE_ID || businessLogic.doesNotContainHardware(userRequest.getHardwareId())) {
                final List<String> availableHardware = businessLogic
                        .getAllHardware()
                        .stream()
                        .map(hardware -> String.format("%s: %s", hardware.getId(), hardware.getHardwareName()))
                        .collect(toList());

                return List.of(String.format("Attribute 'hardwareId' must be one of: %s", availableHardware));
            }
        } catch (final FoldingException e) {
            LOGGER.warn("Unable to get hardware for user {}", userRequest, e);
            return List.of("Unable to check hardware for user");
        }

        return Collections.emptyList();
    }

    private List<String> validateAttributes(final UserRequest userRequest, final Category category) {
        final List<String> failureMessages = new ArrayList<>();

        if (Category.INVALID == category) {
            failureMessages.add(String.format("Attribute 'category' must be one of: %s", Category.getAllValues()));
        }

        if (StringUtils.isBlank(userRequest.getFoldingUserName())) {
            failureMessages.add("Attribute 'foldingUserName' must not be empty");
        }

        if (StringUtils.isBlank(userRequest.getDisplayName())) {
            failureMessages.add("Attribute 'displayName' must not be empty");
        }

        if (StringUtils.isBlank(userRequest.getPasskey())) {
            failureMessages.add("Attribute 'passkey' must not be empty");
        }

        if (userRequest.getPasskey().contains("*")) {
            failureMessages.add("Attribute 'passkey' cannot contain '*' characters");
        }

        if (userRequest.getPasskey().length() != EXPECTED_PASSKEY_LENGTH) {
            failureMessages.add("Attribute 'passkey' must be 32 characters in length");
        }

        if (StringUtils.isNotEmpty(userRequest.getProfileLink()) && !URL_VALIDATOR.isValid(userRequest.getProfileLink())) {
            failureMessages.add(String.format("Attribute 'profileLink' is not a valid link: '%s'", userRequest.getProfileLink()));

        }

        if (StringUtils.isNotEmpty(userRequest.getLiveStatsLink()) && !URL_VALIDATOR.isValid(userRequest.getLiveStatsLink())) {
            failureMessages.add(String.format("Attribute 'liveStatsLink' is not a valid link: '%s'", userRequest.getLiveStatsLink()));
        }
        return failureMessages;
    }

    private List<String> validateUserUnits(final UserRequest userRequest) {
        try {
            final FoldingStatsDetails foldingStatsDetails = FoldingStatsDetails.create(userRequest.getFoldingUserName(), userRequest.getPasskey());
            final int unitsForUserAndPasskey = foldingStatsRetriever.getUnits(foldingStatsDetails);

            if (unitsForUserAndPasskey == 0) {
                return List.of(String.format("User '%s' has 0 completed Work Units with passkey '%s', there must be at least one valid Work Unit submitted on the passkey before adding the user",
                        userRequest.getFoldingUserName(),
                        userRequest.getPasskey()
                ));
            }
        } catch (final Exception e) {
            LOGGER.warn("Unable to get Folding stats for user {}", userRequest, e);
            return List.of("Unable to check stats for user");
        }

        return Collections.emptyList();
    }

    private List<String> validateIfUserCanBeAddedToTeam(final UserRequest userRequest) {
        final List<String> failureMessages = new ArrayList<>();

        try {
            final Team team = businessLogic.getTeam(userRequest.getTeamId());
            final Collection<User> usersOnTeam = businessLogic.getUsersOnTeam(team);

            if (usersOnTeam.size() >= Category.maximumPermittedAmountForAllCategories()) {
                failureMessages.add(String.format("Team '%s' has %s users, maximum permitted is %s", team.getTeamName(), usersOnTeam.size(), Category.maximumPermittedAmountForAllCategories()));
            }

            if (userRequest.isUserIsCaptain()) {
                for (final User existingUserOnTeam : usersOnTeam) {
                    if (existingUserOnTeam.isUserIsCaptain()) {
                        failureMessages.add(String.format("Team '%s' already has a captain (%s), cannot have multiple captains", team.getTeamName(), userRequest.getDisplayName()));
                    }
                }
            }

            final Map<Category, Long> categoryCount = usersOnTeam
                    .stream()
                    .map(User::getCategory)
                    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

            for (final Map.Entry<Category, Long> categoryAndCount : categoryCount.entrySet()) {
                if (categoryAndCount.getValue() > categoryAndCount.getKey().permittedAmount()) {
                    failureMessages.add(String.format("Found %s users of category %s, only %s permitted", categoryAndCount.getValue(), categoryAndCount.getKey().displayName(), categoryAndCount.getKey().permittedAmount()));
                }
            }
        } catch (final FoldingException | TeamNotFoundException e) {
            LOGGER.warn("Unable to validate current team users", e);
            failureMessages.add("Unable to validate current team users");
        }

        return failureMessages;
    }
}

package me.zodac.folding.rest.validator;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import me.zodac.folding.api.ejb.BusinessLogic;
import me.zodac.folding.api.stats.FoldingStatsDetails;
import me.zodac.folding.api.stats.FoldingStatsRetriever;
import me.zodac.folding.api.tc.Category;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.validator.ValidationResponse;
import me.zodac.folding.rest.api.tc.request.UserRequest;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// TODO: [zodac] In severe need of a clean up. Write tests for the validators first though, because you're a moron
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class UserValidator {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final UrlValidator URL_VALIDATOR = new UrlValidator();
    private static final int EXPECTED_PASSKEY_LENGTH = 32;

    private final BusinessLogic businessLogic;
    private final FoldingStatsRetriever foldingStatsRetriever;

    public static UserValidator createValidator(final BusinessLogic businessLogic, final FoldingStatsRetriever foldingStatsRetriever) {
        return new UserValidator(businessLogic, foldingStatsRetriever);
    }

    @SuppressWarnings("PMD.NPathComplexity") // Better than breaking into smaller functions
    public ValidationResponse<User> validateCreate(final UserRequest userRequest) {
        if (userRequest == null) {
            return ValidationResponse.nullObject();
        }

        final List<String> failureMessages = new ArrayList<>();

        final Category category = Category.get(userRequest.getCategory());
        if (Category.INVALID == category) {
            failureMessages.add(String.format("Field 'category' must be one of: %s", Category.getAllValues()));
        }

        if (StringUtils.isBlank(userRequest.getFoldingUserName())) {
            failureMessages.add("Field 'foldingUserName' must not be empty");
        }

        if (StringUtils.isBlank(userRequest.getPasskey())) {
            failureMessages.add("Field 'passkey' must not be empty");
        }

        // If foldingUserName and passkey are valid, ensure they don't already exist
        if (failureMessages.isEmpty()) {
            final Optional<User> userWithMatchingFoldingUserNameAndPasskey =
                businessLogic.getUserWithFoldingUserNameAndPasskey(userRequest.getFoldingUserName(), userRequest.getPasskey());

            if (userWithMatchingFoldingUserNameAndPasskey.isPresent()) {
                return ValidationResponse
                    .conflictingWith(userRequest, userWithMatchingFoldingUserNameAndPasskey.get(), List.of("foldingUserName", "passkey"));
            }
        }

        if (StringUtils.isBlank(userRequest.getDisplayName())) {
            failureMessages.add("Field 'displayName' must not be empty");
        }

        if (userRequest.getPasskey().contains("*")) {
            failureMessages.add("Field 'passkey' cannot contain '*' characters");
        }

        if (userRequest.getPasskey().length() != EXPECTED_PASSKEY_LENGTH) {
            failureMessages.add("Field 'passkey' must be 32 characters in length");
        }

        if (StringUtils.isNotEmpty(userRequest.getProfileLink()) && !URL_VALIDATOR.isValid(userRequest.getProfileLink())) {
            failureMessages.add(String.format("Field 'profileLink' is not a valid link: '%s'", userRequest.getProfileLink()));

        }

        if (StringUtils.isNotEmpty(userRequest.getLiveStatsLink()) && !URL_VALIDATOR.isValid(userRequest.getLiveStatsLink())) {
            failureMessages.add(String.format("Field 'liveStatsLink' is not a valid link: '%s'", userRequest.getLiveStatsLink()));
        }

        final List<String> hardwareValidationFailureMessages = validateHardware(userRequest);
        failureMessages.addAll(hardwareValidationFailureMessages);

        final List<String> teamValidationFailureMessages = validateTeam(userRequest);
        failureMessages.addAll(teamValidationFailureMessages);

        // Since this is a heavy validation check, only do it if the rest of the user is valid
        if (failureMessages.isEmpty()) {
            final List<String> teamFailureMessages = validateIfUserCanBeAddedToTeam(userRequest, category, null);
            failureMessages.addAll(teamFailureMessages);
        }

        // Since this is a heavy validation check, only do it if the rest of the user is valid
        if (failureMessages.isEmpty()) {
            final List<String> userUnitsFailureMessages = validateUserUnits(userRequest);
            failureMessages.addAll(userUnitsFailureMessages);
        }

        if (failureMessages.isEmpty()) {
            final Optional<Hardware> hardware = businessLogic.getHardware(userRequest.getHardwareId());
            final Optional<Team> team = businessLogic.getTeam(userRequest.getTeamId());

            if (hardware.isEmpty()) {
                failureMessages.add(String.format("Unable to retrieve hardware with ID '%s'", userRequest.getHardwareId()));
            } else if (team.isEmpty()) {
                failureMessages.add(String.format("Unable to retrieve team with ID '%s'", userRequest.getTeamId()));
            } else {
                final User user =
                    User.createWithoutId(userRequest.getFoldingUserName(), userRequest.getDisplayName(), userRequest.getPasskey(), category,
                        userRequest.getProfileLink(), userRequest.getLiveStatsLink(), hardware.get(), team.get(), userRequest.isUserIsCaptain());
                return ValidationResponse.success(user);
            }
        }

        return ValidationResponse.failure(userRequest, failureMessages);
    }

    @SuppressWarnings("PMD.NPathComplexity") // Better than breaking into smaller functions
    public ValidationResponse<User> validateUpdate(final UserRequest userRequest, final User existingUser) {
        if (userRequest == null) {
            return ValidationResponse.nullObject();
        }

        final List<String> failureMessages = new ArrayList<>();

        final Category category = Category.get(userRequest.getCategory());
        if (Category.INVALID == category) {
            failureMessages.add(String.format("Field 'category' must be one of: %s", Category.getAllValues()));
        }

        if (StringUtils.isBlank(userRequest.getFoldingUserName())) {
            failureMessages.add("Field 'foldingUserName' must not be empty");
        }

        if (StringUtils.isBlank(userRequest.getPasskey())) {
            failureMessages.add("Field 'passkey' must not be empty");
        }

        if (StringUtils.isBlank(userRequest.getDisplayName())) {
            failureMessages.add("Field 'displayName' must not be empty");
        }

        if (userRequest.getPasskey().contains("*")) {
            failureMessages.add("Field 'passkey' cannot contain '*' characters");
        }

        if (userRequest.getPasskey().length() != EXPECTED_PASSKEY_LENGTH) {
            failureMessages.add("Field 'passkey' must be 32 characters in length");
        }

        if (StringUtils.isNotEmpty(userRequest.getProfileLink()) && !URL_VALIDATOR.isValid(userRequest.getProfileLink())) {
            failureMessages.add(String.format("Field 'profileLink' is not a valid link: '%s'", userRequest.getProfileLink()));
        }

        if (StringUtils.isNotEmpty(userRequest.getLiveStatsLink()) && !URL_VALIDATOR.isValid(userRequest.getLiveStatsLink())) {
            failureMessages.add(String.format("Field 'liveStatsLink' is not a valid link: '%s'", userRequest.getLiveStatsLink()));
        }

        final List<String> hardwareValidationFailureMessages = validateHardware(userRequest);
        failureMessages.addAll(hardwareValidationFailureMessages);

        final List<String> teamValidationFailureMessages = validateTeam(userRequest);
        failureMessages.addAll(teamValidationFailureMessages);

        // Since this is a heavy validation check, only do it if the rest of the user is valid
        if (failureMessages.isEmpty()) {
            final List<String> teamFailureMessages = validateIfUserCanBeAddedToTeam(userRequest, category, existingUser);
            failureMessages.addAll(teamFailureMessages);
        }

        // Since this is a heavy validation check, only do it if the rest of the user is valid
        if (failureMessages.isEmpty()) {
            final List<String> userUnitsFailureMessages = validateUserUnits(userRequest);
            failureMessages.addAll(userUnitsFailureMessages);
        }

        if (failureMessages.isEmpty()) {
            final Optional<Hardware> hardware = businessLogic.getHardware(userRequest.getHardwareId());
            final Optional<Team> team = businessLogic.getTeam(userRequest.getTeamId());

            if (hardware.isEmpty()) {
                failureMessages.add(String.format("Unable to retrieve hardware with ID '%s'", userRequest.getHardwareId()));
            } else if (team.isEmpty()) {
                failureMessages.add(String.format("Unable to retrieve team with ID '%s'", userRequest.getTeamId()));
            } else {
                final User user =
                    User.createWithoutId(userRequest.getFoldingUserName(), userRequest.getDisplayName(), userRequest.getPasskey(), category,
                        userRequest.getProfileLink(), userRequest.getLiveStatsLink(), hardware.get(), team.get(), userRequest.isUserIsCaptain());
                return ValidationResponse.success(user);
            }
        }

        return ValidationResponse.failure(userRequest, failureMessages);
    }

    private List<String> validateTeam(final UserRequest userRequest) {
        if (userRequest.getTeamId() <= Team.EMPTY_TEAM_ID || businessLogic.getTeam(userRequest.getTeamId()).isEmpty()) {
            final List<String> availableTeams = businessLogic
                .getAllTeams()
                .stream()
                .map(team -> String.format("%s: %s", team.getId(), team.getTeamName()))
                .collect(toList());

            return List.of(String.format("Field 'teamId' must be one of: %s", availableTeams));
        }

        return Collections.emptyList();
    }

    private List<String> validateHardware(final UserRequest userRequest) {
        if (userRequest.getHardwareId() <= Hardware.EMPTY_HARDWARE_ID || businessLogic.getHardware(userRequest.getHardwareId()).isEmpty()) {
            final List<String> availableHardware = businessLogic
                .getAllHardware()
                .stream()
                .map(hardware -> String.format("%s: %s", hardware.getId(), hardware.getHardwareName()))
                .collect(toList());

            return List.of(String.format("Field 'hardwareId' must be one of: %s", availableHardware));
        }

        return Collections.emptyList();
    }

    private List<String> validateUserUnits(final UserRequest userRequest) {
        try {
            final FoldingStatsDetails foldingStatsDetails = FoldingStatsDetails.create(userRequest.getFoldingUserName(), userRequest.getPasskey());
            final int unitsForUserAndPasskey = foldingStatsRetriever.getUnits(foldingStatsDetails);

            if (unitsForUserAndPasskey == 0) {
                return List.of(String.format(
                    "User '%s' has 0 completed Work Units with passkey '%s', there must be at least one valid Work Unit submitted on the passkey before adding the user",
                    userRequest.getFoldingUserName(),
                    userRequest.getPasskey()
                ));
            }
        } catch (final Exception e) { // TODO: [zodac] Handle ExternalConnectionException here, otherwise will return 400 response instead of 502
            LOGGER.warn("Unable to get Folding stats for user {}", userRequest, e);
            return List.of("Unable to check stats for user");
        }

        return Collections.emptyList();
    }

    private List<String> validateIfUserCanBeAddedToTeam(final UserRequest userRequest, final Category category, final User existingUser) {
        final Optional<Team> team = businessLogic.getTeam(userRequest.getTeamId());

        if (team.isEmpty()) {
            return List.of(String.format("Unable to retrieve team with ID '%s'", userRequest.getTeamId()));
        }

        final List<String> failureMessages = new ArrayList<>(4);
        final Collection<User> usersOnTeam = businessLogic.getUsersOnTeam(team.get());

        if (userRequest.isUserIsCaptain()) {
            for (final User existingUserOnTeam : usersOnTeam) {
                if (existingUserOnTeam.isUserIsCaptain()) {
                    failureMessages.add(String.format("Team '%s' already has a captain (%s), cannot have multiple captains", team.get().getTeamName(),
                        userRequest.getDisplayName()));
                }
            }
        }

        // What the hell, man? Who approved this shit?
        if (existingUser == null) { // Create
            if (usersOnTeam.size() >= Category.maximumPermittedAmountForAllCategories()) {
                failureMessages.add(String.format("Team '%s' has %s users, maximum permitted is %s", team.get().getTeamName(), usersOnTeam.size(),
                    Category.maximumPermittedAmountForAllCategories()));
            }

            final int permittedNumberForCategory = category.permittedUsers();
            final int numberOfUsersInTeamWithCategory = (int) usersOnTeam
                .stream()
                .filter(user -> user.getCategory() == category)
                .count();

            if (numberOfUsersInTeamWithCategory >= permittedNumberForCategory) {
                failureMessages.add(String.format("Found %s users of category '%s', only %s permitted", numberOfUsersInTeamWithCategory, category,
                    permittedNumberForCategory));
            }
        } else {
            // isUpdate
            if (category != existingUser.getCategory()) {
                final int permittedNumberForCategory = category.permittedUsers();
                final int numberOfUsersInTeamWithCategory = (int) usersOnTeam
                    .stream()
                    .filter(user -> user.getId() != existingUser.getId() && user.getCategory() == category)
                    .count();

                if (numberOfUsersInTeamWithCategory >= permittedNumberForCategory) {
                    failureMessages.add(String
                        .format("Found %s users of category '%s', only %s permitted", numberOfUsersInTeamWithCategory, category,
                            permittedNumberForCategory));
                }
            }
        }

        return failureMessages;
    }
}

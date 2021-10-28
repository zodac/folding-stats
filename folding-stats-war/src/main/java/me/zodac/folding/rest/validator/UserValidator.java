package me.zodac.folding.rest.validator;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import me.zodac.folding.api.stats.FoldingStatsDetails;
import me.zodac.folding.api.stats.FoldingStatsRetriever;
import me.zodac.folding.api.tc.Category;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.tc.stats.Stats;
import me.zodac.folding.rest.api.tc.request.UserRequest;
import me.zodac.folding.stats.HttpFoldingStatsRetriever;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;

/**
 * Validator class to validate a {@link User} or {@link UserRequest}.
 */
// TODO: [zodac] In severe need of a clean up
// TODO: [zodac] Validate the linked hardware matches the user's category
// TODO: [zodac] Update passkey check to a regex of 32 alpha-numeric characters
public final class UserValidator {

    private static final UrlValidator URL_VALIDATOR = new UrlValidator();
    private static final int EXPECTED_PASSKEY_LENGTH = 32;

    private final FoldingStatsRetriever foldingStatsRetriever;

    private UserValidator(final FoldingStatsRetriever foldingStatsRetriever) {
        this.foldingStatsRetriever = foldingStatsRetriever;
    }

    /**
     * Create an instance of {@link UserValidator}.
     *
     * @param foldingStatsRetriever the {@link FoldingStatsRetriever} to verify the user stats
     * @return the created {@link UserValidator}
     */
    public static UserValidator createWithFoldingStatsRetriever(final FoldingStatsRetriever foldingStatsRetriever) {
        return new UserValidator(foldingStatsRetriever);
    }

    /**
     * Create an instance of {@link UserValidator}.
     *
     * <p>
     * Uses an instance of {@link HttpFoldingStatsRetriever}.
     *
     * @return the created {@link UserValidator}
     */
    public static UserValidator create() {
        return new UserValidator(HttpFoldingStatsRetriever.create());
    }

    /**
     * Validates a {@link UserRequest} for a {@link User} to be created on the system.
     *
     * <p>
     * Validation checks include:
     * TODO:
     *
     * @param userRequest the {@link UserRequest} to validate
     * @param allUsers    all existing {@link User}s in the system
     * @param allHardware all existing {@link Hardware}s in the system
     * @param allTeams    all existing {@link Team}s in the system
     * @return the {@link ValidationResult}
     */
    @SuppressWarnings("PMD.NPathComplexity") // Better than breaking into smaller functions
    public ValidationResult<User> validateCreate(final UserRequest userRequest,
                                                 final Collection<User> allUsers,
                                                 final Collection<Hardware> allHardware,
                                                 final Collection<Team> allTeams) {
        if (userRequest == null) {
            return ValidationResult.nullObject();
        }

        final List<String> failureMessages = new ArrayList<>();

        if (StringUtils.isBlank(userRequest.getFoldingUserName())) {
            failureMessages.add("Field 'foldingUserName' must not be empty");
        }

        if (StringUtils.isBlank(userRequest.getPasskey())) {
            failureMessages.add("Field 'passkey' must not be empty");
        } else {
            if (userRequest.getPasskey().length() != EXPECTED_PASSKEY_LENGTH) {
                failureMessages.add(String.format("Field 'passkey' must be %d characters in length", EXPECTED_PASSKEY_LENGTH));
            }

            if (userRequest.getPasskey().contains("*")) {
                failureMessages.add("Field 'passkey' cannot contain '*' characters");
            }
        }

        // If foldingUserName and passkey are valid, ensure they don't already exist
        if (failureMessages.isEmpty()) {
            final Optional<User> matchingUser = getUserWithFoldingUserNameAndPasskey(userRequest, allUsers);

            if (matchingUser.isPresent()) {
                return ValidationResult
                    .conflictingWith(userRequest, matchingUser.get(), List.of("foldingUserName", "passkey"));
            }
        }

        if (StringUtils.isBlank(userRequest.getDisplayName())) {
            failureMessages.add("Field 'displayName' must not be empty");
        }

        final Category category = Category.get(userRequest.getCategory());
        if (Category.INVALID == category) {
            failureMessages.add(String.format("Field 'category' must be one of: %s", Category.getAllValues()));
        }

        if (StringUtils.isNotEmpty(userRequest.getProfileLink()) && !URL_VALIDATOR.isValid(userRequest.getProfileLink())) {
            failureMessages.add(String.format("Field 'profileLink' is not a valid link: '%s'", userRequest.getProfileLink()));

        }

        if (StringUtils.isNotEmpty(userRequest.getLiveStatsLink()) && !URL_VALIDATOR.isValid(userRequest.getLiveStatsLink())) {
            failureMessages.add(String.format("Field 'liveStatsLink' is not a valid link: '%s'", userRequest.getLiveStatsLink()));
        }

        final List<String> hardwareValidationFailureMessages = validateHardware(userRequest, allHardware);
        failureMessages.addAll(hardwareValidationFailureMessages);

        final List<String> teamValidationFailureMessages = validateTeam(userRequest, allTeams);
        failureMessages.addAll(teamValidationFailureMessages);

        // Since this is a heavy validation check, only do it if the rest of the user is valid
        if (failureMessages.isEmpty()) {
            final List<String> teamFailureMessages = validateIfUserCanBeAddedToTeam(userRequest, category, null, allUsers, allTeams);
            failureMessages.addAll(teamFailureMessages);
        }

        // Since this is a heavy validation check, only do it if the rest of the user is valid
        if (failureMessages.isEmpty()) {
            final List<String> userUnitsFailureMessages = validateUserUnits(userRequest);
            failureMessages.addAll(userUnitsFailureMessages);
        }

        if (failureMessages.isEmpty()) {
            // TODO: [zodac] Would have been retrieved earlier, find a way to reuse it
            final Optional<Hardware> hardware = getHardware(userRequest.getHardwareId(), allHardware);
            final Optional<Team> team = getTeam(userRequest.getTeamId(), allTeams);

            if (hardware.isEmpty()) {
                failureMessages.add(String.format("Unable to retrieve hardware with ID '%s'", userRequest.getHardwareId()));
            } else if (team.isEmpty()) {
                failureMessages.add(String.format("Unable to retrieve team with ID '%s'", userRequest.getTeamId()));
            } else {
                final User user =
                    User.createWithoutId(userRequest.getFoldingUserName(), userRequest.getDisplayName(), userRequest.getPasskey(), category,
                        userRequest.getProfileLink(), userRequest.getLiveStatsLink(), hardware.get(), team.get(), userRequest.isUserIsCaptain());
                return ValidationResult.successful(user);
            }
        }

        return ValidationResult.failure(userRequest, failureMessages);
    }

    /**
     * Validates a {@link UserRequest} for a {@link User} to be updated on the system.
     *
     * <p>
     * Validation checks include:
     * TODO:
     *
     * @param userRequest  the {@link UserRequest} to validate
     * @param existingUser the already existing {@link User} in the system to be updated
     * @param allUsers     all existing {@link User}s in the system
     * @param allHardware  all existing {@link Hardware}s in the system
     * @param allTeams     all existing {@link Team}s in the system
     * @return the {@link ValidationResult}
     */
    @SuppressWarnings("PMD.NPathComplexity") // Better than breaking into smaller functions
    public ValidationResult<User> validateUpdate(final UserRequest userRequest,
                                                 final User existingUser,
                                                 final Collection<User> allUsers,
                                                 final Collection<Hardware> allHardware,
                                                 final Collection<Team> allTeams) {
        if (userRequest == null) {
            return ValidationResult.nullObject();
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

        final List<String> hardwareValidationFailureMessages = validateHardware(userRequest, allHardware);
        failureMessages.addAll(hardwareValidationFailureMessages);

        final List<String> teamValidationFailureMessages = validateTeam(userRequest, allTeams);
        failureMessages.addAll(teamValidationFailureMessages);

        // Since this is a heavy validation check, only do it if the rest of the user is valid
        if (failureMessages.isEmpty()) {
            final List<String> teamFailureMessages = validateIfUserCanBeAddedToTeam(userRequest, category, existingUser, allUsers, allTeams);
            failureMessages.addAll(teamFailureMessages);
        }

        // Since this is a heavy validation check, only do it if the rest of the user is valid
        if (failureMessages.isEmpty()) {
            final List<String> userUnitsFailureMessages = validateUserUnits(userRequest);
            failureMessages.addAll(userUnitsFailureMessages);
        }

        if (failureMessages.isEmpty()) {
            final Optional<Hardware> hardware = getHardware(userRequest.getHardwareId(), allHardware);
            final Optional<Team> team = getTeam(userRequest.getTeamId(), allTeams);

            if (hardware.isEmpty()) {
                failureMessages.add(String.format("Unable to retrieve hardware with ID '%s'", userRequest.getHardwareId()));
            } else if (team.isEmpty()) {
                failureMessages.add(String.format("Unable to retrieve team with ID '%s'", userRequest.getTeamId()));
            } else {
                final User user =
                    User.createWithoutId(userRequest.getFoldingUserName(), userRequest.getDisplayName(), userRequest.getPasskey(), category,
                        userRequest.getProfileLink(), userRequest.getLiveStatsLink(), hardware.get(), team.get(), userRequest.isUserIsCaptain());
                return ValidationResult.successful(user);
            }
        }

        return ValidationResult.failure(userRequest, failureMessages);
    }

    private static List<String> validateTeam(final UserRequest userRequest, final Collection<Team> allTeams) {
        if (userRequest.getTeamId() <= Team.EMPTY_TEAM_ID || getTeam(userRequest.getTeamId(), allTeams).isEmpty()) {
            final List<String> availableTeams = allTeams
                .stream()
                .map(team -> String.format("%s: %s", team.getId(), team.getTeamName()))
                .collect(toList());

            return List.of(String.format("Field 'teamId' must be one of: %s", availableTeams));
        }

        return Collections.emptyList();
    }

    private static List<String> validateHardware(final UserRequest userRequest, final Collection<Hardware> allHardware) {
        if (userRequest.getHardwareId() <= Hardware.EMPTY_HARDWARE_ID || getHardware(userRequest.getHardwareId(), allHardware).isEmpty()) {
            final List<String> availableHardware = allHardware
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
            final Stats statsForUserAndPasskey = foldingStatsRetriever.getStats(foldingStatsDetails);

            if (statsForUserAndPasskey.getUnits() == 0) {
                return List.of(String.format(
                    "User '%s' has 0 Work Units with passkey '%s', there must be at least one completed Work Unit before adding the user",
                    userRequest.getFoldingUserName(), userRequest.getPasskey()
                ));
            }
        } catch (final Exception e) { // TODO: [zodac] Handle ExternalConnectionException here, otherwise will return 400 response instead of 502
            return List.of(String.format("Unable to check stats for user '%s': %s", userRequest.getDisplayName(), e.getMessage()));
        }

        return Collections.emptyList();
    }

    private static List<String> validateIfUserCanBeAddedToTeam(final UserRequest userRequest,
                                                               final Category category,
                                                               final User existingUser,
                                                               final Collection<User> allUsers,
                                                               final Collection<Team> allTeams) {
        // TODO: [zodac] We should have this team already
        final Optional<Team> team = getTeam(userRequest.getTeamId(), allTeams);
        if (team.isEmpty()) {
            return List.of(String.format("Unable to retrieve team with ID '%s'", userRequest.getTeamId()));
        }

        final List<String> failureMessages = new ArrayList<>(4);
        final Collection<User> usersOnTeam = getUsersOnTeam(team.get().getId(), allUsers);

        if (userRequest.isUserIsCaptain()) {
            for (final User existingUserOnTeam : usersOnTeam) {
                if (existingUserOnTeam.isUserIsCaptain()) {
                    failureMessages.add(String.format("Team '%s' already has a captain '%s', cannot have multiple captains", team.get().getTeamName(),
                        existingUserOnTeam.getDisplayName()));
                }
            }
        }

        // What the hell, man? Who approved this?
        if (existingUser == null) { // Create
            if (usersOnTeam.size() == Category.maximumPermittedAmountForAllCategories()) {
                failureMessages.add(String.format("Team '%s' has %s users, maximum permitted is %s", team.get().getTeamName(), usersOnTeam.size(),
                    Category.maximumPermittedAmountForAllCategories()));
            }

            final int permittedNumberForCategory = category.permittedUsers();
            final long numberOfUsersInTeamWithCategory = usersOnTeam
                .stream()
                .filter(user -> user.getCategory() == category)
                .count();

            if (numberOfUsersInTeamWithCategory == permittedNumberForCategory) {
                failureMessages.add(String.format("Team '%s' already has %s users in category '%s', only %s permitted", team.get().getTeamName(),
                    numberOfUsersInTeamWithCategory, category, permittedNumberForCategory));
            }
        } else {
            // isUpdate
            if (category != existingUser.getCategory()) {
                final int permittedNumberForCategory = category.permittedUsers();
                final long numberOfUsersInTeamWithCategory = usersOnTeam
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

    private static Optional<User> getUserWithFoldingUserNameAndPasskey(final UserRequest userRequest, final Collection<User> allUsers) {
        final String foldingUserName = userRequest.getFoldingUserName();
        final String passkey = userRequest.getPasskey();

        if (StringUtils.isAnyBlank(foldingUserName, passkey)) {
            return Optional.empty();
        }

        return allUsers
            .stream()
            .filter(user -> user.getFoldingUserName().equalsIgnoreCase(foldingUserName) && user.getPasskey().equalsIgnoreCase(passkey))
            .findAny();
    }

    private static Collection<User> getUsersOnTeam(final int teamId, final Collection<User> allUsers) {
        if (teamId == Team.EMPTY_TEAM_ID) {
            return Collections.emptyList();
        }

        return allUsers
            .stream()
            .filter(user -> user.getTeam().getId() == teamId)
            .collect(toList());
    }

    private static Optional<Hardware> getHardware(final int hardwareId, final Collection<Hardware> allHardware) {
        return allHardware
            .stream()
            .filter(hardware -> hardware.getId() == hardwareId)
            .findAny();
    }

    private static Optional<Team> getTeam(final int teamId, final Collection<Team> allTeams) {
        return allTeams
            .stream()
            .filter(team -> team.getId() == teamId)
            .findAny();
    }
}

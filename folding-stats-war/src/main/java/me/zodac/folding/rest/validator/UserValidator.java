package me.zodac.folding.rest.validator;

import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import me.zodac.folding.api.exception.ExternalConnectionException;
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
// TODO: [zodac] Validate the linked hardware matches the user's category
// TODO: [zodac] Update passkey check to a regex of 32 alpha-numeric characters
// TODO: [zodac] When validating update, only check units if foldingUserName or passkey has changed
public final class UserValidator {

    private static final UrlValidator URL_VALIDATOR = new UrlValidator();
    private static final int EXPECTED_PASSKEY_LENGTH = 32;

    private final FoldingStatsRetriever foldingStatsRetriever;

    // These fields will be set during validation for ease of re-use
    private Hardware hardwareForUser;
    private Team teamForUser;

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
     * <ul>
     *     <li>Input {@code userRequest} must not be <b>null</b></li>
     *     <li>Field 'foldingUserName' must not be empty</li>
     *     <li>Field 'displayName' must not be empty</li>
     *     <li>Field 'passkey' must not be empty, must be 32-characters long, and must only include alphanumeric characters</li>
     *     <li>If fields 'foldingUserName' and 'passkey' are valid, they must not be used by another {@link User}</li>
     *     <li>Field 'category' must be a valid {@link Category}</li>
     *     <li>If field 'profileLink' is not empty, it must be a valid URL</li>
     *     <li>If field 'liveStatsLink' is not empty, it must be a valid URL</li>
     *     <li>Field 'hardwareId' must match an existing {@link Hardware}</li>
     *     <li>Field 'teamId' must match an existing {@link Team}</li>
     *     <li>The {@link User} must not cause its {@link Team} to exceed the {@link Category#maximumPermittedAmountForAllCategories()}</li>
     *     <li>The {@link User} must not cause its {@link Category} to exceed the {@link Category#permittedUsers()}</li>
     *     <li>The {@link User} may only be captain if no other {@link User} in the team is already captain</li>
     *     <li>The 'foldingUserName' and 'passkey' combination has at least 1 Work Unit successfully completed</li>
     * </ul>
     *
     * @param userRequest the {@link UserRequest} to validate
     * @param allUsers    all existing {@link User}s in the system
     * @param allHardware all existing {@link Hardware}s in the system
     * @param allTeams    all existing {@link Team}s in the system
     * @return the {@link ValidationResult}
     */
    public ValidationResult<User> validateCreate(final UserRequest userRequest,
                                                 final Collection<User> allUsers,
                                                 final Collection<Hardware> allHardware,
                                                 final Collection<Team> allTeams) {
        if (userRequest == null) {
            return ValidationResult.nullObject();
        }

        // The foldingUserName and passkey must be unique
        final Optional<User> matchingUser = getUserWithFoldingUserNameAndPasskey(userRequest, allUsers);
        if (matchingUser.isPresent()) {
            return ValidationResult.conflictingWith(userRequest, matchingUser.get(), List.of("foldingUserName", "passkey"));
        }

        final List<String> failureMessages = Stream.of(
                foldingUserName(userRequest),
                displayName(userRequest),
                passkey(userRequest),
                category(userRequest),
                profileLink(userRequest),
                liveStatsLink(userRequest),
                hardware(userRequest, allHardware),
                team(userRequest, allTeams)
            )
            .filter(Objects::nonNull)
            .collect(toList());

        // All subsequent checks will be heavier, so best to return early if any failures occur
        if (!failureMessages.isEmpty()) {
            return ValidationResult.failure(userRequest, failureMessages);
        }

        final Collection<User> usersOnTeam = getUsersOnTeam(teamForUser.getId(), allUsers);
        final Category category = Category.get(userRequest.getCategory());

        final List<String> complexFailureMessages = Stream.of(
                validateNewUserDoesNotExceedTeamLimits(usersOnTeam, category),
                validateNewUserCanBeCaptain(userRequest, usersOnTeam),
                validateUserUnits(userRequest)
            )
            .filter(Objects::nonNull)
            .collect(toList());

        if (!complexFailureMessages.isEmpty()) {
            return ValidationResult.failure(userRequest, complexFailureMessages);
        }

        return ValidationResult.successful(User.createWithoutId(userRequest, hardwareForUser, teamForUser));
    }

    /**
     * Validates a {@link UserRequest} for a {@link User} to be updated on the system.
     *
     * <p>
     * Validation checks include:
     * <ul>
     *     <li>Input {@code userRequest} and {@code existingUser} must not be <b>null</b></li>
     *     <li>Field 'foldingUserName' must not be empty</li>
     *     <li>Field 'displayName' must not be empty</li>
     *     <li>Field 'passkey' must not be empty, must be 32-characters long, and must only include alphanumeric characters</li>
     *     <li>If fields 'foldingUserName' and 'passkey' are valid, they must not be used by another {@link User}, unless it is the {@link User} being
     *     updated</li>
     *     <li>Field 'category' must be a valid {@link Category}</li>
     *     <li>If field 'profileLink' is not empty, it must be a valid URL</li>
     *     <li>If field 'liveStatsLink' is not empty, it must be a valid URL</li>
     *     <li>Field 'hardwareId' must match an existing {@link Hardware}</li>
     *     <li>Field 'teamId' must match an existing {@link Team}</li>
     *     <li>If the {@link User} is updating its {@link Team}, it must not cause its {@link Team} to exceed the
     *     {@link Category#maximumPermittedAmountForAllCategories()}</li>
     *     <li>If the {@link User} is updating its {@link Category} must not cause its {@link Category} to exceed the
     *     {@link Category#permittedUsers()}</li>
     *     <li>The {@link User} may only be captain if no other {@link User} in the team is already captain</li>
     *     <li>The 'foldingUserName' and 'passkey' combination has at least 1 Work Unit successfully completed</li>
     * </ul>
     *
     * @param userRequest  the {@link UserRequest} to validate
     * @param existingUser the already existing {@link User} in the system to be updated
     * @param allUsers     all existing {@link User}s in the system
     * @param allHardware  all existing {@link Hardware}s in the system
     * @param allTeams     all existing {@link Team}s in the system
     * @return the {@link ValidationResult}
     */
    public ValidationResult<User> validateUpdate(final UserRequest userRequest,
                                                 final User existingUser,
                                                 final Collection<User> allUsers,
                                                 final Collection<Hardware> allHardware,
                                                 final Collection<Team> allTeams) {
        if (userRequest == null || existingUser == null) {
            return ValidationResult.nullObject();
        }

        // The foldingUserName and passkey must be unique, unless replacing the same user
        final Optional<User> matchingUser = getUserWithFoldingUserNameAndPasskey(userRequest, allUsers);
        if (matchingUser.isPresent() && matchingUser.get().getId() != existingUser.getId()) {
            return ValidationResult.conflictingWith(userRequest, matchingUser.get(), List.of("foldingUserName", "passkey"));
        }

        final List<String> failureMessages = Stream.of(
                foldingUserName(userRequest),
                displayName(userRequest),
                passkey(userRequest),
                category(userRequest),
                profileLink(userRequest),
                liveStatsLink(userRequest),
                hardware(userRequest, allHardware),
                team(userRequest, allTeams)
            )
            .filter(Objects::nonNull)
            .collect(toList());

        // All subsequent checks will be heavier, so best to return early if any failures occur
        if (!failureMessages.isEmpty()) {
            return ValidationResult.failure(userRequest, failureMessages);
        }

        final Collection<User> usersOnTeam = getUsersOnTeam(teamForUser.getId(), allUsers);
        final Category category = Category.get(userRequest.getCategory());

        final List<String> complexFailureMessages = Stream.of(
                validateUpdatedUserDoesNotExceedTeamLimits(userRequest, existingUser, usersOnTeam, category),
                validateUpdatedUserCanBeCaptain(userRequest, usersOnTeam, existingUser),
                validateUserUnits(userRequest)
            )
            .filter(Objects::nonNull)
            .collect(toList());

        if (!complexFailureMessages.isEmpty()) {
            return ValidationResult.failure(userRequest, complexFailureMessages);
        }

        return ValidationResult.successful(User.createWithoutId(userRequest, hardwareForUser, teamForUser));
    }

    private String validateUserUnits(final UserRequest userRequest) {
        try {
            final FoldingStatsDetails foldingStatsDetails = FoldingStatsDetails.create(userRequest.getFoldingUserName(), userRequest.getPasskey());
            final Stats statsForUserAndPasskey = foldingStatsRetriever.getStats(foldingStatsDetails);

            if (statsForUserAndPasskey.getUnits() == 0) {
                return String.format(
                    "User '%s' has 0 Work Units with passkey '%s', there must be at least one completed Work Unit before adding the user",
                    userRequest.getFoldingUserName(), userRequest.getPasskey());
            }
        } catch (final ExternalConnectionException e) {
            return String.format("Unable to connect to '%s' to check stats for user '%s': %s", e.getUrl(), userRequest.getDisplayName(),
                e.getMessage());
        } catch (final Exception e) {
            return String.format("Unable to check stats for user '%s': %s", userRequest.getDisplayName(), e.getMessage());
        }

        return null;
    }

    private String validateUpdatedUserDoesNotExceedTeamLimits(final UserRequest userRequest, final User existingUser,
                                                              final Collection<User> usersOnTeam,
                                                              final Category category) {
        final boolean userIsChangingTeams = userRequest.getTeamId() != existingUser.getTeam().getId();
        if (userIsChangingTeams) {
            // If we are changing teams, we need to ensure there is enough space in the team and category
            return validateNewUserDoesNotExceedTeamLimits(usersOnTeam, category);
        }

        final boolean userIsChangingCategory = category != existingUser.getCategory();
        if (userIsChangingCategory) {
            // If we are staying on the team but changing category, we need to ensure there is space in the category
            final int permittedNumberForCategory = category.permittedUsers();
            final long numberOfUsersInTeamWithCategory = usersOnTeam
                .stream()
                .filter(user -> user.getId() != existingUser.getId() && user.getCategory() == category)
                .count();

            if (numberOfUsersInTeamWithCategory >= permittedNumberForCategory) {
                return String.format("Team '%s' already has %s users in category '%s', only %s permitted",
                    teamForUser.getTeamName(), numberOfUsersInTeamWithCategory, category, permittedNumberForCategory);
            }
        }

        return null;
    }

    private String validateNewUserCanBeCaptain(final UserRequest userRequest, final Collection<User> usersOnTeam) {
        return validateUserCanBeCaptain(userRequest, usersOnTeam, null);
    }

    private String validateUpdatedUserCanBeCaptain(final UserRequest userRequest, final Collection<User> usersOnTeam, final User existingUser) {
        return validateUserCanBeCaptain(userRequest, usersOnTeam, existingUser);
    }

    private String validateUserCanBeCaptain(final UserRequest userRequest, final Collection<User> usersOnTeam, final User existingUser) {
        if (!userRequest.isUserIsCaptain()) {
            return null;
        }

        for (final User userOnTeam : usersOnTeam) {
            if (userOnTeam.isUserIsCaptain() && (existingUser == null || existingUser.getId() != userOnTeam.getId())) {
                return String.format("Team '%s' already has a captain '%s', cannot have multiple captains", teamForUser.getTeamName(),
                    userOnTeam.getDisplayName());
            }
        }

        return null;
    }

    private String validateNewUserDoesNotExceedTeamLimits(final Collection<User> usersOnTeam, final Category category) {
        if (usersOnTeam.size() == Category.maximumPermittedAmountForAllCategories()) {
            return String.format("Team '%s' has %s users, maximum permitted is %s", teamForUser.getTeamName(), usersOnTeam.size(),
                Category.maximumPermittedAmountForAllCategories());
        }

        final int permittedNumberForCategory = category.permittedUsers();
        final long numberOfUsersInTeamWithCategory = usersOnTeam
            .stream()
            .filter(user -> user.getCategory() == category)
            .count();

        if (numberOfUsersInTeamWithCategory == permittedNumberForCategory) {
            return String.format("Team '%s' already has %s users in category '%s', only %s permitted", teamForUser.getTeamName(),
                numberOfUsersInTeamWithCategory, category, permittedNumberForCategory);
        }

        return null;
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

    private static String foldingUserName(final UserRequest userRequest) {
        return StringUtils.isNotBlank(userRequest.getFoldingUserName())
            ? null
            : "Field 'foldingUserName' must not be empty";
    }

    private static String displayName(final UserRequest userRequest) {
        return StringUtils.isNotBlank(userRequest.getDisplayName())
            ? null
            : "Field 'displayName' must not be empty";
    }

    private static String passkey(final UserRequest userRequest) {
        if (StringUtils.isBlank(userRequest.getPasskey())) {
            return "Field 'passkey' must not be empty";
        }

        if (userRequest.getPasskey().length() != EXPECTED_PASSKEY_LENGTH) {
            return String.format("Field 'passkey' must be %d characters in length", EXPECTED_PASSKEY_LENGTH);
        }

        if (userRequest.getPasskey().contains("*")) {
            return "Field 'passkey' cannot contain '*' characters";
        }

        return null;
    }

    private static String category(final UserRequest userRequest) {
        return Category.get(userRequest.getCategory()) == Category.INVALID
            ? String.format("Field 'category' must be one of: %s", Category.getAllValues())
            : null;
    }

    private static String profileLink(final UserRequest userRequest) {
        return (StringUtils.isBlank(userRequest.getProfileLink()) || URL_VALIDATOR.isValid(userRequest.getProfileLink()))
            ? null
            : String.format("Field 'profileLink' is not a valid link: '%s'", userRequest.getProfileLink());
    }

    private static String liveStatsLink(final UserRequest userRequest) {
        return (StringUtils.isBlank(userRequest.getLiveStatsLink()) || URL_VALIDATOR.isValid(userRequest.getLiveStatsLink()))
            ? null
            : String.format("Field 'liveStatsLink' is not a valid link: '%s'", userRequest.getLiveStatsLink());
    }

    private String hardware(final UserRequest userRequest, final Collection<Hardware> allHardware) {
        if (allHardware.isEmpty()) {
            return "No hardware exist on the system";
        }

        final Optional<Hardware> optionalHardware = getHardware(userRequest.getHardwareId(), allHardware);
        if (optionalHardware.isEmpty()) {
            final List<String> availableHardware = allHardware
                .stream()
                .map(hardware -> String.format("%s: %s", hardware.getId(), hardware.getHardwareName()))
                .collect(toList());

            return String.format("Field 'hardwareId' must be one of: %s", availableHardware);
        }

        hardwareForUser = optionalHardware.get();
        return null;
    }

    private String team(final UserRequest userRequest, final Collection<Team> allTeams) {
        if (allTeams.isEmpty()) {
            return "No teams exist on the system";
        }

        final Optional<Team> optionalTeam = getTeam(userRequest.getTeamId(), allTeams);
        if (optionalTeam.isEmpty()) {
            final List<String> availableTeams = allTeams
                .stream()
                .map(team -> String.format("%s: %s", team.getId(), team.getTeamName()))
                .collect(toList());

            return String.format("Field 'teamId' must be one of: %s", availableTeams);
        }

        teamForUser = optionalTeam.get();
        return null;
    }
}

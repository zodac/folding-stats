/*
 * MIT License
 *
 * Copyright (c) 2021-2022 zodac.me
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.zodac.folding.api.tc.validation;

import static me.zodac.folding.api.util.StringUtils.isBlank;
import static me.zodac.folding.api.util.StringUtils.isBlankOrValidUrl;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import me.zodac.folding.api.exception.ConflictException;
import me.zodac.folding.api.exception.ExternalConnectionException;
import me.zodac.folding.api.exception.ValidationException;
import me.zodac.folding.api.stats.FoldingStatsDetails;
import me.zodac.folding.api.stats.FoldingStatsRetriever;
import me.zodac.folding.api.tc.Category;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.tc.stats.Stats;
import me.zodac.folding.rest.api.tc.request.UserRequest;

/**
 * Validator class to validate a {@link User} or {@link UserRequest}.
 */
public final class UserValidator {

    /**
     * {@link Pattern} defining a valid Folding@Home username.
     */
    public static final Pattern FOLDING_USER_NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9._-]*$");

    /**
     * {@link Pattern} defining a valid Folding@home passkey for a user.
     */
    public static final Pattern PASSKEY_PATTERN = Pattern.compile("[a-zA-Z0-9]{32}");

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
    public static UserValidator create(final FoldingStatsRetriever foldingStatsRetriever) {
        return new UserValidator(foldingStatsRetriever);
    }

    /**
     * Validates a {@link UserRequest} for a {@link User} to be created on the system.
     *
     * <p>
     * Validation checks include:
     * <ul>
     *     <li>Field 'foldingUserName' must not be empty, must only include alphanumeric characters or underscore (_), period (.) or hyphen (-)</li>
     *     <li>Field 'displayName' must not be empty</li>
     *     <li>Field 'passkey' must not be empty, must be 32-characters long, and must only include alphanumeric characters</li>
     *     <li>If fields 'foldingUserName' and 'passkey' are valid, they must not be used by another {@link User}</li>
     *     <li>Field 'category' must be a valid {@link Category}, and must match the {@link me.zodac.folding.api.tc.HardwareMake} and
     *     {@link me.zodac.folding.api.tc.HardwareType} of the user's {@link Hardware}</li>
     *     <li>If field 'profileLink' is not empty, it must be a valid URL</li>
     *     <li>If field 'liveStatsLink' is not empty, it must be a valid URL</li>
     *     <li>Field 'hardwareId' must match an existing {@link Hardware}</li>
     *     <li>Field 'teamId' must match an existing {@link Team}</li>
     *     <li>The {@link User} must not cause its {@link Team} to exceed the {@link Category#maximumPermittedAmountForAllCategories()}</li>
     *     <li>The {@link User} must not cause its {@link Category} to exceed the {@link Category#permittedUsers()}</li>
     *     <li>The 'foldingUserName' and 'passkey' combination has at least 1 Work Unit successfully completed</li>
     * </ul>
     *
     * @param userRequest the {@link UserRequest} to validate
     * @param allUsers    all existing {@link User}s in the system
     * @param allHardware all existing {@link Hardware}s in the system
     * @param allTeams    all existing {@link Team}s in the system
     * @return the validated {@link User}
     * @throws ConflictException   thrown if the input conflicts with an existing {@link User}
     * @throws ValidationException thrown  if the input fails validation
     */
    public User validateCreate(final UserRequest userRequest,
                               final Collection<User> allUsers,
                               final Collection<Hardware> allHardware,
                               final Collection<Team> allTeams) {
        // The foldingUserName and passkey must be unique
        final Optional<User> matchingUser = getUserWithFoldingUserNameAndPasskey(userRequest, allUsers);
        if (matchingUser.isPresent()) {
            throw new ConflictException(userRequest, matchingUser.get(), List.of("foldingUserName", "passkey"));
        }

        // Hardware and team must be validated first, since they may be used by other validation checks
        final List<String> hardwareAndTeamFailureMessages = Stream.of(
                hardware(userRequest, allHardware),
                team(userRequest, allTeams)
            )
            .filter(Objects::nonNull)
            .toList();

        if (!hardwareAndTeamFailureMessages.isEmpty()) {
            throw new ValidationException(userRequest, hardwareAndTeamFailureMessages);
        }

        final List<String> failureMessages = Stream.of(
                foldingUserName(userRequest),
                displayName(userRequest),
                passkey(userRequest),
                category(userRequest),
                profileLink(userRequest),
                liveStatsLink(userRequest)
            )
            .filter(Objects::nonNull)
            .toList();

        if (!failureMessages.isEmpty()) {
            throw new ValidationException(userRequest, failureMessages);
        }

        final Collection<User> usersOnTeam = getUsersOnTeam(teamForUser.getId(), allUsers);
        final Category category = Category.get(userRequest.getCategory());

        final List<String> complexFailureMessages = Stream.of(
                validateNewUserDoesNotExceedTeamLimits(usersOnTeam, category),
                validateNewUserWorkUnits(userRequest)
            )
            .filter(Objects::nonNull)
            .toList();

        if (!complexFailureMessages.isEmpty()) {
            throw new ValidationException(userRequest, complexFailureMessages);
        }

        return User.createWithoutId(userRequest, hardwareForUser, teamForUser);
    }

    /**
     * Validates a {@link UserRequest} for a {@link User} to be updated on the system.
     *
     * <p>
     * Validation checks include:
     * <ul>
     *     <li>Field 'foldingUserName' must not be empty</li>
     *     <li>Field 'displayName' must not be empty</li>
     *     <li>Field 'passkey' must not be empty, must be 32-characters long, and must only include alphanumeric characters</li>
     *     <li>If fields 'foldingUserName' and 'passkey' are valid, they must not be used by another {@link User}, unless it is the {@link User} being
     *     updated</li>
     *     <li>Field 'category' must be a valid {@link Category}, and must match the {@link me.zodac.folding.api.tc.HardwareMake} and
     *     {@link me.zodac.folding.api.tc.HardwareType} of the user's {@link Hardware}</li>
     *     <li>If field 'profileLink' is not empty, it must be a valid URL</li>
     *     <li>If field 'liveStatsLink' is not empty, it must be a valid URL</li>
     *     <li>Field 'hardwareId' must match an existing {@link Hardware}</li>
     *     <li>Field 'teamId' must match an existing {@link Team}</li>
     *     <li>If the {@link User} is updating its {@link Team}, it must not cause its {@link Team} to exceed the
     *     {@link Category#maximumPermittedAmountForAllCategories()}</li>
     *     <li>If the {@link User} is updating its {@link Category} must not cause its {@link Category} to exceed the
     *     {@link Category#permittedUsers()}</li>
     *     <li>The 'foldingUserName' and 'passkey' combination (if either has changed) has at least 1 Work Unit successfully completed</li>
     * </ul>
     *
     * @param userRequest  the {@link UserRequest} to validate
     * @param existingUser the already existing {@link User} in the system to be updated
     * @param allUsers     all existing {@link User}s in the system
     * @param allHardware  all existing {@link Hardware}s in the system
     * @param allTeams     all existing {@link Team}s in the system
     * @return the validated {@link User}
     * @throws ConflictException   thrown if the input conflicts with an existing {@link User}
     * @throws ValidationException thrown  if the input fails validation
     */
    public User validateUpdate(final UserRequest userRequest,
                               final User existingUser,
                               final Collection<User> allUsers,
                               final Collection<Hardware> allHardware,
                               final Collection<Team> allTeams) {
        // The foldingUserName and passkey must be unique, unless replacing the same user
        final Optional<User> matchingUser = getUserWithFoldingUserNameAndPasskey(userRequest, allUsers);
        if (matchingUser.isPresent() && matchingUser.get().getId() != existingUser.getId()) {
            throw new ConflictException(userRequest, matchingUser.get(), List.of("foldingUserName", "passkey"));
        }

        // Hardware and team must be validated first, since they may be used by other validation checks
        final List<String> hardwareAndTeamFailureMessages = Stream.of(
                hardware(userRequest, allHardware),
                team(userRequest, allTeams)
            )
            .filter(Objects::nonNull)
            .toList();

        if (!hardwareAndTeamFailureMessages.isEmpty()) {
            throw new ValidationException(userRequest, hardwareAndTeamFailureMessages);
        }

        final List<String> failureMessages = Stream.of(
                foldingUserName(userRequest),
                displayName(userRequest),
                passkey(userRequest),
                category(userRequest),
                profileLink(userRequest),
                liveStatsLink(userRequest)
            )
            .filter(Objects::nonNull)
            .toList();

        // All subsequent checks will be heavier, so best to return early if any failures occur
        if (!failureMessages.isEmpty()) {
            throw new ValidationException(userRequest, failureMessages);
        }

        final Collection<User> usersOnTeam = getUsersOnTeam(teamForUser.getId(), allUsers);
        final Category category = Category.get(userRequest.getCategory());

        final List<String> complexFailureMessages = Stream.of(
                validateUpdatedUserDoesNotExceedTeamLimits(userRequest, existingUser, usersOnTeam, category),
                validateUpdateUserWorkUnits(userRequest, existingUser)
            )
            .filter(Objects::nonNull)
            .toList();

        if (!complexFailureMessages.isEmpty()) {
            throw new ValidationException(userRequest, complexFailureMessages);
        }

        return User.createWithoutId(userRequest, hardwareForUser, teamForUser);
    }

    /**
     * Validates whether a {@link User} can be deleted from the system.
     *
     * <p>
     * If the {@link User} is their {@link Team} captain, they cannot be deleted.
     *
     * @param user the {@link User} to delete
     * @return the validated {@link User}
     */
    public User validateDelete(final User user) {
        if (user.isUserIsCaptain()) {
            throw new ValidationException(user, String.format("Cannot delete user '%s' since they are team captain", user.getDisplayName()));
        }

        return user;
    }

    private String validateNewUserWorkUnits(final UserRequest userRequest) {
        return validateUserWorkUnits(userRequest);
    }

    private String validateUpdateUserWorkUnits(final UserRequest userRequest, final User existingUser) {
        final boolean isFoldingUserNameChange = !userRequest.getFoldingUserName().equalsIgnoreCase(existingUser.getFoldingUserName());
        final boolean isPasskeyChange = !userRequest.getPasskey().equalsIgnoreCase(existingUser.getPasskey());

        if (isFoldingUserNameChange || isPasskeyChange) {
            return validateUserWorkUnits(userRequest);
        }

        return null;
    }

    private String validateUserWorkUnits(final UserRequest userRequest) {
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

        if (isBlank(foldingUserName) || isBlank(passkey)) {
            return Optional.empty();
        }

        return allUsers
            .stream()
            .filter(user -> user.getFoldingUserName().equalsIgnoreCase(foldingUserName) && user.getPasskey().equalsIgnoreCase(passkey))
            .findAny();
    }

    private static Collection<User> getUsersOnTeam(final int teamId, final Collection<User> allUsers) {
        return allUsers
            .stream()
            .filter(user -> user.getTeam().getId() == teamId)
            .toList();
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
        return isBlank(userRequest.getFoldingUserName()) || !FOLDING_USER_NAME_PATTERN.matcher(userRequest.getFoldingUserName()).find()
            ? "Field 'foldingUserName' must have at least one alphanumeric character, or an underscore, period or hyphen"
            : null;
    }

    private static String displayName(final UserRequest userRequest) {
        return isBlank(userRequest.getDisplayName())
            ? "Field 'displayName' must not be empty"
            : null;
    }

    private static String passkey(final UserRequest userRequest) {
        return isBlank(userRequest.getPasskey()) || !PASSKEY_PATTERN.matcher(userRequest.getPasskey()).find()
            ? "Field 'passkey' must be 32 characters long and include only alphanumeric characters"
            : null;
    }

    private String category(final UserRequest userRequest) {
        final Category category = Category.get(userRequest.getCategory());
        if (category == Category.INVALID) {
            return String.format("Field 'category' must be one of: %s", Category.getAllValues());
        }

        if (!category.isHardwareMakeSupported(hardwareForUser.getHardwareMake())) {
            return String.format("Category '%s' cannot be filled by hardware of make '%s', must be one of: %s", category,
                hardwareForUser.getHardwareMake(), category.supportedHardwareMakes());
        }

        if (!category.isHardwareTypeSupported(hardwareForUser.getHardwareType())) {
            return String.format("Category '%s' cannot be filled by hardware of type '%s', must be one of: %s", category,
                hardwareForUser.getHardwareType(), category.supportedHardwareTypes());
        }

        return null;
    }

    private static String profileLink(final UserRequest userRequest) {
        return isBlankOrValidUrl(userRequest.getProfileLink())
            ? null
            : String.format("Field 'profileLink' is not a valid link: '%s'", userRequest.getProfileLink());
    }

    private static String liveStatsLink(final UserRequest userRequest) {
        return isBlankOrValidUrl(userRequest.getLiveStatsLink())
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
                .toList();

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
                .toList();

            return String.format("Field 'teamId' must be one of: %s", availableTeams);
        }

        teamForUser = optionalTeam.get();
        return null;
    }
}

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

import static me.zodac.folding.api.tc.validation.UserValidator.FOLDING_USER_NAME_PATTERN;
import static me.zodac.folding.api.tc.validation.UserValidator.PASSKEY_PATTERN;
import static me.zodac.folding.api.util.StringUtils.isBlank;
import static me.zodac.folding.api.util.StringUtils.isBlankOrValidUrl;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import me.zodac.folding.api.exception.ExternalConnectionException;
import me.zodac.folding.api.stats.FoldingStatsDetails;
import me.zodac.folding.api.stats.FoldingStatsRetriever;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.tc.change.UserChange;
import me.zodac.folding.api.tc.change.UserChangeState;
import me.zodac.folding.api.tc.stats.Stats;
import me.zodac.folding.rest.api.tc.request.UserChangeRequest;

/**
 * Validator class to validate a {@link UserChange} or {@link UserChangeRequest}.
 */
public final class UserChangeValidator {

    private final FoldingStatsRetriever foldingStatsRetriever;

    // These fields will be set during validation for ease of re-use
    private Hardware newHardware;
    private User previousUser;

    private UserChangeValidator(final FoldingStatsRetriever foldingStatsRetriever) {
        this.foldingStatsRetriever = foldingStatsRetriever;
    }

    /**
     * Create an instance of {@link UserChangeValidator}.
     *
     * @param foldingStatsRetriever the {@link FoldingStatsRetriever} to verify the user stats
     * @return the created {@link UserChangeValidator}
     */
    public static UserChangeValidator create(final FoldingStatsRetriever foldingStatsRetriever) {
        return new UserChangeValidator(foldingStatsRetriever);
    }

    /**
     * Validates a {@link UserChangeRequest} for a {@link UserChange} to be created on the system.
     *
     * <p>
     * Validation checks include:
     * <ul>
     *     <li>Input {@code userChangeRequest} must not be <b>null</b></li>
     *     <li>Field 'hardwareId' must match an existing {@link Hardware}</li>
     *     <li>Field 'userId' must match an existing {@link User}</li>
     *     <li>Field 'foldingUserName' must not be empty, must only include alphanumeric characters or underscore (_), period (.) or hyphen (-)</li>
     *     <li>Field 'passkey' must not be empty, must be 32-characters long, and must only include alphanumeric characters</li>
     *     <li>If field 'liveStatsLink' is not empty, it must be a valid URL</li>
     *     <li>Field 'existingPasskey' must match the value of the existing {@link User}</li>
     *     <li>If fields 'userId, 'hardwareId, 'foldingUserName', 'passkey' and 'liveStatsLink' are valid, they must not be used by another
     *     {@link UserChange}</li>
     *     <li>If fields 'userId, 'hardwareId, 'foldingUserName', 'passkey' and 'liveStatsLink' are valid, they must not be the same values already in
     *     use by the {@link User}</li>
     *     <li>The 'foldingUserName' and 'passkey' combination (if either has changed) has at least 1 Work Unit successfully completed</li>
     * </ul>
     *
     * @param userChangeRequest              the {@link UserChangeRequest} to validate
     * @param allOpenUserChangesWithPasskeys all existing {@link UserChange}s in the system not in {@link UserChangeState#isFinalState()}, with
     *                                       passkeys
     * @param allHardware                    all {@link Hardware} in the system
     * @param allUsersWithPasskeys           all {@link User}s in the system, with passkeys
     * @return the {@link ValidationResult}
     */
    public ValidationResult<UserChange> validate(final UserChangeRequest userChangeRequest,
                                                 final Collection<UserChange> allOpenUserChangesWithPasskeys,
                                                 final Collection<Hardware> allHardware,
                                                 final Collection<User> allUsersWithPasskeys) {
        if (userChangeRequest == null) {
            return ValidationResult.nullObject();
        }

        // Hardware and User must be validated first, since they may be used by other validation checks
        final List<String> hardwareAndUserFailureMessages = Stream.of(
                hardware(userChangeRequest, allHardware),
                user(userChangeRequest, allUsersWithPasskeys)
            )
            .filter(Objects::nonNull)
            .toList();

        if (!hardwareAndUserFailureMessages.isEmpty()) {
            return ValidationResult.failure(userChangeRequest, hardwareAndUserFailureMessages);
        }

        // Validate the content is not-malformed
        final List<String> failureMessages = Stream.of(
                existingPasskey(userChangeRequest),
                foldingUserName(userChangeRequest),
                passkey(userChangeRequest),
                liveStatsLink(userChangeRequest)
            )
            .filter(Objects::nonNull)
            .toList();

        if (!failureMessages.isEmpty()) {
            return ValidationResult.failure(userChangeRequest, failureMessages);
        }

        // Check if the user already has the values in the UserChangeRequest
        if (isUserChangeUnnecessary(userChangeRequest)) {
            return ValidationResult.failure(userChangeRequest, List.of("User already has the values supplied in UserChangeRequest"));
        }

        // Check if an existing UserChange has already been made (and not rejected)
        final Optional<UserChange> matchingUserChange = findExistingUserChange(userChangeRequest, allOpenUserChangesWithPasskeys);
        if (matchingUserChange.isPresent()) {
            return ValidationResult.conflictingWith(userChangeRequest, matchingUserChange.get(),
                List.of("foldingUserName", "passkey", "liveStatsLink", "hardwareId"));
        }

        final String workUnitError = validateUpdateUserWorkUnits(userChangeRequest.getFoldingUserName(), previousUser.getFoldingUserName(),
            userChangeRequest.getPasskey(), previousUser.getPasskey());
        if (workUnitError != null) {
            return ValidationResult.failure(userChangeRequest, List.of(workUnitError));
        }

        final User newUser = User.create(
            previousUser.getId(),
            userChangeRequest.getFoldingUserName(),
            previousUser.getDisplayName(),
            userChangeRequest.getPasskey(),
            previousUser.getCategory(),
            previousUser.getProfileLink(),
            userChangeRequest.getLiveStatsLink(),
            newHardware,
            previousUser.getTeam(),
            previousUser.isUserIsCaptain()
        );
        final UserChangeState userChangeState =
            userChangeRequest.isImmediate() ? UserChangeState.REQUESTED_NOW : UserChangeState.REQUESTED_NEXT_MONTH;

        final UserChange userChange = UserChange.createNow(previousUser, newUser, userChangeState);
        return ValidationResult.successful(userChange);
    }

    private String validateUpdateUserWorkUnits(final String newFoldingUserName,
                                               final String previousFoldingUserName,
                                               final String newPasskey,
                                               final String previousPasskey) {
        final boolean isFoldingUserNameChange = !newFoldingUserName.equalsIgnoreCase(previousFoldingUserName);
        final boolean isPasskeyChange = !newPasskey.equalsIgnoreCase(previousPasskey);

        if (isFoldingUserNameChange || isPasskeyChange) {
            final FoldingStatsDetails foldingStatsDetails = FoldingStatsDetails.create(newFoldingUserName, newPasskey);
            return validateUserWorkUnits(foldingStatsDetails);
        }

        return null;
    }

    private String validateUserWorkUnits(final FoldingStatsDetails foldingStatsDetails) {
        try {
            final Stats statsForUserAndPasskey = foldingStatsRetriever.getStats(foldingStatsDetails);

            if (statsForUserAndPasskey.getUnits() == 0) {
                return String.format(
                    "User '%s' has 0 Work Units with passkey '%s', there must be at least one completed Work Unit before adding the user",
                    foldingStatsDetails.foldingUserName(), foldingStatsDetails.passkey());
            }
        } catch (final ExternalConnectionException e) {
            return String.format("Unable to connect to '%s' to check stats for Folding@Home user '%s': %s", e.getUrl(),
                foldingStatsDetails.foldingUserName(), e.getMessage());
        } catch (final Exception e) {
            return String.format("Unable to check stats for Folding@Home user '%s': %s", foldingStatsDetails.foldingUserName(), e.getMessage());
        }

        return null;
    }

    private boolean isUserChangeUnnecessary(final UserChangeRequest userChangeRequest) {
        return previousUser.getHardware().getId() == userChangeRequest.getHardwareId()
            && previousUser.getFoldingUserName().equals(userChangeRequest.getFoldingUserName())
            && previousUser.getPasskey().equals(userChangeRequest.getPasskey())
            && isEqualSafe(previousUser.getLiveStatsLink(), userChangeRequest.getLiveStatsLink());
    }

    private String hardware(final UserChangeRequest userChangeRequest, final Collection<Hardware> allHardware) {
        if (allHardware.isEmpty()) {
            return "No hardware exist on the system";
        }

        final Optional<Hardware> optionalHardware = getHardware(userChangeRequest.getHardwareId(), allHardware);
        if (optionalHardware.isEmpty()) {
            final List<String> availableHardware = allHardware
                .stream()
                .map(hardware -> String.format("%s: %s", hardware.getId(), hardware.getHardwareName()))
                .toList();

            return String.format("Field 'hardwareId' must be one of: %s", availableHardware);
        }

        newHardware = optionalHardware.get();
        return null;
    }

    private String user(final UserChangeRequest userChangeRequest, final Collection<User> allUsersWithPasskeys) {
        if (allUsersWithPasskeys.isEmpty()) {
            return "No users exist on the system";
        }

        final Optional<User> optionalUser = getUser(userChangeRequest.getUserId(), allUsersWithPasskeys);
        if (optionalUser.isEmpty()) {
            final List<String> availableUsers = allUsersWithPasskeys
                .stream()
                .map(user -> String.format("%s: %s", user.getId(), user.getDisplayName()))
                .toList();

            return String.format("Field 'userId' must be one of: %s", availableUsers);
        }

        previousUser = optionalUser.get();
        return null;
    }

    private static Optional<UserChange> findExistingUserChange(final UserChangeRequest userChangeRequest,
                                                               final Collection<UserChange> allOpenUserChangesWithPasskeys) {
        if (allOpenUserChangesWithPasskeys.isEmpty()) {
            return Optional.empty();
        }

        return allOpenUserChangesWithPasskeys
            .stream()
            .filter(userChange -> isMatchingUserChange(userChange, userChangeRequest))
            .findAny();
    }

    private static boolean isMatchingUserChange(final UserChange userChange, final UserChangeRequest userChangeRequest) {
        final User user = userChange.getNewUser();
        return user.getId() == userChangeRequest.getUserId()
            && user.getHardware().getId() == userChangeRequest.getHardwareId()
            && Objects.equals(user.getFoldingUserName(), userChangeRequest.getFoldingUserName())
            && Objects.equals(user.getPasskey(), userChangeRequest.getPasskey())
            && Objects.equals(user.getLiveStatsLink(), userChangeRequest.getLiveStatsLink());
    }

    private static Optional<Hardware> getHardware(final int hardwareId, final Collection<Hardware> allHardware) {
        return allHardware
            .stream()
            .filter(hardware -> hardware.getId() == hardwareId)
            .findAny();
    }

    private static Optional<User> getUser(final int userId, final Collection<User> allUsers) {
        return allUsers
            .stream()
            .filter(user -> user.getId() == userId)
            .findAny();
    }

    private String existingPasskey(final UserChangeRequest userChangeRequest) {
        return previousUser.getPasskey().equals(userChangeRequest.getExistingPasskey())
            ? null
            : "Field 'existingPasskey' does not match the existing passkey for the user";
    }

    private static String foldingUserName(final UserChangeRequest userChangeRequest) {
        return isBlank(userChangeRequest.getFoldingUserName()) || !FOLDING_USER_NAME_PATTERN.matcher(userChangeRequest.getFoldingUserName()).find()
            ? "Field 'foldingUserName' must have at least one alphanumeric character, or an underscore, period or hyphen"
            : null;
    }

    private static String passkey(final UserChangeRequest userChangeRequest) {
        return isBlank(userChangeRequest.getPasskey()) || !PASSKEY_PATTERN.matcher(userChangeRequest.getPasskey()).find()
            ? "Field 'passkey' must be 32 characters long and include only alphanumeric characters"
            : null;
    }

    private static String liveStatsLink(final UserChangeRequest userChangeRequest) {
        return isBlankOrValidUrl(userChangeRequest.getLiveStatsLink())
            ? null
            : String.format("Field 'liveStatsLink' is not a valid link: '%s'", userChangeRequest.getLiveStatsLink());
    }

    private static boolean isEqualSafe(final String first, final String second) {
        return isEmpty(first) ? isEmpty(second) : first.equals(second);
    }

    private static boolean isEmpty(final String input) {
        return input == null || input.isBlank();
    }
}

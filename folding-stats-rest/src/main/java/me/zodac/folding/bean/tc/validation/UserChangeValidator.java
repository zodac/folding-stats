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

package me.zodac.folding.bean.tc.validation;

import static me.zodac.folding.api.util.StringUtils.isEqualSafe;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import me.zodac.folding.api.FoldingRepository;
import me.zodac.folding.api.exception.ConflictException;
import me.zodac.folding.api.exception.ExternalConnectionException;
import me.zodac.folding.api.exception.ValidationException;
import me.zodac.folding.api.stats.FoldingStatsDetails;
import me.zodac.folding.api.stats.FoldingStatsRetriever;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.tc.change.UserChange;
import me.zodac.folding.api.tc.change.UserChangeState;
import me.zodac.folding.api.tc.stats.Stats;
import me.zodac.folding.rest.api.tc.request.UserChangeRequest;
import me.zodac.folding.rest.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Validator class to validate a {@link UserChange} or {@link UserChangeRequest}.
 */
@Component
public class UserChangeValidator {

    private final FoldingRepository foldingRepository;
    private final FoldingStatsRetriever foldingStatsRetriever;

    /**
     * {@link Autowired} constructor.
     *
     * @param foldingRepository     the {@link FoldingRepository}
     * @param foldingStatsRetriever the {@link FoldingStatsRetriever}
     */
    @Autowired
    public UserChangeValidator(final FoldingRepository foldingRepository, final FoldingStatsRetriever foldingStatsRetriever) {
        this.foldingRepository = foldingRepository;
        this.foldingStatsRetriever = foldingStatsRetriever;
    }

    /**
     * Validates a {@link UserChangeRequest} for a {@link UserChange} to be created on the system.
     *
     * <p>
     * Validation checks include:
     * <ul>
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
     * @param userChangeRequest the {@link UserChangeRequest} to validate
     * @return the validated {@link UserChange}
     * @throws ConflictException   thrown if the input conflicts with an existing {@link UserChange}
     * @throws ValidationException thrown  if the input fails validation
     */
    public UserChange validate(final UserChangeRequest userChangeRequest) {
        userChangeRequest.validate();

        // Check if an existing UserChange has already been made (and not rejected)
        final Optional<UserChange> matchingUserChange = findExistingUserChange(userChangeRequest);
        if (matchingUserChange.isPresent()) {
            throw new ConflictException(userChangeRequest, matchingUserChange.get(),
                List.of("foldingUserName", "passkey", "liveStatsLink", "hardwareId"));
        }

        final User previousUser = user(userChangeRequest);
        validateUserChangeIsUnnecessary(userChangeRequest, previousUser);
        validateExistingPasskeyMatchesExistingUser(userChangeRequest, previousUser);
        validateUpdateUserWorkUnits(userChangeRequest, previousUser);
        final Hardware newHardware = hardware(userChangeRequest);

        final User newUser = User.create(
            previousUser.id(),
            userChangeRequest.getFoldingUserName(),
            previousUser.displayName(),
            userChangeRequest.getPasskey(),
            previousUser.category(),
            previousUser.profileLink(),
            userChangeRequest.getLiveStatsLink(),
            newHardware,
            previousUser.team(),
            previousUser.userIsCaptain()
        );
        final UserChangeState userChangeState =
            userChangeRequest.isImmediate() ? UserChangeState.REQUESTED_NOW : UserChangeState.REQUESTED_NEXT_MONTH;

        return UserChange.createNow(previousUser, newUser, userChangeState);
    }

    private void validateUpdateUserWorkUnits(final UserChangeRequest userChangeRequest, final User previousUser) {
        final String newFoldingUserName = userChangeRequest.getFoldingUserName();
        final String oldFoldingUserName = previousUser.foldingUserName();
        final String newPasskey = userChangeRequest.getPasskey();
        final String oldPasskey = previousUser.passkey();

        final boolean isFoldingUserNameChange = !newFoldingUserName.equalsIgnoreCase(oldFoldingUserName);
        final boolean isPasskeyChange = !newPasskey.equalsIgnoreCase(oldPasskey);

        if (isFoldingUserNameChange || isPasskeyChange) {
            validateUserWorkUnits(userChangeRequest);
        }
    }

    private void validateUserWorkUnits(final UserChangeRequest userChangeRequest) {
        final Stats statsForUserAndPasskey = getStatsForUserAndPasskey(userChangeRequest);
        if (statsForUserAndPasskey.getUnits() == Stats.DEFAULT_UNITS) {
            throw new ValidationException(userChangeRequest, String.format(
                "User '%s' has 0 Work Units with passkey '%s', there must be at least one completed Work Unit before adding the user",
                userChangeRequest.getFoldingUserName(), userChangeRequest.getPasskey()));
        }
    }

    private Stats getStatsForUserAndPasskey(final UserChangeRequest userChangeRequest) {
        try {
            final FoldingStatsDetails foldingStatsDetails =
                FoldingStatsDetails.create(userChangeRequest.getFoldingUserName(), userChangeRequest.getPasskey());
            return foldingStatsRetriever.getStats(foldingStatsDetails);
        } catch (final ExternalConnectionException e) {
            throw new ValidationException(userChangeRequest,
                String.format("Unable to connect to '%s' to check stats for user '%s': %s", e.getUrl(), userChangeRequest.getFoldingUserName(),
                    e.getMessage()), e);
        } catch (final Exception e) {
            throw new ValidationException(userChangeRequest,
                String.format("Unable to check stats for user '%s': %s", userChangeRequest.getFoldingUserName(), e.getMessage()), e);
        }
    }

    private void validateUserChangeIsUnnecessary(final UserChangeRequest userChangeRequest, final User previousUser) {
        if (previousUser.hardware().id() == userChangeRequest.getHardwareId()
            && previousUser.foldingUserName().equals(userChangeRequest.getFoldingUserName())
            && previousUser.passkey().equals(userChangeRequest.getPasskey())
            && isEqualSafe(previousUser.liveStatsLink(), userChangeRequest.getLiveStatsLink())) {
            throw new ValidationException(userChangeRequest, "User already has the values supplied in UserChangeRequest");
        }
    }

    private Hardware hardware(final UserChangeRequest userChangeRequest) {
        try {
            return foldingRepository.getHardware(userChangeRequest.getHardwareId());
        } catch (final NotFoundException e) {
            final Collection<Hardware> allHardwares = foldingRepository.getAllHardware();
            if (allHardwares.isEmpty()) {
                throw new ValidationException(userChangeRequest, "No hardwares exist on the system", e);
            }

            final List<String> availableHardwares = allHardwares
                .stream()
                .map(hardware -> String.format("%s: %s", hardware.id(), hardware.hardwareName()))
                .toList();

            throw new ValidationException(userChangeRequest, String.format("Field 'hardwareId' must be one of: %s", availableHardwares), e);
        }
    }

    private User user(final UserChangeRequest userChangeRequest) {
        try {
            return foldingRepository.getUserWithPasskey(userChangeRequest.getUserId());
        } catch (final NotFoundException e) {
            final Collection<User> allUsers = foldingRepository.getAllUsersWithoutPasskeys();
            if (allUsers.isEmpty()) {
                throw new ValidationException(userChangeRequest, "No users exist on the system", e);
            }

            final List<String> availableUsers = allUsers
                .stream()
                .map(user -> String.format("%s: %s", user.id(), user.displayName()))
                .toList();

            throw new ValidationException(userChangeRequest, String.format("Field 'userId' must be one of: %s", availableUsers), e);
        }
    }

    private Optional<UserChange> findExistingUserChange(final UserChangeRequest userChangeRequest) {
        return foldingRepository.getAllUserChangesWithPasskeys(UserChangeState.getOpenStates(), 0L)
            .stream()
            .filter(userChange -> isMatchingUserChange(userChange, userChangeRequest))
            .findAny();
    }

    private static boolean isMatchingUserChange(final UserChange userChange, final UserChangeRequest userChangeRequest) {
        final User user = userChange.newUser();
        return user.id() == userChangeRequest.getUserId()
            && user.hardware().id() == userChangeRequest.getHardwareId()
            && Objects.equals(user.foldingUserName(), userChangeRequest.getFoldingUserName())
            && Objects.equals(user.passkey(), userChangeRequest.getPasskey())
            && Objects.equals(user.liveStatsLink(), userChangeRequest.getLiveStatsLink());
    }

    private void validateExistingPasskeyMatchesExistingUser(final UserChangeRequest userChangeRequest, final User previousUser) {
        if (!previousUser.passkey().equals(userChangeRequest.getExistingPasskey())) {
            throw new ValidationException(userChangeRequest, "Field 'existingPasskey' does not match the existing passkey for the user");
        }
    }
}

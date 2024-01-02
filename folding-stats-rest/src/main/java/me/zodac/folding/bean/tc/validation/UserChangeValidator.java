/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2024 zodac.me
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package me.zodac.folding.bean.tc.validation;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
import me.zodac.folding.bean.api.FoldingRepository;
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
            userChangeRequest.foldingUserName(),
            previousUser.displayName(),
            userChangeRequest.passkey(),
            previousUser.category(),
            previousUser.profileLink(),
            userChangeRequest.liveStatsLink(),
            newHardware,
            previousUser.team(),
            previousUser.role()
        );
        final UserChangeState userChangeState =
            userChangeRequest.immediate() ? UserChangeState.REQUESTED_NOW : UserChangeState.REQUESTED_NEXT_MONTH;

        return UserChange.createNow(previousUser, newUser, userChangeState);
    }

    private void validateUpdateUserWorkUnits(final UserChangeRequest userChangeRequest, final User previousUser) {
        final String newFoldingUserName = userChangeRequest.foldingUserName();
        final String oldFoldingUserName = previousUser.foldingUserName();
        final String newPasskey = userChangeRequest.passkey();
        final String oldPasskey = previousUser.passkey();

        final boolean isFoldingUserNameChange = !newFoldingUserName.equalsIgnoreCase(oldFoldingUserName);
        final boolean isPasskeyChange = !newPasskey.equalsIgnoreCase(oldPasskey);

        if (isFoldingUserNameChange || isPasskeyChange) {
            validateUserWorkUnits(userChangeRequest);
        }
    }

    private void validateUserWorkUnits(final UserChangeRequest userChangeRequest) {
        final Stats statsForUserAndPasskey = getStatsForUserAndPasskey(userChangeRequest);
        if (statsForUserAndPasskey.units() == Stats.DEFAULT_UNITS) {
            throw new ValidationException(userChangeRequest, String.format(
                "User '%s' has 0 Work Units with passkey '%s', there must be at least one completed Work Unit before adding the user",
                userChangeRequest.foldingUserName(), userChangeRequest.passkey()));
        }
    }

    private Stats getStatsForUserAndPasskey(final UserChangeRequest userChangeRequest) {
        try {
            final FoldingStatsDetails foldingStatsDetails =
                FoldingStatsDetails.create(userChangeRequest.foldingUserName(), userChangeRequest.passkey());
            return foldingStatsRetriever.getStats(foldingStatsDetails);
        } catch (final ExternalConnectionException e) {
            throw new ValidationException(userChangeRequest,
                String.format("Unable to connect to '%s' to check stats for user '%s': %s", e.getUrl(), userChangeRequest.foldingUserName(),
                    e.getMessage()), e);
        } catch (final Exception e) {
            throw new ValidationException(userChangeRequest,
                String.format("Unable to check stats for user '%s': %s", userChangeRequest.foldingUserName(), e.getMessage()), e);
        }
    }

    private static void validateUserChangeIsUnnecessary(final UserChangeRequest userChangeRequest, final User previousUser) {
        if (previousUser.hardware().id() == userChangeRequest.hardwareId()
            && previousUser.foldingUserName().equals(userChangeRequest.foldingUserName())
            && previousUser.passkey().equals(userChangeRequest.passkey())
            && Objects.equals(previousUser.liveStatsLink(), userChangeRequest.liveStatsLink())) {
            throw new ValidationException(userChangeRequest, "User already has the values supplied in UserChangeRequest");
        }
    }

    private Hardware hardware(final UserChangeRequest userChangeRequest) {
        try {
            return foldingRepository.getHardware(userChangeRequest.hardwareId());
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
            return foldingRepository.getUserWithPasskey(userChangeRequest.userId());
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
        return user.id() == userChangeRequest.userId()
            && user.hardware().id() == userChangeRequest.hardwareId()
            && Objects.equals(user.foldingUserName(), userChangeRequest.foldingUserName())
            && Objects.equals(user.passkey(), userChangeRequest.passkey())
            && Objects.equals(user.liveStatsLink(), userChangeRequest.liveStatsLink());
    }

    private static void validateExistingPasskeyMatchesExistingUser(final UserChangeRequest userChangeRequest, final User previousUser) {
        if (!previousUser.passkey().equals(userChangeRequest.existingPasskey())) {
            throw new ValidationException(userChangeRequest, "Field 'existingPasskey' does not match the existing passkey for the user");
        }
    }
}

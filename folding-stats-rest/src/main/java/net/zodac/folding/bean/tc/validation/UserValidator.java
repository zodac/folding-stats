/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2025 zodac.net
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

package net.zodac.folding.bean.tc.validation;

import static net.zodac.folding.api.util.StringUtils.isBlank;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import net.zodac.folding.api.exception.ConflictException;
import net.zodac.folding.api.exception.ExternalConnectionException;
import net.zodac.folding.api.exception.ValidationException;
import net.zodac.folding.api.stats.FoldingStatsDetails;
import net.zodac.folding.api.stats.FoldingStatsRetriever;
import net.zodac.folding.api.tc.Category;
import net.zodac.folding.api.tc.Hardware;
import net.zodac.folding.api.tc.HardwareMake;
import net.zodac.folding.api.tc.HardwareType;
import net.zodac.folding.api.tc.Team;
import net.zodac.folding.api.tc.User;
import net.zodac.folding.api.tc.stats.Stats;
import net.zodac.folding.bean.api.FoldingRepository;
import net.zodac.folding.rest.api.tc.request.UserRequest;
import net.zodac.folding.rest.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Validator class to validate a {@link User} or {@link UserRequest}.
 */
@Component
public class UserValidator {

    private static final Collection<String> CONFLICTING_ATTRIBUTES = List.of("foldingUserName", "passkey");

    private final FoldingRepository foldingRepository;
    private final FoldingStatsRetriever foldingStatsRetriever;

    /**
     * {@link Autowired} constructor.
     *
     * @param foldingRepository     the {@link FoldingRepository}
     * @param foldingStatsRetriever the {@link FoldingStatsRetriever}
     */
    @Autowired
    public UserValidator(final FoldingRepository foldingRepository, final FoldingStatsRetriever foldingStatsRetriever) {
        this.foldingRepository = foldingRepository;
        this.foldingStatsRetriever = foldingStatsRetriever;
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
     *     <li>Field 'category' must be a valid {@link Category}, and must match the {@link HardwareMake} and
     *     {@link HardwareType} of the user's {@link Hardware}</li>
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
     * @return the validated {@link User}
     * @throws ConflictException   thrown if the input conflicts with an existing {@link User}
     * @throws ValidationException thrown if the input fails validation
     */
    public User create(final UserRequest userRequest) {
        // The foldingUserName and passkey must be unique
        final Optional<User> matchingUser = getUserWithFoldingUserNameAndPasskey(userRequest);
        if (matchingUser.isPresent()) {
            throw new ConflictException(userRequest, matchingUser.get(), CONFLICTING_ATTRIBUTES);
        }

        userRequest.validate();
        final Hardware hardwareForUser = hardware(userRequest);
        final Team teamForUser = team(userRequest);
        final Category category = validateCategoryIsValidForHardware(userRequest, hardwareForUser);
        validateNewUserDoesNotExceedTeamLimits(userRequest, teamForUser, category);
        validateUserWorkUnits(userRequest);

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
     *     <li>Field 'category' must be a valid {@link Category}, and must match the {@link HardwareMake} and
     *     {@link HardwareType} of the user's {@link Hardware}</li>
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
     * @return the validated {@link User}
     * @throws ConflictException   thrown if the input conflicts with an existing {@link User}
     * @throws ValidationException thrown if the input fails validation
     */
    public User update(final UserRequest userRequest,
                       final User existingUser) {
        // The foldingUserName and passkey must be unique, unless replacing the same user
        final Optional<User> matchingUser = getUserWithFoldingUserNameAndPasskey(userRequest);
        if (matchingUser.isPresent() && matchingUser.get().id() != existingUser.id()) {
            throw new ConflictException(userRequest, matchingUser.get(), CONFLICTING_ATTRIBUTES);
        }

        userRequest.validate();
        final Hardware hardwareForUser = hardware(userRequest);
        final Team teamForUser = team(userRequest);
        final Category category = validateCategoryIsValidForHardware(userRequest, hardwareForUser);
        validateUpdatedUserDoesNotExceedTeamLimits(userRequest, existingUser, teamForUser, category);
        validateUpdateUserWorkUnits(userRequest, existingUser);

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
    public User delete(final User user) {
        if (user.role().isCaptain()) {
            throw new ValidationException(user, String.format("Cannot delete user '%s' since they are team captain", user.displayName()));
        }

        return user;
    }

    private void validateUpdateUserWorkUnits(final UserRequest userRequest, final User existingUser) {
        final boolean isFoldingUserNameChange = !userRequest.foldingUserName().equalsIgnoreCase(existingUser.foldingUserName());
        final boolean isPasskeyChange = !userRequest.passkey().equalsIgnoreCase(existingUser.passkey());

        if (isFoldingUserNameChange || isPasskeyChange) {
            validateUserWorkUnits(userRequest);
        }
    }

    private void validateUserWorkUnits(final UserRequest userRequest) {
        final Stats statsForUserAndPasskey = getStatsForUserAndPasskey(userRequest);

        if (statsForUserAndPasskey.units() == Stats.DEFAULT_UNITS) {
            throw new ValidationException(userRequest,
                String.format("User '%s' has 0 Work Units with passkey '%s', there must be at least one completed Work Unit before adding the user",
                    userRequest.foldingUserName(), userRequest.passkey()));
        }
    }

    private Stats getStatsForUserAndPasskey(final UserRequest userRequest) {
        try {
            final FoldingStatsDetails foldingStatsDetails = FoldingStatsDetails.create(userRequest.foldingUserName(), userRequest.passkey());
            return foldingStatsRetriever.getStats(foldingStatsDetails);
        } catch (final ExternalConnectionException e) {
            throw new ValidationException(userRequest,
                String.format("Unable to connect to '%s' to check stats for user '%s': %s", e.getUrl(), userRequest.displayName(),
                    e.getMessage()), e);
        } catch (final Exception e) {
            throw new ValidationException(userRequest,
                String.format("Unable to check stats for user '%s': %s", userRequest.displayName(), e.getMessage()), e);
        }
    }

    private void validateUpdatedUserDoesNotExceedTeamLimits(final UserRequest userRequest,
                                                            final User existingUser,
                                                            final Team teamForUser,
                                                            final Category category) {
        final boolean userIsChangingTeams = userRequest.teamId() != existingUser.team().id();
        final Collection<User> usersOnTeam = foldingRepository.getUsersOnTeam(teamForUser);

        if (userIsChangingTeams) {
            // If we are changing teams, we need to ensure there is enough space in the team and category
            validateNewUserDoesNotExceedTeamLimits(userRequest, teamForUser, category);
        }

        final boolean userIsChangingCategory = category != existingUser.category();
        if (userIsChangingCategory) {
            // If we are staying on the team but changing category, we need to ensure there is space in the category
            final int permittedNumberForCategory = category.permittedUsers();
            final long numberOfUsersInTeamWithCategory = usersOnTeam
                .stream()
                .filter(user -> user.id() != existingUser.id() && user.category() == category)
                .count();

            if (numberOfUsersInTeamWithCategory >= permittedNumberForCategory) {
                throw new ValidationException(userRequest,
                    String.format("Team '%s' already has %s users in category '%s', only %s permitted", teamForUser.teamName(),
                        numberOfUsersInTeamWithCategory, category, permittedNumberForCategory));
            }
        }
    }

    private void validateNewUserDoesNotExceedTeamLimits(final UserRequest userRequest, final Team teamForUser, final Category category) {
        final Collection<User> usersOnTeam = foldingRepository.getUsersOnTeam(teamForUser);
        if (usersOnTeam.size() == Category.maximumPermittedAmountForAllCategories()) {
            throw new ValidationException(userRequest,
                String.format("Team '%s' has %s users, maximum permitted is %s", teamForUser.teamName(), usersOnTeam.size(),
                    Category.maximumPermittedAmountForAllCategories()));
        }

        final int permittedNumberForCategory = category.permittedUsers();
        final long numberOfUsersInTeamWithCategory = usersOnTeam
            .stream()
            .filter(user -> user.category() == category)
            .count();

        if (numberOfUsersInTeamWithCategory == permittedNumberForCategory) {
            throw new ValidationException(userRequest,
                String.format("Team '%s' already has %s users in category '%s', only %s permitted", teamForUser.teamName(),
                    numberOfUsersInTeamWithCategory, category, permittedNumberForCategory));
        }
    }

    private Optional<User> getUserWithFoldingUserNameAndPasskey(final UserRequest userRequest) {
        final String foldingUserName = userRequest.foldingUserName();
        final String passkey = userRequest.passkey();

        if (isBlank(foldingUserName) || isBlank(passkey)) {
            return Optional.empty();
        }

        return foldingRepository.getAllUsersWithPasskeys()
            .stream()
            .filter(user -> user.foldingUserName().equalsIgnoreCase(foldingUserName) && user.passkey().equalsIgnoreCase(passkey))
            .findAny();
    }

    private static Category validateCategoryIsValidForHardware(final UserRequest userRequest, final Hardware hardwareForUser) {
        final Category category = Category.get(userRequest.category());

        if (!category.isHardwareMakeSupported(hardwareForUser.hardwareMake())) {
            throw new ValidationException(userRequest,
                String.format("Category '%s' cannot be filled by hardware of make '%s', must be one of: %s", category,
                    hardwareForUser.hardwareMake(), category.supportedHardwareMakes()));
        }

        if (!category.isHardwareTypeSupported(hardwareForUser.hardwareType())) {
            throw new ValidationException(userRequest,
                String.format("Category '%s' cannot be filled by hardware of type '%s', must be one of: %s", category,
                    hardwareForUser.hardwareType(), category.supportedHardwareTypes()));
        }

        return category;
    }

    private Hardware hardware(final UserRequest userRequest) {
        try {
            return foldingRepository.getHardware(userRequest.hardwareId());
        } catch (final NotFoundException e) {
            final Collection<Hardware> allHardwares = foldingRepository.getAllHardware();
            if (allHardwares.isEmpty()) {
                throw new ValidationException(userRequest, "No hardwares exist on the system", e);
            }

            final List<String> availableHardwares = allHardwares
                .stream()
                .map(hardware -> String.format("%s: %s", hardware.id(), hardware.hardwareName()))
                .toList();
            throw new ValidationException(userRequest, String.format("Field 'hardwareId' must be one of: %s", availableHardwares), e);
        }
    }

    private Team team(final UserRequest userRequest) {
        try {
            return foldingRepository.getTeam(userRequest.teamId());
        } catch (final NotFoundException e) {
            final Collection<Team> allTeams = foldingRepository.getAllTeams();
            if (allTeams.isEmpty()) {
                throw new ValidationException(userRequest, "No teams exist on the system", e);
            }

            final List<String> availableTeams = allTeams
                .stream()
                .map(team -> String.format("%s: %s", team.id(), team.teamName()))
                .toList();

            throw new ValidationException(userRequest, String.format("Field 'teamId' must be one of: %s", availableTeams), e);
        }
    }
}

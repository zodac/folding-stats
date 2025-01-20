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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

import net.zodac.folding.api.exception.ConflictException;
import net.zodac.folding.api.exception.ValidationException;
import net.zodac.folding.api.tc.Category;
import net.zodac.folding.api.tc.Hardware;
import net.zodac.folding.api.tc.HardwareMake;
import net.zodac.folding.api.tc.HardwareType;
import net.zodac.folding.api.tc.Role;
import net.zodac.folding.api.tc.Team;
import net.zodac.folding.api.tc.User;
import net.zodac.folding.api.tc.change.UserChange;
import net.zodac.folding.api.tc.change.UserChangeState;
import net.zodac.folding.bean.api.FoldingRepository;
import net.zodac.folding.bean.tc.validation.retriever.ExternalConnectionFoldingStatsRetriever;
import net.zodac.folding.bean.tc.validation.retriever.NoUnitsFoldingStatsRetriever;
import net.zodac.folding.bean.tc.validation.retriever.UnexpectedExceptionFoldingStatsRetriever;
import net.zodac.folding.bean.tc.validation.retriever.ValidFoldingStatsRetriever;
import net.zodac.folding.rest.api.tc.request.UserChangeRequest;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link UserChangeValidator}.
 */
class UserChangeValidatorTest {

    private static final String DUMMY_PASSKEY = "DummyPasskey12345678901234567890";
    private static final String VALID_LIVE_STATS_LINK = "https://www.google.ie";

    private static int hardwareId = 1;
    private static int teamId = 1;
    private static int userId = 1;

    @Test
    void whenValidating_givenValidUserChange_thenSuccessResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final User user = generateUser(hardware);

        final UserChangeRequest userChange = generateUserChangeRequest(
            user.id(),
            user.passkey(),
            user.foldingUserName(),
            user.passkey(),
            VALID_LIVE_STATS_LINK,
            hardware.id(),
            true
        );

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createUser(user);

        final UserChangeValidator userChangeValidator = new UserChangeValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final UserChange response = userChangeValidator.validate(userChange);

        assertThat(response)
            .as("Expected validation to pass")
            .isNotNull();

        assertThat(response.newUser())
            .isNotNull();
        assertThat(response.newUser().hardware())
            .isNotNull();
        assertThat(response.newUser().team())
            .isNotNull();

        assertThat(response.state())
            .isEqualTo(UserChangeState.REQUESTED_NOW);
    }

    @Test
    void whenValidating_givenValidPasskeyChange_thenSuccessResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final User user = generateUser(hardware);

        final UserChangeRequest userChange = generateUserChangeRequest(
            user.id(),
            user.passkey(),
            user.foldingUserName(),
            DUMMY_PASSKEY,
            VALID_LIVE_STATS_LINK,
            hardware.id(),
            true
        );

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createUser(user);

        final UserChangeValidator userChangeValidator = new UserChangeValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final UserChange response = userChangeValidator.validate(userChange);

        assertThat(response)
            .as("Expected validation to pass")
            .isNotNull();
    }

    @Test
    void whenValidating_givenValidFoldingUserNameChange_thenSuccessResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final User user = generateUser(hardware);

        final UserChangeRequest userChange = generateUserChangeRequest(
            user.id(),
            user.passkey(),
            "-folding.Name_2",
            user.passkey(),
            VALID_LIVE_STATS_LINK,
            hardware.id(),
            true
        );

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createUser(user);

        final UserChangeValidator userChangeValidator = new UserChangeValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final UserChange response = userChangeValidator.validate(userChange);

        assertThat(response)
            .as("Expected validation to pass")
            .isNotNull();
    }

    @Test
    void whenValidating_givenInvalidFoldingUserName_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final User user = generateUser(hardware);

        final UserChangeRequest userChange = generateUserChangeRequest(
            user.id(),
            user.passkey(),
            "folding*Name",
            user.passkey(),
            VALID_LIVE_STATS_LINK,
            hardware.id(),
            false
        );

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createUser(user);

        final UserChangeValidator userChangeValidator = new UserChangeValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(ValidationException.class, () -> userChangeValidator.validate(userChange));
        assertThat(e.getValidationFailure().errors())
            .containsOnly("Field 'foldingUserName' must have at least one alphanumeric character, or an underscore, period or hyphen");
    }

    @Test
    void whenValidating_givenInvalidPasskey_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final User user = generateUser(hardware);

        final UserChangeRequest userChange = generateUserChangeRequest(
            user.id(),
            user.passkey(),
            user.foldingUserName(),
            "DummyPasskey1234567890123456789*",
            VALID_LIVE_STATS_LINK,
            hardware.id(),
            false
        );

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createUser(user);

        final UserChangeValidator userChangeValidator = new UserChangeValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(ValidationException.class, () -> userChangeValidator.validate(userChange));
        assertThat(e.getValidationFailure().errors())
            .containsOnly("Field 'passkey' must be 32 characters long and include only alphanumeric characters");
    }

    @Test
    void whenValidating_givenNullLiveStatsLink_thenSuccessResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final User user = generateUser(hardware);

        final UserChangeRequest userChange = generateUserChangeRequest(
            user.id(),
            user.passkey(),
            user.foldingUserName(),
            user.passkey(),
            null,
            hardware.id(),
            false
        );

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createUser(user);

        final UserChangeValidator userChangeValidator = new UserChangeValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final UserChange response = userChangeValidator.validate(userChange);

        assertThat(response)
            .as("Expected validation to pass")
            .isNotNull();
    }

    @Test
    void whenValidating_givenInvalidLiveStatsLink_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final User user = generateUser(hardware);

        final UserChangeRequest userChange = generateUserChangeRequest(
            user.id(),
            user.passkey(),
            user.foldingUserName(),
            user.passkey(),
            "invalidUrl",
            hardware.id(),
            false
        );

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createUser(user);

        final UserChangeValidator userChangeValidator = new UserChangeValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(ValidationException.class, () -> userChangeValidator.validate(userChange));
        assertThat(e.getValidationFailure().errors())
            .containsOnly("Field 'liveStatsLink' is not a valid link: 'invalidUrl'");
    }

    @Test
    void whenValidating_givenInvalidHardwareId_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final User user = generateUser(hardware);

        final UserChangeRequest userChange = generateUserChangeRequest(
            user.id(),
            user.passkey(),
            user.foldingUserName(),
            user.passkey(),
            VALID_LIVE_STATS_LINK,
            -1,
            false
        );

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createUser(user);

        final UserChangeValidator userChangeValidator = new UserChangeValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(ValidationException.class, () -> userChangeValidator.validate(userChange));
        assertThat(e.getValidationFailure().errors())
            .containsOnly(String.format("Field 'hardwareId' must be one of: [%s: %s]", hardware.id(), hardware.hardwareName()));
    }

    @Test
    void whenValidating_givenNoHardwareExists_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final User user = generateUser(hardware);

        final UserChangeRequest userChange = generateUserChangeRequest(
            user.id(),
            user.passkey(),
            user.foldingUserName(),
            user.passkey(),
            VALID_LIVE_STATS_LINK,
            -1,
            false
        );

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createUser(user);

        final UserChangeValidator userChangeValidator = new UserChangeValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(ValidationException.class, () -> userChangeValidator.validate(userChange));
        assertThat(e.getValidationFailure().errors())
            .containsOnly("No hardwares exist on the system");
    }

    @Test
    void whenValidating_givenInvalidUserId_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final User user = generateUser(hardware);

        final UserChangeRequest userChange = generateUserChangeRequest(
            -1,
            user.passkey(),
            user.foldingUserName(),
            user.passkey(),
            VALID_LIVE_STATS_LINK,
            hardware.id(),
            false
        );

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createUser(user);

        final UserChangeValidator userChangeValidator = new UserChangeValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(ValidationException.class, () -> userChangeValidator.validate(userChange));
        assertThat(e.getValidationFailure().errors())
            .containsOnly(String.format("Field 'userId' must be one of: [%s: %s]", user.id(), user.displayName()));
    }

    @Test
    void whenValidating_givenNoUsersExist_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final User user = generateUser(hardware);

        final UserChangeRequest userChange = generateUserChangeRequest(
            -1,
            user.passkey(),
            user.foldingUserName(),
            user.passkey(),
            VALID_LIVE_STATS_LINK,
            hardware.id(),
            false
        );

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);

        final UserChangeValidator userChangeValidator = new UserChangeValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(ValidationException.class, () -> userChangeValidator.validate(userChange));
        assertThat(e.getValidationFailure().errors())
            .containsOnly("No users exist on the system");
    }

    @Test
    void whenValidating_givenExistingPasskeyDoesNotMatchUserPasskey_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final User user = generateUser(hardware);

        final UserChangeRequest userChange = generateUserChangeRequest(
            user.id(),
            "DummyPasskey11111111111111111111",
            user.foldingUserName(),
            user.passkey(),
            VALID_LIVE_STATS_LINK,
            hardware.id(),
            false
        );

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createUser(user);

        final UserChangeValidator userChangeValidator = new UserChangeValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(ValidationException.class, () -> userChangeValidator.validate(userChange));
        assertThat(e.getValidationFailure().errors())
            .containsOnly("Field 'existingPasskey' does not match the existing passkey for the user");
    }

    @Test
    void whenValidating_givenUserDoesNotNeedChange_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final User user = generateUser(hardware);

        final UserChangeRequest userChange = generateUserChangeRequest(
            user.id(),
            user.passkey(),
            user.foldingUserName(),
            user.passkey(),
            user.liveStatsLink(),
            hardware.id(),
            false
        );

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createUser(user);

        final UserChangeValidator userChangeValidator = new UserChangeValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(ValidationException.class, () -> userChangeValidator.validate(userChange));
        assertThat(e.getValidationFailure().errors())
            .containsOnly("User already has the values supplied in UserChangeRequest");
    }

    @Test
    void whenValidating_givenMatchingUserChangeAlreadyExists_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final User user = generateUser(hardware);

        final UserChangeRequest userChange = generateUserChangeRequest(
            user.id(),
            user.passkey(),
            user.foldingUserName(),
            user.passkey(),
            VALID_LIVE_STATS_LINK,
            hardware.id(),
            false
        );

        final UserChange existingUserChange = UserChange.createNow(
            user,
            User.create(
                user.id(),
                user.foldingUserName(),
                user.displayName(),
                user.passkey(),
                user.category(),
                user.profileLink(),
                VALID_LIVE_STATS_LINK,
                user.hardware(),
                user.team(),
                user.role()
            ),
            UserChangeState.APPROVED_NEXT_MONTH
        );

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createUser(user);
        foldingRepository.createUserChange(existingUserChange);

        final UserChangeValidator userChangeValidator = new UserChangeValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final ConflictException e = catchThrowableOfType(ConflictException.class, () -> userChangeValidator.validate(userChange));
        assertThat(e.getConflictFailure().conflictingAttributes())
            .containsOnly(
                "foldingUserName",
                "passkey",
                "liveStatsLink",
                "hardwareId"
            );

        assertThat(e.getConflictFailure().conflictingObject())
            .isNotNull();
    }

    @Test
    void whenValidating_givenMatchingUserChangeExistsWithDifferentLink_thenSuccessResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final User user = generateUser(hardware);

        final UserChangeRequest userChange = generateUserChangeRequest(
            user.id(),
            user.passkey(),
            user.foldingUserName(),
            user.passkey(),
            VALID_LIVE_STATS_LINK,
            hardware.id(),
            false
        );

        final UserChange existingUserChange = UserChange.createNow(
            user,
            User.create(
                user.id(),
                user.foldingUserName(),
                user.displayName(),
                user.passkey(),
                user.category(),
                user.profileLink(),
                "https://www.google.com",
                user.hardware(),
                user.team(),
                user.role()
            ),
            UserChangeState.APPROVED_NEXT_MONTH
        );

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createUser(user);
        foldingRepository.createUserChange(existingUserChange);

        final UserChangeValidator userChangeValidator = new UserChangeValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final UserChange response = userChangeValidator.validate(userChange);
        assertThat(response)
            .as("Expected validation to pass")
            .isNotNull();
    }

    @Test
    void whenValidating_givenMatchingUserChangeAlreadyExists_andUserIdDoesNotMatch_thenSuccessResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final User user = generateUser(hardware);

        final UserChangeRequest userChange = generateUserChangeRequest(
            user.id(),
            user.passkey(),
            user.foldingUserName(),
            user.passkey(),
            VALID_LIVE_STATS_LINK,
            hardware.id(),
            false
        );

        final UserChange existingUserChange = UserChange.createNow(
            user,
            User.create(
                99,
                user.foldingUserName(),
                user.displayName(),
                user.passkey(),
                user.category(),
                user.profileLink(),
                VALID_LIVE_STATS_LINK,
                user.hardware(),
                user.team(),
                user.role()
            ),
            UserChangeState.APPROVED_NOW
        );

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createUser(user);
        foldingRepository.createUserChange(existingUserChange);

        final UserChangeValidator userChangeValidator = new UserChangeValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final UserChange response = userChangeValidator.validate(userChange);
        assertThat(response)
            .as("Expected validation to pass")
            .isNotNull();
    }

    @Test
    void whenValidating_givenPasskeyChange_andNewUserHasNoWorkUnitsCompleted_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final User user = generateUser(hardware);

        final UserChangeRequest userChange = generateUserChangeRequest(
            user.id(),
            user.passkey(),
            user.foldingUserName(),
            "DummyPasskey12345678901234567891",
            VALID_LIVE_STATS_LINK,
            hardware.id(),
            false
        );

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createUser(user);

        final UserChangeValidator userChangeValidator = new UserChangeValidator(foldingRepository, new NoUnitsFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(ValidationException.class, () -> userChangeValidator.validate(userChange));
        assertThat(e.getValidationFailure().errors())
            .containsOnly(
                String.format(
                    "User '%s' has 0 Work Units with passkey '%s', there must be at least one completed Work Unit before adding the user",
                    userChange.foldingUserName(), userChange.passkey())
            );
    }

    @Test
    void whenValidating_givenFoldingUserNameChange_andNewUserHasNoWorkUnitsCompleted_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final User user = generateUser(hardware);

        final UserChangeRequest userChange = generateUserChangeRequest(
            user.id(),
            user.passkey(),
            "-folding.Name_2",
            user.passkey(),
            VALID_LIVE_STATS_LINK,
            hardware.id(),
            false
        );

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createUser(user);

        final UserChangeValidator userChangeValidator = new UserChangeValidator(foldingRepository, new NoUnitsFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(ValidationException.class, () -> userChangeValidator.validate(userChange));
        assertThat(e.getValidationFailure().errors())
            .containsOnly(
                String.format(
                    "User '%s' has 0 Work Units with passkey '%s', there must be at least one completed Work Unit before adding the user",
                    userChange.foldingUserName(), userChange.passkey())
            );
    }

    @Test
    void whenValidating_givenUserWorkUnitsCannotBeAccessed_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final User user = generateUser(hardware);

        final UserChangeRequest userChange = generateUserChangeRequest(
            user.id(),
            user.passkey(),
            user.foldingUserName(),
            "DummyPasskey12345678901234567891",
            VALID_LIVE_STATS_LINK,
            hardware.id(),
            false
        );

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createUser(user);

        final UserChangeValidator userChangeValidator = new UserChangeValidator(foldingRepository, new ExternalConnectionFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(ValidationException.class, () -> userChangeValidator.validate(userChange));
        assertThat(e.getValidationFailure().errors())
            .containsOnly(
                String.format("Unable to connect to 'https://www.google.com' to check stats for user '%s': Error connecting",
                    userChange.foldingUserName())
            );
    }

    @Test
    void whenValidating_givenUnexpectedErrorWhenAccessingUserWorkUnits_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final User user = generateUser(hardware);

        final UserChangeRequest userChange = generateUserChangeRequest(
            user.id(),
            user.passkey(),
            user.foldingUserName(),
            "DummyPasskey12345678901234567891",
            VALID_LIVE_STATS_LINK,
            hardware.id(),
            false
        );

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createUser(user);

        final UserChangeValidator userChangeValidator = new UserChangeValidator(foldingRepository, new UnexpectedExceptionFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(ValidationException.class, () -> userChangeValidator.validate(userChange));
        assertThat(e.getValidationFailure().errors())
            .containsOnly(String.format("Unable to check stats for user '%s': Error", user.foldingUserName()));
    }

    private static Hardware generateHardware() {
        return Hardware.create(
            hardwareId++,
            "hardwareName",
            "displayName",
            HardwareMake.NVIDIA,
            HardwareType.GPU,
            1.00D,
            1L
        );
    }

    private static Team generateTeam() {
        return Team.create(
            teamId++,
            "teamName",
            "teamDescription",
            "https://www.google.com"
        );
    }

    private static User generateUser(final Hardware hardware) {
        return User.create(
            userId++,
            "-folding.Name_1",
            "user",
            "DummyPasskey12345678901234567890",
            Category.NVIDIA_GPU,
            "https://www.google.com",
            "https://www.google.com",
            hardware,
            generateTeam(),
            Role.CAPTAIN
        );
    }

    private static UserChangeRequest generateUserChangeRequest(final int userId,
                                                               final String existingPasskey,
                                                               final String foldingUserName,
                                                               final String passkey,
                                                               final @Nullable String liveStatsLink,
                                                               final int hardwareId,
                                                               final boolean immediate) {
        return new UserChangeRequest(userId, existingPasskey, foldingUserName, passkey, liveStatsLink, hardwareId, immediate);
    }
}

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

import me.zodac.folding.api.exception.ConflictException;
import me.zodac.folding.api.exception.ValidationException;
import me.zodac.folding.api.tc.Category;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.HardwareMake;
import me.zodac.folding.api.tc.HardwareType;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.tc.change.UserChange;
import me.zodac.folding.api.tc.change.UserChangeState;
import me.zodac.folding.bean.api.FoldingRepository;
import me.zodac.folding.bean.tc.validation.retriever.ExternalConnectionFoldingStatsRetriever;
import me.zodac.folding.bean.tc.validation.retriever.NoUnitsFoldingStatsRetriever;
import me.zodac.folding.bean.tc.validation.retriever.UnexpectedExceptionFoldingStatsRetriever;
import me.zodac.folding.bean.tc.validation.retriever.ValidFoldingStatsRetriever;
import me.zodac.folding.rest.api.tc.request.UserChangeRequest;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link UserChangeValidator}.
 */
class UserChangeValidatorTest {

    private static int hardwareId = 1;
    private static int teamId = 1;
    private static int userId = 1;

    @Test
    void whenValidating_givenValidUserChange_thenSuccessResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final User user = generateUser(hardware);

        final UserChangeRequest userChange = UserChangeRequest.builder()
            .existingPasskey(user.passkey())
            .foldingUserName(user.foldingUserName())
            .passkey(user.passkey())
            .hardwareId(hardware.id())
            .userId(user.id())
            .liveStatsLink("https://www.google.ie")
            .immediate(true)
            .build();

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

        final UserChangeRequest userChange = UserChangeRequest.builder()
            .existingPasskey(user.passkey())
            .foldingUserName(user.foldingUserName())
            .passkey("DummyPasskey12345678901234567891")
            .hardwareId(hardware.id())
            .userId(user.id())
            .liveStatsLink("https://www.google.ie")
            .build();

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

        final UserChangeRequest userChange = UserChangeRequest.builder()
            .existingPasskey(user.passkey())
            .foldingUserName("-folding.Name_2")
            .passkey(user.passkey())
            .hardwareId(hardware.id())
            .userId(user.id())
            .liveStatsLink("https://www.google.ie")
            .build();

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
    void whenValidating_givenNullFoldingUserName_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final User user = generateUser(hardware);

        final UserChangeRequest userChange = UserChangeRequest.builder()
            .existingPasskey(user.passkey())
            .foldingUserName(null)
            .passkey(user.passkey())
            .hardwareId(hardware.id())
            .userId(user.id())
            .liveStatsLink("https://www.google.ie")
            .build();

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createUser(user);

        final UserChangeValidator userChangeValidator = new UserChangeValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(() -> userChangeValidator.validate(userChange), ValidationException.class);
        assertThat(e.getValidationFailure().getErrors())
            .containsOnly("Field 'foldingUserName' must have at least one alphanumeric character, or an underscore, period or hyphen");
    }

    @Test
    void whenValidating_givenInvalidFoldingUserName_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final User user = generateUser(hardware);

        final UserChangeRequest userChange = UserChangeRequest.builder()
            .existingPasskey(user.passkey())
            .foldingUserName("folding*Name")
            .passkey(user.passkey())
            .hardwareId(hardware.id())
            .userId(user.id())
            .liveStatsLink("https://www.google.ie")
            .build();

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createUser(user);

        final UserChangeValidator userChangeValidator = new UserChangeValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(() -> userChangeValidator.validate(userChange), ValidationException.class);
        assertThat(e.getValidationFailure().getErrors())
            .containsOnly("Field 'foldingUserName' must have at least one alphanumeric character, or an underscore, period or hyphen");
    }

    @Test
    void whenValidating_givenNullPasskey_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final User user = generateUser(hardware);

        final UserChangeRequest userChange = UserChangeRequest.builder()
            .existingPasskey(user.passkey())
            .foldingUserName(user.foldingUserName())
            .passkey(null)
            .hardwareId(hardware.id())
            .userId(user.id())
            .liveStatsLink("https://www.google.ie")
            .build();

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createUser(user);

        final UserChangeValidator userChangeValidator = new UserChangeValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(() -> userChangeValidator.validate(userChange), ValidationException.class);
        assertThat(e.getValidationFailure().getErrors())
            .containsOnly("Field 'passkey' must be 32 characters long and include only alphanumeric characters");
    }

    @Test
    void whenValidating_givenInvalidPasskey_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final User user = generateUser(hardware);

        final UserChangeRequest userChange = UserChangeRequest.builder()
            .existingPasskey(user.passkey())
            .foldingUserName(user.foldingUserName())
            .passkey("DummyPasskey1234567890123456789*")
            .hardwareId(hardware.id())
            .userId(user.id())
            .liveStatsLink("https://www.google.ie")
            .build();

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createUser(user);

        final UserChangeValidator userChangeValidator = new UserChangeValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(() -> userChangeValidator.validate(userChange), ValidationException.class);
        assertThat(e.getValidationFailure().getErrors())
            .containsOnly("Field 'passkey' must be 32 characters long and include only alphanumeric characters");
    }

    @Test
    void whenValidating_givenNullLiveStatsLink_thenSuccessResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final User user = generateUser(hardware);

        final UserChangeRequest userChange = UserChangeRequest.builder()
            .existingPasskey(user.passkey())
            .foldingUserName(user.foldingUserName())
            .passkey(user.passkey())
            .hardwareId(hardware.id())
            .userId(user.id())
            .liveStatsLink(null)
            .build();

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

        final UserChangeRequest userChange = UserChangeRequest.builder()
            .existingPasskey(user.passkey())
            .foldingUserName(user.foldingUserName())
            .passkey(user.passkey())
            .hardwareId(hardware.id())
            .userId(user.id())
            .liveStatsLink("invalidUrl")
            .build();

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createUser(user);

        final UserChangeValidator userChangeValidator = new UserChangeValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(() -> userChangeValidator.validate(userChange), ValidationException.class);
        assertThat(e.getValidationFailure().getErrors())
            .containsOnly("Field 'liveStatsLink' is not a valid link: 'invalidUrl'");
    }

    @Test
    void whenValidating_givenInvalidHardwareId_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final User user = generateUser(hardware);

        final UserChangeRequest userChange = UserChangeRequest.builder()
            .existingPasskey(user.passkey())
            .foldingUserName(user.foldingUserName())
            .passkey(user.passkey())
            .hardwareId(-1)
            .userId(user.id())
            .liveStatsLink("https://www.google.ie")
            .build();

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createUser(user);

        final UserChangeValidator userChangeValidator = new UserChangeValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(() -> userChangeValidator.validate(userChange), ValidationException.class);
        assertThat(e.getValidationFailure().getErrors())
            .containsOnly(String.format("Field 'hardwareId' must be one of: [%s: %s]", hardware.id(), hardware.hardwareName()));
    }

    @Test
    void whenValidating_givenNoHardwareExists_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final User user = generateUser(hardware);

        final UserChangeRequest userChange = UserChangeRequest.builder()
            .existingPasskey(user.passkey())
            .foldingUserName(user.foldingUserName())
            .passkey(user.passkey())
            .hardwareId(-1)
            .userId(user.id())
            .liveStatsLink("https://www.google.ie")
            .build();

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createUser(user);

        final UserChangeValidator userChangeValidator = new UserChangeValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(() -> userChangeValidator.validate(userChange), ValidationException.class);
        assertThat(e.getValidationFailure().getErrors())
            .containsOnly("No hardwares exist on the system");
    }

    @Test
    void whenValidating_givenInvalidUserId_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final User user = generateUser(hardware);

        final UserChangeRequest userChange = UserChangeRequest.builder()
            .existingPasskey(user.passkey())
            .foldingUserName(user.foldingUserName())
            .passkey(user.passkey())
            .hardwareId(hardware.id())
            .userId(-1)
            .liveStatsLink("https://www.google.ie")
            .build();

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createUser(user);

        final UserChangeValidator userChangeValidator = new UserChangeValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(() -> userChangeValidator.validate(userChange), ValidationException.class);
        assertThat(e.getValidationFailure().getErrors())
            .containsOnly(String.format("Field 'userId' must be one of: [%s: %s]", user.id(), user.displayName()));
    }

    @Test
    void whenValidating_givenNoUsersExist_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final User user = generateUser(hardware);

        final UserChangeRequest userChange = UserChangeRequest.builder()
            .existingPasskey(user.passkey())
            .foldingUserName(user.foldingUserName())
            .passkey(user.passkey())
            .hardwareId(hardware.id())
            .userId(-1)
            .liveStatsLink("https://www.google.ie")
            .build();

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);

        final UserChangeValidator userChangeValidator = new UserChangeValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(() -> userChangeValidator.validate(userChange), ValidationException.class);
        assertThat(e.getValidationFailure().getErrors())
            .containsOnly("No users exist on the system");
    }

    @Test
    void whenValidating_givenExistingPasskeyDoesNotMatchUserPasskey_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final User user = generateUser(hardware);

        final UserChangeRequest userChange = UserChangeRequest.builder()
            .existingPasskey("DummyPasskey11111111111111111111")
            .foldingUserName(user.foldingUserName())
            .passkey(user.passkey())
            .hardwareId(hardware.id())
            .userId(user.id())
            .liveStatsLink("https://www.google.ie")
            .build();

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createUser(user);

        final UserChangeValidator userChangeValidator = new UserChangeValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(() -> userChangeValidator.validate(userChange), ValidationException.class);
        assertThat(e.getValidationFailure().getErrors())
            .containsOnly("Field 'existingPasskey' does not match the existing passkey for the user");
    }

    @Test
    void whenValidating_givenUserDoesNotNeedChange_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final User user = generateUser(hardware);

        final UserChangeRequest userChange = UserChangeRequest.builder()
            .existingPasskey(user.passkey())
            .foldingUserName(user.foldingUserName())
            .passkey(user.passkey())
            .hardwareId(hardware.id())
            .userId(user.id())
            .liveStatsLink(user.liveStatsLink())
            .build();

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createUser(user);

        final UserChangeValidator userChangeValidator = new UserChangeValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(() -> userChangeValidator.validate(userChange), ValidationException.class);
        assertThat(e.getValidationFailure().getErrors())
            .containsOnly("User already has the values supplied in UserChangeRequest");
    }

    @Test
    void whenValidating_givenMatchingUserChangeAlreadyExists_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final User user = generateUser(hardware);

        final UserChangeRequest userChange = UserChangeRequest.builder()
            .existingPasskey(user.passkey())
            .foldingUserName(user.foldingUserName())
            .passkey(user.passkey())
            .hardwareId(hardware.id())
            .userId(user.id())
            .liveStatsLink("https://www.google.ie")
            .build();

        final UserChange existingUserChange = UserChange.createNow(
            user,
            User.create(
                user.id(),
                user.foldingUserName(),
                user.displayName(),
                user.passkey(),
                user.category(),
                user.profileLink(),
                "https://www.google.ie",
                user.hardware(),
                user.team(),
                user.userIsCaptain()
            ),
            UserChangeState.APPROVED_NEXT_MONTH
        );

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createUser(user);
        foldingRepository.createUserChange(existingUserChange);

        final UserChangeValidator userChangeValidator = new UserChangeValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final ConflictException e = catchThrowableOfType(() -> userChangeValidator.validate(userChange), ConflictException.class);
        assertThat(e.getConflictFailure().getConflictingAttributes())
            .containsOnly(
                "foldingUserName",
                "passkey",
                "liveStatsLink",
                "hardwareId"
            );

        assertThat(e.getConflictFailure().getConflictingObject())
            .isNotNull();
    }

    @Test
    void whenValidating_givenMatchingUserChangeExistsWithDifferentLink_thenSuccessResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final User user = generateUser(hardware);

        final UserChangeRequest userChange = UserChangeRequest.builder()
            .existingPasskey(user.passkey())
            .foldingUserName(user.foldingUserName())
            .passkey(user.passkey())
            .hardwareId(hardware.id())
            .userId(user.id())
            .liveStatsLink("https://www.google.ie")
            .build();

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
                user.userIsCaptain()
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

        final UserChangeRequest userChange = UserChangeRequest.builder()
            .existingPasskey(user.passkey())
            .foldingUserName(user.foldingUserName())
            .passkey(user.passkey())
            .hardwareId(hardware.id())
            .userId(user.id())
            .liveStatsLink("https://www.google.ie")
            .build();

        final UserChange existingUserChange = UserChange.createNow(
            user,
            User.create(
                99,
                user.foldingUserName(),
                user.displayName(),
                user.passkey(),
                user.category(),
                user.profileLink(),
                "https://www.google.ie",
                user.hardware(),
                user.team(),
                user.userIsCaptain()
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

        final UserChangeRequest userChange = UserChangeRequest.builder()
            .existingPasskey(user.passkey())
            .foldingUserName(user.foldingUserName())
            .passkey("DummyPasskey12345678901234567891")
            .hardwareId(hardware.id())
            .userId(user.id())
            .liveStatsLink("https://www.google.ie")
            .build();

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createUser(user);

        final UserChangeValidator userChangeValidator = new UserChangeValidator(foldingRepository, new NoUnitsFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(() -> userChangeValidator.validate(userChange), ValidationException.class);
        assertThat(e.getValidationFailure().getErrors())
            .containsOnly(
                String.format(
                    "User '%s' has 0 Work Units with passkey '%s', there must be at least one completed Work Unit before adding the user",
                    userChange.getFoldingUserName(), userChange.getPasskey())
            );
    }

    @Test
    void whenValidating_givenFoldingUserNameChange_andNewUserHasNoWorkUnitsCompleted_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final User user = generateUser(hardware);

        final UserChangeRequest userChange = UserChangeRequest.builder()
            .existingPasskey(user.passkey())
            .foldingUserName("-folding.Name_2")
            .passkey(user.passkey())
            .hardwareId(hardware.id())
            .userId(user.id())
            .liveStatsLink("https://www.google.ie")
            .build();

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createUser(user);

        final UserChangeValidator userChangeValidator = new UserChangeValidator(foldingRepository, new NoUnitsFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(() -> userChangeValidator.validate(userChange), ValidationException.class);
        assertThat(e.getValidationFailure().getErrors())
            .containsOnly(
                String.format(
                    "User '%s' has 0 Work Units with passkey '%s', there must be at least one completed Work Unit before adding the user",
                    userChange.getFoldingUserName(), userChange.getPasskey())
            );
    }

    @Test
    void whenValidating_givenUserWorkUnitsCannotBeAccessed_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final User user = generateUser(hardware);

        final UserChangeRequest userChange = UserChangeRequest.builder()
            .existingPasskey(user.passkey())
            .foldingUserName(user.foldingUserName())
            .passkey("DummyPasskey12345678901234567891")
            .hardwareId(hardware.id())
            .userId(user.id())
            .liveStatsLink("https://www.google.ie")
            .build();

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createUser(user);

        final UserChangeValidator userChangeValidator = new UserChangeValidator(foldingRepository, new ExternalConnectionFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(() -> userChangeValidator.validate(userChange), ValidationException.class);
        assertThat(e.getValidationFailure().getErrors())
            .containsOnly(
                String.format("Unable to connect to 'https://www.google.com' to check stats for user '%s': Error connecting",
                    userChange.getFoldingUserName())
            );
    }

    @Test
    void whenValidating_givenUnexpectedErrorWhenAccessingUserWorkUnits_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final User user = generateUser(hardware);

        final UserChangeRequest userChange = UserChangeRequest.builder()
            .existingPasskey(user.passkey())
            .foldingUserName(user.foldingUserName())
            .passkey("DummyPasskey12345678901234567891")
            .hardwareId(hardware.id())
            .userId(user.id())
            .liveStatsLink("https://www.google.ie")
            .build();

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createUser(user);

        final UserChangeValidator userChangeValidator = new UserChangeValidator(foldingRepository, new UnexpectedExceptionFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(() -> userChangeValidator.validate(userChange), ValidationException.class);
        assertThat(e.getValidationFailure().getErrors())
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
            true
        );
    }
}

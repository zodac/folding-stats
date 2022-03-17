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

import me.zodac.folding.api.FoldingRepository;
import me.zodac.folding.api.exception.ConflictException;
import me.zodac.folding.api.exception.ValidationException;
import me.zodac.folding.api.tc.Category;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.HardwareMake;
import me.zodac.folding.api.tc.HardwareType;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.bean.tc.validation.retriever.ExternalConnectionFoldingStatsRetriever;
import me.zodac.folding.bean.tc.validation.retriever.NoUnitsFoldingStatsRetriever;
import me.zodac.folding.bean.tc.validation.retriever.UnexpectedExceptionFoldingStatsRetriever;
import me.zodac.folding.bean.tc.validation.retriever.ValidFoldingStatsRetriever;
import me.zodac.folding.rest.api.tc.request.UserRequest;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link UserValidator}.
 */
class UserValidatorTest {

    private static int hardwareId = 1;
    private static int teamId = 1;
    private static int userId = 1;

    @Test
    void whenValidatingCreate_givenValidUser_thenSuccessResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final Team team = generateTeam();

        final UserRequest user = UserRequest.builder()
            .foldingUserName("-folding.Name_1")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU.toString())
            .profileLink("https://www.google.com")
            .liveStatsLink("https://www.google.com")
            .userIsCaptain(true)
            .hardwareId(hardware.id())
            .teamId(team.getId())
            .build();

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createTeam(team);

        final UserValidator userValidator = new UserValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final User response = userValidator.create(user);

        assertThat(response)
            .as("Expected validation to pass")
            .isNotNull();
    }

    @Test
    void whenValidatingCreate_givenUserWithInvalidCategory_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final Team team = generateTeam();

        final UserRequest user = UserRequest.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.INVALID.toString())
            .profileLink("https://www.google.com")
            .liveStatsLink("https://www.google.com")
            .userIsCaptain(true)
            .hardwareId(hardware.id())
            .teamId(team.getId())
            .build();

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createTeam(team);

        final UserValidator userValidator = new UserValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(() -> userValidator.create(user), ValidationException.class);
        assertThat(e.getValidationFailure().getErrors())
            .containsOnly("Field 'category' must be one of: [AMD_GPU, NVIDIA_GPU, WILDCARD]");
    }

    @Test
    void whenValidatingCreate_givenUserWithNullFoldingUserName_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final Team team = generateTeam();

        final UserRequest user = UserRequest.builder()
            .foldingUserName(null)
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU.toString())
            .profileLink("https://www.google.com")
            .liveStatsLink("https://www.google.com")
            .userIsCaptain(true)
            .hardwareId(hardware.id())
            .teamId(team.getId())
            .build();
        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createTeam(team);

        final UserValidator userValidator = new UserValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(() -> userValidator.create(user), ValidationException.class);
        assertThat(e.getValidationFailure().getErrors())
            .containsOnly("Field 'foldingUserName' must have at least one alphanumeric character, or an underscore, period or hyphen");
    }

    @Test
    void whenValidatingCreate_givenUserWithInvalidFoldingUserName_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final Team team = generateTeam();

        final UserRequest user = UserRequest.builder()
            .foldingUserName("folding*Name")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU.toString())
            .profileLink("https://www.google.com")
            .liveStatsLink("https://www.google.com")
            .userIsCaptain(true)
            .hardwareId(hardware.id())
            .teamId(team.getId())
            .build();

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createTeam(team);

        final UserValidator userValidator = new UserValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(() -> userValidator.create(user), ValidationException.class);
        assertThat(e.getValidationFailure().getErrors())
            .containsOnly("Field 'foldingUserName' must have at least one alphanumeric character, or an underscore, period or hyphen");
    }

    @Test
    void whenValidatingCreate_givenUserWithNullDisplayName_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final Team team = generateTeam();

        final UserRequest user = UserRequest.builder()
            .foldingUserName("user")
            .displayName(null)
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU.toString())
            .profileLink("https://www.google.com")
            .liveStatsLink("https://www.google.com")
            .userIsCaptain(true)
            .hardwareId(hardware.id())
            .teamId(team.getId())
            .build();

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createTeam(team);

        final UserValidator userValidator = new UserValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(() -> userValidator.create(user), ValidationException.class);
        assertThat(e.getValidationFailure().getErrors())
            .containsOnly("Field 'displayName' must not be empty");
    }

    @Test
    void whenValidatingCreate_givenUserWithNullPasskey_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final Team team = generateTeam();

        final UserRequest user = UserRequest.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey(null)
            .category(Category.NVIDIA_GPU.toString())
            .profileLink("https://www.google.com")
            .liveStatsLink("https://www.google.com")
            .userIsCaptain(true)
            .hardwareId(hardware.id())
            .teamId(team.getId())
            .build();

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createTeam(team);

        final UserValidator userValidator = new UserValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(() -> userValidator.create(user), ValidationException.class);
        assertThat(e.getValidationFailure().getErrors())
            .containsOnly("Field 'passkey' must be 32 characters long and include only alphanumeric characters");
    }

    @Test
    void whenValidatingCreate_givenUserWithPasskeyContainingInvalidCharacters_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final Team team = generateTeam();

        final UserRequest user = UserRequest.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey1234567890123456789*")
            .category(Category.NVIDIA_GPU.toString())
            .profileLink("https://www.google.com")
            .liveStatsLink("https://www.google.com")
            .userIsCaptain(true)
            .hardwareId(hardware.id())
            .teamId(team.getId())
            .build();

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createTeam(team);

        final UserValidator userValidator = new UserValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(() -> userValidator.create(user), ValidationException.class);
        assertThat(e.getValidationFailure().getErrors())
            .containsOnly("Field 'passkey' must be 32 characters long and include only alphanumeric characters");
    }

    @Test
    void whenValidatingCreate_givenUserWithNullProfileLink_thenSuccessResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final Team team = generateTeam();

        final UserRequest user = UserRequest.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU.toString())
            .profileLink(null)
            .liveStatsLink("https://www.google.com")
            .userIsCaptain(true)
            .hardwareId(hardware.id())
            .teamId(team.getId())
            .build();

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createTeam(team);

        final UserValidator userValidator = new UserValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final User response = userValidator.create(user);

        assertThat(response)
            .as("Expected validation to pass")
            .isNotNull();
    }

    @Test
    void whenValidatingCreate_givenUserWithInvalidProfileLink_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final Team team = generateTeam();

        final UserRequest user = UserRequest.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU.toString())
            .profileLink("invalidUrl")
            .liveStatsLink("https://www.google.com")
            .userIsCaptain(true)
            .hardwareId(hardware.id())
            .teamId(team.getId())
            .build();

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createTeam(team);

        final UserValidator userValidator = new UserValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(() -> userValidator.create(user), ValidationException.class);
        assertThat(e.getValidationFailure().getErrors())
            .containsOnly("Field 'profileLink' is not a valid link: 'invalidUrl'");
    }

    @Test
    void whenValidatingCreate_givenUserWithNullLiveStatsLink_thenSuccessResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final Team team = generateTeam();

        final UserRequest user = UserRequest.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU.toString())
            .profileLink("https://www.google.com")
            .liveStatsLink(null)
            .userIsCaptain(true)
            .hardwareId(hardware.id())
            .teamId(team.getId())
            .build();

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createTeam(team);

        final UserValidator userValidator = new UserValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final User response = userValidator.create(user);

        assertThat(response)
            .as("Expected validation to pass")
            .isNotNull();
    }

    @Test
    void whenValidatingCreate_givenUserWithInvalidLiveStatsLink_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final Team team = generateTeam();

        final UserRequest user = UserRequest.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU.toString())
            .profileLink("https://www.google.com")
            .liveStatsLink("invalidUrl")
            .userIsCaptain(true)
            .hardwareId(hardware.id())
            .teamId(team.getId())
            .build();

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createTeam(team);

        final UserValidator userValidator = new UserValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(() -> userValidator.create(user), ValidationException.class);
        assertThat(e.getValidationFailure().getErrors())
            .containsOnly("Field 'liveStatsLink' is not a valid link: 'invalidUrl'");
    }

    @Test
    void whenValidatingCreate_givenOtherUserAlreadyExists_thenSuccessResponseIsReturned() {
        final Hardware otherHardware = generateHardware();
        final Team otherTeam = generateTeam();

        final User otherUser = User.builder()
            .foldingUserName("user1")
            .displayName("user1")
            .passkey("DummyPasskey12345678901234567891")
            .category(Category.NVIDIA_GPU)
            .profileLink("https://www.google.com")
            .liveStatsLink("https://www.google.com")
            .userIsCaptain(false)
            .hardware(otherHardware)
            .team(otherTeam)
            .build();

        final Hardware hardware = generateHardware();
        final Team team = generateTeam();

        final UserRequest user = UserRequest.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU.toString())
            .profileLink("https://www.google.com")
            .liveStatsLink("https://www.google.com")
            .userIsCaptain(true)
            .hardwareId(hardware.id())
            .teamId(team.getId())
            .build();

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createHardware(otherHardware);
        foldingRepository.createTeam(team);
        foldingRepository.createTeam(otherTeam);
        foldingRepository.createUser(otherUser);

        final UserValidator userValidator = new UserValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final User response = userValidator.create(user);

        assertThat(response)
            .as("Expected validation to pass")
            .isNotNull();
    }

    @Test
    void whenValidatingCreate_givenUserWithFoldingUserNameAndPasskeyAlreadyExists_thenFailureResponseIsReturned() {
        final Hardware otherHardware = generateHardware();
        final Team otherTeam = generateTeam();

        final User otherUser = User.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU)
            .profileLink("https://www.google.com")
            .liveStatsLink("https://www.google.com")
            .userIsCaptain(true)
            .hardware(otherHardware)
            .team(otherTeam)
            .build();

        final Hardware hardware = generateHardware();
        final Team team = generateTeam();

        final UserRequest user = UserRequest.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU.toString())
            .profileLink("https://www.google.com")
            .liveStatsLink("https://www.google.com")
            .userIsCaptain(true)
            .hardwareId(hardware.id())
            .teamId(team.getId())
            .build();

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createHardware(otherHardware);
        foldingRepository.createTeam(team);
        foldingRepository.createTeam(otherTeam);
        foldingRepository.createUser(otherUser);

        final UserValidator userValidator = new UserValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final ConflictException e = catchThrowableOfType(() -> userValidator.create(user), ConflictException.class);
        assertThat(e.getConflictFailure().getConflictingAttributes())
            .containsOnly(
                "foldingUserName",
                "passkey"
            );

        assertThat(e.getConflictFailure().getConflictingObject())
            .isNotNull();
    }

    @Test
    void whenValidatingCreate_givenUserWithMisMatchingHardwareMakeAndCategory_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final Team team = generateTeam();

        final UserRequest user = UserRequest.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.AMD_GPU.toString())
            .profileLink("https://www.google.com")
            .liveStatsLink("https://www.google.com")
            .userIsCaptain(true)
            .hardwareId(hardware.id())
            .teamId(team.getId())
            .build();

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createTeam(team);

        final UserValidator userValidator = new UserValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(() -> userValidator.create(user), ValidationException.class);
        assertThat(e.getValidationFailure().getErrors())
            .containsOnly(String.format("Category '%s' cannot be filled by hardware of make '%s', must be one of: %s", Category.AMD_GPU,
                hardware.hardwareMake(), Category.AMD_GPU.supportedHardwareMakes()));
    }

    @Test
    void whenValidatingCreate_givenUserWithMisMatchingHardwareTypeAndCategory_thenFailureResponseIsReturned() {
        final Hardware hardware = Hardware.create(
            hardwareId++,
            "hardwareName",
            "displayName",
            HardwareMake.AMD,
            HardwareType.CPU,
            1.00D,
            1L
        );
        final Team team = generateTeam();

        final UserRequest user = UserRequest.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.AMD_GPU.toString())
            .profileLink("https://www.google.com")
            .liveStatsLink("https://www.google.com")
            .userIsCaptain(true)
            .hardwareId(hardware.id())
            .teamId(team.getId())
            .build();

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createTeam(team);

        final UserValidator userValidator = new UserValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(() -> userValidator.create(user), ValidationException.class);
        assertThat(e.getValidationFailure().getErrors())
            .containsOnly(String.format("Category '%s' cannot be filled by hardware of type '%s', must be one of: %s", Category.AMD_GPU,
                hardware.hardwareType(), Category.AMD_GPU.supportedHardwareTypes()));
    }

    @Test
    void whenValidatingCreate_givenUserWithHardwareWithNonExistingId_thenFailureResponseIsReturned() {
        final Hardware hardware = Hardware.create(
            99,
            "hardwareName",
            "displayName",
            HardwareMake.NVIDIA,
            HardwareType.GPU,
            1.00D,
            1L
        );
        final Team team = generateTeam();

        final UserRequest user = UserRequest.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU.toString())
            .profileLink("https://www.google.com")
            .liveStatsLink("https://www.google.com")
            .userIsCaptain(true)
            .hardwareId(1)
            .teamId(team.getId())
            .build();

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createTeam(team);

        final UserValidator userValidator = new UserValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(() -> userValidator.create(user), ValidationException.class);
        assertThat(e.getValidationFailure().getErrors())
            .containsOnly(String.format("Field 'hardwareId' must be one of: [%s: %s]", hardware.id(), hardware.hardwareName()));
    }

    @Test
    void whenValidatingCreate_givenNoHardwareExistsOnSystem_thenFailureResponseIsReturned() {
        final Team team = generateTeam();

        final UserRequest user = UserRequest.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU.toString())
            .profileLink("https://www.google.com")
            .liveStatsLink("https://www.google.com")
            .userIsCaptain(true)
            .hardwareId(1)
            .teamId(team.getId())
            .build();

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createTeam(team);

        final UserValidator userValidator = new UserValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(() -> userValidator.create(user), ValidationException.class);
        assertThat(e.getValidationFailure().getErrors())
            .containsOnly("No hardwares exist on the system");
    }

    @Test
    void whenValidatingCreate_givenUserWithTeamWithNonExistingId_thenFailureResponseIsReturned() {
        final Hardware hardware = Hardware.create(
            99,
            "hardwareName",
            "displayName",
            HardwareMake.NVIDIA,
            HardwareType.GPU,
            1.00D,
            1L
        );
        final Team team = generateTeam();

        final UserRequest user = UserRequest.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU.toString())
            .profileLink("https://www.google.com")
            .liveStatsLink("https://www.google.com")
            .userIsCaptain(true)
            .hardwareId(hardware.id())
            .teamId(1)
            .build();

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createTeam(team);

        final UserValidator userValidator = new UserValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(() -> userValidator.create(user), ValidationException.class);
        assertThat(e.getValidationFailure().getErrors())
            .containsOnly(String.format("Field 'teamId' must be one of: [%s: %s]", team.getId(), team.getTeamName()));
    }

    @Test
    void whenValidatingCreate_givenNoTeamsExistsOnSystem_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();

        final UserRequest user = UserRequest.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU.toString())
            .profileLink("https://www.google.com")
            .liveStatsLink("https://www.google.com")
            .userIsCaptain(true)
            .hardwareId(hardware.id())
            .teamId(1)
            .build();

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);

        final UserValidator userValidator = new UserValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(() -> userValidator.create(user), ValidationException.class);
        assertThat(e.getValidationFailure().getErrors())
            .containsOnly("No teams exist on the system");
    }

    @Test
    void whenValidatingCreate_givenUserIsCaptain_andTeamAlreadyContainsCaptain_thenSuccessResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final Team team = generateTeam();

        final User currentCaptain = User.builder()
            .foldingUserName("user1")
            .displayName("user1")
            .passkey("DummyPasskey12345678901234567891")
            .category(Category.WILDCARD)
            .profileLink("https://www.google.com")
            .liveStatsLink("https://www.google.com")
            .userIsCaptain(true)
            .hardware(hardware)
            .team(team)
            .build();

        final UserRequest user = UserRequest.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU.toString())
            .profileLink("https://www.google.com")
            .liveStatsLink("https://www.google.com")
            .userIsCaptain(true)
            .hardwareId(hardware.id())
            .teamId(team.getId())
            .build();

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createTeam(team);
        foldingRepository.createUser(currentCaptain);

        final UserValidator userValidator = new UserValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final User response = userValidator.create(user);

        assertThat(response)
            .as("Expected validation to pass")
            .isNotNull();
    }

    @Test
    void whenValidatingCreate_givenUserIsNotCaptain_andTeamAlreadyContainsCaptain_thenSuccessResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final Team team = generateTeam();

        final User currentCaptain = User.builder()
            .foldingUserName("user1")
            .displayName("user1")
            .passkey("DummyPasskey12345678901234567891")
            .category(Category.WILDCARD)
            .profileLink("https://www.google.com")
            .liveStatsLink("https://www.google.com")
            .userIsCaptain(true)
            .hardware(hardware)
            .team(team)
            .build();

        final UserRequest user = UserRequest.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU.toString())
            .profileLink("https://www.google.com")
            .liveStatsLink("https://www.google.com")
            .userIsCaptain(false)
            .hardwareId(hardware.id())
            .teamId(team.getId())
            .build();

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createTeam(team);
        foldingRepository.createUser(currentCaptain);

        final UserValidator userValidator = new UserValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final User response = userValidator.create(user);

        assertThat(response)
            .as("Expected validation to pass")
            .isNotNull();
    }

    @Test
    void whenValidatingCreate_givenTeamAlreadyHasMaximumTotalUsers_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final Team team = generateTeam();

        final UserRequest user = UserRequest.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU.toString())
            .profileLink("https://www.google.com")
            .liveStatsLink("https://www.google.com")
            .userIsCaptain(false)
            .hardwareId(hardware.id())
            .teamId(team.getId())
            .build();

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createTeam(team);

        for (int i = 0; i < Category.maximumPermittedAmountForAllCategories(); i++) {
            foldingRepository.createUser(
                User.builder()
                    .id(userId++)
                    .foldingUserName("user" + i)
                    .displayName("user" + i)
                    .passkey("DummyPasskey12345678901234567891")
                    .category(Category.WILDCARD)
                    .profileLink("https://www.google.com")
                    .liveStatsLink("https://www.google.com")
                    .userIsCaptain(false)
                    .hardware(hardware)
                    .team(team)
                    .build()
            );
        }

        final UserValidator userValidator = new UserValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(() -> userValidator.create(user), ValidationException.class);
        assertThat(e.getValidationFailure().getErrors())
            .containsOnly(String.format("Team '%1$s' has %2$s users, maximum permitted is %2$s", team.getTeamName(),
                Category.maximumPermittedAmountForAllCategories()));
    }

    @Test
    void whenValidatingCreate_givenTeamAlreadyHasUsersInAnotherCategory_thenSuccessResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final Team team = generateTeam();

        final UserRequest user = UserRequest.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU.toString())
            .profileLink("https://www.google.com")
            .liveStatsLink("https://www.google.com")
            .userIsCaptain(true)
            .hardwareId(hardware.id())
            .teamId(team.getId())
            .build();

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createTeam(team);

        for (int i = 0; i < Category.AMD_GPU.permittedUsers(); i++) {
            foldingRepository.createUser(
                User.builder()
                    .foldingUserName("user" + 0)
                    .displayName("user" + 0)
                    .passkey("DummyPasskey12345678901234567891")
                    .category(Category.AMD_GPU)
                    .profileLink("https://www.google.com")
                    .liveStatsLink("https://www.google.com")
                    .userIsCaptain(false)
                    .hardware(hardware)
                    .team(team)
                    .build()
            );
        }

        final UserValidator userValidator = new UserValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final User response = userValidator.create(user);

        assertThat(response)
            .as("Expected validation to pass")
            .isNotNull();
    }

    @Test
    void whenValidatingCreate_givenTeamAlreadyHasMaximumUsersInCategory_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final Team team = generateTeam();

        final UserRequest user = UserRequest.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU.toString())
            .profileLink("https://www.google.com")
            .liveStatsLink("https://www.google.com")
            .userIsCaptain(true)
            .hardwareId(hardware.id())
            .teamId(team.getId())
            .build();

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createTeam(team);

        for (int i = 0; i < Category.NVIDIA_GPU.permittedUsers(); i++) {
            foldingRepository.createUser(
                User.builder()
                    .foldingUserName("user" + 0)
                    .displayName("user" + 0)
                    .passkey("DummyPasskey12345678901234567891")
                    .category(Category.NVIDIA_GPU)
                    .profileLink("https://www.google.com")
                    .liveStatsLink("https://www.google.com")
                    .userIsCaptain(false)
                    .hardware(hardware)
                    .team(team)
                    .build()
            );
        }

        final UserValidator userValidator = new UserValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(() -> userValidator.create(user), ValidationException.class);
        assertThat(e.getValidationFailure().getErrors())
            .containsOnly(
                String.format("Team '%1$s' already has %3$s users in category '%2$s', only %3$s permitted", team.getTeamName(), Category.NVIDIA_GPU,
                    Category.NVIDIA_GPU.permittedUsers()));
    }

    @Test
    void whenValidatingCreate_givenUserHasNoWorkUnitsCompleted_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final Team team = generateTeam();

        final UserRequest user = UserRequest.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU.toString())
            .profileLink("https://www.google.com")
            .liveStatsLink("https://www.google.com")
            .userIsCaptain(true)
            .hardwareId(hardware.id())
            .teamId(team.getId())
            .build();

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createTeam(team);

        final UserValidator userValidator = new UserValidator(foldingRepository, new NoUnitsFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(() -> userValidator.create(user), ValidationException.class);
        assertThat(e.getValidationFailure().getErrors())
            .containsOnly(
                String.format("User '%s' has 0 Work Units with passkey '%s', there must be at least one completed Work Unit before adding the user",
                    user.getDisplayName(), user.getPasskey())
            );
    }

    @Test
    void whenValidatingCreate_givenUserWorkUnitsCannotBeAccessed_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final Team team = generateTeam();

        final UserRequest user = UserRequest.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU.toString())
            .profileLink("https://www.google.com")
            .liveStatsLink("https://www.google.com")
            .userIsCaptain(true)
            .hardwareId(hardware.id())
            .teamId(team.getId())
            .build();

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createTeam(team);

        final UserValidator userValidator = new UserValidator(foldingRepository, new ExternalConnectionFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(() -> userValidator.create(user), ValidationException.class);
        assertThat(e.getValidationFailure().getErrors())
            .containsOnly(String.format("Unable to connect to 'https://www.google.com' to check stats for user '%s': Error connecting",
                user.getDisplayName())
            );
    }

    @Test
    void whenValidatingCreate_givenUnexpectedErrorWhenAccessingUserWorkUnits_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final Team team = generateTeam();

        final UserRequest user = UserRequest.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU.toString())
            .profileLink("https://www.google.com")
            .liveStatsLink("https://www.google.com")
            .userIsCaptain(true)
            .hardwareId(hardware.id())
            .teamId(team.getId())
            .build();

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createTeam(team);

        final UserValidator userValidator = new UserValidator(foldingRepository, new UnexpectedExceptionFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(() -> userValidator.create(user), ValidationException.class);
        assertThat(e.getValidationFailure().getErrors())
            .containsOnly(String.format("Unable to check stats for user '%s': Error", user.getDisplayName()));
    }

    @Test
    void whenValidatingCreate_givenMultipleFailures_thenFailureResponseIsReturned_andAllValidationErrorsAreReturned() {
        final Hardware hardware = generateHardware();
        final Team team = generateTeam();

        final UserRequest user = UserRequest.builder()
            .foldingUserName(null)
            .displayName(null)
            .passkey("invalid")
            .category("invalid")
            .profileLink("invalid")
            .liveStatsLink("invalid")
            .userIsCaptain(true)
            .hardwareId(hardware.id())
            .teamId(team.getId())
            .build();

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createTeam(team);

        final UserValidator userValidator = new UserValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(() -> userValidator.create(user), ValidationException.class);
        assertThat(e.getValidationFailure().getErrors())
            .containsOnly(
                "Field 'foldingUserName' must have at least one alphanumeric character, or an underscore, period or hyphen",
                "Field 'displayName' must not be empty",
                "Field 'passkey' must be 32 characters long and include only alphanumeric characters",
                "Field 'category' must be one of: [AMD_GPU, NVIDIA_GPU, WILDCARD]",
                "Field 'profileLink' is not a valid link: 'invalid'",
                "Field 'liveStatsLink' is not a valid link: 'invalid'"
            );
    }

    @Test
    void whenValidatingUpdate_givenValidUser_thenSuccessResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final Team team = generateTeam();

        final UserRequest user = UserRequest.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU.toString())
            .profileLink("https://www.google.com")
            .liveStatsLink("https://www.google.com")
            .userIsCaptain(true)
            .hardwareId(hardware.id())
            .teamId(team.getId())
            .build();

        final User existingUser = User.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU)
            .profileLink("https://www.google.com")
            .liveStatsLink("https://www.google.com")
            .userIsCaptain(true)
            .hardware(hardware)
            .team(team)
            .build();

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createTeam(team);
        foldingRepository.createUser(existingUser);

        final UserValidator userValidator = new UserValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final User response = userValidator.update(user, existingUser);

        assertThat(response)
            .as("Expected validation to pass")
            .isNotNull();
    }

    @Test
    void whenValidatingUpdate_givenUserWithInvalidCategory_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final Team team = generateTeam();

        final UserRequest user = UserRequest.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category("invalid")
            .profileLink("https://www.google.com")
            .liveStatsLink("https://www.google.com")
            .userIsCaptain(true)
            .hardwareId(hardware.id())
            .teamId(team.getId())
            .build();

        final User existingUser = User.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU)
            .profileLink("https://www.google.com")
            .liveStatsLink("https://www.google.com")
            .userIsCaptain(true)
            .hardware(hardware)
            .team(team)
            .build();

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createTeam(team);
        foldingRepository.createUser(existingUser);

        final UserValidator userValidator = new UserValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(() -> userValidator.update(user, existingUser), ValidationException.class);
        assertThat(e.getValidationFailure().getErrors())
            .containsOnly("Field 'category' must be one of: [AMD_GPU, NVIDIA_GPU, WILDCARD]");
    }

    @Test
    void whenValidatingUpdate_givenUserWithNullFoldingUserName_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final Team team = generateTeam();

        final UserRequest user = UserRequest.builder()
            .foldingUserName(null)
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU.toString())
            .profileLink("https://www.google.com")
            .liveStatsLink("https://www.google.com")
            .userIsCaptain(true)
            .hardwareId(hardware.id())
            .teamId(team.getId())
            .build();

        final User existingUser = User.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU)
            .profileLink("https://www.google.com")
            .liveStatsLink("https://www.google.com")
            .userIsCaptain(true)
            .hardware(hardware)
            .team(team)
            .build();

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createTeam(team);
        foldingRepository.createUser(existingUser);

        final UserValidator userValidator = new UserValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(() -> userValidator.update(user, existingUser), ValidationException.class);
        assertThat(e.getValidationFailure().getErrors())
            .containsOnly("Field 'foldingUserName' must have at least one alphanumeric character, or an underscore, period or hyphen");
    }

    @Test
    void whenValidatingUpdate_givenUserWithInvalidFoldingUserName_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final Team team = generateTeam();

        final UserRequest user = UserRequest.builder()
            .foldingUserName("folding name")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU.toString())
            .profileLink("https://www.google.com")
            .liveStatsLink("https://www.google.com")
            .userIsCaptain(true)
            .hardwareId(hardware.id())
            .teamId(team.getId())
            .build();

        final User existingUser = User.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU)
            .profileLink("https://www.google.com")
            .liveStatsLink("https://www.google.com")
            .userIsCaptain(true)
            .hardware(hardware)
            .team(team)
            .build();

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createTeam(team);
        foldingRepository.createUser(existingUser);

        final UserValidator userValidator = new UserValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(() -> userValidator.update(user, existingUser), ValidationException.class);
        assertThat(e.getValidationFailure().getErrors())
            .containsOnly("Field 'foldingUserName' must have at least one alphanumeric character, or an underscore, period or hyphen");
    }

    @Test
    void whenValidatingUpdate_givenUserWithNullDisplayName_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final Team team = generateTeam();

        final UserRequest user = UserRequest.builder()
            .foldingUserName("user")
            .displayName(null)
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU.toString())
            .profileLink("https://www.google.com")
            .liveStatsLink("https://www.google.com")
            .userIsCaptain(true)
            .hardwareId(hardware.id())
            .teamId(team.getId())
            .build();

        final User existingUser = User.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU)
            .profileLink("https://www.google.com")
            .liveStatsLink("https://www.google.com")
            .userIsCaptain(true)
            .hardware(hardware)
            .team(team)
            .build();

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createTeam(team);
        foldingRepository.createUser(existingUser);

        final UserValidator userValidator = new UserValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(() -> userValidator.update(user, existingUser), ValidationException.class);
        assertThat(e.getValidationFailure().getErrors())
            .containsOnly("Field 'displayName' must not be empty");
    }

    @Test
    void whenValidatingUpdate_givenUserWithNullPasskey_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final Team team = generateTeam();

        final UserRequest user = UserRequest.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey(null)
            .category(Category.NVIDIA_GPU.toString())
            .profileLink("https://www.google.com")
            .liveStatsLink("https://www.google.com")
            .userIsCaptain(true)
            .hardwareId(hardware.id())
            .teamId(team.getId())
            .build();

        final User existingUser = User.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU)
            .profileLink("https://www.google.com")
            .liveStatsLink("https://www.google.com")
            .userIsCaptain(true)
            .hardware(hardware)
            .team(team)
            .build();

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createTeam(team);
        foldingRepository.createUser(existingUser);

        final UserValidator userValidator = new UserValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(() -> userValidator.update(user, existingUser), ValidationException.class);
        assertThat(e.getValidationFailure().getErrors())
            .containsOnly("Field 'passkey' must be 32 characters long and include only alphanumeric characters");
    }

    @Test
    void whenValidatingUpdate_givenUserWithPasskeyOfInvalidLength_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final Team team = generateTeam();

        final UserRequest user = UserRequest.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("1234")
            .category(Category.NVIDIA_GPU.toString())
            .profileLink("https://www.google.com")
            .liveStatsLink("https://www.google.com")
            .userIsCaptain(true)
            .hardwareId(hardware.id())
            .teamId(team.getId())
            .build();

        final User existingUser = User.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU)
            .profileLink("https://www.google.com")
            .liveStatsLink("https://www.google.com")
            .userIsCaptain(true)
            .hardware(hardware)
            .team(team)
            .build();

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createTeam(team);
        foldingRepository.createUser(existingUser);

        final UserValidator userValidator = new UserValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(() -> userValidator.update(user, existingUser), ValidationException.class);
        assertThat(e.getValidationFailure().getErrors())
            .containsOnly("Field 'passkey' must be 32 characters long and include only alphanumeric characters");
    }

    @Test
    void whenValidatingUpdate_givenUserWithPasskeyContainingInvalidCharacters_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final Team team = generateTeam();

        final UserRequest user = UserRequest.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey1234567890123456789*")
            .category(Category.NVIDIA_GPU.toString())
            .profileLink("https://www.google.com")
            .liveStatsLink("https://www.google.com")
            .userIsCaptain(true)
            .hardwareId(hardware.id())
            .teamId(team.getId())
            .build();

        final User existingUser = User.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU)
            .profileLink("https://www.google.com")
            .liveStatsLink("https://www.google.com")
            .userIsCaptain(true)
            .hardware(hardware)
            .team(team)
            .build();

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createTeam(team);
        foldingRepository.createUser(existingUser);

        final UserValidator userValidator = new UserValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(() -> userValidator.update(user, existingUser), ValidationException.class);
        assertThat(e.getValidationFailure().getErrors())
            .containsOnly("Field 'passkey' must be 32 characters long and include only alphanumeric characters");
    }

    @Test
    void whenValidatingUpdate_givenUserWithNullProfileLink_thenSuccessResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final Team team = generateTeam();

        final UserRequest user = UserRequest.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU.toString())
            .profileLink(null)
            .liveStatsLink("https://www.google.com")
            .userIsCaptain(true)
            .hardwareId(hardware.id())
            .teamId(team.getId())
            .build();

        final User existingUser = User.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU)
            .profileLink("https://www.google.com")
            .liveStatsLink("https://www.google.com")
            .userIsCaptain(true)
            .hardware(hardware)
            .team(team)
            .build();

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createTeam(team);
        foldingRepository.createUser(existingUser);

        final UserValidator userValidator = new UserValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final User response = userValidator.update(user, existingUser);

        assertThat(response)
            .as("Expected validation to pass")
            .isNotNull();
    }

    @Test
    void whenValidatingUpdate_givenUserWithInvalidProfileLink_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final Team team = generateTeam();

        final UserRequest user = UserRequest.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU.toString())
            .profileLink("invalidUrl")
            .liveStatsLink("https://www.google.com")
            .userIsCaptain(true)
            .hardwareId(hardware.id())
            .teamId(team.getId())
            .build();

        final User existingUser = User.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU)
            .profileLink("https://www.google.com")
            .liveStatsLink("https://www.google.com")
            .userIsCaptain(true)
            .hardware(hardware)
            .team(team)
            .build();

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createTeam(team);
        foldingRepository.createUser(existingUser);

        final UserValidator userValidator = new UserValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(() -> userValidator.update(user, existingUser), ValidationException.class);
        assertThat(e.getValidationFailure().getErrors())
            .containsOnly("Field 'profileLink' is not a valid link: 'invalidUrl'");
    }

    @Test
    void whenValidatingUpdate_givenUserWithNullLiveStatsLink_thenSuccessResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final Team team = generateTeam();

        final UserRequest user = UserRequest.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU.toString())
            .profileLink("https://www.google.com")
            .liveStatsLink(null)
            .userIsCaptain(true)
            .hardwareId(hardware.id())
            .teamId(team.getId())
            .build();

        final User existingUser = User.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU)
            .profileLink("https://www.google.com")
            .liveStatsLink("https://www.google.com")
            .userIsCaptain(true)
            .hardware(hardware)
            .team(team)
            .build();

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createTeam(team);
        foldingRepository.createUser(existingUser);

        final UserValidator userValidator = new UserValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final User response = userValidator.update(user, existingUser);

        assertThat(response)
            .as("Expected validation to pass")
            .isNotNull();
    }

    @Test
    void whenValidatingUpdate_givenUserWithInvalidLiveStatsLink_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final Team team = generateTeam();

        final UserRequest user = UserRequest.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU.toString())
            .profileLink("https://www.google.com")
            .liveStatsLink("invalidUrl")
            .userIsCaptain(true)
            .hardwareId(hardware.id())
            .teamId(team.getId())
            .build();

        final User existingUser = User.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU)
            .profileLink("https://www.google.com")
            .liveStatsLink("https://www.google.com")
            .userIsCaptain(true)
            .hardware(hardware)
            .team(team)
            .build();

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createTeam(team);
        foldingRepository.createUser(existingUser);

        final UserValidator userValidator = new UserValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(() -> userValidator.update(user, existingUser), ValidationException.class);
        assertThat(e.getValidationFailure().getErrors())
            .containsOnly("Field 'liveStatsLink' is not a valid link: 'invalidUrl'");
    }

    @Test
    void whenValidatingUpdate_givenUserWithFoldingUserNameAndPasskeyAlreadyExists_thenFailureResponseIsReturned() {
        final Hardware otherHardware = generateHardware();
        final Team otherTeam = generateTeam();

        final User otherUser = User.builder()
            .id(userId++)
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU)
            .profileLink("https://www.google.com")
            .liveStatsLink("https://www.google.com")
            .userIsCaptain(true)
            .hardware(otherHardware)
            .team(otherTeam)
            .build();

        final Hardware hardware = generateHardware();
        final Team team = generateTeam();

        final UserRequest user = UserRequest.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU.toString())
            .profileLink("https://www.google.com")
            .liveStatsLink("https://www.google.com")
            .userIsCaptain(true)
            .hardwareId(hardware.id())
            .teamId(team.getId())
            .build();

        final User existingUser = User.builder()
            .id(userId++)
            .foldingUserName("differentUser")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU)
            .profileLink("https://www.google.com")
            .liveStatsLink("https://www.google.com")
            .userIsCaptain(true)
            .hardware(hardware)
            .team(team)
            .build();

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createHardware(otherHardware);
        foldingRepository.createTeam(team);
        foldingRepository.createTeam(otherTeam);
        foldingRepository.createUser(existingUser);
        foldingRepository.createUser(otherUser);

        final UserValidator userValidator = new UserValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final ConflictException e = catchThrowableOfType(() -> userValidator.update(user, existingUser), ConflictException.class);
        assertThat(e.getConflictFailure().getConflictingAttributes())
            .containsOnly(
                "foldingUserName",
                "passkey"
            );

        assertThat(e.getConflictFailure().getConflictingObject())
            .isNotNull();
    }

    @Test
    void whenValidatingUpdate_givenUserWithFoldingUserNameAndPasskeyExists_andExistingUserIsTheOneBeingUpdated_thenSuccessResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final Team team = generateTeam();
        final int commonUserId = userId++;

        final UserRequest user = UserRequest.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU.toString())
            .profileLink("https://www.google.com")
            .liveStatsLink("https://www.google.com")
            .userIsCaptain(true)
            .hardwareId(hardware.id())
            .teamId(team.getId())
            .build();

        final User existingUser = User.builder()
            .id(commonUserId)
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU)
            .profileLink("https://www.google.com")
            .liveStatsLink("https://www.google.com")
            .userIsCaptain(true)
            .hardware(hardware)
            .team(team)
            .build();

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createTeam(team);
        foldingRepository.createUser(existingUser);

        final UserValidator userValidator = new UserValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final User response = userValidator.update(user, existingUser);

        assertThat(response)
            .as("Expected validation to pass")
            .isNotNull();
    }

    @Test
    void whenValidatingUpdate_givenUserIsCaptain_andExistingUserWasAlsoCaptain_andTeamIsNotChanged_thenSuccessResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final Team team = generateTeam();

        final UserRequest user = UserRequest.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU.toString())
            .profileLink("https://www.google.com")
            .liveStatsLink("https://www.google.com")
            .userIsCaptain(true)
            .hardwareId(hardware.id())
            .teamId(team.getId())
            .build();

        final User existingUser = User.builder()
            .id(1)
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU)
            .profileLink("https://www.google.com")
            .liveStatsLink("https://www.google.com")
            .userIsCaptain(true)
            .hardware(hardware)
            .team(team)
            .build();

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createTeam(team);
        foldingRepository.createUser(existingUser);

        final UserValidator userValidator = new UserValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final User response = userValidator.update(user, existingUser);

        assertThat(response)
            .as("Expected validation to pass")
            .isNotNull();
    }

    @Test
    void whenValidatingUpdate_givenUserIsCaptain_andExistingUserWasAlsoCaptain_andTeamIsChanged_andTeamHasCaptain_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final Team team = generateTeam();
        final Team oldTeam = generateTeam();

        final User currentCaptain = User.builder()
            .id(2)
            .foldingUserName("otherUser")
            .displayName("otherUser")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.AMD_GPU)
            .profileLink("https://www.google.com")
            .liveStatsLink("https://www.google.com")
            .userIsCaptain(true)
            .hardware(hardware)
            .team(team)
            .build();

        final UserRequest user = UserRequest.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU.toString())
            .profileLink("https://www.google.com")
            .liveStatsLink("https://www.google.com")
            .userIsCaptain(true)
            .hardwareId(hardware.id())
            .teamId(team.getId())
            .build();

        final User existingUser = User.builder()
            .id(1)
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU)
            .profileLink("https://www.google.com")
            .liveStatsLink("https://www.google.com")
            .userIsCaptain(true)
            .hardware(hardware)
            .team(oldTeam)
            .build();

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createTeam(team);
        foldingRepository.createTeam(oldTeam);
        foldingRepository.createUser(existingUser);
        foldingRepository.createUser(currentCaptain);

        final UserValidator userValidator = new UserValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final User response = userValidator.update(user, existingUser);

        assertThat(response)
            .as("Expected validation to pass")
            .isNotNull();
    }

    @Test
    void whenValidatingUpdate_givenUserIsCaptain_andAnotherUserIsCaptain_thenSuccessResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final Team team = generateTeam();

        final User currentCaptain = User.builder()
            .id(2)
            .foldingUserName("otherUser")
            .displayName("otherUser")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.AMD_GPU)
            .profileLink("https://www.google.com")
            .liveStatsLink("https://www.google.com")
            .userIsCaptain(true)
            .hardware(hardware)
            .team(team)
            .build();

        final UserRequest user = UserRequest.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU.toString())
            .profileLink("https://www.google.com")
            .liveStatsLink("https://www.google.com")
            .userIsCaptain(true)
            .hardwareId(hardware.id())
            .teamId(team.getId())
            .build();

        final User existingUser = User.builder()
            .id(1)
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU)
            .profileLink("https://www.google.com")
            .liveStatsLink("https://www.google.com")
            .userIsCaptain(false)
            .hardware(hardware)
            .team(team)
            .build();

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createTeam(team);
        foldingRepository.createUser(existingUser);
        foldingRepository.createUser(currentCaptain);

        final UserValidator userValidator = new UserValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final User response = userValidator.update(user, existingUser);

        assertThat(response)
            .as("Expected validation to pass")
            .isNotNull();
    }

    @Test
    void whenValidatingUpdate_givenUserIsNotCaptain_andTeamAlreadyContainsCaptain_thenSuccessResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final Team team = generateTeam();

        final User currentCaptain = User.builder()
            .id(2)
            .foldingUserName("user1")
            .displayName("user1")
            .passkey("DummyPasskey12345678901234567891")
            .category(Category.WILDCARD)
            .profileLink("https://www.google.com")
            .liveStatsLink("https://www.google.com")
            .userIsCaptain(true)
            .hardware(hardware)
            .team(team)
            .build();

        final UserRequest user = UserRequest.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU.toString())
            .profileLink("https://www.google.com")
            .liveStatsLink("https://www.google.com")
            .userIsCaptain(false)
            .hardwareId(hardware.id())
            .teamId(team.getId())
            .build();

        final User existingUser = User.builder()
            .id(userId++)
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU)
            .profileLink("https://www.google.com")
            .liveStatsLink("https://www.google.com")
            .userIsCaptain(false)
            .hardware(hardware)
            .team(team)
            .build();

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createTeam(team);
        foldingRepository.createUser(existingUser);
        foldingRepository.createUser(currentCaptain);

        final UserValidator userValidator = new UserValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final User response = userValidator.update(user, existingUser);

        assertThat(response)
            .as("Expected validation to pass")
            .isNotNull();
    }

    @Test
    void whenValidatingUpdate_givenTeamIsUpdated_andTeamAlreadyHasMaximumTotalUsers_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final Team team = generateTeam();
        final Team oldTeam = generateTeam();

        final UserRequest user = UserRequest.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU.toString())
            .profileLink("https://www.google.com")
            .liveStatsLink("https://www.google.com")
            .userIsCaptain(false)
            .hardwareId(hardware.id())
            .teamId(team.getId())
            .build();

        final User existingUser = User.builder()
            .id(userId++)
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU)
            .profileLink("https://www.google.com")
            .liveStatsLink("https://www.google.com")
            .userIsCaptain(false)
            .hardware(hardware)
            .team(oldTeam)
            .build();

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createTeam(team);
        foldingRepository.createUser(existingUser);

        for (int i = 0; i < Category.maximumPermittedAmountForAllCategories(); i++) {
            foldingRepository.createUser(
                User.builder()
                    .id(userId++)
                    .foldingUserName("user" + 0)
                    .displayName("user" + 0)
                    .passkey("DummyPasskey12345678901234567891")
                    .category(Category.WILDCARD)
                    .profileLink("https://www.google.com")
                    .liveStatsLink("https://www.google.com")
                    .userIsCaptain(false)
                    .hardware(hardware)
                    .team(team)
                    .build()
            );
        }

        final UserValidator userValidator = new UserValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(() -> userValidator.update(user, existingUser), ValidationException.class);
        assertThat(e.getValidationFailure().getErrors())
            .containsOnly(String.format("Team '%1$s' has %2$s users, maximum permitted is %2$s", team.getTeamName(),
                Category.maximumPermittedAmountForAllCategories())
            );
    }

    @Test
    void whenValidatingUpdate_givenTeamIsUpdated_andTeamAlreadyHasMaximumUsersInCategory_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final Team team = generateTeam();
        final Team oldTeam = generateTeam();

        final UserRequest user = UserRequest.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU.toString())
            .profileLink("https://www.google.com")
            .liveStatsLink("https://www.google.com")
            .userIsCaptain(true)
            .hardwareId(hardware.id())
            .teamId(team.getId())
            .build();

        final User existingUser = User.builder()
            .id(userId++)
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU)
            .profileLink("https://www.google.com")
            .liveStatsLink("https://www.google.com")
            .userIsCaptain(false)
            .hardware(hardware)
            .team(oldTeam)
            .build();

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createTeam(team);
        foldingRepository.createUser(existingUser);

        for (int i = 0; i < Category.NVIDIA_GPU.permittedUsers(); i++) {
            foldingRepository.createUser(
                User.builder()
                    .id(userId++)
                    .foldingUserName("user" + i)
                    .displayName("user" + i)
                    .passkey("DummyPasskey12345678901234567891")
                    .category(Category.NVIDIA_GPU)
                    .profileLink("https://www.google.com")
                    .liveStatsLink("https://www.google.com")
                    .userIsCaptain(false)
                    .hardware(hardware)
                    .team(team)
                    .build()
            );
        }

        final UserValidator userValidator = new UserValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final ValidationException e =
            catchThrowableOfType(() -> userValidator.update(user, existingUser),
                ValidationException.class);
        assertThat(e.getValidationFailure().getErrors())
            .containsOnly(String.format("Team '%1$s' already has %3$s users in category '%2$s', only %3$s permitted", team.getTeamName(),
                Category.NVIDIA_GPU, Category.NVIDIA_GPU.permittedUsers())
            );
    }

    @Test
    void whenValidatingUpdate_givenCategoryIsUpdated_andTeamAlreadyHasMaximumUsersInCategory_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final Team team = generateTeam();

        final UserRequest user = UserRequest.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU.toString())
            .profileLink("https://www.google.com")
            .liveStatsLink("https://www.google.com")
            .userIsCaptain(true)
            .hardwareId(hardware.id())
            .teamId(team.getId())
            .build();

        final User existingUser = User.builder()
            .id(userId++)
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.AMD_GPU)
            .profileLink("https://www.google.com")
            .liveStatsLink("https://www.google.com")
            .userIsCaptain(false)
            .hardware(hardware)
            .team(team)
            .build();

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createTeam(team);
        foldingRepository.createUser(existingUser);

        for (int i = 0; i < Category.NVIDIA_GPU.permittedUsers(); i++) {
            foldingRepository.createUser(
                User.builder()
                    .id(userId++)
                    .foldingUserName("user" + 0)
                    .displayName("user" + 0)
                    .passkey("DummyPasskey12345678901234567891")
                    .category(Category.NVIDIA_GPU)
                    .profileLink("https://www.google.com")
                    .liveStatsLink("https://www.google.com")
                    .userIsCaptain(false)
                    .hardware(hardware)
                    .team(team)
                    .build()
            );
        }

        final UserValidator userValidator = new UserValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final ValidationException e =
            catchThrowableOfType(() -> userValidator.update(user, existingUser), ValidationException.class);
        assertThat(e.getValidationFailure().getErrors())
            .containsOnly(String.format("Team '%1$s' already has %3$s users in category '%2$s', only %3$s permitted", team.getTeamName(),
                Category.NVIDIA_GPU, Category.NVIDIA_GPU.permittedUsers())
            );
    }

    @Test
    void whenValidatingUpdate_givenUserWithMisMatchingHardwareMakeAndCategory_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final Team team = generateTeam();

        final UserRequest user = UserRequest.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.AMD_GPU.toString())
            .profileLink("https://www.google.com")
            .liveStatsLink("https://www.google.com")
            .userIsCaptain(true)
            .hardwareId(hardware.id())
            .teamId(team.getId())
            .build();

        final User existingUser = User.builder()
            .id(userId++)
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU)
            .profileLink("https://www.google.com")
            .liveStatsLink("https://www.google.com")
            .userIsCaptain(false)
            .hardware(hardware)
            .team(team)
            .build();

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createTeam(team);
        foldingRepository.createUser(existingUser);

        final UserValidator userValidator = new UserValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(() -> userValidator.update(user, existingUser), ValidationException.class);
        assertThat(e.getValidationFailure().getErrors())
            .containsOnly(String.format("Category '%s' cannot be filled by hardware of make '%s', must be one of: %s", Category.AMD_GPU,
                hardware.hardwareMake(), Category.AMD_GPU.supportedHardwareMakes())
            );
    }

    @Test
    void whenValidatingUpdate_givenUserWithMisMatchingHardwareTypeAndCategory_thenFailureResponseIsReturned() {
        final Hardware hardware = Hardware.create(
            hardwareId++,
            "hardwareName",
            "displayName",
            HardwareMake.AMD,
            HardwareType.CPU,
            1.00D,
            1L
        );
        final Team team = generateTeam();

        final UserRequest user = UserRequest.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.AMD_GPU.toString())
            .profileLink("https://www.google.com")
            .liveStatsLink("https://www.google.com")
            .userIsCaptain(true)
            .hardwareId(hardware.id())
            .teamId(team.getId())
            .build();

        final User existingUser = User.builder()
            .id(userId++)
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU)
            .profileLink("https://www.google.com")
            .liveStatsLink("https://www.google.com")
            .userIsCaptain(false)
            .hardware(hardware)
            .team(team)
            .build();

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createTeam(team);
        foldingRepository.createUser(existingUser);

        final UserValidator userValidator = new UserValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(() -> userValidator.update(user, existingUser), ValidationException.class);
        assertThat(e.getValidationFailure().getErrors())
            .containsOnly(String.format("Category '%s' cannot be filled by hardware of type '%s', must be one of: %s", Category.AMD_GPU,
                hardware.hardwareType(), Category.AMD_GPU.supportedHardwareTypes())
            );
    }

    @Test
    void whenValidatingUpdate_givenUserWithHardwareWithNonExistingId_thenFailureResponseIsReturned() {
        final Hardware hardware = Hardware.create(
            99,
            "hardwareName",
            "displayName",
            HardwareMake.NVIDIA,
            HardwareType.GPU,
            1.00D,
            1L
        );

        final Team team = generateTeam();

        final UserRequest user = UserRequest.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU.toString())
            .profileLink("https://www.google.com")
            .liveStatsLink("https://www.google.com")
            .userIsCaptain(true)
            .hardwareId(1)
            .teamId(team.getId())
            .build();

        final User existingUser = User.builder()
            .id(userId++)
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU)
            .profileLink("https://www.google.com")
            .liveStatsLink("https://www.google.com")
            .userIsCaptain(true)
            .hardware(hardware)
            .team(team)
            .build();

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createTeam(team);
        foldingRepository.createUser(existingUser);

        final UserValidator userValidator = new UserValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(() -> userValidator.update(user, existingUser), ValidationException.class);
        assertThat(e.getValidationFailure().getErrors())
            .containsOnly(String.format("Field 'hardwareId' must be one of: [%s: %s]", hardware.id(), hardware.hardwareName()));
    }

    @Test
    void whenValidatingUpdate_givenNoHardwareExistsOnTheSystem_thenFailureResponseIsReturned() {
        final Team team = generateTeam();

        final UserRequest user = UserRequest.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU.toString())
            .profileLink("https://www.google.com")
            .liveStatsLink("https://www.google.com")
            .userIsCaptain(true)
            .hardwareId(1)
            .teamId(team.getId())
            .build();

        final User existingUser = User.builder()
            .id(userId++)
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU)
            .profileLink("https://www.google.com")
            .liveStatsLink("https://www.google.com")
            .userIsCaptain(true)
            .team(team)
            .build();

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createTeam(team);
        foldingRepository.createUser(existingUser);

        final UserValidator userValidator = new UserValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(() -> userValidator.update(user, existingUser), ValidationException.class);
        assertThat(e.getValidationFailure().getErrors())
            .containsOnly("No hardwares exist on the system");
    }

    @Test
    void whenValidatingUpdate_givenUserWithTeamWithNonExistingId_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final Team team = Team.builder()
            .id(99)
            .teamName("teamName")
            .teamDescription("teamDescription")
            .forumLink("https://www.google.com")
            .build();

        final UserRequest user = UserRequest.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU.toString())
            .profileLink("https://www.google.com")
            .liveStatsLink("https://www.google.com")
            .userIsCaptain(true)
            .hardwareId(hardware.id())
            .teamId(1)
            .build();

        final User existingUser = User.builder()
            .id(userId++)
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU)
            .profileLink("https://www.google.com")
            .liveStatsLink("https://www.google.com")
            .userIsCaptain(true)
            .hardware(hardware)
            .team(team)
            .build();

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createTeam(team);
        foldingRepository.createUser(existingUser);

        final UserValidator userValidator = new UserValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(() -> userValidator.update(user, existingUser), ValidationException.class);
        assertThat(e.getValidationFailure().getErrors())
            .containsOnly(String.format("Field 'teamId' must be one of: [%s: %s]", team.getId(), team.getTeamName()));
    }

    @Test
    void whenValidatingUpdate_givenNoTeamsExistOnTheSystem_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();

        final UserRequest user = UserRequest.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU.toString())
            .profileLink("https://www.google.com")
            .liveStatsLink("https://www.google.com")
            .userIsCaptain(true)
            .hardwareId(hardware.id())
            .teamId(1)
            .build();

        final User existingUser = User.builder()
            .id(userId++)
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU)
            .profileLink("https://www.google.com")
            .liveStatsLink("https://www.google.com")
            .userIsCaptain(true)
            .hardware(hardware)
            .build();

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createUser(existingUser);

        final UserValidator userValidator = new UserValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(() -> userValidator.update(user, existingUser), ValidationException.class);
        assertThat(e.getValidationFailure().getErrors())
            .containsOnly("No teams exist on the system");
    }

    @Test
    void whenValidatingUpdate_givenUserHasNoWorkUnitsCompleted_andFoldingUserNameAndPasskeyAreUnchanged_thenSuccessResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final Team team = generateTeam();

        final UserRequest user = UserRequest.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU.toString())
            .profileLink("https://www.google.com")
            .liveStatsLink("https://www.google.com")
            .userIsCaptain(true)
            .hardwareId(hardware.id())
            .teamId(team.getId())
            .build();

        final User existingUser = User.builder()
            .id(userId++)
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU)
            .profileLink("https://www.google.com")
            .liveStatsLink("https://www.google.com")
            .userIsCaptain(true)
            .hardware(hardware)
            .team(team)
            .build();

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createTeam(team);
        foldingRepository.createUser(existingUser);

        final UserValidator userValidator = new UserValidator(foldingRepository, new NoUnitsFoldingStatsRetriever());
        final User response = userValidator.update(user, existingUser);

        assertThat(response)
            .as("Expected validation to pass")
            .isNotNull();
    }

    @Test
    void whenValidatingUpdate_givenUserHasNoWorkUnitsCompleted_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final Team team = generateTeam();

        final UserRequest user = UserRequest.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU.toString())
            .profileLink("https://www.google.com")
            .liveStatsLink("https://www.google.com")
            .userIsCaptain(true)
            .hardwareId(hardware.id())
            .teamId(team.getId())
            .build();

        final User existingUser = User.builder()
            .id(userId++)
            .foldingUserName("oldUserName")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU)
            .profileLink("https://www.google.com")
            .liveStatsLink("https://www.google.com")
            .userIsCaptain(true)
            .hardware(hardware)
            .team(team)
            .build();

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createTeam(team);
        foldingRepository.createUser(existingUser);

        final UserValidator userValidator = new UserValidator(foldingRepository, new NoUnitsFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(() -> userValidator.update(user, existingUser), ValidationException.class);
        assertThat(e.getValidationFailure().getErrors())
            .containsOnly(String.format(
                "User '%s' has 0 Work Units with passkey '%s', there must be at least one completed Work Unit before adding the user",
                user.getDisplayName(), user.getPasskey())
            );
    }

    @Test
    void whenValidatingUpdate_givenUserWorkUnitsCannotBeAccessed_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final Team team = generateTeam();

        final UserRequest user = UserRequest.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU.toString())
            .profileLink("https://www.google.com")
            .liveStatsLink("https://www.google.com")
            .userIsCaptain(true)
            .hardwareId(hardware.id())
            .teamId(team.getId())
            .build();

        final User existingUser = User.builder()
            .id(userId++)
            .foldingUserName("oldUserName")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU)
            .profileLink("https://www.google.com")
            .liveStatsLink("https://www.google.com")
            .userIsCaptain(true)
            .hardware(hardware)
            .team(team)
            .build();

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createTeam(team);
        foldingRepository.createUser(existingUser);

        final UserValidator userValidator = new UserValidator(foldingRepository, new ExternalConnectionFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(() -> userValidator.update(user, existingUser), ValidationException.class);
        assertThat(e.getValidationFailure().getErrors())
            .containsOnly(String.format("Unable to connect to 'https://www.google.com' to check stats for user '%s': Error connecting",
                user.getDisplayName())
            );
    }

    @Test
    void whenValidatingUpdate_givenUnexpectedErrorWhenAccessingUserWorkUnits_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final Team team = generateTeam();

        final UserRequest user = UserRequest.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU.toString())
            .profileLink("https://www.google.com")
            .liveStatsLink("https://www.google.com")
            .userIsCaptain(true)
            .hardwareId(hardware.id())
            .teamId(team.getId())
            .build();

        final User existingUser = User.builder()
            .id(userId++)
            .foldingUserName("oldUserName")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU)
            .profileLink("https://www.google.com")
            .liveStatsLink("https://www.google.com")
            .userIsCaptain(true)
            .hardware(hardware)
            .team(team)
            .build();

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createTeam(team);
        foldingRepository.createUser(existingUser);

        final UserValidator userValidator = new UserValidator(foldingRepository, new UnexpectedExceptionFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(() -> userValidator.update(user, existingUser), ValidationException.class);
        assertThat(e.getValidationFailure().getErrors())
            .containsOnly(String.format("Unable to check stats for user '%s': Error", user.getDisplayName()));
    }

    @Test
    void whenValidatingUpdate_givenMultipleFailures_thenFailureResponseIsReturned_andAllValidationErrorsAreReturned() {
        final Hardware hardware = generateHardware();
        final Team team = generateTeam();

        final UserRequest user = UserRequest.builder()
            .foldingUserName(null)
            .displayName(null)
            .passkey("invalid")
            .category("invalid")
            .profileLink("invalid")
            .liveStatsLink("invalid")
            .userIsCaptain(true)
            .hardwareId(hardware.id())
            .teamId(team.getId())
            .build();

        final User existingUser = User.builder()
            .id(userId++)
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU)
            .profileLink("https://www.google.com")
            .liveStatsLink("https://www.google.com")
            .userIsCaptain(true)
            .team(team)
            .build();

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createTeam(team);
        foldingRepository.createUser(existingUser);

        final UserValidator userValidator = new UserValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(() -> userValidator.update(user, existingUser), ValidationException.class);
        assertThat(e.getValidationFailure().getErrors())
            .containsOnly(
                "Field 'foldingUserName' must have at least one alphanumeric character, or an underscore, period or hyphen",
                "Field 'displayName' must not be empty",
                "Field 'passkey' must be 32 characters long and include only alphanumeric characters",
                "Field 'category' must be one of: [AMD_GPU, NVIDIA_GPU, WILDCARD]",
                "Field 'profileLink' is not a valid link: 'invalid'",
                "Field 'liveStatsLink' is not a valid link: 'invalid'"
            );
    }

    @Test
    void whenValidatingDelete_andUserIsNotCaptain_thenSuccessResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final Team team = generateTeam();

        final User userToDelete = User.builder()
            .id(userId++)
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU)
            .profileLink("https://www.google.com")
            .liveStatsLink("https://www.google.com")
            .userIsCaptain(false)
            .hardware(hardware)
            .team(team)
            .build();

        final FoldingRepository foldingRepository = new MockFoldingRepository();

        final UserValidator userValidator = new UserValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final User response = userValidator.delete(userToDelete);

        assertThat(response)
            .as("Expected validation to pass")
            .isNotNull();
    }

    @Test
    void whenValidatingDelete_andUserIsCaptain_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final Team team = generateTeam();

        final User userToDelete = User.builder()
            .id(userId++)
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU)
            .profileLink("https://www.google.com")
            .liveStatsLink("https://www.google.com")
            .userIsCaptain(true)
            .hardware(hardware)
            .team(team)
            .build();

        final FoldingRepository foldingRepository = new MockFoldingRepository();

        final UserValidator userValidator = new UserValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(() -> userValidator.delete(userToDelete), ValidationException.class);
        assertThat(e.getValidationFailure().getErrors())
            .containsOnly(String.format("Cannot delete user '%s' since they are team captain", userToDelete.getDisplayName()));
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
        return Team.builder()
            .id(teamId++)
            .teamName("teamName")
            .teamDescription("teamDescription")
            .forumLink("https://www.google.com")
            .build();
    }
}

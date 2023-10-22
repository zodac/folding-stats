/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2023 zodac.me
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

import me.zodac.folding.api.exception.ConflictException;
import me.zodac.folding.api.exception.ValidationException;
import me.zodac.folding.api.tc.Category;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.HardwareMake;
import me.zodac.folding.api.tc.HardwareType;
import me.zodac.folding.api.tc.Role;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.bean.api.FoldingRepository;
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

    private static final String DUMMY_PASSKEY = "DummyPasskey12345678901234567890";
    private static final String VALID_PROFILE_LINK = "https://www.google.com";
    private static final String VALID_LIVE_STATS_LINK = "https://www.google.ie";

    private static int hardwareId = 1;
    private static int teamId = 1;
    private static int userId = 1;

    @Test
    void whenValidatingCreate_givenValidUser_thenSuccessResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final Team team = generateTeam();

        final UserRequest user = generateUserRequest(
            "-folding.Name_1",
            "user",
            DUMMY_PASSKEY,
            Category.NVIDIA_GPU,
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            hardware.id(),
            team.id(),
            true
        );

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

        final UserRequest user = generateUserRequest(
            "-folding.Name_1",
            "user",
            DUMMY_PASSKEY,
            Category.INVALID,
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            hardware.id(),
            team.id(),
            true
        );

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createTeam(team);

        final UserValidator userValidator = new UserValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(() -> userValidator.create(user), ValidationException.class);
        assertThat(e.getValidationFailure().errors())
            .containsOnly("Field 'category' must be one of: [AMD_GPU, NVIDIA_GPU, WILDCARD]");
    }

    @Test
    void whenValidatingCreate_givenUserWithNullFoldingUserName_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final Team team = generateTeam();

        final UserRequest user = generateUserRequest(
            null,
            "user",
            DUMMY_PASSKEY,
            Category.NVIDIA_GPU,
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            hardware.id(),
            team.id(),
            true
        );
        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createTeam(team);

        final UserValidator userValidator = new UserValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(() -> userValidator.create(user), ValidationException.class);
        assertThat(e.getValidationFailure().errors())
            .containsOnly("Field 'foldingUserName' must have at least one alphanumeric character, or an underscore, period or hyphen");
    }

    @Test
    void whenValidatingCreate_givenUserWithInvalidFoldingUserName_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final Team team = generateTeam();

        final UserRequest user = generateUserRequest(
            "folding*Name",
            "user",
            DUMMY_PASSKEY,
            Category.NVIDIA_GPU,
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            hardware.id(),
            team.id(),
            true
        );

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createTeam(team);

        final UserValidator userValidator = new UserValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(() -> userValidator.create(user), ValidationException.class);
        assertThat(e.getValidationFailure().errors())
            .containsOnly("Field 'foldingUserName' must have at least one alphanumeric character, or an underscore, period or hyphen");
    }

    @Test
    void whenValidatingCreate_givenUserWithNullDisplayName_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final Team team = generateTeam();

        final UserRequest user = generateUserRequest(
            "-folding.Name_1",
            null,
            DUMMY_PASSKEY,
            Category.NVIDIA_GPU,
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            hardware.id(),
            team.id(),
            true
        );

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createTeam(team);

        final UserValidator userValidator = new UserValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(() -> userValidator.create(user), ValidationException.class);
        assertThat(e.getValidationFailure().errors())
            .containsOnly("Field 'displayName' must not be empty");
    }

    @Test
    void whenValidatingCreate_givenUserWithNullPasskey_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final Team team = generateTeam();

        final UserRequest user = generateUserRequest(
            "-folding.Name_1",
            "user",
            null,
            Category.NVIDIA_GPU,
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            hardware.id(),
            team.id(),
            true
        );

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createTeam(team);

        final UserValidator userValidator = new UserValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(() -> userValidator.create(user), ValidationException.class);
        assertThat(e.getValidationFailure().errors())
            .containsOnly("Field 'passkey' must be 32 characters long and include only alphanumeric characters");
    }

    @Test
    void whenValidatingCreate_givenUserWithInvalidLengthPasskey_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final Team team = generateTeam();

        final UserRequest user = generateUserRequest(
            "-folding.Name_1",
            "user",
            "1234",
            Category.NVIDIA_GPU,
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            hardware.id(),
            team.id(),
            true
        );

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createTeam(team);

        final UserValidator userValidator = new UserValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(() -> userValidator.create(user), ValidationException.class);
        assertThat(e.getValidationFailure().errors())
            .containsOnly("Field 'passkey' must be 32 characters long and include only alphanumeric characters");
    }

    @Test
    void whenValidatingCreate_givenUserWithPasskeyContainingInvalidCharacters_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final Team team = generateTeam();

        final UserRequest user = generateUserRequest(
            "-folding.Name_1",
            "user",
            "DummyPasskey1234567890123456789*",
            Category.NVIDIA_GPU,
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            hardware.id(),
            team.id(),
            true
        );

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createTeam(team);

        final UserValidator userValidator = new UserValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(() -> userValidator.create(user), ValidationException.class);
        assertThat(e.getValidationFailure().errors())
            .containsOnly("Field 'passkey' must be 32 characters long and include only alphanumeric characters");
    }

    @Test
    void whenValidatingCreate_givenUserWithNullProfileLink_thenSuccessResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final Team team = generateTeam();

        final UserRequest user = generateUserRequest(
            "-folding.Name_1",
            "user",
            DUMMY_PASSKEY,
            Category.NVIDIA_GPU,
            null,
            VALID_LIVE_STATS_LINK,
            hardware.id(),
            team.id(),
            true
        );

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

        final UserRequest user = generateUserRequest(
            "-folding.Name_1",
            "user",
            DUMMY_PASSKEY,
            Category.NVIDIA_GPU,
            "invalidUrl",
            VALID_LIVE_STATS_LINK,
            hardware.id(),
            team.id(),
            true
        );

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createTeam(team);

        final UserValidator userValidator = new UserValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(() -> userValidator.create(user), ValidationException.class);
        assertThat(e.getValidationFailure().errors())
            .containsOnly("Field 'profileLink' is not a valid link: 'invalidUrl'");
    }

    @Test
    void whenValidatingCreate_givenUserWithNullLiveStatsLink_thenSuccessResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final Team team = generateTeam();

        final UserRequest user = generateUserRequest(
            "-folding.Name_1",
            "user",
            DUMMY_PASSKEY,
            Category.NVIDIA_GPU,
            VALID_PROFILE_LINK,
            null,
            hardware.id(),
            team.id(),
            true
        );

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

        final UserRequest user = generateUserRequest(
            "-folding.Name_1",
            "user",
            DUMMY_PASSKEY,
            Category.NVIDIA_GPU,
            VALID_PROFILE_LINK,
            "invalidUrl",
            hardware.id(),
            team.id(),
            true
        );

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createTeam(team);

        final UserValidator userValidator = new UserValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(() -> userValidator.create(user), ValidationException.class);
        assertThat(e.getValidationFailure().errors())
            .containsOnly("Field 'liveStatsLink' is not a valid link: 'invalidUrl'");
    }

    @Test
    void whenValidatingCreate_givenOtherUserAlreadyExists_thenSuccessResponseIsReturned() {
        final Hardware otherHardware = generateHardware();
        final Team otherTeam = generateTeam();

        final User otherUser = User.create(
            userId++,
            "user1",
            "user1",
            "DummyPasskey12345678901234567891",
            Category.NVIDIA_GPU,
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            otherHardware,
            otherTeam,
            Role.MEMBER
        );

        final Hardware hardware = generateHardware();
        final Team team = generateTeam();

        final UserRequest user = generateUserRequest(
            "user",
            "user",
            DUMMY_PASSKEY,
            Category.NVIDIA_GPU,
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            hardware.id(),
            team.id(),
            true
        );

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

        final User otherUser = User.create(
            userId++,
            "user",
            "user",
            DUMMY_PASSKEY,
            Category.NVIDIA_GPU,
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            otherHardware,
            otherTeam,
            Role.CAPTAIN
        );

        final Hardware hardware = generateHardware();
        final Team team = generateTeam();

        final UserRequest user = generateUserRequest(
            "user",
            "user",
            DUMMY_PASSKEY,
            Category.NVIDIA_GPU,
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            hardware.id(),
            team.id(),
            true
        );

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createHardware(otherHardware);
        foldingRepository.createTeam(team);
        foldingRepository.createTeam(otherTeam);
        foldingRepository.createUser(otherUser);

        final UserValidator userValidator = new UserValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final ConflictException e = catchThrowableOfType(() -> userValidator.create(user), ConflictException.class);
        assertThat(e.getConflictFailure().conflictingAttributes())
            .containsOnly(
                "foldingUserName",
                "passkey"
            );

        assertThat(e.getConflictFailure().conflictingObject())
            .isNotNull();
    }

    @Test
    void whenValidatingCreate_givenUserWithMisMatchingHardwareMakeAndCategory_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final Team team = generateTeam();

        final UserRequest user = generateUserRequest(
            "user",
            "user",
            DUMMY_PASSKEY,
            Category.AMD_GPU,
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            hardware.id(),
            team.id(),
            true
        );

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createTeam(team);

        final UserValidator userValidator = new UserValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(() -> userValidator.create(user), ValidationException.class);
        assertThat(e.getValidationFailure().errors())
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

        final UserRequest user = generateUserRequest(
            "user",
            "user",
            DUMMY_PASSKEY,
            Category.AMD_GPU,
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            hardware.id(),
            team.id(),
            true
        );

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createTeam(team);

        final UserValidator userValidator = new UserValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(() -> userValidator.create(user), ValidationException.class);
        assertThat(e.getValidationFailure().errors())
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

        final UserRequest user = generateUserRequest(
            "user",
            "user",
            DUMMY_PASSKEY,
            Category.NVIDIA_GPU,
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            1,
            team.id(),
            true
        );

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createTeam(team);

        final UserValidator userValidator = new UserValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(() -> userValidator.create(user), ValidationException.class);
        assertThat(e.getValidationFailure().errors())
            .containsOnly(String.format("Field 'hardwareId' must be one of: [%s: %s]", hardware.id(), hardware.hardwareName()));
    }

    @Test
    void whenValidatingCreate_givenNoHardwareExistsOnSystem_thenFailureResponseIsReturned() {
        final Team team = generateTeam();

        final UserRequest user = generateUserRequest(
            "user",
            "user",
            DUMMY_PASSKEY,
            Category.NVIDIA_GPU,
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            1,
            team.id(),
            true
        );

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createTeam(team);

        final UserValidator userValidator = new UserValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(() -> userValidator.create(user), ValidationException.class);
        assertThat(e.getValidationFailure().errors())
            .containsOnly("No hardwares exist on the system");
    }

    @Test
    void whenValidatingCreate_givenUserWithTeamWithNonExistingId_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final Team team = Team.create(
            99,
            "teamName",
            "teamDescription",
            "https://www.google.com"
        );

        final UserRequest user = generateUserRequest(
            "user",
            "user",
            DUMMY_PASSKEY,
            Category.NVIDIA_GPU,
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            hardware.id(),
            1,
            true
        );

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createTeam(team);

        final UserValidator userValidator = new UserValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(() -> userValidator.create(user), ValidationException.class);
        assertThat(e.getValidationFailure().errors())
            .containsOnly(String.format("Field 'teamId' must be one of: [%s: %s]", team.id(), team.teamName()));
    }

    @Test
    void whenValidatingCreate_givenNoTeamsExistsOnSystem_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();

        final UserRequest user = generateUserRequest(
            "user",
            "user",
            DUMMY_PASSKEY,
            Category.NVIDIA_GPU,
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            hardware.id(),
            1,
            true
        );

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);

        final UserValidator userValidator = new UserValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(() -> userValidator.create(user), ValidationException.class);
        assertThat(e.getValidationFailure().errors())
            .containsOnly("No teams exist on the system");
    }

    @Test
    void whenValidatingCreate_givenUserIsCaptain_andTeamAlreadyContainsCaptain_thenSuccessResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final Team team = generateTeam();

        final User currentCaptain = User.create(
            userId++,
            "user1",
            "user1",
            "DummyPasskey12345678901234567891",
            Category.WILDCARD,
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            hardware,
            team,
            Role.CAPTAIN
        );

        final UserRequest user = generateUserRequest(
            "user",
            "user",
            DUMMY_PASSKEY,
            Category.NVIDIA_GPU,
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            hardware.id(),
            team.id(),
            true
        );

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

        final User currentCaptain = User.create(
            userId++,
            "user1",
            "user1",
            "DummyPasskey12345678901234567891",
            Category.WILDCARD,
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            hardware,
            team,
            Role.CAPTAIN
        );

        final UserRequest user = generateUserRequest(
            "user",
            "user",
            DUMMY_PASSKEY,
            Category.NVIDIA_GPU,
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            hardware.id(),
            team.id(),
            false
        );

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

        final UserRequest user = generateUserRequest(
            "user",
            "user",
            DUMMY_PASSKEY,
            Category.NVIDIA_GPU,
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            hardware.id(),
            team.id(),
            false
        );

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createTeam(team);

        for (int i = 0; i < Category.maximumPermittedAmountForAllCategories(); i++) {
            foldingRepository.createUser(
                User.create(
                    userId++,
                    "user" + i,
                    "user" + i,
                    "DummyPasskey12345678901234567891",
                    Category.WILDCARD,
                    VALID_PROFILE_LINK,
                    VALID_LIVE_STATS_LINK,
                    hardware,
                    team,
                    Role.MEMBER
                )
            );
        }

        final UserValidator userValidator = new UserValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(() -> userValidator.create(user), ValidationException.class);
        assertThat(e.getValidationFailure().errors())
            .containsOnly(String.format("Team '%1$s' has %2$s users, maximum permitted is %2$s", team.teamName(),
                Category.maximumPermittedAmountForAllCategories()));
    }

    @Test
    void whenValidatingCreate_givenTeamAlreadyHasUsersInAnotherCategory_thenSuccessResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final Team team = generateTeam();

        final UserRequest user = generateUserRequest(
            "user",
            "user",
            DUMMY_PASSKEY,
            Category.NVIDIA_GPU,
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            hardware.id(),
            team.id(),
            true
        );

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createTeam(team);

        for (int i = 0; i < Category.AMD_GPU.permittedUsers(); i++) {
            foldingRepository.createUser(
                User.create(
                    userId++,
                    "user" + i,
                    "user" + i,
                    "DummyPasskey12345678901234567891",
                    Category.AMD_GPU,
                    VALID_PROFILE_LINK,
                    VALID_LIVE_STATS_LINK,
                    hardware,
                    team,
                    Role.MEMBER
                )
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

        final UserRequest user = generateUserRequest(
            "user",
            "user",
            DUMMY_PASSKEY,
            Category.NVIDIA_GPU,
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            hardware.id(),
            team.id(),
            true
        );

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createTeam(team);

        for (int i = 0; i < Category.NVIDIA_GPU.permittedUsers(); i++) {
            foldingRepository.createUser(
                User.create(
                    userId++,
                    "user" + i,
                    "user" + i,
                    "DummyPasskey12345678901234567891",
                    Category.NVIDIA_GPU,
                    VALID_PROFILE_LINK,
                    VALID_LIVE_STATS_LINK,
                    hardware,
                    team,
                    Role.MEMBER
                )
            );
        }

        final UserValidator userValidator = new UserValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(() -> userValidator.create(user), ValidationException.class);
        assertThat(e.getValidationFailure().errors())
            .containsOnly(
                String.format("Team '%1$s' already has %3$s users in category '%2$s', only %3$s permitted", team.teamName(), Category.NVIDIA_GPU,
                    Category.NVIDIA_GPU.permittedUsers()));
    }

    @Test
    void whenValidatingCreate_givenUserHasNoWorkUnitsCompleted_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final Team team = generateTeam();

        final UserRequest user = generateUserRequest(
            "user",
            "user",
            DUMMY_PASSKEY,
            Category.NVIDIA_GPU,
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            hardware.id(),
            team.id(),
            true
        );

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createTeam(team);

        final UserValidator userValidator = new UserValidator(foldingRepository, new NoUnitsFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(() -> userValidator.create(user), ValidationException.class);
        assertThat(e.getValidationFailure().errors())
            .containsOnly(
                String.format("User '%s' has 0 Work Units with passkey '%s', there must be at least one completed Work Unit before adding the user",
                    user.displayName(), user.passkey())
            );
    }

    @Test
    void whenValidatingCreate_givenUserWorkUnitsCannotBeAccessed_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final Team team = generateTeam();

        final UserRequest user = generateUserRequest(
            "user",
            "user",
            DUMMY_PASSKEY,
            Category.NVIDIA_GPU,
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            hardware.id(),
            team.id(),
            true
        );

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createTeam(team);

        final UserValidator userValidator = new UserValidator(foldingRepository, new ExternalConnectionFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(() -> userValidator.create(user), ValidationException.class);
        assertThat(e.getValidationFailure().errors())
            .containsOnly(String.format("Unable to connect to 'https://www.google.com' to check stats for user '%s': Error connecting",
                user.displayName())
            );
    }

    @Test
    void whenValidatingCreate_givenUnexpectedErrorWhenAccessingUserWorkUnits_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final Team team = generateTeam();

        final UserRequest user = generateUserRequest(
            "user",
            "user",
            DUMMY_PASSKEY,
            Category.NVIDIA_GPU,
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            hardware.id(),
            team.id(),
            true
        );

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createTeam(team);

        final UserValidator userValidator = new UserValidator(foldingRepository, new UnexpectedExceptionFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(() -> userValidator.create(user), ValidationException.class);
        assertThat(e.getValidationFailure().errors())
            .containsOnly(String.format("Unable to check stats for user '%s': Error", user.displayName()));
    }

    @Test
    void whenValidatingCreate_givenMultipleFailures_thenFailureResponseIsReturned_andAllValidationErrorsAreReturned() {
        final Hardware hardware = generateHardware();
        final Team team = generateTeam();

        final UserRequest user = generateUserRequest(
            null,
            null,
            null,
            Category.INVALID,
            "invalidUrl",
            "invalidUrl",
            hardware.id(),
            team.id(),
            true
        );

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createTeam(team);

        final UserValidator userValidator = new UserValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(() -> userValidator.create(user), ValidationException.class);
        assertThat(e.getValidationFailure().errors())
            .containsOnly(
                "Field 'foldingUserName' must have at least one alphanumeric character, or an underscore, period or hyphen",
                "Field 'displayName' must not be empty",
                "Field 'passkey' must be 32 characters long and include only alphanumeric characters",
                "Field 'category' must be one of: [AMD_GPU, NVIDIA_GPU, WILDCARD]",
                "Field 'profileLink' is not a valid link: 'invalidUrl'",
                "Field 'liveStatsLink' is not a valid link: 'invalidUrl'"
            );
    }

    @Test
    void whenValidatingUpdate_givenValidUser_thenSuccessResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final Team team = generateTeam();

        final UserRequest user = generateUserRequest(
            "user",
            "user",
            DUMMY_PASSKEY,
            Category.NVIDIA_GPU,
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            hardware.id(),
            team.id(),
            true
        );

        final User existingUser = User.create(
            userId++,
            "user",
            "user",
            DUMMY_PASSKEY,
            Category.NVIDIA_GPU,
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            hardware,
            team,
            Role.CAPTAIN
        );

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

        final UserRequest user = generateUserRequest(
            "user",
            "user",
            DUMMY_PASSKEY,
            Category.INVALID,
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            hardware.id(),
            team.id(),
            true
        );

        final User existingUser = User.create(
            userId++,
            "user",
            "user",
            DUMMY_PASSKEY,
            Category.NVIDIA_GPU,
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            hardware,
            team,
            Role.CAPTAIN
        );

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createTeam(team);
        foldingRepository.createUser(existingUser);

        final UserValidator userValidator = new UserValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(() -> userValidator.update(user, existingUser), ValidationException.class);
        assertThat(e.getValidationFailure().errors())
            .containsOnly("Field 'category' must be one of: [AMD_GPU, NVIDIA_GPU, WILDCARD]");
    }

    @Test
    void whenValidatingUpdate_givenUserWithNullFoldingUserName_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final Team team = generateTeam();

        final UserRequest user = generateUserRequest(
            null,
            "user",
            DUMMY_PASSKEY,
            Category.NVIDIA_GPU,
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            hardware.id(),
            team.id(),
            true
        );

        final User existingUser = User.create(
            userId++,
            "user",
            "user",
            DUMMY_PASSKEY,
            Category.NVIDIA_GPU,
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            hardware,
            team,
            Role.CAPTAIN
        );

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createTeam(team);
        foldingRepository.createUser(existingUser);

        final UserValidator userValidator = new UserValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(() -> userValidator.update(user, existingUser), ValidationException.class);
        assertThat(e.getValidationFailure().errors())
            .containsOnly("Field 'foldingUserName' must have at least one alphanumeric character, or an underscore, period or hyphen");
    }

    @Test
    void whenValidatingUpdate_givenUserWithInvalidFoldingUserName_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final Team team = generateTeam();

        final UserRequest user = generateUserRequest(
            "folding*Name",
            "user",
            DUMMY_PASSKEY,
            Category.NVIDIA_GPU,
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            hardware.id(),
            team.id(),
            true
        );

        final User existingUser = User.create(
            userId++,
            "user",
            "user",
            DUMMY_PASSKEY,
            Category.NVIDIA_GPU,
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            hardware,
            team,
            Role.CAPTAIN
        );

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createTeam(team);
        foldingRepository.createUser(existingUser);

        final UserValidator userValidator = new UserValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(() -> userValidator.update(user, existingUser), ValidationException.class);
        assertThat(e.getValidationFailure().errors())
            .containsOnly("Field 'foldingUserName' must have at least one alphanumeric character, or an underscore, period or hyphen");
    }

    @Test
    void whenValidatingUpdate_givenUserWithNullDisplayName_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final Team team = generateTeam();

        final UserRequest user = generateUserRequest(
            "user",
            null,
            DUMMY_PASSKEY,
            Category.NVIDIA_GPU,
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            hardware.id(),
            team.id(),
            true
        );

        final User existingUser = User.create(
            userId++,
            "user",
            "user",
            DUMMY_PASSKEY,
            Category.NVIDIA_GPU,
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            hardware,
            team,
            Role.CAPTAIN
        );

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createTeam(team);
        foldingRepository.createUser(existingUser);

        final UserValidator userValidator = new UserValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(() -> userValidator.update(user, existingUser), ValidationException.class);
        assertThat(e.getValidationFailure().errors())
            .containsOnly("Field 'displayName' must not be empty");
    }

    @Test
    void whenValidatingUpdate_givenUserWithNullPasskey_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final Team team = generateTeam();

        final UserRequest user = generateUserRequest(
            "user",
            "user",
            null,
            Category.NVIDIA_GPU,
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            hardware.id(),
            team.id(),
            true
        );

        final User existingUser = User.create(
            userId++,
            "user",
            "user",
            DUMMY_PASSKEY,
            Category.NVIDIA_GPU,
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            hardware,
            team,
            Role.CAPTAIN
        );

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createTeam(team);
        foldingRepository.createUser(existingUser);

        final UserValidator userValidator = new UserValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(() -> userValidator.update(user, existingUser), ValidationException.class);
        assertThat(e.getValidationFailure().errors())
            .containsOnly("Field 'passkey' must be 32 characters long and include only alphanumeric characters");
    }

    @Test
    void whenValidatingUpdate_givenUserWithPasskeyOfInvalidLength_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final Team team = generateTeam();

        final UserRequest user = generateUserRequest(
            "user",
            "user",
            "1234",
            Category.NVIDIA_GPU,
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            hardware.id(),
            team.id(),
            true
        );

        final User existingUser = User.create(
            userId++,
            "user",
            "user",
            DUMMY_PASSKEY,
            Category.NVIDIA_GPU,
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            hardware,
            team,
            Role.CAPTAIN
        );

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createTeam(team);
        foldingRepository.createUser(existingUser);

        final UserValidator userValidator = new UserValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(() -> userValidator.update(user, existingUser), ValidationException.class);
        assertThat(e.getValidationFailure().errors())
            .containsOnly("Field 'passkey' must be 32 characters long and include only alphanumeric characters");
    }

    @Test
    void whenValidatingUpdate_givenUserWithPasskeyContainingInvalidCharacters_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final Team team = generateTeam();

        final UserRequest user = generateUserRequest(
            "user",
            "user",
            "DummyPasskey1234567890123456789*",
            Category.NVIDIA_GPU,
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            hardware.id(),
            team.id(),
            true
        );

        final User existingUser = User.create(
            userId++,
            "user",
            "user",
            DUMMY_PASSKEY,
            Category.NVIDIA_GPU,
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            hardware,
            team,
            Role.CAPTAIN
        );

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createTeam(team);
        foldingRepository.createUser(existingUser);

        final UserValidator userValidator = new UserValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(() -> userValidator.update(user, existingUser), ValidationException.class);
        assertThat(e.getValidationFailure().errors())
            .containsOnly("Field 'passkey' must be 32 characters long and include only alphanumeric characters");
    }

    @Test
    void whenValidatingUpdate_givenUserWithNullProfileLink_thenSuccessResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final Team team = generateTeam();

        final UserRequest user = generateUserRequest(
            "user",
            "user",
            DUMMY_PASSKEY,
            Category.NVIDIA_GPU,
            null,
            VALID_LIVE_STATS_LINK,
            hardware.id(),
            team.id(),
            true
        );

        final User existingUser = User.create(
            userId++,
            "user",
            "user",
            DUMMY_PASSKEY,
            Category.NVIDIA_GPU,
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            hardware,
            team,
            Role.CAPTAIN
        );

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

        final UserRequest user = generateUserRequest(
            "user",
            "user",
            DUMMY_PASSKEY,
            Category.NVIDIA_GPU,
            "invalidUrl",
            VALID_LIVE_STATS_LINK,
            hardware.id(),
            team.id(),
            true
        );

        final User existingUser = User.create(
            userId++,
            "user",
            "user",
            DUMMY_PASSKEY,
            Category.NVIDIA_GPU,
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            hardware,
            team,
            Role.CAPTAIN
        );

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createTeam(team);
        foldingRepository.createUser(existingUser);

        final UserValidator userValidator = new UserValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(() -> userValidator.update(user, existingUser), ValidationException.class);
        assertThat(e.getValidationFailure().errors())
            .containsOnly("Field 'profileLink' is not a valid link: 'invalidUrl'");
    }

    @Test
    void whenValidatingUpdate_givenUserWithNullLiveStatsLink_thenSuccessResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final Team team = generateTeam();

        final UserRequest user = generateUserRequest(
            "user",
            "user",
            DUMMY_PASSKEY,
            Category.NVIDIA_GPU,
            VALID_PROFILE_LINK,
            null,
            hardware.id(),
            team.id(),
            true
        );

        final User existingUser = User.create(
            userId++,
            "user",
            "user",
            DUMMY_PASSKEY,
            Category.NVIDIA_GPU,
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            hardware,
            team,
            Role.CAPTAIN
        );

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

        final UserRequest user = generateUserRequest(
            "user",
            "user",
            DUMMY_PASSKEY,
            Category.NVIDIA_GPU,
            VALID_PROFILE_LINK,
            "invalidUrl",
            hardware.id(),
            team.id(),
            true
        );

        final User existingUser = User.create(
            userId++,
            "user",
            "user",
            DUMMY_PASSKEY,
            Category.NVIDIA_GPU,
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            hardware,
            team,
            Role.CAPTAIN
        );

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createTeam(team);
        foldingRepository.createUser(existingUser);

        final UserValidator userValidator = new UserValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(() -> userValidator.update(user, existingUser), ValidationException.class);
        assertThat(e.getValidationFailure().errors())
            .containsOnly("Field 'liveStatsLink' is not a valid link: 'invalidUrl'");
    }

    @Test
    void whenValidatingUpdate_givenUserWithFoldingUserNameAndPasskeyAlreadyExists_thenFailureResponseIsReturned() {
        final Hardware otherHardware = generateHardware();
        final Team otherTeam = generateTeam();

        final User otherUser = User.create(
            userId++,
            "user",
            "user",
            DUMMY_PASSKEY,
            Category.NVIDIA_GPU,
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            otherHardware,
            otherTeam,
            Role.CAPTAIN
        );

        final Hardware hardware = generateHardware();
        final Team team = generateTeam();

        final UserRequest user = generateUserRequest(
            "user",
            "user",
            DUMMY_PASSKEY,
            Category.NVIDIA_GPU,
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            hardware.id(),
            team.id(),
            true
        );

        final User existingUser = User.create(
            userId++,
            "differentUser",
            "user",
            DUMMY_PASSKEY,
            Category.NVIDIA_GPU,
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            hardware,
            team,
            Role.CAPTAIN
        );

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createHardware(otherHardware);
        foldingRepository.createTeam(team);
        foldingRepository.createTeam(otherTeam);
        foldingRepository.createUser(existingUser);
        foldingRepository.createUser(otherUser);

        final UserValidator userValidator = new UserValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final ConflictException e = catchThrowableOfType(() -> userValidator.update(user, existingUser), ConflictException.class);
        assertThat(e.getConflictFailure().conflictingAttributes())
            .containsOnly(
                "foldingUserName",
                "passkey"
            );

        assertThat(e.getConflictFailure().conflictingObject())
            .isNotNull();
    }

    @Test
    void whenValidatingUpdate_givenUserWithFoldingUserNameAndPasskeyExists_andExistingUserIsTheOneBeingUpdated_thenSuccessResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final Team team = generateTeam();
        final int commonUserId = userId++;

        final UserRequest user = generateUserRequest(
            "user",
            "user",
            DUMMY_PASSKEY,
            Category.NVIDIA_GPU,
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            hardware.id(),
            team.id(),
            true
        );

        final User existingUser = User.create(
            commonUserId,
            "user",
            "user",
            DUMMY_PASSKEY,
            Category.NVIDIA_GPU,
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            hardware,
            team,
            Role.CAPTAIN
        );

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

        final UserRequest user = generateUserRequest(
            "user",
            "user",
            DUMMY_PASSKEY,
            Category.NVIDIA_GPU,
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            hardware.id(),
            team.id(),
            true
        );

        final User existingUser = User.create(
            1,
            "user",
            "user",
            DUMMY_PASSKEY,
            Category.NVIDIA_GPU,
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            hardware,
            team,
            Role.CAPTAIN
        );

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

        final User currentCaptain = User.create(
            2,
            "otherUser",
            "otherUser",
            DUMMY_PASSKEY,
            Category.AMD_GPU,
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            hardware,
            team,
            Role.CAPTAIN
        );

        final UserRequest user = generateUserRequest(
            "user",
            "user",
            DUMMY_PASSKEY,
            Category.NVIDIA_GPU,
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            hardware.id(),
            team.id(),
            true
        );

        final User existingUser = User.create(
            1,
            "user",
            "user",
            DUMMY_PASSKEY,
            Category.NVIDIA_GPU,
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            hardware,
            oldTeam,
            Role.CAPTAIN
        );

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

        final User currentCaptain = User.create(
            2,
            "otherUser",
            "otherUser",
            DUMMY_PASSKEY,
            Category.NVIDIA_GPU,
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            hardware,
            team,
            Role.CAPTAIN
        );

        final UserRequest user = generateUserRequest(
            "user",
            "user",
            DUMMY_PASSKEY,
            Category.NVIDIA_GPU,
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            hardware.id(),
            team.id(),
            true
        );

        final User existingUser = User.create(
            1,
            "user",
            "user",
            DUMMY_PASSKEY,
            Category.NVIDIA_GPU,
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            hardware,
            team,
            Role.MEMBER
        );

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

        final User currentCaptain = User.create(
            2,
            "user1",
            "user1",
            "DummyPasskey12345678901234567891",
            Category.NVIDIA_GPU,
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            hardware,
            team,
            Role.CAPTAIN
        );

        final UserRequest user = generateUserRequest(
            "user",
            "user",
            DUMMY_PASSKEY,
            Category.NVIDIA_GPU,
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            hardware.id(),
            team.id(),
            false
        );

        final User existingUser = User.create(
            userId++,
            "user",
            "user",
            DUMMY_PASSKEY,
            Category.NVIDIA_GPU,
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            hardware,
            team,
            Role.MEMBER
        );

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

        final UserRequest user = generateUserRequest(
            "user",
            "user",
            DUMMY_PASSKEY,
            Category.NVIDIA_GPU,
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            hardware.id(),
            team.id(),
            false
        );

        final User existingUser = User.create(
            userId++,
            "user",
            "user",
            DUMMY_PASSKEY,
            Category.NVIDIA_GPU,
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            hardware,
            oldTeam,
            Role.MEMBER
        );

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createTeam(team);
        foldingRepository.createUser(existingUser);

        for (int i = 0; i < Category.maximumPermittedAmountForAllCategories(); i++) {
            foldingRepository.createUser(
                User.create(
                    userId++,
                    "user" + i,
                    "user" + i,
                    "DummyPasskey12345678901234567891",
                    Category.WILDCARD,
                    VALID_PROFILE_LINK,
                    VALID_LIVE_STATS_LINK,
                    hardware,
                    team,
                    Role.MEMBER
                )
            );
        }

        final UserValidator userValidator = new UserValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(() -> userValidator.update(user, existingUser), ValidationException.class);
        assertThat(e.getValidationFailure().errors())
            .containsOnly(String.format("Team '%1$s' has %2$s users, maximum permitted is %2$s", team.teamName(),
                Category.maximumPermittedAmountForAllCategories())
            );
    }

    @Test
    void whenValidatingUpdate_givenTeamIsUpdated_andTeamAlreadyHasMaximumUsersInCategory_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final Team team = generateTeam();
        final Team oldTeam = generateTeam();

        final UserRequest user = generateUserRequest(
            "user",
            "user",
            DUMMY_PASSKEY,
            Category.NVIDIA_GPU,
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            hardware.id(),
            team.id(),
            true
        );

        final User existingUser = User.create(
            userId++,
            "user",
            "user",
            DUMMY_PASSKEY,
            Category.NVIDIA_GPU,
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            hardware,
            oldTeam,
            Role.MEMBER
        );

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createTeam(team);
        foldingRepository.createUser(existingUser);

        for (int i = 0; i < Category.NVIDIA_GPU.permittedUsers(); i++) {
            foldingRepository.createUser(
                User.create(
                    userId++,
                    "user" + i,
                    "user" + i,
                    "DummyPasskey12345678901234567891",
                    Category.NVIDIA_GPU,
                    VALID_PROFILE_LINK,
                    VALID_LIVE_STATS_LINK,
                    hardware,
                    team,
                    Role.MEMBER
                )
            );
        }

        final UserValidator userValidator = new UserValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final ValidationException e =
            catchThrowableOfType(() -> userValidator.update(user, existingUser),
                ValidationException.class);
        assertThat(e.getValidationFailure().errors())
            .containsOnly(String.format("Team '%1$s' already has %3$s users in category '%2$s', only %3$s permitted", team.teamName(),
                Category.NVIDIA_GPU, Category.NVIDIA_GPU.permittedUsers())
            );
    }

    @Test
    void whenValidatingUpdate_givenCategoryIsUpdated_andTeamAlreadyHasMaximumUsersInCategory_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final Team team = generateTeam();

        final UserRequest user = generateUserRequest(
            "user",
            "user",
            DUMMY_PASSKEY,
            Category.NVIDIA_GPU,
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            hardware.id(),
            team.id(),
            true
        );

        final User existingUser = User.create(
            userId++,
            "user",
            "user",
            DUMMY_PASSKEY,
            Category.AMD_GPU,
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            hardware,
            team,
            Role.MEMBER
        );

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createTeam(team);
        foldingRepository.createUser(existingUser);

        for (int i = 0; i < Category.NVIDIA_GPU.permittedUsers(); i++) {
            foldingRepository.createUser(
                User.create(
                    userId++,
                    "user" + i,
                    "user" + i,
                    "DummyPasskey12345678901234567891",
                    Category.NVIDIA_GPU,
                    VALID_PROFILE_LINK,
                    VALID_LIVE_STATS_LINK,
                    hardware,
                    team,
                    Role.MEMBER
                )
            );
        }

        final UserValidator userValidator = new UserValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final ValidationException e =
            catchThrowableOfType(() -> userValidator.update(user, existingUser), ValidationException.class);
        assertThat(e.getValidationFailure().errors())
            .containsOnly(String.format("Team '%1$s' already has %3$s users in category '%2$s', only %3$s permitted", team.teamName(),
                Category.NVIDIA_GPU, Category.NVIDIA_GPU.permittedUsers())
            );
    }

    @Test
    void whenValidatingUpdate_givenUserWithMisMatchingHardwareMakeAndCategory_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final Team team = generateTeam();

        final UserRequest user = generateUserRequest(
            "user",
            "user",
            DUMMY_PASSKEY,
            Category.AMD_GPU,
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            hardware.id(),
            team.id(),
            true
        );

        final User existingUser = User.create(
            userId++,
            "user",
            "user",
            DUMMY_PASSKEY,
            Category.NVIDIA_GPU,
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            hardware,
            team,
            Role.MEMBER
        );

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createTeam(team);
        foldingRepository.createUser(existingUser);

        final UserValidator userValidator = new UserValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(() -> userValidator.update(user, existingUser), ValidationException.class);
        assertThat(e.getValidationFailure().errors())
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

        final UserRequest user = generateUserRequest(
            "user",
            "user",
            DUMMY_PASSKEY,
            Category.AMD_GPU,
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            hardware.id(),
            team.id(),
            true
        );

        final User existingUser = User.create(
            userId++,
            "user",
            "user",
            DUMMY_PASSKEY,
            Category.NVIDIA_GPU,
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            hardware,
            team,
            Role.MEMBER
        );

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createTeam(team);
        foldingRepository.createUser(existingUser);

        final UserValidator userValidator = new UserValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(() -> userValidator.update(user, existingUser), ValidationException.class);
        assertThat(e.getValidationFailure().errors())
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

        final UserRequest user = generateUserRequest(
            "user",
            "user",
            DUMMY_PASSKEY,
            Category.NVIDIA_GPU,
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            1,
            team.id(),
            true
        );

        final User existingUser = User.create(
            userId++,
            "user",
            "user",
            DUMMY_PASSKEY,
            Category.NVIDIA_GPU,
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            hardware,
            team,
            Role.CAPTAIN
        );

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createTeam(team);
        foldingRepository.createUser(existingUser);

        final UserValidator userValidator = new UserValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(() -> userValidator.update(user, existingUser), ValidationException.class);
        assertThat(e.getValidationFailure().errors())
            .containsOnly(String.format("Field 'hardwareId' must be one of: [%s: %s]", hardware.id(), hardware.hardwareName()));
    }

    @Test
    void whenValidatingUpdate_givenNoHardwareExistsOnTheSystem_thenFailureResponseIsReturned() {
        final Team team = generateTeam();

        final UserRequest user = generateUserRequest(
            "user",
            "user",
            DUMMY_PASSKEY,
            Category.NVIDIA_GPU,
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            1,
            team.id(),
            true
        );

        final User existingUser = User.create(
            userId++,
            "user",
            "user",
            DUMMY_PASSKEY,
            Category.NVIDIA_GPU,
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            generateHardware(),
            team,
            Role.CAPTAIN
        );

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createTeam(team);
        foldingRepository.createUser(existingUser);

        final UserValidator userValidator = new UserValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(() -> userValidator.update(user, existingUser), ValidationException.class);
        assertThat(e.getValidationFailure().errors())
            .containsOnly("No hardwares exist on the system");
    }

    @Test
    void whenValidatingUpdate_givenUserWithTeamWithNonExistingId_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final Team team = Team.create(
            99,
            "teamName",
            "teamDescription",
            "https://www.google.com"
        );

        final UserRequest user = generateUserRequest(
            "user",
            "user",
            DUMMY_PASSKEY,
            Category.NVIDIA_GPU,
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            hardware.id(),
            1,
            true
        );

        final User existingUser = User.create(
            userId++,
            "user",
            "user",
            DUMMY_PASSKEY,
            Category.NVIDIA_GPU,
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            hardware,
            team,
            Role.CAPTAIN
        );

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createTeam(team);
        foldingRepository.createUser(existingUser);

        final UserValidator userValidator = new UserValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(() -> userValidator.update(user, existingUser), ValidationException.class);
        assertThat(e.getValidationFailure().errors())
            .containsOnly(String.format("Field 'teamId' must be one of: [%s: %s]", team.id(), team.teamName()));
    }

    @Test
    void whenValidatingUpdate_givenNoTeamsExistOnTheSystem_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();

        final UserRequest user = generateUserRequest(
            "user",
            "user",
            DUMMY_PASSKEY,
            Category.NVIDIA_GPU,
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            hardware.id(),
            1,
            true
        );

        final User existingUser = User.create(
            userId++,
            "user",
            "user",
            DUMMY_PASSKEY,
            Category.NVIDIA_GPU,
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            hardware,
            generateTeam(),
            Role.CAPTAIN
        );

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createUser(existingUser);

        final UserValidator userValidator = new UserValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(() -> userValidator.update(user, existingUser), ValidationException.class);
        assertThat(e.getValidationFailure().errors())
            .containsOnly("No teams exist on the system");
    }

    @Test
    void whenValidatingUpdate_givenUserHasNoWorkUnitsCompleted_andFoldingUserNameAndPasskeyAreUnchanged_thenSuccessResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final Team team = generateTeam();

        final UserRequest user = generateUserRequest(
            "user",
            "user",
            DUMMY_PASSKEY,
            Category.NVIDIA_GPU,
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            hardware.id(),
            team.id(),
            true
        );

        final User existingUser = User.create(
            userId++,
            "user",
            "user",
            DUMMY_PASSKEY,
            Category.NVIDIA_GPU,
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            hardware,
            team,
            Role.CAPTAIN
        );

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

        final UserRequest user = generateUserRequest(
            "user",
            "user",
            DUMMY_PASSKEY,
            Category.NVIDIA_GPU,
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            hardware.id(),
            team.id(),
            true
        );

        final User existingUser = User.create(
            userId++,
            "oldUserName",
            "user",
            DUMMY_PASSKEY,
            Category.NVIDIA_GPU,
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            hardware,
            team,
            Role.CAPTAIN
        );

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createTeam(team);
        foldingRepository.createUser(existingUser);

        final UserValidator userValidator = new UserValidator(foldingRepository, new NoUnitsFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(() -> userValidator.update(user, existingUser), ValidationException.class);
        assertThat(e.getValidationFailure().errors())
            .containsOnly(String.format(
                "User '%s' has 0 Work Units with passkey '%s', there must be at least one completed Work Unit before adding the user",
                user.displayName(), user.passkey())
            );
    }

    @Test
    void whenValidatingUpdate_givenUserWorkUnitsCannotBeAccessed_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final Team team = generateTeam();

        final UserRequest user = generateUserRequest(
            "user",
            "user",
            DUMMY_PASSKEY,
            Category.NVIDIA_GPU,
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            hardware.id(),
            team.id(),
            true
        );

        final User existingUser = User.create(
            userId++,
            "oldUserName",
            "user",
            DUMMY_PASSKEY,
            Category.NVIDIA_GPU,
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            hardware,
            team,
            Role.CAPTAIN
        );

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createTeam(team);
        foldingRepository.createUser(existingUser);

        final UserValidator userValidator = new UserValidator(foldingRepository, new ExternalConnectionFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(() -> userValidator.update(user, existingUser), ValidationException.class);
        assertThat(e.getValidationFailure().errors())
            .containsOnly(String.format("Unable to connect to 'https://www.google.com' to check stats for user '%s': Error connecting",
                user.displayName())
            );
    }

    @Test
    void whenValidatingUpdate_givenUnexpectedErrorWhenAccessingUserWorkUnits_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final Team team = generateTeam();

        final UserRequest user = generateUserRequest(
            "user",
            "user",
            DUMMY_PASSKEY,
            Category.NVIDIA_GPU,
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            hardware.id(),
            team.id(),
            true
        );

        final User existingUser = User.create(
            userId++,
            "oldUserName",
            "user",
            DUMMY_PASSKEY,
            Category.NVIDIA_GPU,
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            hardware,
            team,
            Role.CAPTAIN
        );

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createTeam(team);
        foldingRepository.createUser(existingUser);

        final UserValidator userValidator = new UserValidator(foldingRepository, new UnexpectedExceptionFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(() -> userValidator.update(user, existingUser), ValidationException.class);
        assertThat(e.getValidationFailure().errors())
            .containsOnly(String.format("Unable to check stats for user '%s': Error", user.displayName()));
    }

    @Test
    void whenValidatingUpdate_givenMultipleFailures_thenFailureResponseIsReturned_andAllValidationErrorsAreReturned() {
        final Hardware hardware = generateHardware();
        final Team team = generateTeam();

        final UserRequest user = generateUserRequest(
            null,
            null,
            null,
            Category.INVALID,
            "invalidUrl",
            "invalidUrl",
            hardware.id(),
            team.id(),
            true
        );

        final User existingUser = User.create(
            userId++,
            "user",
            "user",
            DUMMY_PASSKEY,
            Category.NVIDIA_GPU,
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            hardware,
            team,
            Role.CAPTAIN
        );

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(hardware);
        foldingRepository.createTeam(team);
        foldingRepository.createUser(existingUser);

        final UserValidator userValidator = new UserValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(() -> userValidator.update(user, existingUser), ValidationException.class);
        assertThat(e.getValidationFailure().errors())
            .containsOnly(
                "Field 'foldingUserName' must have at least one alphanumeric character, or an underscore, period or hyphen",
                "Field 'displayName' must not be empty",
                "Field 'passkey' must be 32 characters long and include only alphanumeric characters",
                "Field 'category' must be one of: [AMD_GPU, NVIDIA_GPU, WILDCARD]",
                "Field 'profileLink' is not a valid link: 'invalidUrl'",
                "Field 'liveStatsLink' is not a valid link: 'invalidUrl'"
            );
    }

    @Test
    void whenValidatingDelete_andUserIsNotCaptain_thenSuccessResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final Team team = generateTeam();

        final User userToDelete = User.create(
            userId++,
            "user",
            "user",
            DUMMY_PASSKEY,
            Category.NVIDIA_GPU,
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            hardware,
            team,
            Role.MEMBER
        );

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

        final User userToDelete = User.create(
            userId++,
            "user",
            "user",
            DUMMY_PASSKEY,
            Category.NVIDIA_GPU,
            VALID_PROFILE_LINK,
            VALID_LIVE_STATS_LINK,
            hardware,
            team,
            Role.CAPTAIN
        );

        final FoldingRepository foldingRepository = new MockFoldingRepository();

        final UserValidator userValidator = new UserValidator(foldingRepository, new ValidFoldingStatsRetriever());
        final ValidationException e = catchThrowableOfType(() -> userValidator.delete(userToDelete), ValidationException.class);
        assertThat(e.getValidationFailure().errors())
            .containsOnly(String.format("Cannot delete user '%s' since they are team captain", userToDelete.displayName()));
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

    private static UserRequest generateUserRequest(final String foldingUserName,
                                                   final String displayName,
                                                   final String passkey,
                                                   final Category category,
                                                   final String profileLink,
                                                   final String liveStatsLink,
                                                   final int hardwareId,
                                                   final int teamId,
                                                   final boolean isCaptain) {
        return new UserRequest(
            foldingUserName,
            displayName,
            passkey,
            category.toString(),
            profileLink,
            liveStatsLink,
            hardwareId,
            teamId,
            isCaptain
        );
    }
}

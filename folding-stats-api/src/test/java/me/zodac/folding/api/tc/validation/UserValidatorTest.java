/*
 * MIT License
 *
 * Copyright (c) 2021 zodac.me
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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import me.zodac.folding.api.exception.ExternalConnectionException;
import me.zodac.folding.api.stats.FoldingStatsDetails;
import me.zodac.folding.api.stats.FoldingStatsRetriever;
import me.zodac.folding.api.tc.Category;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.HardwareMake;
import me.zodac.folding.api.tc.HardwareType;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.tc.stats.Stats;
import me.zodac.folding.api.tc.stats.UserStats;
import me.zodac.folding.api.util.DateTimeUtils;
import me.zodac.folding.rest.api.tc.request.UserRequest;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link UserValidator}.
 */
class UserValidatorTest {

    private static int hardwareId = 1;
    private static int teamId = 1;

    @Test
    void whenValidatingCreate_givenValidUser_thenSuccessResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final Team team = generateTeam();

        final UserRequest user = UserRequest.builder()
            .foldingUserName("-folding.Name_1")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU.toString())
            .profileLink("http://www.google.com")
            .liveStatsLink("http://www.google.com")
            .userIsCaptain(true)
            .hardwareId(hardware.getId())
            .teamId(team.getId())
            .build();

        final UserValidator userValidator = UserValidator.create(new ValidFoldingStatsRetriever());
        final ValidationResult<User> response = userValidator.validateCreate(user,
            Collections.emptyList(),
            List.of(hardware),
            List.of(team)
        );

        assertThat(response.isFailure())
            .as("Expected validation to pass, instead failed with errors: " + response.getErrors())
            .isFalse();
    }

    @Test
    void whenValidatingCreate_givenNullUser_thenFailureResponseIsReturned() {
        final UserValidator userValidator = UserValidator.create(new ValidFoldingStatsRetriever());
        final ValidationResult<User> response = userValidator.validateCreate(null,
            Collections.emptyList(),
            Collections.emptyList(),
            Collections.emptyList()
        );

        assertThat(response.isFailure())
            .isTrue();

        assertThat(response.getErrors())
            .containsOnly("Payload is null");
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
            .profileLink("http://www.google.com")
            .liveStatsLink("http://www.google.com")
            .userIsCaptain(true)
            .hardwareId(hardware.getId())
            .teamId(team.getId())
            .build();

        final UserValidator userValidator = UserValidator.create(new ValidFoldingStatsRetriever());
        final ValidationResult<User> response = userValidator.validateCreate(user,
            Collections.emptyList(),
            List.of(hardware),
            List.of(team)
        );

        assertThat(response.isFailure())
            .isTrue();

        assertThat(response.getErrors())
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
            .profileLink("http://www.google.com")
            .liveStatsLink("http://www.google.com")
            .userIsCaptain(true)
            .hardwareId(hardware.getId())
            .teamId(team.getId())
            .build();

        final UserValidator userValidator = UserValidator.create(new ValidFoldingStatsRetriever());
        final ValidationResult<User> response = userValidator.validateCreate(user,
            Collections.emptyList(),
            List.of(hardware),
            List.of(team)
        );

        assertThat(response.isFailure())
            .isTrue();

        assertThat(response.getErrors())
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
            .profileLink("http://www.google.com")
            .liveStatsLink("http://www.google.com")
            .userIsCaptain(true)
            .hardwareId(hardware.getId())
            .teamId(team.getId())
            .build();

        final UserValidator userValidator = UserValidator.create(new ValidFoldingStatsRetriever());
        final ValidationResult<User> response = userValidator.validateCreate(user,
            Collections.emptyList(),
            List.of(hardware),
            List.of(team)
        );

        assertThat(response.isFailure())
            .isTrue();

        assertThat(response.getErrors())
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
            .profileLink("http://www.google.com")
            .liveStatsLink("http://www.google.com")
            .userIsCaptain(true)
            .hardwareId(hardware.getId())
            .teamId(team.getId())
            .build();

        final UserValidator userValidator = UserValidator.create(new ValidFoldingStatsRetriever());
        final ValidationResult<User> response = userValidator.validateCreate(user,
            Collections.emptyList(),
            List.of(hardware),
            List.of(team)
        );

        assertThat(response.isFailure())
            .isTrue();

        assertThat(response.getErrors())
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
            .profileLink("http://www.google.com")
            .liveStatsLink("http://www.google.com")
            .userIsCaptain(true)
            .hardwareId(hardware.getId())
            .teamId(team.getId())
            .build();

        final UserValidator userValidator = UserValidator.create(new ValidFoldingStatsRetriever());
        final ValidationResult<User> response = userValidator.validateCreate(user,
            Collections.emptyList(),
            List.of(hardware),
            List.of(team)
        );

        assertThat(response.isFailure())
            .isTrue();

        assertThat(response.getErrors())
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
            .profileLink("http://www.google.com")
            .liveStatsLink("http://www.google.com")
            .userIsCaptain(true)
            .hardwareId(hardware.getId())
            .teamId(team.getId())
            .build();

        final UserValidator userValidator = UserValidator.create(new ValidFoldingStatsRetriever());
        final ValidationResult<User> response = userValidator.validateCreate(user,
            Collections.emptyList(),
            List.of(hardware),
            List.of(team)
        );

        assertThat(response.isFailure())
            .isTrue();

        assertThat(response.getErrors())
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
            .liveStatsLink("http://www.google.com")
            .userIsCaptain(true)
            .hardwareId(hardware.getId())
            .teamId(team.getId())
            .build();

        final UserValidator userValidator = UserValidator.create(new ValidFoldingStatsRetriever());
        final ValidationResult<User> response = userValidator.validateCreate(user,
            Collections.emptyList(),
            List.of(hardware),
            List.of(team)
        );

        assertThat(response.isFailure())
            .as("Expected validation to pass, instead failed with errors: " + response.getErrors())
            .isFalse();
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
            .liveStatsLink("http://www.google.com")
            .userIsCaptain(true)
            .hardwareId(hardware.getId())
            .teamId(team.getId())
            .build();

        final UserValidator userValidator = UserValidator.create(new ValidFoldingStatsRetriever());
        final ValidationResult<User> response = userValidator.validateCreate(user,
            Collections.emptyList(),
            List.of(hardware),
            List.of(team)
        );

        assertThat(response.isFailure())
            .isTrue();

        assertThat(response.getErrors())
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
            .profileLink("http://www.google.com")
            .liveStatsLink(null)
            .userIsCaptain(true)
            .hardwareId(hardware.getId())
            .teamId(team.getId())
            .build();

        final UserValidator userValidator = UserValidator.create(new ValidFoldingStatsRetriever());
        final ValidationResult<User> response = userValidator.validateCreate(user,
            Collections.emptyList(),
            List.of(hardware),
            List.of(team)
        );

        assertThat(response.isFailure())
            .as("Expected validation to pass, instead failed with errors: " + response.getErrors())
            .isFalse();
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
            .profileLink("http://www.google.com")
            .liveStatsLink("invalidUrl")
            .userIsCaptain(true)
            .hardwareId(hardware.getId())
            .teamId(team.getId())
            .build();

        final UserValidator userValidator = UserValidator.create(new ValidFoldingStatsRetriever());
        final ValidationResult<User> response = userValidator.validateCreate(user,
            Collections.emptyList(),
            List.of(hardware),
            List.of(team)
        );

        assertThat(response.isFailure())
            .isTrue();

        assertThat(response.getErrors())
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
            .profileLink("http://www.google.com")
            .liveStatsLink("http://www.google.com")
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
            .profileLink("http://www.google.com")
            .liveStatsLink("http://www.google.com")
            .userIsCaptain(true)
            .hardwareId(hardware.getId())
            .teamId(team.getId())
            .build();

        final UserValidator userValidator = UserValidator.create(new ValidFoldingStatsRetriever());
        final ValidationResult<User> response = userValidator.validateCreate(user,
            List.of(otherUser),
            List.of(hardware, otherHardware),
            List.of(team, otherTeam)
        );

        assertThat(response.isFailure())
            .as("Expected validation to pass, instead failed with errors: " + response.getErrors())
            .isFalse();
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
            .profileLink("http://www.google.com")
            .liveStatsLink("http://www.google.com")
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
            .profileLink("http://www.google.com")
            .liveStatsLink("http://www.google.com")
            .userIsCaptain(true)
            .hardwareId(hardware.getId())
            .teamId(team.getId())
            .build();

        final UserValidator userValidator = UserValidator.create(new ValidFoldingStatsRetriever());
        final ValidationResult<User> response = userValidator.validateCreate(user,
            List.of(otherUser),
            List.of(hardware, otherHardware),
            List.of(team, otherTeam)
        );

        assertThat(response.isFailure())
            .isTrue();

        assertThat(response.getErrors())
            .containsOnly("Payload conflicts with an existing object on: [foldingUserName, passkey]");
    }

    @Test
    void whenValidatingCreate_givenUserWithMisMatchingHardwareMakeAndCategory_thenFailureResponseIsReturned() {
        final Hardware hardware = Hardware.builder()
            .id(hardwareId++)
            .hardwareName("hardwareName")
            .displayName("displayName")
            .hardwareMake(HardwareMake.NVIDIA)
            .hardwareType(HardwareType.GPU)
            .multiplier(1.00D)
            .averagePpd(1L)
            .build();
        final Team team = generateTeam();

        final UserRequest user = UserRequest.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.AMD_GPU.toString())
            .profileLink("http://www.google.com")
            .liveStatsLink("http://www.google.com")
            .userIsCaptain(true)
            .hardwareId(hardware.getId())
            .teamId(team.getId())
            .build();

        final UserValidator userValidator = UserValidator.create(new ValidFoldingStatsRetriever());
        final ValidationResult<User> response = userValidator.validateCreate(user,
            Collections.emptyList(),
            List.of(hardware),
            List.of(team)
        );

        assertThat(response.isFailure())
            .isTrue();

        assertThat(response.getErrors())
            .containsOnly(String.format("Category '%s' cannot be filled by hardware of make '%s', must be one of: %s", Category.AMD_GPU,
                hardware.getHardwareMake(), Category.AMD_GPU.supportedHardwareMakes()));
    }

    @Test
    void whenValidatingCreate_givenUserWithMisMatchingHardwareTypeAndCategory_thenFailureResponseIsReturned() {
        final Hardware hardware = Hardware.builder()
            .id(hardwareId++)
            .hardwareName("hardwareName")
            .displayName("displayName")
            .hardwareMake(HardwareMake.AMD)
            .hardwareType(HardwareType.CPU)
            .multiplier(1.00D)
            .averagePpd(1L)
            .build();
        final Team team = generateTeam();

        final UserRequest user = UserRequest.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.AMD_GPU.toString())
            .profileLink("http://www.google.com")
            .liveStatsLink("http://www.google.com")
            .userIsCaptain(true)
            .hardwareId(hardware.getId())
            .teamId(team.getId())
            .build();

        final UserValidator userValidator = UserValidator.create(new ValidFoldingStatsRetriever());
        final ValidationResult<User> response = userValidator.validateCreate(user,
            Collections.emptyList(),
            List.of(hardware),
            List.of(team)
        );

        assertThat(response.isFailure())
            .isTrue();

        assertThat(response.getErrors())
            .containsOnly(String.format("Category '%s' cannot be filled by hardware of type '%s', must be one of: %s", Category.AMD_GPU,
                hardware.getHardwareType(), Category.AMD_GPU.supportedHardwareTypes()));
    }

    @Test
    void whenValidatingCreate_givenUserWithHardwareWithNonExistingId_thenFailureResponseIsReturned() {
        final Hardware hardware = Hardware.builder()
            .id(99)
            .hardwareName("hardwareName")
            .displayName("displayName")
            .hardwareMake(HardwareMake.NVIDIA)
            .hardwareType(HardwareType.GPU)
            .multiplier(1.00D)
            .averagePpd(1L)
            .build();
        final Team team = generateTeam();

        final UserRequest user = UserRequest.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU.toString())
            .profileLink("http://www.google.com")
            .liveStatsLink("http://www.google.com")
            .userIsCaptain(true)
            .hardwareId(1)
            .teamId(team.getId())
            .build();

        final UserValidator userValidator = UserValidator.create(new ValidFoldingStatsRetriever());
        final ValidationResult<User> response = userValidator.validateCreate(user,
            Collections.emptyList(),
            List.of(hardware),
            List.of(team)
        );

        assertThat(response.isFailure())
            .isTrue();

        assertThat(response.getErrors())
            .containsOnly(String.format("Field 'hardwareId' must be one of: [%s: %s]", hardware.getId(), hardware.getHardwareName()));
    }

    @Test
    void whenValidatingCreate_givenNoHardwareExistsOnSystem_thenFailureResponseIsReturned() {
        final Team team = generateTeam();

        final UserRequest user = UserRequest.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU.toString())
            .profileLink("http://www.google.com")
            .liveStatsLink("http://www.google.com")
            .userIsCaptain(true)
            .hardwareId(1)
            .teamId(team.getId())
            .build();

        final UserValidator userValidator = UserValidator.create(new ValidFoldingStatsRetriever());
        final ValidationResult<User> response = userValidator.validateCreate(user,
            Collections.emptyList(),
            Collections.emptyList(),
            List.of(team)
        );

        assertThat(response.isFailure())
            .isTrue();

        assertThat(response.getErrors())
            .containsOnly("No hardware exist on the system");
    }

    @Test
    void whenValidatingCreate_givenUserWithTeamWithNonExistingId_thenFailureResponseIsReturned() {
        final Hardware hardware = Hardware.builder()
            .id(99)
            .hardwareName("hardwareName")
            .displayName("displayName")
            .hardwareMake(HardwareMake.NVIDIA)
            .hardwareType(HardwareType.GPU)
            .multiplier(1.00D)
            .averagePpd(1L)
            .build();
        final Team team = generateTeam();

        final UserRequest user = UserRequest.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU.toString())
            .profileLink("http://www.google.com")
            .liveStatsLink("http://www.google.com")
            .userIsCaptain(true)
            .hardwareId(hardware.getId())
            .teamId(1)
            .build();

        final UserValidator userValidator = UserValidator.create(new ValidFoldingStatsRetriever());
        final ValidationResult<User> response = userValidator.validateCreate(user,
            Collections.emptyList(),
            List.of(hardware),
            List.of(team)
        );

        assertThat(response.isFailure())
            .isTrue();

        assertThat(response.getErrors())
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
            .profileLink("http://www.google.com")
            .liveStatsLink("http://www.google.com")
            .userIsCaptain(true)
            .hardwareId(hardware.getId())
            .teamId(1)
            .build();

        final UserValidator userValidator = UserValidator.create(new ValidFoldingStatsRetriever());
        final ValidationResult<User> response = userValidator.validateCreate(user,
            Collections.emptyList(),
            List.of(hardware),
            Collections.emptyList()
        );

        assertThat(response.isFailure())
            .isTrue();

        assertThat(response.getErrors())
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
            .profileLink("http://www.google.com")
            .liveStatsLink("http://www.google.com")
            .userIsCaptain(true)
            .hardware(hardware)
            .team(team)
            .build();

        final UserRequest user = UserRequest.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU.toString())
            .profileLink("http://www.google.com")
            .liveStatsLink("http://www.google.com")
            .userIsCaptain(true)
            .hardwareId(hardware.getId())
            .teamId(team.getId())
            .build();

        final UserValidator userValidator = UserValidator.create(new ValidFoldingStatsRetriever());
        final ValidationResult<User> response = userValidator.validateCreate(user,
            List.of(currentCaptain),
            List.of(hardware),
            List.of(team)
        );

        assertThat(response.isFailure())
            .as("Expected validation to pass, instead failed with errors: " + response.getErrors())
            .isFalse();
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
            .profileLink("http://www.google.com")
            .liveStatsLink("http://www.google.com")
            .userIsCaptain(true)
            .hardware(hardware)
            .team(team)
            .build();

        final UserRequest user = UserRequest.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU.toString())
            .profileLink("http://www.google.com")
            .liveStatsLink("http://www.google.com")
            .userIsCaptain(false)
            .hardwareId(hardware.getId())
            .teamId(team.getId())
            .build();

        final UserValidator userValidator = UserValidator.create(new ValidFoldingStatsRetriever());
        final ValidationResult<User> response = userValidator.validateCreate(user,
            List.of(currentCaptain),
            List.of(hardware),
            List.of(team)
        );

        assertThat(response.isFailure())
            .as("Expected validation to pass, instead failed with errors: " + response.getErrors())
            .isFalse();
    }

    @Test
    void whenValidatingCreate_givenTeamAlreadyHasMaximumTotalUsers_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final Team team = generateTeam();

        final List<User> usersOnTeam = new ArrayList<>(Category.maximumPermittedAmountForAllCategories());

        for (int i = 0; i < Category.maximumPermittedAmountForAllCategories(); i++) {
            usersOnTeam.add(
                User.builder()
                    .foldingUserName("user" + 0)
                    .displayName("user" + 0)
                    .passkey("DummyPasskey12345678901234567891")
                    .category(Category.WILDCARD)
                    .profileLink("http://www.google.com")
                    .liveStatsLink("http://www.google.com")
                    .userIsCaptain(false)
                    .hardware(hardware)
                    .team(team)
                    .build()
            );
        }

        final UserRequest user = UserRequest.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU.toString())
            .profileLink("http://www.google.com")
            .liveStatsLink("http://www.google.com")
            .userIsCaptain(false)
            .hardwareId(hardware.getId())
            .teamId(team.getId())
            .build();

        final UserValidator userValidator = UserValidator.create(new ValidFoldingStatsRetriever());
        final ValidationResult<User> response = userValidator.validateCreate(user,
            usersOnTeam,
            List.of(hardware),
            List.of(team)
        );

        assertThat(response.isFailure())
            .isTrue();

        assertThat(response.getErrors())
            .containsOnly(String.format("Team '%s' has %s users, maximum permitted is %s", team.getTeamName(), usersOnTeam.size(),
                Category.maximumPermittedAmountForAllCategories()));
    }

    @Test
    void whenValidatingCreate_givenTeamAlreadyHasUsersInAnotherCategory_thenSuccessResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final Team team = generateTeam();

        final List<User> usersOnTeamInCategory = new ArrayList<>(Category.NVIDIA_GPU.permittedUsers());

        for (int i = 0; i < Category.AMD_GPU.permittedUsers(); i++) {
            usersOnTeamInCategory.add(
                User.builder()
                    .foldingUserName("user" + 0)
                    .displayName("user" + 0)
                    .passkey("DummyPasskey12345678901234567891")
                    .category(Category.AMD_GPU)
                    .profileLink("http://www.google.com")
                    .liveStatsLink("http://www.google.com")
                    .userIsCaptain(false)
                    .hardware(hardware)
                    .team(team)
                    .build()
            );
        }

        final UserRequest user = UserRequest.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU.toString())
            .profileLink("http://www.google.com")
            .liveStatsLink("http://www.google.com")
            .userIsCaptain(true)
            .hardwareId(hardware.getId())
            .teamId(team.getId())
            .build();

        final UserValidator userValidator = UserValidator.create(new ValidFoldingStatsRetriever());
        final ValidationResult<User> response = userValidator.validateCreate(user,
            usersOnTeamInCategory,
            List.of(hardware),
            List.of(team)
        );

        assertThat(response.isFailure())
            .as("Expected validation to pass, instead failed with errors: " + response.getErrors())
            .isFalse();
    }

    @Test
    void whenValidatingCreate_givenTeamAlreadyHasMaximumUsersInCategory_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final Team team = generateTeam();

        final List<User> usersOnTeamInCategory = new ArrayList<>(Category.NVIDIA_GPU.permittedUsers());

        for (int i = 0; i < Category.NVIDIA_GPU.permittedUsers(); i++) {
            usersOnTeamInCategory.add(
                User.builder()
                    .foldingUserName("user" + 0)
                    .displayName("user" + 0)
                    .passkey("DummyPasskey12345678901234567891")
                    .category(Category.NVIDIA_GPU)
                    .profileLink("http://www.google.com")
                    .liveStatsLink("http://www.google.com")
                    .userIsCaptain(false)
                    .hardware(hardware)
                    .team(team)
                    .build()
            );
        }

        final UserRequest user = UserRequest.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU.toString())
            .profileLink("http://www.google.com")
            .liveStatsLink("http://www.google.com")
            .userIsCaptain(true)
            .hardwareId(hardware.getId())
            .teamId(team.getId())
            .build();

        final UserValidator userValidator = UserValidator.create(new ValidFoldingStatsRetriever());
        final ValidationResult<User> response = userValidator.validateCreate(user,
            usersOnTeamInCategory,
            List.of(hardware),
            List.of(team)
        );

        assertThat(response.isFailure())
            .isTrue();

        assertThat(response.getErrors())
            .containsOnly(String.format("Team '%s' already has %s users in category '%s', only %s permitted", team.getTeamName(),
                usersOnTeamInCategory.size(), Category.NVIDIA_GPU, Category.NVIDIA_GPU.permittedUsers()));
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
            .profileLink("http://www.google.com")
            .liveStatsLink("http://www.google.com")
            .userIsCaptain(true)
            .hardwareId(hardware.getId())
            .teamId(team.getId())
            .build();

        final UserValidator userValidator = UserValidator.create(new NoUnitsFoldingStatsRetriever());
        final ValidationResult<User> response = userValidator.validateCreate(user,
            Collections.emptyList(),
            List.of(hardware),
            List.of(team)
        );

        assertThat(response.isFailure())
            .isTrue();

        assertThat(response.getErrors())
            .containsOnly("User 'user' has 0 Work Units with passkey 'DummyPasskey12345678901234567890', "
                + "there must be at least one completed Work Unit before adding the user");
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
            .profileLink("http://www.google.com")
            .liveStatsLink("http://www.google.com")
            .userIsCaptain(true)
            .hardwareId(hardware.getId())
            .teamId(team.getId())
            .build();

        final UserValidator userValidator = UserValidator.create(new ExternalConnectionFoldingStatsRetriever());
        final ValidationResult<User> response = userValidator.validateCreate(user,
            Collections.emptyList(),
            List.of(hardware),
            List.of(team)
        );

        assertThat(response.isFailure())
            .isTrue();

        assertThat(response.getErrors())
            .containsOnly("Unable to connect to 'http://www.google.com' to check stats for user 'user': Error connecting");
    }

    @Test
    void whenValidatingCreate_givenNoHardware_thenFailureResponseIsReturned_andNoOtherValidationErrorsAreReturned() {
        final Team team = generateTeam();

        final UserRequest user = UserRequest.builder()
            .foldingUserName(null)
            .displayName(null)
            .passkey("invalid")
            .category("invalid")
            .profileLink("invalid")
            .liveStatsLink("invalid")
            .userIsCaptain(true)
            .teamId(team.getId())
            .build();

        final UserValidator userValidator = UserValidator.create(new ValidFoldingStatsRetriever());
        final ValidationResult<User> response = userValidator.validateCreate(user,
            Collections.emptyList(),
            Collections.emptyList(),
            List.of(team)
        );

        assertThat(response.isFailure())
            .isTrue();

        assertThat(response.getErrors())
            .containsOnly("No hardware exist on the system");
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
            .hardwareId(hardware.getId())
            .teamId(team.getId())
            .build();

        final UserValidator userValidator = UserValidator.create(new ValidFoldingStatsRetriever());
        final ValidationResult<User> response = userValidator.validateCreate(user,
            Collections.emptyList(),
            Collections.emptyList(),
            List.of(team)
        );

        assertThat(response.isFailure())
            .isTrue();

        assertThat(response.getErrors())
            .containsOnly(
                "No hardware exist on the system",
                "No hardware exist on the system",
                "No hardware exist on the system",
                "No hardware exist on the system",
                "No hardware exist on the system",
                "No hardware exist on the system"
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
            .profileLink("http://www.google.com")
            .liveStatsLink("http://www.google.com")
            .userIsCaptain(true)
            .hardwareId(hardware.getId())
            .teamId(team.getId())
            .build();

        final User existingUser = User.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU)
            .profileLink("http://www.google.com")
            .liveStatsLink("http://www.google.com")
            .userIsCaptain(true)
            .hardware(hardware)
            .team(team)
            .build();

        final UserValidator userValidator = UserValidator.create(new ValidFoldingStatsRetriever());
        final ValidationResult<User> response = userValidator.validateUpdate(user, existingUser,
            List.of(existingUser),
            List.of(hardware),
            List.of(team)
        );

        assertThat(response.isFailure())
            .as("Expected validation to pass, instead failed with errors: " + response.getErrors())
            .isFalse();
    }

    @Test
    void whenValidatingUpdate_givenNullUser_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final Team team = generateTeam();

        final User existingUser = User.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU)
            .profileLink("http://www.google.com")
            .liveStatsLink("http://www.google.com")
            .userIsCaptain(true)
            .hardware(hardware)
            .team(team)
            .build();

        final UserValidator userValidator = UserValidator.create(new ValidFoldingStatsRetriever());
        final ValidationResult<User> response = userValidator.validateUpdate(null, existingUser,
            List.of(existingUser),
            Collections.emptyList(),
            Collections.emptyList()
        );

        assertThat(response.isFailure())
            .isTrue();

        assertThat(response.getErrors())
            .containsOnly("Payload is null");
    }

    @Test
    void whenValidatingUpdate_givenNullExistingUser_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final Team team = generateTeam();

        final UserRequest user = UserRequest.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU.toString())
            .profileLink("http://www.google.com")
            .liveStatsLink("http://www.google.com")
            .userIsCaptain(true)
            .hardwareId(hardware.getId())
            .teamId(team.getId())
            .build();

        final UserValidator userValidator = UserValidator.create(new ValidFoldingStatsRetriever());
        final ValidationResult<User> response = userValidator.validateUpdate(user, null,
            Collections.emptyList(),
            Collections.emptyList(),
            Collections.emptyList()
        );

        assertThat(response.isFailure())
            .isTrue();

        assertThat(response.getErrors())
            .containsOnly("Payload is null");
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
            .profileLink("http://www.google.com")
            .liveStatsLink("http://www.google.com")
            .userIsCaptain(true)
            .hardwareId(hardware.getId())
            .teamId(team.getId())
            .build();

        final User existingUser = User.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU)
            .profileLink("http://www.google.com")
            .liveStatsLink("http://www.google.com")
            .userIsCaptain(true)
            .hardware(hardware)
            .team(team)
            .build();

        final UserValidator userValidator = UserValidator.create(new ValidFoldingStatsRetriever());
        final ValidationResult<User> response = userValidator.validateUpdate(user, existingUser,
            List.of(existingUser),
            List.of(hardware),
            List.of(team)
        );

        assertThat(response.isFailure())
            .isTrue();

        assertThat(response.getErrors())
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
            .profileLink("http://www.google.com")
            .liveStatsLink("http://www.google.com")
            .userIsCaptain(true)
            .hardwareId(hardware.getId())
            .teamId(team.getId())
            .build();

        final User existingUser = User.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU)
            .profileLink("http://www.google.com")
            .liveStatsLink("http://www.google.com")
            .userIsCaptain(true)
            .hardware(hardware)
            .team(team)
            .build();

        final UserValidator userValidator = UserValidator.create(new ValidFoldingStatsRetriever());
        final ValidationResult<User> response = userValidator.validateUpdate(user, existingUser,
            List.of(existingUser),
            List.of(hardware),
            List.of(team)
        );

        assertThat(response.isFailure())
            .isTrue();

        assertThat(response.getErrors())
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
            .profileLink("http://www.google.com")
            .liveStatsLink("http://www.google.com")
            .userIsCaptain(true)
            .hardwareId(hardware.getId())
            .teamId(team.getId())
            .build();

        final User existingUser = User.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU)
            .profileLink("http://www.google.com")
            .liveStatsLink("http://www.google.com")
            .userIsCaptain(true)
            .hardware(hardware)
            .team(team)
            .build();

        final UserValidator userValidator = UserValidator.create(new ValidFoldingStatsRetriever());
        final ValidationResult<User> response = userValidator.validateUpdate(user, existingUser,
            List.of(existingUser),
            List.of(hardware),
            List.of(team)
        );

        assertThat(response.isFailure())
            .isTrue();

        assertThat(response.getErrors())
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
            .profileLink("http://www.google.com")
            .liveStatsLink("http://www.google.com")
            .userIsCaptain(true)
            .hardwareId(hardware.getId())
            .teamId(team.getId())
            .build();

        final User existingUser = User.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU)
            .profileLink("http://www.google.com")
            .liveStatsLink("http://www.google.com")
            .userIsCaptain(true)
            .hardware(hardware)
            .team(team)
            .build();

        final UserValidator userValidator = UserValidator.create(new ValidFoldingStatsRetriever());
        final ValidationResult<User> response = userValidator.validateUpdate(user, existingUser,
            List.of(existingUser),
            List.of(hardware),
            List.of(team)
        );

        assertThat(response.isFailure())
            .isTrue();

        assertThat(response.getErrors())
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
            .profileLink("http://www.google.com")
            .liveStatsLink("http://www.google.com")
            .userIsCaptain(true)
            .hardwareId(hardware.getId())
            .teamId(team.getId())
            .build();

        final User existingUser = User.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU)
            .profileLink("http://www.google.com")
            .liveStatsLink("http://www.google.com")
            .userIsCaptain(true)
            .hardware(hardware)
            .team(team)
            .build();

        final UserValidator userValidator = UserValidator.create(new ValidFoldingStatsRetriever());
        final ValidationResult<User> response = userValidator.validateUpdate(user, existingUser,
            List.of(existingUser),
            List.of(hardware),
            List.of(team)
        );

        assertThat(response.isFailure())
            .isTrue();

        assertThat(response.getErrors())
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
            .profileLink("http://www.google.com")
            .liveStatsLink("http://www.google.com")
            .userIsCaptain(true)
            .hardwareId(hardware.getId())
            .teamId(team.getId())
            .build();

        final User existingUser = User.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU)
            .profileLink("http://www.google.com")
            .liveStatsLink("http://www.google.com")
            .userIsCaptain(true)
            .hardware(hardware)
            .team(team)
            .build();

        final UserValidator userValidator = UserValidator.create(new ValidFoldingStatsRetriever());
        final ValidationResult<User> response = userValidator.validateUpdate(user, existingUser,
            List.of(existingUser),
            List.of(hardware),
            List.of(team)
        );

        assertThat(response.isFailure())
            .isTrue();

        assertThat(response.getErrors())
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
            .profileLink("http://www.google.com")
            .liveStatsLink("http://www.google.com")
            .userIsCaptain(true)
            .hardwareId(hardware.getId())
            .teamId(team.getId())
            .build();

        final User existingUser = User.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU)
            .profileLink("http://www.google.com")
            .liveStatsLink("http://www.google.com")
            .userIsCaptain(true)
            .hardware(hardware)
            .team(team)
            .build();

        final UserValidator userValidator = UserValidator.create(new ValidFoldingStatsRetriever());
        final ValidationResult<User> response = userValidator.validateUpdate(user, existingUser,
            List.of(existingUser),
            List.of(hardware),
            List.of(team)
        );

        assertThat(response.isFailure())
            .isTrue();

        assertThat(response.getErrors())
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
            .liveStatsLink("http://www.google.com")
            .userIsCaptain(true)
            .hardwareId(hardware.getId())
            .teamId(team.getId())
            .build();

        final User existingUser = User.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU)
            .profileLink("http://www.google.com")
            .liveStatsLink("http://www.google.com")
            .userIsCaptain(true)
            .hardware(hardware)
            .team(team)
            .build();

        final UserValidator userValidator = UserValidator.create(new ValidFoldingStatsRetriever());
        final ValidationResult<User> response = userValidator.validateUpdate(user, existingUser,
            List.of(existingUser),
            List.of(hardware),
            List.of(team)
        );

        assertThat(response.isFailure())
            .as("Expected validation to pass, instead failed with errors: " + response.getErrors())
            .isFalse();
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
            .liveStatsLink("http://www.google.com")
            .userIsCaptain(true)
            .hardwareId(hardware.getId())
            .teamId(team.getId())
            .build();

        final User existingUser = User.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU)
            .profileLink("http://www.google.com")
            .liveStatsLink("http://www.google.com")
            .userIsCaptain(true)
            .hardware(hardware)
            .team(team)
            .build();

        final UserValidator userValidator = UserValidator.create(new ValidFoldingStatsRetriever());
        final ValidationResult<User> response = userValidator.validateUpdate(user, existingUser,
            List.of(existingUser),
            List.of(hardware),
            List.of(team)
        );

        assertThat(response.isFailure())
            .isTrue();

        assertThat(response.getErrors())
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
            .profileLink("http://www.google.com")
            .liveStatsLink(null)
            .userIsCaptain(true)
            .hardwareId(hardware.getId())
            .teamId(team.getId())
            .build();

        final User existingUser = User.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU)
            .profileLink("http://www.google.com")
            .liveStatsLink("http://www.google.com")
            .userIsCaptain(true)
            .hardware(hardware)
            .team(team)
            .build();

        final UserValidator userValidator = UserValidator.create(new ValidFoldingStatsRetriever());
        final ValidationResult<User> response = userValidator.validateUpdate(user, existingUser,
            List.of(existingUser),
            List.of(hardware),
            List.of(team)
        );

        assertThat(response.isFailure())
            .as("Expected validation to pass, instead failed with errors: " + response.getErrors())
            .isFalse();
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
            .profileLink("http://www.google.com")
            .liveStatsLink("invalidUrl")
            .userIsCaptain(true)
            .hardwareId(hardware.getId())
            .teamId(team.getId())
            .build();

        final User existingUser = User.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU)
            .profileLink("http://www.google.com")
            .liveStatsLink("http://www.google.com")
            .userIsCaptain(true)
            .hardware(hardware)
            .team(team)
            .build();

        final UserValidator userValidator = UserValidator.create(new ValidFoldingStatsRetriever());
        final ValidationResult<User> response = userValidator.validateUpdate(user, existingUser,
            List.of(existingUser),
            List.of(hardware),
            List.of(team)
        );

        assertThat(response.isFailure())
            .isTrue();

        assertThat(response.getErrors())
            .containsOnly("Field 'liveStatsLink' is not a valid link: 'invalidUrl'");
    }

    @Test
    void whenValidatingUpdate_givenUserWithFoldingUserNameAndPasskeyAlreadyExists_thenFailureResponseIsReturned() {
        final Hardware otherHardware = generateHardware();
        final Team otherTeam = generateTeam();

        final User otherUser = User.builder()
            .id(1)
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU)
            .profileLink("http://www.google.com")
            .liveStatsLink("http://www.google.com")
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
            .profileLink("http://www.google.com")
            .liveStatsLink("http://www.google.com")
            .userIsCaptain(true)
            .hardwareId(hardware.getId())
            .teamId(team.getId())
            .build();

        final User existingUser = User.builder()
            .id(2)
            .foldingUserName("differentUser")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU)
            .profileLink("http://www.google.com")
            .liveStatsLink("http://www.google.com")
            .userIsCaptain(true)
            .hardware(hardware)
            .team(team)
            .build();

        final UserValidator userValidator = UserValidator.create(new ValidFoldingStatsRetriever());
        final ValidationResult<User> response = userValidator.validateUpdate(user, existingUser,
            List.of(existingUser, otherUser),
            List.of(hardware, otherHardware),
            List.of(team, otherTeam)
        );

        assertThat(response.isFailure())
            .isTrue();

        assertThat(response.getErrors())
            .containsOnly("Payload conflicts with an existing object on: [foldingUserName, passkey]");
    }

    @Test
    void whenValidatingUpdate_givenUserWithFoldingUserNameAndPasskeyExists_andExistingUserIsTheOneBeingUpdated_thenSuccessResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final Team team = generateTeam();

        final UserRequest user = UserRequest.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU.toString())
            .profileLink("http://www.google.com")
            .liveStatsLink("http://www.google.com")
            .userIsCaptain(true)
            .hardwareId(hardware.getId())
            .teamId(team.getId())
            .build();

        final User existingUser = User.builder()
            .id(1)
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU)
            .profileLink("http://www.google.com")
            .liveStatsLink("http://www.google.com")
            .userIsCaptain(true)
            .hardware(hardware)
            .team(team)
            .build();

        final UserValidator userValidator = UserValidator.create(new ValidFoldingStatsRetriever());
        final ValidationResult<User> response = userValidator.validateUpdate(user, existingUser,
            List.of(existingUser),
            List.of(hardware),
            List.of(team)
        );

        assertThat(response.isFailure())
            .as("Expected validation to pass, instead failed with errors: " + response.getErrors())
            .isFalse();
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
            .profileLink("http://www.google.com")
            .liveStatsLink("http://www.google.com")
            .userIsCaptain(true)
            .hardwareId(hardware.getId())
            .teamId(team.getId())
            .build();

        final User existingUser = User.builder()
            .id(1)
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU)
            .profileLink("http://www.google.com")
            .liveStatsLink("http://www.google.com")
            .userIsCaptain(true)
            .hardware(hardware)
            .team(team)
            .build();

        final UserValidator userValidator = UserValidator.create(new ValidFoldingStatsRetriever());
        final ValidationResult<User> response = userValidator.validateUpdate(user, existingUser,
            List.of(existingUser),
            List.of(hardware),
            List.of(team)
        );

        assertThat(response.isFailure())
            .as("Expected validation to pass, instead failed with errors: " + response.getErrors())
            .isFalse();
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
            .profileLink("http://www.google.com")
            .liveStatsLink("http://www.google.com")
            .userIsCaptain(true)
            .hardware(hardware)
            .team(team)
            .build();

        final UserRequest user = UserRequest.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU.toString())
            .profileLink("http://www.google.com")
            .liveStatsLink("http://www.google.com")
            .userIsCaptain(true)
            .hardwareId(hardware.getId())
            .teamId(team.getId())
            .build();

        final User existingUser = User.builder()
            .id(1)
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU)
            .profileLink("http://www.google.com")
            .liveStatsLink("http://www.google.com")
            .userIsCaptain(true)
            .hardware(hardware)
            .team(oldTeam)
            .build();

        final UserValidator userValidator = UserValidator.create(new ValidFoldingStatsRetriever());
        final ValidationResult<User> response = userValidator.validateUpdate(user, existingUser,
            List.of(existingUser, currentCaptain),
            List.of(hardware),
            List.of(team, oldTeam)
        );

        assertThat(response.isFailure())
            .as("Expected validation to pass, instead failed with errors: " + response.getErrors())
            .isFalse();
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
            .profileLink("http://www.google.com")
            .liveStatsLink("http://www.google.com")
            .userIsCaptain(true)
            .hardware(hardware)
            .team(team)
            .build();

        final UserRequest user = UserRequest.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU.toString())
            .profileLink("http://www.google.com")
            .liveStatsLink("http://www.google.com")
            .userIsCaptain(true)
            .hardwareId(hardware.getId())
            .teamId(team.getId())
            .build();

        final User existingUser = User.builder()
            .id(1)
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU)
            .profileLink("http://www.google.com")
            .liveStatsLink("http://www.google.com")
            .userIsCaptain(false)
            .hardware(hardware)
            .team(team)
            .build();

        final UserValidator userValidator = UserValidator.create(new ValidFoldingStatsRetriever());
        final ValidationResult<User> response = userValidator.validateUpdate(user, existingUser,
            List.of(existingUser, currentCaptain),
            List.of(hardware),
            List.of(team)
        );

        assertThat(response.isFailure())
            .as("Expected validation to pass, instead failed with errors: " + response.getErrors())
            .isFalse();
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
            .profileLink("http://www.google.com")
            .liveStatsLink("http://www.google.com")
            .userIsCaptain(true)
            .hardware(hardware)
            .team(team)
            .build();

        final UserRequest user = UserRequest.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU.toString())
            .profileLink("http://www.google.com")
            .liveStatsLink("http://www.google.com")
            .userIsCaptain(false)
            .hardwareId(hardware.getId())
            .teamId(team.getId())
            .build();

        final User existingUser = User.builder()
            .id(1)
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU)
            .profileLink("http://www.google.com")
            .liveStatsLink("http://www.google.com")
            .userIsCaptain(false)
            .hardware(hardware)
            .team(team)
            .build();

        final UserValidator userValidator = UserValidator.create(new ValidFoldingStatsRetriever());
        final ValidationResult<User> response = userValidator.validateUpdate(user, existingUser,
            List.of(currentCaptain, existingUser),
            List.of(hardware),
            List.of(team)
        );

        assertThat(response.isFailure())
            .as("Expected validation to pass, instead failed with errors: " + response.getErrors())
            .isFalse();
    }

    @Test
    void whenValidatingUpdate_givenTeamIsUpdated_andTeamAlreadyHasMaximumTotalUsers_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final Team team = generateTeam();
        final Team oldTeam = generateTeam();

        final List<User> usersOnTeam = new ArrayList<>(Category.maximumPermittedAmountForAllCategories());

        for (int i = 0; i < Category.maximumPermittedAmountForAllCategories(); i++) {
            usersOnTeam.add(
                User.builder()
                    .foldingUserName("user" + 0)
                    .displayName("user" + 0)
                    .passkey("DummyPasskey12345678901234567891")
                    .category(Category.WILDCARD)
                    .profileLink("http://www.google.com")
                    .liveStatsLink("http://www.google.com")
                    .userIsCaptain(false)
                    .hardware(hardware)
                    .team(team)
                    .build()
            );
        }

        final UserRequest user = UserRequest.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU.toString())
            .profileLink("http://www.google.com")
            .liveStatsLink("http://www.google.com")
            .userIsCaptain(false)
            .hardwareId(hardware.getId())
            .teamId(team.getId())
            .build();

        final User existingUser = User.builder()
            .id(1)
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU)
            .profileLink("http://www.google.com")
            .liveStatsLink("http://www.google.com")
            .userIsCaptain(false)
            .hardware(hardware)
            .team(oldTeam)
            .build();

        final List<User> allUsers = new ArrayList<>(usersOnTeam);
        allUsers.add(existingUser);

        final UserValidator userValidator = UserValidator.create(new ValidFoldingStatsRetriever());
        final ValidationResult<User> response = userValidator.validateUpdate(user, existingUser,
            allUsers,
            List.of(hardware),
            List.of(team, oldTeam)
        );

        assertThat(response.isFailure())
            .isTrue();

        assertThat(response.getErrors())
            .containsOnly(String.format("Team '%s' has %s users, maximum permitted is %s", team.getTeamName(), usersOnTeam.size(),
                Category.maximumPermittedAmountForAllCategories()));
    }

    @Test
    void whenValidatingUpdate_givenTeamIsUpdated_andTeamAlreadyHasMaximumUsersInCategory_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final Team team = generateTeam();
        final Team oldTeam = generateTeam();

        final List<User> usersOnTeamInCategory = new ArrayList<>(Category.NVIDIA_GPU.permittedUsers());

        for (int i = 0; i < Category.NVIDIA_GPU.permittedUsers(); i++) {
            usersOnTeamInCategory.add(
                User.builder()
                    .foldingUserName("user" + 0)
                    .displayName("user" + 0)
                    .passkey("DummyPasskey12345678901234567891")
                    .category(Category.NVIDIA_GPU)
                    .profileLink("http://www.google.com")
                    .liveStatsLink("http://www.google.com")
                    .userIsCaptain(false)
                    .hardware(hardware)
                    .team(team)
                    .build()
            );
        }

        final UserRequest user = UserRequest.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU.toString())
            .profileLink("http://www.google.com")
            .liveStatsLink("http://www.google.com")
            .userIsCaptain(true)
            .hardwareId(hardware.getId())
            .teamId(team.getId())
            .build();

        final User existingUser = User.builder()
            .id(1)
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU)
            .profileLink("http://www.google.com")
            .liveStatsLink("http://www.google.com")
            .userIsCaptain(false)
            .hardware(hardware)
            .team(oldTeam)
            .build();

        final List<User> allUsers = new ArrayList<>(usersOnTeamInCategory);
        allUsers.add(existingUser);

        final UserValidator userValidator = UserValidator.create(new ValidFoldingStatsRetriever());
        final ValidationResult<User> response = userValidator.validateUpdate(user, existingUser,
            allUsers,
            List.of(hardware),
            List.of(team)
        );

        assertThat(response.isFailure())
            .isTrue();

        assertThat(response.getErrors())
            .containsOnly(String.format("Team '%s' already has %s users in category '%s', only %s permitted", team.getTeamName(),
                usersOnTeamInCategory.size(), Category.NVIDIA_GPU, Category.NVIDIA_GPU.permittedUsers()));
    }

    @Test
    void whenValidatingUpdate_givenCategoryIsUpdated_andTeamAlreadyHasMaximumUsersInCategory_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final Team team = generateTeam();

        final List<User> usersOnTeamInCategory = new ArrayList<>(Category.NVIDIA_GPU.permittedUsers());

        for (int i = 0; i < Category.NVIDIA_GPU.permittedUsers(); i++) {
            usersOnTeamInCategory.add(
                User.builder()
                    .foldingUserName("user" + 0)
                    .displayName("user" + 0)
                    .passkey("DummyPasskey12345678901234567891")
                    .category(Category.NVIDIA_GPU)
                    .profileLink("http://www.google.com")
                    .liveStatsLink("http://www.google.com")
                    .userIsCaptain(false)
                    .hardware(hardware)
                    .team(team)
                    .build()
            );
        }

        final UserRequest user = UserRequest.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU.toString())
            .profileLink("http://www.google.com")
            .liveStatsLink("http://www.google.com")
            .userIsCaptain(true)
            .hardwareId(hardware.getId())
            .teamId(team.getId())
            .build();

        final User existingUser = User.builder()
            .id(1)
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.AMD_GPU)
            .profileLink("http://www.google.com")
            .liveStatsLink("http://www.google.com")
            .userIsCaptain(false)
            .hardware(hardware)
            .team(team)
            .build();

        final List<User> allUsers = new ArrayList<>(usersOnTeamInCategory);
        allUsers.add(existingUser);

        final UserValidator userValidator = UserValidator.create(new ValidFoldingStatsRetriever());
        final ValidationResult<User> response = userValidator.validateUpdate(user, existingUser,
            allUsers,
            List.of(hardware),
            List.of(team)
        );

        assertThat(response.isFailure())
            .isTrue();

        assertThat(response.getErrors())
            .containsOnly(String.format("Team '%s' already has %s users in category '%s', only %s permitted", team.getTeamName(),
                usersOnTeamInCategory.size(), Category.NVIDIA_GPU, Category.NVIDIA_GPU.permittedUsers()));
    }

    @Test
    void whenValidatingUpdate_givenUserWithMisMatchingHardwareMakeAndCategory_thenFailureResponseIsReturned() {
        final Hardware hardware = Hardware.builder()
            .id(hardwareId++)
            .hardwareName("hardwareName")
            .displayName("displayName")
            .hardwareMake(HardwareMake.NVIDIA)
            .hardwareType(HardwareType.GPU)
            .multiplier(1.00D)
            .averagePpd(1L)
            .build();
        final Team team = generateTeam();

        final UserRequest user = UserRequest.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.AMD_GPU.toString())
            .profileLink("http://www.google.com")
            .liveStatsLink("http://www.google.com")
            .userIsCaptain(true)
            .hardwareId(hardware.getId())
            .teamId(team.getId())
            .build();

        final User existingUser = User.builder()
            .id(1)
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU)
            .profileLink("http://www.google.com")
            .liveStatsLink("http://www.google.com")
            .userIsCaptain(false)
            .hardware(hardware)
            .team(team)
            .build();

        final UserValidator userValidator = UserValidator.create(new ValidFoldingStatsRetriever());
        final ValidationResult<User> response = userValidator.validateUpdate(user, existingUser,
            List.of(existingUser),
            List.of(hardware),
            List.of(team)
        );

        assertThat(response.isFailure())
            .isTrue();

        assertThat(response.getErrors())
            .containsOnly(String.format("Category '%s' cannot be filled by hardware of make '%s', must be one of: %s", Category.AMD_GPU,
                hardware.getHardwareMake(), Category.AMD_GPU.supportedHardwareMakes()));
    }

    @Test
    void whenValidatingUpdate_givenUserWithMisMatchingHardwareTypeAndCategory_thenFailureResponseIsReturned() {
        final Hardware hardware = Hardware.builder()
            .id(hardwareId++)
            .hardwareName("hardwareName")
            .displayName("displayName")
            .hardwareMake(HardwareMake.AMD)
            .hardwareType(HardwareType.CPU)
            .multiplier(1.00D)
            .averagePpd(1L)
            .build();
        final Team team = generateTeam();

        final UserRequest user = UserRequest.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.AMD_GPU.toString())
            .profileLink("http://www.google.com")
            .liveStatsLink("http://www.google.com")
            .userIsCaptain(true)
            .hardwareId(hardware.getId())
            .teamId(team.getId())
            .build();

        final User existingUser = User.builder()
            .id(1)
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU)
            .profileLink("http://www.google.com")
            .liveStatsLink("http://www.google.com")
            .userIsCaptain(false)
            .hardware(hardware)
            .team(team)
            .build();

        final UserValidator userValidator = UserValidator.create(new ValidFoldingStatsRetriever());
        final ValidationResult<User> response = userValidator.validateUpdate(user, existingUser,
            List.of(existingUser),
            List.of(hardware),
            List.of(team)
        );

        assertThat(response.isFailure())
            .isTrue();

        assertThat(response.getErrors())
            .containsOnly(String.format("Category '%s' cannot be filled by hardware of type '%s', must be one of: %s", Category.AMD_GPU,
                hardware.getHardwareType(), Category.AMD_GPU.supportedHardwareTypes()));
    }

    @Test
    void whenValidatingUpdate_givenUserWithHardwareWithNonExistingId_thenFailureResponseIsReturned() {
        final Hardware hardware = Hardware.builder()
            .id(99)
            .hardwareName("hardwareName")
            .displayName("displayName")
            .hardwareMake(HardwareMake.NVIDIA)
            .hardwareType(HardwareType.GPU)
            .multiplier(1.00D)
            .averagePpd(1L)
            .build();
        final Team team = generateTeam();

        final UserRequest user = UserRequest.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU.toString())
            .profileLink("http://www.google.com")
            .liveStatsLink("http://www.google.com")
            .userIsCaptain(true)
            .hardwareId(1)
            .teamId(team.getId())
            .build();

        final User existingUser = User.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU)
            .profileLink("http://www.google.com")
            .liveStatsLink("http://www.google.com")
            .userIsCaptain(true)
            .hardware(hardware)
            .team(team)
            .build();

        final UserValidator userValidator = UserValidator.create(new ValidFoldingStatsRetriever());
        final ValidationResult<User> response = userValidator.validateUpdate(user, existingUser,
            List.of(existingUser),
            List.of(hardware),
            List.of(team)
        );

        assertThat(response.isFailure())
            .isTrue();

        assertThat(response.getErrors())
            .containsOnly(String.format("Field 'hardwareId' must be one of: [%s: %s]", hardware.getId(), hardware.getHardwareName()));
    }

    @Test
    void whenValidatingUpdate_givenNoHardwareExistsOnTheSystem_thenFailureResponseIsReturned() {
        final Team team = generateTeam();

        final UserRequest user = UserRequest.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU.toString())
            .profileLink("http://www.google.com")
            .liveStatsLink("http://www.google.com")
            .userIsCaptain(true)
            .hardwareId(1)
            .teamId(team.getId())
            .build();

        final User existingUser = User.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU)
            .profileLink("http://www.google.com")
            .liveStatsLink("http://www.google.com")
            .userIsCaptain(true)
            .team(team)
            .build();

        final UserValidator userValidator = UserValidator.create(new ValidFoldingStatsRetriever());
        final ValidationResult<User> response = userValidator.validateUpdate(user, existingUser,
            List.of(existingUser),
            Collections.emptyList(),
            List.of(team)
        );

        assertThat(response.isFailure())
            .isTrue();

        assertThat(response.getErrors())
            .containsOnly("No hardware exist on the system");
    }

    @Test
    void whenValidatingUpdate_givenUserWithTeamWithNonExistingId_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final Team team = Team.builder()
            .id(99)
            .teamName("teamName")
            .teamDescription("teamDescription")
            .forumLink("http://www.google.com")
            .build();

        final UserRequest user = UserRequest.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU.toString())
            .profileLink("http://www.google.com")
            .liveStatsLink("http://www.google.com")
            .userIsCaptain(true)
            .hardwareId(hardware.getId())
            .teamId(1)
            .build();

        final User existingUser = User.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU)
            .profileLink("http://www.google.com")
            .liveStatsLink("http://www.google.com")
            .userIsCaptain(true)
            .hardware(hardware)
            .team(team)
            .build();

        final UserValidator userValidator = UserValidator.create(new ValidFoldingStatsRetriever());
        final ValidationResult<User> response = userValidator.validateUpdate(user, existingUser,
            List.of(existingUser),
            List.of(hardware),
            List.of(team)
        );

        assertThat(response.isFailure())
            .isTrue();

        assertThat(response.getErrors())
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
            .profileLink("http://www.google.com")
            .liveStatsLink("http://www.google.com")
            .userIsCaptain(true)
            .hardwareId(hardware.getId())
            .teamId(1)
            .build();

        final User existingUser = User.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU)
            .profileLink("http://www.google.com")
            .liveStatsLink("http://www.google.com")
            .userIsCaptain(true)
            .hardware(hardware)
            .build();

        final UserValidator userValidator = UserValidator.create(new ValidFoldingStatsRetriever());
        final ValidationResult<User> response = userValidator.validateUpdate(user, existingUser,
            List.of(existingUser),
            List.of(hardware),
            Collections.emptyList()
        );

        assertThat(response.isFailure())
            .isTrue();

        assertThat(response.getErrors())
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
            .profileLink("http://www.google.com")
            .liveStatsLink("http://www.google.com")
            .userIsCaptain(true)
            .hardwareId(hardware.getId())
            .teamId(team.getId())
            .build();

        final User existingUser = User.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU)
            .profileLink("http://www.google.com")
            .liveStatsLink("http://www.google.com")
            .userIsCaptain(true)
            .hardware(hardware)
            .team(team)
            .build();

        final UserValidator userValidator = UserValidator.create(new NoUnitsFoldingStatsRetriever());
        final ValidationResult<User> response = userValidator.validateUpdate(user, existingUser,
            List.of(existingUser),
            List.of(hardware),
            List.of(team)
        );

        assertThat(response.isFailure())
            .as("Expected validation to pass, instead failed with errors: " + response.getErrors())
            .isFalse();
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
            .profileLink("http://www.google.com")
            .liveStatsLink("http://www.google.com")
            .userIsCaptain(true)
            .hardwareId(hardware.getId())
            .teamId(team.getId())
            .build();

        final User existingUser = User.builder()
            .foldingUserName("oldUserName")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU)
            .profileLink("http://www.google.com")
            .liveStatsLink("http://www.google.com")
            .userIsCaptain(true)
            .hardware(hardware)
            .team(team)
            .build();

        final UserValidator userValidator = UserValidator.create(new NoUnitsFoldingStatsRetriever());
        final ValidationResult<User> response = userValidator.validateUpdate(user, existingUser,
            List.of(existingUser),
            List.of(hardware),
            List.of(team)
        );

        assertThat(response.isFailure())
            .isTrue();

        assertThat(response.getErrors())
            .containsOnly("User 'user' has 0 Work Units with passkey 'DummyPasskey12345678901234567890', "
                + "there must be at least one completed Work Unit before adding the user");
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
            .profileLink("http://www.google.com")
            .liveStatsLink("http://www.google.com")
            .userIsCaptain(true)
            .hardwareId(hardware.getId())
            .teamId(team.getId())
            .build();

        final User existingUser = User.builder()
            .foldingUserName("oldUserName")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU)
            .profileLink("http://www.google.com")
            .liveStatsLink("http://www.google.com")
            .userIsCaptain(true)
            .hardware(hardware)
            .team(team)
            .build();

        final UserValidator userValidator = UserValidator.create(new ExternalConnectionFoldingStatsRetriever());
        final ValidationResult<User> response = userValidator.validateUpdate(user, existingUser,
            List.of(existingUser),
            List.of(hardware),
            List.of(team)
        );

        assertThat(response.isFailure())
            .isTrue();

        assertThat(response.getErrors())
            .containsOnly("Unable to connect to 'http://www.google.com' to check stats for user 'user': Error connecting");
    }

    @Test
    void whenValidatingUpdate_givenNoHardware_thenFailureResponseIsReturned_andNoOtherValidationErrorsAreReturned() {
        final Team team = generateTeam();

        final UserRequest user = UserRequest.builder()
            .foldingUserName(null)
            .displayName(null)
            .passkey("invalid")
            .category("invalid")
            .profileLink("invalid")
            .liveStatsLink("invalid")
            .userIsCaptain(true)
            .teamId(team.getId())
            .build();

        final User existingUser = User.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU)
            .profileLink("http://www.google.com")
            .liveStatsLink("http://www.google.com")
            .userIsCaptain(true)
            .team(team)
            .build();

        final UserValidator userValidator = UserValidator.create(new ValidFoldingStatsRetriever());
        final ValidationResult<User> response = userValidator.validateUpdate(user, existingUser,
            List.of(existingUser),
            Collections.emptyList(),
            List.of(team)
        );

        assertThat(response.isFailure())
            .isTrue();

        assertThat(response.getErrors())
            .containsOnly("No hardware exist on the system");
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
            .hardwareId(hardware.getId())
            .teamId(team.getId())
            .build();

        final User existingUser = User.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU)
            .profileLink("http://www.google.com")
            .liveStatsLink("http://www.google.com")
            .userIsCaptain(true)
            .team(team)
            .build();

        final UserValidator userValidator = UserValidator.create(new ValidFoldingStatsRetriever());
        final ValidationResult<User> response = userValidator.validateUpdate(user, existingUser,
            List.of(existingUser),
            List.of(hardware),
            List.of(team)
        );

        assertThat(response.isFailure())
            .isTrue();

        assertThat(response.getErrors())
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
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU)
            .profileLink("http://www.google.com")
            .liveStatsLink("http://www.google.com")
            .userIsCaptain(false)
            .hardware(hardware)
            .team(team)
            .build();

        final UserValidator userValidator = UserValidator.create(new ValidFoldingStatsRetriever());
        final ValidationResult<User> response = userValidator.validateDelete(userToDelete);

        assertThat(response.isFailure())
            .as("Expected validation to pass, instead failed with errors: " + response.getErrors())
            .isFalse();
    }

    @Test
    void whenValidatingDelete_andUserIsCaptain_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final Team team = generateTeam();

        final User userToDelete = User.builder()
            .foldingUserName("user")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU)
            .profileLink("http://www.google.com")
            .liveStatsLink("http://www.google.com")
            .userIsCaptain(true)
            .hardware(hardware)
            .team(team)
            .build();

        final UserValidator userValidator = UserValidator.create(new ValidFoldingStatsRetriever());
        final ValidationResult<User> response = userValidator.validateDelete(userToDelete);

        assertThat(response.isFailure())
            .isTrue();

        assertThat(response.getErrors())
            .containsOnly(String.format("Cannot delete user '%s' since they are team captain", userToDelete.getDisplayName()));
    }

    private static Hardware generateHardware() {
        return Hardware.builder()
            .id(hardwareId++)
            .hardwareName("hardwareName")
            .displayName("displayName")
            .hardwareMake(HardwareMake.NVIDIA)
            .hardwareType(HardwareType.GPU)
            .multiplier(1.00D)
            .averagePpd(1L)
            .build();
    }

    private static Team generateTeam() {
        return Team.builder()
            .id(teamId++)
            .teamName("teamName")
            .teamDescription("teamDescription")
            .forumLink("http://www.google.com")
            .build();
    }

    private static class ValidFoldingStatsRetriever implements FoldingStatsRetriever {

        @Override
        public Stats getStats(final FoldingStatsDetails foldingStatsDetails) {
            return Stats.create(1L, 1);
        }

        @Override
        public UserStats getTotalStats(final User user) {
            return UserStats.create(user.getId(), DateTimeUtils.currentUtcTimestamp(), 1L, 1);
        }
    }

    private static class NoUnitsFoldingStatsRetriever implements FoldingStatsRetriever {

        @Override
        public Stats getStats(final FoldingStatsDetails foldingStatsDetails) {
            return Stats.create(0L, 0);
        }

        @Override
        public UserStats getTotalStats(final User user) {
            return UserStats.create(user.getId(), DateTimeUtils.currentUtcTimestamp(), 0L, 0);
        }
    }

    private static class ExternalConnectionFoldingStatsRetriever implements FoldingStatsRetriever {

        @Override
        public Stats getStats(final FoldingStatsDetails foldingStatsDetails) throws ExternalConnectionException {
            throw new ExternalConnectionException("http://www.google.com", "Error connecting");
        }

        @Override
        public UserStats getTotalStats(final User user) throws ExternalConnectionException {
            throw new ExternalConnectionException("http://www.google.com", "Error connecting");
        }
    }
}
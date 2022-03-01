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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.List;
import me.zodac.folding.api.tc.Category;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.HardwareMake;
import me.zodac.folding.api.tc.HardwareType;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.tc.change.UserChange;
import me.zodac.folding.api.tc.validation.retriever.ExternalConnectionFoldingStatsRetriever;
import me.zodac.folding.api.tc.validation.retriever.NoUnitsFoldingStatsRetriever;
import me.zodac.folding.api.tc.validation.retriever.ValidFoldingStatsRetriever;
import me.zodac.folding.rest.api.tc.request.UserChangeRequest;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link UserChangeValidator}.
 */
class UserChangeValidatorTest {

    private static int hardwareId = 1;
    private static int userId = 1;

    @Test
    void whenValidating_givenValidUserChange_thenSuccessResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final User user = generateUser(hardware);

        final UserChangeRequest userChange = UserChangeRequest.builder()
            .existingPasskey(user.getPasskey())
            .foldingUserName(user.getFoldingUserName())
            .passkey(user.getPasskey())
            .hardwareId(hardware.getId())
            .userId(user.getId())
            .liveStatsLink("https://www.google.ie")
            .build();

        final UserChangeValidator userChangeValidator = UserChangeValidator.create(new ValidFoldingStatsRetriever());
        final ValidationResult<UserChange> response = userChangeValidator.validate(
            userChange,
            Collections.emptyList(),
            List.of(hardware),
            List.of(user)
        );

        assertThat(response.isFailure())
            .as("Expected validation to pass, instead failed with errors: " + response.getErrors())
            .isFalse();
    }

    @Test
    void whenValidating_givenNullUserChange_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final User user = generateUser(hardware);

        final UserChangeValidator userChangeValidator = UserChangeValidator.create(new ValidFoldingStatsRetriever());
        final ValidationResult<UserChange> response = userChangeValidator.validate(
            null,
            Collections.emptyList(),
            List.of(hardware),
            List.of(user)
        );

        assertThat(response.isFailure())
            .isTrue();

        assertThat(response.getErrors())
            .containsOnly("Payload is null");
    }

    @Test
    void whenValidating_givenNullFoldingUserName_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final User user = generateUser(hardware);

        final UserChangeRequest userChange = UserChangeRequest.builder()
            .existingPasskey(user.getPasskey())
            .foldingUserName(null)
            .passkey(user.getPasskey())
            .hardwareId(hardware.getId())
            .userId(user.getId())
            .liveStatsLink("https://www.google.ie")
            .build();

        final UserChangeValidator userChangeValidator = UserChangeValidator.create(new ValidFoldingStatsRetriever());
        final ValidationResult<UserChange> response = userChangeValidator.validate(
            userChange,
            Collections.emptyList(),
            List.of(hardware),
            List.of(user)
        );

        assertThat(response.isFailure())
            .isTrue();

        assertThat(response.getErrors())
            .containsOnly("Field 'foldingUserName' must have at least one alphanumeric character, or an underscore, period or hyphen");
    }

    @Test
    void whenValidating_givenInvalidFoldingUserName_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final User user = generateUser(hardware);

        final UserChangeRequest userChange = UserChangeRequest.builder()
            .existingPasskey(user.getPasskey())
            .foldingUserName("folding*Name")
            .passkey(user.getPasskey())
            .hardwareId(hardware.getId())
            .userId(user.getId())
            .liveStatsLink("https://www.google.ie")
            .build();

        final UserChangeValidator userChangeValidator = UserChangeValidator.create(new ValidFoldingStatsRetriever());
        final ValidationResult<UserChange> response = userChangeValidator.validate(
            userChange,
            Collections.emptyList(),
            List.of(hardware),
            List.of(user)
        );

        assertThat(response.isFailure())
            .isTrue();

        assertThat(response.getErrors())
            .containsOnly("Field 'foldingUserName' must have at least one alphanumeric character, or an underscore, period or hyphen");
    }

    @Test
    void whenValidating_givenNullPasskey_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final User user = generateUser(hardware);

        final UserChangeRequest userChange = UserChangeRequest.builder()
            .existingPasskey(user.getPasskey())
            .foldingUserName(user.getFoldingUserName())
            .passkey(null)
            .hardwareId(hardware.getId())
            .userId(user.getId())
            .liveStatsLink("https://www.google.ie")
            .build();

        final UserChangeValidator userChangeValidator = UserChangeValidator.create(new ValidFoldingStatsRetriever());
        final ValidationResult<UserChange> response = userChangeValidator.validate(
            userChange,
            Collections.emptyList(),
            List.of(hardware),
            List.of(user)
        );

        assertThat(response.isFailure())
            .isTrue();

        assertThat(response.getErrors())
            .containsOnly("Field 'passkey' must be 32 characters long and include only alphanumeric characters");
    }

    @Test
    void whenValidating_givenInvalidPasskey_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final User user = generateUser(hardware);

        final UserChangeRequest userChange = UserChangeRequest.builder()
            .existingPasskey(user.getPasskey())
            .foldingUserName(user.getFoldingUserName())
            .passkey("DummyPasskey1234567890123456789*")
            .hardwareId(hardware.getId())
            .userId(user.getId())
            .liveStatsLink("https://www.google.ie")
            .build();

        final UserChangeValidator userChangeValidator = UserChangeValidator.create(new ValidFoldingStatsRetriever());
        final ValidationResult<UserChange> response = userChangeValidator.validate(
            userChange,
            Collections.emptyList(),
            List.of(hardware),
            List.of(user)
        );

        assertThat(response.isFailure())
            .isTrue();

        assertThat(response.getErrors())
            .containsOnly("Field 'passkey' must be 32 characters long and include only alphanumeric characters");
    }

    @Test
    void whenValidating_givenNullLiveStatsLink_thenSuccessResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final User user = generateUser(hardware);

        final UserChangeRequest userChange = UserChangeRequest.builder()
            .existingPasskey(user.getPasskey())
            .foldingUserName(user.getFoldingUserName())
            .passkey(user.getPasskey())
            .hardwareId(hardware.getId())
            .userId(user.getId())
            .liveStatsLink(null)
            .build();

        final UserChangeValidator userChangeValidator = UserChangeValidator.create(new ValidFoldingStatsRetriever());
        final ValidationResult<UserChange> response = userChangeValidator.validate(
            userChange,
            Collections.emptyList(),
            List.of(hardware),
            List.of(user)
        );

        assertThat(response.isFailure())
            .as("Expected validation to pass, instead failed with errors: " + response.getErrors())
            .isFalse();
    }

    @Test
    void whenValidating_givenInvalidLiveStatsLink_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final User user = generateUser(hardware);

        final UserChangeRequest userChange = UserChangeRequest.builder()
            .existingPasskey(user.getPasskey())
            .foldingUserName(user.getFoldingUserName())
            .passkey(user.getPasskey())
            .hardwareId(hardware.getId())
            .userId(user.getId())
            .liveStatsLink("invalidUrl")
            .build();

        final UserChangeValidator userChangeValidator = UserChangeValidator.create(new ValidFoldingStatsRetriever());
        final ValidationResult<UserChange> response = userChangeValidator.validate(
            userChange,
            Collections.emptyList(),
            List.of(hardware),
            List.of(user)
        );

        assertThat(response.isFailure())
            .isTrue();

        assertThat(response.getErrors())
            .containsOnly("Field 'liveStatsLink' is not a valid link: 'invalidUrl'");
    }

    @Test
    void whenValidating_givenInvalidHardwareId_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final User user = generateUser(hardware);

        final UserChangeRequest userChange = UserChangeRequest.builder()
            .existingPasskey(user.getPasskey())
            .foldingUserName(user.getFoldingUserName())
            .passkey(user.getPasskey())
            .hardwareId(-1)
            .userId(user.getId())
            .liveStatsLink("https://www.google.ie")
            .build();

        final UserChangeValidator userChangeValidator = UserChangeValidator.create(new ValidFoldingStatsRetriever());
        final ValidationResult<UserChange> response = userChangeValidator.validate(
            userChange,
            Collections.emptyList(),
            List.of(hardware),
            List.of(user)
        );

        assertThat(response.isFailure())
            .isTrue();

        assertThat(response.getErrors())
            .containsOnly(String.format("Field 'hardwareId' must be one of: [%s: %s]", hardware.getId(), hardware.getHardwareName()));
    }

    @Test
    void whenValidating_givenNoHardwareExists_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final User user = generateUser(hardware);

        final UserChangeRequest userChange = UserChangeRequest.builder()
            .existingPasskey(user.getPasskey())
            .foldingUserName(user.getFoldingUserName())
            .passkey(user.getPasskey())
            .hardwareId(-1)
            .userId(user.getId())
            .liveStatsLink("https://www.google.ie")
            .build();

        final UserChangeValidator userChangeValidator = UserChangeValidator.create(new ValidFoldingStatsRetriever());
        final ValidationResult<UserChange> response = userChangeValidator.validate(
            userChange,
            Collections.emptyList(),
            Collections.emptyList(),
            List.of(user)
        );

        assertThat(response.isFailure())
            .isTrue();

        assertThat(response.getErrors())
            .containsOnly("No hardware exist on the system");
    }

    @Test
    void whenValidating_givenInvalidUserId_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final User user = generateUser(hardware);

        final UserChangeRequest userChange = UserChangeRequest.builder()
            .existingPasskey(user.getPasskey())
            .foldingUserName(user.getFoldingUserName())
            .passkey(user.getPasskey())
            .hardwareId(hardware.getId())
            .userId(-1)
            .liveStatsLink("https://www.google.ie")
            .build();

        final UserChangeValidator userChangeValidator = UserChangeValidator.create(new ValidFoldingStatsRetriever());
        final ValidationResult<UserChange> response = userChangeValidator.validate(
            userChange,
            Collections.emptyList(),
            List.of(hardware),
            List.of(user)
        );

        assertThat(response.isFailure())
            .isTrue();

        assertThat(response.getErrors())
            .containsOnly(String.format("Field 'userId' must be one of: [%s: %s]", user.getId(), user.getDisplayName()));
    }

    @Test
    void whenValidating_givenNoUsersExist_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final User user = generateUser(hardware);

        final UserChangeRequest userChange = UserChangeRequest.builder()
            .existingPasskey(user.getPasskey())
            .foldingUserName(user.getFoldingUserName())
            .passkey(user.getPasskey())
            .hardwareId(hardware.getId())
            .userId(-1)
            .liveStatsLink("https://www.google.ie")
            .build();

        final UserChangeValidator userChangeValidator = UserChangeValidator.create(new ValidFoldingStatsRetriever());
        final ValidationResult<UserChange> response = userChangeValidator.validate(
            userChange,
            Collections.emptyList(),
            List.of(hardware),
            Collections.emptyList()
        );

        assertThat(response.isFailure())
            .isTrue();

        assertThat(response.getErrors())
            .containsOnly("No users exist on the system");
    }

    @Test
    void whenValidating_givenExistingPasskeyDoesNotMatchUserPasskey_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final User user = generateUser(hardware);

        final UserChangeRequest userChange = UserChangeRequest.builder()
            .existingPasskey("DummyPasskey11111111111111111111")
            .foldingUserName(user.getFoldingUserName())
            .passkey(user.getPasskey())
            .hardwareId(hardware.getId())
            .userId(user.getId())
            .liveStatsLink("https://www.google.ie")
            .build();

        final UserChangeValidator userChangeValidator = UserChangeValidator.create(new ValidFoldingStatsRetriever());
        final ValidationResult<UserChange> response = userChangeValidator.validate(
            userChange,
            Collections.emptyList(),
            List.of(hardware),
            List.of(user)
        );

        assertThat(response.isFailure())
            .isTrue();

        assertThat(response.getErrors())
            .containsOnly("Field 'existingPasskey' does not match the existing passkey for the user");
    }

    @Test
    void whenValidating_givenUserDoesNotNeedChange_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final User user = generateUser(hardware);

        final UserChangeRequest userChange = UserChangeRequest.builder()
            .existingPasskey(user.getPasskey())
            .foldingUserName(user.getFoldingUserName())
            .passkey(user.getPasskey())
            .hardwareId(hardware.getId())
            .userId(user.getId())
            .liveStatsLink(user.getLiveStatsLink())
            .build();

        final UserChangeValidator userChangeValidator = UserChangeValidator.create(new ValidFoldingStatsRetriever());
        final ValidationResult<UserChange> response = userChangeValidator.validate(
            userChange,
            Collections.emptyList(),
            List.of(hardware),
            List.of(user)
        );

        assertThat(response.isFailure())
            .isTrue();

        assertThat(response.getErrors())
            .containsOnly("User already has the values supplied in UserChangeRequest");
    }

    @Test
    void whenValidating_givenMatchingUserChangeAlreadyExists_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final User user = generateUser(hardware);

        final UserChangeRequest userChange = UserChangeRequest.builder()
            .existingPasskey(user.getPasskey())
            .foldingUserName(user.getFoldingUserName())
            .passkey(user.getPasskey())
            .hardwareId(hardware.getId())
            .userId(user.getId())
            .liveStatsLink("https://www.google.ie")
            .build();

        final UserChange existingUserChange = UserChange.builder()
            .newUser(User.builder()
                .id(user.getId())
                .foldingUserName(user.getFoldingUserName())
                .displayName(user.getDisplayName())
                .passkey(user.getPasskey())
                .category(user.getCategory())
                .profileLink(user.getProfileLink())
                .liveStatsLink("https://www.google.ie")
                .userIsCaptain(user.isUserIsCaptain())
                .hardware(user.getHardware())
                .build())
            .build();

        final UserChangeValidator userChangeValidator = UserChangeValidator.create(new ValidFoldingStatsRetriever());
        final ValidationResult<UserChange> response = userChangeValidator.validate(
            userChange,
            List.of(existingUserChange),
            List.of(hardware),
            List.of(user)
        );

        assertThat(response.isFailure())
            .isTrue();

        assertThat(response.getErrors())
            .containsOnly("Payload conflicts with an existing object on: [foldingUserName, passkey, liveStatsLink, hardwareId]");
    }

    @Test
    void whenValidating_givenUserWorkUnitsCannotBeAccessed_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final User user = generateUser(hardware);

        final UserChangeRequest userChange = UserChangeRequest.builder()
            .existingPasskey(user.getPasskey())
            .foldingUserName(user.getFoldingUserName())
            .passkey("DummyPasskey12345678901234567891")
            .hardwareId(hardware.getId())
            .userId(user.getId())
            .liveStatsLink("https://www.google.ie")
            .build();

        final UserChangeValidator userChangeValidator = UserChangeValidator.create(new ExternalConnectionFoldingStatsRetriever());
        final ValidationResult<UserChange> response = userChangeValidator.validate(
            userChange,
            Collections.emptyList(),
            List.of(hardware),
            List.of(user)
        );

        assertThat(response.isFailure())
            .isTrue();

        assertThat(response.getErrors())
            .containsOnly(
                String.format("Unable to connect to 'https://www.google.com' to check stats for Folding@Home user '%s': Error connecting",
                    userChange.getFoldingUserName())
            );
    }

    @Test
    void whenValidating_givenNewUserHasNoWorkUnitsCompleted_thenFailureResponseIsReturned() {
        final Hardware hardware = generateHardware();
        final User user = generateUser(hardware);

        final UserChangeRequest userChange = UserChangeRequest.builder()
            .existingPasskey(user.getPasskey())
            .foldingUserName(user.getFoldingUserName())
            .passkey("DummyPasskey12345678901234567891")
            .hardwareId(hardware.getId())
            .userId(user.getId())
            .liveStatsLink("https://www.google.ie")
            .build();

        final UserChangeValidator userChangeValidator = UserChangeValidator.create(new NoUnitsFoldingStatsRetriever());
        final ValidationResult<UserChange> response = userChangeValidator.validate(
            userChange,
            Collections.emptyList(),
            List.of(hardware),
            List.of(user)
        );

        assertThat(response.isFailure())
            .isTrue();

        assertThat(response.getErrors())
            .containsOnly(
                String.format("User '%s' has 0 Work Units with passkey '%s', there must be at least one completed Work Unit before adding the user",
                    userChange.getFoldingUserName(), userChange.getPasskey())
            );
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

    private static User generateUser(final Hardware hardware) {
        return User.builder()
            .id(userId++)
            .foldingUserName("-folding.Name_1")
            .displayName("user")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.NVIDIA_GPU)
            .profileLink("https://www.google.com")
            .liveStatsLink("https://www.google.com")
            .userIsCaptain(true)
            .hardware(hardware)
            .build();
    }
}

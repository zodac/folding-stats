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

import java.util.List;
import me.zodac.folding.api.exception.ConflictException;
import me.zodac.folding.api.exception.UsedByException;
import me.zodac.folding.api.exception.ValidationException;
import me.zodac.folding.api.tc.Category;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.HardwareMake;
import me.zodac.folding.api.tc.HardwareType;
import me.zodac.folding.api.tc.Role;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.bean.api.FoldingRepository;
import me.zodac.folding.rest.api.tc.request.HardwareRequest;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link HardwareValidator}.
 */
class HardwareValidatorTest {

    private static int hardwareId = 1;
    private static int teamId = 1;
    private static int userId = 1;

    @Test
    void whenValidatingCreate_givenValidHardware_thenSuccessResponseIsReturned() {
        final HardwareRequest hardware = HardwareRequest.builder()
            .hardwareName("hardwareName")
            .displayName("displayName")
            .hardwareMake(HardwareMake.AMD.toString())
            .hardwareType(HardwareType.GPU.toString())
            .multiplier(1.00D)
            .averagePpd(1L)
            .build();

        final FoldingRepository foldingRepository = new MockFoldingRepository();

        final HardwareValidator hardwareValidator = new HardwareValidator(foldingRepository);
        final Hardware response = hardwareValidator.create(hardware);

        assertThat(response)
            .as("Expected validation to pass")
            .isNotNull();
    }

    @Test
    void whenValidatingCreate_givenHardwareWithNullName_thenFailureResponseIsReturned() {
        final HardwareRequest hardware = HardwareRequest.builder()
            .hardwareName(null)
            .displayName("displayName")
            .hardwareMake(HardwareMake.AMD.toString())
            .hardwareType(HardwareType.GPU.toString())
            .multiplier(1.00D)
            .averagePpd(1L)
            .build();

        final FoldingRepository foldingRepository = new MockFoldingRepository();

        final HardwareValidator hardwareValidator = new HardwareValidator(foldingRepository);
        final ValidationException e = catchThrowableOfType(() -> hardwareValidator.create(hardware), ValidationException.class);
        assertThat(e.getValidationFailure().errors())
            .containsOnly("Field 'hardwareName' must not be empty");
    }

    @Test
    void whenValidatingCreate_givenOtherHardwareAlreadyExists_thenSuccessResponseIsReturned() {
        final HardwareRequest hardware = HardwareRequest.builder()
            .hardwareName("existingName")
            .displayName("displayName")
            .hardwareMake(HardwareMake.AMD.toString())
            .hardwareType(HardwareType.GPU.toString())
            .multiplier(1.00D)
            .averagePpd(1L)
            .build();

        final Hardware existingHardware = Hardware.create(
            hardwareId++,
            "anotherName",
            "displayName",
            HardwareMake.AMD,
            HardwareType.GPU,
            1.00D,
            1L
        );

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(existingHardware);

        final HardwareValidator hardwareValidator = new HardwareValidator(foldingRepository);
        final Hardware response = hardwareValidator.create(hardware);

        assertThat(response)
            .as("Expected validation to pass")
            .isNotNull();
    }

    @Test
    void whenValidatingCreate_givenHardwareWithNameAlreadyExists_thenFailureResponseIsReturned() {
        final HardwareRequest hardware = HardwareRequest.builder()
            .hardwareName("existingName")
            .displayName("displayName")
            .hardwareMake(HardwareMake.AMD.toString())
            .hardwareType(HardwareType.GPU.toString())
            .multiplier(1.00D)
            .averagePpd(1L)
            .build();

        final Hardware existingHardware = Hardware.create(
            hardwareId++,
            "existingName",
            "displayName",
            HardwareMake.AMD,
            HardwareType.GPU,
            1.00D,
            1L
        );

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(existingHardware);

        final HardwareValidator hardwareValidator = new HardwareValidator(foldingRepository);
        final ConflictException e = catchThrowableOfType(() -> hardwareValidator.create(hardware), ConflictException.class);
        assertThat(e.getConflictFailure().conflictingAttributes())
            .containsOnly("hardwareName");

        assertThat(e.getConflictFailure().conflictingObject())
            .isNotNull();
    }

    @Test
    void whenValidatingCreate_givenHardwareWithNullDisplayName_thenFailureResponseIsReturned() {
        final HardwareRequest hardware = HardwareRequest.builder()
            .hardwareName("hardwareName")
            .displayName(null)
            .hardwareMake(HardwareMake.AMD.toString())
            .hardwareType(HardwareType.GPU.toString())
            .multiplier(1.00D)
            .averagePpd(1L)
            .build();

        final FoldingRepository foldingRepository = new MockFoldingRepository();

        final HardwareValidator hardwareValidator = new HardwareValidator(foldingRepository);
        final ValidationException e = catchThrowableOfType(() -> hardwareValidator.create(hardware), ValidationException.class);
        assertThat(e.getValidationFailure().errors())
            .containsOnly("Field 'displayName' must not be empty");
    }

    @Test
    void whenValidatingCreate_givenHardwareWithInvalidMake_thenFailureResponseIsReturned() {
        final HardwareRequest hardware = HardwareRequest.builder()
            .hardwareName("hardwareName")
            .displayName("displayName")
            .hardwareMake(HardwareMake.INVALID.toString())
            .hardwareType(HardwareType.GPU.toString())
            .multiplier(1.00D)
            .averagePpd(1L)
            .build();

        final FoldingRepository foldingRepository = new MockFoldingRepository();

        final HardwareValidator hardwareValidator = new HardwareValidator(foldingRepository);
        final ValidationException e = catchThrowableOfType(() -> hardwareValidator.create(hardware), ValidationException.class);
        assertThat(e.getValidationFailure().errors())
            .containsOnly("Field 'hardwareMake' must be one of: [AMD, INTEL, NVIDIA]");
    }

    @Test
    void whenValidatingCreate_givenHardwareWithInvalidType_thenFailureResponseIsReturned() {
        final HardwareRequest hardware = HardwareRequest.builder()
            .hardwareName("hardwareName")
            .displayName("displayName")
            .hardwareMake(HardwareMake.AMD.toString())
            .hardwareType(HardwareType.INVALID.toString())
            .multiplier(1.00D)
            .averagePpd(1L)
            .build();

        final FoldingRepository foldingRepository = new MockFoldingRepository();

        final HardwareValidator hardwareValidator = new HardwareValidator(foldingRepository);
        final ValidationException e = catchThrowableOfType(() -> hardwareValidator.create(hardware), ValidationException.class);
        assertThat(e.getValidationFailure().errors())
            .containsOnly("Field 'hardwareType' must be one of: [CPU, GPU]");
    }

    @Test
    void whenValidatingCreate_givenHardwareWithNegativeMultiplier_thenFailureResponseIsReturned() {
        final HardwareRequest hardware = HardwareRequest.builder()
            .hardwareName("hardwareName")
            .displayName("displayName")
            .hardwareMake(HardwareMake.AMD.toString())
            .hardwareType(HardwareType.GPU.toString())
            .multiplier(-1.00D)
            .averagePpd(1L)
            .build();

        final FoldingRepository foldingRepository = new MockFoldingRepository();

        final HardwareValidator hardwareValidator = new HardwareValidator(foldingRepository);
        final ValidationException e = catchThrowableOfType(() -> hardwareValidator.create(hardware), ValidationException.class);
        assertThat(e.getValidationFailure().errors())
            .containsOnly("Field 'multiplier' must be 1.00 or higher");
    }

    @Test
    void whenValidatingCreate_givenHardwareWithNegativeAveragePpd_thenFailureResponseIsReturned() {
        final HardwareRequest hardware = HardwareRequest.builder()
            .hardwareName("hardwareName")
            .displayName("displayName")
            .hardwareMake(HardwareMake.AMD.toString())
            .hardwareType(HardwareType.GPU.toString())
            .multiplier(1.00D)
            .averagePpd(-1L)
            .build();

        final FoldingRepository foldingRepository = new MockFoldingRepository();

        final HardwareValidator hardwareValidator = new HardwareValidator(foldingRepository);
        final ValidationException e = catchThrowableOfType(() -> hardwareValidator.create(hardware), ValidationException.class);
        assertThat(e.getValidationFailure().errors())
            .containsOnly("Field 'averagePpd' must be 1 or higher");
    }

    @Test
    void whenValidatingCreate_givenHardwareWithMultipleErrors_thenAllErrorsAreReturned() {
        final HardwareRequest hardware = HardwareRequest.builder()
            .hardwareName(null)
            .displayName(null)
            .hardwareMake(HardwareMake.INVALID.toString())
            .hardwareType(HardwareType.INVALID.toString())
            .multiplier(-1.00D)
            .averagePpd(-1L)
            .build();

        final FoldingRepository foldingRepository = new MockFoldingRepository();

        final HardwareValidator hardwareValidator = new HardwareValidator(foldingRepository);
        final ValidationException e = catchThrowableOfType(() -> hardwareValidator.create(hardware), ValidationException.class);
        assertThat(e.getValidationFailure().errors())
            .containsOnly(
                "Field 'hardwareName' must not be empty",
                "Field 'displayName' must not be empty",
                "Field 'hardwareMake' must be one of: [AMD, INTEL, NVIDIA]",
                "Field 'hardwareType' must be one of: [CPU, GPU]",
                "Field 'multiplier' must be 1.00 or higher",
                "Field 'averagePpd' must be 1 or higher"
            );
    }

    @Test
    void whenValidatingUpdate_givenValidHardware_thenSuccessResponseIsReturned() {
        final HardwareRequest hardware = HardwareRequest.builder()
            .hardwareName("hardwareName")
            .displayName("displayName")
            .hardwareMake(HardwareMake.AMD.toString())
            .hardwareType(HardwareType.GPU.toString())
            .multiplier(2.00D)
            .averagePpd(1L)
            .build();

        final Hardware existingHardware = generateHardware();

        final FoldingRepository foldingRepository = new MockFoldingRepository();

        final HardwareValidator hardwareValidator = new HardwareValidator(foldingRepository);
        final Hardware response = hardwareValidator.update(hardware, existingHardware);

        assertThat(response)
            .as("Expected validation to pass")
            .isNotNull();
    }

    @Test
    void whenValidatingUpdate_givenHardwareWithNullName_thenFailureResponseIsReturned() {
        final HardwareRequest hardware = HardwareRequest.builder()
            .hardwareName(null)
            .displayName("displayName")
            .hardwareMake(HardwareMake.AMD.toString())
            .hardwareType(HardwareType.GPU.toString())
            .multiplier(1.00D)
            .averagePpd(1L)
            .build();

        final Hardware existingHardware = generateHardware();

        final FoldingRepository foldingRepository = new MockFoldingRepository();

        final HardwareValidator hardwareValidator = new HardwareValidator(foldingRepository);
        final ValidationException e =
            catchThrowableOfType(() -> hardwareValidator.update(hardware, existingHardware), ValidationException.class);
        assertThat(e.getValidationFailure().errors())
            .containsOnly("Field 'hardwareName' must not be empty");
    }

    @Test
    void whenValidatingUpdate_givenHardwareWithNameAlreadyExists_thenFailureResponseIsReturned() {
        final HardwareRequest hardware = HardwareRequest.builder()
            .hardwareName("hardwareName")
            .displayName("displayName")
            .hardwareMake(HardwareMake.AMD.toString())
            .hardwareType(HardwareType.GPU.toString())
            .multiplier(1.00D)
            .averagePpd(1L)
            .build();

        final Hardware existingHardware = Hardware.create(
            1,
            "differentName",
            "displayName",
            HardwareMake.AMD,
            HardwareType.GPU,
            1.00D,
            1L
        );

        final Hardware otherHardware = Hardware.create(
            20,
            "hardwareName",
            "displayName",
            HardwareMake.AMD,
            HardwareType.GPU,
            1.00D,
            1L
        );

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(otherHardware);

        final HardwareValidator hardwareValidator = new HardwareValidator(foldingRepository);
        final ConflictException e =
            catchThrowableOfType(() -> hardwareValidator.update(hardware, existingHardware), ConflictException.class);
        assertThat(e.getConflictFailure().conflictingAttributes())
            .containsOnly("hardwareName");

        assertThat(e.getConflictFailure().conflictingObject())
            .isNotNull();
    }

    @Test
    void whenValidatingUpdate_givenHardwareWithNameAlreadyExists_andExistingHardwareIsHasSameId_thenSuccessResponseIsReturned() {
        final HardwareRequest hardware = HardwareRequest.builder()
            .hardwareName("hardwareName")
            .displayName("displayName")
            .hardwareMake(HardwareMake.AMD.toString())
            .hardwareType(HardwareType.GPU.toString())
            .multiplier(1.00D)
            .averagePpd(1L)
            .build();

        final Hardware existingHardware = Hardware.create(
            1,
            "hardwareName",
            "displayName",
            HardwareMake.AMD,
            HardwareType.GPU,
            1.00D,
            1L
        );

        final Hardware otherHardware = Hardware.create(
            1,
            "hardwareName",
            "displayName",
            HardwareMake.AMD,
            HardwareType.GPU,
            1.00D,
            1L
        );

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(otherHardware);

        final HardwareValidator hardwareValidator = new HardwareValidator(foldingRepository);
        final Hardware response = hardwareValidator.update(hardware, existingHardware);

        assertThat(response)
            .as("Expected validation to pass")
            .isNotNull();
    }

    @Test
    void whenValidatingUpdate_givenHardwareWithNullDisplayName_thenFailureResponseIsReturned() {
        final HardwareRequest hardware = HardwareRequest.builder()
            .hardwareName("hardwareName")
            .displayName(null)
            .hardwareMake(HardwareMake.AMD.toString())
            .hardwareType(HardwareType.GPU.toString())
            .multiplier(1.00D)
            .averagePpd(1L)
            .build();

        final Hardware existingHardware = generateHardware();

        final FoldingRepository foldingRepository = new MockFoldingRepository();

        final HardwareValidator hardwareValidator = new HardwareValidator(foldingRepository);
        final ValidationException e =
            catchThrowableOfType(() -> hardwareValidator.update(hardware, existingHardware), ValidationException.class);
        assertThat(e.getValidationFailure().errors())
            .containsOnly("Field 'displayName' must not be empty");
    }

    @Test
    void whenValidatingUpdate_givenHardwareWithInvalidMake_thenFailureResponseIsReturned() {
        final HardwareRequest hardware = HardwareRequest.builder()
            .hardwareName("hardwareName")
            .displayName("displayName")
            .hardwareMake(HardwareMake.INVALID.toString())
            .hardwareType(HardwareType.GPU.toString())
            .multiplier(1.00D)
            .averagePpd(1L)
            .build();

        final Hardware existingHardware = generateHardware();

        final FoldingRepository foldingRepository = new MockFoldingRepository();

        final HardwareValidator hardwareValidator = new HardwareValidator(foldingRepository);
        final ValidationException e =
            catchThrowableOfType(() -> hardwareValidator.update(hardware, existingHardware), ValidationException.class);
        assertThat(e.getValidationFailure().errors())
            .containsOnly("Field 'hardwareMake' must be one of: [AMD, INTEL, NVIDIA]");
    }

    @Test
    void whenValidatingUpdate_givenHardwareWithInvalidType_thenFailureResponseIsReturned() {
        final HardwareRequest hardware = HardwareRequest.builder()
            .hardwareName("hardwareName")
            .displayName("displayName")
            .hardwareMake(HardwareMake.AMD.toString())
            .hardwareType(HardwareType.INVALID.toString())
            .multiplier(1.00D)
            .averagePpd(1L)
            .build();

        final Hardware existingHardware = generateHardware();

        final FoldingRepository foldingRepository = new MockFoldingRepository();

        final HardwareValidator hardwareValidator = new HardwareValidator(foldingRepository);
        final ValidationException e =
            catchThrowableOfType(() -> hardwareValidator.update(hardware, existingHardware), ValidationException.class);
        assertThat(e.getValidationFailure().errors())
            .containsOnly("Field 'hardwareType' must be one of: [CPU, GPU]");
    }

    @Test
    void whenValidatingUpdate_givenHardwareWithNegativeMultiplier_thenFailureResponseIsReturned() {
        final HardwareRequest hardware = HardwareRequest.builder()
            .hardwareName("hardwareName")
            .displayName("displayName")
            .hardwareMake(HardwareMake.AMD.toString())
            .hardwareType(HardwareType.GPU.toString())
            .multiplier(-1.00D)
            .averagePpd(1L)
            .build();

        final Hardware existingHardware = generateHardware();

        final FoldingRepository foldingRepository = new MockFoldingRepository();

        final HardwareValidator hardwareValidator = new HardwareValidator(foldingRepository);
        final ValidationException e =
            catchThrowableOfType(() -> hardwareValidator.update(hardware, existingHardware), ValidationException.class);
        assertThat(e.getValidationFailure().errors())
            .containsOnly("Field 'multiplier' must be 1.00 or higher");
    }

    @Test
    void whenValidatingUpdate_givenHardwareWithNegativeAveragePpd_thenFailureResponseIsReturned() {
        final HardwareRequest hardware = HardwareRequest.builder()
            .hardwareName("hardwareName")
            .displayName("displayName")
            .hardwareMake(HardwareMake.AMD.toString())
            .hardwareType(HardwareType.GPU.toString())
            .multiplier(1.00D)
            .averagePpd(-1L)
            .build();

        final Hardware existingHardware = generateHardware();

        final FoldingRepository foldingRepository = new MockFoldingRepository();

        final HardwareValidator hardwareValidator = new HardwareValidator(foldingRepository);
        final ValidationException e =
            catchThrowableOfType(() -> hardwareValidator.update(hardware, existingHardware), ValidationException.class);
        assertThat(e.getValidationFailure().errors())
            .containsOnly("Field 'averagePpd' must be 1 or higher");
    }

    @Test
    void whenValidatingUpdate_givenHardwareWithMultipleErrors_thenAllErrorsReturned() {
        final HardwareRequest hardware = HardwareRequest.builder()
            .hardwareName("hardwareName")
            .displayName(null)
            .hardwareMake(HardwareMake.INVALID.toString())
            .hardwareType(HardwareType.INVALID.toString())
            .multiplier(-1.00D)
            .averagePpd(-1L)
            .build();

        final Hardware existingHardware = generateHardware();

        final FoldingRepository foldingRepository = new MockFoldingRepository();

        final HardwareValidator hardwareValidator = new HardwareValidator(foldingRepository);
        final ValidationException e =
            catchThrowableOfType(() -> hardwareValidator.update(hardware, existingHardware), ValidationException.class);
        assertThat(e.getValidationFailure().errors())
            .containsOnly(
                "Field 'displayName' must not be empty",
                "Field 'hardwareMake' must be one of: [AMD, INTEL, NVIDIA]",
                "Field 'hardwareType' must be one of: [CPU, GPU]",
                "Field 'multiplier' must be 1.00 or higher",
                "Field 'averagePpd' must be 1 or higher"
            );
    }

    @Test
    void whenValidatingDelete_givenHardwareThatIsNotBeingUsed_thenSuccessResponseIsReturned() {
        final Hardware existingHardware = generateHardware();

        final FoldingRepository foldingRepository = new MockFoldingRepository();

        final HardwareValidator hardwareValidator = new HardwareValidator(foldingRepository);
        final Hardware response = hardwareValidator.delete(existingHardware);

        assertThat(response)
            .as("Expected validation to pass")
            .isNotNull();
    }

    @Test
    void whenValidatingDelete_givenHardwareThatIsBeingUsedByUser_thenFailureResponseIsReturned() {
        final Hardware existingHardware = generateHardware();

        final User existingUser = User.create(
            userId++,
            "userName",
            "userName",
            "DummyPasskey12345678901234567890",
            Category.NVIDIA_GPU,
            "https://www.google.com",
            "https://www.google.com",
            existingHardware,
            generateTeam(),
            Role.MEMBER
        );

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createUser(existingUser);

        final HardwareValidator hardwareValidator = new HardwareValidator(foldingRepository);
        final UsedByException e = catchThrowableOfType(() -> hardwareValidator.delete(existingHardware), UsedByException.class);
        final List<?> usedBy = List.of(e.getUsedByFailure().usedBy());
        assertThat(usedBy)
            .hasSize(1);

        assertThat(usedBy.get(0).toString())
            .contains("DummyPas************************")
            .doesNotContain("DummyPasskey12345678901234567890");
    }

    @Test
    void whenValidatingDelete_givenHardwareExistsButIsNotBeingUsedByUser_thenSuccessResponseIsReturned() {
        final Hardware existingHardware = generateHardware();
        final Hardware userHardware = Hardware.create(
            2,
            "hardwareName2",
            "displayName2",
            HardwareMake.AMD,
            HardwareType.GPU,
            1.00D,
            1L
        );

        final User existingUser = User.create(
            userId++,
            "userName",
            "userName",
            "DummyPasskey12345678901234567890",
            Category.NVIDIA_GPU,
            "https://www.google.com",
            "https://www.google.com",
            userHardware,
            generateTeam(),
            Role.MEMBER
        );

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createUser(existingUser);

        final HardwareValidator hardwareValidator = new HardwareValidator(foldingRepository);
        final Hardware response = hardwareValidator.delete(existingHardware);

        assertThat(response)
            .as("Expected validation to pass")
            .isNotNull();
    }

    private static Hardware generateHardware() {
        return Hardware.create(
            hardwareId++,
            "hardwareName",
            "displayName",
            HardwareMake.AMD,
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
            null
        );
    }
}

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

import java.util.List;
import net.zodac.folding.api.exception.ConflictException;
import net.zodac.folding.api.exception.UsedByException;
import net.zodac.folding.api.exception.ValidationException;
import net.zodac.folding.api.tc.Category;
import net.zodac.folding.api.tc.Hardware;
import net.zodac.folding.api.tc.HardwareMake;
import net.zodac.folding.api.tc.HardwareType;
import net.zodac.folding.api.tc.Role;
import net.zodac.folding.api.tc.Team;
import net.zodac.folding.api.tc.User;
import net.zodac.folding.bean.api.FoldingRepository;
import net.zodac.folding.rest.api.tc.request.HardwareRequest;
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
        final HardwareRequest hardware = generateHardwareRequest(
            "hardwareName",
            HardwareMake.AMD,
            HardwareType.GPU,
            1.00D,
            1L
        );

        final FoldingRepository foldingRepository = new MockFoldingRepository();

        final HardwareValidator hardwareValidator = new HardwareValidator(foldingRepository);
        final Hardware response = hardwareValidator.create(hardware);

        assertThat(response)
            .as("Expected validation to pass")
            .isNotNull();
    }

    @Test
    void whenValidatingCreate_givenOtherHardwareAlreadyExists_thenSuccessResponseIsReturned() {
        final HardwareRequest hardware = generateHardwareRequest(
            "existingName",
            HardwareMake.AMD,
            HardwareType.GPU,
            1.00D,
            1L
        );

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
        final HardwareRequest hardware = generateHardwareRequest(
            "existingName",
            HardwareMake.AMD,
            HardwareType.GPU,
            1.00D,
            1L
        );

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
        final ConflictException e = catchThrowableOfType(ConflictException.class, () -> hardwareValidator.create(hardware));
        assertThat(e.getConflictFailure().conflictingAttributes())
            .containsOnly("hardwareName");

        assertThat(e.getConflictFailure().conflictingObject())
            .isNotNull();
    }

    @Test
    void whenValidatingCreate_givenHardwareWithInvalidMake_thenFailureResponseIsReturned() {
        final HardwareRequest hardware = generateHardwareRequest(
            "hardwareName",
            HardwareMake.INVALID,
            HardwareType.GPU,
            1.00D,
            1L
        );

        final FoldingRepository foldingRepository = new MockFoldingRepository();

        final HardwareValidator hardwareValidator = new HardwareValidator(foldingRepository);
        final ValidationException e = catchThrowableOfType(ValidationException.class, () -> hardwareValidator.create(hardware));
        assertThat(e.getValidationFailure().errors())
            .containsOnly("Field 'hardwareMake' must be one of: [AMD, INTEL, NVIDIA]");
    }

    @Test
    void whenValidatingCreate_givenHardwareWithInvalidType_thenFailureResponseIsReturned() {
        final HardwareRequest hardware = generateHardwareRequest(
            "hardwareName",
            HardwareMake.AMD,
            HardwareType.INVALID,
            1.00D,
            1L
        );

        final FoldingRepository foldingRepository = new MockFoldingRepository();

        final HardwareValidator hardwareValidator = new HardwareValidator(foldingRepository);
        final ValidationException e = catchThrowableOfType(ValidationException.class, () -> hardwareValidator.create(hardware));
        assertThat(e.getValidationFailure().errors())
            .containsOnly("Field 'hardwareType' must be one of: [CPU, GPU]");
    }

    @Test
    void whenValidatingCreate_givenHardwareWithNegativeMultiplier_thenFailureResponseIsReturned() {
        final HardwareRequest hardware = generateHardwareRequest(
            "hardwareName",
            HardwareMake.AMD,
            HardwareType.GPU,
            -1.00D,
            1L
        );

        final FoldingRepository foldingRepository = new MockFoldingRepository();

        final HardwareValidator hardwareValidator = new HardwareValidator(foldingRepository);
        final ValidationException e = catchThrowableOfType(ValidationException.class, () -> hardwareValidator.create(hardware));
        assertThat(e.getValidationFailure().errors())
            .containsOnly("Field 'multiplier' must be 1.00 or higher");
    }

    @Test
    void whenValidatingCreate_givenHardwareWithNegativeAveragePpd_thenFailureResponseIsReturned() {
        final HardwareRequest hardware = generateHardwareRequest(
            "hardwareName",
            HardwareMake.AMD,
            HardwareType.GPU,
            1.00D,
            -1L
        );

        final FoldingRepository foldingRepository = new MockFoldingRepository();

        final HardwareValidator hardwareValidator = new HardwareValidator(foldingRepository);
        final ValidationException e = catchThrowableOfType(ValidationException.class, () -> hardwareValidator.create(hardware));
        assertThat(e.getValidationFailure().errors())
            .containsOnly("Field 'averagePpd' must be 1 or higher");
    }

    @Test
    void whenValidatingCreate_givenHardwareWithMultipleErrors_thenAllErrorsAreReturned() {
        final HardwareRequest hardware = generateHardwareRequest(
            "hardwareName",
            HardwareMake.INVALID,
            HardwareType.INVALID,
            -1.00D,
            -1L
        );

        final FoldingRepository foldingRepository = new MockFoldingRepository();

        final HardwareValidator hardwareValidator = new HardwareValidator(foldingRepository);
        final ValidationException e = catchThrowableOfType(ValidationException.class, () -> hardwareValidator.create(hardware));
        assertThat(e.getValidationFailure().errors())
            .containsOnly(
                "Field 'hardwareMake' must be one of: [AMD, INTEL, NVIDIA]",
                "Field 'hardwareType' must be one of: [CPU, GPU]",
                "Field 'multiplier' must be 1.00 or higher",
                "Field 'averagePpd' must be 1 or higher"
            );
    }

    @Test
    void whenValidatingUpdate_givenValidHardware_thenSuccessResponseIsReturned() {
        final HardwareRequest hardware = generateHardwareRequest(
            "hardwareName",
            HardwareMake.AMD,
            HardwareType.GPU,
            2.00D,
            1L
        );

        final Hardware existingHardware = generateHardware();

        final FoldingRepository foldingRepository = new MockFoldingRepository();

        final HardwareValidator hardwareValidator = new HardwareValidator(foldingRepository);
        final Hardware response = hardwareValidator.update(hardware, existingHardware);

        assertThat(response)
            .as("Expected validation to pass")
            .isNotNull();
    }

    @Test
    void whenValidatingUpdate_givenHardwareWithNameAlreadyExists_thenFailureResponseIsReturned() {
        final HardwareRequest hardware = generateHardwareRequest(
            "hardwareName",
            HardwareMake.AMD,
            HardwareType.GPU,
            1.00D,
            1L
        );

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
        final ConflictException e = catchThrowableOfType(ConflictException.class, () -> hardwareValidator.update(hardware, existingHardware));
        assertThat(e.getConflictFailure().conflictingAttributes())
            .containsOnly("hardwareName");

        assertThat(e.getConflictFailure().conflictingObject())
            .isNotNull();
    }

    @Test
    void whenValidatingUpdate_givenHardwareWithNameAlreadyExists_andExistingHardwareIsHasSameId_thenSuccessResponseIsReturned() {
        final HardwareRequest hardware = generateHardwareRequest(
            "hardwareName",
            HardwareMake.AMD,
            HardwareType.GPU,
            1.00D,
            1L
        );

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
    void whenValidatingUpdate_givenHardwareWithInvalidMake_thenFailureResponseIsReturned() {
        final HardwareRequest hardware = generateHardwareRequest(
            "hardwareName",
            HardwareMake.INVALID,
            HardwareType.GPU,
            1.00D,
            1L
        );

        final Hardware existingHardware = generateHardware();

        final FoldingRepository foldingRepository = new MockFoldingRepository();

        final HardwareValidator hardwareValidator = new HardwareValidator(foldingRepository);
        final ValidationException e = catchThrowableOfType(ValidationException.class, () -> hardwareValidator.update(hardware, existingHardware));
        assertThat(e.getValidationFailure().errors())
            .containsOnly("Field 'hardwareMake' must be one of: [AMD, INTEL, NVIDIA]");
    }

    @Test
    void whenValidatingUpdate_givenHardwareWithInvalidType_thenFailureResponseIsReturned() {
        final HardwareRequest hardware = generateHardwareRequest(
            "hardwareName",
            HardwareMake.AMD,
            HardwareType.INVALID,
            1.00D,
            1L
        );

        final Hardware existingHardware = generateHardware();

        final FoldingRepository foldingRepository = new MockFoldingRepository();

        final HardwareValidator hardwareValidator = new HardwareValidator(foldingRepository);
        final ValidationException e = catchThrowableOfType(ValidationException.class, () -> hardwareValidator.update(hardware, existingHardware));
        assertThat(e.getValidationFailure().errors())
            .containsOnly("Field 'hardwareType' must be one of: [CPU, GPU]");
    }

    @Test
    void whenValidatingUpdate_givenHardwareWithNegativeMultiplier_thenFailureResponseIsReturned() {
        final HardwareRequest hardware = generateHardwareRequest(
            "hardwareName",
            HardwareMake.AMD,
            HardwareType.GPU,
            -1.00D,
            1L
        );

        final Hardware existingHardware = generateHardware();

        final FoldingRepository foldingRepository = new MockFoldingRepository();

        final HardwareValidator hardwareValidator = new HardwareValidator(foldingRepository);
        final ValidationException e = catchThrowableOfType(ValidationException.class, () -> hardwareValidator.update(hardware, existingHardware));
        assertThat(e.getValidationFailure().errors())
            .containsOnly("Field 'multiplier' must be 1.00 or higher");
    }

    @Test
    void whenValidatingUpdate_givenHardwareWithNegativeAveragePpd_thenFailureResponseIsReturned() {
        final HardwareRequest hardware = generateHardwareRequest(
            "hardwareName",
            HardwareMake.AMD,
            HardwareType.GPU,
            1.00D,
            -1L
        );

        final Hardware existingHardware = generateHardware();

        final FoldingRepository foldingRepository = new MockFoldingRepository();

        final HardwareValidator hardwareValidator = new HardwareValidator(foldingRepository);
        final ValidationException e = catchThrowableOfType(ValidationException.class, () -> hardwareValidator.update(hardware, existingHardware));
        assertThat(e.getValidationFailure().errors())
            .containsOnly("Field 'averagePpd' must be 1 or higher");
    }

    @Test
    void whenValidatingUpdate_givenHardwareWithMultipleErrors_thenAllErrorsReturned() {
        final HardwareRequest hardware = generateHardwareRequest(
            "hardwareName",
            HardwareMake.INVALID,
            HardwareType.INVALID,
            -1.00D,
            -1L
        );

        final Hardware existingHardware = generateHardware();

        final FoldingRepository foldingRepository = new MockFoldingRepository();

        final HardwareValidator hardwareValidator = new HardwareValidator(foldingRepository);
        final ValidationException e = catchThrowableOfType(ValidationException.class, () -> hardwareValidator.update(hardware, existingHardware));
        assertThat(e.getValidationFailure().errors())
            .containsOnly(
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
        final UsedByException e = catchThrowableOfType(UsedByException.class, () -> hardwareValidator.delete(existingHardware));
        final List<?> usedBy = List.of(e.getUsedByFailure().usedBy());
        assertThat(usedBy)
            .hasSize(1);

        assertThat(usedBy.getFirst().toString())
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

    private static HardwareRequest generateHardwareRequest(final String hardwareName,
                                                           final HardwareMake hardwareMake,
                                                           final HardwareType hardwareType,
                                                           final double multiplier,
                                                           final long averagePpd) {
        return new HardwareRequest(hardwareName, "displayName", hardwareMake.toString(), hardwareType.toString(), multiplier, averagePpd);
    }
}

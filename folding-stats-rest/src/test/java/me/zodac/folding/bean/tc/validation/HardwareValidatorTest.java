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

import java.util.List;
import me.zodac.folding.api.FoldingRepository;
import me.zodac.folding.api.exception.ConflictException;
import me.zodac.folding.api.exception.UsedByException;
import me.zodac.folding.api.exception.ValidationException;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.HardwareMake;
import me.zodac.folding.api.tc.HardwareType;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.rest.api.tc.request.HardwareRequest;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link HardwareValidator}.
 */
class HardwareValidatorTest {

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
        assertThat(e.getValidationFailure().getErrors())
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

        final Hardware existingHardware = Hardware.builder()
            .hardwareName("anotherName")
            .displayName("displayName")
            .hardwareMake(HardwareMake.AMD)
            .hardwareType(HardwareType.GPU)
            .multiplier(1.00D)
            .averagePpd(1L)
            .build();

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

        final Hardware existingHardware = Hardware.builder()
            .hardwareName("existingName")
            .displayName("displayName")
            .hardwareMake(HardwareMake.AMD)
            .hardwareType(HardwareType.GPU)
            .multiplier(1.00D)
            .averagePpd(1L)
            .build();

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(existingHardware);

        final HardwareValidator hardwareValidator = new HardwareValidator(foldingRepository);
        final ConflictException e = catchThrowableOfType(() -> hardwareValidator.create(hardware), ConflictException.class);
        assertThat(e.getConflictFailure().getConflictingAttributes())
            .containsOnly("hardwareName");

        assertThat(e.getConflictFailure().getConflictingObject())
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
        assertThat(e.getValidationFailure().getErrors())
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
        assertThat(e.getValidationFailure().getErrors())
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
        assertThat(e.getValidationFailure().getErrors())
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
        assertThat(e.getValidationFailure().getErrors())
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
        assertThat(e.getValidationFailure().getErrors())
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
        assertThat(e.getValidationFailure().getErrors())
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

        final Hardware existingHardware = Hardware.builder()
            .hardwareName("hardwareName")
            .displayName("displayName")
            .hardwareMake(HardwareMake.AMD)
            .hardwareType(HardwareType.GPU)
            .multiplier(1.00D)
            .averagePpd(1L)
            .build();

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

        final Hardware existingHardware = Hardware.builder()
            .hardwareName("hardwareName")
            .displayName("displayName")
            .hardwareMake(HardwareMake.AMD)
            .hardwareType(HardwareType.GPU)
            .multiplier(1.00D)
            .averagePpd(1L)
            .build();

        final FoldingRepository foldingRepository = new MockFoldingRepository();

        final HardwareValidator hardwareValidator = new HardwareValidator(foldingRepository);
        final ValidationException e =
            catchThrowableOfType(() -> hardwareValidator.update(hardware, existingHardware), ValidationException.class);
        assertThat(e.getValidationFailure().getErrors())
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

        final Hardware existingHardware = Hardware.builder()
            .id(1)
            .hardwareName("differentName")
            .displayName("displayName")
            .hardwareMake(HardwareMake.AMD)
            .hardwareType(HardwareType.GPU)
            .multiplier(1.00D)
            .averagePpd(1L)
            .build();

        final Hardware otherHardware = Hardware.builder()
            .id(20)
            .hardwareName("hardwareName")
            .displayName("displayName")
            .hardwareMake(HardwareMake.AMD)
            .hardwareType(HardwareType.GPU)
            .multiplier(1.00D)
            .averagePpd(1L)
            .build();

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createHardware(otherHardware);

        final HardwareValidator hardwareValidator = new HardwareValidator(foldingRepository);
        final ConflictException e =
            catchThrowableOfType(() -> hardwareValidator.update(hardware, existingHardware), ConflictException.class);
        assertThat(e.getConflictFailure().getConflictingAttributes())
            .containsOnly("hardwareName");

        assertThat(e.getConflictFailure().getConflictingObject())
            .isNotNull();
    }

    @Test
    void whenValidatingUpdate_givenHardwareWithNameAlreadyExists_andExistingHardwareIsTheOneBeingUpdated_thenSuccessResponseIsReturned() {
        final HardwareRequest hardware = HardwareRequest.builder()
            .hardwareName("hardwareName")
            .displayName("displayName")
            .hardwareMake(HardwareMake.AMD.toString())
            .hardwareType(HardwareType.GPU.toString())
            .multiplier(1.00D)
            .averagePpd(1L)
            .build();

        final Hardware existingHardware = Hardware.builder()
            .id(1)
            .hardwareName("hardwareName")
            .displayName("displayName")
            .hardwareMake(HardwareMake.AMD)
            .hardwareType(HardwareType.GPU)
            .multiplier(1.00D)
            .averagePpd(1L)
            .build();

        final Hardware otherHardware = Hardware.builder()
            .id(1)
            .hardwareName("hardwareName")
            .displayName("displayName")
            .hardwareMake(HardwareMake.AMD)
            .hardwareType(HardwareType.GPU)
            .multiplier(1.00D)
            .averagePpd(1L)
            .build();

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

        final Hardware existingHardware = Hardware.builder()
            .hardwareName("hardwareName")
            .displayName("displayName")
            .hardwareMake(HardwareMake.AMD)
            .hardwareType(HardwareType.GPU)
            .multiplier(1.00D)
            .averagePpd(1L)
            .build();

        final FoldingRepository foldingRepository = new MockFoldingRepository();

        final HardwareValidator hardwareValidator = new HardwareValidator(foldingRepository);
        final ValidationException e =
            catchThrowableOfType(() -> hardwareValidator.update(hardware, existingHardware), ValidationException.class);
        assertThat(e.getValidationFailure().getErrors())
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

        final Hardware existingHardware = Hardware.builder()
            .hardwareName("hardwareName")
            .displayName("displayName")
            .hardwareMake(HardwareMake.AMD)
            .hardwareType(HardwareType.GPU)
            .multiplier(1.00D)
            .averagePpd(1L)
            .build();

        final FoldingRepository foldingRepository = new MockFoldingRepository();

        final HardwareValidator hardwareValidator = new HardwareValidator(foldingRepository);
        final ValidationException e =
            catchThrowableOfType(() -> hardwareValidator.update(hardware, existingHardware), ValidationException.class);
        assertThat(e.getValidationFailure().getErrors())
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

        final Hardware existingHardware = Hardware.builder()
            .hardwareName("hardwareName")
            .displayName("displayName")
            .hardwareMake(HardwareMake.AMD)
            .hardwareType(HardwareType.GPU)
            .multiplier(1.00D)
            .averagePpd(1L)
            .build();

        final FoldingRepository foldingRepository = new MockFoldingRepository();

        final HardwareValidator hardwareValidator = new HardwareValidator(foldingRepository);
        final ValidationException e =
            catchThrowableOfType(() -> hardwareValidator.update(hardware, existingHardware), ValidationException.class);
        assertThat(e.getValidationFailure().getErrors())
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

        final Hardware existingHardware = Hardware.builder()
            .hardwareName("hardwareName")
            .displayName("displayName")
            .hardwareMake(HardwareMake.AMD)
            .hardwareType(HardwareType.GPU)
            .multiplier(1.00D)
            .averagePpd(1L)
            .build();

        final FoldingRepository foldingRepository = new MockFoldingRepository();

        final HardwareValidator hardwareValidator = new HardwareValidator(foldingRepository);
        final ValidationException e =
            catchThrowableOfType(() -> hardwareValidator.update(hardware, existingHardware), ValidationException.class);
        assertThat(e.getValidationFailure().getErrors())
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

        final Hardware existingHardware = Hardware.builder()
            .hardwareName("hardwareName")
            .displayName("displayName")
            .hardwareMake(HardwareMake.AMD)
            .hardwareType(HardwareType.GPU)
            .multiplier(1.00D)
            .averagePpd(1L)
            .build();

        final FoldingRepository foldingRepository = new MockFoldingRepository();

        final HardwareValidator hardwareValidator = new HardwareValidator(foldingRepository);
        final ValidationException e =
            catchThrowableOfType(() -> hardwareValidator.update(hardware, existingHardware), ValidationException.class);
        assertThat(e.getValidationFailure().getErrors())
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

        final Hardware existingHardware = Hardware.builder()
            .hardwareName("hardwareName")
            .displayName("displayName")
            .hardwareMake(HardwareMake.AMD)
            .hardwareType(HardwareType.GPU)
            .multiplier(1.00D)
            .averagePpd(1L)
            .build();

        final FoldingRepository foldingRepository = new MockFoldingRepository();

        final HardwareValidator hardwareValidator = new HardwareValidator(foldingRepository);
        final ValidationException e =
            catchThrowableOfType(() -> hardwareValidator.update(hardware, existingHardware), ValidationException.class);
        assertThat(e.getValidationFailure().getErrors())
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
        final Hardware existingHardware = Hardware.builder()
            .hardwareName("hardwareName")
            .displayName("displayName")
            .hardwareMake(HardwareMake.AMD)
            .hardwareType(HardwareType.GPU)
            .multiplier(1.00D)
            .averagePpd(1L)
            .build();

        final FoldingRepository foldingRepository = new MockFoldingRepository();

        final HardwareValidator hardwareValidator = new HardwareValidator(foldingRepository);
        final Hardware response = hardwareValidator.delete(existingHardware);

        assertThat(response)
            .as("Expected validation to pass")
            .isNotNull();
    }

    @Test
    void whenValidatingDelete_givenHardwareThatIsBeingUsedByUser_thenFailureResponseIsReturned() {
        final Hardware existingHardware = Hardware.builder()
            .id(1)
            .hardwareName("hardwareName")
            .displayName("displayName")
            .hardwareMake(HardwareMake.AMD)
            .hardwareType(HardwareType.GPU)
            .multiplier(1.00D)
            .averagePpd(1L)
            .build();

        final User existingUser = User.builder()
            .passkey("DummyPasskey12345678901234567890")
            .hardware(existingHardware)
            .build();

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createUser(existingUser);

        final HardwareValidator hardwareValidator = new HardwareValidator(foldingRepository);
        final UsedByException e = catchThrowableOfType(() -> hardwareValidator.delete(existingHardware), UsedByException.class);
        final List<?> usedBy = List.of(e.getUsedByFailure().getUsedBy());
        assertThat(usedBy)
            .hasSize(1);

        assertThat(usedBy.get(0).toString())
            .contains("DummyPas************************")
            .doesNotContain("DummyPasskey12345678901234567890");
    }

    @Test
    void whenValidatingDelete_givenHardwareExistsButIsNotBeingUsedByUser_thenSuccessResponseIsReturned() {
        final Hardware existingHardware = Hardware.builder()
            .id(1)
            .hardwareName("hardwareName")
            .displayName("displayName")
            .hardwareMake(HardwareMake.AMD)
            .hardwareType(HardwareType.GPU)
            .multiplier(1.00D)
            .averagePpd(1L)
            .build();

        final Hardware userHardware = Hardware.builder()
            .id(2)
            .hardwareName("hardwareName2")
            .displayName("displayName2")
            .hardwareMake(HardwareMake.AMD)
            .hardwareType(HardwareType.GPU)
            .multiplier(1.00D)
            .averagePpd(1L)
            .build();

        final User existingUser = User.builder()
            .hardware(userHardware)
            .passkey("DummyPasskey12345678901234567890")
            .build();

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createUser(existingUser);

        final HardwareValidator hardwareValidator = new HardwareValidator(foldingRepository);
        final Hardware response = hardwareValidator.delete(existingHardware);

        assertThat(response)
            .as("Expected validation to pass")
            .isNotNull();
    }
}

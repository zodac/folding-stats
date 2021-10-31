package me.zodac.folding.rest.validator;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
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

        final ValidationResult<Hardware> response = HardwareValidator.validateCreate(hardware, Collections.emptyList());

        assertThat(response.isFailure())
            .as("Expected validation to pass, instead failed with errors: " + response.getErrors())
            .isFalse();
    }

    @Test
    void whenValidatingCreate_givenNullHardware_thenFailureResponseIsReturned() {
        final ValidationResult<Hardware> response = HardwareValidator.validateCreate(null, Collections.emptyList());

        assertThat(response.isFailure())
            .isTrue();

        assertThat(response.getErrors())
            .containsOnly("Payload is null");
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

        final ValidationResult<Hardware> response = HardwareValidator.validateCreate(hardware, Collections.emptyList());

        assertThat(response.isFailure())
            .isTrue();

        assertThat(response.getErrors())
            .containsOnly("Field 'hardwareName' must not be empty");
    }

    @Test
    void whenValidatingCreate_givenOtherHardwareAlreadyExists_thenSuccessResponseIsReturned() {
        final Collection<Hardware> allHardware = List.of(Hardware.builder()
            .hardwareName("anotherName")
            .displayName("displayName")
            .hardwareMake(HardwareMake.AMD)
            .hardwareType(HardwareType.GPU)
            .multiplier(1.00D)
            .averagePpd(1L)
            .build()
        );

        final HardwareRequest hardware = HardwareRequest.builder()
            .hardwareName("existingName")
            .displayName("displayName")
            .hardwareMake(HardwareMake.AMD.toString())
            .hardwareType(HardwareType.GPU.toString())
            .multiplier(1.00D)
            .averagePpd(1L)
            .build();

        final ValidationResult<Hardware> response = HardwareValidator.validateCreate(hardware, allHardware);

        assertThat(response.isFailure())
            .as("Expected validation to pass, instead failed with errors: " + response.getErrors())
            .isFalse();
    }

    @Test
    void whenValidatingCreate_givenHardwareWithNameAlreadyExists_thenFailureResponseIsReturned() {
        final Collection<Hardware> allHardware = List.of(Hardware.builder()
            .hardwareName("existingName")
            .displayName("displayName")
            .hardwareMake(HardwareMake.AMD)
            .hardwareType(HardwareType.GPU)
            .multiplier(1.00D)
            .averagePpd(1L)
            .build()
        );

        final HardwareRequest hardware = HardwareRequest.builder()
            .hardwareName("existingName")
            .displayName("displayName")
            .hardwareMake(HardwareMake.AMD.toString())
            .hardwareType(HardwareType.GPU.toString())
            .multiplier(1.00D)
            .averagePpd(1L)
            .build();

        final ValidationResult<Hardware> response = HardwareValidator.validateCreate(hardware, allHardware);

        assertThat(response.isFailure())
            .isTrue();

        assertThat(response.getErrors())
            .containsOnly("Payload conflicts with an existing object on: [hardwareName]");
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

        final ValidationResult<Hardware> response = HardwareValidator.validateCreate(hardware, Collections.emptyList());

        assertThat(response.isFailure())
            .isTrue();

        assertThat(response.getErrors())
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

        final ValidationResult<Hardware> response = HardwareValidator.validateCreate(hardware, Collections.emptyList());

        assertThat(response.isFailure())
            .isTrue();

        assertThat(response.getErrors())
            .containsOnly("Field 'hardwareMake' must be one of: " + HardwareMake.getAllValues());
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

        final ValidationResult<Hardware> response = HardwareValidator.validateCreate(hardware, Collections.emptyList());

        assertThat(response.isFailure())
            .isTrue();

        assertThat(response.getErrors())
            .containsOnly("Field 'hardwareType' must be one of: " + HardwareType.getAllValues());
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

        final ValidationResult<Hardware> response = HardwareValidator.validateCreate(hardware, Collections.emptyList());

        assertThat(response.isFailure())
            .isTrue();

        assertThat(response.getErrors())
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

        final ValidationResult<Hardware> response = HardwareValidator.validateCreate(hardware, Collections.emptyList());

        assertThat(response.isFailure())
            .isTrue();

        assertThat(response.getErrors())
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

        final ValidationResult<Hardware> response = HardwareValidator.validateCreate(hardware, Collections.emptyList());

        assertThat(response.isFailure())
            .isTrue();

        assertThat(response.getErrors())
            .as("Did not receive expected error messages")
            .hasSize(6)
            .containsOnly(
                "Field 'hardwareName' must not be empty",
                "Field 'displayName' must not be empty",
                "Field 'hardwareMake' must be one of: " + HardwareMake.getAllValues(),
                "Field 'hardwareType' must be one of: " + HardwareType.getAllValues(),
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

        final ValidationResult<Hardware> response = HardwareValidator.validateUpdate(hardware, existingHardware, Collections.emptyList());

        assertThat(response.isFailure())
            .as("Expected validation to pass, instead failed with errors: " + response.getErrors())
            .isFalse();
    }

    @Test
    void whenValidatingUpdate_givenNullHardware_thenFailureResponseIsReturned() {
        final Hardware existingHardware = Hardware.builder()
            .hardwareName("existingName")
            .displayName("displayName")
            .hardwareMake(HardwareMake.AMD)
            .hardwareType(HardwareType.GPU)
            .multiplier(1.00D)
            .averagePpd(1L)
            .build();

        final ValidationResult<Hardware> response = HardwareValidator.validateUpdate(null, existingHardware, Collections.emptyList());

        assertThat(response.isFailure())
            .isTrue();

        assertThat(response.getErrors())
            .containsOnly("Payload is null");
    }

    @Test
    void whenValidatingUpdate_givenNullExistingHardware_thenFailureResponseIsReturned() {
        final HardwareRequest hardware = HardwareRequest.builder()
            .hardwareName(null)
            .displayName("displayName")
            .hardwareMake(HardwareMake.AMD.toString())
            .hardwareType(HardwareType.GPU.toString())
            .multiplier(1.00D)
            .averagePpd(1L)
            .build();

        final ValidationResult<Hardware> response = HardwareValidator.validateUpdate(hardware, null, Collections.emptyList());

        assertThat(response.isFailure())
            .isTrue();

        assertThat(response.getErrors())
            .containsOnly("Payload is null");
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

        final ValidationResult<Hardware> response = HardwareValidator.validateUpdate(hardware, existingHardware, Collections.emptyList());

        assertThat(response.isFailure())
            .isTrue();

        assertThat(response.getErrors())
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

        final Collection<Hardware> allHardware = List.of(Hardware.builder()
            .id(20)
            .hardwareName("hardwareName")
            .displayName("displayName")
            .hardwareMake(HardwareMake.AMD)
            .hardwareType(HardwareType.GPU)
            .multiplier(1.00D)
            .averagePpd(1L)
            .build()
        );

        final ValidationResult<Hardware> response = HardwareValidator.validateUpdate(hardware, existingHardware, allHardware);

        assertThat(response.isFailure())
            .isTrue();

        assertThat(response.getErrors())
            .containsOnly("Payload conflicts with an existing object on: [hardwareName]");
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

        final Collection<Hardware> allHardware = List.of(Hardware.builder()
            .id(1)
            .hardwareName("hardwareName")
            .displayName("displayName")
            .hardwareMake(HardwareMake.AMD)
            .hardwareType(HardwareType.GPU)
            .multiplier(1.00D)
            .averagePpd(1L)
            .build()
        );

        final ValidationResult<Hardware> response = HardwareValidator.validateUpdate(hardware, existingHardware, allHardware);

        assertThat(response.isFailure())
            .as("Expected validation to pass, instead failed with errors: " + response.getErrors())
            .isFalse();
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

        final ValidationResult<Hardware> response = HardwareValidator.validateUpdate(hardware, existingHardware, Collections.emptyList());

        assertThat(response.isFailure())
            .isTrue();

        assertThat(response.getErrors())
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

        final ValidationResult<Hardware> response = HardwareValidator.validateUpdate(hardware, existingHardware, Collections.emptyList());

        assertThat(response.isFailure())
            .isTrue();

        assertThat(response.getErrors())
            .containsOnly("Field 'hardwareMake' must be one of: " + HardwareMake.getAllValues());
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

        final ValidationResult<Hardware> response = HardwareValidator.validateUpdate(hardware, existingHardware, Collections.emptyList());

        assertThat(response.isFailure())
            .isTrue();

        assertThat(response.getErrors())
            .containsOnly("Field 'hardwareType' must be one of: " + HardwareType.getAllValues());
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

        final ValidationResult<Hardware> response = HardwareValidator.validateUpdate(hardware, existingHardware, Collections.emptyList());

        assertThat(response.isFailure())
            .isTrue();

        assertThat(response.getErrors())
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

        final ValidationResult<Hardware> response = HardwareValidator.validateUpdate(hardware, existingHardware, Collections.emptyList());

        assertThat(response.isFailure())
            .isTrue();

        assertThat(response.getErrors())
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

        final ValidationResult<Hardware> response = HardwareValidator.validateUpdate(hardware, existingHardware, Collections.emptyList());

        assertThat(response.isFailure())
            .isTrue();

        assertThat(response.getErrors())
            .as("Did not receive expected error messages")
            .hasSize(5)
            .containsOnly(
                "Field 'displayName' must not be empty",
                "Field 'hardwareMake' must be one of: " + HardwareMake.getAllValues(),
                "Field 'hardwareType' must be one of: " + HardwareType.getAllValues(),
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

        final ValidationResult<Hardware> response = HardwareValidator.validateDelete(existingHardware, Collections.emptyList());

        assertThat(response.isFailure())
            .as("Expected validation to pass, instead failed with errors: " + response.getErrors())
            .isFalse();
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

        final Collection<User> allUsers = List.of(User.builder()
            .hardware(existingHardware)
            .build()
        );

        final ValidationResult<Hardware> response = HardwareValidator.validateDelete(existingHardware, allUsers);

        assertThat(response.isFailure())
            .isTrue();
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

        final Collection<User> allUsers = List.of(User.builder()
            .hardware(userHardware)
            .build()
        );

        final ValidationResult<Hardware> response = HardwareValidator.validateDelete(existingHardware, allUsers);

        assertThat(response.isFailure())
            .as("Expected validation to pass, instead failed with errors: " + response.getErrors())
            .isFalse();
    }
}

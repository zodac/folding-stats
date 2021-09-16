package me.zodac.folding.rest.validator;

import static org.assertj.core.api.Assertions.assertThat;

import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.OperatingSystem;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.validator.ValidationResponse;
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
            .operatingSystem(OperatingSystem.WINDOWS.toString())
            .multiplier(1.0D)
            .build();

        final MockBusinessLogic mockBusinessLogic = MockBusinessLogic.create();
        final HardwareValidator hardwareValidator = HardwareValidator.create(mockBusinessLogic);
        final ValidationResponse<Hardware> response = hardwareValidator.validateCreate(hardware);

        assertThat(response.isInvalid())
            .as("Expected validation to pass, instead failed with errors: " + response.getErrors())
            .isFalse();
    }

    @Test
    void whenValidatingCreate_givenNullHardware_thenFailureResponseIsReturned() {
        final MockBusinessLogic mockBusinessLogic = MockBusinessLogic.create();
        final HardwareValidator hardwareValidator = HardwareValidator.create(mockBusinessLogic);
        final ValidationResponse<Hardware> response = hardwareValidator.validateCreate(null);

        assertThat(response.isInvalid())
            .isTrue();

        assertThat(response.getErrors())
            .contains("Payload is null");
    }

    @Test
    void whenValidatingCreate_givenHardwareWithNullName_thenFailureResponseIsReturned() {
        final HardwareRequest hardware = HardwareRequest.builder()
            .hardwareName(null)
            .displayName("displayName")
            .operatingSystem(OperatingSystem.WINDOWS.toString())
            .multiplier(1.0D)
            .build();

        final MockBusinessLogic mockBusinessLogic = MockBusinessLogic.create();
        final HardwareValidator hardwareValidator = HardwareValidator.create(mockBusinessLogic);
        final ValidationResponse<Hardware> response = hardwareValidator.validateCreate(hardware);

        assertThat(response.isInvalid())
            .isTrue();

        assertThat(response.getErrors())
            .contains("Field 'hardwareName' must not be empty");
    }

    @Test
    void whenValidatingCreate_givenHardwareWithNameAlreadyExists_thenFailureResponseIsReturned() {
        final MockBusinessLogic mockBusinessLogic = MockBusinessLogic.create();
        mockBusinessLogic.createHardware(Hardware.builder()
            .hardwareName("existingName")
            .displayName("displayName")
            .operatingSystem(OperatingSystem.WINDOWS)
            .multiplier(1.0D)
            .build()
        );

        final HardwareValidator hardwareValidator = HardwareValidator.create(mockBusinessLogic);
        final HardwareRequest hardware = HardwareRequest.builder()
            .hardwareName("existingName")
            .displayName("displayName")
            .operatingSystem(OperatingSystem.WINDOWS.toString())
            .multiplier(1.0D)
            .build();

        final ValidationResponse<Hardware> response = hardwareValidator.validateCreate(hardware);

        assertThat(response.isInvalid())
            .isTrue();

        assertThat(response.getErrors())
            .contains("Payload conflicts with an existing object on: [hardwareName]");
    }

    @Test
    void whenValidatingCreate_givenHardwareWithNullDisplayName_thenFailureResponseIsReturned() {
        final HardwareRequest hardware = HardwareRequest.builder()
            .hardwareName("hardwareName")
            .displayName(null)
            .operatingSystem(OperatingSystem.WINDOWS.toString())
            .multiplier(1.0D)
            .build();

        final MockBusinessLogic mockBusinessLogic = MockBusinessLogic.create();
        final HardwareValidator hardwareValidator = HardwareValidator.create(mockBusinessLogic);
        final ValidationResponse<Hardware> response = hardwareValidator.validateCreate(hardware);

        assertThat(response.isInvalid())
            .isTrue();

        assertThat(response.getErrors())
            .contains("Field 'displayName' must not be empty");
    }

    @Test
    void whenValidatingCreate_givenHardwareWithInvalidCategory_thenFailureResponseIsReturned() {
        final HardwareRequest hardware = HardwareRequest.builder()
            .hardwareName("hardwareName")
            .displayName("displayName")
            .operatingSystem(OperatingSystem.INVALID.toString())
            .multiplier(1.0D)
            .build();

        final MockBusinessLogic mockBusinessLogic = MockBusinessLogic.create();
        final HardwareValidator hardwareValidator = HardwareValidator.create(mockBusinessLogic);
        final ValidationResponse<Hardware> response = hardwareValidator.validateCreate(hardware);

        assertThat(response.isInvalid())
            .isTrue();

        assertThat(response.getErrors())
            .contains("Field 'operatingSystem' must be one of: " + OperatingSystem.getAllValues().toString());
    }

    @Test
    void whenValidatingCreate_givenHardwareWithNegativeMultiplier_thenFailureResponseIsReturned() {
        final HardwareRequest hardware = HardwareRequest.builder()
            .hardwareName("hardwareName")
            .displayName("displayName")
            .operatingSystem(OperatingSystem.WINDOWS.toString())
            .multiplier(-1.0D)
            .build();

        final MockBusinessLogic mockBusinessLogic = MockBusinessLogic.create();
        final HardwareValidator hardwareValidator = HardwareValidator.create(mockBusinessLogic);
        final ValidationResponse<Hardware> response = hardwareValidator.validateCreate(hardware);

        assertThat(response.isInvalid())
            .isTrue();

        assertThat(response.getErrors())
            .contains("Field 'multiplier' must be over 0.00");
    }

    @Test
    void whenValidatingUpdate_givenValidHardware_thenSuccessResponseIsReturned() {
        final HardwareRequest hardware = HardwareRequest.builder()
            .hardwareName("hardwareName")
            .displayName("displayName")
            .operatingSystem(OperatingSystem.WINDOWS.toString())
            .multiplier(2.0D)
            .build();

        final Hardware existingHardware = Hardware.builder()
            .hardwareName("hardwareName")
            .displayName("displayName")
            .operatingSystem(OperatingSystem.WINDOWS)
            .multiplier(1.0D)
            .build();

        final MockBusinessLogic mockBusinessLogic = MockBusinessLogic.create();
        final HardwareValidator hardwareValidator = HardwareValidator.create(mockBusinessLogic);
        final ValidationResponse<Hardware> response = hardwareValidator.validateUpdate(hardware, existingHardware);

        assertThat(response.isInvalid())
            .as("Expected validation to pass, instead failed with errors: " + response.getErrors())
            .isFalse();
    }

    @Test
    void whenValidatingUpdate_givenNullHardware_thenFailureResponseIsReturned() {
        final Hardware existingHardware = Hardware.builder()
            .hardwareName("existingName")
            .displayName("displayName")
            .operatingSystem(OperatingSystem.WINDOWS)
            .multiplier(1.0D)
            .build();

        final MockBusinessLogic mockBusinessLogic = MockBusinessLogic.create();
        final HardwareValidator hardwareValidator = HardwareValidator.create(mockBusinessLogic);
        final ValidationResponse<Hardware> response = hardwareValidator.validateUpdate(null, existingHardware);

        assertThat(response.isInvalid())
            .isTrue();

        assertThat(response.getErrors())
            .contains("Payload is null");
    }

    @Test
    void whenValidatingUpdate_givenNullExistingHardware_thenFailureResponseIsReturned() {
        final HardwareRequest hardware = HardwareRequest.builder()
            .hardwareName(null)
            .displayName("displayName")
            .operatingSystem(OperatingSystem.WINDOWS.toString())
            .multiplier(1.0D)
            .build();

        final MockBusinessLogic mockBusinessLogic = MockBusinessLogic.create();
        final HardwareValidator hardwareValidator = HardwareValidator.create(mockBusinessLogic);
        final ValidationResponse<Hardware> response = hardwareValidator.validateUpdate(hardware, null);

        assertThat(response.isInvalid())
            .isTrue();

        assertThat(response.getErrors())
            .contains("Payload is null");
    }

    @Test
    void whenValidatingUpdate_givenHardwareWithNullName_thenFailureResponseIsReturned() {
        final HardwareRequest hardware = HardwareRequest.builder()
            .hardwareName(null)
            .displayName("displayName")
            .operatingSystem(OperatingSystem.WINDOWS.toString())
            .multiplier(1.0D)
            .build();

        final Hardware existingHardware = Hardware.builder()
            .hardwareName("hardwareName")
            .displayName("displayName")
            .operatingSystem(OperatingSystem.WINDOWS)
            .multiplier(1.0D)
            .build();

        final MockBusinessLogic mockBusinessLogic = MockBusinessLogic.create();
        final HardwareValidator hardwareValidator = HardwareValidator.create(mockBusinessLogic);
        final ValidationResponse<Hardware> response = hardwareValidator.validateUpdate(hardware, existingHardware);

        assertThat(response.isInvalid())
            .isTrue();

        assertThat(response.getErrors())
            .contains("Field 'hardwareName' must not be empty");
    }

    @Test
    void whenValidatingUpdate_givenHardwareWithNameNotMatchingExistingHardware_thenFailureResponseIsReturned() {
        final HardwareRequest hardware = HardwareRequest.builder()
            .hardwareName("hardwareName")
            .displayName("displayName")
            .operatingSystem(OperatingSystem.WINDOWS.toString())
            .multiplier(1.0D)
            .build();

        final Hardware existingHardware = Hardware.builder()
            .hardwareName("differentName")
            .displayName("displayName")
            .operatingSystem(OperatingSystem.WINDOWS)
            .multiplier(1.0D)
            .build();

        final MockBusinessLogic mockBusinessLogic = MockBusinessLogic.create();
        final HardwareValidator hardwareValidator = HardwareValidator.create(mockBusinessLogic);
        final ValidationResponse<Hardware> response = hardwareValidator.validateUpdate(hardware, existingHardware);

        assertThat(response.isInvalid())
            .isTrue();

        assertThat(response.getErrors())
            .contains("Field 'hardwareName' does not match existing hardware name 'differentName'");
    }

    @Test
    void whenValidatingUpdate_givenHardwareWithNullDisplayName_thenFailureResponseIsReturned() {
        final HardwareRequest hardware = HardwareRequest.builder()
            .hardwareName("hardwareName")
            .displayName(null)
            .operatingSystem(OperatingSystem.WINDOWS.toString())
            .multiplier(1.0D)
            .build();

        final Hardware existingHardware = Hardware.builder()
            .hardwareName("hardwareName")
            .displayName("displayName")
            .operatingSystem(OperatingSystem.WINDOWS)
            .multiplier(1.0D)
            .build();

        final MockBusinessLogic mockBusinessLogic = MockBusinessLogic.create();
        final HardwareValidator hardwareValidator = HardwareValidator.create(mockBusinessLogic);
        final ValidationResponse<Hardware> response = hardwareValidator.validateUpdate(hardware, existingHardware);

        assertThat(response.isInvalid())
            .isTrue();

        assertThat(response.getErrors())
            .contains("Field 'displayName' must not be empty");
    }

    @Test
    void whenValidatingUpdate_givenHardwareWithInvalidCategory_thenFailureResponseIsReturned() {
        final HardwareRequest hardware = HardwareRequest.builder()
            .hardwareName("hardwareName")
            .displayName("displayName")
            .operatingSystem(OperatingSystem.INVALID.toString())
            .multiplier(1.0D)
            .build();

        final Hardware existingHardware = Hardware.builder()
            .hardwareName("hardwareName")
            .displayName("displayName")
            .operatingSystem(OperatingSystem.WINDOWS)
            .multiplier(1.0D)
            .build();

        final MockBusinessLogic mockBusinessLogic = MockBusinessLogic.create();
        final HardwareValidator hardwareValidator = HardwareValidator.create(mockBusinessLogic);
        final ValidationResponse<Hardware> response = hardwareValidator.validateUpdate(hardware, existingHardware);

        assertThat(response.isInvalid())
            .isTrue();

        assertThat(response.getErrors())
            .contains("Field 'operatingSystem' must be one of: " + OperatingSystem.getAllValues().toString());
    }

    @Test
    void whenValidatingUpdate_givenHardwareWithNegativeMultiplier_thenFailureResponseIsReturned() {
        final HardwareRequest hardware = HardwareRequest.builder()
            .hardwareName("hardwareName")
            .displayName("displayName")
            .operatingSystem(OperatingSystem.WINDOWS.toString())
            .multiplier(-1.0D)
            .build();

        final Hardware existingHardware = Hardware.builder()
            .hardwareName("hardwareName")
            .displayName("displayName")
            .operatingSystem(OperatingSystem.WINDOWS)
            .multiplier(1.0D)
            .build();

        final MockBusinessLogic mockBusinessLogic = MockBusinessLogic.create();
        final HardwareValidator hardwareValidator = HardwareValidator.create(mockBusinessLogic);
        final ValidationResponse<Hardware> response = hardwareValidator.validateUpdate(hardware, existingHardware);

        assertThat(response.isInvalid())
            .isTrue();

        assertThat(response.getErrors())
            .contains("Field 'multiplier' must be over 0.00");
    }

    @Test
    void whenValidatingDelete_givenHardwareThatIsNotBeingUsed_thenSuccessResponseIsReturned() {
        final Hardware existingHardware = Hardware.builder()
            .hardwareName("hardwareName")
            .displayName("displayName")
            .operatingSystem(OperatingSystem.WINDOWS)
            .multiplier(1.0D)
            .build();

        final MockBusinessLogic mockBusinessLogic = MockBusinessLogic.create();
        final HardwareValidator hardwareValidator = HardwareValidator.create(mockBusinessLogic);
        final ValidationResponse<Hardware> response = hardwareValidator.validateDelete(existingHardware);

        assertThat(response.isInvalid())
            .as("Expected validation to pass, instead failed with errors: " + response.getErrors())
            .isFalse();
    }

    @Test
    void whenValidatingDelete_givenHardwareThatIsBeingUsedByUser_thenFailureResponseIsReturned() {
        final Hardware existingHardware = Hardware.builder()
            .id(1)
            .hardwareName("hardwareName")
            .displayName("displayName")
            .operatingSystem(OperatingSystem.WINDOWS)
            .multiplier(1.0D)
            .build();

        final User userUsingHardware = User.builder()
            .hardware(existingHardware)
            .build();

        final MockBusinessLogic mockBusinessLogic = MockBusinessLogic.create();
        mockBusinessLogic.createUser(userUsingHardware);
        final HardwareValidator hardwareValidator = HardwareValidator.create(mockBusinessLogic);
        final ValidationResponse<Hardware> response = hardwareValidator.validateDelete(existingHardware);

        assertThat(response.isInvalid())
            .isTrue();
    }
}

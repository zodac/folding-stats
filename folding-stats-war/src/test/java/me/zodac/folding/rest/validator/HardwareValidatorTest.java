package me.zodac.folding.rest.validator;

import static org.assertj.core.api.Assertions.assertThat;

import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.HardwareMake;
import me.zodac.folding.api.tc.HardwareType;
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
            .hardwareMake(HardwareMake.AMD.toString())
            .hardwareType(HardwareType.GPU.toString())
            .multiplier(1.00D)
            .averagePpd(1L)
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
            .hardwareMake(HardwareMake.AMD.toString())
            .hardwareType(HardwareType.GPU.toString())
            .multiplier(1.00D)
            .averagePpd(1L)
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
            .hardwareMake(HardwareMake.AMD)
            .hardwareType(HardwareType.GPU)
            .multiplier(1.00D)
            .averagePpd(1L)
            .build()
        );

        final HardwareValidator hardwareValidator = HardwareValidator.create(mockBusinessLogic);
        final HardwareRequest hardware = HardwareRequest.builder()
            .hardwareName("existingName")
            .displayName("displayName")
            .hardwareMake(HardwareMake.AMD.toString())
            .hardwareType(HardwareType.GPU.toString())
            .multiplier(1.00D)
            .averagePpd(1L)
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
            .hardwareMake(HardwareMake.AMD.toString())
            .hardwareType(HardwareType.GPU.toString())
            .multiplier(1.00D)
            .averagePpd(1L)
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
    void whenValidatingCreate_givenHardwareWithInvalidMake_thenFailureResponseIsReturned() {
        final HardwareRequest hardware = HardwareRequest.builder()
            .hardwareName("hardwareName")
            .displayName("displayName")
            .hardwareMake(HardwareMake.INVALID.toString())
            .hardwareType(HardwareType.GPU.toString())
            .multiplier(1.00D)
            .averagePpd(1L)
            .build();

        final MockBusinessLogic mockBusinessLogic = MockBusinessLogic.create();
        final HardwareValidator hardwareValidator = HardwareValidator.create(mockBusinessLogic);
        final ValidationResponse<Hardware> response = hardwareValidator.validateCreate(hardware);

        assertThat(response.isInvalid())
            .isTrue();

        assertThat(response.getErrors())
            .contains("Field 'hardwareMake' must be one of: " + HardwareMake.getAllValues());
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

        final MockBusinessLogic mockBusinessLogic = MockBusinessLogic.create();
        final HardwareValidator hardwareValidator = HardwareValidator.create(mockBusinessLogic);
        final ValidationResponse<Hardware> response = hardwareValidator.validateCreate(hardware);

        assertThat(response.isInvalid())
            .isTrue();

        assertThat(response.getErrors())
            .contains("Field 'hardwareType' must be one of: " + HardwareType.getAllValues());
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

        final MockBusinessLogic mockBusinessLogic = MockBusinessLogic.create();
        final HardwareValidator hardwareValidator = HardwareValidator.create(mockBusinessLogic);
        final ValidationResponse<Hardware> response = hardwareValidator.validateCreate(hardware);

        assertThat(response.isInvalid())
            .isTrue();

        assertThat(response.getErrors())
            .contains("Field 'multiplier' must be 1.00 or higher");
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

        final MockBusinessLogic mockBusinessLogic = MockBusinessLogic.create();
        final HardwareValidator hardwareValidator = HardwareValidator.create(mockBusinessLogic);
        final ValidationResponse<Hardware> response = hardwareValidator.validateCreate(hardware);

        assertThat(response.isInvalid())
            .isTrue();

        assertThat(response.getErrors())
            .contains("Field 'averagePpd' must be 1 or higher");
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

        final MockBusinessLogic mockBusinessLogic = MockBusinessLogic.create();
        final HardwareValidator hardwareValidator = HardwareValidator.create(mockBusinessLogic);
        final ValidationResponse<Hardware> response = hardwareValidator.validateCreate(hardware);

        assertThat(response.isInvalid())
            .isTrue();

        assertThat(response.getErrors())
            .as("Did not receive expected error messages")
            .hasSize(6)
            .contains(
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
            .hardwareMake(HardwareMake.AMD)
            .hardwareType(HardwareType.GPU)
            .multiplier(1.00D)
            .averagePpd(1L)
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
            .hardwareMake(HardwareMake.AMD.toString())
            .hardwareType(HardwareType.GPU.toString())
            .multiplier(1.00D)
            .averagePpd(1L)
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

        final MockBusinessLogic mockBusinessLogic = MockBusinessLogic.create();
        final HardwareValidator hardwareValidator = HardwareValidator.create(mockBusinessLogic);
        final ValidationResponse<Hardware> response = hardwareValidator.validateUpdate(hardware, existingHardware);

        assertThat(response.isInvalid())
            .isTrue();

        assertThat(response.getErrors())
            .contains("Field 'hardwareName' must not be empty");
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

        final MockBusinessLogic mockBusinessLogic = MockBusinessLogic.create();
        mockBusinessLogic.createHardware(Hardware.builder()
            .id(20)
            .hardwareName("hardwareName")
            .displayName("displayName")
            .hardwareMake(HardwareMake.AMD)
            .hardwareType(HardwareType.GPU)
            .multiplier(1.00D)
            .averagePpd(1L)
            .build()
        );

        final HardwareValidator hardwareValidator = HardwareValidator.create(mockBusinessLogic);
        final ValidationResponse<Hardware> response = hardwareValidator.validateUpdate(hardware, existingHardware);

        assertThat(response.isInvalid())
            .isTrue();

        assertThat(response.getErrors())
            .contains("Payload conflicts with an existing object on: [hardwareName]");
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

        final MockBusinessLogic mockBusinessLogic = MockBusinessLogic.create();
        mockBusinessLogic.createHardware(Hardware.builder()
            .id(1)
            .hardwareName("hardwareName")
            .displayName("displayName")
            .hardwareMake(HardwareMake.AMD)
            .hardwareType(HardwareType.GPU)
            .multiplier(1.00D)
            .averagePpd(1L)
            .build()
        );

        final HardwareValidator hardwareValidator = HardwareValidator.create(mockBusinessLogic);
        final ValidationResponse<Hardware> response = hardwareValidator.validateUpdate(hardware, existingHardware);

        assertThat(response.isInvalid())
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

        final MockBusinessLogic mockBusinessLogic = MockBusinessLogic.create();
        final HardwareValidator hardwareValidator = HardwareValidator.create(mockBusinessLogic);
        final ValidationResponse<Hardware> response = hardwareValidator.validateUpdate(hardware, existingHardware);

        assertThat(response.isInvalid())
            .isTrue();

        assertThat(response.getErrors())
            .contains("Field 'displayName' must not be empty");
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

        final MockBusinessLogic mockBusinessLogic = MockBusinessLogic.create();
        final HardwareValidator hardwareValidator = HardwareValidator.create(mockBusinessLogic);
        final ValidationResponse<Hardware> response = hardwareValidator.validateUpdate(hardware, existingHardware);

        assertThat(response.isInvalid())
            .isTrue();

        assertThat(response.getErrors())
            .contains("Field 'hardwareMake' must be one of: " + HardwareMake.getAllValues());
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

        final MockBusinessLogic mockBusinessLogic = MockBusinessLogic.create();
        final HardwareValidator hardwareValidator = HardwareValidator.create(mockBusinessLogic);
        final ValidationResponse<Hardware> response = hardwareValidator.validateUpdate(hardware, existingHardware);

        assertThat(response.isInvalid())
            .isTrue();

        assertThat(response.getErrors())
            .contains("Field 'hardwareType' must be one of: " + HardwareType.getAllValues());
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

        final MockBusinessLogic mockBusinessLogic = MockBusinessLogic.create();
        final HardwareValidator hardwareValidator = HardwareValidator.create(mockBusinessLogic);
        final ValidationResponse<Hardware> response = hardwareValidator.validateUpdate(hardware, existingHardware);

        assertThat(response.isInvalid())
            .isTrue();

        assertThat(response.getErrors())
            .contains("Field 'multiplier' must be 1.00 or higher");
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

        final MockBusinessLogic mockBusinessLogic = MockBusinessLogic.create();
        final HardwareValidator hardwareValidator = HardwareValidator.create(mockBusinessLogic);
        final ValidationResponse<Hardware> response = hardwareValidator.validateUpdate(hardware, existingHardware);

        assertThat(response.isInvalid())
            .isTrue();

        assertThat(response.getErrors())
            .contains("Field 'averagePpd' must be 1 or higher");
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

        final MockBusinessLogic mockBusinessLogic = MockBusinessLogic.create();
        final HardwareValidator hardwareValidator = HardwareValidator.create(mockBusinessLogic);
        final ValidationResponse<Hardware> response = hardwareValidator.validateUpdate(hardware, existingHardware);

        assertThat(response.isInvalid())
            .isTrue();

        assertThat(response.getErrors())
            .as("Did not receive expected error messages")
            .hasSize(5)
            .contains(
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
            .hardwareMake(HardwareMake.AMD)
            .hardwareType(HardwareType.GPU)
            .multiplier(1.00D)
            .averagePpd(1L)
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

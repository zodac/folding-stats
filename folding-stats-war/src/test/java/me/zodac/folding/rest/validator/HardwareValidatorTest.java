package me.zodac.folding.rest.validator;

import static org.assertj.core.api.Assertions.assertThat;

import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.OperatingSystem;
import me.zodac.folding.api.validator.ValidationResponse;
import me.zodac.folding.rest.api.tc.request.HardwareRequest;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link HardwareValidator}.
 */
class HardwareValidatorTest {

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
}

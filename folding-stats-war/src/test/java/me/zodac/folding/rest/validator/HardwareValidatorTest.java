package me.zodac.folding.rest.validator;

import static org.assertj.core.api.Assertions.assertThat;

import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.validator.ValidationResponse;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link HardwareValidator}.
 */
class HardwareValidatorTest {

    private static final HardwareValidator HARDWARE_VALIDATOR = HardwareValidator.create(null);

    @Test
    void whenValidatingCreate_givenNullHardware_thenFailureResponseIsReturned() {
        final ValidationResponse<Hardware> response = HARDWARE_VALIDATOR.validateCreate(null);

        assertThat(response.isInvalid())
            .isTrue();

        assertThat(response.getErrors())
            .contains("Payload is null");
    }
}

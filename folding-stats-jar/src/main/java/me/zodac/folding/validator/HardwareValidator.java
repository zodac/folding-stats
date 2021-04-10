package me.zodac.folding.validator;

import me.zodac.folding.api.Hardware;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class HardwareValidator {

    private HardwareValidator() {

    }

    public static ValidationResponse isValid(final Hardware hardware) {
        final List<String> failureMessages = new ArrayList<>();

        if (StringUtils.isBlank(hardware.getHardwareName())) {
            failureMessages.add("Attribute 'hardwareName' must not be empty");
        }

        if (StringUtils.isBlank(hardware.getDisplayName())) {
            failureMessages.add("Attribute 'displayName' must not be empty");
        }

        // I am assuming multiplier cannot be less than 0, also assuming we might want a 0.1/0.5 at some point with future hardware
        if (hardware.getMultiplier() <= 0.0D) {
            failureMessages.add("Attribute 'multiplier' must be over 0.0");
        }

        if (failureMessages.isEmpty()) {
            return ValidationResponse.success();
        }

        return ValidationResponse.failure(hardware, failureMessages);
    }
}

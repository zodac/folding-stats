package me.zodac.folding.validator;

import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.OperatingSystem;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

// TODO: [zodac] Move validators to WAR/EJB module, use StorageFacade instead of caches, then add unit tests
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

        if (OperatingSystem.INVALID == OperatingSystem.get(hardware.getOperatingSystem())) {
            failureMessages.add(String.format("Attribute 'operatingSystem' must be one of: %s", OperatingSystem.getAllValues()));
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

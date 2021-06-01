package me.zodac.folding.rest.util.validator;

import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.OperatingSystem;
import me.zodac.folding.api.validator.ValidationResponse;
import me.zodac.folding.rest.api.tc.request.HardwareRequest;

import java.util.ArrayList;
import java.util.List;

public class HardwareValidator {


    public static HardwareValidator create() {
        return new HardwareValidator();
    }

    public ValidationResponse<Hardware> isValid(final HardwareRequest hardwareRequest) {
        if (hardwareRequest == null) {
            return ValidationResponse.nullObject();
        }

        final List<String> failureMessages = new ArrayList<>();

        if (isBlank(hardwareRequest.getHardwareName())) {
            failureMessages.add("Attribute 'hardwareName' must not be empty");
        }

        if (isBlank(hardwareRequest.getDisplayName())) {
            failureMessages.add("Attribute 'displayName' must not be empty");
        }

        final OperatingSystem operatingSystem = OperatingSystem.get(hardwareRequest.getOperatingSystem());
        if (OperatingSystem.INVALID == operatingSystem) {
            failureMessages.add(String.format("Attribute 'operatingSystem' must be one of: %s", OperatingSystem.getAllValues()));
        }

        // I am assuming multiplier cannot be less than 0, also assuming we might want a 0.1/0.5 at some point with future hardware
        if (hardwareRequest.getMultiplier() <= 0.0D) {
            failureMessages.add("Attribute 'multiplier' must be over 0.0");
        }

        if (failureMessages.isEmpty()) {
            final Hardware convertedHardware = Hardware.createWithoutId(hardwareRequest.getHardwareName(), hardwareRequest.getDisplayName(), operatingSystem, hardwareRequest.getMultiplier());
            return ValidationResponse.success(convertedHardware);
        }

        return ValidationResponse.failure(hardwareRequest, failureMessages);
    }

    private boolean isBlank(final String input) {
        return input == null || input.isBlank();
    }
}

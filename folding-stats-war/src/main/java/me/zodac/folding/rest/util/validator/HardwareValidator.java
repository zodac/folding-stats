package me.zodac.folding.rest.util.validator;

import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.OperatingSystem;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.validator.ValidationResponse;
import me.zodac.folding.ejb.OldFacade;
import me.zodac.folding.rest.api.tc.request.HardwareRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


// TODO: [zodac] Validate constraints:
//   - The 'hardwareName' is unique (for CREATE/UPDATE)
//   - Hardware is not in use by user (for DELETE, doesn't use validator flow...)
//   - Also for other validators
public final class HardwareValidator {

    // Assuming multiplier cannot be less than 0, also assuming we might want a 0.1/0.5 at some point with future hardware
    private static final double INVALID_MULTIPLIER_VALUE = 0.0D;

    private transient final OldFacade oldFacade;

    private HardwareValidator(final OldFacade oldFacade) {
        this.oldFacade = oldFacade;
    }

    public static HardwareValidator create(final OldFacade oldFacade) {
        return new HardwareValidator(oldFacade);
    }

    public ValidationResponse<Hardware> validateCreate(final HardwareRequest hardwareRequest) {
        if (hardwareRequest == null) {
            return ValidationResponse.nullObject();
        }

        final List<String> failureMessages = new ArrayList<>();

        if (isBlank(hardwareRequest.getHardwareName())) {
            failureMessages.add("Field 'hardwareName' must not be empty");
        } else {
            final Optional<Hardware> hardwareWithMatchingName = oldFacade.getHardwareWithName(hardwareRequest.getHardwareName());

            if (hardwareWithMatchingName.isPresent()) {
                return ValidationResponse.conflictingWith(hardwareRequest, hardwareWithMatchingName.get(), List.of("hardwareName"));
            }
        }

        if (isBlank(hardwareRequest.getDisplayName())) {
            failureMessages.add("Field 'displayName' must not be empty");
        }

        final OperatingSystem operatingSystem = OperatingSystem.get(hardwareRequest.getOperatingSystem());
        if (OperatingSystem.INVALID == operatingSystem) {
            failureMessages.add(String.format("Field 'operatingSystem' must be one of: %s", OperatingSystem.getAllValues()));
        }

        if (hardwareRequest.getMultiplier() <= INVALID_MULTIPLIER_VALUE) {
            failureMessages.add("Field 'multiplier' must be over 0.0");
        }

        if (failureMessages.isEmpty()) {
            final Hardware convertedHardware = Hardware.createWithoutId(hardwareRequest.getHardwareName(), hardwareRequest.getDisplayName(), operatingSystem, hardwareRequest.getMultiplier());
            return ValidationResponse.success(convertedHardware);
        }

        return ValidationResponse.failure(hardwareRequest, failureMessages);
    }

    public ValidationResponse<Hardware> validateUpdate(final HardwareRequest hardwareRequest) {
        if (hardwareRequest == null) {
            return ValidationResponse.nullObject();
        }

        final List<String> failureMessages = new ArrayList<>();

        if (isBlank(hardwareRequest.getHardwareName())) {
            failureMessages.add("Field 'hardwareName' must not be empty");
        }

        if (isBlank(hardwareRequest.getDisplayName())) {
            failureMessages.add("Field 'displayName' must not be empty");
        }

        final OperatingSystem operatingSystem = OperatingSystem.get(hardwareRequest.getOperatingSystem());
        if (OperatingSystem.INVALID == operatingSystem) {
            failureMessages.add(String.format("Field 'operatingSystem' must be one of: %s", OperatingSystem.getAllValues()));
        }

        if (hardwareRequest.getMultiplier() <= INVALID_MULTIPLIER_VALUE) {
            failureMessages.add("Field 'multiplier' must be over 0.0");
        }

        if (failureMessages.isEmpty()) {
            final Hardware convertedHardware = Hardware.createWithoutId(hardwareRequest.getHardwareName(), hardwareRequest.getDisplayName(), operatingSystem, hardwareRequest.getMultiplier());
            return ValidationResponse.success(convertedHardware);
        }

        return ValidationResponse.failure(hardwareRequest, failureMessages);
    }

    public ValidationResponse<Hardware> validateDelete(final Hardware hardware) {
        final Optional<User> userWithMatchingHardware = oldFacade.getUserWithHardware(hardware);

        if (userWithMatchingHardware.isPresent()) {
            return ValidationResponse.usedBy(hardware, userWithMatchingHardware.get());
        }

        return ValidationResponse.success(hardware);
    }

    private boolean isBlank(final String input) {
        return input == null || input.isBlank();
    }
}

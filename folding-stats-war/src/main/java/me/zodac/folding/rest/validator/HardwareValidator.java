package me.zodac.folding.rest.validator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import me.zodac.folding.api.ejb.BusinessLogic;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.OperatingSystem;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.validator.ValidationResponse;
import me.zodac.folding.rest.api.tc.request.HardwareRequest;
import org.apache.commons.lang3.StringUtils;

/**
 * Validator class to validate a {@link Hardware} or {@link HardwareRequest}.
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class HardwareValidator {

    // Assuming multiplier cannot be less than 0, also assuming we might want a 0.1/0.5 at some point with future hardware
    private static final double INVALID_MULTIPLIER_VALUE = 0.0D;

    private final transient BusinessLogic businessLogic;

    /**
     * Creates a {@link HardwareValidator}.
     *
     * @param businessLogic the {@link BusinessLogic} used for retrieval of {@link User}s for conflict checks
     * @return the created {@link HardwareValidator}
     */
    public static HardwareValidator createValidator(final BusinessLogic businessLogic) {
        return new HardwareValidator(businessLogic);
    }

    /**
     * Validates a {@link HardwareRequest} for a {@link Hardware} to be created on the system.
     *
     * <p>
     * Validation checks include:
     * <ul>
     *     <li>Field 'hardwareName' must not be empty</li>
     *     <li>If field 'hardwareName' is not empty, it must not be used by another {@link Hardware}</li>
     *     <li>Field 'displayName' must not be empty</li>
     *     <li>Field 'operatingSystem' must be a valid {@link OperatingSystem}</li>
     *     <li>Field 'multiplier' must not be over <b>0.0</b></li>
     * </ul>
     *
     * @param hardwareRequest the {@link HardwareRequest} to validate
     * @return the {@link ValidationResponse}
     */
    public ValidationResponse<Hardware> validateCreate(final HardwareRequest hardwareRequest) {
        if (hardwareRequest == null) {
            return ValidationResponse.nullObject();
        }

        final List<String> failureMessages = new ArrayList<>(4);

        if (StringUtils.isBlank(hardwareRequest.getHardwareName())) {
            failureMessages.add("Field 'hardwareName' must not be empty");
        } else {
            final Optional<Hardware> hardwareWithMatchingName = businessLogic.getHardwareWithName(hardwareRequest.getHardwareName());

            if (hardwareWithMatchingName.isPresent()) {
                return ValidationResponse.conflictingWith(hardwareRequest, hardwareWithMatchingName.get(), List.of("hardwareName"));
            }
        }

        if (StringUtils.isBlank(hardwareRequest.getDisplayName())) {
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
            final Hardware convertedHardware = Hardware
                .createWithoutId(hardwareRequest.getHardwareName(), hardwareRequest.getDisplayName(), operatingSystem,
                    hardwareRequest.getMultiplier());
            return ValidationResponse.success(convertedHardware);
        }

        return ValidationResponse.failure(hardwareRequest, failureMessages);
    }

    /**
     * Validates a {@link HardwareRequest} to update an existing {@link Hardware} on the system.
     *
     * <p>
     * Validation checks include:
     * <ul>
     *     <li>Field 'hardwareName' must not be empty</li>
     *     <li>If field 'hardwareName' is not empty, it must not be used by another {@link Hardware}</li>
     *     <li>Field 'displayName' must not be empty</li>
     *     <li>Field 'operatingSystem' must be a valid {@link OperatingSystem}</li>
     *     <li>Field 'multiplier' must not be over <b>0.0</b></li>
     * </ul>
     *
     * @param hardwareRequest the {@link HardwareRequest} to validate
     * @return the {@link ValidationResponse}
     */
    public ValidationResponse<Hardware> validateUpdate(final HardwareRequest hardwareRequest) {
        if (hardwareRequest == null) {
            return ValidationResponse.nullObject();
        }

        final List<String> failureMessages = new ArrayList<>(4);

        if (StringUtils.isBlank(hardwareRequest.getHardwareName())) {
            failureMessages.add("Field 'hardwareName' must not be empty");
        }

        if (StringUtils.isBlank(hardwareRequest.getDisplayName())) {
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
            final Hardware convertedHardware = Hardware
                .createWithoutId(hardwareRequest.getHardwareName(), hardwareRequest.getDisplayName(), operatingSystem,
                    hardwareRequest.getMultiplier());
            return ValidationResponse.success(convertedHardware);
        }

        return ValidationResponse.failure(hardwareRequest, failureMessages);
    }

    /**
     * Validates a {@link Hardware} to be deleted from the system.
     *
     * @param hardware the {@link Hardware} to validate
     * @return the {@link ValidationResponse}
     */
    public ValidationResponse<Hardware> validateDelete(final Hardware hardware) {
        final Collection<User> usersWithMatchingHardware = businessLogic.getUsersWithHardware(hardware);

        if (usersWithMatchingHardware.isEmpty()) {
            return ValidationResponse.success(hardware);
        }

        return ValidationResponse.usedBy(hardware, usersWithMatchingHardware);
    }
}

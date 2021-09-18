package me.zodac.folding.rest.validator;

import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
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

    // Assuming the 'best' hardware will have a multiplier of 1.00, and all others will be based on that
    private static final double MINIMUM_MULTIPLIER_VALUE = 1.00D;

    private final transient BusinessLogic businessLogic;

    /**
     * Creates a {@link HardwareValidator}.
     *
     * @param businessLogic the {@link BusinessLogic} used for retrieval of {@link User}s for conflict checks
     * @return the created {@link HardwareValidator}
     */
    public static HardwareValidator create(final BusinessLogic businessLogic) {
        return new HardwareValidator(businessLogic);
    }

    private static String hardwareName(final HardwareRequest hardwareRequest) {
        return StringUtils.isNotBlank(hardwareRequest.getHardwareName()) ? null : "Field 'hardwareName' must not be empty";
    }

    private static String displayName(final HardwareRequest hardwareRequest) {
        return StringUtils.isNotBlank(hardwareRequest.getDisplayName()) ? null : "Field 'displayName' must not be empty";
    }

    private static String operatingSystem(final HardwareRequest hardwareRequest) {
        final OperatingSystem operatingSystem = OperatingSystem.get(hardwareRequest.getOperatingSystem());
        return operatingSystem == OperatingSystem.INVALID
            ? String.format("Field 'operatingSystem' must be one of: %s", OperatingSystem.getAllValues()) : null;
    }

    private static String multiplier(final HardwareRequest hardwareRequest) {
        return hardwareRequest.getMultiplier() >= MINIMUM_MULTIPLIER_VALUE ? null :
            String.format("Field 'multiplier' must be %.2f or higher", MINIMUM_MULTIPLIER_VALUE);
    }

    /**
     * Validates a {@link HardwareRequest} for a {@link Hardware} to be created on the system.
     *
     * <p>
     * Validation checks include:
     * <ul>
     *     <li>Input {@code hardwareRequest} must not be <b>null</b></li>
     *     <li>Field 'hardwareName' must not be empty</li>
     *     <li>If field 'hardwareName' is not empty, it must not be used by another {@link Hardware}</li>
     *     <li>Field 'displayName' must not be empty</li>
     *     <li>Field 'operatingSystem' must be a valid {@link OperatingSystem}</li>
     *     <li>Field 'multiplier' must be over <b>0.00</b></li>
     * </ul>
     *
     * @param hardwareRequest the {@link HardwareRequest} to validate
     * @return the {@link ValidationResponse}
     */
    public ValidationResponse<Hardware> validateCreate(final HardwareRequest hardwareRequest) {
        if (hardwareRequest == null) {
            return ValidationResponse.nullObject();
        }

        // Hardware name must be unique
        final Optional<Hardware> hardwareWithMatchingName = businessLogic.getHardwareWithName(hardwareRequest.getHardwareName());
        if (hardwareWithMatchingName.isPresent()) {
            return ValidationResponse.conflictingWith(hardwareRequest, hardwareWithMatchingName.get(), List.of("hardwareName"));
        }

        final List<String> failureMessages = Stream.of(
                hardwareName(hardwareRequest),
                displayName(hardwareRequest),
                operatingSystem(hardwareRequest),
                multiplier(hardwareRequest)
            )
            .filter(Objects::nonNull)
            .collect(toList());

        if (!failureMessages.isEmpty()) {
            return ValidationResponse.failure(hardwareRequest, failureMessages);
        }

        final Hardware convertedHardware = Hardware.createWithoutId(hardwareRequest);
        return ValidationResponse.success(convertedHardware);
    }

    /**
     * Validates a {@link HardwareRequest} to update an existing {@link Hardware} on the system.
     *
     * <p>
     * Validation checks include:
     * <ul>
     *     <li>Input {@code hardwareRequest} and {@code existingHardware} must not be <b>null</b></li>
     *     <li>Field 'hardwareName' must not be empty</li>
     *     <li>If field 'hardwareName' is not empty, it must not be used by another {@link Hardware}, unless it is the {@link Hardware} to be
     *     updated</li>
     *     <li>Field 'displayName' must not be empty</li>
     *     <li>Field 'operatingSystem' must be a valid {@link OperatingSystem}</li>
     *     <li>Field 'multiplier' must be over <b>0.00</b></li>
     * </ul>
     *
     * @param hardwareRequest  the {@link HardwareRequest} to validate
     * @param existingHardware the already existing {@link Hardware} in the system to be updated
     * @return the {@link ValidationResponse}
     */
    public ValidationResponse<Hardware> validateUpdate(final HardwareRequest hardwareRequest, final Hardware existingHardware) {
        if (hardwareRequest == null || existingHardware == null) {
            return ValidationResponse.nullObject();
        }

        // Hardware name must be unique
        final Optional<Hardware> hardwareWithMatchingName = businessLogic.getHardwareWithName(hardwareRequest.getHardwareName());
        if (hardwareWithMatchingName.isPresent() && hardwareWithMatchingName.get().getId() != existingHardware.getId()) {
            return ValidationResponse.conflictingWith(hardwareRequest, hardwareWithMatchingName.get(), List.of("hardwareName"));
        }

        final List<String> failureMessages = Stream.of(
                hardwareName(hardwareRequest),
                displayName(hardwareRequest),
                operatingSystem(hardwareRequest),
                multiplier(hardwareRequest)
            )
            .filter(Objects::nonNull)
            .collect(toList());

        if (!failureMessages.isEmpty()) {
            return ValidationResponse.failure(hardwareRequest, failureMessages);
        }

        final Hardware convertedHardware = Hardware.createWithoutId(hardwareRequest);
        return ValidationResponse.success(convertedHardware);
    }

    /**
     * Validates a {@link Hardware} to be deleted from the system.
     *
     * @param hardware the {@link Hardware} to validate
     * @return the {@link ValidationResponse}
     */
    public ValidationResponse<Hardware> validateDelete(final Hardware hardware) {
        final Collection<User> usersWithMatchingHardware = businessLogic.getUsersWithHardware(hardware);

        if (!usersWithMatchingHardware.isEmpty()) {
            return ValidationResponse.usedBy(hardware, usersWithMatchingHardware);
        }

        return ValidationResponse.success(hardware);
    }
}

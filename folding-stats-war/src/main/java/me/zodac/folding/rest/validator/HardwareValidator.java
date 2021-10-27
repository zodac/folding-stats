package me.zodac.folding.rest.validator;

import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.HardwareMake;
import me.zodac.folding.api.tc.HardwareType;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.rest.api.tc.request.HardwareRequest;
import org.apache.commons.lang3.StringUtils;

/**
 * Validator class to validate a {@link Hardware} or {@link HardwareRequest}.
 */
public final class HardwareValidator {

    // Assuming the 'best' hardware will have a multiplier of <b>1.00</b>, and all others will be based on that
    private static final double MINIMUM_MULTIPLIER_VALUE = 1.00D;
    private static final long MINIMUM_AVERAGE_PPD_VALUE = 1L;

    private HardwareValidator() {

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
     *     <li>Field 'multiplier' must be over <b>0.00</b></li>
     * </ul>
     *
     * @param hardwareRequest the {@link HardwareRequest} to validate
     *                        @param allHardware      all existing {@link Hardware}s in the system
     * @return the {@link ValidationResult}
     */
    public static ValidationResult<Hardware> validateCreate(final HardwareRequest hardwareRequest, final Collection<Hardware> allHardware) {
        if (hardwareRequest == null) {
            return ValidationResult.nullObject();
        }

        // Hardware name must be unique
        final Optional<Hardware> hardwareWithMatchingName = getHardwareWithName(hardwareRequest.getHardwareName(), allHardware);
        if (hardwareWithMatchingName.isPresent()) {
            return ValidationResult.conflictingWith(hardwareRequest, hardwareWithMatchingName.get(), List.of("hardwareName"));
        }

        final List<String> failureMessages = Stream.of(
                hardwareName(hardwareRequest),
                displayName(hardwareRequest),
                hardwareMake(hardwareRequest),
                hardwareType(hardwareRequest),
                multiplier(hardwareRequest),
                averagePpd(hardwareRequest)
            )
            .filter(Objects::nonNull)
            .collect(toList());

        if (!failureMessages.isEmpty()) {
            return ValidationResult.failure(hardwareRequest, failureMessages);
        }

        final Hardware convertedHardware = Hardware.createWithoutId(hardwareRequest);
        return ValidationResult.successful(convertedHardware);
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
     *     <li>Field 'multiplier' must be over <b>0.00</b></li>
     * </ul>
     *
     * @param hardwareRequest  the {@link HardwareRequest} to validate
     * @param existingHardware the already existing {@link Hardware} in the system to be updated
     * @param allHardware      all existing {@link Hardware}s in the system
     * @return the {@link ValidationResult}
     */
    public static ValidationResult<Hardware> validateUpdate(final HardwareRequest hardwareRequest,
                                                            final Hardware existingHardware,
                                                            final Collection<Hardware> allHardware) {
        if (hardwareRequest == null || existingHardware == null) {
            return ValidationResult.nullObject();
        }

        // Hardware name must be unique
        final Optional<Hardware> hardwareWithMatchingName = getHardwareWithName(hardwareRequest.getHardwareName(), allHardware);
        if (hardwareWithMatchingName.isPresent() && hardwareWithMatchingName.get().getId() != existingHardware.getId()) {
            return ValidationResult.conflictingWith(hardwareRequest, hardwareWithMatchingName.get(), List.of("hardwareName"));
        }

        final List<String> failureMessages = Stream.of(
                hardwareName(hardwareRequest),
                displayName(hardwareRequest),
                hardwareMake(hardwareRequest),
                hardwareType(hardwareRequest),
                multiplier(hardwareRequest),
                averagePpd(hardwareRequest)
            )
            .filter(Objects::nonNull)
            .collect(toList());

        if (!failureMessages.isEmpty()) {
            return ValidationResult.failure(hardwareRequest, failureMessages);
        }

        final Hardware convertedHardware = Hardware.createWithoutId(hardwareRequest);
        return ValidationResult.successful(convertedHardware);
    }

    /**
     * Validates a {@link Hardware} to be deleted from the system.
     *
     * @param hardware the {@link Hardware} to validate
     * @param allUsers all existing {@link User}s in the system
     * @return the {@link ValidationResult}
     */
    public static ValidationResult<Hardware> validateDelete(final Hardware hardware, final Collection<User> allUsers) {
        final Collection<User> usersWithMatchingHardware = getUsersWithHardware(hardware.getId(), allUsers);

        if (!usersWithMatchingHardware.isEmpty()) {
            return ValidationResult.usedBy(hardware, usersWithMatchingHardware);
        }

        return ValidationResult.successful(hardware);
    }

    private static Optional<Hardware> getHardwareWithName(final String hardwareName, final Collection<Hardware> allHardware) {
        if (StringUtils.isBlank(hardwareName)) {
            return Optional.empty();
        }

        return allHardware
            .stream()
            .filter(hardware -> hardware.getHardwareName().equalsIgnoreCase(hardwareName))
            .findAny();
    }

    private static Collection<User> getUsersWithHardware(final int hardwareId, final Collection<User> allUsers) {
        if (hardwareId == Hardware.EMPTY_HARDWARE_ID) {
            return Collections.emptyList();
        }

        return allUsers
            .stream()
            .filter(user -> user.getHardware().getId() == hardwareId)
            .collect(toList());
    }

    private static String hardwareName(final HardwareRequest hardwareRequest) {
        return StringUtils.isNotBlank(hardwareRequest.getHardwareName())
            ? null
            : "Field 'hardwareName' must not be empty";
    }

    private static String displayName(final HardwareRequest hardwareRequest) {
        return StringUtils.isNotBlank(hardwareRequest.getDisplayName())
            ? null
            : "Field 'displayName' must not be empty";
    }

    private static String hardwareMake(final HardwareRequest hardwareRequest) {
        return HardwareMake.get(hardwareRequest.getHardwareMake()) == HardwareMake.INVALID
            ? String.format("Field 'hardwareMake' must be one of: %s", HardwareMake.getAllValues())
            : null;
    }

    private static String hardwareType(final HardwareRequest hardwareRequest) {
        return HardwareType.get(hardwareRequest.getHardwareType()) == HardwareType.INVALID
            ? String.format("Field 'hardwareType' must be one of: %s", HardwareType.getAllValues())
            : null;
    }

    private static String multiplier(final HardwareRequest hardwareRequest) {
        return hardwareRequest.getMultiplier() >= MINIMUM_MULTIPLIER_VALUE
            ? null
            : String.format("Field 'multiplier' must be %.2f or higher", MINIMUM_MULTIPLIER_VALUE);
    }

    private static String averagePpd(final HardwareRequest hardwareRequest) {
        return hardwareRequest.getAveragePpd() >= MINIMUM_AVERAGE_PPD_VALUE
            ? null
            : String.format("Field 'averagePpd' must be %d or higher", MINIMUM_AVERAGE_PPD_VALUE);
    }
}

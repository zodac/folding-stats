/*
 * MIT License
 *
 * Copyright (c) 2021-2022 zodac.me
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.zodac.folding.bean.tc.validation;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import me.zodac.folding.api.exception.ConflictException;
import me.zodac.folding.api.exception.UsedByException;
import me.zodac.folding.api.exception.ValidationException;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.HardwareMake;
import me.zodac.folding.api.tc.HardwareType;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.bean.api.FoldingRepository;
import me.zodac.folding.rest.api.tc.request.HardwareRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Validator class to validate a {@link Hardware} or {@link HardwareRequest}.
 */
@Component
public class HardwareValidator {

    private static final String CONFLICTING_ATTRIBUTE = "hardwareName";

    private final FoldingRepository foldingRepository;

    /**
     * {@link Autowired} constructor.
     *
     * @param foldingRepository the {@link FoldingRepository}
     */
    @Autowired
    public HardwareValidator(final FoldingRepository foldingRepository) {
        this.foldingRepository = foldingRepository;
    }

    /**
     * Validates a {@link HardwareRequest} for a {@link Hardware} to be created on the system.
     *
     * <p>
     * Validation checks include:
     * <ul>
     *     <li>Field 'hardwareName' must not be empty</li>
     *     <li>If field 'hardwareName' is valid, it must not be used by another {@link Hardware}</li>
     *     <li>Field 'displayName' must not be empty</li>
     *     <li>Field 'hardwareMake' must be a valid {@link HardwareMake}</li>
     *     <li>Field 'hardwareType' must be a valid {@link HardwareType}</li>
     *     <li>Field 'multiplier' must be over <b>0.00</b></li>
     *     <li>Field 'averagePpd' must be over <b>1</b></li>
     * </ul>
     *
     * @param hardwareRequest the {@link HardwareRequest} to validate
     * @return the validated {@link Hardware}
     * @throws ConflictException   thrown if the input conflicts with an existing {@link Hardware}
     * @throws ValidationException thrown  if the input fails validation
     */
    public Hardware create(final HardwareRequest hardwareRequest) {
        foldingRepository.getAllTeams();
        // The hardwareName must be unique
        final Optional<Hardware> hardwareWithMatchingName = getHardwareWithName(hardwareRequest.getHardwareName());
        if (hardwareWithMatchingName.isPresent()) {
            throw new ConflictException(hardwareRequest, hardwareWithMatchingName.get(), CONFLICTING_ATTRIBUTE);
        }

        hardwareRequest.validate();
        return Hardware.createWithoutId(hardwareRequest);
    }

    /**
     * Validates a {@link HardwareRequest} to update an existing {@link Hardware} on the system.
     *
     * <p>
     * Validation checks include:
     * <ul>
     *     <li>Field 'hardwareName' must not be empty</li>
     *     <li>If field 'hardwareName' is valid, it must not be used by another {@link Hardware}, unless it is the {@link Hardware} to be
     *     updated</li>
     *     <li>Field 'displayName' must not be empty</li>
     *     <li>Field 'hardwareMake' must be a valid {@link HardwareMake}</li>
     *     <li>Field 'hardwareType' must be a valid {@link HardwareType}</li>
     *     <li>Field 'multiplier' must be over <b>0.00</b></li>
     *     <li>Field 'averagePpd' must be over <b>1</b></li>
     * </ul>
     *
     * @param hardwareRequest  the {@link HardwareRequest} to validate
     * @param existingHardware the already existing {@link Hardware} in the system to be updated
     * @return the validated {@link Hardware}
     * @throws ConflictException   thrown if the input conflicts with an existing {@link Hardware}
     * @throws ValidationException thrown if the input fails validation
     */
    public Hardware update(final HardwareRequest hardwareRequest, final Hardware existingHardware) {
        // The hardwareName must be unique, unless replacing the same hardware
        final Optional<Hardware> hardwareWithMatchingName = getHardwareWithName(hardwareRequest.getHardwareName());
        if (hardwareWithMatchingName.isPresent() && hardwareWithMatchingName.get().id() != existingHardware.id()) {
            throw new ConflictException(hardwareRequest, hardwareWithMatchingName.get(), CONFLICTING_ATTRIBUTE);
        }

        hardwareRequest.validate();
        return Hardware.createWithoutId(hardwareRequest);
    }

    /**
     * Validates a {@link Hardware} to be deleted from the system.
     *
     * <p>
     * If the {@link Hardware} is in use by a {@link User}, it cannot be deleted.
     *
     * @param hardware the {@link Hardware} to validate
     * @return the validated {@link Hardware}
     * @throws UsedByException thrown if the {@link Hardware} is in use by a {@link User}
     */
    public Hardware delete(final Hardware hardware) {
        final Collection<User> usersWithMatchingHardware = getUsersWithHardware(hardware.id());

        if (!usersWithMatchingHardware.isEmpty()) {
            throw new UsedByException(hardware, usersWithMatchingHardware);
        }

        return hardware;
    }

    private Optional<Hardware> getHardwareWithName(final String hardwareName) {
        return foldingRepository.getAllHardware()
            .stream()
            .filter(hardware -> hardware.hardwareName().equalsIgnoreCase(hardwareName))
            .findAny();
    }

    private Collection<User> getUsersWithHardware(final int hardwareId) {
        if (hardwareId == Hardware.EMPTY_HARDWARE_ID) {
            return Collections.emptyList();
        }

        return foldingRepository.getAllUsersWithoutPasskeys()
            .stream()
            .filter(user -> user.hardware().id() == hardwareId)
            .map(User::hidePasskey)
            .toList();
    }
}

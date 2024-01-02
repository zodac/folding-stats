/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2024 zodac.me
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package me.zodac.folding.api.tc;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import me.zodac.folding.api.util.EnvironmentVariableUtils;

/**
 * Lists the possible {@link Team} categories for a {@link User} taking part in the {@code Team Competition}.
 *
 * <p>
 * None of these have a user-friendly display name defined, as those will be handled by the frontend.
 */
public enum Category {

    /**
     * {@link User} is using an {@link HardwareMake#AMD} {@link HardwareType#GPU} as their {@link Hardware}.
     */
    AMD_GPU(getCategoryCount("USERS_IN_AMD_GPU"), HardwareMake.AMD),

    /**
     * {@link User} is using an {@link HardwareMake#NVIDIA} {@link HardwareType#GPU} as their {@link Hardware}.
     */
    NVIDIA_GPU(getCategoryCount("USERS_IN_NVIDIA_GPU"), HardwareMake.NVIDIA),

    /**
     * {@link User} is permitted to use any {@link HardwareMake} and {@link HardwareType} as their {@link Hardware}.
     */
    WILDCARD(getCategoryCount("USERS_IN_WILDCARD"), HardwareMake.getAllValues(), HardwareType.getAllValues()),

    /**
     * Not a valid {@link Category}.
     */
    INVALID(0, Set.of(), Set.of());

    private static final int DEFAULT_USERS_PER_CATEGORY = 1;

    private static final Collection<Category> ALL_VALUES = List.of(values());

    private final int permittedUsers;
    private final Set<HardwareMake> supportedHardwareMakes;
    private final Set<HardwareType> supportedHardwareTypes;

    /**
     * Constructs a {@link Category}.
     *
     * @param permittedUsers the maximum number of {@link User}s permitted in the {@link Category}
     * @param hardwareMakes  the supported {@link HardwareMake}s for a piece of {@link Hardware} used by a {@link User}
     * @param hardwareTypes  the supported {@link HardwareType}s for a piece of {@link Hardware} used by a {@link User}
     */
    Category(final int permittedUsers, final Collection<HardwareMake> hardwareMakes, final Collection<HardwareType> hardwareTypes) {
        this.permittedUsers = permittedUsers;
        supportedHardwareMakes = hardwareMakes.isEmpty() ? EnumSet.noneOf(HardwareMake.class) : EnumSet.copyOf(hardwareMakes);
        supportedHardwareTypes = hardwareTypes.isEmpty() ? EnumSet.noneOf(HardwareType.class) : EnumSet.copyOf(hardwareTypes);
    }

    /**
     * Constructs a {@link Category}.
     *
     * @param permittedUsers the maximum number of {@link User}s permitted in the {@link Category}
     * @param hardwareMake   a supported {@link HardwareMake} for a piece of {@link Hardware} used by a {@link User}
     */
    Category(final int permittedUsers, final HardwareMake hardwareMake) {
        this.permittedUsers = permittedUsers;
        supportedHardwareMakes = EnumSet.of(hardwareMake);
        supportedHardwareTypes = EnumSet.of(HardwareType.GPU);
    }

    /**
     * Retrieve all available {@link Category}s (excluding {@link Category#INVALID}).
     *
     * <p>
     * Should be used instead of {@link Category#values()}, as that recalculates the array for each call,
     * while this method uses a static {@link Collection}.
     *
     * @return an unmodifiable {@link Collection} of all {@link Category}s
     */
    public static Collection<Category> getAllValues() {
        return ALL_VALUES
            .stream()
            .filter(value -> value != INVALID)
            .toList();
    }

    /**
     * Calculates the total number of {@link User}s permitted in a {@link Team} as the sum of the permitted amounts for each {@link Category}.
     *
     * @return the total number of {@link User}s for all {@link Category}s
     */
    public static int maximumPermittedAmountForAllCategories() {
        return ALL_VALUES.stream()
            .mapToInt(value -> value.permittedUsers)
            .sum();
    }

    /**
     * Retrieve a {@link Category} based on the input {@link String}. The search is case-insensitive.
     *
     * @param input the {@link Category} as a {@link String}
     * @return the matching {@link Category}, or {@link Category#INVALID} if none is found
     */
    public static Category get(final String input) {
        return ALL_VALUES
            .stream()
            .filter(category -> category.toString().equalsIgnoreCase(input))
            .findAny()
            .orElse(INVALID);
    }

    /**
     * Returns the number of permitted {@link User}s per team in the {@link Category}.
     *
     * @return the number of permitted {@link User}s
     */
    public int permittedUsers() {
        return permittedUsers;
    }

    /**
     * Checks if the provided {@link HardwareMake} is supported by the {@link Category}.
     *
     * @param hardwareMake the {@link HardwareMake} to check
     * @return <b>true</b> if the {@link HardwareMake} is supported
     */
    public boolean isHardwareMakeSupported(final HardwareMake hardwareMake) {
        return supportedHardwareMakes.contains(hardwareMake);
    }

    /**
     * Checks if the provided {@link HardwareType} is supported by the {@link Category}.
     *
     * @param hardwareType the {@link HardwareType} to check
     * @return <b>true</b> if the {@link HardwareType} is supported
     */
    public boolean isHardwareTypeSupported(final HardwareType hardwareType) {
        return supportedHardwareTypes.contains(hardwareType);
    }

    /**
     * Returns the supported {@link HardwareMake}s in the {@link Category}.
     *
     * @return a {@link Set} of the supported {@link HardwareMake}s
     */
    public Set<HardwareMake> supportedHardwareMakes() {
        return Collections.unmodifiableSet(supportedHardwareMakes);
    }

    /**
     * Returns the supported {@link HardwareType}s in the {@link Category}.
     *
     * @return a {@link Set} of the supported {@link HardwareType}s
     */
    public Set<HardwareType> supportedHardwareTypes() {
        return Collections.unmodifiableSet(supportedHardwareTypes);
    }

    private static int getCategoryCount(final String categoryEnvironmentVariable) {
        return EnvironmentVariableUtils.getIntOrDefault(categoryEnvironmentVariable, DEFAULT_USERS_PER_CATEGORY);
    }
}

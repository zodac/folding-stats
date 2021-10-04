package me.zodac.folding.api.tc;

import static java.util.stream.Collectors.toUnmodifiableList;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Lists the possible {@link Team} categories for a {@link User} taking part in the <code>Team Competition</code>.
 *
 * <p>
 * None of these have a user-friendly display name defined, as those will be handled by the frontend.
 */
public enum Category {

    /**
     * {@link User} is using an {@link HardwareMake#AMD} {@link HardwareType#GPU} as their {@link Hardware}.
     */
    AMD_GPU(1, HardwareMake.AMD, HardwareType.GPU),

    /**
     * {@link User} is using an {@link HardwareMake#NVIDIA} {@link HardwareType#GPU} as their {@link Hardware}.
     */
    NVIDIA_GPU(1, HardwareMake.NVIDIA, HardwareType.GPU),

    /**
     * {@link User} is permitted to use any {@link HardwareMake} and {@link HardwareType} as their {@link Hardware}.
     */
    WILDCARD(1, HardwareMake.getAllValues(), HardwareType.getAllValues()),

    /**
     * Not a valid {@link Category}.
     */
    INVALID(0, Collections.emptySet(), Collections.emptySet());

    private static final Collection<Category> ALL_VALUES = Stream.of(values())
        .filter(value -> value != INVALID)
        .collect(toUnmodifiableList());

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
        supportedHardwareMakes = new HashSet<>(hardwareMakes);
        supportedHardwareTypes = new HashSet<>(hardwareTypes);
    }

    /**
     * Constructs a {@link Category}.
     *
     * @param permittedUsers the maximum number of {@link User}s permitted in the {@link Category}
     * @param hardwareMake   a supported {@link HardwareMake} for a piece of {@link Hardware} used by a {@link User}
     * @param hardwareType   a supported {@link HardwareType} for a piece of {@link Hardware} used by a {@link User}
     */
    Category(final int permittedUsers, final HardwareMake hardwareMake, final HardwareType hardwareType) {
        this.permittedUsers = permittedUsers;
        supportedHardwareMakes = Set.of(hardwareMake);
        supportedHardwareTypes = Set.of(hardwareType);
    }

    /**
     * Retrieve all available {@link Category}s (excluding {@link Category#INVALID}).
     *
     * <p>
     * Should be used instead of {@link Category#values()}, as that recalculates the array for each call,
     * while this method uses a static {@link Collection}.
     *
     * @return a {@link Collection} of all {@link Category}s
     */
    public static Collection<Category> getAllValues() {
        return ALL_VALUES;
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
            .orElse(Category.INVALID);
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
     * Returns the supported {@link HardwareMake}s in the {@link Category}.
     *
     * @return a {@link Set} of the supported {@link HardwareMake}s
     */
    public Set<HardwareMake> supportedHardwareMakes() {
        return supportedHardwareMakes;
    }

    /**
     * Returns the supported {@link HardwareType}s in the {@link Category}.
     *
     * @return a {@link Set} of the supported {@link HardwareType}s
     */
    public Set<HardwareType> supportedHardwareTypes() {
        return supportedHardwareTypes;
    }
}
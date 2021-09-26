package me.zodac.folding.api.tc;

import static java.util.stream.Collectors.toUnmodifiableList;

import java.util.Collection;
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
     * {@link User} is using an <b>AMD</b> {@link HardwareType#GPU} as their {@link Hardware}.
     */
    AMD_GPU(1, HardwareType.GPU),

    /**
     * {@link User} is using an <b>nVidia</b> {@link HardwareType#GPU} as their {@link Hardware}.
     */
    NVIDIA_GPU(1, HardwareType.GPU),

    /**
     * {@link User} is permitted to use any {@link HardwareType} as their {@link Hardware}.
     */
    WILDCARD(1, HardwareType.GPU, HardwareType.CPU),

    /**
     * Not a valid {@link Category}.
     */
    INVALID(0);

    private static final Collection<Category> ALL_VALUES = Stream.of(values())
        .filter(value -> value != INVALID)
        .collect(toUnmodifiableList());

    private final int permittedUsers;
    private final Set<HardwareType> supportedHardwareTypes;

    /**
     * Constructs a {@link Category} with a maximum number of {@link User}s.
     *
     * @param permittedUsers the maximum number of {@link User}s permitted in the {@link Category}
     */
    Category(final int permittedUsers, final HardwareType... hardwareTypes) {
        this.permittedUsers = permittedUsers;
        supportedHardwareTypes = Set.of(hardwareTypes);
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
     * Returns the supported {@link HardwareType}s in the {@link Category}.
     *
     * @return a {@link Set} of the supported {@link HardwareType}s
     */
    public Set<HardwareType> supportedHardwareTypes() {
        return supportedHardwareTypes;
    }
}
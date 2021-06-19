package me.zodac.folding.api.tc;

import static java.util.stream.Collectors.toUnmodifiableList;

import java.util.Collection;
import java.util.stream.Stream;

/**
 * Lists the possible {@link Team} categories for a {@link User} taking part in the <code>Team Competition</code>.
 *
 * <p>
 * None of these have a user-friendly display name defined, as those will be handled by the frontend.
 */
public enum Category {

    /**
     * {@link User} is using an AMD GPU as their {@link Hardware}.
     */
    AMD_GPU(1),

    /**
     * {@link User} is using an nVidia GPU as their {@link Hardware}.
     */
    NVIDIA_GPU(1),

    /**
     * {@link User} is permitted to use any GPU or CPU as their {@link Hardware}.
     */
    WILDCARD(1),

    /**
     * Not a valid {@link Category}.
     */
    INVALID(0);

    private static final Collection<Category> ALL_VALUES = Stream.of(values())
        .filter(value -> value != INVALID)
        .collect(toUnmodifiableList());

    private final int permittedUsers;

    /**
     * Constructs a {@link Category} with a maximum number of {@link User}s.
     *
     * @param permittedUsers the maximum number of {@link User}s permitted in the {@link Category}
     */
    Category(final int permittedUsers) {
        this.permittedUsers = permittedUsers;
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
}
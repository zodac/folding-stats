package me.zodac.folding.api.tc;

import java.util.Collection;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toUnmodifiableList;

/**
 * Lists the possible {@link Team} categories for a {@link User} taking part in the <code>Team Competition</code>.
 */
public enum Category {

    /**
     * {@link User} is using an AMD GPU as their {@link Hardware}.
     */
    AMD_GPU("AMD GPU", 1),

    /**
     * {@link User} is using an nVidia GPU as their {@link Hardware}.
     */
    NVIDIA_GPU("nVidia GPU", 1),

    /**
     * {@link User} is permitted to use any GPU or CPU as their {@link Hardware}.
     */
    WILDCARD("Wildcard", 1),

    /**
     * Not a valid {@link Category}.
     */
    INVALID("Invalid", 0);

    private static final Collection<Category> ALL_VALUES = Stream.of(values())
            .filter(value -> value != INVALID)
            .collect(toUnmodifiableList());

    private final String displayName;
    private final int permittedAmount;

    Category(final String displayName, final int permittedAmount) {
        this.displayName = displayName;
        this.permittedAmount = permittedAmount;
    }

    public String displayName() {
        return displayName;
    }

    public int permittedAmount() {
        return permittedAmount;
    }

    public static Collection<Category> getAllValues() {
        return ALL_VALUES;
    }

    public static int maximumPermittedAmountForAllCategories() {
        return ALL_VALUES.stream()
                .mapToInt(value -> value.permittedAmount)
                .sum();
    }

    public static Category get(final String input) {
        for (final Category category : ALL_VALUES) {
            if (category.toString().equalsIgnoreCase(input) || category.displayName.equalsIgnoreCase(input)) {
                return category;
            }
        }

        return Category.INVALID;
    }
}
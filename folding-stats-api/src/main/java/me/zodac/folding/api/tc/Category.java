package me.zodac.folding.api.tc;

import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toUnmodifiableList;

/**
 * Lists the possible team categories for a user taking part in the <code>Team Competition</code>.
 */
public enum Category {

    AMD_GPU("AMD GPU", 1),
    NVIDIA_GPU("nVidia GPU", 1),
    WILDCARD("Wildcard", 1),
    INVALID("Invalid", 0);

    private static final List<Category> VALUES_AS_LIST = Stream.of(values())
            .filter(value -> value != INVALID)
            .collect(toUnmodifiableList());

    private final String displayName;
    private final int permittedAmount;

    Category(final String displayName, final int permittedAmount) {
        this.displayName = displayName;
        this.permittedAmount = permittedAmount;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getPermittedAmount() {
        return permittedAmount;
    }

    public static List<Category> getValuesAsList() {
        return VALUES_AS_LIST;
    }

    public static Category get(final String input) {
        for (final Category category : VALUES_AS_LIST) {
            if (category.toString().equalsIgnoreCase(input) || category.displayName.equalsIgnoreCase(input)) {
                return category;
            }
        }

        return Category.INVALID;
    }
}
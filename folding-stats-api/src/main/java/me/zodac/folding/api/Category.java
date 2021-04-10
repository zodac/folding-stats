package me.zodac.folding.api;

import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toUnmodifiableList;

public enum Category {

    AMD_GPU("AMD GPU", 1),
    NVIDIA_GPU("nVidia GPU", 1),
    WILDCARD("Wildcard", 1),
    INVALID("Invalid", 0);

    private static final List<Category> VALUES_AS_LIST = Stream.of(values())
            .filter(value -> !value.toString().equalsIgnoreCase("invalid"))
            .collect(toUnmodifiableList());

    private final String displayName;
    private final int numberAllowed;

    Category(final String displayName, final int numberAllowed) {
        this.displayName = displayName;
        this.numberAllowed = numberAllowed;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getNumberAllowed() {
        return numberAllowed;
    }

    public static List<Category> getValuesAsList() {
        return VALUES_AS_LIST;
    }

    public static Category get(final String input) {
        for (final Category category : VALUES_AS_LIST) {
            if (category.toString().equalsIgnoreCase(input)) {
                return category;
            }
        }

        return Category.INVALID;
    }
}
package me.zodac.folding.api.tc;

import java.util.Collection;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toUnmodifiableList;


/**
 * Lists the supported operating systems for {@link Hardware} permitted in the <code>Team Competition</code>.
 */
public enum OperatingSystem {

    /**
     * All Microsoft Windows {@link OperatingSystem}s.
     */
    WINDOWS("Windows"),

    /**
     * All Linux {@link OperatingSystem} distributions.
     */
    LINUX("Linux"),

    /**
     * Not a valid {@link OperatingSystem}.
     */
    INVALID("Invalid");

    private static final Collection<OperatingSystem> ALL_VALUES = Stream.of(values())
            .filter(value -> value != INVALID)
            .collect(toUnmodifiableList());

    private final String displayName;

    OperatingSystem(final String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }

    public static Collection<OperatingSystem> getAllValues() {
        return ALL_VALUES;
    }

    public static OperatingSystem get(final String input) {
        for (final OperatingSystem category : ALL_VALUES) {
            if (category.toString().equalsIgnoreCase(input)) {
                return category;
            }
        }

        return OperatingSystem.INVALID;
    }
}
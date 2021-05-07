package me.zodac.folding.api.tc;

import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toUnmodifiableList;


/**
 * Lists the supported operating systems for {@link Hardware} permitted in the <code>Team Competition</code>.
 */
public enum OperatingSystem {

    WINDOWS("Windows"),
    LINUX("Linux"),
    INVALID("Invalid");

    private static final List<OperatingSystem> VALUES_AS_LIST = Stream.of(values())
            .filter(value -> value != INVALID)
            .collect(toUnmodifiableList());

    private final String displayName;

    OperatingSystem(final String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }

    public static List<OperatingSystem> getValuesAsList() {
        return VALUES_AS_LIST;
    }

    public static OperatingSystem get(final String input) {
        for (final OperatingSystem category : VALUES_AS_LIST) {
            if (category.toString().equalsIgnoreCase(input)) {
                return category;
            }
        }

        return OperatingSystem.INVALID;
    }
}
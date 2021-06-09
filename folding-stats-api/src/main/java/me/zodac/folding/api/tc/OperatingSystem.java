package me.zodac.folding.api.tc;

import java.util.Collection;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toUnmodifiableList;


/**
 * Lists the supported operating systems for {@link Hardware} permitted in the <code>Team Competition</code>.
 * <p>
 * None of these have a user-friendly display name defined, as those will be handled by the frontend.
 */
public enum OperatingSystem {

    /**
     * All Microsoft Windows {@link OperatingSystem}s.
     */
    WINDOWS,

    /**
     * All Linux {@link OperatingSystem} distributions.
     */
    LINUX,

    /**
     * Not a valid {@link OperatingSystem}.
     */
    INVALID;

    private static final Collection<OperatingSystem> ALL_VALUES = Stream.of(values())
            .filter(value -> value != INVALID)
            .collect(toUnmodifiableList());

    /**
     * Retrieve all available {@link OperatingSystem}s (excluding {@link OperatingSystem#INVALID}).
     * <p>
     * Should be used instead of {@link OperatingSystem#values()}, as that recalculates the array for each call,
     * while this method uses a static {@link Collection}.
     *
     * @return a {@link Collection} of all {@link OperatingSystem}s
     */
    public static Collection<OperatingSystem> getAllValues() {
        return ALL_VALUES;
    }

    /**
     * Retrieve an {@link OperatingSystem} based on the input {@link String}. The search is case-insensitive.
     *
     * @param input the {@link OperatingSystem} as a {@link String}
     * @return the matching {@link OperatingSystem}, or {@link OperatingSystem#INVALID} if none is found
     */
    public static OperatingSystem get(final String input) {
        return ALL_VALUES
                .stream()
                .filter(operatingSystem -> operatingSystem.toString().equalsIgnoreCase(input))
                .findAny()
                .orElse(OperatingSystem.INVALID);
    }
}
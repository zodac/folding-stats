package me.zodac.folding.api.tc;

import static java.util.stream.Collectors.toUnmodifiableList;

import java.util.Collection;
import java.util.stream.Stream;

/**
 * Specifies the make/brand of {@link Hardware}.
 */
public enum HardwareMake {

    /**
     * The {@link Hardware} is by <b>AMD</b>.
     */
    AMD,

    /**
     * The {@link Hardware} is by <b>Intel</b>.
     */
    INTEL,

    /**
     * The {@link Hardware} is by <b>nVidia</b>.
     */
    NVIDIA,

    /**
     * Not a valid {@link HardwareMake}.
     */
    INVALID;

    private static final Collection<HardwareMake> ALL_VALUES = Stream.of(values())
        .filter(value -> value != INVALID)
        .collect(toUnmodifiableList());

    /**
     * Retrieve all available {@link HardwareMake}s (excluding {@link HardwareMake#INVALID}).
     *
     * <p>
     * Should be used instead of {@link HardwareMake#values()}, as that recalculates the array for each call,
     * while this method uses a static {@link Collection}.
     *
     * @return a {@link Collection} of all {@link HardwareMake}s
     */
    public static Collection<HardwareMake> getAllValues() {
        return ALL_VALUES;
    }

    /**
     * Retrieve a {@link HardwareMake} based on the input {@link String}. The search is case-insensitive.
     *
     * @param input the {@link HardwareMake} as a {@link String}
     * @return the matching {@link HardwareMake}, or {@link HardwareMake#INVALID} if none is found
     */
    public static HardwareMake get(final String input) {
        return ALL_VALUES
            .stream()
            .filter(hardwareMake -> hardwareMake.toString().equalsIgnoreCase(input))
            .findAny()
            .orElse(HardwareMake.INVALID);
    }
}

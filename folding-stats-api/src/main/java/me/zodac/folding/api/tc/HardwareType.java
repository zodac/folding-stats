package me.zodac.folding.api.tc;

import static java.util.stream.Collectors.toUnmodifiableList;

import java.util.Collection;
import java.util.stream.Stream;

/**
 * Specifies the type of {@link Hardware}.
 */
public enum HardwareType {

    /**
     * The {@link Hardware} is a CPU.
     */
    CPU,

    /**
     * The {@link Hardware} is a GPU.
     */
    GPU,

    /**
     * Not a valid {@link HardwareType}.
     */
    INVALID;

    private static final Collection<HardwareType> ALL_VALUES = Stream.of(values())
        .filter(value -> value != INVALID)
        .collect(toUnmodifiableList());

    /**
     * Retrieve all available {@link HardwareType}s (excluding {@link HardwareType#INVALID}).
     *
     * <p>
     * Should be used instead of {@link HardwareType#values()}, as that recalculates the array for each call,
     * while this method uses a static {@link Collection}.
     *
     * @return a {@link Collection} of all {@link HardwareType}s
     */
    public static Collection<HardwareType> getAllValues() {
        return ALL_VALUES;
    }

    /**
     * Retrieve a {@link HardwareType} based on the input {@link String}. The search is case-insensitive.
     *
     * @param input the {@link HardwareType} as a {@link String}
     * @return the matching {@link HardwareType}, or {@link HardwareType#INVALID} if none is found
     */
    public static HardwareType get(final String input) {
        return ALL_VALUES
            .stream()
            .filter(hardwareType -> hardwareType.toString().equalsIgnoreCase(input))
            .findAny()
            .orElse(HardwareType.INVALID);
    }
}

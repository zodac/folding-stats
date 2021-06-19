package me.zodac.folding.db;

import static java.util.stream.Collectors.toUnmodifiableList;

import java.util.Collection;
import java.util.stream.Stream;

/**
 * Lists the supported databases for the system. Should be set by the environment variable <b>DEPLOYED_DATABASE</b>.
 */
public enum DatabaseType {

    /**
     * The system is using a PostgreSQL database.
     */
    POSTGRESQL,

    /**
     * Not a valid {@link DatabaseType}.
     */
    INVALID;

    private static final Collection<DatabaseType> ALL_VALUES = Stream.of(values())
        .filter(value -> value != INVALID)
        .collect(toUnmodifiableList());

    /**
     * Retrieve a {@link DatabaseType} based on the input {@link String}. The search is case-insensitive.
     *
     * @param input the {@link DatabaseType} as a {@link String}
     * @return the matching {@link DatabaseType}, or {@link DatabaseType#INVALID} if none is found
     */
    public static DatabaseType get(final String input) {
        return ALL_VALUES
            .stream()
            .filter(category -> category.toString().equalsIgnoreCase(input))
            .findAny()
            .orElse(DatabaseType.INVALID);
    }
}
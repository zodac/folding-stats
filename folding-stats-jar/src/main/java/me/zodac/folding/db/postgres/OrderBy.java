package me.zodac.folding.db.postgres;

import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toUnmodifiableList;

/**
 * Lists the types of ordering possible within the backend storage.
 */
public enum OrderBy {

    ASCENDING("ASC"),
    DESCENDING("DESC");

    private final String sqlValue;

    private static final List<OrderBy> VALUES_AS_LIST = Stream.of(values())
            .collect(toUnmodifiableList());

    OrderBy(final String sqlValue) {
        this.sqlValue = sqlValue;
    }

    public String getSqlValue() {
        return sqlValue;
    }

    public static List<OrderBy> getValuesAsList() {
        return VALUES_AS_LIST;
    }
}

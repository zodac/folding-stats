package me.zodac.folding.api.db;

/**
 * {@link Enum} defining the types of ordering possible within the DB.
 */
public enum OrderBy {

    ASCENDING("ASC"),
    DESCENDING("DESC");

    private final String sqlValue;

    OrderBy(final String sqlValue) {
        this.sqlValue = sqlValue;
    }

    public String getSqlValue() {
        return sqlValue;
    }
}

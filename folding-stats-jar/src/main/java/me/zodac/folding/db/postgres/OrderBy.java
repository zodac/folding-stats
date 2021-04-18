package me.zodac.folding.db.postgres;

/**
 * Lists the types of ordering possible within the backend storage.
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

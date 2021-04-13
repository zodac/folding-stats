package me.zodac.folding.api.db;

public enum OrderBy {

    ASCENDING("ASC"),
    DESCENDING("DESC");

    private final String sqlValue;

    OrderBy(String sqlValue) {
        this.sqlValue = sqlValue;
    }

    public String getSqlValue() {
        return sqlValue;
    }
}

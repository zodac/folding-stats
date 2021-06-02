package me.zodac.folding.rest.api;

/**
 * Set of REST headers and their name.
 */
public enum RestHeader {

    AUTHORIZATION("Authorization"),
    CONTENT_TYPE("Content-Type"),
    IF_NONE_MATCH("If-None-Match");

    private final String headerName;

    RestHeader(final String headerName) {
        this.headerName = headerName;
    }

    public String headerName() {
        return headerName;
    }
}

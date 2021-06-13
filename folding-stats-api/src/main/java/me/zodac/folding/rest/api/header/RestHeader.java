package me.zodac.folding.rest.api.header;

/**
 * Set of REST headers and their name.
 */
public enum RestHeader {

    /**
     * The <code>Authorization</code> header.
     */
    AUTHORIZATION("Authorization"),

    /**
     * The <code>Cache-Control</code> header.
     */
    CACHE_CONTROL("Cache-Control"),

    /**
     * The <code>Content-Type</code> header.
     */
    CONTENT_TYPE("Content-Type"),

    /**
     * The <code>If-None-Match</code> header.
     */
    IF_NONE_MATCH("If-None-Match");

    private final String headerName;

    /**
     * Constructs a {@link RestHeader}.
     *
     * @param headerName the {@link RestHeader} name
     */
    RestHeader(final String headerName) {
        this.headerName = headerName;
    }

    /**
     * The {@link RestHeader} name.
     *
     * @return the name of the {@link RestHeader}
     */
    public String headerName() {
        return headerName;
    }
}

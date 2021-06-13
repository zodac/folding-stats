package me.zodac.folding.rest.api.header;

/**
 * Values for {@link RestHeader#CONTENT_TYPE} REST headers.
 */
public enum ContentType {

    /**
     * The <code>application/json</code> value.
     */
    JSON("application/json");

    private final String contentType;

    /**
     * Constructs a {@link ContentType} with the header value as a {@link String}.
     *
     * @param contentType the {@link ContentType} value as a {@link String}
     */
    ContentType(final String contentType) {
        this.contentType = contentType;
    }

    /**
     * The value of the {@link ContentType}.
     *
     * @return the {@link ContentType} value
     */
    public String contentType() {
        return contentType;
    }
}

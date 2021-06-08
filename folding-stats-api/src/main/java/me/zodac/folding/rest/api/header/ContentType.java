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

    ContentType(final String contentType) {
        this.contentType = contentType;
    }

    public String contentType() {
        return contentType;
    }
}

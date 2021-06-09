package me.zodac.folding.rest.api.header;

/**
 * Values for {@link RestHeader#CACHE_CONTROL} REST headers.
 */
public enum CacheControl {

    /**
     * The <code>no-cache</code> value.
     */
    NO_CACHE("no-cache");

    private final String cacheControl;

    CacheControl(final String cacheControl) {
        this.cacheControl = cacheControl;
    }

    public String cacheControl() {
        return cacheControl;
    }
}

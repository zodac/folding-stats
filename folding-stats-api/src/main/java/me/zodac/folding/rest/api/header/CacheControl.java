package me.zodac.folding.rest.api.header;

/**
 * Values for {@link RestHeader#CACHE_CONTROL} REST headers.
 */
public enum CacheControl {

    /**
     * The <code>no-cache</code> value.
     */
    NO_CACHE("no-cache");

    private final String cacheControlValue;

    /**
     * Constructs a {@link CacheControl} with the header value as a {@link String}.
     *
     * @param cacheControl the {@link CacheControl} value as a {@link String}
     */
    CacheControl(final String cacheControl) {
        cacheControlValue = cacheControl;
    }

    /**
     * The value of the {@link CacheControl}.
     *
     * @return the {@link CacheControl} value
     */
    public String cacheControl() {
        return cacheControlValue;
    }
}

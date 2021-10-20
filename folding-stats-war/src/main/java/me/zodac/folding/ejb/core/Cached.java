package me.zodac.folding.ejb.core;

import me.zodac.folding.cache.BaseCache;

/**
 * Marker annotation to define a function in {@link Storage} that persists and retrieves data from a database, but also caches that data locally.
 */
@interface Cached {

    /**
     * The implementations of {@link BaseCache} used to perform the caching.
     *
     * @return the {@link BaseCache} implementations
     */
    Class<? extends BaseCache<?>>[] value();
}

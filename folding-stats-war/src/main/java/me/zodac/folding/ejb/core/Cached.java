package me.zodac.folding.ejb.core;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import me.zodac.folding.cache.BaseCache;

/**
 * Marker annotation to define a function in {@link Storage} that persists and retrieves data from a database, but also caches that data locally.
 */
@Documented
@Target(ElementType.METHOD)
@interface Cached {

    /**
     * The implementations of {@link BaseCache} used to perform the caching.
     *
     * @return the {@link BaseCache} implementations
     */
    Class<? extends BaseCache<?>>[] value();
}

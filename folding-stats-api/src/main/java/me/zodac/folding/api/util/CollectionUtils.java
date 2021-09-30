package me.zodac.folding.api.util;

import java.util.Collection;
import java.util.Collections;

/**
 * Utility class with convenient {@link Collection}-based functions.
 */
public final class CollectionUtils {

    private CollectionUtils() {

    }

    /**
     * Checks that there are no matches between the two {@link Collection}s.
     *
     * @param first  the first {@link Collection}
     * @param second the second {@link Collection}
     * @param <V>    the type of the {@link Collection}s
     * @return <code>true</code> if there are no common elements in the two {@link Collection}s
     * @see Collections#disjoint(Collection, Collection)
     */
    public static <V> boolean containsNoMatches(final Collection<V> first, final Collection<V> second) {
        return Collections.disjoint(first, second);
    }
}

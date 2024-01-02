/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2024 zodac.me
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package me.zodac.folding.api.util;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Utility class with convenient {@link Collection}-based functions.
 */
public final class CollectionUtils {

    private CollectionUtils() {

    }

    /**
     * Checks if there are no matches between the two {@link Collection}s.
     *
     * @param first  the first {@link Collection}
     * @param second the second {@link Collection}
     * @param <E>    the type of the elements in the {@link Collection}s
     * @return {@code true} if there are no common elements in the two {@link Collection}s
     * @see Collections#disjoint(Collection, Collection)
     */
    public static <E> boolean containsNoMatches(final Collection<E> first, final Collection<E> second) {
        return Collections.disjoint(first, second);
    }

    /**
     * Returns any elements that exist only in the first {@link Collection}, and do not also exist in the second {@link Collection}.
     *
     * @param first  the first {@link Collection}
     * @param second the second {@link Collection}
     * @param <E>    the type of the elements in the {@link Collection}s
     * @return an unsorted {@link Set} of the elements that only exist in the first {@link Collection}
     */
    public static <E> Set<E> existsInFirstOnly(final Collection<? extends E> first, final Collection<E> second) {
        final Set<E> copyOfFirst = new HashSet<>(first);
        copyOfFirst.removeAll(second);
        return copyOfFirst;
    }
}

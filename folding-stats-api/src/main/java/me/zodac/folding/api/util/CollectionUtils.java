/*
 * MIT License
 *
 * Copyright (c) 2021-2022 zodac.me
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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

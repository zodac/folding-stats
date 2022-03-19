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

package me.zodac.folding.rest.api.tc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * Ranking logic for teams/users to rank by a specified function (getting points, or units, etc.).
 *
 * @param <T> the type of the {@link Object}
 * @see <a href="https://stackoverflow.com/a/41608187/2000246">How to rank collection of objects</a>
 */
record IntegerRankingCollector<T>(Comparator<? super T> comparator, Function<T, Integer> ranker,
                                  BiFunction<T, Integer, T> creator) implements Collector<T, List<T>, List<T>> {

    private static final Set<Characteristics> COLLECTOR_CHARACTERISTICS = Collections.unmodifiableSet(EnumSet.of(Characteristics.IDENTITY_FINISH));

    /**
     * Constructs an {@link IntegerRankingCollector}.
     *
     * @param comparator a {@link Comparator} defining what value to be compared
     * @param ranker     the existing rank of the object being ranked
     * @param creator    the {@link BiFunction} defining the output rank of the object
     */
    static <T> IntegerRankingCollector<T> create(final Comparator<? super T> comparator,
                                                 final Function<T, Integer> ranker,
                                                 final BiFunction<T, Integer, T> creator) {
        return new IntegerRankingCollector<>(comparator, ranker, creator);
    }

    @Override
    @SuppressWarnings("ReturnValueIgnored") // False positive
    public BiConsumer<List<T>, T> accumulator() {
        return (list, current) -> {
            final List<T> right = new ArrayList<>(1);
            right.add(creator.apply(current, 1));
            combiner().apply(list, right);
        };
    }

    @Override
    public BinaryOperator<List<T>> combiner() {
        return (left, right) -> {
            final int rankAdjustment = getRankAdjustment(left, right);
            for (final T t : right) {
                left.add(creator.apply(t, rankAdjustment + ranker.apply(t)));
            }
            return left;
        };
    }

    private int getRankAdjustment(final List<T> left, final List<T> right) {
        final Optional<T> lastElementOnTheLeft = optGet(left, left.size() - 1);
        final Optional<T> firstElementOnTheRight = optGet(right, 0);

        if (lastElementOnTheLeft.isEmpty() || firstElementOnTheRight.isEmpty()) {
            return 0;
        }

        if (comparator.compare(firstElementOnTheRight.get(), lastElementOnTheLeft.get()) == 0) {
            return ranker.apply(lastElementOnTheLeft.get()) - 1;
        }

        return ranker.apply(lastElementOnTheLeft.get());
    }

    private Optional<T> optGet(final List<T> list, final int index) {
        return list == null || list.isEmpty() ? Optional.empty() : Optional.of(list.get(index));
    }

    @Override
    public Supplier<List<T>> supplier() {
        return ArrayList::new;
    }

    @Override
    public Function<List<T>, List<T>> finisher() {
        return list -> list;
    }

    @Override
    public Set<Characteristics> characteristics() {
        return COLLECTOR_CHARACTERISTICS;
    }
}
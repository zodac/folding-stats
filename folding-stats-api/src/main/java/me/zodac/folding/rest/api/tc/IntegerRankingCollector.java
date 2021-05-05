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
 * Ranking logic for teams/users to rank by a specified function (getting points, or units, etc).
 *
 * @see <a href="https://stackoverflow.com/a/41608187/2000246">How to rank collection of objects</a>
 */
class IntegerRankingCollector<T> implements Collector<T, List<T>, List<T>> {

    private static final Set<Characteristics> CHARACTERISTICS = Collections.unmodifiableSet(EnumSet.of(Characteristics.IDENTITY_FINISH));
    private final Comparator<? super T> comparator;
    private final BiFunction<T, Integer, T> creator;
    private final Function<T, Integer> ranker;

    public IntegerRankingCollector(final Comparator<? super T> comparator, final Function<T, Integer> ranker, final BiFunction<T, Integer, T> creator) {
        this.comparator = comparator;
        this.ranker = ranker;
        this.creator = creator;
    }

    @Override
    public BiConsumer<List<T>, T> accumulator() {
        return (list, current) -> {
            final List<T> right = new ArrayList<>();
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
        return (list == null || list.isEmpty()) ? Optional.empty() : Optional.of(list.get(index));
    }

    @Override
    public Supplier<List<T>> supplier() {
        return ArrayList::new;
    }

    @Override
    public Function<List<T>, List<T>> finisher() {
        return l -> l;
    }

    @Override
    public Set<Characteristics> characteristics() {
        return CHARACTERISTICS;
    }
}
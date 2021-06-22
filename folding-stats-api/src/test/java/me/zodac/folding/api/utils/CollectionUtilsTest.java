package me.zodac.folding.api.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link CollectionUtils}.
 */
class CollectionUtilsTest {

    @Test
    void whenContainsNoMatches_givenTwoCollectionsWithNoMatches_thenTheReturnedValueIsTrue() {
        final Collection<String> first = Set.of("one", "two");
        final Collection<String> second = Set.of("three", "four");

        final boolean result = CollectionUtils.containsNoMatches(first, second);

        assertThat(result)
            .isTrue();
    }

    @Test
    void whenContainsNoMatches_givenTwoCollectionsWithOneMatch_thenTheReturnedValueIsFalse() {
        final Collection<String> first = Set.of("one", "two");
        final Collection<String> second = Set.of("two", "three");

        final boolean result = CollectionUtils.containsNoMatches(first, second);

        assertThat(result)
            .isFalse();
    }

    @Test
    void whenContainsNoMatches_givenTwoCollectionsThatAreEqual_thenTheReturnedValueIsFalse() {
        final Collection<String> first = Set.of("one", "two");
        final Collection<String> second = Set.of("one", "two");

        final boolean result = CollectionUtils.containsNoMatches(first, second);

        assertThat(result)
            .isFalse();
    }

    @Test
    void whenContainsNoMatches_givenTwoCollections_andOneCollectionIsSubSetOfTheOther_thenTheReturnedValueIsFalse() {
        final Collection<String> first = Set.of("one", "two");
        final Collection<String> second = Set.of("one", "two", "three");

        final boolean result = CollectionUtils.containsNoMatches(first, second);

        assertThat(result)
            .isFalse();
    }
}

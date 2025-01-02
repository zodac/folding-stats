/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2025 zodac.me
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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link CollectionUtils}.
 */
class CollectionUtilsTest {

    @Test
    void whenContainsNoMatches_givenTwoEmptyCollections_thenTheReturnedValueIsTrue() {
        final Collection<String> first = List.of();
        final Collection<String> second = List.of();

        final boolean result = CollectionUtils.containsNoMatches(first, second);

        assertThat(result)
            .as("Expected there to be no matches")
            .isTrue();
    }

    @Test
    void whenContainsNoMatches_givenTwoCollectionsWithNoMatches_thenTheReturnedValueIsTrue() {
        final Collection<String> first = Set.of("one", "two");
        final Collection<String> second = Set.of("three", "four");

        final boolean result = CollectionUtils.containsNoMatches(first, second);

        assertThat(result)
            .as("Expected there to be no matches")
            .isTrue();
    }

    @Test
    void whenContainsNoMatches_givenTwoCollectionsWithOneMatch_thenTheReturnedValueIsFalse() {
        final Collection<String> first = Set.of("one", "two");
        final Collection<String> second = Set.of("two", "three");

        final boolean result = CollectionUtils.containsNoMatches(first, second);

        assertThat(result)
            .as("Expected there to be a match")
            .isFalse();
    }

    @Test
    void whenContainsNoMatches_givenTwoCollectionsThatAreEqual_thenTheReturnedValueIsFalse() {
        final Collection<String> first = Set.of("one", "two");
        final Collection<String> second = Set.of("one", "two");

        final boolean result = CollectionUtils.containsNoMatches(first, second);

        assertThat(result)
            .as("Expected there to be a match")
            .isFalse();
    }

    @Test
    void whenContainsNoMatches_givenTwoCollections_andOneCollectionIsSubSetOfTheOther_thenTheReturnedValueIsFalse() {
        final Collection<String> first = Set.of("one", "two");
        final Collection<String> second = Set.of("one", "two", "three");

        final boolean result = CollectionUtils.containsNoMatches(first, second);

        assertThat(result)
            .as("Expected there to be a match")
            .isFalse();
    }

    @Test
    void whenExistsInFirstOnly_givenTwoCollections_andBothAreEmpty_thenEmptySetIsReturned() {
        final Collection<String> first = Set.of();
        final Collection<String> second = Set.of();

        final Set<String> result = CollectionUtils.existsInFirstOnly(first, second);

        assertThat(result)
            .isEmpty();
    }

    @Test
    void whenExistsInFirstOnly_givenTwoCollections_andBothContainSameElements_thenEmptySetIsReturned() {
        final Collection<String> first = Set.of("one", "two");
        final Collection<String> second = Set.of("one", "two");

        final Set<String> result = CollectionUtils.existsInFirstOnly(first, second);

        assertThat(result)
            .isEmpty();
    }

    @Test
    void whenExistsInFirstOnly_givenTwoCollections_andFirstHasElements_andSecondIsEmpty_thenFirstElementsAreReturned() {
        final Collection<String> first = Set.of("one", "two");
        final Collection<String> second = Set.of();

        final Set<String> result = CollectionUtils.existsInFirstOnly(first, second);

        assertThat(result)
            .isEqualTo(first);
    }

    @Test
    void whenExistsInFirstOnly_givenTwoCollections_andFirstIsEmpty_andSecondHasElements_thenEmptySetIsReturned() {
        final Collection<String> first = Set.of();
        final Collection<String> second = Set.of("one", "two");

        final Set<String> result = CollectionUtils.existsInFirstOnly(first, second);

        assertThat(result)
            .isEmpty();
    }

    @Test
    void whenExistsInFirstOnly_givenTwoCollections_andBothCollectionsHaveDifferentElements_thenFirstElementsAreReturned() {
        final Collection<String> first = Set.of("one", "two");
        final Collection<String> second = Set.of("three", "four");

        final Set<String> result = CollectionUtils.existsInFirstOnly(first, second);

        assertThat(result)
            .isEqualTo(first);
    }

    @Test
    void whenExistsInFirstOnly_givenTwoCollections_andSecondCollectionIsSubSetOfFirst_thenOnlyFirstElementsAreReturned() {
        final Collection<String> first = Set.of("one", "two", "three");
        final Collection<String> second = Set.of("two", "three");

        final Set<String> result = CollectionUtils.existsInFirstOnly(first, second);

        assertThat(result)
            .containsOnly("one");
    }
}

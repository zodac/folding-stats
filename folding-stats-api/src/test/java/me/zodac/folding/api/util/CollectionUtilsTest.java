/*
 * MIT License
 *
 * Copyright (c) 2021 zodac.me
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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link CollectionUtils}.
 */
class CollectionUtilsTest {

    @Test
    void whenContainsNoMatches_givenTwoEmptyCollections_thenTheReturnedValueIsTrue() {
        final Collection<String> first = Collections.emptyList();
        final Collection<String> second = Collections.emptyList();

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
        final Collection<String> first = Collections.emptyList();
        final Collection<String> second = Collections.emptyList();

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
        final Collection<String> second = Collections.emptyList();

        final Set<String> result = CollectionUtils.existsInFirstOnly(first, second);

        assertThat(result)
            .isEqualTo(first);
    }

    @Test
    void whenExistsInFirstOnly_givenTwoCollections_andFirstIsEmpty_andSecondHasElements_thenEmptySetIsReturned() {
        final Collection<String> first = Collections.emptyList();
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

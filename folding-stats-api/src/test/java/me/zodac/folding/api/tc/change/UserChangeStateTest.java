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

package me.zodac.folding.api.tc.change;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link UserChangeState}.
 */
class UserChangeStateTest {

    @Test
    void whenGetAllValues_thenInvalidEntryIsNotIncluded() {
        assertThat(UserChangeState.getAllValues())
            .hasSize(6)
            .doesNotContain(UserChangeState.INVALID);
    }

    @Test
    void whenGetValue_givenValidInput_thenMatchingStateIsReturned() {
        assertThat(UserChangeState.get("COMPLETED"))
            .isEqualTo(UserChangeState.COMPLETED);
    }

    @Test
    void whenGetValue_givenCaseInsensitiveInput_thenMatchingStateIsReturned() {
        assertThat(UserChangeState.get("coMplEted"))
            .isEqualTo(UserChangeState.COMPLETED);
    }

    @Test
    void whenGetValue_givenInvalidInput_thenInvalidStateIsReturned() {
        assertThat(UserChangeState.get("invalidValue"))
            .isEqualTo(UserChangeState.INVALID);
    }

    @Test
    void whenGetAllValues_thenInvalidStateIsNotIncluded() {
        assertThat(UserChangeState.getAllValues())
            .doesNotContain(UserChangeState.INVALID);
    }

    @Test
    void whenGetOpenStates_thenReturnedStatesCanBeUpdated() {
        final Collection<UserChangeState> openStates = UserChangeState.getOpenStates();

        for (final UserChangeState openState : openStates) {
            assertThat(openState.isFinalState())
                .isFalse();
        }
    }

    @Test
    void whenGetFinalStates_thenReturnedStatesCanBeUpdated() {
        final Collection<UserChangeState> openStates = UserChangeState.getOpenStates();
        final Collection<UserChangeState> closedStates = new ArrayList<>(UserChangeState.getAllValues());
        closedStates.removeAll(openStates);

        for (final UserChangeState closedState : closedStates) {
            assertThat(closedState.isFinalState())
                .isTrue();
        }
    }
}

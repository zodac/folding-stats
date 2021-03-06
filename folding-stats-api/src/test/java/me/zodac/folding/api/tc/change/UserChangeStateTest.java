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

package me.zodac.folding.api.tc.change;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link UserChangeState}.
 */
class UserChangeStateTest {

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
}

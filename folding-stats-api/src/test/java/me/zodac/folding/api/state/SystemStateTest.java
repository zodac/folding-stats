/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2023 zodac.me
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

package me.zodac.folding.api.state;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link SystemState}.
 */
class SystemStateTest {

    @Test
    void verifyUnblockedStates() {
        Set.of(
            SystemState.AVAILABLE,
            SystemState.WRITE_EXECUTED
        ).forEach(
            systemState -> {
                assertThat(systemState.isReadBlocked())
                    .isFalse();
                assertThat(systemState.isWriteBlocked())
                    .isFalse();
            }
        );
    }

    @Test
    void verifyReadBlockedStates() {
        Set.of(
            SystemState.RESETTING_STATS,
            SystemState.STARTING
        ).forEach(
            systemState -> {
                assertThat(systemState.isReadBlocked())
                    .isTrue();
                assertThat(systemState.isWriteBlocked())
                    .isTrue();
            }
        );
    }

    @Test
    void verifyWriteBlockedStates() {
        Set.of(
            SystemState.UPDATING_STATS
        ).forEach(
            systemState -> {
                assertThat(systemState.isReadBlocked())
                    .isFalse();
                assertThat(systemState.isWriteBlocked())
                    .isTrue();
            }
        );
    }
}

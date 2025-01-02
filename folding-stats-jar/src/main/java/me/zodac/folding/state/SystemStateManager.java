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

package me.zodac.folding.state;

import me.zodac.folding.api.state.SystemState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Utility class to manage the {@link SystemState}.
 */
public final class SystemStateManager {

    private static final Logger LOGGER = LogManager.getLogger();

    private static SystemState currentState = SystemState.STARTING;

    private SystemStateManager() {

    }

    /**
     * Get the current {@link SystemState}. On system startup, the {@link SystemState} is {@link SystemState#STARTING}.
     *
     * @return the current {@link SystemState}
     */
    public static SystemState current() {
        return currentState;
    }

    /**
     * Change to the next {@link SystemState}.
     *
     * @param nextState the {@link SystemState} to transition to
     */
    public static void next(final SystemState nextState) {
        if (nextState == currentState) {
            LOGGER.trace("System state is already {}, no transition needed", nextState);
            return;
        }

        LOGGER.debug("Transitioning system state from {} to {}", currentState, nextState);
        currentState = nextState;
    }
}

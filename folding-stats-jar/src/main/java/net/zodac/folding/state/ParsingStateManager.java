/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2025 zodac.net
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

package net.zodac.folding.state;

import net.zodac.folding.api.state.ParsingState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Utility class to manage the {@link ParsingState}.
 */
public final class ParsingStateManager {

    private static final Logger LOGGER = LogManager.getLogger();

    private static ParsingState currentState = ParsingState.DISABLED;

    private ParsingStateManager() {

    }

    /**
     * Get the current {@link ParsingState}. On system startup, the {@link ParsingState} is {@link ParsingState#DISABLED}.
     *
     * @return the current {@link ParsingState}
     */
    public static ParsingState current() {
        return currentState;
    }

    /**
     * Change to the next {@link ParsingState}.
     *
     * @param nextState the {@link ParsingState} to transition to
     */
    public static void next(final ParsingState nextState) {
        if (nextState == currentState) {
            LOGGER.trace("Parsing state is already {}, no transition needed", nextState);
            return;
        }

        LOGGER.debug("Transitioning parsing state from {} to {}", currentState, nextState);
        currentState = nextState;
    }
}

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

package me.zodac.folding.state;

import me.zodac.folding.api.state.ParsingState;
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

    // TODO: [zodac] Replace with env check?

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

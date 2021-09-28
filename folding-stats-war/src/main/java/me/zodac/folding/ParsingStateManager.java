package me.zodac.folding;

import me.zodac.folding.api.ParsingState;
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

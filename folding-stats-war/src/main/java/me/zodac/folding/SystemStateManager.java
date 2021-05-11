package me.zodac.folding;

import me.zodac.folding.api.SystemState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class to manage the {@link SystemState}.
 */
public final class SystemStateManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(SystemStateManager.class);

    private static SystemState currentState = SystemState.STARTING;

    private SystemStateManager() {

    }

    /**
     * Get the current {@link SystemState}.
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

        // TODO: [zodac] Define permitted state transitions here, and validate them?
        LOGGER.info("Transitioning system state from {} to {}", currentState, nextState);
        currentState = nextState;
    }
}

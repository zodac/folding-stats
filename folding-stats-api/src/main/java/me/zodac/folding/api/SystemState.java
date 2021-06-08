package me.zodac.folding.api;

/**
 * The various states that the system can be in at a given time.
 */
public enum SystemState {

    /**
     * System is online and available for read/write requests.
     */
    AVAILABLE(true, true),

    /**
     * System is in the process of resetting stats. Since not all caches may have been wiped yet, system is not ready for read/write requests.
     */
    RESETTING_STATS(false, false),

    /**
     * System is coming online, not yet ready for read/write requests.
     */
    STARTING(false, false),

    /**
     * System is in the process of updating stats (for a single {@link me.zodac.folding.api.tc.User}, or all {@link me.zodac.folding.api.tc.User}s), system is ready for read requests, but not write requests.
     */
    UPDATING_STATS(true, false),

    /**
     * System is in state similar to {@link #AVAILABLE}, but a write has been executed, possibly invalidating caches.
     */
    WRITE_EXECUTED(true, true);

    private final boolean isReadAllowed;
    private final boolean isWriteAllowed;

    SystemState(final boolean isReadAllowed, final boolean isWriteAllowed) {
        this.isReadAllowed = isReadAllowed;
        this.isWriteAllowed = isWriteAllowed;
    }

    /**
     * Checks if a read operation is blocked by the {@link SystemState}.
     *
     * @return <code>true</code> if read operations are blocked
     */
    public boolean isReadBlocked() {
        return !isReadAllowed;
    }

    /**
     * Checks if a write operation is blocked by the {@link SystemState}.
     *
     * @return <code>true</code> if write operations are blocked
     */
    public boolean isWriteBlocked() {
        return !isWriteAllowed;
    }
}

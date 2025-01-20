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

package net.zodac.folding.api.state;

import static net.zodac.folding.api.state.OperationType.READ;
import static net.zodac.folding.api.state.OperationType.WRITE;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import net.zodac.folding.api.tc.User;

/**
 * The various states that the system can be in at a given time.
 */
public enum SystemState {

    /**
     * System is online and available for {@link OperationType#READ} and {@link OperationType#WRITE} operations.
     */
    AVAILABLE(READ, WRITE),

    /**
     * System is currently resetting stats. Since not all caches may have been wiped yet, system is not ready for {@link OperationType#READ}
     * or {@link OperationType#WRITE} operations.
     */
    RESETTING_STATS,

    /**
     * System is coming online, not yet ready for {@link OperationType#READ} or {@link OperationType#WRITE} operations.
     */
    STARTING,

    /**
     * System is currently updating stats (for a single {@link User}, or all {@link User}s),
     * system is ready for {@link OperationType#READ} operations, but not {@link OperationType#WRITE} operations.
     */
    UPDATING_STATS(READ),

    /**
     * System is in state similar to {@link #AVAILABLE}, but a {@link OperationType#WRITE} operation has been executed, possibly invalidating caches.
     */
    WRITE_EXECUTED(READ, WRITE);

    private final Set<OperationType> permittedOperationTypes;

    /**
     * Constructs the {@link SystemState}.
     *
     * @param operationTypes the {@link OperationType}s permitted for the {@link SystemState}
     */
    SystemState(final OperationType... operationTypes) {
        permittedOperationTypes = operationTypes.length == 0 ? EnumSet.noneOf(OperationType.class) : EnumSet.copyOf(List.of(operationTypes));
    }

    /**
     * Checks if a {@link OperationType#READ} operation is blocked by the {@link SystemState}.
     *
     * @return {@code true} if {@link OperationType#READ} operations are blocked
     */
    public boolean isReadBlocked() {
        return !permittedOperationTypes.contains(READ);
    }

    /**
     * Checks if a {@link OperationType#WRITE} operation is blocked by the {@link SystemState}.
     *
     * @return {@code true} if {@link OperationType#WRITE} operations are blocked
     */
    public boolean isWriteBlocked() {
        return !permittedOperationTypes.contains(WRITE);
    }
}

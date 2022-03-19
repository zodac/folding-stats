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

package me.zodac.folding.api.state;

import static me.zodac.folding.api.state.OperationType.READ;
import static me.zodac.folding.api.state.OperationType.WRITE;

import java.util.EnumSet;
import java.util.List;

/**
 * The various states that the system can be in at a given time.
 */
public enum SystemState {

    /**
     * System is online and available for {@link OperationType#READ} and {@link OperationType#WRITE} operations.
     */
    AVAILABLE(READ, WRITE),

    /**
     * System is in the process of resetting stats. Since not all caches may have been wiped yet, system is not ready for {@link OperationType#READ}
     * or {@link OperationType#WRITE} operations.
     */
    RESETTING_STATS,

    /**
     * System is coming online, not yet ready for {@link OperationType#READ} or {@link OperationType#WRITE} operations.
     */
    STARTING,

    /**
     * System is in the process of updating stats (for a single {@link me.zodac.folding.api.tc.User}, or all {@link me.zodac.folding.api.tc.User}s),
     * system is ready for {@link OperationType#READ} operations, but not {@link OperationType#WRITE} operations.
     */
    UPDATING_STATS(READ),

    /**
     * System is in state similar to {@link #AVAILABLE}, but a {@link OperationType#WRITE} operation has been executed, possibly invalidating caches.
     */
    WRITE_EXECUTED(READ, WRITE);

    private final EnumSet<OperationType> permittedOperationTypes;

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

/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2022 zodac.me
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

package me.zodac.folding.api.tc.change;

import java.util.Collection;
import java.util.List;
import me.zodac.folding.api.tc.HardwareType;

/**
 * Specifies the valid states for a {@link UserChange}.
 */
public enum UserChangeState {

    /**
     * A {@link UserChange} has been requested, to be applied immediately.
     */
    REQUESTED_NOW(false),

    /**
     * A {@link UserChange} has been requested, to be applied next month.
     */
    REQUESTED_NEXT_MONTH(false),

    /**
     * A {@link UserChange} has been approved, to be applied immediately.
     */
    APPROVED_NOW(false),

    /**
     * A {@link UserChange} has been approved, to be applied next month.
     */
    APPROVED_NEXT_MONTH(false),

    /**
     * A {@link UserChange} has been rejected.
     */
    REJECTED(true),

    /**
     * {@link UserChange} has been applied to the system.
     */
    COMPLETED(true),

    /**
     * Not a valid {@link UserChangeState}.
     */
    INVALID(true);

    /**
     * {@link String} denoting all {@link UserChangeState}s should be included.
     */
    public static final String ALL_STATES = "*";

    private static final Collection<UserChangeState> ALL_VALUES = List.of(values());

    private final boolean isFinalState;

    UserChangeState(final boolean isFinalState) {
        this.isFinalState = isFinalState;
    }

    /**
     * Retrieve a {@link HardwareType} based on the input {@link String}. The search is case-insensitive.
     *
     * @param input the {@link HardwareType} as a {@link String}
     * @return the matching {@link HardwareType}, or {@link HardwareType#INVALID} if none is found
     */
    public static UserChangeState get(final String input) {
        return ALL_VALUES
            .stream()
            .filter(changeRequestState -> changeRequestState.toString().equalsIgnoreCase(input))
            .findAny()
            .orElse(INVALID);
    }

    /**
     * Retrieve all available {@link UserChangeState}s (excluding {@link UserChangeState#INVALID}).
     *
     * <p>
     * Should be used instead of {@link UserChangeState#values()}, as that recalculates the array for each call,
     * while this method uses a static {@link UserChangeState}.
     *
     * @return an unmodifiable {@link Collection} of all {@link UserChangeState}s
     */
    public static Collection<UserChangeState> getAllValues() {
        return ALL_VALUES
            .stream()
            .filter(value -> value != INVALID)
            .toList();
    }

    /**
     * Retrieve all available {@link UserChangeState}s not in {@link #isFinalState()}.
     *
     * @return a {@link Collection} of all {@link UserChangeState}s
     */
    public static Collection<UserChangeState> getOpenStates() {
        return ALL_VALUES
            .stream()
            .filter(changeRequestState -> !changeRequestState.isFinalState)
            .toList();
    }

    /**
     * Returns whether the {@link UserChangeState} is a final {@link UserChangeState} and cannot be updated.
     *
     * @return {@code true} if the {@link UserChangeState} is not permitted to be updated to another value
     */
    public boolean isFinalState() {
        return isFinalState;
    }
}
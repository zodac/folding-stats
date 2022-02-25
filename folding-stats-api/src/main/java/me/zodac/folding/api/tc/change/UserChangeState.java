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

package me.zodac.folding.api.tc.change;

import java.util.Collection;
import java.util.stream.Stream;
import me.zodac.folding.api.tc.HardwareType;

/**
 * Specifies the valid states for a {@link UserChange}.
 */
public enum UserChangeState {

    /**
     * A {@link UserChange} has been requested, to be applied immediately.
     */
    REQUESTED_NOW,

    /**
     * A {@link UserChange} has been requested, to be applied next month.
     */
    REQUESTED_NEXT_MONTH,

    /**
     * A {@link UserChange} has been approved, to be applied immediately.
     */
    APPROVED_NOW,

    /**
     * A {@link UserChange} has been approved, to be applied next month.
     */
    APPROVED_NEXT_MONTH,

    /**
     * A {@link UserChange} has been rejected.
     */
    REJECTED,

    /**
     * {@link UserChange} has been applied to the system.
     */
    COMPLETED,

    /**
     * Not a valid {@link UserChangeState}.
     */
    INVALID;

    private static final Collection<UserChangeState> ALL_VALUES = Stream.of(values())
        .filter(value -> value != INVALID)
        .toList();

    /**
     * Retrieve all available {@link UserChangeState}s (excluding {@link UserChangeState#INVALID}).
     *
     * <p>
     * Should be used instead of {@link UserChangeState#values()}, as that recalculates the array for each call,
     * while this method uses a static {@link Collection}.
     *
     * @return a {@link Collection} of all {@link UserChangeState}s
     */
    public static Collection<UserChangeState> getAllValues() {
        return ALL_VALUES;
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
            .orElse(UserChangeState.INVALID);
    }

}
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

package me.zodac.folding.api.stats;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import me.zodac.folding.api.tc.User;

/**
 * Simple POJO encapsulating the username and passkey for a Folding@Home user.
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@EqualsAndHashCode
@ToString(doNotUseGetters = true)
public class FoldingStatsDetails {

    private final String foldingUserName;
    private final String passkey;

    /**
     * Creates a {@link FoldingStatsDetails}.
     *
     * @param foldingUserName the Folding@Home username
     * @param passkey         the passkey for the user
     * @return the created {@link FoldingStatsDetails}
     */
    public static FoldingStatsDetails create(final String foldingUserName, final String passkey) {
        return new FoldingStatsDetails(foldingUserName, passkey);
    }

    /**
     * Creates a {@link FoldingStatsDetails}.
     *
     * @param user the {@link User} from which we can get the Folding@Home username and passkey
     * @return the created {@link FoldingStatsDetails}
     */
    public static FoldingStatsDetails createFromUser(final User user) {
        return create(user.getFoldingUserName(), user.getPasskey());
    }
}

/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2024 zodac.me
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

package me.zodac.folding.api.stats;

import me.zodac.folding.api.tc.User;

/**
 * Simple POJO encapsulating the username and passkey for a Folding@Home user.
 *
 * @param foldingUserName the Folding@Home username
 * @param passkey         the passkey for the user
 */
public record FoldingStatsDetails(String foldingUserName, String passkey) {

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
        return create(user.foldingUserName(), user.passkey());
    }
}

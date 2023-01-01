/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2023 zodac.me
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

import java.time.LocalDateTime;
import me.zodac.folding.api.ResponsePojo;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.util.DateTimeUtils;

/**
 * POJO defining a {@link UserChange} to request a change for a {@code Team Competition} {@link User} that must be approved/rejected by an admin.
 *
 * @param id                  the ID
 * @param createdUtcTimestamp the UTC {@link LocalDateTime} for when the {@link UserChange} was created
 * @param updatedUtcTimestamp the UTC {@link LocalDateTime} for when the {@link UserChange} was last updated
 * @param previousUser        the previous {@link User} to be updated
 * @param newUser             the {@link User} with changes to be applied
 * @param state               the {@link UserChangeState}
 */
public record UserChange(int id,
                         LocalDateTime createdUtcTimestamp,
                         LocalDateTime updatedUtcTimestamp,
                         User previousUser,
                         User newUser,
                         UserChangeState state
) implements ResponsePojo {

    private static final int EMPTY_USER_CHANGE_ID = 0;
    private static final DateTimeUtils DATE_TIME_UTILS = DateTimeUtils.create();

    /**
     * Creates a {@link UserChange}.
     *
     * <p>
     * Since the DB auto-generates the ID, this function should be used when creating a {@link UserChange} from the DB response.
     *
     * <p>
     * <b>NOTE:</b> The {@link LocalDateTime}s provided will have their nanoseconds stripped, due to precision errors that can occur after retrieving
     * a persisted value from the DB.
     *
     * @param id                  the ID
     * @param createdUtcTimestamp the UTC {@link LocalDateTime} for when the {@link UserChange} was created
     * @param updatedUtcTimestamp the UTC {@link LocalDateTime} for when the {@link UserChange} was last updated
     * @param previousUser        the previous {@link User} to be updated
     * @param newUser             the {@link User} with changes to be applied
     * @param state               the {@link UserChangeState}
     * @return the created {@link UserChange}
     */
    public static UserChange create(final int id,
                                    final LocalDateTime createdUtcTimestamp,
                                    final LocalDateTime updatedUtcTimestamp,
                                    final User previousUser,
                                    final User newUser,
                                    final UserChangeState state) {
        return new UserChange(id, createdUtcTimestamp.withNano(0), updatedUtcTimestamp.withNano(0), previousUser, newUser, state);
    }

    /**
     * Creates a {@link UserChange}, using the {@link DateTimeUtils#currentUtcLocalDateTime()}.
     *
     * <p>
     * Since the DB auto-generates the ID, this function should be used when creating a {@link UserChange} from the DB response.
     *
     * <p>
     * <b>NOTE:</b> The {@link LocalDateTime}s provided will have their nanoseconds stripped, due to precision errors that can occur after retrieving
     * a persisted value from the DB.
     *
     * @param previousUser the previous {@link User} to be updated
     * @param newUser      the {@link User} with changes to be applied
     * @param state        the {@link UserChangeState}
     * @return the created {@link UserChange}
     */
    public static UserChange createNow(final User previousUser, final User newUser, final UserChangeState state) {
        final LocalDateTime currentUtcTime = DATE_TIME_UTILS.currentUtcLocalDateTime();
        return create(EMPTY_USER_CHANGE_ID, currentUtcTime, currentUtcTime, previousUser, newUser, state);
    }

    /**
     * Updates a {@link UserChange} with the given ID.
     *
     * <p>
     * Once the {@link UserChange} has been persisted in the DB, we will know its ID. We create a new {@link UserChange} instance with this ID,
     * which can be used to retrieval/referencing later.
     *
     * <p>
     * <b>NOTE: </b> Will not update the {@code updateUtcTimestamp}, as we are only applying the ID, not the content of the {@link UserChange}.
     *
     * @param userChangeId the DB-generated ID
     * @param userChange   the {@link UserChange} to be updated with the ID
     * @return the updated {@link UserChange}
     */
    public static UserChange updateWithId(final int userChangeId, final UserChange userChange) {
        return create(
            userChangeId,
            userChange.createdUtcTimestamp,
            userChange.updatedUtcTimestamp,
            userChange.previousUser,
            userChange.newUser,
            userChange.state
        );
    }

    /**
     * Hides the {@code passkey} for the given {@link UserChange}'s {@link User}.
     *
     * <p>
     * Since we do not want {@link User} passkeys to be made available through the REST API, we hide most of the passkey, though we leave the first
     * eight (8) digits visible.
     *
     * @param userChange the {@link UserChange} whose {@link User} passkey is to be hidden
     * @return the updated {@link User}
     */
    public static UserChange hidePasskey(final UserChange userChange) {
        return create(
            userChange.id,
            userChange.createdUtcTimestamp,
            userChange.updatedUtcTimestamp,
            User.hidePasskey(userChange.previousUser),
            User.hidePasskey(userChange.newUser),
            userChange.state
        );
    }

    /**
     * Updates a {@link UserChange} with the given {@link UserChangeState}.
     *
     * @param userChangeState the new {@link UserChangeState}
     * @param userChange      the {@link UserChange} to be updated with the ID
     * @return the updated {@link UserChange}
     */
    public static UserChange updateWithState(final UserChangeState userChangeState, final UserChange userChange) {
        return create(
            userChange.id,
            userChange.createdUtcTimestamp,
            DATE_TIME_UTILS.currentUtcLocalDateTime(),
            userChange.previousUser,
            userChange.newUser,
            userChangeState
        );
    }
}

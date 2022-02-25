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

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import me.zodac.folding.api.ResponsePojo;
import me.zodac.folding.api.tc.User;

/**
 * POJO defining a {@link UserChange} to request a change for a <code>Team Competition</code> {@link User} that must be approved/rejected by an admin.
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@EqualsAndHashCode
@ToString(doNotUseGetters = true)
public class UserChange implements ResponsePojo {

    private static final int EMPTY_USER_CHANGE_ID = 0;

    final int id;
    final LocalDateTime createdUtcTimestamp;
    final LocalDateTime updatedUtcTimestamp;
    final User user;
    final UserChangeState state;

    /**
     * Creates a {@link UserChange}.
     *
     * <p>
     * Since the DB auto-generates the ID, this function should be used when creating a {@link UserChange} from the DB response.
     *
     * @param id                  the ID
     * @param createdUtcTimestamp the UTC {@link LocalDateTime} for when the {@link UserChange} was created
     * @param updatedUtcTimestamp the UTC {@link LocalDateTime} for when the {@link UserChange} was last updated
     * @param user                the {@link User} with changes to be applied
     * @param state               the {@link UserChangeState}
     * @return the created {@link UserChange}
     */
    public static UserChange create(final int id,
                                    final LocalDateTime createdUtcTimestamp,
                                    final LocalDateTime updatedUtcTimestamp,
                                    final User user,
                                    final UserChangeState state) {
        return new UserChange(id, createdUtcTimestamp.withNano(0), updatedUtcTimestamp.withNano(0), user, state);
    }

    /**
     * Creates a {@link UserChange}.
     *
     * <p>
     * Since we do not know the ID until the DB has persisted the {@link User}, the {@link #EMPTY_USER_CHANGE_ID} will be used instead.
     *
     * @param createdUtcTimestamp the UTC {@link LocalDateTime} for when the {@link UserChange} was created
     * @param updatedUtcTimestamp the UTC {@link LocalDateTime} for when the {@link UserChange} was last updated
     * @param user                the {@link User} with changes to be applied
     * @param state               the {@link UserChangeState}
     * @return the created {@link UserChange}
     */
    public static UserChange createWithoutId(final LocalDateTime createdUtcTimestamp,
                                             final LocalDateTime updatedUtcTimestamp,
                                             final User user,
                                             final UserChangeState state) {
        return new UserChange(EMPTY_USER_CHANGE_ID, createdUtcTimestamp.withNano(0), updatedUtcTimestamp.withNano(0), user, state);
    }
}
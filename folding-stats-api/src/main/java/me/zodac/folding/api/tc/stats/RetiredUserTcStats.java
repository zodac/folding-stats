/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2025 zodac.me
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

package me.zodac.folding.api.tc.stats;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.util.StringUtils;

/**
 * POJO that extends {@link UserTcStats} adding a retired user ID, team ID and a display name.When a
 * {@link me.zodac.folding.api.tc.User} is deleted, their stats are stored for the remainder of the {@code Team Competition}
 * period for their team.
 */
@Accessors(fluent = true)
@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(doNotUseGetters = true, callSuper = true)
public class RetiredUserTcStats extends UserTcStats {

    /**
     * The default {@link RetiredUserTcStats} ID. We may not know the ID at the time of object creation, so we use this and update the ID later.
     */
    public static final int EMPTY_RETIRED_USER_ID = 0;

    private final int retiredUserId;
    private final int teamId;
    private final String displayName;

    /**
     * Constructor for {@link RetiredUserTcStats}.
     *
     * @param retiredUserId the retired user ID
     * @param teamId        the ID of the {@link Team} the stats will be contributing to
     * @param displayName   the {@link User}'s display name for the points
     * @param userTcStats   the {@link UserTcStats}
     */
    protected RetiredUserTcStats(final int retiredUserId, final int teamId, final String displayName, final UserTcStats userTcStats) {
        super(userTcStats.userId(), userTcStats.timestamp(), userTcStats.points(), userTcStats.multipliedPoints(), userTcStats.units());
        this.retiredUserId = retiredUserId;
        this.teamId = teamId;
        this.displayName = displayName;
    }

    /**
     * Creates an instance of {@link RetiredUserTcStats} for a {@link User} within a {@link Team}.
     *
     * <p>
     * Since the DB auto-generates the ID, this function should be used when creating a {@link RetiredUserTcStats} from the DB response.
     *
     * @param retiredUserId  the retired user ID
     * @param teamId         the ID of the {@link Team} the stats will be contributing to
     * @param displayName    the {@link User}'s display name for the points
     * @param retiredTcStats the {@link UserTcStats}
     * @return the created {@link RetiredUserTcStats}
     * @throws IllegalArgumentException thrown if {@code displayName} {@link StringUtils#isBlank(String)}
     */
    public static RetiredUserTcStats create(final int retiredUserId, final int teamId, final String displayName, final UserTcStats retiredTcStats) {
        if (StringUtils.isBlank(displayName)) {
            throw new IllegalArgumentException("'displayName' must not be null or blank");
        }

        return new RetiredUserTcStats(retiredUserId, teamId, displayName, retiredTcStats);
    }

    /**
     * Creates an instance of {@link RetiredUserTcStats} for a {@link User} within a {@link Team}.
     *
     * <p>
     * Since we do not know the ID until the DB has persisted the {@link RetiredUserTcStats}, the {@link #EMPTY_RETIRED_USER_ID} will be used instead.
     *
     * @param teamId         the ID of the {@link Team} the stats will be contributing to
     * @param displayName    the {@link User}'s display name for the points
     * @param retiredTcStats the {@link UserTcStats}
     * @return the created {@link RetiredUserTcStats}
     * @throws IllegalArgumentException thrown if {@code displayName} {@link StringUtils#isBlank(String)}
     */
    public static RetiredUserTcStats createWithoutId(final int teamId, final String displayName, final UserTcStats retiredTcStats) {
        return create(EMPTY_RETIRED_USER_ID, teamId, displayName, retiredTcStats);
    }

    /**
     * Creates an instance of {@link RetiredUserTcStats} for a {@link User} within a {@link Team}.
     *
     * <p>
     * Since we do not know the ID until the DB has persisted the {@link RetiredUserTcStats}, the {@link #EMPTY_RETIRED_USER_ID} will be used instead.
     *
     * @param user           the {@link User} being retired
     * @param retiredTcStats the {@link UserTcStats}
     * @return the created {@link RetiredUserTcStats}
     * @throws IllegalArgumentException thrown if {@code displayName} {@link StringUtils#isBlank(String)}
     */
    public static RetiredUserTcStats createWithoutId(final User user, final UserTcStats retiredTcStats) {
        return create(EMPTY_RETIRED_USER_ID, user.team().id(), user.displayName(), retiredTcStats);
    }

    /**
     * Updates a {@link RetiredUserTcStats} with the given ID.
     *
     * <p>
     * Once the {@link RetiredUserTcStats} has been persisted in the DB, we will know its ID. We create a new {@link RetiredUserTcStats} instance with
     * this ID, which can be used to retrieval/referencing later.
     *
     * @param retiredUserId      the DB-generated ID
     * @param retiredUserTcStats the {@link RetiredUserTcStats} to be updated with the ID
     * @return the updated {@link RetiredUserTcStats}
     * @throws IllegalArgumentException thrown if {@code displayName} {@link StringUtils#isBlank(String)}
     */
    public static RetiredUserTcStats updateWithId(final int retiredUserId, final RetiredUserTcStats retiredUserTcStats) {
        return create(
            retiredUserId,
            retiredUserTcStats.teamId,
            retiredUserTcStats.displayName,
            retiredUserTcStats
        );
    }
}

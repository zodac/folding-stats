package me.zodac.folding.api.tc.stats;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;

/**
 * POJO that extends {@link UserTcStats} adding a retired user ID, team ID and a display name.When a
 * {@link me.zodac.folding.api.tc.User} is deleted, their stats are stored for the remainder of the <code>Team Competition</code>
 * period for their team.
 */
@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(doNotUseGetters = true, callSuper = true)
public class RetiredUserTcStats extends UserTcStats {

    private static final String DEFAULT_DISPLAY_NAME = "retiredUser";
    private static final int EMPTY_RETIRED_USER_ID = 0;
    private static final int EMPTY_TEAM_ID = 0;

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
        super(userTcStats.getUserId(), userTcStats.getTimestamp(), userTcStats.getPoints(), userTcStats.getMultipliedPoints(),
            userTcStats.getUnits());
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
     */
    public static RetiredUserTcStats create(final int retiredUserId, final int teamId, final String displayName, final UserTcStats retiredTcStats) {
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
     */
    public static RetiredUserTcStats createWithoutId(final int teamId, final String displayName, final UserTcStats retiredTcStats) {
        return create(EMPTY_RETIRED_USER_ID, teamId, displayName, retiredTcStats);
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
     */
    public static RetiredUserTcStats updateWithId(final int retiredUserId, final RetiredUserTcStats retiredUserTcStats) {
        return create(
            retiredUserId,
            retiredUserTcStats.teamId,
            retiredUserTcStats.displayName,
            retiredUserTcStats
        );
    }

    /**
     * Creates an empty instance of {@link RetiredUserTcStats}, with no values.
     *
     * @return the empty {@link RetiredUserTcStats}
     */
    public static RetiredUserTcStats empty() {
        return create(EMPTY_RETIRED_USER_ID, EMPTY_TEAM_ID, DEFAULT_DISPLAY_NAME, UserTcStats.empty());
    }
}
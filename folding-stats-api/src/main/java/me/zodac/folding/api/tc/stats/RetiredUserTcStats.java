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
     * Creates an empty instance of {@link RetiredUserTcStats}, with no values.
     *
     * @return the empty {@link RetiredUserTcStats}
     */
    public static RetiredUserTcStats empty() {
        return create(User.EMPTY_USER_ID, Team.EMPTY_TEAM_ID, "retiredUser", UserTcStats.empty());
    }
}
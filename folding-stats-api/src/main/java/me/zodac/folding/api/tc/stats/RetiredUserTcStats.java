package me.zodac.folding.api.tc.stats;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * When a {@link me.zodac.folding.api.tc.User} is deleted, their stats are stored for the remainder of the <code>Team Competition</code>
 * period for their team.
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@EqualsAndHashCode
@ToString(doNotUseGetters = true)
public class RetiredUserTcStats {

    private final int retiredUserId;
    private final int teamId;
    private final String displayUserName;

    @Getter(AccessLevel.NONE)
    private final UserTcStats userTcStats;

    /**
     * Creates an instance of {@link RetiredUserTcStats} for a {@link me.zodac.folding.api.tc.User} within a {@link me.zodac.folding.api.tc.Team}.
     *
     * @param retiredUserId      the retired user ID
     * @param teamId             the ID of the team the stats will be contributing to
     * @param displayUserName    the display name for the points
     * @param retiredUserTcStats the {@link UserTcStats} stats
     * @return the created {@link RetiredUserTcStats}
     */
    public static RetiredUserTcStats create(final int retiredUserId, final int teamId, final String displayUserName, final UserTcStats retiredUserTcStats) {
        return new RetiredUserTcStats(retiredUserId, teamId, displayUserName, retiredUserTcStats);
    }

    public int getUserId() {
        return userTcStats.getUserId();
    }

    public long getPoints() {
        return userTcStats.getPoints();
    }

    public long getMultipliedPoints() {
        return userTcStats.getMultipliedPoints();
    }

    public int getUnits() {
        return userTcStats.getUnits();
    }
}
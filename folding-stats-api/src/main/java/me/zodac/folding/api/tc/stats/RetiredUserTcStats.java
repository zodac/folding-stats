package me.zodac.folding.api.tc.stats;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

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
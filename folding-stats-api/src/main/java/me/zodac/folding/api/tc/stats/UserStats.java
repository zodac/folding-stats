package me.zodac.folding.api.tc.stats;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.utils.DateTimeUtils;

import java.sql.Timestamp;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
@Getter
@ToString(doNotUseGetters = true)
public class UserStats {

    private final int userId;
    private final Timestamp timestamp;
    private final Stats stats;

    public static UserStats create(final int userId, final Timestamp timestamp, final Stats totalStats) {
        return new UserStats(userId, timestamp, totalStats);
    }

    public static UserStats empty() {
        return new UserStats(User.EMPTY_USER_ID, DateTimeUtils.currentUtcTimestamp(), Stats.empty());
    }

    public boolean isEmpty() {
        return userId == 0 && stats.isEmpty();
    }

    public long getPoints() {
        return stats.getPoints();
    }

    public int getUnits() {
        return stats.getUnits();
    }
}
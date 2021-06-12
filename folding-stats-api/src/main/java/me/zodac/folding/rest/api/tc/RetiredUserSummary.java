package me.zodac.folding.rest.api.tc;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import me.zodac.folding.api.tc.stats.RetiredUserTcStats;

/**
 * Summary of the stats of a retired {@link me.zodac.folding.api.tc.User} in the <code>Team Competition</code>.
 */
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Setter
@EqualsAndHashCode
@ToString(doNotUseGetters = true)
public class RetiredUserSummary {

    private static final int DEFAULT_USER_RANK = 0;

    private int id;
    private String displayName;

    private long points;
    private long multipliedPoints;
    private int units;
    private int rankInTeam;

    /**
     * Creates a {@link RetiredUserSummary}, summarising the stats for a now retired {@link me.zodac.folding.api.tc.User}.
     *
     * <p>
     * The {@link RetiredUserSummary} is not ranked to begin with, since it is not aware of the other
     * {@link RetiredUserSummary}s or {@link UserSummary}s. The rank can be updated later using
     * {@link RetiredUserSummary#updateWithRankInTeam(RetiredUserSummary, int)}.
     *
     * @param retiredUserTcStats the {@link RetiredUserTcStats} for the {@link me.zodac.folding.api.tc.User}
     * @return the created {@link RetiredUserSummary}
     */
    public static RetiredUserSummary create(final RetiredUserTcStats retiredUserTcStats) {
        return new RetiredUserSummary(retiredUserTcStats.getUserId(), retiredUserTcStats.getDisplayName(), retiredUserTcStats.getPoints(),
            retiredUserTcStats.getMultipliedPoints(), retiredUserTcStats.getUnits(), DEFAULT_USER_RANK);
    }

    /**
     * Updates a {@link RetiredUserSummary} with a rank, after it has been calculated.
     *
     * @param retiredUserSummary the {@link RetiredUserSummary} to update
     * @param rankInTeam         the rank within the {@link TeamSummary}
     * @return the updated {@link RetiredUserSummary}
     */
    public static RetiredUserSummary updateWithRankInTeam(final RetiredUserSummary retiredUserSummary, final int rankInTeam) {
        return new RetiredUserSummary(retiredUserSummary.id, retiredUserSummary.displayName, retiredUserSummary.points,
            retiredUserSummary.multipliedPoints, retiredUserSummary.units, rankInTeam);
    }
}
package me.zodac.folding.rest.api.tc;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import me.zodac.folding.api.tc.Category;
import me.zodac.folding.api.tc.Hardware;

/**
 * Summary of the stats of an active {@link me.zodac.folding.api.tc.User} in the <code>Team Competition</code>.
 */
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Setter
@EqualsAndHashCode
@ToString(doNotUseGetters = true)
public class UserSummary {

    private static final int DEFAULT_RANK = 0;
    private static final long DEFAULT_POINTS = 0L;
    private static final long DEFAULT_MULTIPLIED_POINTS = 0L;
    private static final int DEFAULT_UNITS = 0;

    private int id;
    private String displayName;
    private String foldingName;
    private Hardware hardware;
    private String category;
    private String profileLink;
    private String liveStatsLink;

    private long points;
    private long multipliedPoints;
    private int units;
    private int rankInTeam;

    /**
     * Creates a {@link UserSummary}, summarising the stats for an active {@link me.zodac.folding.api.tc.User}.
     *
     * @param id               the ID of the {@link me.zodac.folding.api.tc.User}
     * @param displayName      the display name of the {@link me.zodac.folding.api.tc.User}
     * @param foldingName      the Folding@Home username of the {@link me.zodac.folding.api.tc.User}
     * @param hardware         the {@link Hardware} used by the the {@link me.zodac.folding.api.tc.User}
     * @param category         the {@link Category} of the {@link me.zodac.folding.api.tc.User}
     * @param profileLink      the ID of the {@link me.zodac.folding.api.tc.User}
     * @param liveStatsLink    the ID of the {@link me.zodac.folding.api.tc.User}
     * @param points           the points of the {@link me.zodac.folding.api.tc.User}
     * @param multipliedPoints the multiplied points of the {@link me.zodac.folding.api.tc.User}
     * @param units            the units of the {@link me.zodac.folding.api.tc.User}
     * @param rankInTeam       the rank of the {@link me.zodac.folding.api.tc.User} in thit {@link me.zodac.folding.api.tc.Team}
     * @return the created {@link UserSummary}
     */
    public static UserSummary create(final int id,
                                     final String displayName,
                                     final String foldingName,
                                     final Hardware hardware,
                                     final Category category,
                                     final String profileLink,
                                     final String liveStatsLink,
                                     final long points,
                                     final long multipliedPoints,
                                     final int units,
                                     final int rankInTeam) {
        return new UserSummary(id, displayName, foldingName, hardware, category.toString(), profileLink, liveStatsLink, points, multipliedPoints,
            units, rankInTeam);
    }

    /**
     * Creates a {@link UserSummary}, summarising the stats for an active {@link me.zodac.folding.api.tc.User}.
     *
     * <p>
     * The {@link UserSummary} is not ranked to begin with, since it is not aware of the other {@link UserSummary}s.
     * The rank can be updated later using {@link UserSummary#updateWithRankInTeam(UserSummary, int)}.
     *
     * @param id               the ID of the {@link me.zodac.folding.api.tc.User}
     * @param displayName      the display name of the {@link me.zodac.folding.api.tc.User}
     * @param foldingName      the Folding@Home username of the {@link me.zodac.folding.api.tc.User}
     * @param hardware         the {@link Hardware} used by the the {@link me.zodac.folding.api.tc.User}
     * @param category         the {@link Category} of the {@link me.zodac.folding.api.tc.User}
     * @param profileLink      the ID of the {@link me.zodac.folding.api.tc.User}
     * @param liveStatsLink    the ID of the {@link me.zodac.folding.api.tc.User}
     * @param points           the points of the {@link me.zodac.folding.api.tc.User}
     * @param multipliedPoints the multiplied points of the {@link me.zodac.folding.api.tc.User}
     * @param units            the units of the {@link me.zodac.folding.api.tc.User}
     * @return the created {@link UserSummary}
     */
    // TODO: [zodac] Use User here?
    public static UserSummary createWithDefaultRank(final int id,
                                                    final String displayName,
                                                    final String foldingName,
                                                    final Hardware hardware,
                                                    final Category category,
                                                    final String profileLink,
                                                    final String liveStatsLink,
                                                    final long points,
                                                    final long multipliedPoints,
                                                    final int units) {
        return create(id, displayName, foldingName, hardware, category, profileLink, liveStatsLink, points, multipliedPoints, units, DEFAULT_RANK);
    }

    /**
     * Updates a {@link UserSummary} with a rank, after it has been calculated.
     *
     * @param userSummary the {@link UserSummary} to update
     * @param rankInTeam  the rank within the {@link TeamSummary}
     * @return the updated {@link UserSummary}
     */
    public static UserSummary updateWithRankInTeam(final UserSummary userSummary, final int rankInTeam) {
        return create(userSummary.id, userSummary.displayName, userSummary.foldingName, userSummary.hardware, Category.get(userSummary.category),
            userSummary.profileLink, userSummary.liveStatsLink, userSummary.points, userSummary.multipliedPoints, userSummary.units, rankInTeam);
    }
}
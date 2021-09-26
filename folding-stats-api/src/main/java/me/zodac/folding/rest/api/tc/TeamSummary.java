package me.zodac.folding.rest.api.tc;

import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Summary of the stats of a {@link me.zodac.folding.api.tc.Team}s and its {@link me.zodac.folding.api.tc.User}s in
 * the <code>Team Competition</code>.
 */
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Setter
@EqualsAndHashCode
@ToString(doNotUseGetters = true)
public class TeamSummary {

    private static final int DEFAULT_TEAM_RANK = 0;

    private String teamName;
    private String teamDescription;
    private String forumLink;
    private String captainName;

    private long teamPoints;
    private long teamMultipliedPoints;
    private int teamUnits;
    private int rank; // Rank in 'division', but we only have one division so no need to be more explicit with the name

    private Collection<UserSummary> activeUsers;
    private Collection<RetiredUserSummary> retiredUsers;

    /**
     * Creates a {@link TeamSummary}, using a {@link Collection} of {@link UserSummary}s and {@link RetiredUserSummary}.
     *
     * <p>
     * The {@link TeamSummary} is not ranked to begin with, since it is not aware of the other {@link TeamSummary}s. The
     * rank can be updated later using {@link TeamSummary#updateWithRank(TeamSummary, int)}.
     *
     * <p>
     * The points, multiplied points and units from each {@link UserSummary} and {@link RetiredUserSummary} are added
     * up to give the total team points, multiplied points and units.
     *
     * <p>
     * <b>NOTE:</b> The {@link RetiredUserSummary}s will always be ranked after the {@link UserSummary}s, to highlight
     * the active {@link me.zodac.folding.api.tc.User}s.
     *
     * @param teamName        the name of the {@link me.zodac.folding.api.tc.Team}
     * @param teamDescription the description of the {@link me.zodac.folding.api.tc.Team}
     * @param forumLink       the URL to the forum thread for the {@link me.zodac.folding.api.tc.Team}
     * @param captainName     the captain's display name for the {@link me.zodac.folding.api.tc.Team}, or null if no captain
     * @param activeUsers     the active {@link me.zodac.folding.api.tc.User} {@link UserSummary}s
     * @param retiredUsers    the retired {@link me.zodac.folding.api.tc.User} {@link RetiredUserSummary}
     * @return the created {@link TeamSummary}
     */
    public static TeamSummary createWithDefaultRank(final String teamName,
                                                    final String teamDescription,
                                                    final String forumLink,
                                                    final String captainName,
                                                    final Collection<UserSummary> activeUsers,
                                                    final Collection<RetiredUserSummary> retiredUsers) {
        int teamUnits = 0;
        long teamPoints = 0L;
        long teamMultipliedPoints = 0L;

        for (final UserSummary activeUser : activeUsers) {
            teamUnits += activeUser.getUnits();
            teamPoints += activeUser.getPoints();
            teamMultipliedPoints += activeUser.getMultipliedPoints();
        }

        for (final RetiredUserSummary retired : retiredUsers) {
            teamUnits += retired.getUnits();
            teamPoints += retired.getPoints();
            teamMultipliedPoints += retired.getMultipliedPoints();
        }

        final List<UserSummary> rankedActiveUsers = activeUsers
            .stream()
            .sorted(Comparator.comparingLong(UserSummary::getMultipliedPoints).reversed())
            .collect(new IntegerRankingCollector<>(
                Comparator.comparingLong(UserSummary::getMultipliedPoints),
                UserSummary::getRankInTeam,
                UserSummary::updateWithRankInTeam)
            );

        final List<RetiredUserSummary> rankedRetiredUsers = retiredUsers
            .stream()
            .sorted(Comparator.comparingLong(RetiredUserSummary::getMultipliedPoints).reversed())
            .collect(new IntegerRankingCollector<>(
                Comparator.comparingLong(RetiredUserSummary::getMultipliedPoints),
                RetiredUserSummary::getRankInTeam,
                RetiredUserSummary::updateWithRankInTeam)
            )
            // We need to offset the ranks of the retired users, so they are ranked below active users
            // Annoyingly, we cannot simply add an offset to the #updateWithRankInTeam call above, since the collector applies it multiple times
            // Instead, we now iterate over the retired users again and manually offset them
            .stream()
            .map(rankedRetiredUser -> RetiredUserSummary
                .updateWithRankInTeam(rankedRetiredUser, rankedRetiredUser.getRankInTeam() + activeUsers.size()))
            .collect(toList());

        // Not ranked to begin with, will be updated by the calling class
        return new TeamSummary(teamName, teamDescription, forumLink, captainName, teamPoints, teamMultipliedPoints, teamUnits, DEFAULT_TEAM_RANK,
            rankedActiveUsers, rankedRetiredUsers);
    }

    /**
     * Updates a {@link TeamSummary} with a rank, after it has been calculated.
     *
     * @param teamSummary the {@link TeamSummary} to update
     * @param rank        the rank
     * @return the updated {@link TeamSummary}
     */
    public static TeamSummary updateWithRank(final TeamSummary teamSummary, final int rank) {
        return new TeamSummary(teamSummary.teamName, teamSummary.teamDescription, teamSummary.forumLink, teamSummary.captainName,
            teamSummary.teamPoints, teamSummary.teamMultipliedPoints, teamSummary.teamUnits, rank, teamSummary.activeUsers, teamSummary.retiredUsers);
    }
}
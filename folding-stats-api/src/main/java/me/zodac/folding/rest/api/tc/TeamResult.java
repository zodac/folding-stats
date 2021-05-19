package me.zodac.folding.rest.api.tc;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import static java.util.stream.Collectors.toList;

@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Setter
@EqualsAndHashCode
@ToString(doNotUseGetters = true)
public class TeamResult {

    private static final int DEFAULT_TEAM_RANK = 0;

    private String teamName;
    private String teamDescription;
    private String forumLink;
    private String captainName;

    private long teamPoints;
    private long teamMultipliedPoints;
    private int teamUnits;
    private int rank; // Rank in 'division', but we only have one division so no need to be more explicit, yet
    private Collection<UserResult> activeUsers;
    private Collection<UserResult> retiredUsers;

    public static TeamResult create(final String teamName, final String teamDescription, final String forumLink, final String captainName, final List<UserResult> activeUsers, final List<UserResult> retiredUsers) {
        int teamUnits = 0;
        long teamPoints = 0L;
        long teamMultipliedPoints = 0L;


        for (final UserResult activeUser : activeUsers) {
            teamUnits += activeUser.getUnits();
            teamPoints += activeUser.getPoints();
            teamMultipliedPoints += activeUser.getMultipliedPoints();
        }

        for (final UserResult retired : retiredUsers) {
            teamUnits += retired.getUnits();
            teamPoints += retired.getPoints();
            teamMultipliedPoints += retired.getMultipliedPoints();
        }

        final List<UserResult> rankedActiveUsers = activeUsers
                .stream()
                .sorted(Comparator.comparingLong(UserResult::getMultipliedPoints).reversed())
                .collect(new IntegerRankingCollector<>(
                        Comparator.comparingLong(UserResult::getMultipliedPoints),
                        UserResult::getRankInTeam,
                        UserResult::updateWithRankInTeam)
                );

        final List<UserResult> rankedRetiredUsers = retiredUsers
                .stream()
                .sorted(Comparator.comparingLong(UserResult::getMultipliedPoints).reversed())
                .collect(new IntegerRankingCollector<>(
                        Comparator.comparingLong(UserResult::getMultipliedPoints),
                        UserResult::getRankInTeam,
                        UserResult::updateWithRankInTeam)
                )
                // We need to offset the ranks of the retired users, so they are ranked below active users
                // Annoyingly, we cannot simply add an offset to the #updateWithRankInTeam call above, since the collector applies it multiple times
                // Instead, we now iterate over the retired users again and manually offset them
                .stream()
                .map(rankedRetiredUser -> UserResult.updateWithRankInTeam(rankedRetiredUser, rankedRetiredUser.getRankInTeam() + activeUsers.size()))
                .collect(toList());

        // Not ranked to begin with, will be updated by the calling class
        return new TeamResult(teamName, teamDescription, forumLink, captainName, teamPoints, teamMultipliedPoints, teamUnits, DEFAULT_TEAM_RANK, rankedActiveUsers, rankedRetiredUsers);
    }


    public static TeamResult updateWithRank(final TeamResult teamResult, final int rank) {
        return new TeamResult(teamResult.teamName, teamResult.teamDescription, teamResult.forumLink, teamResult.captainName, teamResult.teamPoints, teamResult.teamMultipliedPoints, teamResult.teamUnits, rank, teamResult.activeUsers, teamResult.retiredUsers);
    }
}
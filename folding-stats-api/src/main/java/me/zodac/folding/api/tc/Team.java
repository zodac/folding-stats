package me.zodac.folding.api.tc;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import me.zodac.folding.api.Identifiable;

import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Setter
@EqualsAndHashCode
@ToString(doNotUseGetters = true)
public class Team implements Identifiable {
    
    private int id;
    private String teamName;
    private String teamDescription;
    private int captainUserId;
    private Set<Integer> userIds = new HashSet<>(0);
    private Set<Integer> retiredUserIds = new HashSet<>(0);

    public static Team create(final int id, final String teamName, final String teamDescription, final int captainUserId, final Set<Integer> userIds, final Set<Integer> retiredUserStats) {
        return new Team(id, teamName, teamDescription, captainUserId, userIds, retiredUserStats);
    }

    public static Team updateWithId(final int teamId, final Team team) {
        return new Team(teamId, team.teamName, team.teamDescription, team.captainUserId, team.userIds, team.retiredUserIds);
    }

    public static Team retireUser(final Team team, final int userId, final int retiredUserId) {
        final Set<Integer> updateUserIds = new HashSet<>(team.getUserIds());
        updateUserIds.remove(userId);

        final Set<Integer> retiredUserStats = new HashSet<>(team.getRetiredUserIds());
        retiredUserStats.add(retiredUserId);

        return new Team(team.id, team.teamName, team.teamDescription, team.captainUserId, updateUserIds, retiredUserStats);
    }

    public static Team unretireUser(final Team team, final int userId, final int retiredUserId) {
        final Set<Integer> updateUserIds = new HashSet<>(team.getUserIds());
        updateUserIds.add(userId);

        final Set<Integer> retiredUserStats = new HashSet<>(team.getRetiredUserIds());
        retiredUserStats.remove(retiredUserId);

        return new Team(team.id, team.teamName, team.teamDescription, team.captainUserId, updateUserIds, retiredUserStats);
    }
}

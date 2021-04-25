package me.zodac.folding.api.tc;

import me.zodac.folding.api.Identifiable;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Team implements Identifiable {

    private static final long serialVersionUID = -8765213859473081036L;

    private int id;
    private String teamName;
    private String teamDescription;
    private int captainUserId;
    private Set<Integer> userIds = new HashSet<>(0);
    private Set<Integer> retiredUserIds = new HashSet<>(0);

    public Team() {

    }


    public Team(final int id, final String teamName, final String teamDescription, final int captainUserId, final Set<Integer> userIds, final Set<Integer> retiredUserIds) {
        this.id = id;
        this.teamName = teamName;
        this.teamDescription = teamDescription;
        this.captainUserId = captainUserId;
        this.userIds = userIds;
        this.retiredUserIds = retiredUserIds;
    }

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

    @Override
    public int getId() {
        return id;
    }

    public void setId(final int id) {
        this.id = id;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(final String teamName) {
        this.teamName = teamName == null ? "" : teamName.trim();
    }

    public String getTeamDescription() {
        return teamDescription;
    }

    public void setTeamDescription(final String teamDescription) {
        this.teamDescription = teamDescription == null ? "" : teamDescription.trim();
    }

    public int getCaptainUserId() {
        return captainUserId;
    }

    public void setCaptainUserId(final int captainUserId) {
        this.captainUserId = captainUserId;
    }

    public Set<Integer> getUserIds() {
        return userIds;
    }

    public void setUserIds(final Set<Integer> userIds) {
        this.userIds = userIds;
    }

    public Set<Integer> getRetiredUserIds() {
        return retiredUserIds;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Team team = (Team) o;
        return id == team.id && captainUserId == team.captainUserId && Objects.equals(teamName, team.teamName) && Objects.equals(teamDescription, team.teamDescription) && Objects.equals(userIds, team.userIds) && Objects.equals(retiredUserIds, team.retiredUserIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, teamName, teamDescription, captainUserId, userIds, retiredUserIds);
    }


    @Override
    public String toString() {
        return "Team::{" +
                "id: " + id +
                ", teamName: '" + teamName + "'" +
                ", teamDescription: '" + teamDescription + "'" +
                ", captainUserId: " + captainUserId +
                ", userIds: " + userIds +
                ", retiredUserIds: " + retiredUserIds +
                '}';
    }
}

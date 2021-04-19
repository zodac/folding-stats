package me.zodac.folding.api.tc;

import me.zodac.folding.api.Identifiable;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class Team implements Identifiable {

    private static final long serialVersionUID = -8765213859473081036L;

    private int id;
    private String teamName;
    private String teamDescription;
    private int captainUserId;
    private Set<Integer> userIds;

    public Team() {

    }


    public Team(final int id, final String teamName, final String teamDescription, final int captainUserId, final Set<Integer> userIds) {
        this.id = id;
        this.teamName = teamName;
        this.teamDescription = teamDescription;
        this.captainUserId = captainUserId;
        this.userIds = userIds;
    }

    public static class Builder {

        private final String teamName;
        private final Set<Integer> userIds = new HashSet<>();

        private int teamId = 0;
        private int captainUserId = 0;
        private String teamDescription = "";


        public Builder(final String teamName) {
            this.teamName = teamName;
        }

        public Builder teamId(final int id) {
            this.teamId = id;
            return this;
        }

        public Builder teamDescription(final String teamDescription) {
            this.teamDescription = teamDescription;
            return this;
        }

        public Builder captainUserId(final int captainUserId) {
            this.captainUserId = captainUserId;
            userIds.add(captainUserId);
            return this;
        }

        public Builder userId(final int userId) {
            this.userIds.add(userId);
            return this;
        }

        public Builder userIds(final List<Integer> userIds) {
            this.userIds.addAll(userIds);
            return this;
        }

        public Team createTeam() {
            return new Team(teamId, teamName, teamDescription, captainUserId, userIds);
        }
    }

    public static Team updateWithId(final int teamId, final Team team) {
        return new Team(teamId, team.getTeamName(), team.getTeamDescription(), team.getCaptainUserId(), team.getUserIds());
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
        this.teamName = teamName;
    }

    public String getTeamDescription() {
        return teamDescription;
    }

    public void setTeamDescription(final String teamDescription) {
        this.teamDescription = teamDescription;
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

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Team team = (Team) o;
        return id == team.id && captainUserId == team.captainUserId && Objects.equals(teamName, team.teamName) && Objects.equals(teamDescription, team.teamDescription) && Objects.equals(userIds, team.userIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, teamName, teamDescription, captainUserId, userIds);
    }


    @Override
    public String toString() {
        return "Team::{" +
                "id: " + id +
                ", teamName: '" + teamName + "'" +
                ", teamDescription: '" + teamDescription + "'" +
                ", captainUserId: " + captainUserId +
                ", userIds: " + userIds +
                '}';
    }
}

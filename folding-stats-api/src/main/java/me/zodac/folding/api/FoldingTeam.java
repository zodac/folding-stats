package me.zodac.folding.api;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static me.zodac.folding.api.util.StringUtils.isNotBlank;

public class FoldingTeam implements ObjectWithId, Serializable {

    public static final int EMPTY_POSITION = 0;

    private static final long serialVersionUID = -8765213859473081036L;


    private int id;
    private String teamName;
    private int captainUserId;
    private int nvidiaGpuUserId;
    private int amdGpuUserId;
    private int wildcardUserId;


    public FoldingTeam() {

    }

    public FoldingTeam(final int id, final String teamName, final int captainUserId, final int nvidiaGpuUserId, final int amdGpuUserId, final int wildcardUserId) {
        this.id = id;
        this.teamName = teamName;
        this.captainUserId = captainUserId;
        this.nvidiaGpuUserId = nvidiaGpuUserId;
        this.amdGpuUserId = amdGpuUserId;
        this.wildcardUserId = wildcardUserId;
    }

    public static FoldingTeam create(final int teamId, final String teamName, final int captainUserId, final int nvidiaGpuUserId, final int amdGpuUserId, final int wildcardUserId) {
        return new FoldingTeam(teamId, teamName, captainUserId, nvidiaGpuUserId, amdGpuUserId, wildcardUserId);
    }

    public static FoldingTeam createWithoutId(final String teamName, final int captainUserId, final int nvidiaGpuUserId, final int amdGpuUserId, final int wildcardUserId) {
        return new FoldingTeam(0, teamName, captainUserId, nvidiaGpuUserId, amdGpuUserId, wildcardUserId);
    }

    public static FoldingTeam updateWithId(final int teamId, final FoldingTeam foldingTeam) {
        return new FoldingTeam(teamId, foldingTeam.getTeamName(), foldingTeam.getCaptainUserId(), foldingTeam.getNvidiaGpuUserId(), foldingTeam.getAmdGpuUserId(), foldingTeam.getWildcardUserId());
    }

    // Quick function used for REST requests. Since a JSON payload may have a missing/incorrect field, we need to check
    // User IDs less than 0 are invalid, but an ID of 0 means the position is empty
    // TODO: [zodac] Verify the user IDs against the user cache
    public boolean isValid() {
        return isNotBlank(teamName) && captainUserId > EMPTY_POSITION && usersAreUniqueOrEmpty(nvidiaGpuUserId, amdGpuUserId, wildcardUserId);
    }


    // TODO: [zodac] I'm aware this is probably shit, I'll get to it later. Maybe. Maths sucks.
    private static boolean usersAreUniqueOrEmpty(final int... userIds) {
        final List<Integer> validUsers = new ArrayList<>(userIds.length);

        for (final int userId : userIds) {
            if (userId > EMPTY_POSITION) {
                validUsers.add(userId);
            }
        }

        // No position filled, invalid team
        if (validUsers.isEmpty()) {
            return false;
        }

        // Team has at least one valid user, now confirm the same userId isn't being used multiple times
        final Set<Integer> uniqueValidUsers = new HashSet<>(validUsers);

        // If the list and set are not equal in size, then at least one duplicate userId exists
        // Could possibly return the duplicate user IDs, help the 400 HTTP response
        return validUsers.size() == uniqueValidUsers.size();
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

    public int getCaptainUserId() {
        return captainUserId;
    }

    public void setCaptainUserId(final int captainUserId) {
        this.captainUserId = captainUserId;
    }

    public int getNvidiaGpuUserId() {
        return nvidiaGpuUserId;
    }

    public void setNvidiaGpuUserId(final int nvidiaGpuUserId) {
        this.nvidiaGpuUserId = nvidiaGpuUserId;
    }

    public int getAmdGpuUserId() {
        return amdGpuUserId;
    }

    public void setAmdGpuUserId(final int amdGpuUserId) {
        this.amdGpuUserId = amdGpuUserId;
    }

    public int getWildcardUserId() {
        return wildcardUserId;
    }

    public void setWildcardUserId(final int wildcardUserId) {
        this.wildcardUserId = wildcardUserId;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final FoldingTeam foldingTeam = (FoldingTeam) o;
        return id == foldingTeam.id && Objects.equals(teamName, foldingTeam.teamName) && Objects.equals(captainUserId, foldingTeam.captainUserId) && Objects.equals(nvidiaGpuUserId, foldingTeam.nvidiaGpuUserId) && Objects.equals(amdGpuUserId, foldingTeam.amdGpuUserId) && Objects.equals(wildcardUserId, foldingTeam.wildcardUserId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, teamName, captainUserId, nvidiaGpuUserId, amdGpuUserId, wildcardUserId);
    }

    @Override
    public String toString() {
        return String.format("%s::{id: '%s', teamName: '%s', captainUserId: '%s', nvidiaGpuUserId: '%s', amdGpuUserId: '%s', wildcardUserId: '%s'", this.getClass().getSimpleName(), id, teamName, captainUserId, nvidiaGpuUserId, amdGpuUserId, wildcardUserId);
    }
}

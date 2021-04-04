package me.zodac.folding.api;

import java.io.Serializable;
import java.util.Objects;

import static me.zodac.folding.api.util.StringUtils.isNotBlank;

// TODO: [zodac] Dunno if I want this, keeping here in case I do :)
public class FoldingTeamWithUsers implements Serializable {
    
    private static final long serialVersionUID = 7100454644477596702L;

    private int id;
    private String teamName;
    private FoldingUser nvidiaGpuUser;
    private FoldingUser amdGpuUser;
    private FoldingUser wildcardUser;

    public FoldingTeamWithUsers() {

    }

    public FoldingTeamWithUsers(final int id, final String teamName, final FoldingUser nvidiaGpuUser, final FoldingUser amdGpuUser, final FoldingUser wildcardUser) {
        this.id = id;
        this.teamName = teamName;
        this.nvidiaGpuUser = nvidiaGpuUser;
        this.amdGpuUser = amdGpuUser;
        this.wildcardUser = wildcardUser;
    }

    public static FoldingTeamWithUsers create(final int teamId, final String teamName, final FoldingUser nvidiaGpuUser, final FoldingUser amdGpuUser, final FoldingUser wildcardUser) {
        return new FoldingTeamWithUsers(teamId, teamName, nvidiaGpuUser, amdGpuUser, wildcardUser);
    }

    public static FoldingTeamWithUsers createWithoutId(final int teamId, final String teamName, final FoldingUser nvidiaGpuUser, final FoldingUser amdGpuUser, final FoldingUser wildcardUser) {
        return new FoldingTeamWithUsers(0, teamName, nvidiaGpuUser, amdGpuUser, wildcardUser);
    }

    public static FoldingTeamWithUsers updateWithId(final int teamId, final FoldingTeamWithUsers foldingTeam) {
        return new FoldingTeamWithUsers(teamId, foldingTeam.getTeamName(), foldingTeam.getNvidiaGpuUser(), foldingTeam.getAmdGpuUser(), foldingTeam.getWildcardUser());
    }

    // Quick function used for REST requests. Since a JSON payload may have a missing/incorrect field, we need to check
    // TODO: [zodac] Verify the users against the user cache
    public boolean isValid() {
        return isNotBlank(teamName);
    }

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

    public FoldingUser getNvidiaGpuUser() {
        return nvidiaGpuUser;
    }

    public void setNvidiaGpuUser(final FoldingUser nvidiaGpuUser) {
        this.nvidiaGpuUser = nvidiaGpuUser;
    }

    public FoldingUser getAmdGpuUser() {
        return amdGpuUser;
    }

    public void setAmdGpuUser(final FoldingUser amdGpuUser) {
        this.amdGpuUser = amdGpuUser;
    }

    public FoldingUser getWildcardUser() {
        return wildcardUser;
    }

    public void setWildcardUser(final FoldingUser wildcardUser) {
        this.wildcardUser = wildcardUser;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final FoldingTeamWithUsers foldingTeam = (FoldingTeamWithUsers) o;
        return id == foldingTeam.id && Objects.equals(teamName, foldingTeam.teamName) && Objects.equals(nvidiaGpuUser, foldingTeam.nvidiaGpuUser) && Objects.equals(amdGpuUser, foldingTeam.amdGpuUser) && Objects.equals(wildcardUser, foldingTeam.wildcardUser);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, teamName, nvidiaGpuUser, amdGpuUser, wildcardUser);
    }

    @Override
    public String toString() {
        return String.format("%s::{id: '%s', teamName: '%s', nvidiaGpuUserId: '%s', amdGpuUserId: '%s', wildcardUserId: '%s'", this.getClass().getSimpleName(), id, teamName, nvidiaGpuUser.toString(), amdGpuUser.toString(), wildcardUser.toString());
    }
}

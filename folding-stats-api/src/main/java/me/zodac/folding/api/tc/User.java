package me.zodac.folding.api.tc;

import me.zodac.folding.api.Identifiable;

import java.util.Objects;

public class User implements Identifiable {

    private static final long serialVersionUID = -1919458037620452556L;

    public static final int EMPTY_USER_ID = 0;

    private int id;
    private String foldingUserName;
    private String displayName;
    private String passkey;
    private String category;
    private int hardwareId;
    private int foldingTeamNumber;
    private String liveStatsLink;

    public User() {

    }

    private User(final int id, final String foldingUserName, final String displayName, final String passkey, final String category, final int hardwareId, final int foldingTeamNumber, final String liveStatsLink) {
        this.id = id;
        this.foldingUserName = foldingUserName;
        this.displayName = displayName;
        this.passkey = passkey;
        this.category = category;
        this.hardwareId = hardwareId;
        this.foldingTeamNumber = foldingTeamNumber;
        this.liveStatsLink = liveStatsLink;
    }

    public static User create(final int userId, final String foldingUserName, final String displayName, final String passkey, final String category, final int hardwareId, final int foldingTeamNumber, final String liveStatsLink) {
        return new User(userId, foldingUserName, displayName, passkey, category, hardwareId, foldingTeamNumber, liveStatsLink);
    }

    public static User createWithoutId(final String foldingUserName, final String displayName, final String passkey, final String category, final int hardwareId, final int foldingTeamNumber, final String liveStatsLink) {
        return new User(0, foldingUserName, displayName, passkey, category, hardwareId, foldingTeamNumber, liveStatsLink);
    }

    public static User updateWithId(final int userId, final User user) {
        return new User(userId, user.getFoldingUserName(), user.getDisplayName(), user.getPasskey(), user.getCategory(), user.getHardwareId(), user.getFoldingTeamNumber(), user.getLiveStatsLink());
    }

    @Override
    public int getId() {
        return id;
    }

    public void setId(final int id) {
        this.id = id;
    }

    public String getFoldingUserName() {
        return foldingUserName;
    }

    public void setFoldingUserName(final String foldingUserName) {
        this.foldingUserName = foldingUserName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(final String displayName) {
        this.displayName = displayName;
    }

    public String getPasskey() {
        return passkey;
    }

    public void setPasskey(final String passkey) {
        this.passkey = passkey;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(final String category) {
        this.category = category;
    }

    public int getHardwareId() {
        return hardwareId;
    }

    public void setHardwareId(final int hardwareId) {
        this.hardwareId = hardwareId;
    }

    public int getFoldingTeamNumber() {
        return foldingTeamNumber;
    }

    public void setFoldingTeamNumber(final int foldingTeamNumber) {
        this.foldingTeamNumber = foldingTeamNumber;
    }

    public String getLiveStatsLink() {
        return liveStatsLink;
    }

    public void setLiveStatsLink(final String liveStatsLink) {
        this.liveStatsLink = liveStatsLink;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final User user = (User) o;
        return id == user.id && hardwareId == user.hardwareId && foldingTeamNumber == user.foldingTeamNumber && Objects.equals(foldingUserName, user.foldingUserName) && Objects.equals(displayName, user.displayName) && Objects.equals(passkey, user.passkey) && Objects.equals(category, user.category) && Objects.equals(liveStatsLink, user.liveStatsLink);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, foldingUserName, displayName, passkey, category, hardwareId, foldingTeamNumber, liveStatsLink);
    }


    @Override
    public String toString() {
        return "User::{" +
                "id: " + id +
                ", foldingUserName: '" + foldingUserName + "'" +
                ", displayName: '" + displayName + "'" +
                ", passkey: '" + passkey + "'" +
                ", category: '" + category + "'" +
                ", hardwareId: " + hardwareId +
                ", foldingTeamNumber: " + foldingTeamNumber +
                ", liveStatsLink: '" + liveStatsLink + "'" +
                '}';
    }
}

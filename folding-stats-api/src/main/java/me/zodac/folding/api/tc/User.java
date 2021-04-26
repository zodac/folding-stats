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
    private String liveStatsLink;
    private boolean isRetired;

    public User() {

    }

    private User(final int id, final String foldingUserName, final String displayName, final String passkey, final String category, final int hardwareId, final String liveStatsLink, final boolean isRetired) {
        this.id = id;
        this.foldingUserName = foldingUserName == null ? null : foldingUserName.trim();
        this.displayName = displayName == null ? null : displayName.trim();
        this.passkey = passkey == null ? null : passkey.trim();
        this.category = category == null ? null : category.trim();
        this.hardwareId = hardwareId;
        this.liveStatsLink = liveStatsLink == null ? null : liveStatsLink.trim();
        this.isRetired = isRetired;
    }

    public static User create(final int userId, final String foldingUserName, final String displayName, final String passkey, final String category, final int hardwareId, final String liveStatsLink, final boolean isRetired) {
        return new User(userId, foldingUserName, displayName, passkey, category, hardwareId, liveStatsLink, isRetired);
    }

    public static User updateWithId(final int userId, final User user) {
        return new User(userId, user.foldingUserName, user.displayName, user.passkey, user.category, user.hardwareId, user.liveStatsLink, user.isRetired);
    }

    public static User unretireUser(final User user) {
        return new User(user.id, user.foldingUserName, user.displayName, user.passkey, user.category, user.hardwareId, user.liveStatsLink, false);
    }

    public static User retireUser(final User user) {
        return new User(user.id, user.foldingUserName, user.displayName, user.passkey, user.category, user.hardwareId, user.liveStatsLink, true);
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
        this.foldingUserName = foldingUserName == null ? null : foldingUserName.trim();
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(final String displayName) {
        this.displayName = displayName == null ? null : displayName.trim();
    }

    public String getPasskey() {
        return passkey;
    }

    public void setPasskey(final String passkey) {
        this.passkey = passkey == null ? null : passkey.trim();
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

    public String getLiveStatsLink() {
        return liveStatsLink;
    }

    public void setLiveStatsLink(final String liveStatsLink) {
        this.liveStatsLink = liveStatsLink == null ? null : liveStatsLink.trim();
    }

    public boolean isActive() {
        return !isRetired();
    }

    public boolean isRetired() {
        return isRetired;
    }

    public void setIsRetired(final boolean isRetired) {
        this.isRetired = isRetired;
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
        return id == user.id && hardwareId == user.hardwareId && Objects.equals(foldingUserName, user.foldingUserName) && Objects.equals(displayName, user.displayName) && Objects.equals(passkey, user.passkey) && Objects.equals(category, user.category) && Objects.equals(liveStatsLink, user.liveStatsLink) && isRetired == user.isRetired;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, foldingUserName, displayName, passkey, category, hardwareId, liveStatsLink, isRetired);
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
                ", liveStatsLink: '" + liveStatsLink + "'" +
                ", isRetired: " + isRetired +
                '}';
    }
}

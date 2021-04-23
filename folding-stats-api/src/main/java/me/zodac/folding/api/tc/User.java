package me.zodac.folding.api.tc;

import me.zodac.folding.api.Identifiable;

import java.util.Objects;

import static me.zodac.folding.api.utils.NumberUtils.formatWithCommas;


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
    private long pointsOffset;
    private int unitsOffset;

    public User() {

    }

    private User(final int id, final String foldingUserName, final String displayName, final String passkey, final String category, final int hardwareId, final int foldingTeamNumber, final String liveStatsLink, final long pointsOffset, final int unitsOffset) {
        this.id = id;
        this.foldingUserName = foldingUserName == null ? null : foldingUserName.trim();
        this.displayName = displayName == null ? null : displayName.trim();
        this.passkey = passkey == null ? null : passkey.trim();
        this.category = category == null ? null : category.trim();
        this.hardwareId = hardwareId;
        this.foldingTeamNumber = foldingTeamNumber;
        this.liveStatsLink = liveStatsLink == null ? null : liveStatsLink.trim();
        this.pointsOffset = pointsOffset;
        this.unitsOffset = unitsOffset;
    }

    public static User create(final int userId, final String foldingUserName, final String displayName, final String passkey, final String category, final int hardwareId, final int foldingTeamNumber, final String liveStatsLink, final long pointsOffset, final int unitsOffset) {
        return new User(userId, foldingUserName, displayName, passkey, category, hardwareId, foldingTeamNumber, liveStatsLink, pointsOffset, unitsOffset);
    }

    public static User updateWithId(final int userId, final User user) {
        return new User(userId, user.foldingUserName, user.displayName, user.passkey, user.category, user.hardwareId, user.foldingTeamNumber, user.liveStatsLink, user.pointsOffset, user.unitsOffset);
    }

    public static User updateWithNoOffsets(final User user) {
        return new User(user.id, user.foldingUserName, user.displayName, user.passkey, user.category, user.hardwareId, user.foldingTeamNumber, user.liveStatsLink, 0L, 0);
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
        this.liveStatsLink = liveStatsLink == null ? null : liveStatsLink.trim();
    }

    public long getPointsOffset() {
        return pointsOffset;
    }

    public void setPointsOffset(final long pointsOffset) {
        this.pointsOffset = pointsOffset;
    }

    public int getUnitsOffset() {
        return unitsOffset;
    }

    public void setUnitsOffset(final int unitsOffset) {
        this.unitsOffset = unitsOffset;
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
        return id == user.id && hardwareId == user.hardwareId && foldingTeamNumber == user.foldingTeamNumber && pointsOffset == user.pointsOffset && unitsOffset == user.unitsOffset &&
                Objects.equals(foldingUserName, user.foldingUserName) && Objects.equals(displayName, user.displayName) && Objects.equals(passkey, user.passkey) && Objects.equals(category, user.category) && Objects.equals(liveStatsLink, user.liveStatsLink);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, foldingUserName, displayName, passkey, category, hardwareId, foldingTeamNumber, liveStatsLink, pointsOffset, unitsOffset);
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
                ", pointsOffset: " + formatWithCommas(pointsOffset) +
                ", unitsOffset: " + formatWithCommas(unitsOffset) +
                '}';
    }
}

package me.zodac.folding.api;

import java.util.Objects;

// TODO: [zodac] This should really be a TcUser. I think there will be confusion between this and non-TC users (like for a Foldathon).
//   Need to distinguish the names better, could be very confusing as is.
public class FoldingUser implements Identifiable {

    private static final long serialVersionUID = -1919458037620452556L;

    public static final int EMPTY_USER_ID = 0;

    private int id;
    private String foldingUserName;
    private String displayName;
    private String passkey;
    private String category;
    private int hardwareId;
    private int foldingTeamNumber;

    public FoldingUser() {

    }

    private FoldingUser(final int id, final String foldingUserName, final String displayName, final String passkey, final String category, final int hardwareId, final int foldingTeamNumber) {
        this.id = id;
        this.foldingUserName = foldingUserName;
        this.displayName = displayName;
        this.passkey = passkey;
        this.category = category;
        this.hardwareId = hardwareId;
        this.foldingTeamNumber = foldingTeamNumber;
    }

    public static FoldingUser create(final int userId, final String foldingUserName, final String displayName, final String passkey, final String category, final int hardwareId, final int foldingTeamNumber) {
        return new FoldingUser(userId, foldingUserName, displayName, passkey, category, hardwareId, foldingTeamNumber);
    }

    public static FoldingUser createWithoutId(final String foldingUserName, final String displayName, final String passkey, final String category, final int hardwareId, final int foldingTeamNumber) {
        return new FoldingUser(0, foldingUserName, displayName, passkey, category, hardwareId, foldingTeamNumber);
    }

    public static FoldingUser updateWithId(final int userId, final FoldingUser foldingUser) {
        return new FoldingUser(userId, foldingUser.getFoldingUserName(), foldingUser.getDisplayName(), foldingUser.getPasskey(), foldingUser.getCategory(), foldingUser.getHardwareId(), foldingUser.getFoldingTeamNumber());
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

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final FoldingUser that = (FoldingUser) o;
        return id == that.id && hardwareId == that.hardwareId && Objects.equals(category, that.category) && foldingTeamNumber == that.foldingTeamNumber && Objects.equals(foldingUserName, that.foldingUserName) && Objects.equals(displayName, that.displayName) && Objects.equals(passkey, that.passkey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, foldingUserName, displayName, passkey, category, hardwareId, foldingTeamNumber);
    }

    @Override
    public String toString() {
        return String.format("%s::{id: '%s', foldingUserName: '%s', displayName: '%s', passkey: '%s', category: '%s', hardwareId: '%s', foldingTeamNumber: '%s'}", this.getClass().getSimpleName(), id, foldingUserName, displayName, passkey, category, hardwareId, foldingTeamNumber);
    }
}

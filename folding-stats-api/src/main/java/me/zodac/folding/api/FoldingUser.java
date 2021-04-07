package me.zodac.folding.api;

import java.io.Serializable;
import java.util.Objects;

import static me.zodac.folding.api.util.StringUtils.isNotBlank;

public class FoldingUser implements PojoWithId, Serializable {

    private static final long serialVersionUID = -1919458037620452556L;

    public static final FoldingUser EMPTY_USER = FoldingUser.create(0, "", "", "", 0);

    private int id;
    private String foldingUserName;
    private String displayName;
    private String passkey;
    private int hardwareId;

    public FoldingUser() {

    }

    private FoldingUser(final int id, final String foldingUserName, final String displayName, final String passkey, final int hardwareId) {
        this.id = id;
        this.foldingUserName = foldingUserName;
        this.displayName = displayName;
        this.passkey = passkey;
        this.hardwareId = hardwareId;
    }

    public static FoldingUser create(final int userId, final String foldingUserName, final String displayName, final String passkey, final int hardwareId) {
        return new FoldingUser(userId, foldingUserName, displayName, passkey, hardwareId);
    }

    public static FoldingUser createWithoutId(final String foldingUserName, final String displayName, final String passkey, final int hardwareId) {
        return new FoldingUser(0, foldingUserName, displayName, passkey, hardwareId);
    }

    public static FoldingUser updateWithId(final int userId, final FoldingUser foldingUser) {
        return new FoldingUser(userId, foldingUser.getFoldingUserName(), foldingUser.getDisplayName(), foldingUser.getPasskey(), foldingUser.getHardwareId());
    }

    // Quick function used for REST requests. Since a JSON payload may have a missing/incorrect field, we need to check
    // TODO: [zodac] hardwareId needs to be checked against DB (cache)
    public boolean isValid() {
        return isNotBlank(foldingUserName) && isNotBlank(displayName) && isNotBlank(passkey) && hardwareId > 0;
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

    public int getHardwareId() {
        return hardwareId;
    }

    public void setHardwareId(final int hardwareId) {
        this.hardwareId = hardwareId;
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
        return id == that.id && hardwareId == that.hardwareId && Objects.equals(foldingUserName, that.foldingUserName) && Objects.equals(displayName, that.displayName) && Objects.equals(passkey, that.passkey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, foldingUserName, displayName, passkey, hardwareId);
    }

    @Override
    public String toString() {
        return String.format("%s::{id: '%s', foldingUserName: '%s', displayName: '%s', passkey: '%s', hardwareId: '%s'}", this.getClass().getSimpleName(), id, foldingUserName, displayName, passkey, hardwareId);
    }
}

package me.zodac.folding.api;

import java.io.Serializable;
import java.util.Objects;

import static me.zodac.folding.api.util.StringUtils.isNotBlank;

public class FoldingUser implements Serializable {

    private static final long serialVersionUID = -1919458037620452556L;

    private int id;
    private String foldingUserName;
    private String displayName;
    private String passkey;
    private int hardwareCategoryId;
    private String hardwareName;

    public FoldingUser() {

    }

    private FoldingUser(final int id, final String foldingUserName, final String displayName, final String passkey, final int hardwareCategoryId, final String hardwareName) {
        this.id = id;
        this.foldingUserName = foldingUserName;
        this.displayName = displayName;
        this.passkey = passkey;
        this.hardwareCategoryId = hardwareCategoryId;
        this.hardwareName = hardwareName;
    }

    public static FoldingUser create(final int userId, final String foldingUserName, final String displayName, final String passkey, final int hardwareCategoryId, final String hardwareName) {
        return new FoldingUser(userId, foldingUserName, displayName, passkey, hardwareCategoryId, hardwareName);
    }

    public static FoldingUser createWithoutId(final String foldingUserName, final String displayName, final String passkey, final int hardwareCategoryId, final String hardwareName) {
        return new FoldingUser(0, foldingUserName, displayName, passkey, hardwareCategoryId, hardwareName);
    }

    public static FoldingUser updateWithId(final int userId, final FoldingUser foldingUser) {
        return new FoldingUser(userId, foldingUser.getFoldingUserName(), foldingUser.getDisplayName(), foldingUser.getPasskey(), foldingUser.getHardwareCategoryId(), foldingUser.getHardwareName());
    }

    // Quick function used for REST requests. Since a JSON payload may have a missing/incorrect field, we need to check
    // TODO: [zodac] hardwareCategoryId needs to be checked against DB (cache)
    public boolean isValid() {
        return isNotBlank(foldingUserName) && isNotBlank(displayName) && isNotBlank(passkey) && isNotBlank(hardwareName) && hardwareCategoryId > 0;
    }

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

    public int getHardwareCategoryId() {
        return hardwareCategoryId;
    }

    public void setHardwareCategoryId(final int hardwareCategoryId) {
        this.hardwareCategoryId = hardwareCategoryId;
    }

    public String getHardwareName() {
        return hardwareName;
    }

    public void setHardwareName(final String hardwareName) {
        this.hardwareName = hardwareName;
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
        return id == that.id && hardwareCategoryId == that.hardwareCategoryId && Objects.equals(foldingUserName, that.foldingUserName) && Objects.equals(displayName, that.displayName) && Objects.equals(passkey, that.passkey) && Objects.equals(hardwareName, that.hardwareName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, foldingUserName, displayName, passkey, hardwareCategoryId, hardwareName);
    }

    @Override
    public String toString() {
        return String.format("%s::{id: '%s', foldingUserName: '%s', displayName: '%s', passkey: '%s', hardwareCategoryId: '%s', hardwareName: '%s'", this.getClass().getSimpleName(), id, foldingUserName, displayName, passkey, hardwareCategoryId, hardwareName);
    }
}

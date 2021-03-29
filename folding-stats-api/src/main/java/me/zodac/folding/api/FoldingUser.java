package me.zodac.folding.api;

import java.io.Serializable;
import java.util.Objects;

import static me.zodac.folding.api.util.StringUtils.isNotBlank;

public class FoldingUser implements Serializable {

    private static final long serialVersionUID = -1919458037620452556L;

    private int id;
    private String userName;
    private String passkey;
    private int hardwareCategoryId;
    private String hardwareName;

    public FoldingUser() {

    }

    private FoldingUser(final int id, final String userName, final String passkey, final int hardwareCategoryId, final String hardwareName) {
        this.id = id;
        this.userName = userName;
        this.passkey = passkey;
        this.hardwareCategoryId = hardwareCategoryId;
        this.hardwareName = hardwareName;
    }

    public static FoldingUser create(final int userId, final String userName, final String passkey, final int hardwareCategoryId, final String hardwareName) {
        return new FoldingUser(userId, userName, passkey, hardwareCategoryId, hardwareName);
    }

    public static FoldingUser createWithoutId(final String userName, final String passkey, final int hardwareCategoryId, final String hardwareName) {
        return new FoldingUser(0, userName, passkey, hardwareCategoryId, hardwareName);
    }

    public static FoldingUser updateFoldingUserWithId(final int userId, final FoldingUser foldingUser) {
        return new FoldingUser(userId, foldingUser.getUserName(), foldingUser.getPasskey(), foldingUser.getHardwareCategoryId(), foldingUser.getHardwareName());
    }

    // Quick function used for REST requests. Since a JSON payload may have a missing/incorrect field, we need to check
    // TODO: [zodac] hardwareCategoryId needs to be checked against DB (cache)
    public boolean isValid() {
        return isNotBlank(userName) && isNotBlank(passkey) && isNotBlank(hardwareName);
    }

    public int getId() {
        return id;
    }

    public void setId(final int id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(final String userName) {
        this.userName = userName;
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
        return id == that.id && hardwareCategoryId == that.hardwareCategoryId && Objects.equals(userName, that.userName) && Objects.equals(passkey, that.passkey) && Objects.equals(hardwareName, that.hardwareName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userName, passkey, hardwareCategoryId, hardwareName);
    }

    @Override
    public String toString() {
        return String.format("%s::{id: '%s', userName: '%s', passkey: '%s', hardwareCategoryId: '%s', hardwareName: '%s'", this.getClass().getSimpleName(), id, userName, passkey, hardwareCategoryId, hardwareName);
    }
}

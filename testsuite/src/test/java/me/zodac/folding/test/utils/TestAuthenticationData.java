package me.zodac.folding.test.utils;

/**
 * Simple {@link Enum} containing the user name and password data for test execution. Using default details that should
 * not be used in production.
 */
public enum TestAuthenticationData {

    ADMIN_USER("root", "shroot"),
    READ_ONLY_USER("root2", "shroot2"),
    INVALID_USERNAME("invalid", "shroot"),
    INVALID_PASSWORD("root", "invalid");

    private final String userName;
    private final String password;

    TestAuthenticationData(final String userName, final String password) {
        this.userName = userName;
        this.password = password;
    }

    public String userName() {
        return userName;
    }

    public String password() {
        return password;
    }
}

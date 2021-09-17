package me.zodac.folding.test.utils;

/**
 * Simple {@link Enum} containing the username and password data for test execution. Using default details that should not be used in production.
 */
public enum TestAuthenticationData {

    /**
     * A valid user with the 'admin' role.
     */
    ADMIN_USER("root", "shroot"),

    /**
     * A valid user with the 'read-only' role.
     */
    READ_ONLY_USER("root2", "shroot2"),

    /**
     * An invalid user with an invalid username.
     */
    INVALID_USERNAME("invalid", "shroot"),

    /**
     * An invalid user with an invalid password.
     */
    INVALID_PASSWORD("root", "invalid");

    private final String userName;
    private final String password;

    /**
     * Constructs a {@link TestAuthenticationData}.
     *
     * @param userName the username
     * @param password the password
     */
    TestAuthenticationData(final String userName, final String password) {
        this.userName = userName;
        this.password = password;
    }

    /**
     * Retrieves the username.
     *
     * @return the username
     */
    public String userName() {
        return userName;
    }

    /**
     * Retrieves the password.
     *
     * @return the password
     */
    public String password() {
        return password;
    }
}

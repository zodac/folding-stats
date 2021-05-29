package me.zodac.folding.api.db;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.Collections;
import java.util.Set;

/**
 * POJO defining the authentication response for a user/password, and the roles for that user if successful.
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@EqualsAndHashCode
@ToString(doNotUseGetters = true)
public class SystemUserAuthentication {

    private static final String ADMIN_ROLE = "admin";

    private final boolean userExists;
    private final boolean passwordMatch;
    private final Set<String> userRoles;

    /**
     * User does not exist {@link SystemUserAuthentication}.
     *
     * @return the {@link SystemUserAuthentication}
     */
    public static SystemUserAuthentication userDoesNotExist() {
        return new SystemUserAuthentication(false, false, Collections.emptySet());
    }

    /**
     * User exists, but invalid password provided {@link SystemUserAuthentication}.
     *
     * @return the {@link SystemUserAuthentication}
     */
    public static SystemUserAuthentication invalidPassword() {
        return new SystemUserAuthentication(true, false, Collections.emptySet());
    }

    /**
     * Successful {@link SystemUserAuthentication} with retrieved user roles.
     *
     * @return the {@link SystemUserAuthentication}
     */
    public static SystemUserAuthentication success(final Set<String> userRoles) {
        return new SystemUserAuthentication(true, true, userRoles);
    }

    /**
     * Checks if the {@code userRoles} of the {@link SystemUserAuthentication} contain the 'admin' user role.
     *
     * @return <code>true</code> if the {@code userRoles} contains the 'admin' user role
     */
    public boolean isAdmin() {
        return userRoles.contains(ADMIN_ROLE);
    }
}

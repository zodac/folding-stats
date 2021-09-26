package me.zodac.folding.api;

import java.util.Collections;
import java.util.Set;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * POJO defining the authentication response for a system user/password, and the roles for that user if successful.
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@EqualsAndHashCode
@ToString(doNotUseGetters = true)
public class UserAuthenticationResult {

    private static final String ADMIN_ROLE = "admin";

    private final boolean userExists;
    private final boolean passwordMatch;
    private final Set<String> userRoles;

    /**
     * User does not exist {@link UserAuthenticationResult}.
     *
     * @return the {@link UserAuthenticationResult}
     */
    public static UserAuthenticationResult userDoesNotExist() {
        return new UserAuthenticationResult(false, false, Collections.emptySet());
    }

    /**
     * User exists, but invalid password provided {@link UserAuthenticationResult}.
     *
     * @return the {@link UserAuthenticationResult}
     */
    public static UserAuthenticationResult invalidPassword() {
        return new UserAuthenticationResult(true, false, Collections.emptySet());
    }

    /**
     * Successful {@link UserAuthenticationResult} with retrieved user roles.
     *
     * @param userRoles the roles of the user that has been successfully logged in
     * @return the {@link UserAuthenticationResult}
     */
    public static UserAuthenticationResult success(final Set<String> userRoles) {
        return new UserAuthenticationResult(true, true, userRoles);
    }

    /**
     * Checks if the {@code userRoles} of the {@link UserAuthenticationResult} contain the 'admin' user role.
     *
     * @return <code>true</code> if the {@code userRoles} contains the 'admin' user role
     */
    public boolean isAdmin() {
        return userRoles.contains(ADMIN_ROLE);
    }
}

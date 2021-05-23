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
public class AuthenticationResponse {

    private static final String ADMIN_ROLE = "admin";

    private final boolean userExists;
    private final boolean passwordMatch;
    private final Set<String> userRoles;

    /**
     * User does not exist {@link AuthenticationResponse}.
     *
     * @return the {@link AuthenticationResponse}
     */
    public static AuthenticationResponse userDoesNotExist() {
        return new AuthenticationResponse(false, false, Collections.emptySet());
    }

    /**
     * User exists, but invalid password provided {@link AuthenticationResponse}.
     *
     * @return the {@link AuthenticationResponse}
     */
    public static AuthenticationResponse invalidPassword() {
        return new AuthenticationResponse(true, false, Collections.emptySet());
    }

    /**
     * Successful {@link AuthenticationResponse} with retrieved user roles.
     *
     * @return the {@link AuthenticationResponse}
     */
    public static AuthenticationResponse success(final Set<String> userRoles) {
        return new AuthenticationResponse(true, true, userRoles);
    }

    /**
     * Checks if the {@code userRoles} of the {@link AuthenticationResponse} contain the 'admin' user role.
     *
     * @return <code>true</code> if the {@code userRoles} contains the 'admin' user role
     */
    public boolean isAdmin() {
        return userRoles.contains(ADMIN_ROLE);
    }
}

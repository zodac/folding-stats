package me.zodac.folding.rest.api;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import me.zodac.folding.api.utils.EncodingUtils;

/**
 * Simple POJO defining {@link java.util.Base64}-encoded credentials. The credentials are created by:
 * <ol>
 *     <li>Concatenate the username and password, with a <code>:</code> as delimiter (i.e., 'username:password')</li>
 *     <li>Encode the result with {@link java.util.Base64}</li>
 *     <li>Prefix the encoded result with "Basic " (note the space)</li>
 * </ol>
 */
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Setter
@EqualsAndHashCode
@ToString(doNotUseGetters = true)
public class LoginCredentials {

    private String encodedUserNameAndPassword;

    /**
     * Creates an instance of {@link LoginCredentials} given a {@link java.util.Base64}-encoded username/password credentials.
     * <p>
     * If the input does not start with {@link EncodingUtils#BASIC_AUTHENTICATION_SCHEME}, it will be prefixed.
     *
     * @param encodedUserNameAndPassword the {@link java.util.Base64}-encoded username/password
     * @return the {@link LoginCredentials} for the given credentials
     */
    public static LoginCredentials createWithBasicAuthentication(final String encodedUserNameAndPassword) {
        if (!encodedUserNameAndPassword.startsWith(EncodingUtils.BASIC_AUTHENTICATION_SCHEME)) {
            return new LoginCredentials(EncodingUtils.BASIC_AUTHENTICATION_SCHEME + encodedUserNameAndPassword);
        }

        return new LoginCredentials(encodedUserNameAndPassword);
    }
}

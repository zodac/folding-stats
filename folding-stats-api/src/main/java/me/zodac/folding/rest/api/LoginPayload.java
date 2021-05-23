package me.zodac.folding.rest.api;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import me.zodac.folding.api.utils.EncodingUtils;

@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Setter
@EqualsAndHashCode
@ToString(doNotUseGetters = true)
public class LoginPayload {

    private String encodedUserNameAndPassword;

    public static LoginPayload createWithBasicAuthentication(final String encodedUserNameAndPassword) {
        if (!encodedUserNameAndPassword.startsWith(EncodingUtils.BASIC_AUTHENTICATION_SCHEME)) {
            return new LoginPayload(EncodingUtils.BASIC_AUTHENTICATION_SCHEME + encodedUserNameAndPassword);
        }
        
        return new LoginPayload(encodedUserNameAndPassword);
    }
}

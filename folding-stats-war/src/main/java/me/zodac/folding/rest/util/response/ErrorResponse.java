package me.zodac.folding.rest.util.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Simple POJO used for REST responses where an error message is required.
 * <p>
 * When using {@link com.google.gson.Gson}, the response will be in the form:
 * <pre>
 *     {
 *         "error": "My error message here"
 *     }
 * </pre>
 */
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Setter
@EqualsAndHashCode
@ToString(doNotUseGetters = true)
public class ErrorResponse {

    private String error;

    public static ErrorResponse create(final String error) {
        return new ErrorResponse(error);
    }
}

package me.zodac.folding.rest.endpoint.util;

import javax.ws.rs.core.Response;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * POJO defining the result of {@link IntegerParser#parsePositive(String)}.
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@EqualsAndHashCode
@ToString(doNotUseGetters = true)
public class IdResult {

    private static final int INVALID_ID = -1;

    private final boolean successful;
    private final int id;
    private final Response failureResponse;

    /**
     * Creates an {@link IdResult} for a successfully parsed {@link Integer} ID.
     *
     * @param id the parsed {@link Integer} ID
     * @return the {@link IdResult}
     */
    public static IdResult success(final int id) {
        return new IdResult(true, id, null);
    }

    /**
     * Creates an {@link IdResult} for an invalid parsing due the input value not being a valid
     * {@link Integer}.
     *
     * @param response the failure {@link Response}
     * @return the {@link IdResult}
     */
    public static IdResult failure(final Response response) {
        return new IdResult(false, INVALID_ID, response);
    }

    /**
     * Checks if the {@link IdResult} failed and does not contain a valid {@link Integer} ID.
     *
     * @return <code>true</code> if the {@link IdResult} does not contain an ID
     */
    public boolean isFailure() {
        return !successful;
    }
}
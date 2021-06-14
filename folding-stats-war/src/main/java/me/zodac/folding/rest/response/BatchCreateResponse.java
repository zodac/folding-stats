package me.zodac.folding.rest.response;

import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Response POJO used to encapsulate the result of a batch create operation through the REST layer.
 */
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Setter
@EqualsAndHashCode
@ToString(doNotUseGetters = true)
public class BatchCreateResponse {

    private List<Object> successful;
    private List<Object> unsuccessful;

    /**
     * Create a {@link BatchCreateResponse} with {@code successful} and {@code unsuccessful} {@link List}s of the
     * payload attempting to be created.
     *
     * @param successful   the successfully created {@link Object}s
     * @param unsuccessful the unsuccessfully created {@link Object}s
     * @return the created {@link BatchCreateResponse}
     */
    public static BatchCreateResponse create(final List<Object> successful, final List<Object> unsuccessful) {
        return new BatchCreateResponse(successful, unsuccessful);
    }
}
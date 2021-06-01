package me.zodac.folding.rest.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import me.zodac.folding.api.ResponsePojo;

import java.util.Collection;

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

    private Collection<? extends ResponsePojo> successful;
    private Collection<? extends ResponsePojo> unsuccessful;

    /**
     * Create a {@link BatchCreateResponse} with {@code successful} and {@code unsuccessful} {@link Collection}s of the
     * {@link ResponsePojo} implementation attempting to be created.
     *
     * @param successful   the successfully created {@link ResponsePojo}s
     * @param unsuccessful the unsuccessfully created {@link ResponsePojo}s
     * @return the created {@link BatchCreateResponse}
     */
    public static BatchCreateResponse create(final Collection<? extends ResponsePojo> successful, final Collection<? extends ResponsePojo> unsuccessful) {
        return new BatchCreateResponse(successful, unsuccessful);
    }
}
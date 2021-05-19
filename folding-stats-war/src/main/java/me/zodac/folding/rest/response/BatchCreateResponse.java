package me.zodac.folding.rest.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import me.zodac.folding.api.Identifiable;

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

    private Collection<? extends Identifiable> successful;
    private Collection<? extends Identifiable> unsuccessful;

    /**
     * Create a {@link BatchCreateResponse} with {@code successful} and {@code unsuccessful} {@link Collection}s of the
     * {@link Identifiable} implementation attempting to be created.
     *
     * @param successful   the successfully created {@link Identifiable}s
     * @param unsuccessful the unsuccessfully created {@link Identifiable}s
     * @return the created {@link BatchCreateResponse}
     */
    public static BatchCreateResponse create(final Collection<? extends Identifiable> successful, final Collection<? extends Identifiable> unsuccessful) {
        return new BatchCreateResponse(successful, unsuccessful);
    }
}
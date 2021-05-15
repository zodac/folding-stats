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

@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Setter
@EqualsAndHashCode
@ToString(doNotUseGetters = true)
public class BulkCreateResponse {

    private Collection<? extends Identifiable> successful;
    private Collection<? extends Identifiable> unsuccessful;

    public static BulkCreateResponse create(final Collection<? extends Identifiable> successful, final Collection<? extends Identifiable> unsuccessful) {
        return new BulkCreateResponse(successful, unsuccessful);
    }
}
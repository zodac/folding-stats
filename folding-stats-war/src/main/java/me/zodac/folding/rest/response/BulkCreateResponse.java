package me.zodac.folding.rest.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import me.zodac.folding.api.Identifiable;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Setter
@EqualsAndHashCode
@ToString(doNotUseGetters = true)
public class BulkCreateResponse {

    private List<? extends Identifiable> successful;
    private List<? extends Identifiable> unsuccessful;

    public static BulkCreateResponse create(final List<? extends Identifiable> successful, final List<? extends Identifiable> unsuccessful) {
        return new BulkCreateResponse(successful, unsuccessful);
    }
}
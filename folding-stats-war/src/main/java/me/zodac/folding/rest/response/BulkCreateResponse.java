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

    private List<Identifiable> successful;
    private List<Identifiable> unsuccessful;

    public static BulkCreateResponse create(final List<Identifiable> successful, final List<Identifiable> unsuccessful) {
        return new BulkCreateResponse(successful, unsuccessful);
    }
}
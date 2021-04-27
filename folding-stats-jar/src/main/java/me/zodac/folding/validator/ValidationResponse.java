package me.zodac.folding.validator;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Collections;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Setter
@EqualsAndHashCode
@ToString(doNotUseGetters = true)
public class ValidationResponse {
    
    private Object invalidObject;
    private List<String> errors;

    public static ValidationResponse success() {
        return new ValidationResponse(null, Collections.emptyList());
    }

    public static ValidationResponse failure(final Object invalidObject, final List<String> errors) {
        return new ValidationResponse(invalidObject, errors);
    }

    public boolean isInvalid() {
        return !errors.isEmpty();
    }
}
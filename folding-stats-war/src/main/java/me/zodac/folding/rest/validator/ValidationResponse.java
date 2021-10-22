package me.zodac.folding.rest.validator;

import java.util.Collection;

/**
 * Simple interface defining the errors for a {@link ValidationResponse}.
 */
public interface ValidationResponse {

    /**
     * The errors for a failed {@link ValidationResponse}.
     *
     * @return a {@link Collection} of the validation error {@link String}s
     */
    Collection<String> getErrors();
}
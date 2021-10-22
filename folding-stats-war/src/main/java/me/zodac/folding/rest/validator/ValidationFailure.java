package me.zodac.folding.rest.validator;

import java.util.Collection;

/**
 * Simple interface defining the errors for a {@link ValidationFailure}.
 */
public interface ValidationFailure {

    /**
     * The errors for a failed {@link ValidationFailure}.
     *
     * @return a {@link Collection} of the validation error {@link String}s
     */
    Collection<String> getErrors();
}
package me.zodac.folding.api.validator;

/**
 * Enum defining the different types of results returned by a {@link ValidationResponse}.
 */
public enum ValidationResult {

    /**
     * Validation failure due to a conflicting item already existing.
     */
    FAILURE_DUE_TO_CONFLICT,

    /**
     * Validation failure based on the attributes provided.
     */
    FAILURE_ON_VALIDATION,

    /**
     * Validation passed successfully.
     */
    SUCCESSFUL_VALIDATION
}

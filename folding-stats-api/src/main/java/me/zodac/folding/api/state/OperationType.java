package me.zodac.folding.api.state;

/**
 * Simple ENUM defining the types of executions that can be performed on the system by a {@link SystemState}.
 */
public enum OperationType {

    /**
     * The system can perform <b>READ</b> operations.
     */
    READ,

    /**
     * The system can perform <b>WRITE</b> operations.
     */
    WRITE
}

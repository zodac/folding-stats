package me.zodac.folding.api.utils;

/**
 * Lists the possible types of executions for long-running tasks.
 */
public enum ExecutionType {

    /**
     * Marks a specific function, method or task to be run in a background process, if possible.
     */
    ASYNCHRONOUS,

    /**
     * Marks a specific function, method or task to be run in the main process and block until completed, if possible.
     */
    SYNCHRONOUS
}

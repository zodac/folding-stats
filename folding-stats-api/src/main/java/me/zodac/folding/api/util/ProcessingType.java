package me.zodac.folding.api.util;

/**
 * Defines the possible types of executions for long-running tasks.
 */
public enum ProcessingType {

    /**
     * Marks a specific function, method or task to be run in a background process, if possible.
     */
    ASYNCHRONOUS,

    /**
     * Marks a specific function, method or task to be run in the main process and block until completed, if possible.
     */
    SYNCHRONOUS
}

package me.zodac.folding.api.exception;

/**
 * {@link RuntimeException} used when an error occurs opening or closing a database connection.
 *
 * <p>
 * In general, I'm not a fan of {@link RuntimeException}s since I think they should be handled at some level, same as
 * a checked {@link Exception}. However, to keep method signatures clean for others who might be reading this codebase,
 * I'll live with it.
 *
 * <p>
 * It does mean we need to ensure that our boundaries (schedules EJBs, REST request handlers, etc.) are at least handling
 * generic {@link Exception}s, or even handling this one explicitly.
 */
public class DatabaseConnectionException extends RuntimeException {

    private static final long serialVersionUID = 2662806281406745266L;

    /**
     * Constructor taking in an error message and a cause {@link Throwable}.
     *
     * @param message   the error message
     * @param throwable the cause {@link Throwable}
     */
    public DatabaseConnectionException(final String message, final Throwable throwable) {
        super(message, throwable);
    }
}

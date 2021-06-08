package me.zodac.folding.rest.api.exception;

/**
 * Application {@link Exception} used when an error occurs sending a REST request.
 */
public class FoldingRestException extends Exception {

    private static final long serialVersionUID = -3883148353675655633L;

    /**
     * Constructor taking in an error message.
     *
     * @param message the error message
     */
    public FoldingRestException(final String message) {
        super(message);
    }

    /**
     * Constructor taking in an error message and a cause {@link Throwable}.
     *
     * @param message   the error message
     * @param throwable the cause {@link Throwable}
     */
    public FoldingRestException(final String message, final Throwable throwable) {
        super(message, throwable);
    }
}

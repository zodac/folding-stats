package me.zodac.folding.ejb.tc.lars;

/**
 * Marker {@link Exception} to handle control flow (yes, I know) in {@link LarsGpuParser}.
 */
class ParseException extends Exception {

    private static final long serialVersionUID = -4059920979584050749L;

    /**
     * Constructor taking in an error message.
     *
     * @param message the error message
     */
    ParseException(final String message) {
        super(message);
    }

    /**
     * Constructor taking in an error message and a cause {@link Throwable}.
     *
     * @param message   the error message
     * @param throwable the cause {@link Throwable}
     */
    ParseException(final String message, final Throwable throwable) {
        super(message, throwable);
    }
}
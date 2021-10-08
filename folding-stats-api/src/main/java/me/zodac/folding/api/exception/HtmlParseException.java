package me.zodac.folding.api.exception;

/**
 * {@link Exception} for when errors occur when parsing HTML content.
 */
public class HtmlParseException extends Exception {

    private static final long serialVersionUID = -4059920979584050749L;

    /**
     * Constructor taking in an error message.
     *
     * @param message the error message
     */
    public HtmlParseException(final String message) {
        super(message);
    }

    /**
     * Constructor taking in an error message and a cause {@link Throwable}.
     *
     * @param message   the error message
     * @param throwable the cause {@link Throwable}
     */
    public HtmlParseException(final String message, final Throwable throwable) {
        super(message, throwable);
    }
}
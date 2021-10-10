package me.zodac.folding.api.exception;

/**
 * {@link Exception} for errors when connecting to an external service, or when an unexpected response is returned.
 */
// TODO: [zodac] Replace with java.rmi.ConnectException?
public class ExternalConnectionException extends Exception {

    private static final long serialVersionUID = 2084075114898438910L;

    private final String url;

    /**
     * Constructor taking in the failing URL and an error message.
     *
     * @param url     the URL which was unable to be connected to
     * @param message the error message
     */
    public ExternalConnectionException(final String url, final String message) {
        super(message);
        this.url = url;
    }

    /**
     * Constructor taking in the failing URL, an error message and a cause {@link Throwable}.
     *
     * @param url       the URL which was unable to be connected to
     * @param message   the error message
     * @param throwable the cause {@link Throwable}
     */
    public ExternalConnectionException(final String url, final String message, final Throwable throwable) {
        super(message, throwable);
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}

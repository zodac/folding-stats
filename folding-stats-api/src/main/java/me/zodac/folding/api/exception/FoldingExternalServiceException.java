package me.zodac.folding.api.exception;

/**
 * {@link Exception} for errors when connecting to an external service, or when an unexpected response is returned.
 */
public class FoldingExternalServiceException extends Exception {

    private static final long serialVersionUID = 2084075114898438910L;

    private final String url;

    public FoldingExternalServiceException(final String url, final String message) {
        super(message);
        this.url = url;
    }

    public FoldingExternalServiceException(final String url, final String message, final Throwable throwable) {
        super(message, throwable);
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}

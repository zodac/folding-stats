package me.zodac.folding.rest.api.exception;

public class FoldingRestException extends Exception {

    private static final long serialVersionUID = -3883148353675655633L;

    public FoldingRestException(final String message) {
        super(message);
    }

    public FoldingRestException(final String message, final Throwable throwable) {
        super(message, throwable);
    }
}
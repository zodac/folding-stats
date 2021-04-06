package me.zodac.folding.api.exception;

public class NotFoundException extends Exception {

    private static final long serialVersionUID = 4515679320571283490L;

    public NotFoundException() {
        super();
    }

    public NotFoundException(final String message) {
        super(message);
    }

    public NotFoundException(final String message, final Throwable throwable) {
        super(message, throwable);
    }
}

package me.zodac.folding.api.exception;

public class TeamNotFoundException extends NotFoundException {

    private static final long serialVersionUID = 4515679320571283490L;

    public TeamNotFoundException() {
        super();
    }

    public TeamNotFoundException(final String message) {
        super(message);
    }

    public TeamNotFoundException(final String message, final Throwable throwable) {
        super(message, throwable);
    }
}

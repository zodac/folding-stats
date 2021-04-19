package me.zodac.folding.api.exception;

public class UserNotFoundException extends NotFoundException {

    private static final long serialVersionUID = 4515679320571283490L;

    public UserNotFoundException() {
        super();
    }

    public UserNotFoundException(final String message) {
        super(message);
    }

    public UserNotFoundException(final String message, final Throwable throwable) {
        super(message, throwable);
    }
}

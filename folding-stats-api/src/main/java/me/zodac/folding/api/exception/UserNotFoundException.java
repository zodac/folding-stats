package me.zodac.folding.api.exception;

public class UserNotFoundException extends NotFoundException {

    private static final long serialVersionUID = -8218328365953705107L;

    public UserNotFoundException(final int id) {
        super("user", id);
    }
}

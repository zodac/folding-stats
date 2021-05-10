package me.zodac.folding.api.tc.exception;

public class UserNotFoundException extends NotFoundException {

    private static final long serialVersionUID = 4515679320571283490L;

    public UserNotFoundException(final int id) {
        super("user", id);
    }
}

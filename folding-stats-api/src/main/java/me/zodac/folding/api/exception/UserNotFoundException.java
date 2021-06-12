package me.zodac.folding.api.exception;

/**
 * Implementation of {@link NotFoundException} for {@link me.zodac.folding.api.tc.User}.
 */
public class UserNotFoundException extends NotFoundException {

    private static final long serialVersionUID = -8218328365953705107L;

    /**
     * Create with a {@link me.zodac.folding.api.tc.User} ID.
     *
     * @param id the {@link me.zodac.folding.api.tc.User} ID
     */
    public UserNotFoundException(final int id) {
        super("user", id);
    }
}

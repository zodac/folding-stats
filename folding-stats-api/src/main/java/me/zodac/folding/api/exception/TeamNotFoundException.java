package me.zodac.folding.api.exception;

/**
 * Implementation of {@link NotFoundException} for {@link me.zodac.folding.api.tc.Team}.
 */
public class TeamNotFoundException extends NotFoundException {

    private static final long serialVersionUID = 3034609756524440126L;

    /**
     * Create with a {@link me.zodac.folding.api.tc.Team} ID.
     *
     * @param id the {@link me.zodac.folding.api.tc.Team} ID
     */
    public TeamNotFoundException(final int id) {
        super("team", id);
    }
}

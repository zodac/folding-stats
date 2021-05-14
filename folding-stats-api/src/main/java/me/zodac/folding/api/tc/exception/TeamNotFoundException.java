package me.zodac.folding.api.tc.exception;

public class TeamNotFoundException extends NotFoundException {

    private static final long serialVersionUID = 3034609756524440126L;

    public TeamNotFoundException(final int id) {
        super("team", id);
    }
}

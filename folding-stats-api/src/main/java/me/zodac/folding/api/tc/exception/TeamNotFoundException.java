package me.zodac.folding.api.tc.exception;

public class TeamNotFoundException extends NotFoundException {

    private static final long serialVersionUID = 4515679320571283490L;

    public TeamNotFoundException(final int id) {
        super("team", id);
    }
}

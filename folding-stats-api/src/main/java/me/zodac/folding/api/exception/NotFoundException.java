package me.zodac.folding.api.exception;

public class NotFoundException extends Exception {

    private static final long serialVersionUID = 4515679320571283490L;

    private final String type;
    private final int id;

    public NotFoundException(final String type, final int id) {
        super();
        this.type = type;
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public int getId() {
        return id;
    }
}

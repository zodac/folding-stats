package me.zodac.folding.api.tc.exception;

public class NotFoundException extends Exception {
    
    private static final long serialVersionUID = 4925691536809868407L;

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

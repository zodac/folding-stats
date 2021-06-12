package me.zodac.folding.api.exception;

/**
 * Thrown then an element ({@link me.zodac.folding.api.tc.Hardware}, {@link me.zodac.folding.api.tc.Team} or {@link me.zodac.folding.api.tc.User} can
 * not be found.
 */
public class NotFoundException extends Exception {

    private static final long serialVersionUID = 4925691536809868407L;

    private final String type;
    private final int id;

    /**
     * Create with the type and its ID.
     *
     * @param type the type of the missing element
     * @param id   the ID of the missing element
     */
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

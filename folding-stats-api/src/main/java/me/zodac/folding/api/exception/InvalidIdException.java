package me.zodac.folding.api.exception;

/**
 *
 */
public class InvalidIdException extends Exception {

    private static final long serialVersionUID = 4818274484760492269L;
    private final String id;

    public InvalidIdException(final String id, final Throwable throwable) {
        super(throwable);
        this.id = id;
    }

    public String getId() {
        return id;
    }
}

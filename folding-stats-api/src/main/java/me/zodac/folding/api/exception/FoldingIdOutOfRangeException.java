package me.zodac.folding.api.exception;

public class FoldingIdOutOfRangeException extends Exception {

    private static final long serialVersionUID = 4818274484760492269L;
    private final int id;

    public FoldingIdOutOfRangeException(final int id) {
        super();
        this.id = id;
    }

    public int getId() {
        return id;
    }
}

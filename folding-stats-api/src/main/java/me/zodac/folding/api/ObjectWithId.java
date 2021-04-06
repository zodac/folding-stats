package me.zodac.folding.api;

/**
 * Simple interface for POJOs that exposes a method {@link #getId()}. Since almost all POJOs are persisted in the DB
 * with an ID, this allows us to abstract the items a bit.
 */
public interface ObjectWithId {

    /**
     * Returns the ID of the {@link ObjectWithId}.
     *
     * @return the ID
     */
    int getId();
}

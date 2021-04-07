package me.zodac.folding.api;

/**
 * Simple interface for POJOs that exposes a method {@link #getId()}. Since almost all POJOs are persisted
 * with an ID, this allows us to abstract the items a bit.
 */
public interface PojoWithId {

    /**
     * Returns the ID of the {@link PojoWithId}.
     *
     * @return the ID
     */
    int getId();
}

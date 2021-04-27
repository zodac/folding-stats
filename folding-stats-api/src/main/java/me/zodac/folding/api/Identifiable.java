package me.zodac.folding.api;

/**
 * Simple interface for POJOs that have some natural identifier.
 */
public interface Identifiable {

    /**
     * Returns the ID of the {@link Identifiable}.
     *
     * @return the ID
     */
    int getId();
}

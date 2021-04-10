package me.zodac.folding.api;

import java.io.Serializable;

/**
 * Simple interface for POJOs that have some natural identifier.
 */
public interface Identifiable extends Serializable {

    /**
     * Returns the ID of the {@link Identifiable}.
     *
     * @return the ID
     */
    int getId();
}

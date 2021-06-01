package me.zodac.folding.api;

/**
 * Simple interface for POJOs are sent in a REST request to the service.
 */
public interface RequestPojo {

    /**
     * Returns the ID of the {@link RequestPojo}.
     *
     * @return the ID
     */
    int getId();
}

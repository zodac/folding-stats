package me.zodac.folding.api;

/**
 * Simple interface for POJOs are returned from the service.
 */
public interface ResponsePojo {

    /**
     * Returns the ID of the {@link ResponsePojo}.
     *
     * @return the ID
     */
    int getId();

    /**
     * Checks if the input {@link RequestPojo} is equal to the {@link ResponsePojo}.
     *
     * <p>
     * While the {@link RequestPojo} will likely not be a complete match, there should be enough fields to verify
     * if it is the same as an existing {@link ResponsePojo}.
     *
     * @param inputRequest input {@link RequestPojo}
     * @return <code>true</code> if the input{@link RequestPojo} is equal to the {@link ResponsePojo}
     */
    boolean isEqualRequest(final RequestPojo inputRequest);
}

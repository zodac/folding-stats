package me.zodac.folding.api.ejb;


import me.zodac.folding.api.tc.Hardware;

import java.util.Collection;
import java.util.Optional;

/**
 * In order to decouple the REST layer from any business requirements, we move that logic into this interface, to be
 * implemented as an EJB. This should simplify the REST layer to simply validate incoming requests and forward to here.
 */
public interface BusinessLogic {

    // Simple CRUD

    /**
     * Creates a {@link Hardware}.
     *
     * @param hardware the {@link Hardware} to create
     * @return the created {@link Hardware}, with ID
     */
    Hardware createHardware(final Hardware hardware);

    /**
     * Retrieves a {@link Hardware}.
     *
     * @param hardwareId the ID of the {@link Hardware} to retrieve
     * @return an {@link Optional} of the retrieved {@link Hardware}
     */
    Optional<Hardware> getHardware(final int hardwareId);

    /**
     * Retrieves all {@link Hardware}.
     *
     * @return a {@link Collection} of the retrieved {@link Hardware}
     */
    Collection<Hardware> getAllHardware();

    /**
     * Deletes a {@link Hardware}.
     *
     * @param hardware the {@link Hardware} to delete
     */
    void deleteHardware(final Hardware hardware);


    // Complex CRUD

    /**
     * Retrieves a {@link Hardware} with the given name.
     * <p>
     * Checks {@link #getAllHardware()} for a case-insensitive match on the {@link Hardware} name.
     * <p>
     * If there is more than one result (though there should not be!), there is no guarantee on which will be
     * returned.
     *
     * @param hardwareName the name of the {@link Hardware}
     * @return an {@link Optional} of the matching {@link Hardware}
     */
    Optional<Hardware> getHardwareWithName(final String hardwareName);
}

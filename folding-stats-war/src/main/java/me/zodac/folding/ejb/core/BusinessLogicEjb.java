package me.zodac.folding.ejb.core;


import me.zodac.folding.api.ejb.BusinessLogic;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.User;

import javax.ejb.Singleton;
import java.util.Collection;
import java.util.Optional;

/**
 * {@link Singleton} EJB implementation of {@link BusinessLogic}.
 * <p>
 * For the most part, this will serve as a wrapper to {@link Storage}, which knows how to perform CRUD operations on
 * the backend storage and caches. But since some logic is needed for special cases (like retrieving the latest
 * Folding@Home stats for a {@link User} when it is created), we implement that logic here, and delegate any CRUD
 * needs to {@link Storage}.
 */
@Singleton
public class BusinessLogicEjb implements BusinessLogic {

    private static final Storage STORAGE = Storage.getInstance();

    // Simple CRUD

    @Override
    public Hardware createHardware(final Hardware hardware) {
        return STORAGE.createHardware(hardware);
    }

    @Override
    public Optional<Hardware> getHardware(final int hardwareId) {
        return STORAGE.getHardware(hardwareId);
    }

    @Override
    public Collection<Hardware> getAllHardware() {
        return STORAGE.getAllHardware();
    }

    @Override
    public void deleteHardware(final int hardwareId) {
        STORAGE.deleteHardware(hardwareId);
    }

    // Complex CRUD

    @Override
    public Optional<Hardware> getHardwareWithName(final String hardwareName) {
        return getAllHardware()
                .stream()
                .filter(hardware -> hardware.getHardwareName().equalsIgnoreCase(hardwareName))
                .findAny();
    }
}

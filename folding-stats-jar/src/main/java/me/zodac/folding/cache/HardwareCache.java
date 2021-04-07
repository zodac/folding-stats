package me.zodac.folding.cache;

import me.zodac.folding.api.Hardware;

public class HardwareCache extends AbstractPojoCache<Hardware> {

    private static HardwareCache INSTANCE = null;

    private HardwareCache() {
        super();
    }

    public static HardwareCache getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new HardwareCache();
        }

        return INSTANCE;
    }
}

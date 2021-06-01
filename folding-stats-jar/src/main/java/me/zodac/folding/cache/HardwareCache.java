package me.zodac.folding.cache;

import me.zodac.folding.api.tc.Hardware;

public class HardwareCache extends AbstractCache<Hardware> {

    private static HardwareCache INSTANCE = null;

    private HardwareCache() {
        super();
    }

    @Override
    protected String elementType() {
        return "hardware";
    }

    public static HardwareCache get() {
        if (INSTANCE == null) {
            INSTANCE = new HardwareCache();
        }

        return INSTANCE;
    }
}

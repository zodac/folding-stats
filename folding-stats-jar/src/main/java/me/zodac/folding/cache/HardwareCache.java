package me.zodac.folding.cache;

import me.zodac.folding.api.tc.Hardware;

public final class HardwareCache extends AbstractCache<Hardware> {

    private static final HardwareCache INSTANCE = new HardwareCache();

    private HardwareCache() {
        super();
    }

    @Override
    protected String elementType() {
        return "hardware";
    }

    public static HardwareCache get() {
        return INSTANCE;
    }
}

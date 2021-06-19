package me.zodac.folding.cache;

import me.zodac.folding.api.tc.Hardware;

/**
 * Implementation of {@link BaseCache} for {@link Hardware}s.
 *
 * <p>
 * <b>key:</b> {@link Hardware} ID
 *
 * <p>
 * <b>value:</b> {@link Hardware}
 */
public final class HardwareCache extends BaseCache<Hardware> {

    private static final HardwareCache INSTANCE = new HardwareCache();

    private HardwareCache() {
        super();
    }

    /**
     * Returns a singleton instance of {@link HardwareCache}.
     *
     * @return the {@link HardwareCache}
     */
    public static HardwareCache getInstance() {
        return INSTANCE;
    }
}

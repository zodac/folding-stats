package me.zodac.folding.cache;

import me.zodac.folding.api.tc.stats.OffsetTcStats;

/**
 * Implementation of {@link BaseCache} for {@link OffsetTcStats}s.
 *
 * <p>
 * <b>key:</b> the {@link me.zodac.folding.api.tc.User} ID
 *
 * <p>
 * <b>value:</b> {@link OffsetTcStats}
 */
public final class OffsetTcStatsCache extends BaseCache<OffsetTcStats> {

    private static final OffsetTcStatsCache INSTANCE = new OffsetTcStatsCache();

    private OffsetTcStatsCache() {
        super();
    }

    /**
     * Returns a singleton instance of {@link OffsetTcStatsCache}.
     *
     * @return the {@link OffsetTcStatsCache}
     */
    public static OffsetTcStatsCache getInstance() {
        return INSTANCE;
    }
}

package me.zodac.folding.cache;

import me.zodac.folding.api.tc.stats.RetiredUserTcStats;

/**
 * Implementation of {@link BaseCache} for {@link RetiredUserTcStats}s.
 *
 * <p>
 * <b>key:</b> {@link RetiredUserTcStats} retired user ID
 *
 * <p>
 * <b>value:</b> {@link RetiredUserTcStats}
 */
public final class RetiredTcStatsCache extends BaseCache<RetiredUserTcStats> {

    private static final RetiredTcStatsCache INSTANCE = new RetiredTcStatsCache();

    private RetiredTcStatsCache() {
        super();
    }

    /**
     * Returns a singleton instance of {@link RetiredTcStatsCache}.
     *
     * @return the {@link RetiredTcStatsCache}
     */
    public static RetiredTcStatsCache getInstance() {
        return INSTANCE;
    }
}

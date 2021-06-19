package me.zodac.folding.cache;

import me.zodac.folding.api.tc.stats.Stats;

/**
 * Implementation of {@link BaseCache} for total {@link me.zodac.folding.api.tc.User} {@link Stats}s.
 *
 * <p>
 * <b>key:</b> the {@link me.zodac.folding.api.tc.User} ID
 *
 * <p>
 * <b>value:</b> {@link Stats}
 */
public final class TotalStatsCache extends BaseCache<Stats> {

    private static final TotalStatsCache INSTANCE = new TotalStatsCache();

    private TotalStatsCache() {
        super();
    }

    /**
     * Returns a singleton instance of {@link TotalStatsCache}.
     *
     * @return the {@link TotalStatsCache}
     */
    public static TotalStatsCache getInstance() {
        return INSTANCE;
    }
}

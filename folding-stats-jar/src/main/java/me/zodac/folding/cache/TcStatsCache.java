package me.zodac.folding.cache;

import me.zodac.folding.api.tc.stats.UserTcStats;

/**
 * Implementation of {@link BaseCache} for {@link UserTcStats}s.
 *
 * <p>
 * <b>key:</b> {@link UserTcStats} ID
 *
 * <p>
 * <b>value:</b> {@link UserTcStats}
 */
public final class TcStatsCache extends BaseCache<UserTcStats> {

    private static final TcStatsCache INSTANCE = new TcStatsCache();

    private TcStatsCache() {
        super();
    }

    /**
     * Returns a singleton instance of {@link TcStatsCache}.
     *
     * @return the {@link TcStatsCache}
     */
    public static TcStatsCache getInstance() {
        return INSTANCE;
    }
}

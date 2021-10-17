package me.zodac.folding.cache;

import me.zodac.folding.api.tc.stats.Stats;
import me.zodac.folding.api.tc.stats.UserStats;

/**
 * Implementation of {@link BaseCache} for initial {@link UserStats}s.
 *
 * <p>
 * <b>key:</b> the {@link me.zodac.folding.api.tc.User} ID
 *
 * <p>
 * <b>value:</b> {@link Stats}
 */
public final class InitialStatsCache extends BaseCache<UserStats> {

    private static final InitialStatsCache INSTANCE = new InitialStatsCache();

    private InitialStatsCache() {
        super();
    }

    /**
     * Returns a singleton instance of {@link InitialStatsCache}.
     *
     * @return the {@link InitialStatsCache}
     */
    public static InitialStatsCache getInstance() {
        return INSTANCE;
    }
}

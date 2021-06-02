package me.zodac.folding.cache;


import me.zodac.folding.api.tc.stats.Stats;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class TotalStatsCache {

    private static final TotalStatsCache INSTANCE = new TotalStatsCache();

    private transient final Map<Integer, Stats> totalStatsByUserId = new ConcurrentHashMap<>();

    private TotalStatsCache() {

    }

    public static TotalStatsCache get() {
        return INSTANCE;
    }

    public void add(final int userId, final Stats userTotalStats) {
        totalStatsByUserId.put(userId, userTotalStats);
    }

    public Optional<Stats> get(final int userId) {
        return Optional.ofNullable(totalStatsByUserId.get(userId));
    }

    public void clear() {
        totalStatsByUserId.clear();
    }
}

package me.zodac.folding.cache;


import me.zodac.folding.api.tc.stats.Stats;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class TotalStatsCache {

    private static TotalStatsCache INSTANCE = null;

    private final Map<Integer, Stats> totalStatsByUserId = new HashMap<>();

    private TotalStatsCache() {

    }

    public static TotalStatsCache get() {
        if (INSTANCE == null) {
            INSTANCE = new TotalStatsCache();
        }

        return INSTANCE;
    }

    public void add(final int userId, final Stats userTotalStats) {
        totalStatsByUserId.put(userId, userTotalStats);
    }

    public Optional<Stats> get(final int userId) {
        return Optional.ofNullable(totalStatsByUserId.get(userId));
    }
}

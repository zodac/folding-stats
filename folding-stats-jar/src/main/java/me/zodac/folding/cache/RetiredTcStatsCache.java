package me.zodac.folding.cache;


import me.zodac.folding.api.tc.stats.RetiredUserTcStats;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class RetiredTcStatsCache {

    private static RetiredTcStatsCache INSTANCE = null;

    private final Map<Integer, RetiredUserTcStats> retiredTcStatsByUserId = new HashMap<>();

    private RetiredTcStatsCache() {

    }

    public static RetiredTcStatsCache get() {
        if (INSTANCE == null) {
            INSTANCE = new RetiredTcStatsCache();
        }

        return INSTANCE;
    }

    public void add(final RetiredUserTcStats retiredTcStats) {
        retiredTcStatsByUserId.put(retiredTcStats.getRetiredUserId(), retiredTcStats);
    }

    public Optional<RetiredUserTcStats> get(final int retiredUserId) {
        return Optional.ofNullable(retiredTcStatsByUserId.get(retiredUserId));
    }
}

package me.zodac.folding.cache;


import me.zodac.folding.api.tc.stats.RetiredUserTcStats;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class RetiredTcStatsCache {

    private static final RetiredTcStatsCache INSTANCE = new RetiredTcStatsCache();

    private transient final Map<Integer, RetiredUserTcStats> retiredTcStatsByRetiredUserId = new ConcurrentHashMap<>();

    private RetiredTcStatsCache() {

    }

    public static RetiredTcStatsCache get() {
        return INSTANCE;
    }

    public void add(final RetiredUserTcStats retiredTcStats) {
        retiredTcStatsByRetiredUserId.put(retiredTcStats.getRetiredUserId(), retiredTcStats);
    }

    public Optional<RetiredUserTcStats> get(final int retiredUserId) {
        return Optional.ofNullable(retiredTcStatsByRetiredUserId.get(retiredUserId));
    }

    public Collection<RetiredUserTcStats> getAll() {
        return retiredTcStatsByRetiredUserId.values();
    }

    public void clear() {
        retiredTcStatsByRetiredUserId.clear();
    }
}

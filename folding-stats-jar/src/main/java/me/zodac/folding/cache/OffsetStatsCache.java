package me.zodac.folding.cache;


import me.zodac.folding.api.tc.stats.OffsetStats;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class OffsetStatsCache {

    private static final OffsetStatsCache INSTANCE = new OffsetStatsCache();

    private transient final Map<Integer, OffsetStats> offsetStatsByUserId = new ConcurrentHashMap<>();

    private OffsetStatsCache() {

    }

    public static OffsetStatsCache get() {
        return INSTANCE;
    }

    public void add(final int userId, final OffsetStats offsetStats) {
        offsetStatsByUserId.put(userId, offsetStats);
    }

    public Optional<OffsetStats> get(final int userId) {
        return Optional.ofNullable(offsetStatsByUserId.get(userId));
    }

    public void clearOffsets() {
        offsetStatsByUserId.clear();
    }
}

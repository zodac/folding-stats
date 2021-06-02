package me.zodac.folding.cache;


import me.zodac.folding.api.tc.stats.Stats;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class InitialStatsCache {

    private static final InitialStatsCache INSTANCE = new InitialStatsCache();

    private transient final Map<Integer, Stats> initialStatsByUserId = new ConcurrentHashMap<>();

    private InitialStatsCache() {

    }

    public static InitialStatsCache get() {
        return INSTANCE;
    }

    public void add(final int userId, final Stats userInitialStats) {
        initialStatsByUserId.put(userId, userInitialStats);
    }

    public Optional<Stats> get(final int userId) {
        return Optional.ofNullable(initialStatsByUserId.get(userId));
    }
}

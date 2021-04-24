package me.zodac.folding.cache;


import me.zodac.folding.api.tc.stats.Stats;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InitialStatsCache {

    private static InitialStatsCache INSTANCE = null;

    private final Map<Integer, Stats> initialStatsByUserId = new HashMap<>();

    private InitialStatsCache() {

    }

    public static InitialStatsCache get() {
        if (INSTANCE == null) {
            INSTANCE = new InitialStatsCache();
        }

        return INSTANCE;
    }

    public void add(final int userId, final Stats userInitialStats) {
        initialStatsByUserId.put(userId, userInitialStats);
    }

    public Optional<Stats> get(final int userId) {
        return Optional.ofNullable(initialStatsByUserId.get(userId));
    }
}

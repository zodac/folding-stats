package me.zodac.folding.cache.tc;

import me.zodac.folding.api.UserStats;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class TcStatsCache {

    private static TcStatsCache INSTANCE = null;

    private final Map<Integer, UserStats> initialStatsByUserId = new HashMap<>();
    private final Map<Integer, UserStats> currentStatsByUserId = new HashMap<>();

    private TcStatsCache() {

    }

    public static TcStatsCache get() {
        if (INSTANCE == null) {
            INSTANCE = new TcStatsCache();
        }

        return INSTANCE;
    }

    public void addInitialStats(final int userId, final UserStats userInitialStats) {
        initialStatsByUserId.put(userId, userInitialStats);
    }

    public Optional<UserStats> getInitialStatsForUser(final int userId) {
        return Optional.ofNullable(initialStatsByUserId.get(userId));
    }

    public void addCurrentStats(final int userId, final UserStats userCurrentStats) {
        // If no entry exists in the cache, first time we pull stats for the user is also the initial state
        if (!initialStatsByUserId.containsKey(userId)) {
            initialStatsByUserId.put(userId, userCurrentStats);
        }

        currentStatsByUserId.put(userId, userCurrentStats);
    }

    public Optional<UserStats> getCurrentStatsForUser(final int userId) {
        return Optional.ofNullable(currentStatsByUserId.get(userId));
    }
}

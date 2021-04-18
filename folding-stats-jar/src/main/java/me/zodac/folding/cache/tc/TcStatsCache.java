package me.zodac.folding.cache.tc;

import me.zodac.folding.api.Stats;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class TcStatsCache {

    private static TcStatsCache INSTANCE = null;

    private final Map<Integer, Stats> initialStatsByUserId = new HashMap<>();
    private final Map<Integer, Stats> currentStatsByUserId = new HashMap<>();

    private TcStatsCache() {

    }

    public static TcStatsCache get() {
        if (INSTANCE == null) {
            INSTANCE = new TcStatsCache();
        }

        return INSTANCE;
    }

    public void addInitialStats(final int userId, final Stats userInitialStats) {
        initialStatsByUserId.put(userId, userInitialStats);
    }

    public Optional<Stats> getInitialStatsForUser(final int userId) {
        return Optional.ofNullable(initialStatsByUserId.get(userId));
    }

    public void addCurrentStats(final int userId, final Stats userCurrentStats) {
        // If no entry exists in the cache, first time we pull stats for the user is also the initial state
        if (!initialStatsByUserId.containsKey(userId)) {
            initialStatsByUserId.put(userId, userCurrentStats);
        }

        currentStatsByUserId.put(userId, userCurrentStats);
    }

    public Optional<Stats> getCurrentStatsForUser(final int userId) {
        return Optional.ofNullable(currentStatsByUserId.get(userId));
    }

    public void resetInitialCache() {
        emptyInitialCache();
        initialStatsByUserId.putAll(currentStatsByUserId);
    }

    public void emptyInitialCache() {
        initialStatsByUserId.clear();
    }

    public void emptyCurrentCache() {
        currentStatsByUserId.clear();
    }
}

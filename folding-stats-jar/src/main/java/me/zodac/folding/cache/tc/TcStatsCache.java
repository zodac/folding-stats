package me.zodac.folding.cache.tc;

import me.zodac.folding.api.UserStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class TcStatsCache {

    private static final Logger LOGGER = LoggerFactory.getLogger(TcStatsCache.class);

    private static TcStatsCache INSTANCE = null;

    private final Map<Integer, UserStats> initialStatsByUserId = new HashMap<>();
    private final Map<Integer, UserStats> currentStatsByUserId = new HashMap<>();

    private TcStatsCache() {

    }


    public void print() {
        LOGGER.info("Initial: {}", initialStatsByUserId);
        LOGGER.info("Current: {}", currentStatsByUserId);
    }

    public static TcStatsCache getInstance() {
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

    public boolean haveInitialStatsForUser(final int userId) {
        return initialStatsByUserId.containsKey(userId);
    }

    public void addCurrentStats(final int userId, final UserStats userCurrentStats) {
        currentStatsByUserId.put(userId, userCurrentStats);
    }

    public Optional<UserStats> getCurrentStatsForUser(final int userId) {
        return Optional.ofNullable(currentStatsByUserId.get(userId));
    }

    public boolean haveCurrentStatsForUser(final int userId) {
        return currentStatsByUserId.containsKey(userId);
    }
}

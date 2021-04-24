package me.zodac.folding.cache;


import me.zodac.folding.api.tc.stats.Stats;
import me.zodac.folding.api.tc.stats.UserStats;

import java.util.HashMap;
import java.util.List;
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

    public void addAll(final List<UserStats> userStats) {
        for (final UserStats statsForUser : userStats) {
            totalStatsByUserId.put(statsForUser.getUserId(), statsForUser.getStats());
        }
    }

    public Optional<Stats> get(final int userId) {
        return Optional.ofNullable(totalStatsByUserId.get(userId));
    }
}

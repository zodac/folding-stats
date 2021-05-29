package me.zodac.folding.cache;


import me.zodac.folding.api.tc.stats.UserTcStats;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class TcStatsCache {

    private static TcStatsCache INSTANCE = null;

    private final Map<Integer, UserTcStats> tcStatsByUserId = new HashMap<>();

    private TcStatsCache() {

    }

    public static TcStatsCache get() {
        if (INSTANCE == null) {
            INSTANCE = new TcStatsCache();
        }

        return INSTANCE;
    }

    public void add(final int userId, final UserTcStats userTcStats) {
        tcStatsByUserId.put(userId, userTcStats);
    }

    public Optional<UserTcStats> get(final int userId) {
        return Optional.ofNullable(tcStatsByUserId.get(userId));
    }

    public void clear() {
        tcStatsByUserId.clear();
    }
}

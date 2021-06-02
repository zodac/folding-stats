package me.zodac.folding.cache;


import me.zodac.folding.api.tc.stats.UserTcStats;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class TcStatsCache {

    private static final TcStatsCache INSTANCE = new TcStatsCache();

    private transient final Map<Integer, UserTcStats> tcStatsByUserId = new ConcurrentHashMap<>();

    private TcStatsCache() {

    }

    public static TcStatsCache get() {
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

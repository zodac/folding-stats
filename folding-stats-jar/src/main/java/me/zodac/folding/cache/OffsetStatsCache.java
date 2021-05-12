package me.zodac.folding.cache;


import me.zodac.folding.api.tc.stats.UserStatsOffset;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class OffsetStatsCache {

    private static OffsetStatsCache INSTANCE = null;

    private final Map<Integer, UserStatsOffset> offsetStatsByUserId = new HashMap<>();

    private OffsetStatsCache() {

    }

    public static OffsetStatsCache get() {
        if (INSTANCE == null) {
            INSTANCE = new OffsetStatsCache();
        }

        return INSTANCE;
    }

    public void add(final int userId, final UserStatsOffset userStatsOffset) {
        offsetStatsByUserId.put(userId, userStatsOffset);
    }

    public Optional<UserStatsOffset> get(final int userId) {
        return Optional.ofNullable(offsetStatsByUserId.get(userId));
    }

    public void clearOffsets() {
        offsetStatsByUserId.clear();
    }
}

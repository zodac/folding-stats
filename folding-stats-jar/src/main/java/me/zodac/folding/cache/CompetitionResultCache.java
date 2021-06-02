package me.zodac.folding.cache;

import me.zodac.folding.rest.api.tc.CompetitionResult;

import java.util.Optional;

public final class CompetitionResultCache {

    private static final CompetitionResultCache INSTANCE = new CompetitionResultCache();

    private transient CompetitionResult cachedResult;

    private CompetitionResultCache() {

    }

    public static CompetitionResultCache get() {
        return INSTANCE;
    }

    public boolean hasCachedResult() {
        return cachedResult != null;
    }

    public void add(final CompetitionResult competitionResult) {
        cachedResult = competitionResult;
    }

    public Optional<CompetitionResult> getResult() {
        return Optional.ofNullable(cachedResult);
    }
}

package me.zodac.folding.cache;

import me.zodac.folding.rest.api.tc.CompetitionSummary;

import java.util.Optional;

public final class CompetitionResultCache {

    private static final CompetitionResultCache INSTANCE = new CompetitionResultCache();

    private transient CompetitionSummary cachedResult;

    private CompetitionResultCache() {

    }

    public static CompetitionResultCache get() {
        return INSTANCE;
    }

    public boolean hasCachedResult() {
        return cachedResult != null;
    }

    public void add(final CompetitionSummary competitionSummary) {
        cachedResult = competitionSummary;
    }

    public Optional<CompetitionSummary> getResult() {
        return Optional.ofNullable(cachedResult);
    }
}

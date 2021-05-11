package me.zodac.folding.cache;

import me.zodac.folding.rest.api.tc.CompetitionResult;

import java.util.Optional;

public final class CompetitionResultCache {

    private static CompetitionResult cachedResult = null;

    private CompetitionResultCache() {

    }

    public static boolean hasCachedResult() {
        return cachedResult != null;
    }

    public static void add(final CompetitionResult competitionResult) {
        cachedResult = competitionResult;
    }

    public static Optional<CompetitionResult> get() {
        return Optional.ofNullable(cachedResult);
    }
}

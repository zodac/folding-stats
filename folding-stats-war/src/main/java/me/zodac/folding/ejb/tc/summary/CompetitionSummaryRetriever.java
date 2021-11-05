package me.zodac.folding.ejb.tc.summary;

import java.util.Optional;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import me.zodac.folding.SystemStateManager;
import me.zodac.folding.api.state.SystemState;
import me.zodac.folding.cache.CompetitionSummaryCache;
import me.zodac.folding.ejb.api.BusinessLogic;
import me.zodac.folding.rest.api.tc.CompetitionSummary;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * {@link Singleton} EJB that serves as a cache/{@link me.zodac.folding.api.state.SystemState} checker for {@link CompetitionSummaryGenerator}.
 */
@Singleton
public class CompetitionSummaryRetriever {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final CompetitionSummaryCache COMPETITION_SUMMARY_CACHE = CompetitionSummaryCache.getInstance();

    @EJB
    private BusinessLogic businessLogic;

    /**
     * Retrieves the current {@link CompetitionSummary}.
     *
     * <p>
     * Will retrieve a cached instance if one exists and the {@link SystemState} is not {@link SystemState#WRITE_EXECUTED}.
     *
     * @return the latest {@link CompetitionSummary}
     * @see CompetitionSummaryGenerator#generate(BusinessLogic)
     */
    public CompetitionSummary retrieve() {
        if (SystemStateManager.current() != SystemState.WRITE_EXECUTED && COMPETITION_SUMMARY_CACHE.hasCachedResult()) {
            LOGGER.debug("System is not in state {} and has a cached TC result, using cache", SystemState.WRITE_EXECUTED);

            final Optional<CompetitionSummary> cachedCompetitionResult = COMPETITION_SUMMARY_CACHE.get();
            if (cachedCompetitionResult.isPresent()) {
                return cachedCompetitionResult.get();
            } else {
                LOGGER.warn("Cache said it had TC result, but none was returned! Calculating new TC result");
            }
        }

        final CompetitionSummary competitionSummary = CompetitionSummaryGenerator.generate(businessLogic);
        COMPETITION_SUMMARY_CACHE.add(competitionSummary);
        SystemStateManager.next(SystemState.AVAILABLE);

        return competitionSummary;
    }
}

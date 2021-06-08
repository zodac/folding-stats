package me.zodac.folding.api.exception;

/**
 * {@link Exception} used when retrieving {@link me.zodac.folding.api.tc.User}/{@link me.zodac.folding.api.tc.Team} {@link me.zodac.folding.api.tc.stats.Stats}, but none are available.
 * Not to be used when errors occur during retrieval, but when no {@link me.zodac.folding.api.tc.stats.Stats} for a given time period exist.
 */
public class NoStatsAvailableException extends Exception {

    private static final long serialVersionUID = -4992232296280738264L;

    public NoStatsAvailableException(final String type, final int id) {
        super(String.format("Unable to find stats for %s with ID: '%s'", type, id));
    }
}

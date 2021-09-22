package me.zodac.folding.api;

/**
 * The various states that the system is performing with stats parsing.
 */
public enum ParsingState {

    /**
     * System is parsing <code>Team Competition</code> stats.
     */
    PARSING_TEAM_COMPETITION_STATS,

    /**
     * System is not parsing any stats.
     */
    NOT_PARSING_STATS
}

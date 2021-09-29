package me.zodac.folding.api;

/**
 * The various states that the system is performing with stats parsing.
 */
public enum ParsingState {

    /**
     * System is not parsing any stats.
     */
    DISABLED,

    /**
     * System is parsing <code>Team Competition</code> stats.
     */
    ENABLED_TEAM_COMPETITION
}

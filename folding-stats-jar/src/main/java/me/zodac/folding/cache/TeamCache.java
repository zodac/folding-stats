package me.zodac.folding.cache;

import me.zodac.folding.api.tc.Team;

/**
 * Implementation of {@link BaseCache} for {@link Team}s.
 *
 * <p>
 * <b>key:</b> {@link Team} ID
 *
 * <p>
 * <b>value:</b> {@link Team}
 */
public final class TeamCache extends BaseCache<Team> {

    private static final TeamCache INSTANCE = new TeamCache();

    private TeamCache() {
        super();
    }

    /**
     * Returns a singleton instance of {@link TeamCache}.
     *
     * @return the {@link TeamCache}
     */
    public static TeamCache getInstance() {
        return INSTANCE;
    }
}

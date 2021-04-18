package me.zodac.folding.cache;


import me.zodac.folding.api.tc.Team;

public class TeamCache extends AbstractIdentifiableCache<Team> {

    private static TeamCache INSTANCE = null;

    private TeamCache() {
        super();
    }

    public static TeamCache get() {
        if (INSTANCE == null) {
            INSTANCE = new TeamCache();
        }

        return INSTANCE;
    }
}

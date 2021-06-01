package me.zodac.folding.cache;


import me.zodac.folding.api.tc.Team;

public class TeamCache extends AbstractCache<Team> {

    private static TeamCache INSTANCE = null;

    private TeamCache() {
        super();
    }

    @Override
    protected String elementType() {
        return "team";
    }

    public static TeamCache get() {
        if (INSTANCE == null) {
            INSTANCE = new TeamCache();
        }

        return INSTANCE;
    }
}

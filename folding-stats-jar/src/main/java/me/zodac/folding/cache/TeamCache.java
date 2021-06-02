package me.zodac.folding.cache;


import me.zodac.folding.api.tc.Team;

public final class TeamCache extends AbstractCache<Team> {

    private static final TeamCache INSTANCE = new TeamCache();

    private TeamCache() {
        super();
    }

    @Override
    protected String elementType() {
        return "team";
    }

    public static TeamCache get() {
        return INSTANCE;
    }
}

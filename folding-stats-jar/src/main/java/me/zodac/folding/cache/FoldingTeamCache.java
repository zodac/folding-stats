package me.zodac.folding.cache;

import me.zodac.folding.api.FoldingTeam;

public class FoldingTeamCache extends AbstractPojoCache<FoldingTeam> {

    private static FoldingTeamCache INSTANCE = null;

    private FoldingTeamCache() {
        super();
    }

    public static FoldingTeamCache getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FoldingTeamCache();
        }

        return INSTANCE;
    }
}

package me.zodac.folding.cache;

import me.zodac.folding.api.FoldingUser;

public class FoldingUserCache extends AbstractCache<FoldingUser> {

    private static FoldingUserCache INSTANCE = null;

    private FoldingUserCache() {
        super();
    }

    public static FoldingUserCache getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FoldingUserCache();
        }

        return INSTANCE;
    }
}

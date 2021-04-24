package me.zodac.folding.cache;

import me.zodac.folding.api.tc.User;

public class UserCache extends AbstractIdentifiableCache<User> {

    private static UserCache INSTANCE = null;

    private UserCache() {
        super();
    }

    @Override
    protected String elementType() {
        return "user";
    }

    public static UserCache get() {
        if (INSTANCE == null) {
            INSTANCE = new UserCache();
        }

        return INSTANCE;
    }
}

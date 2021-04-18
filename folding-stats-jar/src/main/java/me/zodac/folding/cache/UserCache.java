package me.zodac.folding.cache;

import me.zodac.folding.api.tc.User;

public class UserCache extends AbstractIdentifiableCache<User> {

    private static UserCache INSTANCE = null;

    private UserCache() {
        super();
    }

    public static UserCache get() {
        if (INSTANCE == null) {
            INSTANCE = new UserCache();
        }

        return INSTANCE;
    }
}

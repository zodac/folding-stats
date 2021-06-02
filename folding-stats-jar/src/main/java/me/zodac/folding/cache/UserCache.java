package me.zodac.folding.cache;

import me.zodac.folding.api.tc.User;

public final class UserCache extends AbstractCache<User> {

    private static final UserCache INSTANCE = new UserCache();

    private UserCache() {
        super();
    }

    @Override
    protected String elementType() {
        return "user";
    }

    public static UserCache get() {
        return INSTANCE;
    }
}

package me.zodac.folding.cache;

import me.zodac.folding.api.tc.User;

/**
 * Implementation of {@link BaseCache} for {@link User}s.
 *
 * <p>
 * <b>key:</b> {@link User} ID
 *
 * <p>
 * <b>value:</b> {@link User}
 */
public final class UserCache extends BaseCache<User> {

    private static final UserCache INSTANCE = new UserCache();

    private UserCache() {
        super();
    }

    /**
     * Returns a singleton instance of {@link UserCache}.
     *
     * @return the {@link UserCache}
     */
    public static UserCache getInstance() {
        return INSTANCE;
    }
}

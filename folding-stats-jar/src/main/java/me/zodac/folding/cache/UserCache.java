package me.zodac.folding.cache;

import me.zodac.folding.api.tc.User;

public class UserCache extends AbstractIdentifiableCache<User> {

    // TODO: [zodac] Do I really need a singleton instance? May as well make it a static class?
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

//    public Map<Integer, User> getRetired() {
//        return super.getAll()
//                .stream()
//                .filter(User::isRetired)
//                .collect(toMap(User::getId, retiredUser -> retiredUser));
//    }
}

package me.zodac.folding.cache;

import me.zodac.folding.api.FoldingUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FoldingUsersCache {

    private static FoldingUsersCache INSTANCE = null;

    private final Map<Integer, FoldingUser> foldingUsersById;

    private FoldingUsersCache() {
        this.foldingUsersById = new HashMap<>();
    }

    public static FoldingUsersCache getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FoldingUsersCache();
        }

        return INSTANCE;
    }

    public void addToCache(final FoldingUser foldingUser) {
        foldingUsersById.put(foldingUser.getId(), foldingUser);
    }

    public FoldingUser getUserById(final int id) {
        return foldingUsersById.get(id);
    }

    public List<FoldingUser> getAllUsers() {
        return new ArrayList<>(foldingUsersById.values());
    }
}

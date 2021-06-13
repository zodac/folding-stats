package me.zodac.folding.client.java.response;

import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.net.http.HttpResponse;
import java.util.Collection;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.client.java.util.RestUtilConstants;

/**
 * Utility class used to parse a {@link HttpResponse} returned from {@link me.zodac.folding.client.java.request.TeamRequestSender}.
 */
public final class TeamResponseParser {

    private TeamResponseParser() {

    }

    /**
     * Returns the {@link Team}s retrieved by {@link me.zodac.folding.client.java.request.TeamRequestSender#getAll()}.
     *
     * @param response the {@link HttpResponse} to parse
     * @return the retrieved {@link Team}s
     */
    public static Collection<Team> getAll(final HttpResponse<String> response) {
        final Type collectionType = new TypeToken<Collection<Team>>() {
        }.getType();
        return RestUtilConstants.GSON.fromJson(response.body(), collectionType);
    }

    /**
     * Returns the {@link Team} retrieved by {@link me.zodac.folding.client.java.request.TeamRequestSender#get(int)}.
     *
     * @param response the {@link HttpResponse} to parse
     * @return the retrieved {@link Team}
     */
    public static Team get(final HttpResponse<String> response) {
        return RestUtilConstants.GSON.fromJson(response.body(), Team.class);
    }

    /**
     * Returns the {@link Team} created by
     * {@link me.zodac.folding.client.java.request.TeamRequestSender#create(me.zodac.folding.rest.api.tc.request.TeamRequest)}.
     *
     * @param response the {@link HttpResponse} to parse
     * @return the created {@link Team}
     */
    public static Team create(final HttpResponse<String> response) {
        return RestUtilConstants.GSON.fromJson(response.body(), Team.class);
    }

    /**
     * Returns the {@link Team} updated by
     * {@link me.zodac.folding.client.java.request.TeamRequestSender#update(int, me.zodac.folding.rest.api.tc.request.TeamRequest)}.
     *
     * @param response the {@link HttpResponse} to parse
     * @return the updated {@link Team}
     */
    public static Team update(final HttpResponse<String> response) {
        return RestUtilConstants.GSON.fromJson(response.body(), Team.class);
    }
}
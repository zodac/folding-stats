package me.zodac.folding.client.java.response;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import lombok.experimental.UtilityClass;
import me.zodac.folding.api.tc.Team;

import java.lang.reflect.Type;
import java.net.http.HttpResponse;
import java.util.Collection;

/**
 * {@link UtilityClass} used to parse a {@link HttpResponse} returned from {@link me.zodac.folding.client.java.request.TeamRequestSender}.
 */
@UtilityClass
public final class TeamResponseParser {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    /**
     * Returns the {@link Team}s retrieved by {@link me.zodac.folding.client.java.request.TeamRequestSender#getAll()}.
     *
     * @param response the {@link HttpResponse} to parse
     * @return the retrieved {@link Team}s
     */
    public static Collection<Team> getAll(final HttpResponse<String> response) {
        final Type collectionType = new TypeToken<Collection<Team>>() {
        }.getType();
        return GSON.fromJson(response.body(), collectionType);
    }

    /**
     * Returns the {@link Team} retrieved by {@link me.zodac.folding.client.java.request.TeamRequestSender#get(int)}.
     *
     * @param response the {@link HttpResponse} to parse
     * @return the retrieved {@link Team}
     */
    public static Team get(final HttpResponse<String> response) {
        return GSON.fromJson(response.body(), Team.class);
    }

    /**
     * Returns the {@link Team} created by {@link me.zodac.folding.client.java.request.TeamRequestSender#create(me.zodac.folding.rest.api.tc.request.TeamRequest)}.
     *
     * @param response the {@link HttpResponse} to parse
     * @return the created {@link Team}
     */
    public static Team create(final HttpResponse<String> response) {
        return GSON.fromJson(response.body(), Team.class);
    }

    /**
     * Returns the {@link Team} updated by {@link me.zodac.folding.client.java.request.TeamRequestSender#update(me.zodac.folding.rest.api.tc.request.TeamRequest)}.
     *
     * @param response the {@link HttpResponse} to parse
     * @return the updated {@link Team}
     */
    public static Team update(final HttpResponse<String> response) {
        return GSON.fromJson(response.body(), Team.class);
    }
}
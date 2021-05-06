package me.zodac.folding.client.java.response;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import lombok.experimental.UtilityClass;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.client.java.request.TeamRequestSender;
import me.zodac.folding.client.java.request.UserRequestSender;

import java.lang.reflect.Type;
import java.net.http.HttpResponse;
import java.util.Collection;

/**
 * {@link UtilityClass} used to parse a {@link HttpResponse} returned from {@link TeamRequestSender}.
 */
@UtilityClass
public final class TeamResponseParser {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    /**
     * Returns the {@link Team}s retrieved by {@link UserRequestSender#getAll()}.
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
     * Returns the {@link Team} retrieved by {@link TeamRequestSender#get(int)}.
     *
     * @param response the {@link HttpResponse} to parse
     * @return the retrieved {@link Team}
     */
    public static Team get(final HttpResponse<String> response) {
        return GSON.fromJson(response.body(), Team.class);
    }

    /**
     * Returns the {@link Team} created by {@link TeamRequestSender#create(Team)}.
     *
     * @param response the {@link HttpResponse} to parse
     * @return the created {@link Team}
     */
    public static Team create(final HttpResponse<String> response) {
        return GSON.fromJson(response.body(), Team.class);
    }

    /**
     * Returns the {@link Team} updated by {@link TeamRequestSender#update(Team)}.
     *
     * @param response the {@link HttpResponse} to parse
     * @return the updated {@link Team}
     */
    public static Team update(final HttpResponse<String> response) {
        return GSON.fromJson(response.body(), Team.class);
    }

    /**
     * Returns the {@link Team} updated by {@link TeamRequestSender#retireUser(int, int)}.
     *
     * @param response the {@link HttpResponse} to parse
     * @return the updated {@link Team}
     */
    public static Team retireUser(final HttpResponse<String> response) {
        return GSON.fromJson(response.body(), Team.class);
    }

    /**
     * Returns the {@link Team} updated by {@link TeamRequestSender#unretireUser(int, int)}.
     *
     * @param response the {@link HttpResponse} to parse
     * @return the updated {@link Team}
     */
    public static Team unretireUser(final HttpResponse<String> response) {
        return GSON.fromJson(response.body(), Team.class);
    }
}
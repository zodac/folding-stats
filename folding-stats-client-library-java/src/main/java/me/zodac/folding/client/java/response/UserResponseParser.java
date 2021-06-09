package me.zodac.folding.client.java.response;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import me.zodac.folding.api.tc.User;

import java.lang.reflect.Type;
import java.net.http.HttpResponse;
import java.util.Collection;

/**
 * Utility class used to parse a {@link HttpResponse} returned from {@link me.zodac.folding.client.java.request.UserRequestSender}.
 */
public final class UserResponseParser {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private UserResponseParser() {

    }

    /**
     * Returns the {@link User}s retrieved by {@link me.zodac.folding.client.java.request.UserRequestSender#getAll()}.
     *
     * @param response the {@link HttpResponse} to parse
     * @return the retrieved {@link User}s
     */
    public static Collection<User> getAll(final HttpResponse<String> response) {
        final Type collectionType = new TypeToken<Collection<User>>() {
        }.getType();
        return GSON.fromJson(response.body(), collectionType);
    }

    /**
     * Returns the {@link User} retrieved by {@link me.zodac.folding.client.java.request.UserRequestSender#get(int)}.
     *
     * @param response the {@link HttpResponse} to parse
     * @return the retrieved {@link User}
     */
    public static User get(final HttpResponse<String> response) {
        return GSON.fromJson(response.body(), User.class);
    }

    /**
     * Returns the {@link User} created by {@link me.zodac.folding.client.java.request.UserRequestSender#create(me.zodac.folding.rest.api.tc.request.UserRequest)}.
     *
     * @param response the {@link HttpResponse} to parse
     * @return the created {@link User}
     */
    public static User create(final HttpResponse<String> response) {
        return GSON.fromJson(response.body(), User.class);
    }

    /**
     * Returns the {@link User} updated by {@link me.zodac.folding.client.java.request.UserRequestSender#update(int, me.zodac.folding.rest.api.tc.request.UserRequest)}.
     *
     * @param response the {@link HttpResponse} to parse
     * @return the updated {@link User}
     */
    public static User update(final HttpResponse<String> response) {
        return GSON.fromJson(response.body(), User.class);
    }
}
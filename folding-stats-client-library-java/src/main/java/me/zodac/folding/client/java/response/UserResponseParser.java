package me.zodac.folding.client.java.response;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import lombok.experimental.UtilityClass;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.client.java.request.UserRequestSender;

import java.lang.reflect.Type;
import java.net.http.HttpResponse;
import java.util.Collection;

/**
 * {@link UtilityClass} used to parse a {@link HttpResponse} returned from {@link UserRequestSender}.
 */
@UtilityClass
public final class UserResponseParser {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    /**
     * Returns the {@link User}s retrieved by {@link UserRequestSender#getAll()}.
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
     * Returns the {@link User} retrieved by {@link UserRequestSender#get(int)}.
     *
     * @param response the {@link HttpResponse} to parse
     * @return the retrieved {@link User}
     */
    public static User get(final HttpResponse<String> response) {
        return GSON.fromJson(response.body(), User.class);
    }

    /**
     * Returns the {@link User} created by {@link UserRequestSender#create(User)}.
     *
     * @param response the {@link HttpResponse} to parse
     * @return the created {@link User}
     */
    public static User create(final HttpResponse<String> response) {
        return GSON.fromJson(response.body(), User.class);
    }

    /**
     * Returns the {@link User} updated by {@link UserRequestSender#update(User)}.
     *
     * @param response the {@link HttpResponse} to parse
     * @return the updated {@link User}
     */
    public static User update(final HttpResponse<String> response) {
        return GSON.fromJson(response.body(), User.class);
    }
}
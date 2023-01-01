/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2023 zodac.me
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package me.zodac.folding.client.java.response;

import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.net.http.HttpResponse;
import java.util.Collection;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.rest.api.util.RestUtilConstants;

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
        final Type collectionType = TeamCollectionType.getInstance().getType();
        return RestUtilConstants.GSON.fromJson(response.body(), collectionType);
    }

    /**
     * Returns the {@link Team} retrieved by {@link me.zodac.folding.client.java.request.TeamRequestSender#get(int)} or
     * {@link me.zodac.folding.client.java.request.TeamRequestSender#get(String)}.
     *
     * @param response the {@link HttpResponse} to parse
     * @return the retrieved {@link Team}
     */
    public static Team get(final HttpResponse<String> response) {
        return RestUtilConstants.GSON.fromJson(response.body(), Team.class);
    }

    /**
     * Returns the {@link Team} created by
     * {@link me.zodac.folding.client.java.request.TeamRequestSender#create(me.zodac.folding.rest.api.tc.request.TeamRequest, String, String)}.
     *
     * @param response the {@link HttpResponse} to parse
     * @return the created {@link Team}
     */
    public static Team create(final HttpResponse<String> response) {
        return RestUtilConstants.GSON.fromJson(response.body(), Team.class);
    }

    /**
     * Returns the {@link Team} updated by
     * {@link me.zodac.folding.client.java.request.TeamRequestSender#update(int, me.zodac.folding.rest.api.tc.request.TeamRequest, String, String)}.
     *
     * @param response the {@link HttpResponse} to parse
     * @return the updated {@link Team}
     */
    public static Team update(final HttpResponse<String> response) {
        return RestUtilConstants.GSON.fromJson(response.body(), Team.class);
    }

    /**
     * Private class defining the {@link Collection} for {@link Team}s.
     */
    private static final class TeamCollectionType extends TypeToken<Collection<Team>> {

        private static final TeamCollectionType INSTANCE = new TeamCollectionType();

        /**
         * Retrieve a singleton instance of {@link TeamCollectionType}.
         *
         * @return {@link TeamCollectionType} instance.
         */
        static TeamCollectionType getInstance() {
            return INSTANCE;
        }
    }
}

/*
 * MIT License
 *
 * Copyright (c) 2021-2022 zodac.me
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.zodac.folding.client.java.response;

import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.net.http.HttpResponse;
import java.util.Collection;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.rest.util.RestUtilConstants;

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
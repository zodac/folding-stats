/*
 * MIT License
 *
 * Copyright (c) 2021 zodac.me
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

package me.zodac.folding.test.stub;

import com.google.gson.Gson;
import java.util.HashMap;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


/**
 * Stubbed endpoint for the Folding@Home user 'stats' API. Used to retrieve the total points count for a user/passkey across all teams for tests,
 * rather than going to the real API.
 *
 * <p>
 * We expose additional endpoints to allow the tests to set the points for a given user. The endpoint is {@link ApplicationScoped} so we can store
 * updates across multiple HTTP requests and tests.
 *
 * @see <a href="https://api2.foldingathome.org/#GET-/user/:name/stats">Real user stats API</a>
 */
@Path("/user/")
@ApplicationScoped
public class StubbedPointsEndpoint {

    private static final Gson GSON = new Gson();
    private static final long NO_POINTS = 0L;

    private final Map<String, Long> pointsByUserAndPasskey = new HashMap<>();

    /**
     * {@link GET} request that retrieves the points for a Folding@Home user.
     *
     * @param foldingUserName the Folding@Home user's username
     * @param passkey         the Folding@Home user's passkey
     * @return {@link Response.Status#OK} with the Folding@Home user's points
     */
    @GET
    @Path("/{foldingUserName}/stats")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserPoints(@PathParam("foldingUserName") final String foldingUserName, @QueryParam("passkey") final String passkey) {
        return Response
            .ok()
            .entity(GSON.toJson(createResponse(foldingUserName, passkey)))
            .build();
    }

    /**
     * {@link POST} request that sets the points for a Folding@Home user.
     *
     * <p>
     * Since in the test environment we do not want a user to actually have to complete units and earn points, we can manually set them here.
     *
     * @param foldingUserName the Folding@Home user's username
     * @param passkey         the Folding@Home user's passkey
     * @param points          the points to set
     * @return {@link Response.Status#OK}
     */
    @POST
    @Path("/{foldingUserName}/stats")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateUserPoints(@PathParam("foldingUserName") final String foldingUserName,
                                     @QueryParam("passkey") final String passkey,
                                     @QueryParam("points") final long points) {
        final String key = foldingUserName + passkey;

        if (points == NO_POINTS) {
            // Remove all points from the user
            pointsByUserAndPasskey.put(key, points);
        } else {
            pointsByUserAndPasskey.put(key, pointsByUserAndPasskey.getOrDefault(key, NO_POINTS) + points);
        }

        return Response
            .ok()
            .build();
    }

    /**
     * {@link DELETE} request that resets the points for all Folding@Home users.
     *
     * @return {@link Response.Status#OK}
     */
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteUserPoints() {
        pointsByUserAndPasskey.clear();
        return Response
            .ok()
            .build();
    }

    private PointsResponse createResponse(final String foldingUserName, final String passkey) {
        final String key = foldingUserName + passkey;

        if (pointsByUserAndPasskey.containsKey(key)) {
            return PointsResponse.create(pointsByUserAndPasskey.get(key));
        }

        return PointsResponse.empty();
    }

    @NoArgsConstructor
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Getter
    @Setter
    @EqualsAndHashCode
    @ToString(doNotUseGetters = true)
    private static class PointsResponse {

        private long earned;

        public static PointsResponse create(final long earned) {
            return new PointsResponse(earned);
        }

        public static PointsResponse empty() {
            return create(NO_POINTS);
        }
    }
}

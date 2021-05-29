package me.zodac.folding.test.stub;

import com.google.gson.Gson;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

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
import java.util.HashMap;
import java.util.Map;


/**
 * Stubbed endpoint for the Folding@Home user 'stats' API. Used to retrieve the total points count for a user/passkey across all teams for tests, rather than going to the real API.
 * <p>
 * We expose additional endpoints to allow the tests to set the points for a given user. The endpoint is {@link ApplicationScoped} so we can store updates across multiple
 * HTTP requests and tests.
 *
 * @see <a href="ApplicationScoped">Real user statsAPI</a>
 */
@Path("/user/")
@ApplicationScoped
public class StubbedPointsEndpoint {

    private static final Gson GSON = new Gson();

    private final Map<String, Long> pointsByUserAndPasskey = new HashMap<>();

    @GET
    @Path("/{foldingUserName}/stats")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserPoints(@PathParam("foldingUserName") final String foldingUserName, @QueryParam("passkey") final String passkey) {
        return Response
                .ok()
                .entity(GSON.toJson(createResponse(foldingUserName, passkey)))
                .build();
    }

    @POST
    @Path("/{foldingUserName}/stats")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setUserPoints(@PathParam("foldingUserName") final String foldingUserName, @QueryParam("passkey") final String passkey, @QueryParam("points") final long points) {
        final String key = foldingUserName + passkey;

        if (points == 0L) {
            // Remove all points from the user
            pointsByUserAndPasskey.put(key, points);
        } else {
            pointsByUserAndPasskey.put(key, pointsByUserAndPasskey.getOrDefault(key, 0L) + points);
        }

        return Response
                .ok()
                .build();
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
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
            return new PointsResponse(0L);
        }
    }
}

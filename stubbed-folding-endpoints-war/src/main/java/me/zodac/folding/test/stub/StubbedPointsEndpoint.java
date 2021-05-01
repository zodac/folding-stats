package me.zodac.folding.test.stub;

import com.google.gson.Gson;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.enterprise.context.RequestScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


@Path("/user/")
@RequestScoped
public class StubbedPointsEndpoint {

    private static final Gson GSON = new Gson();

    @GET
    @Path("/{foldingUserName}/stats")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserPoints(@PathParam("foldingUserName") final String foldingUserName, @QueryParam("team") final String team, @QueryParam("passkey") final String passkey) {
        return Response
                .ok()
                .entity(createResponse(foldingUserName, team, passkey))
                .build();
    }

    private Object createResponse(final String foldingUserName, final String team, final String passkey) {
        return GSON.toJson(PointsResponse.create(1L));
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
    }
}

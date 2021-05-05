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
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Stubbed endpoint for the Folding@Home 'bonus' API. Used to retrieve the WU count for a user/passkey for tests, rather than going to the real API.
 * <p>
 * We expose additional endpoints to allow the tests to set the WUs for a given user. The endpoint is {@link ApplicationScoped} so we can store updates across multiple
 * HTTP requests and tests.
 *
 * @see <a href="https://api2.foldingathome.org/#GET-/bonus">Real API</a>
 */
@Path("/bonus/")
@ApplicationScoped
public class StubbedUnitsEndpoint {

    private static final Gson GSON = new Gson();

    private final Map<String, Integer> unitsByUserAndPasskey = new HashMap<>();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserUnits(@QueryParam("user") final String foldingUserName, @QueryParam("passkey") final String passkey) {

        return Response
                .ok()
                .entity(GSON.toJson(createResponse(foldingUserName, passkey)))
                .build();
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response setUserUnits(@QueryParam("user") final String foldingUserName, @QueryParam("passkey") final String passkey, @QueryParam("units") final int units) {
        final String key = foldingUserName + passkey;
        unitsByUserAndPasskey.put(key, units);

        return Response
                .ok()
                .build();
    }

    private List<UnitsResponse> createResponse(final String foldingUserName, final String passkey) {
        final String key = foldingUserName + passkey;
        if (unitsByUserAndPasskey.containsKey(key)) {
            return List.of(UnitsResponse.create(unitsByUserAndPasskey.get(key)));
        }

        return Collections.emptyList();
    }

    @NoArgsConstructor
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Getter
    @Setter
    @EqualsAndHashCode
    @ToString(doNotUseGetters = true)
    private static class UnitsResponse {

        private int finished;

        public static UnitsResponse create(final int finished) {
            return new UnitsResponse(finished);
        }
    }
}
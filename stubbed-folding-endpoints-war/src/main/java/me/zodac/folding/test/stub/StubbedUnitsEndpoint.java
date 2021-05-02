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
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;


@Path("/bonus/")
@RequestScoped
public class StubbedUnitsEndpoint {

    private static final Gson GSON = new Gson();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserUnits(@QueryParam("user") final String foldingUserName, @QueryParam("passkey") final String passkey) {
        return Response
                .ok()
                .entity(createResponse(foldingUserName, passkey))
                .build();
    }

    private Object createResponse(final String foldingUserName, final String passkey) {
        switch (foldingUserName) {
            case "Dummy_User":
                return GSON.toJson(List.of(UnitsResponse.create(1)));
            default:
                return GSON.toJson(List.of(UnitsResponse.create(0)));
        }
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

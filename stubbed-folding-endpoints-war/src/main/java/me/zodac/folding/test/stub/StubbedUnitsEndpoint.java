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

package me.zodac.folding.test.stub;

import com.google.gson.Gson;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
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
 * Stubbed endpoint for the Folding@Home 'bonus' API. Used to retrieve the WU count for a user/passkey for tests, rather than going to the real API.
 *
 * <p>
 * We expose additional endpoints to allow the tests to set the WUs for a given user. The endpoint is {@link ApplicationScoped} so we can store
 * updates across multiple HTTP requests and tests.
 *
 * @see <a href="https://api2.foldingathome.org/#GET-/bonus">Real bonus API</a>
 */
@Path("/bonus/")
@ApplicationScoped
public class StubbedUnitsEndpoint {

    private static final Gson GSON = new Gson();
    private static final int NO_UNITS = 0;

    private final Map<String, Integer> unitsByUserAndPasskey = new HashMap<>();

    /**
     * {@link GET} request that retrieves the units for a Folding@Home user.
     *
     * @param foldingUserName the Folding@Home user's username
     * @param passkey         the Folding@Home user's passkey
     * @return {@link Response.Status#OK} with the Folding@Home user's units
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserUnits(@QueryParam("user") final String foldingUserName, @QueryParam("passkey") final String passkey) {
        return Response
            .ok()
            .entity(GSON.toJson(createResponse(foldingUserName, passkey)))
            .build();
    }

    /**
     * {@link POST} request that sets the units for a Folding@Home user.
     *
     * <p>
     * Since in the test environment we do not want a user to actually have to complete units, we can manually set them here.
     *
     * @param foldingUserName the Folding@Home user's username
     * @param passkey         the Folding@Home user's passkey
     * @param units           the units to set
     * @return {@link Response.Status#OK}
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateUserUnits(@QueryParam("user") final String foldingUserName,
                                    @QueryParam("passkey") final String passkey,
                                    @QueryParam("units") final int units) {
        final String key = foldingUserName + passkey;

        if (units == NO_UNITS) {
            // Remove all units from the user
            unitsByUserAndPasskey.put(key, units);
        } else {
            unitsByUserAndPasskey.put(key, unitsByUserAndPasskey.getOrDefault(key, NO_UNITS) + units);
        }

        return Response
            .ok()
            .build();
    }

    /**
     * {@link DELETE} request that resets the units for all Folding@Home users.
     *
     * @return {@link Response.Status#OK}
     */
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteUserUnits() {
        unitsByUserAndPasskey.clear();

        return Response
            .ok()
            .build();
    }

    private Collection<UnitsResponse> createResponse(final String foldingUserName, final String passkey) {
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

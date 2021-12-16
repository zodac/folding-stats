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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Stubbed endpoint for the Folding@Home 'bonus' API. Used to retrieve the WU count for a user/passkey for tests, rather than going to the real API.
 *
 * <p>
 * We expose additional endpoints to allow the tests to set the WUs for a given user.
 *
 * @see <a href="https://api2.foldingathome.org/#GET-/bonus">Real bonus API</a>
 */
@RestController
@RequestMapping("/bonus")
public class StubbedUnitsEndpoint {

    private static final int NO_UNITS = 0;

    private final Map<String, Integer> unitsByUserAndPasskey = new HashMap<>();

    /**
     * <b>GET</b> request that retrieves the units for a Folding@Home user.
     *
     * @param foldingUserName the Folding@Home user's username
     * @param passkey         the Folding@Home user's passkey
     * @return the Folding@Home user's {@link UnitsResponse}
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Collection<UnitsResponse> getUserUnits(@RequestParam("user") final String foldingUserName, @RequestParam("passkey") final String passkey) {
        return createResponse(foldingUserName, passkey);
    }

    /**
     * <b>POST</b> request that sets the units for a Folding@Home user.
     *
     * <p>
     * Since in the test environment we do not want a user to actually have to complete units, we can manually set them here.
     *
     * @param foldingUserName the Folding@Home user's username
     * @param passkey         the Folding@Home user's passkey
     * @param units           the units to set
     */
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public void updateUserUnits(@RequestParam("user") final String foldingUserName,
                                @RequestParam("passkey") final String passkey,
                                @RequestParam("units") final int units) {
        final String key = foldingUserName + passkey;

        if (units == NO_UNITS) {
            // Remove all units from the user
            unitsByUserAndPasskey.put(key, units);
        } else {
            unitsByUserAndPasskey.put(key, unitsByUserAndPasskey.getOrDefault(key, NO_UNITS) + units);
        }
    }

    /**
     * <b>DELETE</b>> request that resets the units for all Folding@Home users.
     */
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping
    public void deleteUserUnits() {
        unitsByUserAndPasskey.clear();
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
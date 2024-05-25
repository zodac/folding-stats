/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2024 zodac.me
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

package me.zodac.folding.rest.stub;

import io.swagger.v3.oas.annotations.Hidden;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Stubbed endpoint for the Folding@Home 'bonus' API. Used to retrieve the WU count for a user/passkey for tests, rather than going to the real API.
 *
 * <p>
 * We expose additional endpoints to allow the tests to set the WUs for a given user.
 *
 * @see <a href="https://api2.foldingathome.org/#GET-/bonus">Real bonus API</a>
 */
@Hidden
@ConditionalOnProperty(name = "stubbed.endpoints.enabled", havingValue = "true")
@RestController
@RequestMapping("/bonus")
public class StubbedUnitsEndpoint {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final int NO_UNITS = 0;

    private final Map<String, Integer> unitsByUserAndPasskey = new HashMap<>();

    /**
     * {@link GetMapping} request that retrieves the units for a Folding@Home user.
     *
     * <p>
     * Shadows the existing {@code /bonus?user={user}&passkey={passkey}} GET endpoint for retrieving user points.
     *
     * @param foldingUserName the Folding@Home user's username
     * @param passkey         the Folding@Home user's passkey
     * @return the Folding@Home user's {@link UnitsResponse}s
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Collection<UnitsResponse>> getUserUnits(@RequestParam("user") final String foldingUserName,
                                                                  @RequestParam("passkey") final String passkey) {
        LOGGER.debug("Getting units for '{}:{}'", foldingUserName, passkey);
        return ResponseEntity
            .ok()
            .body(createResponse(foldingUserName, passkey));
    }

    /**
     * {@link PostMapping} request that sets the units for a Folding@Home user.
     *
     * <p>
     * This does not shadow an existing endpoint. Since in the test environment we do not want a user to actually have to complete units, we can
     * manually set them here.
     *
     * <p>
     * If the input {@code units} is <b>0</b>, the user's units will be set to <b>0</b>, rather than having no change.
     *
     * @param foldingUserName the Folding@Home user's username
     * @param passkey         the Folding@Home user's passkey
     * @param units           the units to add for the user (can be positive or negative)
     * @return {@link HttpStatus#CREATED} {@link ResponseEntity}
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> updateUserUnits(@RequestParam("user") final String foldingUserName,
                                                @RequestParam("passkey") final String passkey,
                                                @RequestParam("units") final int units) {
        LOGGER.debug("Adding {} units for '{}:{}'", units, foldingUserName, passkey);
        final String key = foldingUserName + passkey;

        if (units == NO_UNITS) {
            // Remove all units from the user
            unitsByUserAndPasskey.put(key, NO_UNITS);
        } else {
            unitsByUserAndPasskey.put(key, unitsByUserAndPasskey.getOrDefault(key, NO_UNITS) + units);
        }

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .build();
    }

    /**
     * {@link DeleteMapping} request that resets the units for all Folding@Home users.
     *
     * <p>
     * This does not shadow an existing endpoint. Used for the test environment to clean up all user units.
     *
     * @return {@link HttpStatus#OK} {@link ResponseEntity}
     */
    @DeleteMapping
    public ResponseEntity<Void> deleteUserUnits() {
        LOGGER.debug("Deleting all units: {}", unitsByUserAndPasskey);
        unitsByUserAndPasskey.clear();
        return ResponseEntity
            .ok()
            .build();
    }

    private Collection<UnitsResponse> createResponse(final String foldingUserName, final String passkey) {
        final String key = foldingUserName + passkey;
        if (unitsByUserAndPasskey.containsKey(key)) {
            return List.of(UnitsResponse.create(unitsByUserAndPasskey.get(key)));
        }

        return List.of();
    }

    /**
     * POJO defining the response for the number of finished units.
     *
     * @param finished the number of finished units
     */
    public record UnitsResponse(int finished) {

        /**
         * Create a new {@link UnitsResponse}.
         *
         * @param finished the finished units
         * @return the {@link UnitsResponse}
         */
        public static UnitsResponse create(final int finished) {
            return new UnitsResponse(finished);
        }
    }
}

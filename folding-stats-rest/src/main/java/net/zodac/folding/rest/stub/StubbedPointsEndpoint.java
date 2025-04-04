/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2025 zodac.net
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

package net.zodac.folding.rest.stub;

import io.swagger.v3.oas.annotations.Hidden;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Stubbed endpoint for the Folding@Home user 'stats' API. Used to retrieve the total points count for a user/passkey across all teams for tests,
 * rather than going to the real API.
 *
 * <p>
 * We expose additional endpoints to allow the tests to set the points for a given user.
 *
 * @see <a href="https://api2.foldingathome.org/#GET-/user/:name/stats">Real user stats API</a>
 */
@Hidden
@ConditionalOnProperty(name = "stubbed.endpoints.enabled", havingValue = "true")
@RestController
@RequestMapping("/user")
public class StubbedPointsEndpoint {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final long NO_POINTS = 0L;

    private final Map<String, Long> pointsByUserAndPasskey = new HashMap<>();

    /**
     * {@link GetMapping} request that retrieves the points for a Folding@Home user.
     *
     * <p>
     * Shadows the existing {@code /user/{user}/stats?passkey={passkey}} GET endpoint for retrieving user points.
     *
     * @param foldingUserName the Folding@Home user's username
     * @param passkey         the Folding@Home user's passkey
     * @return the Folding@Home user's {@link PointsResponse}
     */
    @GetMapping(value = "/{foldingUserName}/stats", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PointsResponse> getUserPoints(@PathVariable("foldingUserName") final String foldingUserName,
                                                        @RequestParam("passkey") final String passkey) {
        LOGGER.debug("Getting points for '{}:{}'", foldingUserName, passkey);
        return ResponseEntity
            .ok()
            .body(createResponse(foldingUserName, passkey));
    }

    /**
     * {@link PostMapping} request that adds points for a Folding@Home user.
     *
     * <p>
     * This does not shadow an existing endpoint. Since in the test environment we do not want a user to actually have to complete units and
     * earn points, we can manually set them here.
     *
     * <p>
     * If the input {@code points} is <b>0</b>, the user's points will be set to <b>0</b>, rather than having no change.
     *
     * @param foldingUserName the Folding@Home user's username
     * @param passkey         the Folding@Home user's passkey
     * @param points          the points to add for the user (can be positive or negative)
     * @return {@link HttpStatus#CREATED} {@link ResponseEntity}
     */
    @PostMapping(value = "/{foldingUserName}/stats", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> updateUserPoints(@PathVariable("foldingUserName") final String foldingUserName,
                                                 @RequestParam("passkey") final String passkey,
                                                 @RequestParam("points") final long points) {
        LOGGER.debug("Adding {} points for '{}:{}'", points, foldingUserName, passkey);
        final String key = foldingUserName + passkey;

        if (points == NO_POINTS) {
            // Remove all points from the user
            pointsByUserAndPasskey.put(key, NO_POINTS);
        } else {
            pointsByUserAndPasskey.put(key, pointsByUserAndPasskey.getOrDefault(key, NO_POINTS) + points);
        }

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .build();
    }

    /**
     * {@link DeleteMapping} request that resets the points for all Folding@Home users.
     *
     * <p>
     * This does not shadow an existing endpoint. Used for the test environment to clean up all user points.
     *
     * @return {@link HttpStatus#OK} {@link ResponseEntity}
     */
    @DeleteMapping
    public ResponseEntity<Void> deleteUserPoints() {
        LOGGER.debug("Deleting all points: {}", pointsByUserAndPasskey);
        pointsByUserAndPasskey.clear();
        return ResponseEntity
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

    /**
     * POJO defining the response for the number of earned points.
     *
     * @param earned the number of earned points
     */
    public record PointsResponse(long earned) {

        /**
         * Create a new {@link PointsResponse}.
         *
         * @param earned the earned points
         * @return the {@link PointsResponse}
         */
        public static PointsResponse create(final long earned) {
            return new PointsResponse(earned);
        }

        /**
         * Create a new {@link PointsResponse} with no earned points.
         *
         * @return the {@link PointsResponse}
         */
        public static PointsResponse empty() {
            return create(0L);
        }
    }
}

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

import java.util.HashMap;
import java.util.Map;
import javax.websocket.server.PathParam;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
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
@RestController
@RequestMapping("/user")
public class StubbedPointsEndpoint {

    private static final long NO_POINTS = 0L;

    private final Map<String, Long> pointsByUserAndPasskey = new HashMap<>();

    /**
     * <b>GET</b> request that retrieves the points for a Folding@Home user.
     *
     * @param foldingUserName the Folding@Home user's username
     * @param passkey         the Folding@Home user's passkey
     * @return the Folding@Home user's {@link PointsResponse}
     */
    @GetMapping(value = "/{foldingUserName}/stats", produces = MediaType.APPLICATION_JSON_VALUE)
    public PointsResponse getUserPoints(@PathParam("foldingUserName") final String foldingUserName, @RequestParam("passkey") final String passkey) {
        return createResponse(foldingUserName, passkey);
    }

    /**
     * <b>POST</b>> request that sets the points for a Folding@Home user.
     *
     * <p>
     * Since in the test environment we do not want a user to actually have to complete units and earn points, we can manually set them here.
     *
     * @param foldingUserName the Folding@Home user's username
     * @param passkey         the Folding@Home user's passkey
     * @param points          the points to set
     */
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/{foldingUserName}/stats", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public void updateUserPoints(@PathParam("foldingUserName") final String foldingUserName,
                                 @RequestParam("passkey") final String passkey,
                                 @RequestParam("points") final long points) {
        final String key = foldingUserName + passkey;

        if (points == NO_POINTS) {
            // Remove all points from the user
            pointsByUserAndPasskey.put(key, points);
        } else {
            pointsByUserAndPasskey.put(key, pointsByUserAndPasskey.getOrDefault(key, NO_POINTS) + points);
        }
    }

    /**
     * <b>DELETE</b> request that resets the points for all Folding@Home users.
     */
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping
    public void deleteUserPoints() {
        pointsByUserAndPasskey.clear();
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

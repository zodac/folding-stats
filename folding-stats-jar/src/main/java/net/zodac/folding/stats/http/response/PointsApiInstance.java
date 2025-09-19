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

package net.zodac.folding.stats.http.response;

import java.util.Comparator;
import java.util.Objects;
import net.zodac.folding.stats.http.request.PointsUrlBuilder;

/**
 * Response returned from {@link PointsUrlBuilder} for a user/passkey.
 *
 * <p>
 * Expected response:
 * <pre>
 *     {
 *         "earned": 97802740,      [This is the total points earned by the user/passkey combo, for all teams, which is what we want]
 *         "contributed": 76694831, [This is the total points earned by the user/passkey combo, but only for the specified team (can be null)]
 *         "team_total": 5526874925,
 *         "team_name": "ExtremeHW",
 *         "team_url": "https://extremehw.net/",
 *         "team_rank": 219,
 *         "team_urllogo": "https://image.extremehw.net/images/2020/03/15/LOGO-EXTREME-ON-TRANSPARENT-BACKGROUNDbd5ff1f81fff3068.png",
 *         "url": "https://stats.foldingathome.org/donor/BWG"
 *      }
 * </pre>
 */
record PointsApiInstance(long earned) implements Comparable<PointsApiInstance> {

    @Override
    public int compareTo(final PointsApiInstance o) {
        return Comparator.comparingLong(PointsApiInstance::earned)
            .compare(o, this);
    }

    @Override
    public boolean equals(final Object obj) {
        return (obj instanceof PointsApiInstance(final long otherEarned)) && earned == otherEarned;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(earned);
    }
}

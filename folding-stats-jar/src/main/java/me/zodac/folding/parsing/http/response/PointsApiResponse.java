package me.zodac.folding.parsing.http.response;

import java.util.Comparator;
import java.util.Objects;

import static me.zodac.folding.api.utils.NumberUtils.formatWithCommas;

/**
 * Valid response:
 * <pre>
 *     {
 *         "earned": 97802740,      <-- This is the total points earned by the user+passkey combo, for all teams, which is what we want
 *         "contributed": 76694831, <-- This is the total points earned by the user+passkey combo, but only for the specified team (can be null)
 *         "team_total": 5526874925,
 *         "team_name": "ExtremeHW",
 *         "team_url": "https://extremehw.net/",
 *         "team_rank": 219,
 *         "team_urllogo": "https://image.extremehw.net/images/2020/03/15/LOGO-EXTREME-ON-TRANSPARENT-BACKGROUNDbd5ff1f81fff3068.png",
 *         "url": "https://stats.foldingathome.org/donor/BWG"
 *      }
 * </pre>
 */
class PointsApiResponse implements Comparable<PointsApiResponse> {

    private long earned;

    public PointsApiResponse() {

    }

    public long getEarned() {
        return earned;
    }

    public void setEarned(final long earned) {
        this.earned = earned;
    }

    @Override
    public int compareTo(final PointsApiResponse other) {
        return Comparator.comparingLong(PointsApiResponse::getEarned)
                .compare(other, this);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final PointsApiResponse that = (PointsApiResponse) o;
        return earned == that.earned;
    }

    @Override
    public int hashCode() {
        return Objects.hash(earned);
    }

    @Override
    public String toString() {
        return "PointsApiResponse::{" +
                "points: " + formatWithCommas(earned) +
                '}';
    }
}
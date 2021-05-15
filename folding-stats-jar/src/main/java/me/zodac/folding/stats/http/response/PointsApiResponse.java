package me.zodac.folding.stats.http.response;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Comparator;

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
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString(doNotUseGetters = true)
class PointsApiResponse implements Comparable<PointsApiResponse> {

    private long earned;

    @Override
    public int compareTo(final PointsApiResponse other) {
        return Comparator.comparingLong(PointsApiResponse::getEarned)
                .compare(other, this);
    }
}
package me.zodac.folding.stats.http.response;

import java.util.Comparator;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Response returned from {@link me.zodac.folding.stats.http.request.PointsUrlBuilder} for a user/passkey.
 *
 * <p>
 * Expected response:
 * <pre>
 *     {
 *         "earned": 97802740,      This is the total points earned by the user/passkey combo, for all teams, which is what we want
 *         "contributed": 76694831, This is the total points earned by the user/passkey combo, but only for the specified team (can be null)
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
class PointsApiInstance implements Comparable<PointsApiInstance> {

    private long earned;

    @Override
    public int compareTo(final PointsApiInstance other) {
        return Comparator.comparingLong(PointsApiInstance::getEarned)
            .compare(other, this);
    }
}
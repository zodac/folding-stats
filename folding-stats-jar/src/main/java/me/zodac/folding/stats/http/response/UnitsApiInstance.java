package me.zodac.folding.stats.http.response;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Comparator;

/**
 * Response returned from {@link me.zodac.folding.stats.http.request.UnitsUrlBuilder} for a user/passkey.
 * <p>
 * Expected response:
 * <pre>
 *     [
 *         {
 *             "finished":21260, <-- Value we are interested in
 *             "expired":60,
 *             "active":1
 *         },
 *         {
 *             "finished":512,
 *             "expired":4,
 *             "active":0
 *         }
 *     ]
 * </pre>
 */
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString(doNotUseGetters = true)
class UnitsApiInstance implements Comparable<UnitsApiInstance> {

    private int finished;
    private int expired;
    private int active;

    @Override
    public int compareTo(final UnitsApiInstance other) {
        return Comparator.comparingInt(UnitsApiInstance::getFinished)
                .thenComparingInt(UnitsApiInstance::getActive)
                .thenComparingInt(UnitsApiInstance::getExpired)
                .compare(other, this);
    }
}
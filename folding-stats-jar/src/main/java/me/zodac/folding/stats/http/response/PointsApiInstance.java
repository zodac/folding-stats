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

package me.zodac.folding.stats.http.response;

import com.google.gson.annotations.SerializedName;
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

    @SerializedName("earned")
    private long earned;

    @Override
    public int compareTo(final PointsApiInstance other) {
        return Comparator.comparingLong(PointsApiInstance::getEarned)
            .compare(other, this);
    }
}
package me.zodac.folding.parsing.http.response;

import java.util.Objects;

import static me.zodac.folding.api.utils.NumberUtils.formatWithCommas;

/**
 * Valid response:
 * <pre>
 *     {
 *         "earned": 97802740,
 *         "contributed": 76694831, <-- Value we are interested in
 *         "team_total": 5526874925,
 *         "team_name": "ExtremeHW",
 *         "team_url": "https://extremehw.net/",
 *         "team_rank": 219,
 *         "team_urllogo": "https://image.extremehw.net/images/2020/03/15/LOGO-EXTREME-ON-TRANSPARENT-BACKGROUNDbd5ff1f81fff3068.png",
 *         "url": "https://stats.foldingathome.org/donor/BWG"
 *      }
 * </pre>
 */
class PointsApiResponse {

    private long contributed;

    public PointsApiResponse() {

    }

    public long getContributed() {
        return contributed;
    }

    public void setContributed(final long contributed) {
        this.contributed = contributed;
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
        return contributed == that.contributed;
    }

    @Override
    public int hashCode() {
        return Objects.hash(contributed);
    }

    @Override
    public String toString() {
        return "PointsApiResponse::{" +
                "points: " + formatWithCommas(contributed) +
                '}';
    }
}
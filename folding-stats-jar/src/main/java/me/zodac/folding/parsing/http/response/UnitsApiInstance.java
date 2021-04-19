package me.zodac.folding.parsing.http.response;

import java.util.Objects;

import static me.zodac.folding.api.utils.NumberUtils.formatWithCommas;

/**
 * Valid response:
 * <pre>
 *     [
 *         {
 *             "finished":21260, <-- Value we are interested in
 *             "expired":60,
 *             "active":1
 *         }
 *     ]
 * </pre>
 */
class UnitsApiInstance {

    private int finished;
    private int expired;
    private int active;

    public UnitsApiInstance() {

    }

    public int getFinished() {
        return finished;
    }

    public void setFinished(final int finished) {
        this.finished = finished;
    }

    public int getExpired() {
        return expired;
    }

    public void setExpired(final int expired) {
        this.expired = expired;
    }

    public int getActive() {
        return active;
    }

    public void setActive(final int active) {
        this.active = active;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final UnitsApiInstance that = (UnitsApiInstance) o;
        return finished == that.finished && expired == that.expired && active == that.active;
    }

    @Override
    public int hashCode() {
        return Objects.hash(finished, expired, active);
    }

    @Override
    public String toString() {
        return "UnitsApiInstance::{" +
                "finishedUnits: " + formatWithCommas(finished) +
                ", expired: " + formatWithCommas(expired) +
                ", active: " + formatWithCommas(active) +
                '}';
    }
}
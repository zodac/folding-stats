package me.zodac.folding.rest.response;

import me.zodac.folding.api.Identifiable;

import java.util.List;
import java.util.Objects;

public class BulkCreateResponse {

    private List<Identifiable> successful;
    private List<Identifiable> unsuccessful;

    public BulkCreateResponse() {

    }

    private BulkCreateResponse(final List<Identifiable> successful, final List<Identifiable> unsuccessful) {
        this.successful = successful;
        this.unsuccessful = unsuccessful;
    }

    public static BulkCreateResponse create(final List<Identifiable> successful, final List<Identifiable> unsuccessful) {
        return new BulkCreateResponse(successful, unsuccessful);
    }

    public List<Identifiable> getSuccessful() {
        return successful;
    }

    public void setSuccessful(final List<Identifiable> successful) {
        this.successful = successful;
    }

    public List<Identifiable> getUnsuccessful() {
        return unsuccessful;
    }

    public void setUnsuccessful(final List<Identifiable> unsuccessful) {
        this.unsuccessful = unsuccessful;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final BulkCreateResponse that = (BulkCreateResponse) o;
        return Objects.equals(successful, that.successful) && Objects.equals(unsuccessful, that.unsuccessful);
    }

    @Override
    public int hashCode() {
        return Objects.hash(successful, unsuccessful);
    }

    @Override
    public String toString() {
        return "BulkCreateResponse::{" +
                "successful: " + successful +
                ", unsuccessful: " + unsuccessful +
                '}';
    }
}
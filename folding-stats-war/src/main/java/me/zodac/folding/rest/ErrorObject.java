package me.zodac.folding.rest;

import java.util.Objects;

/**
 * Simple POJO used for REST responses where an error message is required.
 * <p>
 * When using {@link com.google.gson.Gson}, the response will be in the form:
 * <pre>
 *     {
 *         "error": "My error message here"
 *     }
 * </pre>
 */
public class ErrorObject {

    private String error;

    public ErrorObject() {

    }

    public ErrorObject(final String error) {
        this.error = error;
    }

    public String getError() {
        return error;
    }

    public void setError(final String error) {
        this.error = error;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ErrorObject that = (ErrorObject) o;
        return Objects.equals(error, that.error);
    }

    @Override
    public int hashCode() {
        return Objects.hash(error);
    }


    @Override
    public String toString() {
        return "ErrorObject::{" +
                "error: '" + error + "'" +
                '}';
    }
}

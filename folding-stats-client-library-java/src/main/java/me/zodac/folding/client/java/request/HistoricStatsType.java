/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2024 zodac.me
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

package me.zodac.folding.client.java.request;

/**
 * Defines the type of historic stats we want to retrieve.
 */
public enum HistoricStatsType {

    /**
     * Historic stats for a {@link me.zodac.folding.api.tc.Team}.
     */
    TEAM("teams"),

    /**
     * Historic stats for a {@link me.zodac.folding.api.tc.User}.
     */
    USER("users");

    private final String endpointUrl;

    /**
     * Constructs a {@link HistoricStatsType}.
     *
     * @param endpointUrl the value of the {@link HistoricStatsType}
     */
    HistoricStatsType(final String endpointUrl) {
        this.endpointUrl = endpointUrl;
    }

    /**
     * The value of the endpoint URL.
     *
     * @return the endpoint URL
     */
    public String endpointUrl() {
        return endpointUrl;
    }
}

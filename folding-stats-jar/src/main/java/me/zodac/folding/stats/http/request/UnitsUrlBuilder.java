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

package me.zodac.folding.stats.http.request;

import me.zodac.folding.api.util.EnvironmentVariableUtils;
import me.zodac.folding.api.util.StringUtils;

/**
 * Utility class used to build the URL to request Folding Work Units for a user from the Stanford Folding@Home API.
 */
public class UnitsUrlBuilder {

    private static final String STATS_URL_ROOT = EnvironmentVariableUtils.getOrDefault("STATS_URL_ROOT", "https://api2.foldingathome.org");
    private static final String UNITS_URL_ROOT = STATS_URL_ROOT + "/bonus";

    private String user = "";
    private String passkey = "";

    /**
     * Update the {@link UnitsUrlBuilder} with the 'user' for the URL.
     *
     * @param user the user for the request
     * @return the updated {@link UnitsUrlBuilder}
     */
    public UnitsUrlBuilder forUser(final String user) {
        this.user = user;
        return this;
    }

    /**
     * Update the {@link UnitsUrlBuilder} with the 'passkey' for the URL.
     *
     * @param passkey the passkey for the request
     * @return the updated {@link UnitsUrlBuilder}
     */
    public UnitsUrlBuilder withPasskey(final String passkey) {
        this.passkey = passkey;
        return this;
    }

    /**
     * Build the Work Units REST request URL for a user/passkey.
     *
     * @return the URL to request units
     */
    public StatsRequestUrl build() {
        if (StringUtils.isBlank(user)) {
            throw new IllegalArgumentException("'user' cannot be null or empty");
        }

        final StringBuilder statsUrl = new StringBuilder(UNITS_URL_ROOT).append("?user=").append(user);

        if (StringUtils.isNotBlank(passkey)) {
            statsUrl.append("&passkey=").append(passkey);
        }

        return StatsRequestUrl.create(statsUrl);
    }
}


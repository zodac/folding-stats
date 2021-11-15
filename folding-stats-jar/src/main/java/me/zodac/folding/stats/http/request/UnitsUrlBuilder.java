/*
 * MIT License
 *
 * Copyright (c) 2021 zodac.me
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

package me.zodac.folding.stats.http.request;

import me.zodac.folding.api.util.EnvironmentVariableUtils;
import me.zodac.folding.api.util.StringUtils;

/**
 * Utility class used to build the URL to request Folding Work Units for a user from the Stanford Folding@Home API.
 */
public class UnitsUrlBuilder {

    private static final String STATS_URL_ROOT = EnvironmentVariableUtils.getOrDefault("STATS_URL_ROOT", "https://api2.foldingathome.org");
    private static final String UNITS_URL_ROOT = STATS_URL_ROOT + "/bonus";

    private String user;
    private String passkey;

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


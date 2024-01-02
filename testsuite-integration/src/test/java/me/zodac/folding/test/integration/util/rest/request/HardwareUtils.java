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

package me.zodac.folding.test.integration.util.rest.request;

import static me.zodac.folding.test.integration.util.DummyAuthenticationData.ADMIN_USER;
import static me.zodac.folding.test.integration.util.rest.response.HttpResponseHeaderUtils.getTotalCount;

import java.net.HttpURLConnection;
import java.net.http.HttpResponse;
import java.util.Collection;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.client.java.request.HardwareRequestSender;
import me.zodac.folding.client.java.response.HardwareResponseParser;
import me.zodac.folding.rest.api.exception.FoldingRestException;
import me.zodac.folding.rest.api.tc.request.HardwareRequest;
import me.zodac.folding.test.integration.util.TestConstants;

/**
 * Utility class for {@link Hardware}-based tests.
 */
public final class HardwareUtils {

    public static final HardwareRequestSender HARDWARE_REQUEST_SENDER = HardwareRequestSender.createWithUrl(TestConstants.FOLDING_URL);

    private HardwareUtils() {

    }

    /**
     * Creates the given {@link HardwareRequest}.
     *
     * @param hardware the {@link HardwareRequest} to create
     * @return the created {@link Hardware}
     * @throws FoldingRestException thrown if an error occurs creating the {@link Hardware}
     */
    public static Hardware create(final HardwareRequest hardware) throws FoldingRestException {
        final HttpResponse<String> response = HARDWARE_REQUEST_SENDER.create(hardware, ADMIN_USER.userName(), ADMIN_USER.password());
        if (response.statusCode() == HttpURLConnection.HTTP_CREATED) {
            return HardwareResponseParser.create(response);
        }

        throw new FoldingRestException(String.format("Invalid response (%s) when creating hardware: %s", response.statusCode(), response.body()));
    }

    /**
     * Retrieves all {@link Hardware}s.
     *
     * @return the {@link Hardware}s
     * @throws FoldingRestException thrown if an error occurs retrieving the {@link Hardware}s
     */
    public static Collection<Hardware> getAll() throws FoldingRestException {
        final HttpResponse<String> response = HARDWARE_REQUEST_SENDER.getAll();
        if (response.statusCode() == HttpURLConnection.HTTP_OK) {
            return HardwareResponseParser.getAll(response);
        }

        throw new FoldingRestException(
            String.format("Invalid response (%s) when getting all hardware with: %s", response.statusCode(), response.body()));
    }

    /**
     * Retrieves the number of {@link Hardware}s.
     *
     * @return the number of {@link Hardware}s
     * @throws FoldingRestException thrown if an error occurs retrieving the {@link Hardware} count
     */
    public static int getNumberOfHardware() throws FoldingRestException {
        final HttpResponse<String> response = HARDWARE_REQUEST_SENDER.getAll();
        return getTotalCount(response);
    }
}

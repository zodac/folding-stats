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

package me.zodac.folding.integration.util.rest.request;

import static me.zodac.folding.integration.util.rest.response.HttpResponseHeaderUtils.getTotalCount;

import java.net.HttpURLConnection;
import java.net.http.HttpResponse;
import java.util.Collection;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.client.java.request.HardwareRequestSender;
import me.zodac.folding.client.java.response.HardwareResponseParser;
import me.zodac.folding.integration.util.TestAuthenticationData;
import me.zodac.folding.integration.util.TestConstants;
import me.zodac.folding.rest.api.exception.FoldingRestException;
import me.zodac.folding.rest.api.tc.request.HardwareRequest;

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
        final HttpResponse<String> response = HARDWARE_REQUEST_SENDER.create(hardware, TestAuthenticationData.ADMIN_USER.userName(), TestAuthenticationData.ADMIN_USER.password());
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

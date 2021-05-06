package me.zodac.folding.test.utils;

import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.client.java.request.HardwareRequestSender;
import me.zodac.folding.client.java.response.HardwareResponseParser;
import me.zodac.folding.rest.api.exception.FoldingRestException;

import java.net.HttpURLConnection;
import java.net.http.HttpResponse;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Utility class for {@link Hardware}-based tests.
 */
public class HardwareUtils {

    public static final HardwareRequestSender HARDWARE_REQUEST_SENDER = HardwareRequestSender.create("http://192.168.99.100:8081/folding");

    private HardwareUtils() {

    }

    /**
     * Creates the given {@link Hardware}, or if it already exists, returns the existing one.
     *
     * @param hardware the {@link Hardware} to create/retrieve
     * @return the created {@link Hardware} or existing {@link Hardware}
     * @throws FoldingRestException thrown if an error occurs creating/retrieving the {@link Hardware}
     */
    public static Hardware createOrConflict(final Hardware hardware) throws FoldingRestException {
        final HttpResponse<String> response = HARDWARE_REQUEST_SENDER.create(hardware);
        if (response.statusCode() == HttpURLConnection.HTTP_CREATED) {
            return HardwareResponseParser.create(response);
        }

        if (response.statusCode() == HttpURLConnection.HTTP_CONFLICT) {
            return get(hardware.getId());
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

        throw new FoldingRestException(String.format("Invalid response (%s) when getting all hardware with: %s", response.statusCode(), response.body()));
    }

    /**
     * Retrieves the number of {@link Hardware}s.
     *
     * @return the number of {@link Hardware}s
     * @throws FoldingRestException thrown if an error occurs retrieving the {@link Hardware} count
     */
    public static int getNumberOfHardware() throws FoldingRestException {
        final HttpResponse<String> response = HARDWARE_REQUEST_SENDER.getAll();
        final Map<String, List<String>> headers = response.headers().map();
        if (headers.containsKey("X-Total-Count")) {
            final String firstHeaderValue = headers.get("X-Total-Count").get(0);

            try {
                return Integer.parseInt(firstHeaderValue);
            } catch (final NumberFormatException e) {
                throw new FoldingRestException(String.format("Error parsing 'X-Total-Count' header %s", firstHeaderValue), e);
            }
        }
        throw new FoldingRestException(String.format("Unable to find 'X-Total-Count' header: %s", headers));
    }

    /**
     * Retrieves a {@link Hardware} with the given ID.
     *
     * @param hardwareId the ID of the {@link Hardware} to retrieve
     * @return the {@link Hardware}
     * @throws FoldingRestException thrown if an error occurs retrieving the {@link Hardware}
     */
    public static Hardware get(final int hardwareId) throws FoldingRestException {
        final HttpResponse<String> response = HARDWARE_REQUEST_SENDER.get(hardwareId);
        if (response.statusCode() == HttpURLConnection.HTTP_OK) {
            return HardwareResponseParser.get(response);
        }

        throw new FoldingRestException(String.format("Invalid response (%s) when getting hardware with ID %s: %s", response.statusCode(), hardwareId, response.body()));
    }
}

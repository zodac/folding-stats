package me.zodac.folding.test.util.rest.request;

import static me.zodac.folding.test.util.TestAuthenticationData.ADMIN_USER;
import static me.zodac.folding.test.util.TestConstants.FOLDING_URL;
import static me.zodac.folding.test.util.rest.response.HttpResponseHeaderUtils.getTotalCount;

import java.net.HttpURLConnection;
import java.net.http.HttpResponse;
import java.util.Collection;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.client.java.request.HardwareRequestSender;
import me.zodac.folding.client.java.response.HardwareResponseParser;
import me.zodac.folding.rest.api.exception.FoldingRestException;
import me.zodac.folding.rest.api.tc.request.HardwareRequest;

/**
 * Utility class for {@link Hardware}-based tests.
 */
public final class HardwareUtils {

    public static final HardwareRequestSender HARDWARE_REQUEST_SENDER = HardwareRequestSender.createWithUrl(FOLDING_URL);

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

        throw new FoldingRestException(
            String.format("Invalid response (%s) when getting hardware with ID %s: %s", response.statusCode(), hardwareId, response.body()));
    }
}

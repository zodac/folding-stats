package me.zodac.folding.test;

import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.OperatingSystem;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.client.java.response.HardwareResponseParser;
import me.zodac.folding.rest.api.exception.FoldingRestException;
import me.zodac.folding.test.utils.TestGenerator;
import me.zodac.folding.test.utils.rest.request.HardwareUtils;
import me.zodac.folding.test.utils.rest.request.UserUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static me.zodac.folding.api.utils.EncodingUtils.encodeBasicAuthentication;
import static me.zodac.folding.test.utils.SystemCleaner.cleanSystemForSimpleTests;
import static me.zodac.folding.test.utils.TestAuthenticationData.ADMIN_USER;
import static me.zodac.folding.test.utils.TestAuthenticationData.INVALID_PASSWORD;
import static me.zodac.folding.test.utils.TestAuthenticationData.INVALID_USERNAME;
import static me.zodac.folding.test.utils.TestAuthenticationData.READ_ONLY_USER;
import static me.zodac.folding.test.utils.TestConstants.FOLDING_URL;
import static me.zodac.folding.test.utils.TestConstants.HTTP_CLIENT;
import static me.zodac.folding.test.utils.TestGenerator.generateHardware;
import static me.zodac.folding.test.utils.rest.request.HardwareUtils.HARDWARE_REQUEST_SENDER;
import static me.zodac.folding.test.utils.rest.response.HttpResponseHeaderUtils.getETag;
import static me.zodac.folding.test.utils.rest.response.HttpResponseHeaderUtils.getXTotalCount;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the {@link Hardware} REST endpoint at <code>/folding/hardware</code>.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class HardwareTest {

    @BeforeAll
    public static void setUp() throws FoldingRestException {
        cleanSystemForSimpleTests();
    }

    @Test
    @Order(1)
    public void whenGettingAllHardware_givenNoHardwareHasBeenCreated_thenAnEmptyJsonResponseIsReturned_andHasA200Status() throws FoldingRestException {
        final HttpResponse<String> response = HARDWARE_REQUEST_SENDER.getAll();
        assertThat(response.statusCode())
                .as("Did not receive a 200_OK HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_OK);

        final Collection<Hardware> allHardware = HardwareResponseParser.getAll(response);
        final int xTotalCount = getXTotalCount(response);

        assertThat(xTotalCount)
                .isEqualTo(allHardware.size());

        assertThat(allHardware)
                .isEmpty();
    }

    @Test
    public void whenCreatingHardware_givenPayloadIsValid_thenTheCreatedHardwareIsReturnedInResponse_andHasId_andResponseHasA201StatusCode() throws FoldingRestException {
        final Hardware hardwareToCreate = generateHardware();
        final HttpResponse<String> response = HARDWARE_REQUEST_SENDER.create(hardwareToCreate, ADMIN_USER.userName(), ADMIN_USER.password());
        assertThat(response.statusCode())
                .as("Did not receive a 201_CREATED HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_CREATED);

        final Hardware actual = HardwareResponseParser.create(response);
        final Hardware expected = Hardware.updateWithId(actual.getId(), hardwareToCreate);
        assertThat(actual)
                .as("Did not receive created object as JSON response: " + response.body())
                .isEqualTo(expected);
    }

    @Test
    public void whenCreatingBatchOfHardware_givenPayloadIsValid_thenTheHardwareIsCreated_andResponseHasA200Status() throws FoldingRestException {
        final int initialSize = HardwareUtils.getNumberOfHardware();

        final List<Hardware> batchOfHardware = List.of(
                generateHardware(),
                generateHardware(),
                generateHardware()
        );

        final HttpResponse<String> response = HARDWARE_REQUEST_SENDER.createBatchOf(batchOfHardware, ADMIN_USER.userName(), ADMIN_USER.password());
        assertThat(response.statusCode())
                .as("Did not receive a 200_OK HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_OK);

        final int newSize = HardwareUtils.getNumberOfHardware();
        assertThat(newSize)
                .as("Get all response did not return the initial hardware + new hardware: " + response.body())
                .isEqualTo(initialSize + batchOfHardware.size());
    }

    @Test
    public void whenGettingHardware_givenAValidHardwareId_thenHardwareIsReturned_andHasA200Status() throws FoldingRestException {
        final int hardwareId = HardwareUtils.createOrConflict(generateHardware()).getId();

        final HttpResponse<String> response = HARDWARE_REQUEST_SENDER.get(hardwareId);
        assertThat(response.statusCode())
                .as("Did not receive a 200_OK HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_OK);

        final Hardware hardware = HardwareResponseParser.get(response);
        assertThat(hardware.getId())
                .as("Did not receive the expected hardware: " + response.body())
                .isEqualTo(hardwareId);
    }

    @Test
    public void whenUpdatingHardware_givenAValidHardwareId_andAValidPayload_thenUpdatedHardwareIsReturned_andNoNewHardwareIsCreated_andHasA200Status() throws FoldingRestException {
        final int hardwareId = HardwareUtils.createOrConflict(generateHardware()).getId();
        final int initialSize = HardwareUtils.getNumberOfHardware();

        final Hardware updatedHardware = Hardware.updateWithId(hardwareId, HardwareUtils.get(hardwareId));
        updatedHardware.setOperatingSystem(OperatingSystem.LINUX.displayName());

        final HttpResponse<String> response = HARDWARE_REQUEST_SENDER.update(updatedHardware, ADMIN_USER.userName(), ADMIN_USER.password());
        assertThat(response.statusCode())
                .as("Did not receive a 200_OK HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_OK);

        final Hardware actual = HardwareResponseParser.update(response);
        assertThat(actual)
                .as("Did not receive created object as JSON response: " + response.body())
                .isEqualTo(updatedHardware);


        final int allHardwareAfterUpdate = HardwareUtils.getNumberOfHardware();
        assertThat(allHardwareAfterUpdate)
                .as("Expected no new hardware instances to be created")
                .isEqualTo(initialSize);
    }

    @Test
    public void whenDeletingHardware_givenAValidHardwareId_thenHardwareIsDeleted_andHasA200Status_andHardwareCountIsReduced_andHardwareCannotBeRetrievedAgain() throws FoldingRestException {
        final int hardwareId = HardwareUtils.createOrConflict(generateHardware()).getId();
        final int initialSize = HardwareUtils.getNumberOfHardware();

        final HttpResponse<Void> response = HARDWARE_REQUEST_SENDER.delete(hardwareId, ADMIN_USER.userName(), ADMIN_USER.password());
        assertThat(response.statusCode())
                .as("Did not receive a 200_OK HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_OK);


        final HttpResponse<String> getResponse = HARDWARE_REQUEST_SENDER.get(hardwareId);
        assertThat(getResponse.statusCode())
                .as("Was able to retrieve the hardware instance, despite deleting it")
                .isEqualTo(HttpURLConnection.HTTP_NOT_FOUND);

        final int newSize = HardwareUtils.getNumberOfHardware();
        assertThat(newSize)
                .as("Get all response did not return the initial hardware - deleted hardware")
                .isEqualTo(initialSize - 1);
    }

    // Negative/alternative test cases

    @Test
    public void whenCreatingHardware_givenAHardwareWithInvalidOperatingSystem_thenJsonResponseWithErrorIsReturned_andHasA400Status() throws FoldingRestException {
        final Hardware hardware = TestGenerator.generateHardwareWithOperatingSystem(OperatingSystem.INVALID);
        final HttpResponse<String> response = HARDWARE_REQUEST_SENDER.create(hardware, ADMIN_USER.userName(), ADMIN_USER.password());

        assertThat(response.statusCode())
                .as("Did not receive a 400_BAD_REQUEST HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);
    }

    @Test
    public void whenCreatingHardware_givenHardwareWithTheSameNameAndOperatingSystemAlreadyExists_thenA409ResponseIsReturned() throws FoldingRestException {
        final Hardware hardwareToCreate = generateHardware();
        HARDWARE_REQUEST_SENDER.create(hardwareToCreate, ADMIN_USER.userName(), ADMIN_USER.password()); // Send one request and ignore it (even if the user already exists, we can verify the conflict with the next one)
        final HttpResponse<String> response = HARDWARE_REQUEST_SENDER.create(hardwareToCreate, ADMIN_USER.userName(), ADMIN_USER.password());

        assertThat(response.statusCode())
                .as("Did not receive a 409_CONFLICT HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_CONFLICT);
    }

    @Test
    public void whenGettingHardware_givenANonExistingHardwareId_thenNoJsonResponseIsReturned_andHasA404Status() throws FoldingRestException {
        final int invalidId = 99;
        final HttpResponse<String> response = HARDWARE_REQUEST_SENDER.get(invalidId);

        assertThat(response.statusCode())
                .as("Did not receive a 404_NOT_FOUND HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_NOT_FOUND);

        assertThat(response.body())
                .as("Did not receive an empty JSON response: " + response.body())
                .isEmpty();
    }

    @Test
    public void whenUpdatingHardware_givenANonExistingHardwareId_thenNoJsonResponseIsReturned_andHasA404Status() throws FoldingRestException {
        final int invalidId = 99;
        final Hardware updatedHardware = TestGenerator.generateHardwareWithId(invalidId);

        final HttpResponse<String> response = HARDWARE_REQUEST_SENDER.update(updatedHardware, ADMIN_USER.userName(), ADMIN_USER.password());
        assertThat(response.statusCode())
                .as("Did not receive a 404_NOT_FOUND HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_NOT_FOUND);

        assertThat(response.body())
                .as("Did not receive an empty JSON response: " + response.body())
                .isEmpty();
    }

    @Test
    public void whenDeletingHardware_givenANonExistingHardwareId_thenResponseHasA404Status() throws FoldingRestException {
        final int invalidId = 99;
        final HttpResponse<Void> response = HARDWARE_REQUEST_SENDER.delete(invalidId, ADMIN_USER.userName(), ADMIN_USER.password());

        assertThat(response.statusCode())
                .as("Did not receive a 404_NOT_FOUND HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_NOT_FOUND);
    }

    @Test
    public void whenUpdatingHardware_givenAValidHardwareId_andPayloadHasNoChanges_thenOriginalHardwareIsReturned_andHasA200Status() throws FoldingRestException {
        final Hardware hardware = generateHardware();
        final int createdHardwareId = HardwareUtils.createOrConflict(hardware).getId();

        final Hardware hardwareWithId = Hardware.updateWithId(createdHardwareId, hardware);
        final HttpResponse<String> updateResponse = HARDWARE_REQUEST_SENDER.update(hardwareWithId, ADMIN_USER.userName(), ADMIN_USER.password());

        assertThat(updateResponse.statusCode())
                .as("Did not receive a 200_OK HTTP response: " + updateResponse.body())
                .isEqualTo(HttpURLConnection.HTTP_OK);

        final Hardware actual = HardwareResponseParser.update(updateResponse);
        assertThat(actual)
                .as("Did not receive the original hardware in response")
                .isEqualTo(hardwareWithId);
    }

    @Test
    public void whenCreatingBatchOfHardware_givenPayloadIsPartiallyValid_thenOnlyValidHardwareIsCreated_andResponseHasA200Status() throws FoldingRestException {
        final int initialHardwareSize = HardwareUtils.getNumberOfHardware();

        final List<Hardware> batchOfValidHardware = List.of(
                generateHardware(),
                generateHardware()
        );
        final List<Hardware> batchOfInvalidHardware = List.of(
                TestGenerator.generateHardwareWithOperatingSystem(OperatingSystem.INVALID),
                TestGenerator.generateHardwareWithOperatingSystem(OperatingSystem.INVALID)
        );
        final List<Hardware> batchOfHardware = new ArrayList<>(batchOfValidHardware.size() + batchOfInvalidHardware.size());
        batchOfHardware.addAll(batchOfValidHardware);
        batchOfHardware.addAll(batchOfInvalidHardware);

        final HttpResponse<String> response = HARDWARE_REQUEST_SENDER.createBatchOf(batchOfHardware, ADMIN_USER.userName(), ADMIN_USER.password());
        assertThat(response.statusCode())
                .as("Did not receive a 200_OK HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_OK);

        final int newHardwareSize = HardwareUtils.getNumberOfHardware();
        assertThat(newHardwareSize)
                .as("Get all response did not return the initial hardware + new valid hardware")
                .isEqualTo(initialHardwareSize + batchOfValidHardware.size());
    }

    @Test
    public void whenCreatingBatchOfHardware_givenPayloadIsInvalid_thenResponseHasA400Status() throws FoldingRestException {
        final int initialHardwareSize = HardwareUtils.getNumberOfHardware();

        final List<Hardware> batchOfInvalidHardware = List.of(
                TestGenerator.generateHardwareWithOperatingSystem(OperatingSystem.INVALID),
                TestGenerator.generateHardwareWithOperatingSystem(OperatingSystem.INVALID)
        );

        final HttpResponse<String> response = HARDWARE_REQUEST_SENDER.createBatchOf(batchOfInvalidHardware, ADMIN_USER.userName(), ADMIN_USER.password());
        assertThat(response.statusCode())
                .as("Did not receive a 400_BAD_REQUEST HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);

        final int newHardwareSize = HardwareUtils.getNumberOfHardware();
        assertThat(newHardwareSize)
                .as("Get all response did not return only the initial hardware")
                .isEqualTo(initialHardwareSize);
    }

    @Test
    public void whenDeletingHardware_givenTheHardwareIsLinkedToAUser_thenResponseHasA409Status() throws FoldingRestException {
        final int hardwareId = HardwareUtils.createOrConflict(generateHardware()).getId();
        final User user = TestGenerator.generateUserWithHardwareId(hardwareId);
        UserUtils.createOrConflict(user);

        final HttpResponse<Void> deleteHardwareResponse = HARDWARE_REQUEST_SENDER.delete(hardwareId, ADMIN_USER.userName(), ADMIN_USER.password());
        assertThat(deleteHardwareResponse.statusCode())
                .as("Expected to fail due to a 409_CONFLICT: " + deleteHardwareResponse.body())
                .isEqualTo(HttpURLConnection.HTTP_CONFLICT);
    }

    @Test
    public void whenGettingHardwareById_givenRequestUsesPreviousETag_andHardwareHasNotChanged_thenResponseHasA304Status_andNoBody() throws FoldingRestException {
        final int hardwareId = HardwareUtils.createOrConflict(generateHardware()).getId();

        final HttpResponse<String> response = HARDWARE_REQUEST_SENDER.get(hardwareId);
        assertThat(response.statusCode())
                .as("Expected first request to have a 200_OK HTTP response")
                .isEqualTo(HttpURLConnection.HTTP_OK);

        final String eTag = getETag(response);

        final HttpResponse<String> cachedResponse = HARDWARE_REQUEST_SENDER.get(hardwareId, eTag);
        assertThat(cachedResponse.statusCode())
                .as("Expected second request to have a 304_NOT_MODIFIED HTTP response")
                .isEqualTo(HttpURLConnection.HTTP_NOT_MODIFIED);

        assertThat(HardwareResponseParser.get(cachedResponse))
                .as("Expected cached response to have the same content as the non-cached response")
                .isNull();
    }

    @Test
    public void whenGettingAllHardware_givenRequestUsesPreviousETag_andHardwareHasNotChanged_thenResponseHasA304Status_andNoBody() throws FoldingRestException {
        HardwareUtils.createOrConflict(generateHardware());

        final HttpResponse<String> response = HARDWARE_REQUEST_SENDER.getAll();
        assertThat(response.statusCode())
                .as("Expected first GET request to have a 200_OK HTTP response")
                .isEqualTo(HttpURLConnection.HTTP_OK);

        final String eTag = getETag(response);

        final HttpResponse<String> cachedResponse = HARDWARE_REQUEST_SENDER.getAll(eTag);
        assertThat(cachedResponse.statusCode())
                .as("Expected second request to have a 304_NOT_MODIFIED HTTP response")
                .isEqualTo(HttpURLConnection.HTTP_NOT_MODIFIED);

        assertThat(HardwareResponseParser.getAll(cachedResponse))
                .as("Expected cached response to have the same content as the non-cached response")
                .isNull();
    }

    @Test
    public void whenCreatingHardware_givenNoAuthentication_thenRequestFails_andResponseHasA401StatusCode() throws FoldingRestException {
        final Hardware hardwareToCreate = generateHardware();
        final HttpResponse<String> response = HARDWARE_REQUEST_SENDER.create(hardwareToCreate);
        assertThat(response.statusCode())
                .as("Did not receive a 401_UNAUTHORIZED HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_UNAUTHORIZED);
    }

    @Test
    public void whenCreatingBatchOfHardware_givenNoAuthentication_thenRequestFails_andResponseHasA401StatusCode() throws FoldingRestException {
        final List<Hardware> batchOfHardware = List.of(
                generateHardware(),
                generateHardware(),
                generateHardware()
        );

        final HttpResponse<String> response = HARDWARE_REQUEST_SENDER.createBatchOf(batchOfHardware);
        assertThat(response.statusCode())
                .as("Did not receive a 401_UNAUTHORIZED HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_UNAUTHORIZED);
    }

    @Test
    public void whenUpdatingHardware_givenNoAuthentication_thenRequestFails_andResponseHasA401StatusCode() throws FoldingRestException {
        final int hardwareId = HardwareUtils.createOrConflict(generateHardware()).getId();

        final Hardware updatedHardware = Hardware.updateWithId(hardwareId, HardwareUtils.get(hardwareId));
        updatedHardware.setOperatingSystem(OperatingSystem.LINUX.displayName());

        final HttpResponse<String> response = HARDWARE_REQUEST_SENDER.update(updatedHardware);
        assertThat(response.statusCode())
                .as("Did not receive a 401_UNAUTHORIZED HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_UNAUTHORIZED);
    }

    @Test
    public void whenDeletingHardware_givenNoAuthentication_thenRequestFails_andResponseHasA401StatusCode() throws FoldingRestException {
        final int hardwareId = HardwareUtils.createOrConflict(generateHardware()).getId();

        final HttpResponse<Void> response = HARDWARE_REQUEST_SENDER.delete(hardwareId);
        assertThat(response.statusCode())
                .as("Did not receive a 401_UNAUTHORIZED HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_UNAUTHORIZED);
    }

    @Test
    public void whenCreatingHardware_givenAuthentication_andAuthenticationHasInvalidUser_thenRequestFails_andResponseHasA401StatusCode() throws FoldingRestException {
        final Hardware hardwareToCreate = generateHardware();
        final HttpResponse<String> response = HARDWARE_REQUEST_SENDER.create(hardwareToCreate, INVALID_USERNAME.userName(), INVALID_USERNAME.password());
        assertThat(response.statusCode())
                .as("Did not receive a 401_UNAUTHORIZED HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_UNAUTHORIZED);
    }

    @Test
    public void whenCreatingHardware_givenAuthentication_andAuthenticationHasInvalidPassword_thenRequestFails_andResponseHasA401StatusCode() throws FoldingRestException {
        final Hardware hardwareToCreate = generateHardware();
        final HttpResponse<String> response = HARDWARE_REQUEST_SENDER.create(hardwareToCreate, INVALID_PASSWORD.userName(), INVALID_PASSWORD.password());
        assertThat(response.statusCode())
                .as("Did not receive a 401_UNAUTHORIZED HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_UNAUTHORIZED);
    }

    @Test
    public void whenCreatingHardware_givenAuthentication_andUserDoesNotHaveAdminRole_thenRequestFails_andResponseHasA403StatusCode() throws FoldingRestException {
        final Hardware hardwareToCreate = generateHardware();
        final HttpResponse<String> response = HARDWARE_REQUEST_SENDER.create(hardwareToCreate, READ_ONLY_USER.userName(), READ_ONLY_USER.password());
        assertThat(response.statusCode())
                .as("Did not receive a 403_FORBIDDEN HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_FORBIDDEN);
    }

    @Test
    public void whenCreatingHardware_givenEmptyPayload_thenRequestFails_andResponseHasA400StatusCode() throws IOException, InterruptedException {
        final HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.noBody())
                .uri(URI.create(FOLDING_URL + "/hardware"))
                .header("Content-Type", "application/json")
                .header("Authorization", encodeBasicAuthentication(ADMIN_USER.userName(), ADMIN_USER.password()))
                .build();

        final HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode())
                .as("Did not receive a 400_BAD_REQUEST HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);
    }

    @Test
    public void whenUpdatingHardware_givenEmptyPayload_thenRequestFails_andResponseHasA400StatusCode() throws FoldingRestException, IOException, InterruptedException {
        final int hardwareId = HardwareUtils.createOrConflict(generateHardware()).getId();
        final Hardware updatedHardware = Hardware.updateWithId(hardwareId, HardwareUtils.get(hardwareId));
        updatedHardware.setOperatingSystem(OperatingSystem.LINUX.displayName());

        final HttpRequest request = HttpRequest.newBuilder()
                .PUT(HttpRequest.BodyPublishers.noBody())
                .uri(URI.create(FOLDING_URL + "/hardware/" + updatedHardware.getId()))
                .header("Content-Type", "application/json")
                .header("Authorization", encodeBasicAuthentication(ADMIN_USER.userName(), ADMIN_USER.password()))
                .build();

        final HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode())
                .as("Did not receive a 400_BAD_REQUEST HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);
    }

    @AfterAll
    public static void tearDown() throws FoldingRestException {
        cleanSystemForSimpleTests();
    }
}

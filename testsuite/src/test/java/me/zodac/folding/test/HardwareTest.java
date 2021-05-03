package me.zodac.folding.test;

import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.OperatingSystem;
import me.zodac.folding.test.utils.HardwareUtils;
import me.zodac.folding.test.utils.StubbedFoldingEndpointUtils;
import me.zodac.folding.test.utils.UserUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.HttpURLConnection;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static me.zodac.folding.test.utils.SystemCleaner.cleanSystemForTests;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the {@link Hardware} REST endpoint at <code>/folding/hardware</code>.
 */
public class HardwareTest {

    public static final Hardware DUMMY_HARDWARE = Hardware.createWithoutId("Dummy_Hardware", "Dummy Hardware", OperatingSystem.WINDOWS, 1.0D);

    @BeforeClass
    public static void setUp() {
        cleanSystemForTests();
    }

    @Test
    public void whenGettingAllHardware_givenNoHardwareHasBeenCreated_thenAnEmptyJsonResponseIsReturned_andHasA200Status() {
        cleanSystemForTests();

        final HttpResponse<String> response = HardwareUtils.RequestSender.getAll();
        assertThat(response.statusCode())
                .as("Did not receive a 200_OK HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_OK);

        final Collection<Hardware> allHardware = HardwareUtils.ResponseParser.getAll(response);
        final Map<String, List<String>> headers = response.headers().map();
        assertThat(headers)
                .containsKey("X-Total-Count");

        assertThat(headers.get("X-Total-Count").get(0))
                .isEqualTo(String.valueOf(allHardware.size()));

        assertThat(allHardware)
                .isEmpty();
    }

    @Test
    public void whenCreatingHardware_givenPayloadIsValid_thenTheCreatedHardwareIsReturnedInResponse_andHasId_andResponseHasA201StatusCode() {
        final Hardware hardwareToCreate = Hardware.createWithoutId("Dummy_Hardware1", "Dummy Hardware1", OperatingSystem.WINDOWS, 1.0D);
        final HttpResponse<String> response = HardwareUtils.RequestSender.create(hardwareToCreate);
        assertThat(response.statusCode())
                .as("Did not receive a 201_CREATED HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_CREATED);

        final Hardware actual = HardwareUtils.ResponseParser.create(response);
        final Hardware expected = Hardware.updateWithId(actual.getId(), hardwareToCreate);
        assertThat(actual)
                .as("Did not receive created object as JSON response: " + response.body())
                .isEqualTo(expected);
    }

    @Test
    public void whenCreatingBatchOfHardware_givenPayloadIsValid_thenTheHardwareIsCreated_andResponseHasA200Status() {
        final int initialSize = HardwareUtils.ResponseParser.getAll(HardwareUtils.RequestSender.getAll()).size();

        final List<Hardware> batchOfHardware = List.of(
                Hardware.createWithoutId("Dummy_Hardware2", "Dummy Hardware2", OperatingSystem.WINDOWS, 1.0D),
                Hardware.createWithoutId("Dummy_Hardware3", "Dummy Hardware3", OperatingSystem.WINDOWS, 1.0D),
                Hardware.createWithoutId("Dummy_Hardware4", "Dummy Hardware4", OperatingSystem.WINDOWS, 1.0D)
        );

        final HttpResponse<String> response = HardwareUtils.RequestSender.createBatchOf(batchOfHardware);
        assertThat(response.statusCode())
                .as("Did not receive a 200_OK HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_OK);

        final int newSize = HardwareUtils.ResponseParser.getAll(HardwareUtils.RequestSender.getAll()).size();
        assertThat(newSize)
                .as("Get all response did not return the initial hardware + new hardware: " + response.body())
                .isEqualTo(initialSize + batchOfHardware.size());
    }

    @Test
    public void whenGettingHardware_givenAValidHardwareId_thenHardwareIsReturned_andHasA200Status() {
        final Collection<Hardware> allHardware = HardwareUtils.ResponseParser.getAll(HardwareUtils.RequestSender.getAll());
        int hardwareId = allHardware.size();

        if (allHardware.isEmpty()) {
            hardwareId = HardwareUtils.ResponseParser.create(HardwareUtils.RequestSender.create(DUMMY_HARDWARE)).getId();
        }

        final HttpResponse<String> response = HardwareUtils.RequestSender.get(hardwareId);
        assertThat(response.statusCode())
                .as("Did not receive a 200_OK HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_OK);

        final Hardware hardware = HardwareUtils.ResponseParser.get(response);
        assertThat(hardware)
                .as("Did not receive the expected hardware: " + response.body())
                .extracting("id")
                .isEqualTo(hardwareId);
    }

    @Test
    public void whenUpdatingHardware_givenAValidHardwareId_andAValidPayload_thenUpdatedHardwareIsReturned_andNoNewHardwareIsCreated_andHasA200Status() {
        final Collection<Hardware> allHardware = HardwareUtils.ResponseParser.getAll(HardwareUtils.RequestSender.getAll());
        int hardwareId = allHardware.size();

        if (allHardware.isEmpty()) {
            hardwareId = HardwareUtils.ResponseParser.create(HardwareUtils.RequestSender.create(DUMMY_HARDWARE)).getId();
        }

        final Hardware updatedHardware = Hardware.updateWithId(hardwareId, DUMMY_HARDWARE);
        updatedHardware.setOperatingSystem(OperatingSystem.LINUX.displayName());

        final HttpResponse<String> response = HardwareUtils.RequestSender.update(updatedHardware);
        assertThat(response.statusCode())
                .as("Did not receive a 200_OK HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_OK);

        final Hardware actual = HardwareUtils.ResponseParser.update(response);
        assertThat(actual)
                .as("Did not receive created object as JSON response: " + response.body())
                .isEqualTo(updatedHardware);


        final Collection<Hardware> allHardwareAfterUpdate = HardwareUtils.ResponseParser.getAll(HardwareUtils.RequestSender.getAll());
        assertThat(allHardwareAfterUpdate)
                .as("Expected no new hardware instances to be created")
                .hasSize(hardwareId);
    }

    @Test
    public void whenDeletingHardware_givenAValidHardwareId_thenHardwareIsDeleted_andHasA200Status_andHardwareCountIsReduced_andHardwareCannotBeRetrievedAgain() {
        final Collection<Hardware> allHardware = HardwareUtils.ResponseParser.getAll(HardwareUtils.RequestSender.getAll());
        int hardwareId = allHardware.size();

        if (allHardware.isEmpty()) {
            hardwareId = HardwareUtils.ResponseParser.create(HardwareUtils.RequestSender.create(DUMMY_HARDWARE)).getId();
        }

        final HttpResponse<Void> response = HardwareUtils.RequestSender.delete(hardwareId);
        assertThat(response.statusCode())
                .as("Did not receive a 200_OK HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_OK);


        final HttpResponse<String> getResponse = HardwareUtils.RequestSender.get(hardwareId);
        assertThat(getResponse.statusCode())
                .as("Was able to retrieve the hardware instance, despite deleting it")
                .isEqualTo(HttpURLConnection.HTTP_NOT_FOUND);

        final int newSize = HardwareUtils.ResponseParser.getAll(HardwareUtils.RequestSender.getAll()).size();
        assertThat(newSize)
                .as("Get all response did not return the initial hardware - deleted hardware")
                .isEqualTo(hardwareId - 1);
    }

    // Negative/alternative test cases

    @Test
    public void whenCreatingHardware_givenAHardwareWithInvalidOperatingSystem_thenJsonResponseWithErrorIsReturned_andHasA400Status() {
        final Hardware hardware = Hardware.createWithoutId("Test GPU", "Base GPU", OperatingSystem.INVALID, 1.0D);

        final HttpResponse<String> response = HardwareUtils.RequestSender.create(hardware);

        assertThat(response.statusCode())
                .as("Did not receive a 400_BAD_REQUEST HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);
    }

    @Test
    public void whenCreatingHardware_givenHardwareWithTheSameNameAndOperatingSystemAlreadyExists_thenA409ResponseIsReturned() {
        HardwareUtils.RequestSender.create(DUMMY_HARDWARE);
        final HttpResponse<String> response = HardwareUtils.RequestSender.create(DUMMY_HARDWARE);

        assertThat(response.statusCode())
                .as("Did not receive a 409_CONFLICT HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_CONFLICT);
    }

    @Test
    public void whenGettingHardware_givenANonExistingHardwareId_thenNoJsonResponseIsReturned_andHasA404Status() {
        final int invalidId = 99;
        final HttpResponse<String> response = HardwareUtils.RequestSender.get(invalidId);

        assertThat(response.statusCode())
                .as("Did not receive a 404_NOT_FOUND HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_NOT_FOUND);

        assertThat(response.body())
                .as("Did not receive an empty JSON response: " + response.body())
                .isEmpty();
    }

    @Test
    public void whenUpdatingHardware_givenANonExistingHardwareId_thenNoJsonResponseIsReturned_andHasA404Status() {
        final int invalidId = 99;
        final Hardware updatedHardware = Hardware.create(invalidId, "Test GPU", "Base GPU", OperatingSystem.WINDOWS, 1.0D);

        final HttpResponse<String> response = HardwareUtils.RequestSender.update(updatedHardware);
        assertThat(response.statusCode())
                .as("Did not receive a 404_NOT_FOUND HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_NOT_FOUND);

        assertThat(response.body())
                .as("Did not receive an empty JSON response: " + response.body())
                .isEmpty();
    }

    @Test
    public void whenDeletingHardware_givenANonExistingHardwareId_thenResponseHasA404Status() {
        final int invalidId = 99;
        final HttpResponse<Void> response = HardwareUtils.RequestSender.delete(invalidId);

        assertThat(response.statusCode())
                .as("Did not receive a 404_NOT_FOUND HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_NOT_FOUND);
    }

    @Test
    public void whenUpdatingHardware_givenAValidHardwareId_andPayloadHasNoChanges_thenOriginalHardwareIsReturned_andHasA200Status() {
        final Hardware hardware = Hardware.createWithoutId("Test GPU", "Base GPU", OperatingSystem.WINDOWS, 1.0D);

        final HttpResponse<String> createResponse = HardwareUtils.RequestSender.create(hardware);
        assertThat(createResponse.statusCode())
                .as("Did not receive a 201_CREATED HTTP response: " + createResponse.body())
                .isEqualTo(HttpURLConnection.HTTP_CREATED);

        final int createdHardwareId = HardwareUtils.ResponseParser.create(createResponse).getId();
        final Hardware hardwareWithId = Hardware.updateWithId(createdHardwareId, hardware);

        final HttpResponse<String> updateResponse = HardwareUtils.RequestSender.update(hardwareWithId);

        assertThat(updateResponse.statusCode())
                .as("Did not receive a 200_OK HTTP response: " + updateResponse.body())
                .isEqualTo(HttpURLConnection.HTTP_OK);

        final Hardware actual = HardwareUtils.ResponseParser.update(updateResponse);

        assertThat(actual)
                .as("Did not receive the original hardware in response")
                .isEqualTo(hardwareWithId);
    }

    @Test
    public void whenCreatingBatchOfHardware_givenPayloadIsPartiallyValid_thenOnlyValidHardwareIsCreated_andResponseHasA200Status() {
        final int initialHardwareSize = HardwareUtils.ResponseParser.getAll(HardwareUtils.RequestSender.getAll()).size();

        final List<Hardware> batchOfValidHardware = List.of(
                Hardware.createWithoutId("Hardware1", "Hardware1", OperatingSystem.WINDOWS, 1.0D),
                Hardware.createWithoutId("Hardware2", "Hardware2", OperatingSystem.WINDOWS, 1.0D)
        );
        final List<Hardware> batchOfInvalidHardware = List.of(
                Hardware.createWithoutId("Hardware1", "Hardware1", OperatingSystem.INVALID, 1.0D),
                Hardware.createWithoutId("Hardware2", "Hardware2", OperatingSystem.INVALID, 1.0D)
        );
        final List<Hardware> batchOfHardware = new ArrayList<>(batchOfValidHardware.size() + batchOfInvalidHardware.size());
        batchOfHardware.addAll(batchOfValidHardware);
        batchOfHardware.addAll(batchOfInvalidHardware);

        final HttpResponse<String> response = HardwareUtils.RequestSender.createBatchOf(batchOfHardware);
        assertThat(response.statusCode())
                .as("Did not receive a 200_OK HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_OK);

        final int newHardwareSize = HardwareUtils.ResponseParser.getAll(HardwareUtils.RequestSender.getAll()).size();
        assertThat(newHardwareSize)
                .as("Get all response did not return the initial hardware + new valid hardware")
                .isEqualTo(initialHardwareSize + batchOfValidHardware.size());
    }

    @Test
    public void whenCreatingBatchOfHardware_givenPayloadIsInvalid_thenResponseHasA400Status() {
        final int initialHardwareSize = HardwareUtils.ResponseParser.getAll(HardwareUtils.RequestSender.getAll()).size();

        final List<Hardware> batchOfInvalidHardware = List.of(
                Hardware.createWithoutId("Hardware1", "Hardware1", OperatingSystem.INVALID, 1.0D),
                Hardware.createWithoutId("Hardware2", "Hardware2", OperatingSystem.INVALID, 1.0D)
        );

        final HttpResponse<String> response = HardwareUtils.RequestSender.createBatchOf(batchOfInvalidHardware);
        assertThat(response.statusCode())
                .as("Did not receive a 400_BAD_REQUEST HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);

        final int newHardwareSize = HardwareUtils.ResponseParser.getAll(HardwareUtils.RequestSender.getAll()).size();
        assertThat(newHardwareSize)
                .as("Get all response did not return only the initial hardware")
                .isEqualTo(initialHardwareSize);
    }

    @Test
    public void whenDeletingHardware_givenTheHardwareIsLinkedToAUser_thenResponseHasA409Status() {
        final HttpResponse<String> createHardwareResponse = HardwareUtils.RequestSender.create(DUMMY_HARDWARE);
        assertThat(createHardwareResponse.statusCode())
                .as("Was not able to create hardware: " + createHardwareResponse.body())
                .isEqualTo(HttpURLConnection.HTTP_CREATED);

        final int hardwareId = HardwareUtils.ResponseParser.create(createHardwareResponse).getId();

        StubbedFoldingEndpointUtils.enableUser(UserTest.DUMMY_USER);
        final HttpResponse<String> createUserResponse = UserUtils.RequestSender.create(UserTest.DUMMY_USER);
        assertThat(createUserResponse.statusCode())
                .as("Was not able to create user: " + createUserResponse.body())
                .isEqualTo(HttpURLConnection.HTTP_CREATED);


        final HttpResponse<Void> deleteHardwareResponse = HardwareUtils.RequestSender.delete(hardwareId);
        assertThat(deleteHardwareResponse.statusCode())
                .as("Expected to fail due to a 409_CONFLICT: " + deleteHardwareResponse.body())
                .isEqualTo(HttpURLConnection.HTTP_CONFLICT);
    }

    @AfterClass
    public static void tearDown() {
        cleanSystemForTests();
    }
}

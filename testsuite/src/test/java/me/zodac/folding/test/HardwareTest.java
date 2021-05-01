package me.zodac.folding.test;

import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.OperatingSystem;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.test.utils.DatabaseCleaner;
import me.zodac.folding.test.utils.HardwareUtils;
import me.zodac.folding.test.utils.UserUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.http.HttpResponse;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class HardwareTest {

    public static final Hardware DUMMY_HARDWARE = Hardware.createWithoutId("Dummy_Hardware", "Dummy Hardware", OperatingSystem.WINDOWS, 1.0D);

    @BeforeClass
    public static void setUp() throws SQLException, IOException, InterruptedException {
        cleanSystemForHardwareTests();
    }

    @Test
    public void whenGettingAllHardware_givenNoHardwareHasBeenCreated_thenAnEmptyJsonResponseIsReturned_andHasA200Status() throws IOException, InterruptedException, SQLException {
        cleanSystemForHardwareTests(); // No guarantee that this test runs first, so we need to clean the system again
        final HttpResponse<String> response = HardwareUtils.RequestSender.getAll();
        assertThat(response.statusCode())
                .as("Did not receive a 200_OK HTTP response")
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
    public void whenCreatingHardware_givenPayloadIsValid_thenTheCreatedHardwareIsReturnedInResponse_andHasId_andResponseHasA201StatusCode() throws IOException, InterruptedException {
        final HttpResponse<String> response = HardwareUtils.RequestSender.create(DUMMY_HARDWARE);
        assertThat(response.statusCode())
                .as("Did not receive a 201_CREATED HTTP response")
                .isEqualTo(HttpURLConnection.HTTP_CREATED);

        final Hardware actual = HardwareUtils.ResponseParser.create(response);
        final Hardware expected = Hardware.updateWithId(actual.getId(), DUMMY_HARDWARE);
        assertThat(actual)
                .as("Did not receive created object as JSON response")
                .isEqualTo(expected);
    }

    @Test
    public void whenCreatingBatchOfHardware_givenPayloadIsValid_thenTheHardwareIsCreated_andResponseHasA200Status() throws IOException, InterruptedException {
        final int initialSize = HardwareUtils.ResponseParser.getAll(HardwareUtils.RequestSender.getAll()).size();

        final List<Hardware> batchOfHardware = List.of(
                Hardware.createWithoutId("Dummy_Hardware2", "Dummy Hardware2", OperatingSystem.WINDOWS, 1.0D),
                Hardware.createWithoutId("Dummy_Hardware3", "Dummy Hardware3", OperatingSystem.WINDOWS, 1.0D),
                Hardware.createWithoutId("Dummy_Hardware4", "Dummy Hardware4", OperatingSystem.WINDOWS, 1.0D)
        );

        final HttpResponse<String> response = HardwareUtils.RequestSender.createBatchOf(batchOfHardware);
        assertThat(response.statusCode())
                .as("Did not receive a 200_OK HTTP response")
                .isEqualTo(HttpURLConnection.HTTP_OK);

        final int newSize = HardwareUtils.ResponseParser.getAll(HardwareUtils.RequestSender.getAll()).size();
        assertThat(newSize)
                .as("Get all response did not return the initial hardware + new hardware")
                .isEqualTo(initialSize + batchOfHardware.size());
    }

    @Test
    public void whenGettingHardware_givenAValidHardwareId_thenHardwareIsReturned_andHasA200Status() throws IOException, InterruptedException {
        final Collection<Hardware> allHardware = HardwareUtils.ResponseParser.getAll(HardwareUtils.RequestSender.getAll());
        int hardwareId = allHardware.size();

        if (allHardware.isEmpty()) {
            hardwareId = HardwareUtils.ResponseParser.create(HardwareUtils.RequestSender.create(DUMMY_HARDWARE)).getId();
        }

        final HttpResponse<String> response = HardwareUtils.RequestSender.get(hardwareId);
        assertThat(response.statusCode())
                .as("Did not receive a 200_OK HTTP response")
                .isEqualTo(HttpURLConnection.HTTP_OK);

        final Hardware hardware = HardwareUtils.ResponseParser.get(response);
        assertThat(hardware)
                .as("Did not receive a valid hardware")
                .extracting("id")
                .isEqualTo(hardwareId);
    }

    @Test
    public void whenUpdatingHardware_givenAValidHardwareId_andAValidPayload_thenUpdatedHardwareIsReturned_andNoNewHardwareIsCreated_andHasA200Status() throws IOException, InterruptedException {
        final Collection<Hardware> allHardware = HardwareUtils.ResponseParser.getAll(HardwareUtils.RequestSender.getAll());
        int hardwareId = allHardware.size();

        if (allHardware.isEmpty()) {
            hardwareId = HardwareUtils.ResponseParser.create(HardwareUtils.RequestSender.create(DUMMY_HARDWARE)).getId();
        }

        final Hardware updatedHardware = Hardware.create(hardwareId, "Dummy_Hardware5", "Dummy Hardware5", OperatingSystem.LINUX, 1.0D);
        final HttpResponse<String> response = HardwareUtils.RequestSender.update(updatedHardware);
        assertThat(response.statusCode())
                .as("Did not receive a 200_OK HTTP response")
                .isEqualTo(HttpURLConnection.HTTP_OK);

        final Hardware actual = HardwareUtils.ResponseParser.update(response);
        assertThat(actual)
                .as("Did not receive created object as JSON response")
                .isEqualTo(updatedHardware);


        final Collection<Hardware> allHardwareAfterUpdate = HardwareUtils.ResponseParser.getAll(HardwareUtils.RequestSender.getAll());
        assertThat(allHardwareAfterUpdate)
                .as("Expected no new hardware instances to be created")
                .hasSize(allHardware.size());
    }

    @Test
    public void whenDeletingHardware_givenAValidHardwareId_thenHardwareIsDeleted_andHasA200Status_andHardwareCountIsReduced_andHardwareCannotBeRetrievedAgain() throws IOException, InterruptedException {
        final Collection<Hardware> allHardware = HardwareUtils.ResponseParser.getAll(HardwareUtils.RequestSender.getAll());
        int hardwareId = allHardware.size();

        if (allHardware.isEmpty()) {
            hardwareId = HardwareUtils.ResponseParser.create(HardwareUtils.RequestSender.create(DUMMY_HARDWARE)).getId();
        }

        final HttpResponse<String> response = HardwareUtils.RequestSender.delete(hardwareId);
        assertThat(response.statusCode())
                .as("Did not receive a 200_OK HTTP response")
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
    public void whenCreatingHardware_givenAnInvalidHardware_thenJsonResponseWithErrorsIsReturned_andHasA400Status() throws IOException, InterruptedException {
        final Hardware hardware = Hardware.createWithoutId("Test GPU", "Base GPU", OperatingSystem.INVALID, 1.0D);

        final HttpResponse<String> response = HardwareUtils.RequestSender.create(hardware);

        assertThat(response.statusCode())
                .as("Did not receive a 400_BAD_REQUEST HTTP response")
                .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);
    }


    @Test
    public void whenGettingHardware_givenANonExistingHardwareId_thenNoJsonResponseIsReturned_andHasA404Status() throws IOException, InterruptedException {
        final int invalidId = 99;
        final HttpResponse<String> response = HardwareUtils.RequestSender.get(invalidId);

        assertThat(response.statusCode())
                .as("Did not receive a 404_NOT_FOUND HTTP response")
                .isEqualTo(HttpURLConnection.HTTP_NOT_FOUND);

        assertThat(response.body())
                .as("Did not receive an empty JSON response")
                .isEmpty();
    }

    @Test
    public void whenUpdatingHardware_givenANonExistingHardwareId_thenNoJsonResponseIsReturned_andHasA404Status() throws IOException, InterruptedException {
        final int invalidId = 99;
        final Hardware updatedHardware = Hardware.create(invalidId, "Test GPU", "Base GPU", OperatingSystem.WINDOWS, 1.0D);

        final HttpResponse<String> response = HardwareUtils.RequestSender.update(updatedHardware);
        assertThat(response.statusCode())
                .as("Did not receive a 404_NOT_FOUND HTTP response")
                .isEqualTo(HttpURLConnection.HTTP_NOT_FOUND);

        assertThat(response.body())
                .as("Did not receive an empty JSON response")
                .isEmpty();
    }

    @Test
    public void whenDeletingHardware_givenANonExistingHardwareId_thenNoJsonResponseIsReturned_andHasA404Status() throws IOException, InterruptedException {
        final int invalidId = 99;
        final HttpResponse<String> response = HardwareUtils.RequestSender.delete(invalidId);

        assertThat(response.statusCode())
                .as("Did not receive a 404_NOT_FOUND HTTP response")
                .isEqualTo(HttpURLConnection.HTTP_NOT_FOUND);

        assertThat(response.body())
                .as("Did not receive an empty JSON response")
                .isEmpty();
    }

    @Test
    public void whenUpdatingHardware_givenAValidHardwareId_andPayloadHasNoChanges_thenOriginalHardwareIsReturned_andHasA200Status() throws IOException, InterruptedException {
        final Hardware hardware = Hardware.createWithoutId("Test GPU", "Base GPU", OperatingSystem.WINDOWS, 1.0D);

        final HttpResponse<String> createResponse = HardwareUtils.RequestSender.create(hardware);
        assertThat(createResponse.statusCode())
                .as("Did not receive a 201_CREATED HTTP response")
                .isEqualTo(HttpURLConnection.HTTP_CREATED);

        final int createdHardwareId = HardwareUtils.ResponseParser.create(createResponse).getId();
        final Hardware hardwareWithId = Hardware.updateWithId(createdHardwareId, hardware);

        final HttpResponse<String> updateResponse = HardwareUtils.RequestSender.update(hardwareWithId);

        assertThat(updateResponse.statusCode())
                .as("Did not receive a 200_OK HTTP response")
                .isEqualTo(HttpURLConnection.HTTP_OK);

        final Hardware actual = HardwareUtils.ResponseParser.update(updateResponse);

        assertThat(actual)
                .as("Did not receive an empty JSON response")
                .isEqualTo(hardwareWithId);
    }

    @Test
    public void whenCreatingBatchOfHardware_givenPayloadIsPartiallyValid_thenOnlyValidHardwareIsCreated_andResponseHasA200Status() throws IOException, InterruptedException {
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
                .as("Did not receive a 200_OK HTTP response")
                .isEqualTo(HttpURLConnection.HTTP_OK);

        final int newHardwareSize = HardwareUtils.ResponseParser.getAll(HardwareUtils.RequestSender.getAll()).size();
        assertThat(newHardwareSize)
                .as("Get all response did not return the initial hardware + new valid hardware")
                .isEqualTo(initialHardwareSize + batchOfValidHardware.size());
    }

    @Test
    public void whenCreatingBatchOfHardware_givenPayloadIsInvalid_thenResponseHasA400Status() throws IOException, InterruptedException {
        final int initialHardwareSize = HardwareUtils.ResponseParser.getAll(HardwareUtils.RequestSender.getAll()).size();

        final List<Hardware> batchOfInvalidHardware = List.of(
                Hardware.createWithoutId("Hardware1", "Hardware1", OperatingSystem.INVALID, 1.0D),
                Hardware.createWithoutId("Hardware2", "Hardware2", OperatingSystem.INVALID, 1.0D)
        );

        final HttpResponse<String> response = HardwareUtils.RequestSender.createBatchOf(batchOfInvalidHardware);
        assertThat(response.statusCode())
                .as("Did not receive a 400_BAD_REQUEST HTTP response")
                .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);

        final int newHardwareSize = HardwareUtils.ResponseParser.getAll(HardwareUtils.RequestSender.getAll()).size();
        assertThat(newHardwareSize)
                .as("Get all response did not return only the initial hardware")
                .isEqualTo(initialHardwareSize);
    }

    @Test
    public void whenDeletingHardware_givenTheHardwareIsLinkedToAUser_thenResponseHasA409Status() throws IOException, InterruptedException {
        final HttpResponse<String> createHardwareResponse = HardwareUtils.RequestSender.create(DUMMY_HARDWARE);
        assertThat(createHardwareResponse.statusCode())
                .as("Was not able to create hardware")
                .isEqualTo(HttpURLConnection.HTTP_CREATED);

        final int hardwareId = HardwareUtils.ResponseParser.create(createHardwareResponse).getId();

        final HttpResponse<String> createUserResponse = UserUtils.RequestSender.create(UserTest.DUMMY_USER);
        assertThat(createUserResponse.statusCode())
                .as("Was not able to create user: " + createUserResponse.statusCode() + ": " + createUserResponse.body())
                .isEqualTo(HttpURLConnection.HTTP_CREATED);

        final HttpResponse<String> deleteHardwareResponse = HardwareUtils.RequestSender.delete(hardwareId);
        assertThat(deleteHardwareResponse.statusCode())
                .as("Expected to fail due to a 409_CONFLICT")
                .isEqualTo(HttpURLConnection.HTTP_CONFLICT);
    }

    @AfterClass
    public static void tearDown() throws SQLException, IOException, InterruptedException {
        cleanSystemForHardwareTests();
    }

    private static void cleanSystemForHardwareTests() throws IOException, InterruptedException, SQLException {
        final Collection<User> allUsers = UserUtils.ResponseParser.getAll(UserUtils.RequestSender.getAll());
        for (final User user : allUsers) {
            UserUtils.RequestSender.delete(user.getId());
        }

        final Collection<Hardware> allHardware = HardwareUtils.ResponseParser.getAll(HardwareUtils.RequestSender.getAll());
        for (final Hardware hardware : allHardware) {
            HardwareUtils.RequestSender.delete(hardware.getId());
        }

        DatabaseCleaner.truncateTableAndResetId("users", "hardware");
    }
}

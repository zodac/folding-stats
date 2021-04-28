package me.zodac.folding.test;

import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.OperatingSystem;
import me.zodac.folding.test.utils.DatabaseCleaner;
import me.zodac.folding.test.utils.HardwareUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.http.HttpResponse;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Arquillian.class)
public class HardwareIT {

    private static final Hardware DEFAULT_HARDWARE = Hardware.createWithoutId("Test GPU", "Base GPU", OperatingSystem.WINDOWS, 1.0D);

    @Deployment(order = 1)
    public static EnterpriseArchive getTestEar() {
        return Deployments.getTestEar();
    }

    @Test
    @RunAsClient
    @InSequence(1)
    public void whenGettingAllHardware_givenNoHardwareHasBeenCreated_thenAnEmptyJsonResponseIsReturned_andHasA200Status() throws IOException, InterruptedException {
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
    @RunAsClient
    @InSequence(2)
    public void whenCreatingHardware_givenPayloadIsValid_thenTheCreatedHardwareIsReturnedInResponse_andHasId_andResponseHasA201StatusCode() throws IOException, InterruptedException {
        final HttpResponse<String> response = HardwareUtils.RequestSender.create(DEFAULT_HARDWARE);
        assertThat(response.statusCode())
                .as("Did not receive a 201_CREATED HTTP response")
                .isEqualTo(HttpURLConnection.HTTP_CREATED);

        final Hardware actualHardware = HardwareUtils.ResponseParser.create(response);
        final Hardware expectedHardware = Hardware.updateWithId(actualHardware.getId(), DEFAULT_HARDWARE);
        assertThat(actualHardware)
                .as("Did not receive created object as JSON response")
                .isEqualTo(expectedHardware);
    }

    @Test
    @RunAsClient
    @InSequence(3)
    public void whenGettingHardware_givenAnValidHardwareId_thenHardwareIsReturned_andHasA200Status() throws IOException, InterruptedException {
        final Collection<Hardware> allHardware = HardwareUtils.ResponseParser.getAll(HardwareUtils.RequestSender.getAll());
        int hardwareId = allHardware.size();

        if (allHardware.isEmpty()) {
            HardwareUtils.RequestSender.create(DEFAULT_HARDWARE);
            hardwareId = 1;
        }

        final HttpResponse<String> response = HardwareUtils.RequestSender.get(hardwareId);
        assertThat(response.statusCode())
                .as("Did not receive a 200_OK HTTP response")
                .isEqualTo(HttpURLConnection.HTTP_OK);

        final Hardware hardware = HardwareUtils.ResponseParser.get(response);
        assertThat(hardware)
                .as("Did not receive a valid hardware")
                .extracting("id", "hardwareName", "displayName", "operatingSystem", "multiplier")
                .doesNotContainNull()
                .containsExactly(hardwareId, "Test GPU", "Base GPU", OperatingSystem.WINDOWS.displayName(), 1.0D);
    }

    @Test
    @RunAsClient
    @InSequence(4)
    public void whenUpdatingHardware_givenAValidHardwareId_andAValidPayload_thenUpdatedHardwareIsReturned_andNoNewHardwareIsCreated_andHasA200Status() throws IOException, InterruptedException {
        final Collection<Hardware> allHardware = HardwareUtils.ResponseParser.getAll(HardwareUtils.RequestSender.getAll());
        int hardwareId = allHardware.size();

        if (allHardware.isEmpty()) {
            HardwareUtils.RequestSender.create(DEFAULT_HARDWARE);
            hardwareId = 1;
        }

        final Hardware updatedHardware = Hardware.create(hardwareId, "Test GPU", "Base GPU", OperatingSystem.LINUX, 1.0D);
        final HttpResponse<String> response = HardwareUtils.RequestSender.update(updatedHardware);
        assertThat(response.statusCode())
                .as("Did not receive a 200_OK HTTP response")
                .isEqualTo(HttpURLConnection.HTTP_OK);

        final Hardware actualHardware = HardwareUtils.ResponseParser.update(response);
        assertThat(actualHardware)
                .as("Did not receive created object as JSON response")
                .isEqualTo(updatedHardware);


        final Collection<Hardware> allHardwareAfterUpdate = HardwareUtils.ResponseParser.getAll(HardwareUtils.RequestSender.getAll());
        assertThat(allHardwareAfterUpdate)
                .as("Expected no new hardware instances to be created")
                .hasSize(allHardware.size());
    }

    @Test
    @RunAsClient
    @InSequence(5)
    public void whenDeletingHardware_givenAValidHardwareId_thenHardwareIsDeleted_andHasA204Status_andHardwareCannotBeRetrievedAgain() throws IOException, InterruptedException {
        final Collection<Hardware> allHardware = HardwareUtils.ResponseParser.getAll(HardwareUtils.RequestSender.getAll());
        int hardwareId = allHardware.size();

        if (allHardware.isEmpty()) {
            HardwareUtils.RequestSender.create(DEFAULT_HARDWARE);
            hardwareId = 1;
        }

        final HttpResponse<String> response = HardwareUtils.RequestSender.delete(hardwareId);
        assertThat(response.statusCode())
                .as("Did not receive a 204_NO_CONTENT HTTP response")
                .isEqualTo(HttpURLConnection.HTTP_NO_CONTENT);


        final HttpResponse<String> getResponse = HardwareUtils.RequestSender.get(hardwareId);

        assertThat(getResponse.statusCode())
                .as("Was able to retrieve the hardware instance, despite deleting it")
                .isEqualTo(HttpURLConnection.HTTP_NOT_FOUND);
    }

    // Negative/alternative test cases

    @Test
    @RunAsClient
    @InSequence(6)
    public void whenCreatingHardware_givenAnInvalidHardware_thenJsonResponseWithErrorsIsReturned_andHasA400Status() throws IOException, InterruptedException {
        final Hardware hardware = Hardware.createWithoutId("Test GPU", "Base GPU", OperatingSystem.INVALID, 1.0D);

        final HttpResponse<String> response = HardwareUtils.RequestSender.create(hardware);

        assertThat(response.statusCode())
                .as("Did not receive a 400_BAD_REQUEST HTTP response")
                .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);

        assertThat(response.body())
                .as("Did not get expected error response explaining the invalid operating system")
                .contains("Attribute 'operatingSystem' must be one of:", OperatingSystem.getValuesAsList().toString());
    }


    @Test
    @RunAsClient
    @InSequence(7)
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
    @RunAsClient
    @InSequence(8)
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
    @RunAsClient
    @InSequence(9)
    public void whenDeletingHardware_givenANonExistingHardwareId_thenNoJsonResponseIsReturned_andHasA204Status() throws IOException, InterruptedException {
        final int invalidId = 99;
        final HttpResponse<String> response = HardwareUtils.RequestSender.delete(invalidId);

        assertThat(response.statusCode())
                .as("Did not receive a 204_NO_CONTENT HTTP response")
                .isEqualTo(HttpURLConnection.HTTP_NO_CONTENT);

        assertThat(response.body())
                .as("Did not receive an empty JSON response")
                .isEmpty();
    }

    @Test
    @RunAsClient
    @InSequence(10)
    public void whenUpdatingHardware_givenAValidHardwareId_andPayloadHasNoChanges_thenOriginalHardwareIsReturned_andHasA204Status() throws IOException, InterruptedException {
        final Hardware hardware = Hardware.createWithoutId("Test GPU", "Base GPU", OperatingSystem.WINDOWS, 1.0D);

        final HttpResponse<String> createResponse = HardwareUtils.RequestSender.create(hardware);
        assertThat(createResponse.statusCode())
                .as("Did not receive a 201_CREATED HTTP response")
                .isEqualTo(HttpURLConnection.HTTP_CREATED);

        final int createdHardwareId = HardwareUtils.ResponseParser.create(createResponse).getId();
        final Hardware hardwareWithId = Hardware.updateWithId(createdHardwareId, hardware);

        final HttpResponse<String> updateResponse = HardwareUtils.RequestSender.update(hardwareWithId);

        assertThat(updateResponse.statusCode())
                .as("Did not receive a 204_NO_CONTENT HTTP response")
                .isEqualTo(HttpURLConnection.HTTP_NO_CONTENT);

        assertThat(updateResponse.body())
                .as("Did not receive an empty JSON response")
                .isEmpty();
    }

    // TODO: [zodac] If hardware is linked to a user when deleted, test for a 409_CONFLICT, but need UserUtils first

    @Test
    @RunAsClient
    @InSequence(99)
    public void tearDown() throws SQLException, IOException, InterruptedException {
        final Collection<Hardware> allHardware = HardwareUtils.ResponseParser.getAll(HardwareUtils.RequestSender.getAll());

        for (final Hardware hardware : allHardware) {
            HardwareUtils.RequestSender.delete(hardware.getId());
        }

        DatabaseCleaner.truncateTableAndResetId("hardware");
    }
}

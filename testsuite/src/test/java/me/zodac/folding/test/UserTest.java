package me.zodac.folding.test;

import me.zodac.folding.api.tc.Category;
import me.zodac.folding.api.tc.Hardware;
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
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class UserTest {

    // The StubbedUnitsEndpoint has been configured to always return 1 WU for 'Dummy_User'
    public static final User DUMMY_USER = User.createWithoutId("Dummy_User", "Dummy User", "DummyPasskey", Category.NVIDIA_GPU, 1, "", false);

    @BeforeClass
    public static void setUp() throws SQLException, IOException, InterruptedException {
        cleanSystemForUserTests();
        HardwareUtils.RequestSender.create(HardwareTest.DUMMY_HARDWARE);
    }

    @Test
    public void whenGettingAllUsers_givenNoUserHasBeenCreated_thenAnEmptyJsonResponseIsReturned_andHasA200Status() throws IOException, InterruptedException, SQLException {
        cleanSystemOfUsers(); // No guarantee that this test runs first, so we need to clean the system of users again
        final HttpResponse<String> response = UserUtils.RequestSender.getAll();
        assertThat(response.statusCode())
                .as("Did not receive a 200_OK HTTP response")
                .isEqualTo(HttpURLConnection.HTTP_OK);

        final Collection<User> allUsers = UserUtils.ResponseParser.getAll(response);
        final Map<String, List<String>> headers = response.headers().map();
        assertThat(headers)
                .containsKey("X-Total-Count");

        assertThat(headers.get("X-Total-Count").get(0))
                .isEqualTo(String.valueOf(allUsers.size()));

        assertThat(allUsers)
                .isEmpty();
    }

    @Test
    public void whenCreatingUser_givenPayloadIsValid_thenTheCreatedUserIsReturnedInResponse_andHasId_andResponseHasA201StatusCode() throws IOException, InterruptedException {
        final User userToCreate = User.createWithoutId("Dummy_User", "Dummy User", "DummyPasskey1", Category.NVIDIA_GPU, 1, "", false);
        final HttpResponse<String> response = UserUtils.RequestSender.create(userToCreate);
        assertThat(response.statusCode())
                .as("Did not receive a 201_CREATED HTTP response")
                .isEqualTo(HttpURLConnection.HTTP_CREATED);

        final User actual = UserUtils.ResponseParser.create(response);
        final User expected = User.updateWithId(actual.getId(), userToCreate);
        assertThat(actual)
                .as("Did not receive created object as JSON response")
                .isEqualTo(expected);
    }

    @Test
    public void whenCreatingBatchOfUsers_givenPayloadIsValid_thenTheUsersAreCreated_andResponseHasA200Status() throws IOException, InterruptedException {
        final int initialSize = UserUtils.ResponseParser.getAll(UserUtils.RequestSender.getAll()).size();

        final List<User> batchOfUsers = List.of(
                User.createWithoutId("Dummy_User", "Dummy User", "DummyPasskey2", Category.NVIDIA_GPU, 1, "", false),
                User.createWithoutId("Dummy_User", "Dummy User", "DummyPasskey3", Category.NVIDIA_GPU, 1, "", false),
                User.createWithoutId("Dummy_User", "Dummy User", "DummyPasskey4", Category.NVIDIA_GPU, 1, "", false)
        );

        final HttpResponse<String> response = UserUtils.RequestSender.createBatchOf(batchOfUsers);
        assertThat(response.statusCode())
                .as("Did not receive a 200_OK HTTP response")
                .isEqualTo(HttpURLConnection.HTTP_OK);

        final int newSize = UserUtils.ResponseParser.getAll(UserUtils.RequestSender.getAll()).size();
        assertThat(newSize)
                .as("Get all response did not return the initial users + new users")
                .isEqualTo(initialSize + batchOfUsers.size());
    }

    @Test
    public void whenGettingUser_givenAValidUserId_thenUserIsReturned_andHasA200Status() throws IOException, InterruptedException {
        final Collection<User> allUsers = UserUtils.ResponseParser.getAll(UserUtils.RequestSender.getAll());
        int userId = allUsers.size();

        if (allUsers.isEmpty()) {
            userId = UserUtils.ResponseParser.create(UserUtils.RequestSender.create(DUMMY_USER)).getId();
        }

        final HttpResponse<String> response = UserUtils.RequestSender.get(userId);
        assertThat(response.statusCode())
                .as("Did not receive a 200_OK HTTP response")
                .isEqualTo(HttpURLConnection.HTTP_OK);

        final User user = UserUtils.ResponseParser.get(response);
        assertThat(user)
                .as("Did not receive a valid user")
                .extracting("id")
                .isEqualTo(userId);
    }

    @Test
    public void whenUpdatingUser_givenAValidUserId_andAValidPayload_thenUpdatedUserIsReturned_andNoNewUserIsCreated_andHasA200Status() throws IOException, InterruptedException {
        final Collection<User> allUsers = UserUtils.ResponseParser.getAll(UserUtils.RequestSender.getAll());
        int userId = allUsers.size();

        if (allUsers.isEmpty()) {
            userId = UserUtils.ResponseParser.create(UserUtils.RequestSender.create(DUMMY_USER)).getId();
        }

        final User updatedUser = User.create(userId, "Dummy_User", "Dummy User", "DummyPasskey", Category.AMD_GPU, 1, "", false);
        final HttpResponse<String> response = UserUtils.RequestSender.update(updatedUser);
        assertThat(response.statusCode())
                .as("Did not receive a 200_OK HTTP response")
                .isEqualTo(HttpURLConnection.HTTP_OK);

        final User actual = UserUtils.ResponseParser.update(response);
        assertThat(actual)
                .as("Did not receive created object as JSON response")
                .isEqualTo(updatedUser);


        final Collection<User> allHardwareAfterUpdate = UserUtils.ResponseParser.getAll(UserUtils.RequestSender.getAll());
        assertThat(allHardwareAfterUpdate)
                .as("Expected no new user instances to be created")
                .hasSize(allUsers.size());
    }

    @Test
    public void whenDeletingUser_givenAValidUserId_thenUserIsDeleted_andHasA200Status_andUserCountIsReduced_andUserCannotBeRetrievedAgain() throws IOException, InterruptedException {
        final Collection<User> allUsers = UserUtils.ResponseParser.getAll(UserUtils.RequestSender.getAll());
        int userId = allUsers.size();

        if (allUsers.isEmpty()) {
            userId = UserUtils.ResponseParser.create(UserUtils.RequestSender.create(DUMMY_USER)).getId();
        }

        final HttpResponse<String> response = UserUtils.RequestSender.delete(userId);
        assertThat(response.statusCode())
                .as("Did not receive a 200_OK HTTP response")
                .isEqualTo(HttpURLConnection.HTTP_OK);

        final HttpResponse<String> getResponse = UserUtils.RequestSender.get(userId);
        assertThat(getResponse.statusCode())
                .as("Was able to retrieve the user instance, despite deleting it")
                .isEqualTo(HttpURLConnection.HTTP_NOT_FOUND);

        final int newSize = UserUtils.ResponseParser.getAll(UserUtils.RequestSender.getAll()).size();
        assertThat(newSize)
                .as("Get all response did not return the initial users - deleted user")
                .isEqualTo(userId - 1);
    }

    // Negative/alternative test cases

    // TODO: [zodac] Update a user with new hardware, stats will need to be updated, will cause a problem since they won't have any stats to begin with! Do in TcStatsTest

//    @Test
//    public void whenCreatingHardware_givenAnInvalidHardware_thenJsonResponseWithErrorsIsReturned_andHasA400Status() throws IOException, InterruptedException {
//        final Hardware hardware = Hardware.createWithoutId("Test GPU", "Base GPU", OperatingSystem.INVALID, 1.0D);
//
//        final HttpResponse<String> response = UserUtils.RequestSender.create(hardware);
//
//        assertThat(response.statusCode())
//                .as("Did not receive a 400_BAD_REQUEST HTTP response")
//                .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);
//    }
//
//
//    @Test
//    public void whenGettingHardware_givenANonExistingHardwareId_thenNoJsonResponseIsReturned_andHasA404Status() throws IOException, InterruptedException {
//        final int invalidId = 99;
//        final HttpResponse<String> response = UserUtils.RequestSender.get(invalidId);
//
//        assertThat(response.statusCode())
//                .as("Did not receive a 404_NOT_FOUND HTTP response")
//                .isEqualTo(HttpURLConnection.HTTP_NOT_FOUND);
//
//        assertThat(response.body())
//                .as("Did not receive an empty JSON response")
//                .isEmpty();
//    }
//
//    @Test
//    public void whenUpdatingHardware_givenANonExistingHardwareId_thenNoJsonResponseIsReturned_andHasA404Status() throws IOException, InterruptedException {
//        final int invalidId = 99;
//        final Hardware updatedHardware = Hardware.create(invalidId, "Test GPU", "Base GPU", OperatingSystem.WINDOWS, 1.0D);
//
//        final HttpResponse<String> response = UserUtils.RequestSender.update(updatedHardware);
//        assertThat(response.statusCode())
//                .as("Did not receive a 404_NOT_FOUND HTTP response")
//                .isEqualTo(HttpURLConnection.HTTP_NOT_FOUND);
//
//        assertThat(response.body())
//                .as("Did not receive an empty JSON response")
//                .isEmpty();
//    }
//
//    @Test
//    public void whenDeletingHardware_givenANonExistingHardwareId_thenNoJsonResponseIsReturned_andHasA204Status() throws IOException, InterruptedException {
//        final int invalidId = 99;
//        final HttpResponse<String> response = UserUtils.RequestSender.delete(invalidId);
//
//        assertThat(response.statusCode())
//                .as("Did not receive a 204_NO_CONTENT HTTP response")
//                .isEqualTo(HttpURLConnection.HTTP_NO_CONTENT);
//
//        assertThat(response.body())
//                .as("Did not receive an empty JSON response")
//                .isEmpty();
//    }
//
//    @Test
//    public void whenUpdatingHardware_givenAValidHardwareId_andPayloadHasNoChanges_thenOriginalHardwareIsReturned_andHasA204Status() throws IOException, InterruptedException {
//        final Hardware hardware = Hardware.createWithoutId("Test GPU", "Base GPU", OperatingSystem.WINDOWS, 1.0D);
//
//        final HttpResponse<String> createResponse = UserUtils.RequestSender.create(hardware);
//        assertThat(createResponse.statusCode())
//                .as("Did not receive a 201_CREATED HTTP response")
//                .isEqualTo(HttpURLConnection.HTTP_CREATED);
//
//        final int createdHardwareId = UserUtils.ResponseParser.create(createResponse).getId();
//        final Hardware hardwareWithId = Hardware.updateWithId(createdHardwareId, hardware);
//
//        final HttpResponse<String> updateResponse = UserUtils.RequestSender.update(hardwareWithId);
//
//        assertThat(updateResponse.statusCode())
//                .as("Did not receive a 204_NO_CONTENT HTTP response")
//                .isEqualTo(HttpURLConnection.HTTP_NO_CONTENT);
//
//        assertThat(updateResponse.body())
//                .as("Did not receive an empty JSON response")
//                .isEmpty();
//    }
//
//    @Test
//    public void whenCreatingBatchOfHardware_givenPayloadIsPartiallyValid_thenOnlyValidHardwareIsCreated_andResponseHasA200Status() throws IOException, InterruptedException {
//        final int initialHardwareSize = UserUtils.ResponseParser.getAll(UserUtils.RequestSender.getAll()).size();
//
//        final List<Hardware> batchOfValidHardware = List.of(
//                Hardware.createWithoutId("Hardware1", "Hardware1", OperatingSystem.WINDOWS, 1.0D),
//                Hardware.createWithoutId("Hardware2", "Hardware2", OperatingSystem.WINDOWS, 1.0D)
//        );
//        final List<Hardware> batchOfInvalidHardware = List.of(
//                Hardware.createWithoutId("Hardware1", "Hardware1", OperatingSystem.INVALID, 1.0D),
//                Hardware.createWithoutId("Hardware2", "Hardware2", OperatingSystem.INVALID, 1.0D)
//        );
//        final List<Hardware> batchOfHardware = new ArrayList<>(batchOfValidHardware.size() + batchOfInvalidHardware.size());
//        batchOfHardware.addAll(batchOfValidHardware);
//        batchOfHardware.addAll(batchOfInvalidHardware);
//
//        final HttpResponse<String> response = UserUtils.RequestSender.createBatchOf(batchOfHardware);
//        assertThat(response.statusCode())
//                .as("Did not receive a 200_OK HTTP response")
//                .isEqualTo(HttpURLConnection.HTTP_OK);
//
//        final int newHardwareSize = UserUtils.ResponseParser.getAll(UserUtils.RequestSender.getAll()).size();
//        assertThat(newHardwareSize)
//                .as("Get all response did not return the initial hardware + new valid hardware")
//                .isEqualTo(initialHardwareSize + batchOfValidHardware.size());
//    }
//
//    @Test
//    public void whenCreatingBatchOfHardware_givenPayloadIsInvalid_thenResponseHasA400Status() throws IOException, InterruptedException {
//        final int initialHardwareSize = UserUtils.ResponseParser.getAll(UserUtils.RequestSender.getAll()).size();
//
//        final List<Hardware> batchOfInvalidHardware = List.of(
//                Hardware.createWithoutId("Hardware1", "Hardware1", OperatingSystem.INVALID, 1.0D),
//                Hardware.createWithoutId("Hardware2", "Hardware2", OperatingSystem.INVALID, 1.0D)
//        );
//
//        final HttpResponse<String> response = UserUtils.RequestSender.createBatchOf(batchOfInvalidHardware);
//        assertThat(response.statusCode())
//                .as("Did not receive a 400_BAD_REQUEST HTTP response")
//                .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);
//
//        final int newHardwareSize = UserUtils.ResponseParser.getAll(UserUtils.RequestSender.getAll()).size();
//        assertThat(newHardwareSize)
//                .as("Get all response did not return only the initial hardware")
//                .isEqualTo(initialHardwareSize);
//    }
//
//    @Test
//    public void whenDeletingHardware_givenTheHardwareIsLinkedToAUser_thenResponseHasA409Status() throws IOException, InterruptedException {
//        final HttpResponse<String> createHardwareResponse = UserUtils.RequestSender.create(DEFAULT_HARDWARE);
//        assertThat(createHardwareResponse.statusCode())
//                .as("Was not able to create hardware")
//                .isEqualTo(HttpURLConnection.HTTP_CREATED);
//
//        final int hardwareId = UserUtils.ResponseParser.create(createHardwareResponse).getId();
//
//        final User user = User.createWithoutId("user", "user", "passkey", Category.AMD_GPU, hardwareId, "", false);
//        final HttpResponse<String> createUserResponse = UserUtils.RequestSender.create(user);
//        assertThat(createUserResponse.statusCode())
//                .as("Was not able to create user")
//                .isEqualTo(HttpURLConnection.HTTP_CREATED);
//
//        final HttpResponse<String> deleteHardwareResponse = UserUtils.RequestSender.delete(hardwareId);
//        assertThat(deleteHardwareResponse.statusCode())
//                .as("Expected to fail due to a 409_CONFLICT")
//                .isEqualTo(HttpURLConnection.HTTP_CONFLICT);
//    }

    @AfterClass
    public static void tearDown() throws SQLException, IOException, InterruptedException {
        cleanSystemForUserTests();
    }

    private static void cleanSystemOfUsers() throws SQLException, IOException, InterruptedException {
        final Collection<User> allUsers = UserUtils.ResponseParser.getAll(UserUtils.RequestSender.getAll());
        for (final User user : allUsers) {
            UserUtils.RequestSender.delete(user.getId());
        }

        DatabaseCleaner.truncateTableAndResetId("users");
    }

    private static void cleanSystemForUserTests() throws IOException, InterruptedException, SQLException {
        final Collection<User> allUsers = UserUtils.ResponseParser.getAll(UserUtils.RequestSender.getAll());
        for (final User user : allUsers) {
            UserUtils.RequestSender.delete(user.getId());
        }

        final Collection<Hardware> allHardware = HardwareUtils.ResponseParser.getAll(HardwareUtils.RequestSender.getAll());
        for (final Hardware hardware : allHardware) {
            HardwareUtils.RequestSender.delete(hardware.getId());
        }

        DatabaseCleaner.truncateTableAndResetId("hardware", "users");
    }
}

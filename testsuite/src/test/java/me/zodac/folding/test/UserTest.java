package me.zodac.folding.test;

import me.zodac.folding.api.tc.Category;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.test.utils.DatabaseCleaner;
import me.zodac.folding.test.utils.HardwareUtils;
import me.zodac.folding.test.utils.StubbedFoldingEndpointUtils;
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

public class UserTest {

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
                .as("Did not receive a 200_OK HTTP response: " + response.body())
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
        final User userToCreate = User.createWithoutId("Dummy_User1", "Dummy User", "DummyPasskey1", Category.NVIDIA_GPU, 1, "", false);
        StubbedFoldingEndpointUtils.enableUser(userToCreate);

        final HttpResponse<String> response = UserUtils.RequestSender.create(userToCreate);
        assertThat(response.statusCode())
                .as("Did not receive a 201_CREATED HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_CREATED);

        final User actual = UserUtils.ResponseParser.create(response);
        final User expected = User.updateWithId(actual.getId(), userToCreate);
        assertThat(actual)
                .as("Did not receive created object as JSON response: " + response.body())
                .isEqualTo(expected);
    }

    @Test
    public void whenCreatingBatchOfUsers_givenPayloadIsValid_thenTheUsersAreCreated_andResponseHasA200Status() throws IOException, InterruptedException {
        final int initialSize = UserUtils.ResponseParser.getAll(UserUtils.RequestSender.getAll()).size();

        final List<User> batchOfUsers = List.of(
                User.createWithoutId("Dummy_User2", "Dummy User", "DummyPasskey2", Category.NVIDIA_GPU, 1, "", false),
                User.createWithoutId("Dummy_User3", "Dummy User", "DummyPasskey3", Category.NVIDIA_GPU, 1, "", false),
                User.createWithoutId("Dummy_User4", "Dummy User", "DummyPasskey4", Category.NVIDIA_GPU, 1, "", false)
        );

        for (final User user : batchOfUsers) {
            StubbedFoldingEndpointUtils.enableUser(user);
        }


        final HttpResponse<String> response = UserUtils.RequestSender.createBatchOf(batchOfUsers);
        assertThat(response.statusCode())
                .as("Did not receive a 200_OK HTTP response: " + response.body())
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
            StubbedFoldingEndpointUtils.enableUser(UserTest.DUMMY_USER);
            userId = UserUtils.ResponseParser.create(UserUtils.RequestSender.create(DUMMY_USER)).getId();
        }

        final HttpResponse<String> response = UserUtils.RequestSender.get(userId);
        assertThat(response.statusCode())
                .as("Did not receive a 200_OK HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_OK);

        final User user = UserUtils.ResponseParser.get(response);
        assertThat(user)
                .as("Did not receive the expected user: " + response.body())
                .extracting("id")
                .isEqualTo(userId);
    }

    @Test
    public void whenUpdatingUser_givenAValidUserId_andAValidPayload_thenUpdatedUserIsReturned_andNoNewUserIsCreated_andHasA200Status() throws IOException, InterruptedException {
        final Collection<User> allUsers = UserUtils.ResponseParser.getAll(UserUtils.RequestSender.getAll());
        int userId = allUsers.size();

        if (allUsers.isEmpty()) {
            StubbedFoldingEndpointUtils.enableUser(UserTest.DUMMY_USER);
            userId = UserUtils.ResponseParser.create(UserUtils.RequestSender.create(DUMMY_USER)).getId();
        }

        final User updatedUser = User.create(userId, "Dummy_User", "Dummy User", "DummyPasskey", Category.AMD_GPU, 1, "", false);
        final HttpResponse<String> response = UserUtils.RequestSender.update(updatedUser);
        assertThat(response.statusCode())
                .as("Did not receive a 200_OK HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_OK);

        final User actual = UserUtils.ResponseParser.update(response);
        assertThat(actual)
                .as("Did not receive created object as JSON response: " + response.body())
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
            StubbedFoldingEndpointUtils.enableUser(UserTest.DUMMY_USER);
            userId = UserUtils.ResponseParser.create(UserUtils.RequestSender.create(DUMMY_USER)).getId();
        }

        final HttpResponse<String> response = UserUtils.RequestSender.delete(userId);
        assertThat(response.statusCode())
                .as("Did not receive a 200_OK HTTP response: " + response.body())
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

    @Test
    public void whenCreatingUser_givenAUserWithInvalidHardwareId_thenJsonResponseWithErrorIsReturned_andHasA400Status() throws IOException, InterruptedException {
        final User user = User.createWithoutId("Invalid_User", "Invalid User", "InvalidPasskey", Category.NVIDIA_GPU, 0, "", false);

        final HttpResponse<String> response = UserUtils.RequestSender.create(user);

        assertThat(response.statusCode())
                .as("Did not receive a 400_BAD_REQUEST HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);

        assertThat(response.body())
                .as("Did not receive expected error message in response")
                .contains("hardwareId");
    }

    @Test
    public void whenCreatingUser_givenUserHasNoUnitsCompleted_thenUserIsNotCreated_andHasA400Stats() throws IOException, InterruptedException {
        final User user = User.createWithoutId("Invalid_User", "Invalid User", "InvalidPasskey", Category.NVIDIA_GPU, 0, "", false);
        StubbedFoldingEndpointUtils.disableUser(user);

        final HttpResponse<String> response = UserUtils.RequestSender.create(user);

        assertThat(response.statusCode())
                .as("Did not receive a 400_BAD_REQUEST HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);
    }

    @Test
    public void whenCreatingUser_givenUserWithTheSameFoldingNameAndPasskeyAlreadyExists_thenA409ResponseIsReturned() throws IOException, InterruptedException {
        if (UserUtils.RequestSender.get(DUMMY_USER.getId()).statusCode() == HttpURLConnection.HTTP_NOT_FOUND) {
            UserUtils.RequestSender.create(DUMMY_USER);
        }
        final HttpResponse<String> response = UserUtils.RequestSender.create(DUMMY_USER);

        assertThat(response.statusCode())
                .as("Did not receive a 409_CONFLICT HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_CONFLICT);
    }

    @Test
    public void whenGettingUser_givenANonExistingUserId_thenNoJsonResponseIsReturned_andHasA404Status() throws IOException, InterruptedException {
        final int invalidId = 99;
        final HttpResponse<String> response = UserUtils.RequestSender.get(invalidId);

        assertThat(response.statusCode())
                .as("Did not receive a 404_NOT_FOUND HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_NOT_FOUND);

        assertThat(response.body())
                .as("Did not receive an empty JSON response: " + response.body())
                .isEmpty();
    }

    @Test
    public void whenUpdatingUser_givenANonExistingUserId_thenNoJsonResponseIsReturned_andHasA404Status() throws IOException, InterruptedException {
        final int invalidId = 99;
        final User updatedUser = User.create(invalidId, "Invalid_User", "Invalid User", "InvalidPasskey", Category.NVIDIA_GPU, 1, "", false);
        StubbedFoldingEndpointUtils.enableUser(updatedUser);

        final HttpResponse<String> response = UserUtils.RequestSender.update(updatedUser);
        assertThat(response.statusCode())
                .as("Did not receive a 404_NOT_FOUND HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_NOT_FOUND);

        assertThat(response.body())
                .as("Did not receive an empty JSON response: " + response.body())
                .isEmpty();
    }

    @Test
    public void whenDeletingUser_givenANonExistingUserId_thenNoJsonResponseIsReturned_andHasA404Status() throws IOException, InterruptedException {
        final int invalidId = 99;
        final HttpResponse<String> response = UserUtils.RequestSender.delete(invalidId);

        assertThat(response.statusCode())
                .as("Did not receive a 404_NOT_FOUND HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_NOT_FOUND);

        assertThat(response.body())
                .as("Did not receive an empty JSON response: " + response.body())
                .isEmpty();
    }

    @Test
    public void whenUpdatingUser_givenAValidUserId_andPayloadHasNoChanges_thenOriginalUserIsReturned_andHasA200Status() throws IOException, InterruptedException {
        final User user = User.createWithoutId("Dummy_User6", "Dummy User6", "DummyPasskey6", Category.NVIDIA_GPU, 1, "", false);
        StubbedFoldingEndpointUtils.enableUser(user);

        final HttpResponse<String> createResponse = UserUtils.RequestSender.create(user);
        assertThat(createResponse.statusCode())
                .as("Did not receive a 201_CREATED HTTP response: " + createResponse.body())
                .isEqualTo(HttpURLConnection.HTTP_CREATED);

        final int createdUserId = UserUtils.ResponseParser.create(createResponse).getId();
        final User userWithId = User.updateWithId(createdUserId, user);

        final HttpResponse<String> updateResponse = UserUtils.RequestSender.update(userWithId);

        assertThat(updateResponse.statusCode())
                .as("Did not receive a 200_OK HTTP response: " + updateResponse.body())
                .isEqualTo(HttpURLConnection.HTTP_OK);

        final User actual = UserUtils.ResponseParser.update(updateResponse);

        assertThat(actual)
                .as("Did not receive the original user in response")
                .isEqualTo(userWithId);
    }

    @Test
    public void whenCreatingBatchOfUsers_givenPayloadIsPartiallyValid_thenOnlyValidUsersAreCreated_andResponseHasA200Status() throws IOException, InterruptedException {
        final int initialUsersSize = UserUtils.ResponseParser.getAll(UserUtils.RequestSender.getAll()).size();

        final List<User> batchOfValidUsers = List.of(
                User.createWithoutId("Dummy_User7", "Dummy User7", "DummyPasskey7", Category.NVIDIA_GPU, 1, "", false),
                User.createWithoutId("Dummy_User8", "Dummy User8", "DummyPasskey8", Category.NVIDIA_GPU, 1, "", false)
        );
        final List<User> batchOfInvalidUsers = List.of(
                User.createWithoutId("Dummy_User9", "Dummy User9", "DummyPasskey9", Category.NVIDIA_GPU, 0, "", false),
                User.createWithoutId("Dummy_User10", "Dummy User10", "DummyPasskey10", Category.NVIDIA_GPU, 0, "", false)
        );
        final List<User> batchOfUsers = new ArrayList<>(batchOfValidUsers.size() + batchOfInvalidUsers.size());
        batchOfUsers.addAll(batchOfValidUsers);
        batchOfUsers.addAll(batchOfInvalidUsers);

        for (final User validUser : batchOfValidUsers) {
            StubbedFoldingEndpointUtils.enableUser(validUser);
        }


        final HttpResponse<String> response = UserUtils.RequestSender.createBatchOf(batchOfUsers);
        assertThat(response.statusCode())
                .as("Did not receive a 200_OK HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_OK);

        final int newUsersSize = UserUtils.ResponseParser.getAll(UserUtils.RequestSender.getAll()).size();
        assertThat(newUsersSize)
                .as("Get all response did not return the initial users + new valid users")
                .isEqualTo(initialUsersSize + batchOfValidUsers.size());
    }

    @Test
    public void whenCreatingBatchOfUsers_givenPayloadIsInvalid_thenResponseHasA400Status() throws IOException, InterruptedException {
        final int initialUsersSize = UserUtils.ResponseParser.getAll(UserUtils.RequestSender.getAll()).size();

        final List<User> batchOfInvalidUsers = List.of(
                User.createWithoutId("Dummy_User11", "Dummy User11", "DummyPasskey11", Category.NVIDIA_GPU, 0, "", false),
                User.createWithoutId("Dummy_User11", "Dummy User11", "DummyPasskey11", Category.NVIDIA_GPU, 0, "", false)
        );

        final HttpResponse<String> response = UserUtils.RequestSender.createBatchOf(batchOfInvalidUsers);
        assertThat(response.statusCode())
                .as("Did not receive a 400_BAD_REQUEST HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);

        final int newHardwareSize = UserUtils.ResponseParser.getAll(UserUtils.RequestSender.getAll()).size();
        assertThat(newHardwareSize)
                .as("Get all response did not return only the initial users")
                .isEqualTo(initialUsersSize);
    }

    // TODO: [zodac] When team tests are complete
//    @Test
//    public void whenDeletingUser_givenTheUserIsLinkedToTeam_thenResponseHasA409Status() throws IOException, InterruptedException {
//        final HttpResponse<String> createHardwareResponse = UserUtils.RequestSender.create(DEFAULT_HARDWARE);
//        assertThat(createHardwareResponse.statusCode())
//                .as("Was not able to create hardware: " + createHardwareResponse.body())
//                .isEqualTo(HttpURLConnection.HTTP_CREATED);
//
//        final int hardwareId = UserUtils.ResponseParser.create(createHardwareResponse).getId();
//
//        final User user = User.createWithoutId("user", "user", "passkey", Category.AMD_GPU, hardwareId, "", false);
//        final HttpResponse<String> createUserResponse = UserUtils.RequestSender.create(user);
//        assertThat(createUserResponse.statusCode())
//                .as("Was not able to create user: " + createUserResponse.body())
//                .isEqualTo(HttpURLConnection.HTTP_CREATED);
//
//        final HttpResponse<String> deleteHardwareResponse = UserUtils.RequestSender.delete(hardwareId);
//        assertThat(deleteHardwareResponse.statusCode())
//                .as("Expected to fail due to a 409_CONFLICT: " + deleteHardwareResponse.body())
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

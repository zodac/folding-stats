package me.zodac.folding.test;

import me.zodac.folding.api.tc.Team;
import me.zodac.folding.test.utils.HardwareUtils;
import me.zodac.folding.test.utils.UserUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import static me.zodac.folding.test.utils.SystemCleaner.cleanSystemForTests;

/**
 * Tests for the {@link Team} REST endpoint at <code>/folding/teams</code>.
 */
public class TeamTest {

//    public static final Team DUMMY_TEAM = Team.createWithoutId("Dummy_Team", "Dummy Team", 1, Set.of(1), Collections.emptySet());

    @BeforeClass
    public static void setUp() {
        cleanSystemForTests();
        HardwareUtils.RequestSender.create(HardwareTest.DUMMY_HARDWARE);
        UserUtils.RequestSender.create(UserTest.DUMMY_USER);
    }

    //    @Test
//    public void whenGettingAllTeams_givenNoTeamHasBeenCreated_thenAnEmptyJsonResponseIsReturned_andHasA200Status() {
//        cleanSystemOfTeams(); // No guarantee that this test runs first, so we need to clean the system of users again
//        final HttpResponse<String> response = TeamUtils.RequestSender.getAll();
//        assertThat(response.statusCode())
//                .as("Did not receive a 200_OK HTTP response: " + response.body())
//                .isEqualTo(HttpURLConnection.HTTP_OK);
//
//        final Collection<User> allUsers = TeamUtils.ResponseParser.getAll(response);
//        final Map<String, List<String>> headers = response.headers().map();
//        assertThat(headers)
//                .containsKey("X-Total-Count");
//
//        assertThat(headers.get("X-Total-Count").get(0))
//                .isEqualTo(String.valueOf(allUsers.size()));
//
//        assertThat(allUsers)
//                .isEmpty();
//    }
//
//    @Test
//    public void whenCreatingUser_givenPayloadIsValid_thenTheCreatedUserIsReturnedInResponse_andHasId_andResponseHasA201StatusCode() {
//        final User userToCreate = User.createWithoutId("Dummy_User1", "Dummy User", "DummyPasskey1", Category.NVIDIA_GPU, 1, "", false);
//        StubbedFoldingEndpointUtils.enableUser(userToCreate);
//
//        final HttpResponse<String> response = TeamUtils.RequestSender.create(userToCreate);
//        assertThat(response.statusCode())
//                .as("Did not receive a 201_CREATED HTTP response: " + response.body())
//                .isEqualTo(HttpURLConnection.HTTP_CREATED);
//
//        final User actual = TeamUtils.ResponseParser.create(response);
//        final User expected = User.updateWithId(actual.getId(), userToCreate);
//        assertThat(actual)
//                .as("Did not receive created object as JSON response: " + response.body())
//                .isEqualTo(expected);
//    }
//
//    @Test
//    public void whenCreatingBatchOfUsers_givenPayloadIsValid_thenTheUsersAreCreated_andResponseHasA200Status() {
//        final int initialSize = TeamUtils.ResponseParser.getAll(TeamUtils.RequestSender.getAll()).size();
//
//        final List<User> batchOfUsers = List.of(
//                User.createWithoutId("Dummy_User2", "Dummy User", "DummyPasskey2", Category.NVIDIA_GPU, 1, "", false),
//                User.createWithoutId("Dummy_User3", "Dummy User", "DummyPasskey3", Category.NVIDIA_GPU, 1, "", false),
//                User.createWithoutId("Dummy_User4", "Dummy User", "DummyPasskey4", Category.NVIDIA_GPU, 1, "", false)
//        );
//
//        for (final User user : batchOfUsers) {
//            StubbedFoldingEndpointUtils.enableUser(user);
//        }
//
//
//        final HttpResponse<String> response = TeamUtils.RequestSender.createBatchOf(batchOfUsers);
//        assertThat(response.statusCode())
//                .as("Did not receive a 200_OK HTTP response: " + response.body())
//                .isEqualTo(HttpURLConnection.HTTP_OK);
//
//        final int newSize = TeamUtils.ResponseParser.getAll(TeamUtils.RequestSender.getAll()).size();
//        assertThat(newSize)
//                .as("Get all response did not return the initial users + new users")
//                .isEqualTo(initialSize + batchOfUsers.size());
//    }
//
//    @Test
//    public void whenGettingUser_givenAValidUserId_thenUserIsReturned_andHasA200Status() {
//        final Collection<User> allUsers = TeamUtils.ResponseParser.getAll(TeamUtils.RequestSender.getAll());
//        int userId = allUsers.size();
//
//        if (allUsers.isEmpty()) {
//            StubbedFoldingEndpointUtils.enableUser(TeamTest.DUMMY_USER);
//            userId = TeamUtils.ResponseParser.create(TeamUtils.RequestSender.create(DUMMY_USER)).getId();
//        }
//
//        final HttpResponse<String> response = TeamUtils.RequestSender.get(userId);
//        assertThat(response.statusCode())
//                .as("Did not receive a 200_OK HTTP response: " + response.body())
//                .isEqualTo(HttpURLConnection.HTTP_OK);
//
//        final User user = TeamUtils.ResponseParser.get(response);
//        assertThat(user)
//                .as("Did not receive the expected user: " + response.body())
//                .extracting("id")
//                .isEqualTo(userId);
//    }
//
//    @Test
//    public void whenUpdatingUser_givenAValidUserId_andAValidPayload_thenUpdatedUserIsReturned_andNoNewUserIsCreated_andHasA200Status() {
//        final Collection<User> allUsers = TeamUtils.ResponseParser.getAll(TeamUtils.RequestSender.getAll());
//        int userId = allUsers.size();
//
//        if (allUsers.isEmpty()) {
//            StubbedFoldingEndpointUtils.enableUser(TeamTest.DUMMY_USER);
//            userId = TeamUtils.ResponseParser.create(TeamUtils.RequestSender.create(DUMMY_USER)).getId();
//        }
//
//        final User updatedUser = User.create(userId, "Dummy_User", "Dummy User", "DummyPasskey", Category.AMD_GPU, 1, "", false);
//        final HttpResponse<String> response = TeamUtils.RequestSender.update(updatedUser);
//        assertThat(response.statusCode())
//                .as("Did not receive a 200_OK HTTP response: " + response.body())
//                .isEqualTo(HttpURLConnection.HTTP_OK);
//
//        final User actual = TeamUtils.ResponseParser.update(response);
//        assertThat(actual)
//                .as("Did not receive created object as JSON response: " + response.body())
//                .isEqualTo(updatedUser);
//
//
//        final Collection<User> allHardwareAfterUpdate = TeamUtils.ResponseParser.getAll(TeamUtils.RequestSender.getAll());
//        assertThat(allHardwareAfterUpdate)
//                .as("Expected no new user instances to be created")
//                .hasSize(allUsers.size());
//    }
//
//    @Test
//    public void whenDeletingUser_givenAValidUserId_thenUserIsDeleted_andHasA200Status_andUserCountIsReduced_andUserCannotBeRetrievedAgain() {
//        final Collection<User> allUsers = TeamUtils.ResponseParser.getAll(TeamUtils.RequestSender.getAll());
//        int userId = allUsers.size();
//
//        if (allUsers.isEmpty()) {
//            StubbedFoldingEndpointUtils.enableUser(TeamTest.DUMMY_USER);
//            userId = TeamUtils.ResponseParser.create(TeamUtils.RequestSender.create(DUMMY_USER)).getId();
//        }
//
//        final HttpResponse<String> response = TeamUtils.RequestSender.delete(userId);
//        assertThat(response.statusCode())
//                .as("Did not receive a 200_OK HTTP response: " + response.body())
//                .isEqualTo(HttpURLConnection.HTTP_OK);
//
//        final HttpResponse<String> getResponse = TeamUtils.RequestSender.get(userId);
//        assertThat(getResponse.statusCode())
//                .as("Was able to retrieve the user instance, despite deleting it")
//                .isEqualTo(HttpURLConnection.HTTP_NOT_FOUND);
//
//        final int newSize = TeamUtils.ResponseParser.getAll(TeamUtils.RequestSender.getAll()).size();
//        assertThat(newSize)
//                .as("Get all response did not return the initial users - deleted user")
//                .isEqualTo(userId - 1);
//    }
//
//    // Negative/alternative test cases
//
//    // TODO: [zodac] Update a user with new hardware, stats will need to be updated, may cause a problem since they won't have any stats to begin with! Do in TcStatsTest
//
//    @Test
//    public void whenCreatingUser_givenAUserWithInvalidHardwareId_thenJsonResponseWithErrorIsReturned_andHasA400Status() {
//        final User user = User.createWithoutId("Invalid_User", "Invalid User", "InvalidPasskey", Category.NVIDIA_GPU, 0, "", false);
//
//        final HttpResponse<String> response = TeamUtils.RequestSender.create(user);
//
//        assertThat(response.statusCode())
//                .as("Did not receive a 400_BAD_REQUEST HTTP response: " + response.body())
//                .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);
//
//        assertThat(response.body())
//                .as("Did not receive expected error message in response")
//                .contains("hardwareId");
//    }
//
//    @Test
//    public void whenCreatingUser_givenUserHasNoUnitsCompleted_thenUserIsNotCreated_andHasA400Stats() {
//        final User user = User.createWithoutId("Invalid_User", "Invalid User", "InvalidPasskey", Category.NVIDIA_GPU, 0, "", false);
//        StubbedFoldingEndpointUtils.disableUser(user);
//
//        final HttpResponse<String> response = TeamUtils.RequestSender.create(user);
//
//        assertThat(response.statusCode())
//                .as("Did not receive a 400_BAD_REQUEST HTTP response: " + response.body())
//                .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);
//    }
//
//    @Test
//    public void whenCreatingUser_givenUserWithTheSameFoldingNameAndPasskeyAlreadyExists_thenA409ResponseIsReturned() {
//        if (TeamUtils.RequestSender.get(DUMMY_USER.getId()).statusCode() == HttpURLConnection.HTTP_NOT_FOUND) {
//            TeamUtils.RequestSender.create(DUMMY_USER);
//        }
//        final HttpResponse<String> response = TeamUtils.RequestSender.create(DUMMY_USER);
//
//        assertThat(response.statusCode())
//                .as("Did not receive a 409_CONFLICT HTTP response: " + response.body())
//                .isEqualTo(HttpURLConnection.HTTP_CONFLICT);
//    }
//
//    @Test
//    public void whenGettingUser_givenANonExistingUserId_thenNoJsonResponseIsReturned_andHasA404Status() {
//        final int invalidId = 99;
//        final HttpResponse<String> response = TeamUtils.RequestSender.get(invalidId);
//
//        assertThat(response.statusCode())
//                .as("Did not receive a 404_NOT_FOUND HTTP response: " + response.body())
//                .isEqualTo(HttpURLConnection.HTTP_NOT_FOUND);
//
//        assertThat(response.body())
//                .as("Did not receive an empty JSON response: " + response.body())
//                .isEmpty();
//    }
//
//    @Test
//    public void whenUpdatingUser_givenANonExistingUserId_thenNoJsonResponseIsReturned_andHasA404Status() {
//        final int invalidId = 99;
//        final User updatedUser = User.create(invalidId, "Invalid_User", "Invalid User", "InvalidPasskey", Category.NVIDIA_GPU, 1, "", false);
//        StubbedFoldingEndpointUtils.enableUser(updatedUser);
//
//        final HttpResponse<String> response = TeamUtils.RequestSender.update(updatedUser);
//        assertThat(response.statusCode())
//                .as("Did not receive a 404_NOT_FOUND HTTP response: " + response.body())
//                .isEqualTo(HttpURLConnection.HTTP_NOT_FOUND);
//
//        assertThat(response.body())
//                .as("Did not receive an empty JSON response: " + response.body())
//                .isEmpty();
//    }
//
//    @Test
//    public void whenDeletingUser_givenANonExistingUserId_thenNoJsonResponseIsReturned_andHasA404Status() {
//        final int invalidId = 99;
//        final HttpResponse<String> response = TeamUtils.RequestSender.delete(invalidId);
//
//        assertThat(response.statusCode())
//                .as("Did not receive a 404_NOT_FOUND HTTP response: " + response.body())
//                .isEqualTo(HttpURLConnection.HTTP_NOT_FOUND);
//
//        assertThat(response.body())
//                .as("Did not receive an empty JSON response: " + response.body())
//                .isEmpty();
//    }
//
//    @Test
//    public void whenUpdatingUser_givenAValidUserId_andPayloadHasNoChanges_thenOriginalUserIsReturned_andHasA200Status() {
//        final User user = User.createWithoutId("Dummy_User6", "Dummy User6", "DummyPasskey6", Category.NVIDIA_GPU, 1, "", false);
//        StubbedFoldingEndpointUtils.enableUser(user);
//
//        final HttpResponse<String> createResponse = TeamUtils.RequestSender.create(user);
//        assertThat(createResponse.statusCode())
//                .as("Did not receive a 201_CREATED HTTP response: " + createResponse.body())
//                .isEqualTo(HttpURLConnection.HTTP_CREATED);
//
//        final int createdUserId = TeamUtils.ResponseParser.create(createResponse).getId();
//        final User userWithId = User.updateWithId(createdUserId, user);
//
//        final HttpResponse<String> updateResponse = TeamUtils.RequestSender.update(userWithId);
//
//        assertThat(updateResponse.statusCode())
//                .as("Did not receive a 200_OK HTTP response: " + updateResponse.body())
//                .isEqualTo(HttpURLConnection.HTTP_OK);
//
//        final User actual = TeamUtils.ResponseParser.update(updateResponse);
//
//        assertThat(actual)
//                .as("Did not receive the original user in response")
//                .isEqualTo(userWithId);
//    }
//
//    @Test
//    public void whenCreatingBatchOfUsers_givenPayloadIsPartiallyValid_thenOnlyValidUsersAreCreated_andResponseHasA200Status() {
//        final int initialUsersSize = TeamUtils.ResponseParser.getAll(TeamUtils.RequestSender.getAll()).size();
//
//        final List<User> batchOfValidUsers = List.of(
//                User.createWithoutId("Dummy_User7", "Dummy User7", "DummyPasskey7", Category.NVIDIA_GPU, 1, "", false),
//                User.createWithoutId("Dummy_User8", "Dummy User8", "DummyPasskey8", Category.NVIDIA_GPU, 1, "", false)
//        );
//        final List<User> batchOfInvalidUsers = List.of(
//                User.createWithoutId("Dummy_User9", "Dummy User9", "DummyPasskey9", Category.NVIDIA_GPU, 0, "", false),
//                User.createWithoutId("Dummy_User10", "Dummy User10", "DummyPasskey10", Category.NVIDIA_GPU, 0, "", false)
//        );
//        final List<User> batchOfUsers = new ArrayList<>(batchOfValidUsers.size() + batchOfInvalidUsers.size());
//        batchOfUsers.addAll(batchOfValidUsers);
//        batchOfUsers.addAll(batchOfInvalidUsers);
//
//        for (final User validUser : batchOfValidUsers) {
//            StubbedFoldingEndpointUtils.enableUser(validUser);
//        }
//
//
//        final HttpResponse<String> response = TeamUtils.RequestSender.createBatchOf(batchOfUsers);
//        assertThat(response.statusCode())
//                .as("Did not receive a 200_OK HTTP response: " + response.body())
//                .isEqualTo(HttpURLConnection.HTTP_OK);
//
//        final int newUsersSize = TeamUtils.ResponseParser.getAll(TeamUtils.RequestSender.getAll()).size();
//        assertThat(newUsersSize)
//                .as("Get all response did not return the initial users + new valid users")
//                .isEqualTo(initialUsersSize + batchOfValidUsers.size());
//    }
//
//    @Test
//    public void whenCreatingBatchOfUsers_givenPayloadIsInvalid_thenResponseHasA400Status() {
//        final int initialUsersSize = TeamUtils.ResponseParser.getAll(TeamUtils.RequestSender.getAll()).size();
//
//        final List<User> batchOfInvalidUsers = List.of(
//                User.createWithoutId("Dummy_User11", "Dummy User11", "DummyPasskey11", Category.NVIDIA_GPU, 0, "", false),
//                User.createWithoutId("Dummy_User12", "Dummy User12", "DummyPasskey12", Category.NVIDIA_GPU, 0, "", false)
//        );
//
//        final HttpResponse<String> response = TeamUtils.RequestSender.createBatchOf(batchOfInvalidUsers);
//        assertThat(response.statusCode())
//                .as("Did not receive a 400_BAD_REQUEST HTTP response: " + response.body())
//                .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);
//
//        final int newHardwareSize = TeamUtils.ResponseParser.getAll(TeamUtils.RequestSender.getAll()).size();
//        assertThat(newHardwareSize)
//                .as("Get all response did not return only the initial users")
//                .isEqualTo(initialUsersSize);
//    }
//
//    @Test
//    public void whenDeletingUser_givenTheUserIsLinkedToTeam_thenResponseHasA409Status() {
//        final User user = User.createWithoutId("Dummy_User13", "Dummy User13", "DummyPasskey13", Category.NVIDIA_GPU, 1, "", false);
//        StubbedFoldingEndpointUtils.enableUser(user);
//
//        final HttpResponse<String> createUserResponse = TeamUtils.RequestSender.create(user);
//        assertThat(createUserResponse.statusCode())
//                .as("Was not able to create user: " + createUserResponse.body())
//                .isEqualTo(HttpURLConnection.HTTP_CREATED);
//
//        final int userId = TeamUtils.ResponseParser.create(createUserResponse).getId();
//
//        final Team team = Team.createWithoutId("DummyTeam", "Dummy team", userId, Set.of(userId), Collections.emptySet());
//        final HttpResponse<String> createTeamResponse = TeamUtils.RequestSender.create(team);
//        assertThat(createUserResponse.statusCode())
//                .as("Was not able to create team: " + createTeamResponse.body())
//                .isEqualTo(HttpURLConnection.HTTP_CREATED);
//
//        final HttpResponse<String> deleteUserResponse = TeamUtils.RequestSender.delete(userId);
//        assertThat(deleteUserResponse.statusCode())
//                .as("Expected to fail due to a 409_CONFLICT: " + deleteUserResponse.body())
//                .isEqualTo(HttpURLConnection.HTTP_CONFLICT);
//    }
//
    @AfterClass
    public static void tearDown() {
        cleanSystemForTests();
    }
//
//    private static void cleanSystemOfTeams() {
//        final Collection<Team> allTeams = TeamUtils.ResponseParser.getAll(TeamUtils.RequestSender.getAll());
//        for (final Team team : allTeams) {
//            TeamUtils.RequestSender.delete(team.getId());
//        }
//
//        DatabaseCleaner.truncateTableAndResetId("teams");
//    }
}

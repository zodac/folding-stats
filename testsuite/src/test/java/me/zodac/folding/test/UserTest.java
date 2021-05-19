package me.zodac.folding.test;

import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.client.java.response.UserResponseParser;
import me.zodac.folding.rest.api.exception.FoldingRestException;
import me.zodac.folding.test.utils.HardwareUtils;
import me.zodac.folding.test.utils.StubbedFoldingEndpointUtils;
import me.zodac.folding.test.utils.TeamUtils;
import me.zodac.folding.test.utils.UserUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.net.HttpURLConnection;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static me.zodac.folding.test.utils.HttpResponseHeaderUtils.getETag;
import static me.zodac.folding.test.utils.HttpResponseHeaderUtils.getXTotalCount;
import static me.zodac.folding.test.utils.SystemCleaner.cleanSystemForSimpleTests;
import static me.zodac.folding.test.utils.TestGenerator.generateHardware;
import static me.zodac.folding.test.utils.TestGenerator.generateUser;
import static me.zodac.folding.test.utils.TestGenerator.generateUserWithHardwareId;
import static me.zodac.folding.test.utils.TestGenerator.generateUserWithUserId;
import static me.zodac.folding.test.utils.UserUtils.USER_REQUEST_SENDER;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the {@link User} REST endpoint at <code>/folding/users</code>.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserTest {

    @BeforeAll
    public static void setUp() throws FoldingRestException {
        cleanSystemForSimpleTests();
    }

    @Test
    @Order(1)
    public void whenGettingAllUsers_givenNoUserHasBeenCreated_thenAnEmptyJsonResponseIsReturned_andHasA200Status() throws FoldingRestException {
        final HttpResponse<String> response = USER_REQUEST_SENDER.getAll();
        assertThat(response.statusCode())
                .as("Did not receive a 200_OK HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_OK);

        final Collection<User> allUsers = UserResponseParser.getAll(response);
        final int xTotalCount = getXTotalCount(response);

        assertThat(xTotalCount)
                .isEqualTo(allUsers.size());

        assertThat(allUsers)
                .isEmpty();
    }

    @Test
    public void whenCreatingUser_givenPayloadIsValid_thenTheCreatedUserIsReturnedInResponse_andHasId_andResponseHasA201StatusCode() throws FoldingRestException {
        final User userToCreate = generateUser();
        StubbedFoldingEndpointUtils.enableUser(userToCreate);

        final HttpResponse<String> response = USER_REQUEST_SENDER.create(userToCreate);
        assertThat(response.statusCode())
                .as("Did not receive a 201_CREATED HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_CREATED);

        final User actual = UserResponseParser.create(response);
        final User expected = User.updateWithId(actual.getId(), userToCreate);
        assertThat(actual)
                .as("Did not receive created object as JSON response: " + response.body())
                .isEqualTo(expected);
    }

    @Test
    public void whenCreatingBatchOfUsers_givenPayloadIsValid_thenTheUsersAreCreated_andResponseHasA200Status() throws FoldingRestException {
        final int initialSize = UserUtils.getNumberOfUsers();

        final List<User> batchOfUsers = List.of(
                generateUser(),
                generateUser(),
                generateUser()
        );

        for (final User user : batchOfUsers) {
            StubbedFoldingEndpointUtils.enableUser(user);
        }

        final HttpResponse<String> response = USER_REQUEST_SENDER.createBatchOf(batchOfUsers);
        assertThat(response.statusCode())
                .as("Did not receive a 200_OK HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_OK);

        final int newSize = UserUtils.getNumberOfUsers();
        assertThat(newSize)
                .as("Get all response did not return the initial users + new users")
                .isEqualTo(initialSize + batchOfUsers.size());
    }

    @Test
    public void whenGettingUser_givenAValidUserId_thenUserIsReturned_andHasA200Status() throws FoldingRestException {
        final int userId = UserUtils.createOrConflict(generateUser()).getId();

        final HttpResponse<String> response = USER_REQUEST_SENDER.get(userId);
        assertThat(response.statusCode())
                .as("Did not receive a 200_OK HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_OK);

        final User user = UserResponseParser.get(response);
        assertThat(user)
                .as("Did not receive the expected user: " + response.body())
                .extracting("id")
                .isEqualTo(userId);
    }

    @Test
    public void whenUpdatingUser_givenAValidUserId_andAValidPayload_thenUpdatedUserIsReturned_andNoNewUserIsCreated_andHasA200Status() throws FoldingRestException {
        final int userId = UserUtils.createOrConflict(generateUser()).getId();
        final int initialSize = UserUtils.getNumberOfUsers();

        final User updatedUser = User.updateWithId(userId, UserUtils.get(userId));
        updatedUser.setPasskey("updatedPasskey");
        StubbedFoldingEndpointUtils.enableUser(updatedUser);

        final HttpResponse<String> response = USER_REQUEST_SENDER.update(updatedUser);
        assertThat(response.statusCode())
                .as("Did not receive a 200_OK HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_OK);

        final User actual = UserResponseParser.update(response);
        assertThat(actual)
                .as("Did not receive created object as JSON response: " + response.body())
                .isEqualTo(updatedUser);


        final int allUsersAfterUpdate = UserUtils.getNumberOfUsers();
        assertThat(allUsersAfterUpdate)
                .as("Expected no new user instances to be created")
                .isEqualTo(initialSize);
    }

    @Test
    public void whenDeletingUser_givenAValidUserId_thenUserIsDeleted_andHasA200Status_andUserCountIsReduced_andUserCannotBeRetrievedAgain() throws FoldingRestException {
        final int userId = UserUtils.createOrConflict(generateUser()).getId();
        final int initialSize = UserUtils.getNumberOfUsers();

        final HttpResponse<Void> response = USER_REQUEST_SENDER.delete(userId);
        assertThat(response.statusCode())
                .as("Did not receive a 200_OK HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_OK);

        final HttpResponse<String> getResponse = USER_REQUEST_SENDER.get(userId);
        assertThat(getResponse.statusCode())
                .as("Was able to retrieve the user instance, despite deleting it")
                .isEqualTo(HttpURLConnection.HTTP_NOT_FOUND);

        final int newSize = UserUtils.getNumberOfUsers();
        assertThat(newSize)
                .as("Get all response did not return the initial users - deleted user")
                .isEqualTo(initialSize - 1);
    }

    @Test
    public void whenPatchingAUserWithPointsOffsets_givenThePayloadIsValid_thenResponseHasA200Status() throws FoldingRestException {
        final Hardware hardware = HardwareUtils.createOrConflict(generateHardware());
        final User user = generateUserWithUserId(hardware.getId());

        final int userId = UserUtils.createOrConflict(user).getId();
        final HttpResponse<Void> patchResponse = USER_REQUEST_SENDER.offset(userId, 100L, Math.round(100L * hardware.getMultiplier()), 10);
        assertThat(patchResponse.statusCode())
                .as("Was not able to patch user: " + patchResponse.body())
                .isEqualTo(HttpURLConnection.HTTP_OK);
    }

    // Negative/alternative test cases

    @Test
    public void whenCreatingUser_givenAUserWithInvalidHardwareId_thenJsonResponseWithErrorIsReturned_andHasA400Status() throws FoldingRestException {
        final int invalidHardwareId = 0;
        final User user = generateUserWithHardwareId(invalidHardwareId);

        final HttpResponse<String> response = USER_REQUEST_SENDER.create(user);

        assertThat(response.statusCode())
                .as("Did not receive a 400_BAD_REQUEST HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);

        assertThat(response.body())
                .as("Did not receive expected error message in response")
                .contains("hardwareId");
    }

    @Test
    public void whenCreatingUser_givenUserHasNoUnitsCompleted_thenUserIsNotCreated_andHasA400Stats() throws FoldingRestException {
        final User user = generateUser();
        StubbedFoldingEndpointUtils.disableUser(user);

        final HttpResponse<String> response = USER_REQUEST_SENDER.create(user);

        assertThat(response.statusCode())
                .as("Did not receive a 400_BAD_REQUEST HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);
    }

    @Test
    public void whenCreatingUser_givenUserWithTheSameFoldingNameAndPasskeyAlreadyExists_thenA409ResponseIsReturned() throws FoldingRestException {
        final User userToCreate = generateUser();
        StubbedFoldingEndpointUtils.enableUser(userToCreate);

        USER_REQUEST_SENDER.create(userToCreate); // Send one request and ignore it (even if the user already exists, we can verify the conflict with the next one)
        final HttpResponse<String> response = USER_REQUEST_SENDER.create(userToCreate);

        assertThat(response.statusCode())
                .as("Did not receive a 409_CONFLICT HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_CONFLICT);
    }

    @Test
    public void whenGettingUser_givenANonExistingUserId_thenNoJsonResponseIsReturned_andHasA404Status() throws FoldingRestException {
        final int invalidId = 99;
        final HttpResponse<String> response = USER_REQUEST_SENDER.get(invalidId);

        assertThat(response.statusCode())
                .as("Did not receive a 404_NOT_FOUND HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_NOT_FOUND);

        assertThat(response.body())
                .as("Did not receive an empty JSON response: " + response.body())
                .isEmpty();
    }

    @Test
    public void whenUpdatingUser_givenANonExistingUserId_thenNoJsonResponseIsReturned_andHasA404Status() throws FoldingRestException {
        final int invalidId = 99;
        final User updatedUser = generateUserWithUserId(invalidId);
        StubbedFoldingEndpointUtils.enableUser(updatedUser);

        final HttpResponse<String> response = USER_REQUEST_SENDER.update(updatedUser);
        assertThat(response.statusCode())
                .as("Did not receive a 404_NOT_FOUND HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_NOT_FOUND);

        assertThat(response.body())
                .as("Did not receive an empty JSON response: " + response.body())
                .isEmpty();
    }

    @Test
    public void whenDeletingUser_givenANonExistingUserId_thenResponseHasA404Status() throws FoldingRestException {
        final int invalidId = 99;
        final HttpResponse<Void> response = USER_REQUEST_SENDER.delete(invalidId);

        assertThat(response.statusCode())
                .as("Did not receive a 404_NOT_FOUND HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_NOT_FOUND);
    }

    @Test
    public void whenUpdatingUser_givenAValidUserId_andPayloadHasNoChanges_thenOriginalUserIsReturned_andHasA200Status() throws FoldingRestException {
        final User user = generateUser();
        final int createdUserId = UserUtils.createOrConflict(user).getId();
        final User userWithId = User.updateWithId(createdUserId, user);

        final HttpResponse<String> updateResponse = USER_REQUEST_SENDER.update(userWithId);

        assertThat(updateResponse.statusCode())
                .as("Did not receive a 200_OK HTTP response: " + updateResponse.body())
                .isEqualTo(HttpURLConnection.HTTP_OK);

        final User actual = UserResponseParser.update(updateResponse);

        assertThat(actual)
                .as("Did not receive the original user in response")
                .isEqualTo(userWithId);
    }

    @Test
    public void whenCreatingBatchOfUsers_givenPayloadIsPartiallyValid_thenOnlyValidUsersAreCreated_andResponseHasA200Status() throws FoldingRestException {
        final int initialUsersSize = UserUtils.getNumberOfUsers();

        final List<User> batchOfValidUsers = List.of(
                generateUser(),
                generateUser()
        );
        final List<User> batchOfInvalidUsers = List.of(
                generateUserWithHardwareId(0),
                generateUserWithHardwareId(0)
        );
        final List<User> batchOfUsers = new ArrayList<>(batchOfValidUsers.size() + batchOfInvalidUsers.size());
        batchOfUsers.addAll(batchOfValidUsers);
        batchOfUsers.addAll(batchOfInvalidUsers);

        for (final User validUser : batchOfValidUsers) {
            StubbedFoldingEndpointUtils.enableUser(validUser);
        }

        final HttpResponse<String> response = USER_REQUEST_SENDER.createBatchOf(batchOfUsers);
        assertThat(response.statusCode())
                .as("Did not receive a 200_OK HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_OK);

        final int newUsersSize = UserUtils.getNumberOfUsers();
        assertThat(newUsersSize)
                .as("Get all response did not return the initial users + new valid users")
                .isEqualTo(initialUsersSize + batchOfValidUsers.size());
    }

    @Test
    public void whenCreatingBatchOfUsers_givenPayloadIsInvalid_thenResponseHasA400Status() throws FoldingRestException {
        final int initialUsersSize = UserUtils.getNumberOfUsers();
        final List<User> batchOfInvalidUsers = List.of(
                generateUserWithHardwareId(0),
                generateUserWithHardwareId(0)
        );

        final HttpResponse<String> response = USER_REQUEST_SENDER.createBatchOf(batchOfInvalidUsers);
        assertThat(response.statusCode())
                .as("Did not receive a 400_BAD_REQUEST HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);

        final int newUsersSize = UserUtils.getNumberOfUsers();
        assertThat(newUsersSize)
                .as("Get all response did not return only the initial users")
                .isEqualTo(initialUsersSize);
    }

    @Test
    public void whenDeletingUser_givenTheUserIsLinkedToTeam_thenResponseHasA409Status() throws FoldingRestException {
        final User user = generateUser();
        final int userId = UserUtils.createOrConflict(user).getId();

        final Team team = Team.createWithoutId("DummyTeam", "Dummy team", userId, Set.of(userId), Collections.emptySet());
        TeamUtils.createOrConflict(team);

        final HttpResponse<Void> deleteUserResponse = USER_REQUEST_SENDER.delete(userId);
        assertThat(deleteUserResponse.statusCode())
                .as("Expected to fail due to a 409_CONFLICT: " + deleteUserResponse.body())
                .isEqualTo(HttpURLConnection.HTTP_CONFLICT);
    }

    @Test
    public void whenGettingUserById_givenRequestUsesPreviousETag_andUserHasNotChanged_thenResponseHasA304Status_andNoBody() throws FoldingRestException {
        final int userId = UserUtils.createOrConflict(generateUser()).getId();

        final HttpResponse<String> response = USER_REQUEST_SENDER.get(userId);
        assertThat(response.statusCode())
                .as("Expected first request to have a 200_OK HTTP response")
                .isEqualTo(HttpURLConnection.HTTP_OK);

        final String eTag = getETag(response);

        final HttpResponse<String> cachedResponse = USER_REQUEST_SENDER.get(userId, eTag);
        assertThat(cachedResponse.statusCode())
                .as("Expected second request to have a 304_NOT_MODIFIED HTTP response")
                .isEqualTo(HttpURLConnection.HTTP_NOT_MODIFIED);

        assertThat(UserResponseParser.get(cachedResponse))
                .as("Expected cached response to have the same content as the non-cached response")
                .isNull();
    }

    @Test
    public void whenGettingAllUsers_givenRequestUsesPreviousETag_andUsersHaveNotChanged_thenResponseHasA304Status_andNoBody() throws FoldingRestException {
        UserUtils.createOrConflict(generateUser());

        final HttpResponse<String> response = USER_REQUEST_SENDER.getAll();
        assertThat(response.statusCode())
                .as("Expected first GET request to have a 200_OK HTTP response")
                .isEqualTo(HttpURLConnection.HTTP_OK);

        final String eTag = getETag(response);

        final HttpResponse<String> cachedResponse = USER_REQUEST_SENDER.getAll(eTag);
        assertThat(cachedResponse.statusCode())
                .as("Expected second request to have a 304_NOT_MODIFIED HTTP response")
                .isEqualTo(HttpURLConnection.HTTP_NOT_MODIFIED);

        assertThat(UserResponseParser.getAll(cachedResponse))
                .as("Expected cached response to have the same content as the non-cached response")
                .isNull();
    }

    @Test
    public void whenPatchingAUserWithPointsOffsets_AndUserDoesNotExist_thenResponseHasA404Status() throws FoldingRestException {
        final int invalidId = 99;
        final HttpResponse<Void> patchResponse = USER_REQUEST_SENDER.offset(invalidId, 100L, 1_000L, 10);
        assertThat(patchResponse.statusCode())
                .as("Was able to patch user, was expected user to not be found: " + patchResponse.body())
                .isEqualTo(HttpURLConnection.HTTP_NOT_FOUND);
    }

    @AfterAll
    public static void tearDown() throws FoldingRestException {
        cleanSystemForSimpleTests();
    }
}

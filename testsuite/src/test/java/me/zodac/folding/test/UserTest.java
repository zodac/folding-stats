package me.zodac.folding.test;

import static me.zodac.folding.api.utils.EncodingUtils.encodeBasicAuthentication;
import static me.zodac.folding.test.utils.SystemCleaner.cleanSystemForSimpleTests;
import static me.zodac.folding.test.utils.TestAuthenticationData.ADMIN_USER;
import static me.zodac.folding.test.utils.TestAuthenticationData.INVALID_PASSWORD;
import static me.zodac.folding.test.utils.TestAuthenticationData.INVALID_USERNAME;
import static me.zodac.folding.test.utils.TestAuthenticationData.READ_ONLY_USER;
import static me.zodac.folding.test.utils.TestConstants.FOLDING_URL;
import static me.zodac.folding.test.utils.TestConstants.GSON;
import static me.zodac.folding.test.utils.TestConstants.HTTP_CLIENT;
import static me.zodac.folding.test.utils.TestGenerator.generateTeam;
import static me.zodac.folding.test.utils.TestGenerator.generateUser;
import static me.zodac.folding.test.utils.TestGenerator.generateUserWithCategory;
import static me.zodac.folding.test.utils.TestGenerator.generateUserWithHardwareId;
import static me.zodac.folding.test.utils.TestGenerator.generateUserWithLiveStatsLink;
import static me.zodac.folding.test.utils.TestGenerator.generateUserWithTeamId;
import static me.zodac.folding.test.utils.rest.request.HardwareUtils.HARDWARE_REQUEST_SENDER;
import static me.zodac.folding.test.utils.rest.request.UserUtils.USER_REQUEST_SENDER;
import static me.zodac.folding.test.utils.rest.request.UserUtils.create;
import static me.zodac.folding.test.utils.rest.response.HttpResponseHeaderUtils.getEntityTag;
import static me.zodac.folding.test.utils.rest.response.HttpResponseHeaderUtils.getTotalCount;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;
import me.zodac.folding.api.tc.Category;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.client.java.response.UserResponseParser;
import me.zodac.folding.rest.api.exception.FoldingRestException;
import me.zodac.folding.rest.api.header.ContentType;
import me.zodac.folding.rest.api.header.RestHeader;
import me.zodac.folding.rest.api.tc.request.HardwareRequest;
import me.zodac.folding.rest.api.tc.request.UserRequest;
import me.zodac.folding.test.utils.TestConstants;
import me.zodac.folding.test.utils.rest.request.StubbedFoldingEndpointUtils;
import me.zodac.folding.test.utils.rest.request.TeamUtils;
import me.zodac.folding.test.utils.rest.request.UserUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * Tests for the {@link User} REST endpoint at <code>/folding/users</code>.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserTest {

    @BeforeAll
    static void setUp() throws FoldingRestException {
        cleanSystemForSimpleTests();
    }

    @Test
    @Order(1)
    void whenGettingAllUsers_givenNoUserHasBeenCreated_thenAnEmptyJsonResponseIsReturned_andHas200Status() throws FoldingRestException {
        final HttpResponse<String> response = USER_REQUEST_SENDER.getAll();
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final Collection<User> allUsers = UserResponseParser.getAll(response);
        final int xTotalCount = getTotalCount(response);

        assertThat(xTotalCount)
            .isEqualTo(allUsers.size());

        assertThat(allUsers)
            .isEmpty();
    }

    @Test
    void whenCreatingUser_givenPayloadIsValid_thenTheCreatedUserIsReturnedInResponse_andHasId_andResponseHas201StatusCode()
        throws FoldingRestException {
        final UserRequest userToCreate = generateUser();
        StubbedFoldingEndpointUtils.enableUser(userToCreate);

        final HttpResponse<String> response = USER_REQUEST_SENDER.create(userToCreate, ADMIN_USER.userName(), ADMIN_USER.password());
        assertThat(response.statusCode())
            .as("Did not receive a 201_CREATED HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_CREATED);

        final User actual = UserResponseParser.create(response);
        assertThat(actual)
            .as("Did not receive created object as JSON response: " + response.body())
            .extracting("foldingUserName", "displayName", "passkey", "category", "profileLink", "liveStatsLink", "userIsCaptain")
            .containsExactly(userToCreate.getFoldingUserName(), userToCreate.getDisplayName(), userToCreate.getPasskey(),
                Category.get(userToCreate.getCategory()), userToCreate.getProfileLink(), userToCreate.getLiveStatsLink(),
                userToCreate.isUserIsCaptain());
    }

    @Test
    void whenCreatingBatchOfUsers_givenPayloadIsValid_thenTheUsersAreCreated_andResponseHas200Status() throws FoldingRestException {
        final int initialSize = UserUtils.getNumberOfUsers();

        final List<UserRequest> batchOfUsers = List.of(
            generateUser(),
            generateUser(),
            generateUser()
        );

        for (final UserRequest user : batchOfUsers) {
            StubbedFoldingEndpointUtils.enableUser(user);
        }

        final HttpResponse<String> response = USER_REQUEST_SENDER.createBatchOf(batchOfUsers, ADMIN_USER.userName(), ADMIN_USER.password());
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final int newSize = UserUtils.getNumberOfUsers();
        assertThat(newSize)
            .as("Get all response did not return the initial users + new users")
            .isEqualTo(initialSize + batchOfUsers.size());
    }

    @Test
    void whenGettingUser_givenValidUserId_thenUserIsReturned_andPasskeyIsMasked_andHas200Status() throws FoldingRestException {
        final int userId = create(generateUser()).getId();

        final HttpResponse<String> response = USER_REQUEST_SENDER.get(userId);
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final User user = UserResponseParser.get(response);
        assertThat(user.getId())
            .as("Did not receive the expected user: " + response.body())
            .isEqualTo(userId);

        final int lengthOfUnmaskedPasskey = 8;
        final String firstPartOfPasskey = user.getPasskey().substring(0, lengthOfUnmaskedPasskey);
        final String secondPartOfPasskey = user.getPasskey().substring(lengthOfUnmaskedPasskey);

        assertThat(firstPartOfPasskey)
            .as("Expected the first 8 characters of the passkey to be shown")
            .doesNotContain("*");

        assertThat(secondPartOfPasskey)
            .as("Expected the remaining 24 characters of the passkey to be masked")
            .doesNotContainPattern(Pattern.compile("[a-zA-Z]"));
    }

    @Test
    void whenUpdatingUser_givenValidUserId_andValidPayload_thenUpdatedUserIsReturned_andNoNewUserIsCreated_andHas200Status()
        throws FoldingRestException {
        final User createdUser = create(generateUser());
        final int initialSize = UserUtils.getNumberOfUsers();

        final String updatedPasskey = "updatedPasskey123456789012345678";
        final UserRequest userToUpdate = UserRequest.builder()
            .foldingUserName(createdUser.getFoldingUserName())
            .displayName(createdUser.getDisplayName())
            .passkey(updatedPasskey)
            .category(createdUser.getCategory().toString())
            .profileLink(createdUser.getProfileLink())
            .liveStatsLink(createdUser.getLiveStatsLink())
            .hardwareId(createdUser.getHardware().getId())
            .teamId(createdUser.getTeam().getId())
            .userIsCaptain(createdUser.isUserIsCaptain())
            .build();
        StubbedFoldingEndpointUtils.enableUser(userToUpdate);

        final HttpResponse<String> response =
            USER_REQUEST_SENDER.update(createdUser.getId(), userToUpdate, ADMIN_USER.userName(), ADMIN_USER.password());
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final User actual = UserResponseParser.update(response);
        assertThat(actual)
            .as("Did not receive created object as JSON response: " + response.body())
            .extracting("id", "foldingUserName", "displayName", "passkey", "category", "profileLink", "liveStatsLink", "userIsCaptain")
            .containsExactly(createdUser.getId(), createdUser.getFoldingUserName(), createdUser.getDisplayName(), updatedPasskey,
                createdUser.getCategory(), createdUser.getProfileLink(), createdUser.getLiveStatsLink(), createdUser.isUserIsCaptain());

        final int allUsersAfterUpdate = UserUtils.getNumberOfUsers();
        assertThat(allUsersAfterUpdate)
            .as("Expected no new user instances to be created")
            .isEqualTo(initialSize);
    }

    @Test
    void whenDeletingUser_givenValidUserId_thenUserIsDeleted_andHas200Status_andUserCountIsReduced_andUserCannotBeRetrievedAgain()
        throws FoldingRestException {
        final int userId = create(generateUser()).getId();
        final int initialSize = UserUtils.getNumberOfUsers();

        final HttpResponse<Void> response = USER_REQUEST_SENDER.delete(userId, ADMIN_USER.userName(), ADMIN_USER.password());
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

    // Negative/alternative test cases

    @Test
    void whenCreatingUser_givenUserWithInvalidHardwareId_thenJsonResponseWithErrorIsReturned_andHas400Status() throws FoldingRestException {
        final int invalidHardwareId = 0;
        final UserRequest user = generateUserWithHardwareId(invalidHardwareId);

        final HttpResponse<String> response = USER_REQUEST_SENDER.create(user, ADMIN_USER.userName(), ADMIN_USER.password());

        assertThat(response.statusCode())
            .as("Did not receive a 400_BAD_REQUEST HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);

        assertThat(response.body())
            .as("Did not receive expected error message in response")
            .contains("hardwareId");
    }

    @Test
    void whenCreatingUser_givenUserHasNoUnitsCompleted_thenUserIsNotCreated_andHas400Stats() throws FoldingRestException {
        final UserRequest user = generateUser();
        StubbedFoldingEndpointUtils.disableUser(user);

        final HttpResponse<String> response = USER_REQUEST_SENDER.create(user, ADMIN_USER.userName(), ADMIN_USER.password());

        assertThat(response.statusCode())
            .as("Did not receive a 400_BAD_REQUEST HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);
    }

    @Test
    void whenCreatingUser_givenUserWithTheSameFoldingNameAndPasskeyAlreadyExists_thenA409ResponseIsReturned() throws FoldingRestException {
        final UserRequest userToCreate = generateUser();
        StubbedFoldingEndpointUtils.enableUser(userToCreate);

        USER_REQUEST_SENDER.create(userToCreate, ADMIN_USER.userName(),
            ADMIN_USER.password()); // Send one request and ignore it (even if the user already exists, we can verify the conflict with the next one)
        final HttpResponse<String> response = USER_REQUEST_SENDER.create(userToCreate, ADMIN_USER.userName(), ADMIN_USER.password());

        assertThat(response.statusCode())
            .as("Did not receive a 409_CONFLICT HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_CONFLICT);
    }

    @Test
    void whenGettingUser_givenNonExistingUserId_thenResponseHas404Status() throws FoldingRestException {
        final HttpResponse<String> response = USER_REQUEST_SENDER.get(TestConstants.NON_EXISTING_ID);

        assertThat(response.statusCode())
            .as("Did not receive a 404_NOT_FOUND HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_NOT_FOUND);

        assertThat(response.body())
            .as("Did not receive an empty JSON response: " + response.body())
            .isEmpty();
    }

    @Test
    void whenGettingUser_givenAnOutOfRangeUserId_thenResponseHas400Status() throws FoldingRestException {
        final HttpResponse<String> response = USER_REQUEST_SENDER.get(TestConstants.OUT_OF_RANGE_ID);

        assertThat(response.statusCode())
            .as("Did not receive a 400_BAD_REQUEST HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);

        assertThat(response.body())
            .as("Did not receive valid error message: " + response.body())
            .contains("out of range");
    }

    @Test
    void whenGettingUser_givenInvalidUserId_thenResponseHas400Status() throws IOException, InterruptedException {
        final HttpRequest request = HttpRequest.newBuilder()
            .GET()
            .uri(URI.create(FOLDING_URL + "/users/" + TestConstants.INVALID_FORMAT_ID))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentType())
            .build();

        final HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode())
            .as("Did not receive a 400_BAD_REQUEST HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);

        assertThat(response.body())
            .as("Did not receive valid error message: " + response.body())
            .contains("not a valid format");
    }

    @Test
    void whenUpdatingUser_givenNonExistingUserId_thenResponseHas404Status() throws FoldingRestException {
        final UserRequest updatedUser = generateUser();
        StubbedFoldingEndpointUtils.enableUser(updatedUser);

        final HttpResponse<String> response =
            USER_REQUEST_SENDER.update(TestConstants.NON_EXISTING_ID, updatedUser, ADMIN_USER.userName(), ADMIN_USER.password());
        assertThat(response.statusCode())
            .as("Did not receive a 404_NOT_FOUND HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_NOT_FOUND);
    }

    @Test
    void whenUpdatingUser_givenOutOfRangeUserId_thenResponseHas400Status() throws FoldingRestException {
        final UserRequest updatedUser = generateUser();
        final HttpResponse<String> response =
            USER_REQUEST_SENDER.update(TestConstants.OUT_OF_RANGE_ID, updatedUser, ADMIN_USER.userName(), ADMIN_USER.password());
        assertThat(response.statusCode())
            .as("Did not receive a 400_BAD_REQUEST HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);

        assertThat(response.body())
            .as("Did not receive valid error message: " + response.body())
            .contains("out of range");
    }

    @Test
    void whenUpdatingUser_givenInvalidUserId_thenNoJsonResponseIsReturned_andHas400Status()
        throws IOException, InterruptedException, FoldingRestException {
        final User createdUser = create(generateUser());

        final String updatedPasskey = "updatedPasskey123456789012345678";
        final UserRequest userToUpdate = UserRequest.builder()
            .foldingUserName(createdUser.getFoldingUserName())
            .displayName(createdUser.getDisplayName())
            .passkey(updatedPasskey)
            .category(createdUser.getCategory().toString())
            .profileLink(createdUser.getProfileLink())
            .liveStatsLink(createdUser.getLiveStatsLink())
            .hardwareId(createdUser.getHardware().getId())
            .teamId(createdUser.getTeam().getId())
            .userIsCaptain(createdUser.isUserIsCaptain())
            .build();
        StubbedFoldingEndpointUtils.enableUser(userToUpdate);

        final HttpRequest request = HttpRequest.newBuilder()
            .PUT(HttpRequest.BodyPublishers.ofString(GSON.toJson(userToUpdate)))
            .uri(URI.create(FOLDING_URL + "/users/" + TestConstants.INVALID_FORMAT_ID))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentType())
            .header(RestHeader.AUTHORIZATION.headerName(), encodeBasicAuthentication(ADMIN_USER.userName(), ADMIN_USER.password()))
            .build();

        final HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode())
            .as("Did not receive a 400_BAD_REQUEST HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);

        assertThat(response.body())
            .as("Did not receive valid error message: " + response.body())
            .contains("not a valid format");
    }

    @Test
    void whenDeletingUser_givenNonExistingUserId_thenResponseHas404Status() throws FoldingRestException {
        final HttpResponse<Void> response = USER_REQUEST_SENDER.delete(TestConstants.NON_EXISTING_ID, ADMIN_USER.userName(), ADMIN_USER.password());

        assertThat(response.statusCode())
            .as("Did not receive a 404_NOT_FOUND HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_NOT_FOUND);
    }

    @Test
    void whenDeletingUser_givenOutOfRangeUserId_thenResponseHas400Status() throws FoldingRestException {
        final HttpResponse<Void> response = USER_REQUEST_SENDER.delete(TestConstants.OUT_OF_RANGE_ID, ADMIN_USER.userName(), ADMIN_USER.password());

        assertThat(response.statusCode())
            .as("Did not receive a 400_BAD_REQUEST HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);
    }

    @Test
    void whenDeletingUser_givenInvalidUserId_thenResponseHas400Status() throws IOException, InterruptedException {
        final HttpRequest request = HttpRequest.newBuilder()
            .DELETE()
            .uri(URI.create(FOLDING_URL + "/users/" + TestConstants.INVALID_FORMAT_ID))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentType())
            .header(RestHeader.AUTHORIZATION.headerName(), encodeBasicAuthentication(ADMIN_USER.userName(), ADMIN_USER.password()))
            .build();

        final HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode())
            .as("Did not receive a 400_BAD_REQUEST HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);

        assertThat(response.body())
            .as("Did not receive valid error message: " + response.body())
            .contains("not a valid format");
    }

    @Test
    void whenUpdatingUser_givenValidUserId_andPayloadHasNoChanges_thenOriginalUserIsReturned_andHas200Status() throws FoldingRestException {
        final User createdUser = create(generateUser());
        final UserRequest userToUpdate = UserRequest.builder()
            .foldingUserName(createdUser.getFoldingUserName())
            .displayName(createdUser.getDisplayName())
            .passkey(createdUser.getPasskey())
            .category(createdUser.getCategory().toString())
            .profileLink(createdUser.getProfileLink())
            .liveStatsLink(createdUser.getLiveStatsLink())
            .hardwareId(createdUser.getHardware().getId())
            .teamId(createdUser.getTeam().getId())
            .userIsCaptain(createdUser.isUserIsCaptain())
            .build();

        final HttpResponse<String> updateResponse =
            USER_REQUEST_SENDER.update(createdUser.getId(), userToUpdate, ADMIN_USER.userName(), ADMIN_USER.password());

        assertThat(updateResponse.statusCode())
            .as("Did not receive a 200_OK HTTP response: " + updateResponse.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final User actual = UserResponseParser.update(updateResponse);

        assertThat(actual)
            .extracting("id", "foldingUserName", "displayName", "passkey", "category", "profileLink", "liveStatsLink", "userIsCaptain")
            .containsExactly(createdUser.getId(), createdUser.getFoldingUserName(), createdUser.getDisplayName(), createdUser.getPasskey(),
                createdUser.getCategory(), createdUser.getProfileLink(), createdUser.getLiveStatsLink(), createdUser.isUserIsCaptain());
    }

    @Test
    void whenCreatingBatchOfUsers_givenPayloadIsPartiallyValid_thenOnlyValidUsersAreCreated_andResponseHas200Status() throws FoldingRestException {
        final int initialUsersSize = UserUtils.getNumberOfUsers();

        final List<UserRequest> batchOfValidUsers = List.of(
            generateUser(),
            generateUser()
        );
        final List<UserRequest> batchOfInvalidUsers = List.of(
            generateUserWithHardwareId(0),
            generateUserWithHardwareId(0)
        );
        final List<UserRequest> batchOfUsers = new ArrayList<>(batchOfValidUsers.size() + batchOfInvalidUsers.size());
        batchOfUsers.addAll(batchOfValidUsers);
        batchOfUsers.addAll(batchOfInvalidUsers);

        for (final UserRequest validUser : batchOfValidUsers) {
            StubbedFoldingEndpointUtils.enableUser(validUser);
        }

        final HttpResponse<String> response = USER_REQUEST_SENDER.createBatchOf(batchOfUsers, ADMIN_USER.userName(), ADMIN_USER.password());
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final int newUsersSize = UserUtils.getNumberOfUsers();
        assertThat(newUsersSize)
            .as("Get all response did not return the initial users + new valid users")
            .isEqualTo(initialUsersSize + batchOfValidUsers.size());
    }

    @Test
    void whenCreatingBatchOfUsers_givenPayloadIsInvalid_thenResponseHas400Status() throws FoldingRestException {
        final int initialUsersSize = UserUtils.getNumberOfUsers();
        final List<UserRequest> batchOfInvalidUsers = List.of(
            generateUserWithHardwareId(0),
            generateUserWithHardwareId(0)
        );

        final HttpResponse<String> response = USER_REQUEST_SENDER.createBatchOf(batchOfInvalidUsers, ADMIN_USER.userName(), ADMIN_USER.password());
        assertThat(response.statusCode())
            .as("Did not receive a 400_BAD_REQUEST HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);

        final int newUsersSize = UserUtils.getNumberOfUsers();
        assertThat(newUsersSize)
            .as("Get all response did not return only the initial users")
            .isEqualTo(initialUsersSize);
    }

    @Test
    void whenGettingUserById_givenRequestUsesPreviousEntityTag_andUserHasNotChanged_thenResponseHas304Status_andNoBody() throws FoldingRestException {
        final int userId = create(generateUser()).getId();

        final HttpResponse<String> response = USER_REQUEST_SENDER.get(userId);
        assertThat(response.statusCode())
            .as("Expected first request to have a 200_OK HTTP response")
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final String eTag = getEntityTag(response);

        final HttpResponse<String> cachedResponse = USER_REQUEST_SENDER.get(userId, eTag);
        assertThat(cachedResponse.statusCode())
            .as("Expected second request to have a 304_NOT_MODIFIED HTTP response")
            .isEqualTo(HttpURLConnection.HTTP_NOT_MODIFIED);

        assertThat(UserResponseParser.get(cachedResponse))
            .as("Expected cached response to have the same content as the non-cached response")
            .isNull();
    }

    @Test
    void whenGettingAllUsers_givenRequestUsesPreviousEntityTag_andUsersHaveNotChanged_thenResponseHas304Status_andNoBody()
        throws FoldingRestException {
        create(generateUser());

        final HttpResponse<String> response = USER_REQUEST_SENDER.getAll();
        assertThat(response.statusCode())
            .as("Expected first GET request to have a 200_OK HTTP response")
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final String eTag = getEntityTag(response);

        final HttpResponse<String> cachedResponse = USER_REQUEST_SENDER.getAll(eTag);
        assertThat(cachedResponse.statusCode())
            .as("Expected second request to have a 304_NOT_MODIFIED HTTP response")
            .isEqualTo(HttpURLConnection.HTTP_NOT_MODIFIED);

        assertThat(UserResponseParser.getAll(cachedResponse))
            .as("Expected cached response to have the same content as the non-cached response")
            .isNull();
    }

    @Test
    void whenCreatingUser_givenNoAuthentication_thenRequestFails_andResponseHas401StatusCode() throws FoldingRestException {
        final UserRequest userToCreate = generateUser();
        StubbedFoldingEndpointUtils.enableUser(userToCreate);

        final HttpResponse<String> response = USER_REQUEST_SENDER.create(userToCreate);
        assertThat(response.statusCode())
            .as("Did not receive a 401_UNAUTHORIZED HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_UNAUTHORIZED);
    }

    @Test
    void whenCreatingBatchOfUsers_givenNoAuthentication_thenRequestFails_andResponseHas401StatusCode() throws FoldingRestException {
        final List<UserRequest> batchOfUsers = List.of(
            generateUser(),
            generateUser(),
            generateUser()
        );

        for (final UserRequest user : batchOfUsers) {
            StubbedFoldingEndpointUtils.enableUser(user);
        }

        final HttpResponse<String> response = USER_REQUEST_SENDER.createBatchOf(batchOfUsers);
        assertThat(response.statusCode())
            .as("Did not receive a 401_UNAUTHORIZED HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_UNAUTHORIZED);
    }

    @Test
    void whenUpdatingUser_givenNoAuthentication_thenRequestFails_andResponseHas401StatusCode() throws FoldingRestException {
        final User createdUser = create(generateUser());

        final UserRequest userToUpdate = UserRequest.builder()
            .foldingUserName(createdUser.getFoldingUserName())
            .displayName(createdUser.getDisplayName())
            .passkey("updatedPasskey123456789012345678")
            .category(createdUser.getCategory().toString())
            .profileLink(createdUser.getProfileLink())
            .liveStatsLink(createdUser.getLiveStatsLink())
            .hardwareId(createdUser.getHardware().getId())
            .teamId(createdUser.getTeam().getId())
            .userIsCaptain(createdUser.isUserIsCaptain())
            .build();
        StubbedFoldingEndpointUtils.enableUser(userToUpdate);

        final HttpResponse<String> response = USER_REQUEST_SENDER.update(createdUser.getId(), userToUpdate);
        assertThat(response.statusCode())
            .as("Did not receive a 401_UNAUTHORIZED HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_UNAUTHORIZED);
    }

    @Test
    void whenDeletingUser_givenNoAuthentication_thenRequestFails_andResponseHas401StatusCode() throws FoldingRestException {
        final int userId = create(generateUser()).getId();

        final HttpResponse<Void> response = USER_REQUEST_SENDER.delete(userId);
        assertThat(response.statusCode())
            .as("Did not receive a 401_UNAUTHORIZED HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_UNAUTHORIZED);
    }

    @Test
    void whenCreatingUser_givenAuthentication_andAuthenticationHasInvalidUser_thenRequestFails_andResponseHas401StatusCode()
        throws FoldingRestException {
        final UserRequest userToCreate = generateUser();
        StubbedFoldingEndpointUtils.enableUser(userToCreate);

        final HttpResponse<String> response = USER_REQUEST_SENDER.create(userToCreate, INVALID_USERNAME.userName(), INVALID_USERNAME.password());
        assertThat(response.statusCode())
            .as("Did not receive a 401_UNAUTHORIZED HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_UNAUTHORIZED);
    }

    @Test
    void whenCreatingUser_givenAuthentication_andAuthenticationHasInvalidPassword_thenRequestFails_andResponseHas401StatusCode()
        throws FoldingRestException {
        final UserRequest userToCreate = generateUser();
        StubbedFoldingEndpointUtils.enableUser(userToCreate);

        final HttpResponse<String> response = USER_REQUEST_SENDER.create(userToCreate, INVALID_PASSWORD.userName(), INVALID_PASSWORD.password());
        assertThat(response.statusCode())
            .as("Did not receive a 401_UNAUTHORIZED HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_UNAUTHORIZED);
    }

    @Test
    void whenCreatingUser_givenAuthentication_andUserDoesNotHaveAdminRole_thenRequestFails_andResponseHas403StatusCode()
        throws FoldingRestException {
        final UserRequest userToCreate = generateUser();
        StubbedFoldingEndpointUtils.enableUser(userToCreate);

        final HttpResponse<String> response = USER_REQUEST_SENDER.create(userToCreate, READ_ONLY_USER.userName(), READ_ONLY_USER.password());
        assertThat(response.statusCode())
            .as("Did not receive a 403_FORBIDDEN HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_FORBIDDEN);
    }

    @Test
    void whenCreatingUser_givenEmptyPayload_thenRequestFails_andResponseHas400StatusCode() throws IOException, InterruptedException {
        final HttpRequest request = HttpRequest.newBuilder()
            .POST(HttpRequest.BodyPublishers.noBody())
            .uri(URI.create(FOLDING_URL + "/users"))
            .header("Content-Type", "application/json")
            .header("Authorization", encodeBasicAuthentication(ADMIN_USER.userName(), ADMIN_USER.password()))
            .build();

        final HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode())
            .as("Did not receive a 400_BAD_REQUEST HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);
    }

    @Test
    void whenUpdatingUser_givenEmptyPayload_thenRequestFails_andResponseHas400StatusCode()
        throws FoldingRestException, IOException, InterruptedException {
        final User createdUser = create(generateUser());

        final HttpRequest request = HttpRequest.newBuilder()
            .PUT(HttpRequest.BodyPublishers.noBody())
            .uri(URI.create(FOLDING_URL + "/users/" + createdUser.getId()))
            .header("Content-Type", "application/json")
            .header("Authorization", encodeBasicAuthentication(ADMIN_USER.userName(), ADMIN_USER.password()))
            .build();

        final HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode())
            .as("Did not receive a 400_BAD_REQUEST HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);
    }

    @Test
    void whenCreatingUser_andOptionalFieldIsEmptyString_thenValueShouldBeNullNotEmpty() throws FoldingRestException {
        final UserRequest user = generateUser();
        StubbedFoldingEndpointUtils.enableUser(user);
        final int userId = create(user).getId();

        final User actual = UserUtils.get(userId);
        assertThat(actual)
            .as("Empty optional value should not be returned: " + actual)
            .extracting("liveStatsLink")
            .isEqualTo(null);
    }

    @Test
    void whenUpdatingUser_andOptionalFieldIsEmptyString_thenValueShouldBeNullNotEmpty() throws FoldingRestException {
        final User createdUser = create(generateUserWithLiveStatsLink("http://google.com"));

        final UserRequest userToUpdate = UserRequest.builder()
            .foldingUserName(createdUser.getFoldingUserName())
            .displayName(createdUser.getDisplayName())
            .passkey(createdUser.getPasskey())
            .category(createdUser.getCategory().toString())
            .profileLink(createdUser.getProfileLink())
            .liveStatsLink("")
            .hardwareId(createdUser.getHardware().getId())
            .teamId(createdUser.getTeam().getId())
            .userIsCaptain(createdUser.isUserIsCaptain())
            .build();

        final HttpResponse<String> response =
            USER_REQUEST_SENDER.update(createdUser.getId(), userToUpdate, ADMIN_USER.userName(), ADMIN_USER.password());
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final User actual = UserUtils.get(createdUser.getId());
        assertThat(actual)
            .as("Empty optional value should not be returned: " + response.body())
            .extracting("liveStatsLink")
            .isEqualTo(null);
    }

    // TODO: [zodac] Category and captain checks can be done in UserValidatorTest class?
    @Test
    void whenCreatingUser_givenUsersExceedingPermittedAmountForCategory_thenUserIsNotCreated_andResponseHas400Status() throws FoldingRestException {
        final Team team = TeamUtils.create(generateTeam());
        final UserRequest firstUser = generateUserWithTeamId(team.getId());
        firstUser.setCategory(Category.NVIDIA_GPU.toString());
        create(firstUser);

        final UserRequest secondUser = generateUserWithTeamId(team.getId());
        secondUser.setCategory(Category.NVIDIA_GPU.toString());
        StubbedFoldingEndpointUtils.enableUser(secondUser);

        final HttpResponse<String> response = USER_REQUEST_SENDER.create(secondUser, ADMIN_USER.userName(), ADMIN_USER.password());
        assertThat(response.statusCode())
            .as("Did not receive a 400_BAD_REQUEST HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);

        assertThat(response.body())
            .as("Did not receive an error message specifying too many users for a specific category")
            .contains("category '" + Category.NVIDIA_GPU + "'");
    }

    @Test
    void whenCreatingUser_givenUsersExceedingTotalPermittedAmountForTeam_thenUserIsNotCreated_andResponseHas400Status()
        throws FoldingRestException {
        final Team team = TeamUtils.create(generateTeam());
        final UserRequest firstUser = generateUserWithCategory(Category.NVIDIA_GPU);
        firstUser.setTeamId(team.getId());
        create(firstUser);
        final UserRequest secondUser = generateUserWithCategory(Category.WILDCARD);
        secondUser.setTeamId(team.getId());
        create(secondUser);
        final UserRequest thirdUser = generateUserWithCategory(Category.AMD_GPU);
        thirdUser.setTeamId(team.getId());
        create(thirdUser);

        final UserRequest fourthUser = generateUserWithCategory(Category.NVIDIA_GPU);
        fourthUser.setTeamId(team.getId());

        final HttpResponse<String> response = USER_REQUEST_SENDER.create(fourthUser, ADMIN_USER.userName(), ADMIN_USER.password());
        assertThat(response.statusCode())
            .as("Did not receive a 400_BAD_REQUEST HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);

        assertThat(response.body())
            .as("Did not receive an error message specifying the team is too large")
            .contains("maximum permitted");
    }

    @Test
    void whenCreatingUser_andUserIsCaptain_givenTeamAlreadyHasCaptain_thenUserIsNotCreated_andResponseHas400Status() throws FoldingRestException {
        final UserRequest firstCaptain = generateUserWithCategory(Category.NVIDIA_GPU);
        firstCaptain.setUserIsCaptain(true);
        create(firstCaptain);

        final UserRequest secondCaptain = generateUserWithCategory(Category.AMD_GPU);
        secondCaptain.setTeamId(firstCaptain.getTeamId());
        secondCaptain.setUserIsCaptain(true);
        StubbedFoldingEndpointUtils.enableUser(secondCaptain);

        final HttpResponse<String> response = USER_REQUEST_SENDER.create(secondCaptain, ADMIN_USER.userName(), ADMIN_USER.password());
        assertThat(response.statusCode())
            .as("Did not receive a 400_BAD_REQUEST HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);

        assertThat(response.body())
            .as("Did not receive an error message specifying the team already has a captain")
            .contains("cannot have multiple captains");
    }

    @Test
    void whenUpdatingUser_andUserIsChangingCategory_givenCategoryExceedsTotalPermittedAmount_thenUserIsNotUpdated_andResponseHas400Status()
        throws FoldingRestException {
        final Team team = TeamUtils.create(generateTeam());
        final UserRequest firstUser = generateUserWithCategory(Category.NVIDIA_GPU);
        firstUser.setTeamId(team.getId());
        create(firstUser);
        final UserRequest secondUser = generateUserWithCategory(Category.WILDCARD);
        secondUser.setTeamId(team.getId());
        create(secondUser);
        final UserRequest thirdUser = generateUserWithCategory(Category.AMD_GPU);
        thirdUser.setTeamId(team.getId());
        final User createdUser = create(thirdUser);

        final UserRequest userToUpdate = UserRequest.builder()
            .foldingUserName(createdUser.getFoldingUserName())
            .displayName(createdUser.getDisplayName())
            .passkey(createdUser.getPasskey())
            .category(Category.NVIDIA_GPU.toString())
            .profileLink(createdUser.getProfileLink())
            .liveStatsLink(createdUser.getLiveStatsLink())
            .hardwareId(createdUser.getHardware().getId())
            .teamId(createdUser.getTeam().getId())
            .userIsCaptain(createdUser.isUserIsCaptain())
            .build();

        final HttpResponse<String> response =
            USER_REQUEST_SENDER.update(createdUser.getId(), userToUpdate, ADMIN_USER.userName(), ADMIN_USER.password());
        assertThat(response.statusCode())
            .as("Did not receive a 400_BAD_REQUEST HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);

        assertThat(response.body())
            .as("Did not receive an error message specifying the team is too large")
            .contains("category '" + Category.NVIDIA_GPU + "'");
    }

    @Test
    void whenUpdateUser_andUserIsBecomingCaptain_givenTeamAlreadyHasCaptain_thenUserIsNotUpdated_andResponseHas400Status()
        throws FoldingRestException {
        final UserRequest firstCaptain = generateUserWithCategory(Category.NVIDIA_GPU);
        firstCaptain.setUserIsCaptain(true);
        create(firstCaptain);

        final UserRequest secondCaptain = generateUserWithCategory(Category.AMD_GPU);
        secondCaptain.setTeamId(firstCaptain.getTeamId());
        final User createdUser = create(secondCaptain);

        final UserRequest userToUpdate = UserRequest.builder()
            .foldingUserName(createdUser.getFoldingUserName())
            .displayName(createdUser.getDisplayName())
            .passkey(createdUser.getPasskey())
            .category(createdUser.getCategory().toString())
            .profileLink(createdUser.getProfileLink())
            .liveStatsLink(createdUser.getLiveStatsLink())
            .hardwareId(createdUser.getHardware().getId())
            .teamId(createdUser.getTeam().getId())
            .userIsCaptain(true)
            .build();

        final HttpResponse<String> response =
            USER_REQUEST_SENDER.update(createdUser.getId(), userToUpdate, ADMIN_USER.userName(), ADMIN_USER.password());
        assertThat(response.statusCode())
            .as("Did not receive a 400_BAD_REQUEST HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);

        assertThat(response.body())
            .as("Did not receive an error message specifying the team already has a captain")
            .contains("cannot have multiple captains");
    }

    @Test
    void whenUpdateHardware_givenUserReferencesHardware_thenUserIsUpdatedWithNewHardwareDetails_andResponseHas200Status()
        throws FoldingRestException {
        final User createdUser = create(generateUser());

        final Hardware originalHardware = createdUser.getHardware();

        final HardwareRequest hardwareToUpdate = HardwareRequest.builder()
            .hardwareName(originalHardware.getHardwareName())
            .displayName("New Name")
            .operatingSystem(originalHardware.getOperatingSystem().toString())
            .multiplier(originalHardware.getMultiplier())
            .build();

        final HttpResponse<String> response =
            HARDWARE_REQUEST_SENDER.update(originalHardware.getId(), hardwareToUpdate, ADMIN_USER.userName(), ADMIN_USER.password());
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final User userAfterHardwareUpdate = UserResponseParser.get(USER_REQUEST_SENDER.get(createdUser.getId()));

        assertThat(userAfterHardwareUpdate.getHardware())
            .as("Expected user's hardware to be changed: " + userAfterHardwareUpdate)
            .isNotEqualTo(originalHardware);

        assertThat(userAfterHardwareUpdate.getHardware().getDisplayName())
            .as("Expected user's hardware display name to be updated to new one: " + userAfterHardwareUpdate)
            .isEqualTo(hardwareToUpdate.getDisplayName());
    }

    @AfterAll
    static void tearDown() throws FoldingRestException {
        cleanSystemForSimpleTests();
    }
}

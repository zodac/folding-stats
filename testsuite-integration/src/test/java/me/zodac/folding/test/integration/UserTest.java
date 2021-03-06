/*
 * MIT License
 *
 * Copyright (c) 2021-2022 zodac.me
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.zodac.folding.test.integration;

import static me.zodac.folding.api.util.EncodingUtils.encodeBasicAuthentication;
import static me.zodac.folding.rest.api.util.RestUtilConstants.GSON;
import static me.zodac.folding.rest.api.util.RestUtilConstants.HTTP_CLIENT;
import static me.zodac.folding.test.integration.util.TestAuthenticationData.ADMIN_USER;
import static me.zodac.folding.test.integration.util.TestAuthenticationData.INVALID_PASSWORD;
import static me.zodac.folding.test.integration.util.TestAuthenticationData.INVALID_USERNAME;
import static me.zodac.folding.test.integration.util.TestAuthenticationData.READ_ONLY_USER;
import static me.zodac.folding.test.integration.util.rest.request.UserUtils.USER_REQUEST_SENDER;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collection;
import me.zodac.folding.api.tc.Category;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.client.java.response.HardwareResponseParser;
import me.zodac.folding.client.java.response.TeamResponseParser;
import me.zodac.folding.client.java.response.UserResponseParser;
import me.zodac.folding.rest.api.exception.FoldingRestException;
import me.zodac.folding.rest.api.header.ContentType;
import me.zodac.folding.rest.api.header.RestHeader;
import me.zodac.folding.rest.api.tc.request.HardwareRequest;
import me.zodac.folding.rest.api.tc.request.TeamRequest;
import me.zodac.folding.rest.api.tc.request.UserRequest;
import me.zodac.folding.test.integration.util.PasskeyChecker;
import me.zodac.folding.test.integration.util.SystemCleaner;
import me.zodac.folding.test.integration.util.TestConstants;
import me.zodac.folding.test.integration.util.TestGenerator;
import me.zodac.folding.test.integration.util.rest.request.HardwareUtils;
import me.zodac.folding.test.integration.util.rest.request.StubbedFoldingEndpointUtils;
import me.zodac.folding.test.integration.util.rest.request.TeamUtils;
import me.zodac.folding.test.integration.util.rest.request.UserUtils;
import me.zodac.folding.test.integration.util.rest.response.HttpResponseHeaderUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link User} REST endpoint at {@code /folding/users}.
 */
class UserTest {

    @BeforeEach
    void setUp() throws FoldingRestException {
        SystemCleaner.cleanSystemForSimpleTests();
    }

    @AfterAll
    static void tearDown() throws FoldingRestException {
        SystemCleaner.cleanSystemForSimpleTests();
    }

    @Test
    void whenGettingAllUsers_givenNoUserHasBeenCreated_thenAnEmptyJsonResponseIsReturned_andHas200Status() throws FoldingRestException {
        final HttpResponse<String> response = USER_REQUEST_SENDER.getAllWithoutPasskeys();
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final Collection<User> allUsers = UserResponseParser.getAll(response);
        final int xTotalCount = HttpResponseHeaderUtils.getTotalCount(response);

        assertThat(xTotalCount)
            .isEqualTo(allUsers.size());

        assertThat(allUsers)
            .isEmpty();
    }

    @Test
    void whenGettingAllUsers_givenSomeUsersHaveBeenCreated_thenAllAreReturned_andPasskeyIsHidden_andHas200Status() throws FoldingRestException {
        UserUtils.create(TestGenerator.generateUser());
        UserUtils.create(TestGenerator.generateUser());

        final HttpResponse<String> response = USER_REQUEST_SENDER.getAllWithoutPasskeys();
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final Collection<User> allUsers = UserResponseParser.getAll(response);
        final int xTotalCount = HttpResponseHeaderUtils.getTotalCount(response);

        assertThat(xTotalCount)
            .isEqualTo(2);

        assertThat(allUsers)
            .hasSize(2);

        final User retrievedUser = allUsers.iterator().next();
        PasskeyChecker.assertPasskeyIsHidden(retrievedUser.passkey());
    }

    @Test
    void whenGettingAllUsers_givenSomeUsersHaveBeenCreated_andPasskeysAreRequest_thenAllAreReturned_andPasskeyIsShown_andHas200Status()
        throws FoldingRestException {
        UserUtils.create(TestGenerator.generateUser());
        UserUtils.create(TestGenerator.generateUser());

        final HttpResponse<String> response = USER_REQUEST_SENDER.getAllWithPasskeys(ADMIN_USER.userName(), ADMIN_USER.password());
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final Collection<User> allUsers = UserResponseParser.getAll(response);
        final int xTotalCount = HttpResponseHeaderUtils.getTotalCount(response);

        assertThat(xTotalCount)
            .isEqualTo(2);

        assertThat(allUsers)
            .hasSize(2);

        final User retrievedUser = allUsers.iterator().next();
        PasskeyChecker.assertPasskeyIsShown(retrievedUser.passkey());
    }

    @Test
    void whenCreatingUser_givenPayloadIsValid_thenTheCreatedUserIsReturnedInResponse_andHasId_andResponseHas201Status()
        throws FoldingRestException {
        final UserRequest userToCreate = TestGenerator.generateUser();
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
    void whenGettingUser_givenValidUserId_thenUserIsReturned_andPasskeyIsMasked_andHas200Status() throws FoldingRestException {
        final int userId = UserUtils.create(TestGenerator.generateUser()).id();

        final HttpResponse<String> response = USER_REQUEST_SENDER.get(userId);
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final User user = UserResponseParser.get(response);
        assertThat(user.id())
            .as("Did not receive the expected user: " + response.body())
            .isEqualTo(userId);
        PasskeyChecker.assertPasskeyIsHidden(user.passkey());
    }

    @Test
    void whenGettingUserWithPasskey_givenValidUserId_thenUserIsReturned_andPasskeyIsNotMasked_andHas200Status() throws FoldingRestException {
        final int userId = UserUtils.create(TestGenerator.generateUser()).id();

        final HttpResponse<String> response = USER_REQUEST_SENDER.getWithPasskey(userId, ADMIN_USER.userName(), ADMIN_USER.password());
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final User user = UserResponseParser.get(response);
        assertThat(user.id())
            .as("Did not receive the expected user: " + response.body())
            .isEqualTo(userId);
        PasskeyChecker.assertPasskeyIsShown(user.passkey());
    }

    @Test
    void whenUpdatingUser_givenValidUserId_andValidPayload_thenUpdatedUserIsReturned_andNoNewUserIsCreated_andHas200Status()
        throws FoldingRestException {
        final User createdUser = UserUtils.create(TestGenerator.generateUser());
        final int initialSize = UserUtils.getNumberOfUsers();

        final String updatedPasskey = "updatedPasskey123456789012345678";
        final UserRequest userToUpdate = UserRequest.builder()
            .foldingUserName(createdUser.foldingUserName())
            .displayName(createdUser.displayName())
            .passkey(updatedPasskey)
            .category(createdUser.category().toString())
            .profileLink(createdUser.profileLink())
            .liveStatsLink(createdUser.liveStatsLink())
            .hardwareId(createdUser.hardware().id())
            .teamId(createdUser.team().id())
            .userIsCaptain(createdUser.userIsCaptain())
            .build();
        StubbedFoldingEndpointUtils.enableUser(userToUpdate);

        final HttpResponse<String> response =
            USER_REQUEST_SENDER.update(createdUser.id(), userToUpdate, ADMIN_USER.userName(), ADMIN_USER.password());
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final User actual = UserResponseParser.update(response);
        assertThat(actual)
            .as("Did not receive created object as JSON response: " + response.body())
            .extracting("id", "foldingUserName", "displayName", "passkey", "category", "profileLink", "liveStatsLink", "userIsCaptain")
            .containsExactly(createdUser.id(), createdUser.foldingUserName(), createdUser.displayName(), updatedPasskey,
                createdUser.category(), createdUser.profileLink(), createdUser.liveStatsLink(), createdUser.userIsCaptain());

        final int allUsersAfterUpdate = UserUtils.getNumberOfUsers();
        assertThat(allUsersAfterUpdate)
            .as("Expected no new user instances to be created")
            .isEqualTo(initialSize);
    }

    @Test
    void whenDeletingUser_givenValidUserId_thenUserIsDeleted_andHas200Status_andUserCountIsReduced_andUserCannotBeRetrievedAgain()
        throws FoldingRestException {
        final int userId = UserUtils.create(TestGenerator.generateUser()).id();
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
        final UserRequest user = TestGenerator.generateUserWithHardwareId(invalidHardwareId);

        final HttpResponse<String> response = USER_REQUEST_SENDER.create(user, ADMIN_USER.userName(), ADMIN_USER.password());

        assertThat(response.statusCode())
            .as("Did not receive a 400_BAD_REQUEST HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);

        assertThat(response.body())
            .as("Did not receive expected error message in response")
            .contains("hardwareId");
    }

    @Test
    void whenCreatingUser_givenUserHasNoUnitsCompleted_thenUserIsNotCreated_andHas400Status() throws FoldingRestException {
        final UserRequest user = TestGenerator.generateUser();
        StubbedFoldingEndpointUtils.disableUser(user);

        final HttpResponse<String> response = USER_REQUEST_SENDER.create(user, ADMIN_USER.userName(), ADMIN_USER.password());

        assertThat(response.statusCode())
            .as("Did not receive a 400_BAD_REQUEST HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);
    }

    @Test
    void whenCreatingUser_givenUserWithTheSameFoldingNameAndPasskeyAlreadyExists_thenA409ResponseIsReturned() throws FoldingRestException {
        final UserRequest userToCreate = TestGenerator.generateUser();
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
    void whenGettingUser_givenInvalidUserId_thenResponseHas400Status() throws IOException, InterruptedException {
        final HttpRequest request = HttpRequest.newBuilder()
            .GET()
            .uri(URI.create(TestConstants.FOLDING_URL + "/users/" + TestConstants.INVALID_FORMAT_ID))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentTypeValue())
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
        final UserRequest updatedUser = TestGenerator.generateUser();
        StubbedFoldingEndpointUtils.enableUser(updatedUser);

        final HttpResponse<String> response =
            USER_REQUEST_SENDER.update(TestConstants.NON_EXISTING_ID, updatedUser, ADMIN_USER.userName(), ADMIN_USER.password());
        assertThat(response.statusCode())
            .as("Did not receive a 404_NOT_FOUND HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_NOT_FOUND);
    }

    @Test
    void whenUpdatingUser_givenInvalidUserId_thenNoJsonResponseIsReturned_andHas400Status()
        throws IOException, InterruptedException, FoldingRestException {
        final User createdUser = UserUtils.create(TestGenerator.generateUser());

        final String updatedPasskey = "updatedPasskey123456789012345678";
        final UserRequest userToUpdate = UserRequest.builder()
            .foldingUserName(createdUser.foldingUserName())
            .displayName(createdUser.displayName())
            .passkey(updatedPasskey)
            .category(createdUser.category().toString())
            .profileLink(createdUser.profileLink())
            .liveStatsLink(createdUser.liveStatsLink())
            .hardwareId(createdUser.hardware().id())
            .teamId(createdUser.team().id())
            .userIsCaptain(createdUser.userIsCaptain())
            .build();
        StubbedFoldingEndpointUtils.enableUser(userToUpdate);

        final HttpRequest request = HttpRequest.newBuilder()
            .PUT(HttpRequest.BodyPublishers.ofString(GSON.toJson(userToUpdate)))
            .uri(URI.create(TestConstants.FOLDING_URL + "/users/" + TestConstants.INVALID_FORMAT_ID))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentTypeValue())
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
    void whenDeletingUser_givenUserIsTeamCaptain_thenResponseHas400Status() throws FoldingRestException {
        final User captainUser = UserUtils.create(TestGenerator.generateCaptainUser());
        final HttpResponse<Void> response = USER_REQUEST_SENDER.delete(captainUser.id(), ADMIN_USER.userName(), ADMIN_USER.password());

        assertThat(response.statusCode())
            .as("Did not receive a 400_BAD_REQUEST HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);
    }

    @Test
    void whenDeletingUser_givenNonExistingUserId_thenResponseHas404Status() throws FoldingRestException {
        final HttpResponse<Void> response = USER_REQUEST_SENDER.delete(TestConstants.NON_EXISTING_ID, ADMIN_USER.userName(), ADMIN_USER.password());

        assertThat(response.statusCode())
            .as("Did not receive a 404_NOT_FOUND HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_NOT_FOUND);
    }

    @Test
    void whenDeletingUser_givenInvalidUserId_thenResponseHas400Status() throws IOException, InterruptedException {
        final HttpRequest request = HttpRequest.newBuilder()
            .DELETE()
            .uri(URI.create(TestConstants.FOLDING_URL + "/users/" + TestConstants.INVALID_FORMAT_ID))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentTypeValue())
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
        final User createdUser = UserUtils.create(TestGenerator.generateUser());
        final UserRequest userToUpdate = UserRequest.builder()
            .foldingUserName(createdUser.foldingUserName())
            .displayName(createdUser.displayName())
            .passkey(createdUser.passkey())
            .category(createdUser.category().toString())
            .profileLink(createdUser.profileLink())
            .liveStatsLink(createdUser.liveStatsLink())
            .hardwareId(createdUser.hardware().id())
            .teamId(createdUser.team().id())
            .userIsCaptain(createdUser.userIsCaptain())
            .build();

        final HttpResponse<String> updateResponse =
            USER_REQUEST_SENDER.update(createdUser.id(), userToUpdate, ADMIN_USER.userName(), ADMIN_USER.password());

        assertThat(updateResponse.statusCode())
            .as("Did not receive a 200_OK HTTP response: " + updateResponse.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final User actual = UserResponseParser.update(updateResponse);

        assertThat(actual)
            .extracting("id", "foldingUserName", "displayName", "category", "profileLink", "liveStatsLink", "userIsCaptain")
            .containsExactly(createdUser.id(), createdUser.foldingUserName(), createdUser.displayName(), createdUser.category(),
                createdUser.profileLink(), createdUser.liveStatsLink(), createdUser.userIsCaptain());
        PasskeyChecker.assertPasskeyIsHidden(actual.passkey());
    }

    @Test
    void whenGettingUserById_givenRequestUsesPreviousEntityTag_andUserHasNotChanged_thenResponseHas304Status_andNoBody() throws FoldingRestException {
        final int userId = UserUtils.create(TestGenerator.generateUser()).id();

        final HttpResponse<String> response = USER_REQUEST_SENDER.get(userId);
        assertThat(response.statusCode())
            .as("Expected first request to have a 200_OK HTTP response")
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final String eTag = HttpResponseHeaderUtils.getEntityTag(response);

        final HttpResponse<String> cachedResponse = USER_REQUEST_SENDER.get(userId, eTag);
        assertThat(cachedResponse.statusCode())
            .as("Expected second request to have a 304_NOT_MODIFIED HTTP response")
            .isEqualTo(HttpURLConnection.HTTP_NOT_MODIFIED);

        assertThat(UserResponseParser.get(cachedResponse))
            .as("Expected cached response to have the same content as the non-cached response")
            .isNull();
    }

    @Test
    void whenGettingAllUsersWithoutPasskeys_givenRequestUsesPreviousEntityTag_andUsersHaveNotChanged_thenResponseHas304Status_andNoBody()
        throws FoldingRestException {
        UserUtils.create(TestGenerator.generateUser());

        final HttpResponse<String> response = USER_REQUEST_SENDER.getAllWithoutPasskeys();
        assertThat(response.statusCode())
            .as("Expected first GET request to have a 200_OK HTTP response")
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final String eTag = HttpResponseHeaderUtils.getEntityTag(response);

        final HttpResponse<String> cachedResponse = USER_REQUEST_SENDER.getAllWithoutPasskeys(eTag);
        assertThat(cachedResponse.statusCode())
            .as("Expected second request to have a 304_NOT_MODIFIED HTTP response")
            .isEqualTo(HttpURLConnection.HTTP_NOT_MODIFIED);

        assertThat(UserResponseParser.getAll(cachedResponse))
            .as("Expected cached response to have the same content as the non-cached response")
            .isNull();
    }

    @Test
    void whenGettingAllUsersWithPasskeys_givenRequestUsesPreviousEntityTag_andUsersHaveNotChanged_thenResponseHas304Status_andNoBody()
        throws FoldingRestException {
        UserUtils.create(TestGenerator.generateUser());

        final HttpResponse<String> response = USER_REQUEST_SENDER.getAllWithPasskeys(ADMIN_USER.userName(), ADMIN_USER.password());
        assertThat(response.statusCode())
            .as("Expected first GET request to have a 200_OK HTTP response")
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final String eTag = HttpResponseHeaderUtils.getEntityTag(response);

        final HttpResponse<String> cachedResponse = USER_REQUEST_SENDER.getAllWithPasskeys(eTag, ADMIN_USER.userName(), ADMIN_USER.password());
        assertThat(cachedResponse.statusCode())
            .as("Expected second request to have a 304_NOT_MODIFIED HTTP response")
            .isEqualTo(HttpURLConnection.HTTP_NOT_MODIFIED);

        assertThat(UserResponseParser.getAll(cachedResponse))
            .as("Expected cached response to have the same content as the non-cached response")
            .isNull();
    }

    @Test
    void whenCreatingUser_givenNoAuthentication_thenRequestFails_andResponseHas401Status()
        throws FoldingRestException, IOException, InterruptedException {
        final UserRequest userToCreate = TestGenerator.generateUser();
        StubbedFoldingEndpointUtils.enableUser(userToCreate);

        final HttpRequest request = HttpRequest.newBuilder()
            .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(userToCreate)))
            .uri(URI.create(TestConstants.FOLDING_URL + "/users"))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentTypeValue())
            .build();

        final HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode())
            .as("Did not receive a 401_UNAUTHORIZED HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_UNAUTHORIZED);
    }

    @Test
    void whenUpdatingUser_givenNoAuthentication_thenRequestFails_andResponseHas401Status()
        throws FoldingRestException, IOException, InterruptedException {
        final User createdUser = UserUtils.create(TestGenerator.generateUser());

        final UserRequest userToUpdate = UserRequest.builder()
            .foldingUserName(createdUser.foldingUserName())
            .displayName(createdUser.displayName())
            .passkey("updatedPasskey123456789012345678")
            .category(createdUser.category().toString())
            .profileLink(createdUser.profileLink())
            .liveStatsLink(createdUser.liveStatsLink())
            .hardwareId(createdUser.hardware().id())
            .teamId(createdUser.team().id())
            .userIsCaptain(createdUser.userIsCaptain())
            .build();
        StubbedFoldingEndpointUtils.enableUser(userToUpdate);

        final HttpRequest request = HttpRequest.newBuilder()
            .PUT(HttpRequest.BodyPublishers.ofString(GSON.toJson(userToUpdate)))
            .uri(URI.create(TestConstants.FOLDING_URL + "/users/" + createdUser.id()))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentTypeValue())
            .build();

        final HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode())
            .as("Did not receive a 401_UNAUTHORIZED HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_UNAUTHORIZED);
    }

    @Test
    void whenDeletingUser_givenNoAuthentication_thenRequestFails_andResponseHas401Status()
        throws FoldingRestException, IOException, InterruptedException {
        final int userId = UserUtils.create(TestGenerator.generateUser()).id();

        final HttpRequest request = HttpRequest.newBuilder()
            .DELETE()
            .uri(URI.create(TestConstants.FOLDING_URL + "/users/" + userId))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentTypeValue())
            .build();

        final HttpResponse<Void> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.discarding());

        assertThat(response.statusCode())
            .as("Did not receive a 401_UNAUTHORIZED HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_UNAUTHORIZED);
    }

    @Test
    void whenCreatingUser_givenAuthentication_andAuthenticationHasInvalidUser_thenRequestFails_andResponseHas401Status()
        throws FoldingRestException {
        final UserRequest userToCreate = TestGenerator.generateUser();
        StubbedFoldingEndpointUtils.enableUser(userToCreate);

        final HttpResponse<String> response = USER_REQUEST_SENDER.create(userToCreate, INVALID_USERNAME.userName(), INVALID_USERNAME.password());
        assertThat(response.statusCode())
            .as("Did not receive a 401_UNAUTHORIZED HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_UNAUTHORIZED);
    }

    @Test
    void whenCreatingUser_givenAuthentication_andAuthenticationHasInvalidPassword_thenRequestFails_andResponseHas401Status()
        throws FoldingRestException {
        final UserRequest userToCreate = TestGenerator.generateUser();
        StubbedFoldingEndpointUtils.enableUser(userToCreate);

        final HttpResponse<String> response = USER_REQUEST_SENDER.create(userToCreate, INVALID_PASSWORD.userName(), INVALID_PASSWORD.password());
        assertThat(response.statusCode())
            .as("Did not receive a 401_UNAUTHORIZED HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_UNAUTHORIZED);
    }

    @Test
    void whenCreatingUser_givenAuthentication_andUserDoesNotHaveAdminRole_thenRequestFails_andResponseHas403Status()
        throws FoldingRestException {
        final UserRequest userToCreate = TestGenerator.generateUser();
        StubbedFoldingEndpointUtils.enableUser(userToCreate);

        final HttpResponse<String> response = USER_REQUEST_SENDER.create(userToCreate, READ_ONLY_USER.userName(), READ_ONLY_USER.password());
        assertThat(response.statusCode())
            .as("Did not receive a 403_FORBIDDEN HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_FORBIDDEN);
    }

    @Test
    void whenCreatingUser_givenEmptyPayload_thenRequestFails_andResponseHas400Status() throws IOException, InterruptedException {
        final HttpRequest request = HttpRequest.newBuilder()
            .POST(HttpRequest.BodyPublishers.noBody())
            .uri(URI.create(TestConstants.FOLDING_URL + "/users"))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentTypeValue())
            .header("Authorization", encodeBasicAuthentication(ADMIN_USER.userName(), ADMIN_USER.password()))
            .build();

        final HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode())
            .as("Did not receive a 400_BAD_REQUEST HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);
    }

    @Test
    void whenUpdatingUser_givenEmptyPayload_thenRequestFails_andResponseHas400Status()
        throws FoldingRestException, IOException, InterruptedException {
        final User createdUser = UserUtils.create(TestGenerator.generateUser());

        final HttpRequest request = HttpRequest.newBuilder()
            .PUT(HttpRequest.BodyPublishers.noBody())
            .uri(URI.create(TestConstants.FOLDING_URL + "/users/" + createdUser.id()))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentTypeValue())
            .header("Authorization", encodeBasicAuthentication(ADMIN_USER.userName(), ADMIN_USER.password()))
            .build();

        final HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode())
            .as("Did not receive a 400_BAD_REQUEST HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);
    }

    @Test
    void whenCreatingUser_andOptionalFieldIsEmptyString_thenValueShouldBeNullNotEmpty() throws FoldingRestException {
        final UserRequest user = TestGenerator.generateUser();
        StubbedFoldingEndpointUtils.enableUser(user);
        final int userId = UserUtils.create(user).id();

        final User actual = UserUtils.get(userId);
        assertThat(actual)
            .as("Empty optional value should not be returned: " + actual)
            .extracting("liveStatsLink")
            .isNull();
    }

    @Test
    void whenUpdatingUser_andOptionalFieldIsEmptyString_thenValueShouldBeNullNotEmpty() throws FoldingRestException {
        final User createdUser = UserUtils.create(TestGenerator.generateUserWithLiveStatsLink("http://google.com"));

        final UserRequest userToUpdate = UserRequest.builder()
            .foldingUserName(createdUser.foldingUserName())
            .displayName(createdUser.displayName())
            .passkey(createdUser.passkey())
            .category(createdUser.category().toString())
            .profileLink(createdUser.profileLink())
            .liveStatsLink("")
            .hardwareId(createdUser.hardware().id())
            .teamId(createdUser.team().id())
            .userIsCaptain(createdUser.userIsCaptain())
            .build();

        final HttpResponse<String> response =
            USER_REQUEST_SENDER.update(createdUser.id(), userToUpdate, ADMIN_USER.userName(), ADMIN_USER.password());
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final User actual = UserUtils.get(createdUser.id());
        assertThat(actual)
            .as("Empty optional value should not be returned: " + response.body())
            .extracting("liveStatsLink")
            .isNull();
    }

    @Test
    void whenUpdatingHardware_givenUserUsesTheHardware_thenUserWillReflectTheChanges_andResponseHas200Status() throws FoldingRestException {
        final Hardware hardware = HardwareUtils.create(TestGenerator.generateHardware());
        final UserRequest userRequest = TestGenerator.generateUserWithHardwareId(hardware.id());
        final User user = UserUtils.create(userRequest);
        final User initialUser = UserUtils.get(user.id());

        assertThat(initialUser.hardware())
            .as("Expected user to contain initial hardware")
            .isEqualTo(hardware);

        final HardwareRequest hardwareUpdateRequest = HardwareRequest.builder()
            .hardwareName("updatedHardwareName")
            .displayName(hardware.displayName())
            .hardwareMake(hardware.hardwareMake().toString())
            .hardwareType(hardware.hardwareType().toString())
            .multiplier(hardware.multiplier())
            .averagePpd(hardware.averagePpd())
            .build();

        final HttpResponse<String> response =
            HardwareUtils.HARDWARE_REQUEST_SENDER.update(hardware.id(), hardwareUpdateRequest, ADMIN_USER.userName(), ADMIN_USER.password());
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final Hardware updatedHardware = HardwareResponseParser.update(response);
        final User userAfterHardwareUpdate = UserUtils.get(user.id());

        assertThat(userAfterHardwareUpdate.hardware())
            .as("Expected user to contain updated hardware")
            .isEqualTo(updatedHardware);

        final Collection<User> usersAfterUpdate = UserUtils.getAll();
        User foundUser = null;
        for (final User userAfterUpdate : usersAfterUpdate) {
            if (userAfterUpdate.id() == user.id()) {
                foundUser = userAfterUpdate;
                assertThat(foundUser.hardware())
                    .as("Expected user to contain updated team")
                    .isEqualTo(updatedHardware);
                break;
            }
        }

        assertThat(foundUser)
            .as("Could not find updated user after hardware was updated: " + usersAfterUpdate)
            .isNotNull();
    }

    @Test
    void whenUpdatingTeam_givenUserIsOnTheTeam_thenUserWillReflectTheChanges_andResponseHas200Status() throws FoldingRestException {
        final Team team = TeamUtils.create(TestGenerator.generateTeam());
        final UserRequest userRequest = TestGenerator.generateUserWithTeamId(team.id());
        final User user = UserUtils.create(userRequest);
        final User initialUser = UserUtils.get(user.id());

        assertThat(initialUser.team())
            .as("Expected user to contain initial team")
            .isEqualTo(team);

        final TeamRequest teamUpdateRequest = TeamRequest.builder()
            .teamName("updatedTeamName")
            .teamDescription(team.teamDescription())
            .forumLink(team.forumLink())
            .build();

        final HttpResponse<String> response =
            TeamUtils.TEAM_REQUEST_SENDER.update(team.id(), teamUpdateRequest, ADMIN_USER.userName(), ADMIN_USER.password());
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final Team updatedTeam = TeamResponseParser.update(response);
        final User userAfterTeamUpdate = UserUtils.get(user.id());

        assertThat(userAfterTeamUpdate.team())
            .as("Expected user to contain updated team")
            .isEqualTo(updatedTeam);

        final Collection<User> usersAfterUpdate = UserUtils.getAll();
        final User userWithId = findUserById(usersAfterUpdate, user.id());

        assertThat(userWithId)
            .as("Could not find updated user after team was updated: " + usersAfterUpdate)
            .isNotNull();
        assertThat(userWithId.team())
            .as("Expected user to contain updated team")
            .isEqualTo(updatedTeam);
    }

    @Test
    void whenCreatingUser_givenUserIsCaptain_andCaptainAlreadyExistsInTeam_thenUserBecomesCaptain_andOldUserIsRemovedAsCaptain()
        throws FoldingRestException {
        final User existingCaptain = UserUtils.create(TestGenerator.generateCaptainUser());

        final User retrievedExistingCaptain = UserResponseParser.get(USER_REQUEST_SENDER.get(existingCaptain.id()));
        assertThat(retrievedExistingCaptain.userIsCaptain())
            .as("Expected existing user to be captain")
            .isTrue();

        final Hardware newHardware = HardwareUtils.create(TestGenerator.generateHardwareFromCategory(Category.AMD_GPU));
        final UserRequest userRequest = UserRequest.builder()
            .foldingUserName(TestGenerator.nextUserName())
            .displayName("newUser")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.AMD_GPU.toString())
            .hardwareId(newHardware.id())
            .teamId(existingCaptain.team().id())
            .userIsCaptain(true)
            .build();

        final User newCaptain = UserUtils.create(userRequest);

        final User retrievedOldCaptain = UserResponseParser.get(USER_REQUEST_SENDER.get(existingCaptain.id()));
        assertThat(retrievedOldCaptain.userIsCaptain())
            .as("Expected original user to no longer be captain")
            .isFalse();

        final User retrievedNewCaptain = UserResponseParser.get(USER_REQUEST_SENDER.get(newCaptain.id()));
        assertThat(retrievedNewCaptain.userIsCaptain())
            .as("Expected new user to be captain")
            .isTrue();
    }

    @Test
    void whenUpdatingUser_givenUserIsCaptain_andCaptainAlreadyExistsInTeam_thenUserReplacesOldUserAsCaptain()
        throws FoldingRestException {
        final Hardware newHardware = HardwareUtils.create(TestGenerator.generateHardwareFromCategory(Category.AMD_GPU));

        final User existingCaptain = UserUtils.create(TestGenerator.generateCaptainUser());
        final User nonCaptain = UserUtils.create(UserRequest.builder()
            .foldingUserName(TestGenerator.nextUserName())
            .displayName("newUser")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.AMD_GPU.toString())
            .hardwareId(newHardware.id())
            .teamId(existingCaptain.team().id())
            .build());

        final User retrievedExistingCaptain = UserResponseParser.get(USER_REQUEST_SENDER.get(existingCaptain.id()));
        assertThat(retrievedExistingCaptain.userIsCaptain())
            .as("Expected existing captain to be captain")
            .isTrue();
        final User retrievedExistingNonCaptain = UserResponseParser.get(USER_REQUEST_SENDER.get(nonCaptain.id()));
        assertThat(retrievedExistingNonCaptain.userIsCaptain())
            .as("Expected other user to not be captain")
            .isFalse();

        final User newCaptain = UserUtils.update(nonCaptain.id(), UserRequest.builder()
            .foldingUserName(TestGenerator.nextUserName())
            .displayName("newUser")
            .passkey("DummyPasskey12345678901234567890")
            .category(Category.AMD_GPU.toString())
            .hardwareId(newHardware.id())
            .teamId(existingCaptain.team().id())
            .userIsCaptain(true)
            .build());

        final User retrievedOldCaptain = UserResponseParser.get(USER_REQUEST_SENDER.get(existingCaptain.id()));
        assertThat(retrievedOldCaptain.userIsCaptain())
            .as("Expected original captain to no longer be captain")
            .isFalse();
        final User retrievedNewCaptain = UserResponseParser.get(USER_REQUEST_SENDER.get(newCaptain.id()));
        assertThat(retrievedNewCaptain.userIsCaptain())
            .as("Expected other user to be new captain")
            .isTrue();
    }

    @Test
    void whenUpdatingUser_givenUserIsCaptain_andUserIsChangingTeams_andCaptainAlreadyExistsInTeam_thenUserReplacesOldUserAsCaptain()
        throws FoldingRestException {

        final User firstTeamCaptain = UserUtils.create(TestGenerator.generateCaptainUser());
        final User secondTeamCaptain = UserUtils.create(TestGenerator.generateCaptainUser());

        final User retrievedFirstTeamCaptain = UserResponseParser.get(USER_REQUEST_SENDER.get(firstTeamCaptain.id()));
        assertThat(retrievedFirstTeamCaptain.userIsCaptain())
            .as("Expected user of first team to be captain")
            .isTrue();
        final User retrievedSecondTeamCaptain = UserResponseParser.get(USER_REQUEST_SENDER.get(secondTeamCaptain.id()));
        assertThat(retrievedSecondTeamCaptain.userIsCaptain())
            .as("Expected user of second team to be captain")
            .isTrue();

        final User firstUserMovingToSecondTeam = UserUtils.update(firstTeamCaptain.id(), UserRequest.builder()
            .foldingUserName(firstTeamCaptain.foldingUserName())
            .displayName(firstTeamCaptain.displayName())
            .passkey(firstTeamCaptain.passkey())
            .category(Category.WILDCARD.toString())
            .hardwareId(firstTeamCaptain.hardware().id())
            .teamId(secondTeamCaptain.team().id())
            .userIsCaptain(firstTeamCaptain.userIsCaptain())
            .build());

        final User retrievedSecondTeamNewCaptain = UserResponseParser.get(USER_REQUEST_SENDER.get(firstUserMovingToSecondTeam.id()));
        assertThat(retrievedSecondTeamNewCaptain.userIsCaptain())
            .as("Expected moved user to be captain of new team")
            .isTrue();
        final User retrievedSecondTeamOldCaptain = UserResponseParser.get(USER_REQUEST_SENDER.get(secondTeamCaptain.id()));
        assertThat(retrievedSecondTeamOldCaptain.userIsCaptain())
            .as("Expected old captain of team to no longer be captain")
            .isFalse();
    }

    @Test
    void whenCreatingUser_andContentTypeIsNotJson_thenResponse415Status() throws IOException, InterruptedException, FoldingRestException {
        final UserRequest userToCreate = TestGenerator.generateUser();
        StubbedFoldingEndpointUtils.enableUser(userToCreate);

        final HttpRequest request = HttpRequest.newBuilder()
            .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(userToCreate)))
            .uri(URI.create(TestConstants.FOLDING_URL + "/users/"))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.TEXT.contentTypeValue())
            .header(RestHeader.AUTHORIZATION.headerName(), encodeBasicAuthentication(ADMIN_USER.userName(), ADMIN_USER.password()))
            .build();

        final HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode())
            .as("Did not receive a 415_UNSUPPORTED_MEDIA_TYPE HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_UNSUPPORTED_TYPE);
    }

    private static User findUserById(final Collection<User> users, final int id) {
        for (final User user : users) {
            if (user.id() == id) {
                return user;
            }
        }
        return null;
    }
}

/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2023 zodac.me
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package me.zodac.folding.test.integration;

import static me.zodac.folding.api.util.EncodingUtils.encodeBasicAuthentication;
import static me.zodac.folding.rest.api.util.RestUtilConstants.GSON;
import static me.zodac.folding.rest.api.util.RestUtilConstants.HTTP_CLIENT;
import static me.zodac.folding.test.integration.util.DummyAuthenticationData.ADMIN_USER;
import static me.zodac.folding.test.integration.util.DummyAuthenticationData.INVALID_PASSWORD;
import static me.zodac.folding.test.integration.util.DummyAuthenticationData.INVALID_USERNAME;
import static me.zodac.folding.test.integration.util.DummyAuthenticationData.READ_ONLY_USER;
import static me.zodac.folding.test.integration.util.rest.request.UserUtils.USER_REQUEST_SENDER;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import me.zodac.folding.api.tc.Category;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.Role;
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
import me.zodac.folding.test.integration.util.DummyDataGenerator;
import me.zodac.folding.test.integration.util.PasskeyChecker;
import me.zodac.folding.test.integration.util.SystemCleaner;
import me.zodac.folding.test.integration.util.TestConstants;
import me.zodac.folding.test.integration.util.rest.request.HardwareUtils;
import me.zodac.folding.test.integration.util.rest.request.StubbedFoldingEndpointUtils;
import me.zodac.folding.test.integration.util.rest.request.TeamUtils;
import me.zodac.folding.test.integration.util.rest.request.UserUtils;
import me.zodac.folding.test.integration.util.rest.response.HttpResponseHeaderUtils;
import org.checkerframework.nullaway.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
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
    @Order(1)
    void whenGetAllUsers_givenNoUserHasBeenCreated_thenAnEmptyJsonResponseIsReturned_andHas200Status() throws FoldingRestException {
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
    void whenGetAllUsers_givenUserHasBeenCreated_thenAllAreReturned_andPasskeyIsHidden_andHas200Status_withCorsHeaders() throws FoldingRestException {
        UserUtils.create(DummyDataGenerator.generateUser());
        UserUtils.create(DummyDataGenerator.generateUser());

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

        final Map<String, List<String>> httpHeaders = response.headers().map();
        assertThat(httpHeaders)
            .containsAllEntriesOf(HttpResponseHeaderUtils.expectedCorsHeaders());
    }

    @Test
    void whenGetAllUsers_givenSomeUsersHaveBeenCreated_andPasskeysAreRequested_thenAllAreReturned_andPasskeyIsShown_andHas200Status()
        throws FoldingRestException {
        UserUtils.create(DummyDataGenerator.generateUser());
        UserUtils.create(DummyDataGenerator.generateUser());

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
    void whenCreatingUser_givenPayloadIsValid_thenTheCreatedUserIsReturnedInResponse_andHasId_andResponseHas201Status_withCorsHeaders()
        throws FoldingRestException {
        final UserRequest userToCreate = DummyDataGenerator.generateUser();
        StubbedFoldingEndpointUtils.enableUser(userToCreate);

        final HttpResponse<String> response = USER_REQUEST_SENDER.create(userToCreate, ADMIN_USER.userName(), ADMIN_USER.password());
        assertThat(response.statusCode())
            .as("Did not receive a 201_CREATED HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_CREATED);

        final User actual = UserResponseParser.create(response);
        assertThat(actual)
            .as("Did not receive created object as JSON response: " + response.body())
            .extracting("foldingUserName", "displayName", "passkey", "category", "profileLink", "liveStatsLink", "role")
            .containsExactly(userToCreate.foldingUserName(), userToCreate.displayName(), userToCreate.passkey(),
                Category.get(userToCreate.category()), userToCreate.profileLink(), userToCreate.liveStatsLink(), Role.MEMBER);

        final Map<String, List<String>> httpHeaders = response.headers().map();
        assertThat(httpHeaders)
            .containsAllEntriesOf(HttpResponseHeaderUtils.expectedCorsHeaders());
    }

    @Test
    void whenGetUser_givenValidUserId_thenUserIsReturned_andPasskeyIsMasked_andHas200Status_withCorsHeaders() throws FoldingRestException {
        final int userId = UserUtils.create(DummyDataGenerator.generateUser()).id();

        final HttpResponse<String> response = USER_REQUEST_SENDER.get(userId);
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final User user = UserResponseParser.get(response);
        assertThat(user.id())
            .as("Did not receive the expected user: " + response.body())
            .isEqualTo(userId);
        PasskeyChecker.assertPasskeyIsHidden(user.passkey());

        final Map<String, List<String>> httpHeaders = response.headers().map();
        assertThat(httpHeaders)
            .containsAllEntriesOf(HttpResponseHeaderUtils.expectedCorsHeaders());
    }

    @Test
    void whenGetUserWithPasskey_givenValidUserId_thenUserIsReturned_andPasskeyIsNotMasked_andHas200Status() throws FoldingRestException {
        final int userId = UserUtils.create(DummyDataGenerator.generateUser()).id();

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
    void whenUpdatingUser_givenValidUserId_andValidPayload_thenUpdatedUserIsReturned_andNoNewUserIsCreated_andHas200Status_withCorsHeaders()
        throws FoldingRestException {
        final User createdUser = UserUtils.create(DummyDataGenerator.generateUser());
        final int initialSize = UserUtils.getNumberOfUsers();

        final String updatedPasskey = "updatedPasskey123456789012345678";
        final UserRequest userToUpdate = generateUserRequest(
            createdUser.foldingUserName(),
            createdUser.displayName(),
            updatedPasskey,
            createdUser.category(),
            createdUser.profileLink(),
            createdUser.liveStatsLink(),
            createdUser.hardware().id(),
            createdUser.team().id(),
            createdUser.role().isCaptain()
        );
        StubbedFoldingEndpointUtils.enableUser(userToUpdate);

        final HttpResponse<String> response =
            USER_REQUEST_SENDER.update(createdUser.id(), userToUpdate, ADMIN_USER.userName(), ADMIN_USER.password());
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final User actual = UserResponseParser.update(response);
        assertThat(actual)
            .as("Did not receive created object as JSON response: " + response.body())
            .extracting("id", "foldingUserName", "displayName", "passkey", "category", "profileLink", "liveStatsLink", "role")
            .containsExactly(createdUser.id(), createdUser.foldingUserName(), createdUser.displayName(), updatedPasskey,
                createdUser.category(), createdUser.profileLink(), createdUser.liveStatsLink(), createdUser.role());

        final int allUsersAfterUpdate = UserUtils.getNumberOfUsers();
        assertThat(allUsersAfterUpdate)
            .as("Expected no new user instances to be created")
            .isEqualTo(initialSize);

        final Map<String, List<String>> httpHeaders = response.headers().map();
        assertThat(httpHeaders)
            .containsAllEntriesOf(HttpResponseHeaderUtils.expectedCorsHeaders());
    }

    @Test
    void whenDeletingUser_givenValidUserId_thenUserIsDeleted_andHas200Status_andUserCountIsReduced_andUserCannotBeRetrievedAgain_withCorsHeaders()
        throws FoldingRestException {
        final int userId = UserUtils.create(DummyDataGenerator.generateUser()).id();
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
            .as("Get all response did not return (initial users - deleted user)")
            .isEqualTo(initialSize - 1);

        final Map<String, List<String>> httpHeaders = response.headers().map();
        assertThat(httpHeaders)
            .containsAllEntriesOf(HttpResponseHeaderUtils.expectedCorsHeaders());
    }

    // Negative/alternative test cases

    @Test
    void whenCreatingUser_givenUserWithInvalidHardwareId_thenJsonResponseWithErrorIsReturned_andHas400Status() throws FoldingRestException {
        final int invalidHardwareId = 0;
        final UserRequest user = DummyDataGenerator.generateUserWithHardwareId(invalidHardwareId);

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
        final UserRequest user = DummyDataGenerator.generateUser();
        StubbedFoldingEndpointUtils.disableUser(user);

        final HttpResponse<String> response = USER_REQUEST_SENDER.create(user, ADMIN_USER.userName(), ADMIN_USER.password());

        assertThat(response.statusCode())
            .as("Did not receive a 400_BAD_REQUEST HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);
    }

    @Test
    void whenCreatingUser_givenUserWithTheSameFoldingNameAndPasskeyAlreadyExists_thenA409ResponseIsReturned() throws FoldingRestException {
        final UserRequest userToCreate = DummyDataGenerator.generateUser();
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
        final UserRequest updatedUser = DummyDataGenerator.generateUser();
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
        final User createdUser = UserUtils.create(DummyDataGenerator.generateUser());

        final String updatedPasskey = "updatedPasskey123456789012345678";
        final UserRequest userToUpdate = generateUserRequest(
            createdUser.foldingUserName(),
            createdUser.displayName(),
            updatedPasskey,
            createdUser.category(),
            createdUser.profileLink(),
            createdUser.liveStatsLink(),
            createdUser.hardware().id(),
            createdUser.team().id(),
            createdUser.role().isCaptain()
        );
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
        final User captainUser = UserUtils.create(DummyDataGenerator.generateCaptain());
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
        final User createdUser = UserUtils.create(DummyDataGenerator.generateUser());
        final UserRequest userToUpdate = generateUserRequest(
            createdUser.foldingUserName(),
            createdUser.displayName(),
            createdUser.passkey(),
            createdUser.category(),
            createdUser.profileLink(),
            createdUser.liveStatsLink(),
            createdUser.hardware().id(),
            createdUser.team().id(),
            createdUser.role().isCaptain()
        );

        final HttpResponse<String> updateResponse =
            USER_REQUEST_SENDER.update(createdUser.id(), userToUpdate, ADMIN_USER.userName(), ADMIN_USER.password());

        assertThat(updateResponse.statusCode())
            .as("Did not receive a 200_OK HTTP response: " + updateResponse.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final User actual = UserResponseParser.update(updateResponse);

        assertThat(actual)
            .extracting("id", "foldingUserName", "displayName", "category", "profileLink", "liveStatsLink", "role")
            .containsExactly(createdUser.id(), createdUser.foldingUserName(), createdUser.displayName(), createdUser.category(),
                createdUser.profileLink(), createdUser.liveStatsLink(), createdUser.role());
        PasskeyChecker.assertPasskeyIsHidden(actual.passkey());
    }

    @Test
    void whenGettingUserById_givenRequestUsesPreviousEntityTag_andUserHasNotChanged_thenResponseHas304Status_andNoBody() throws FoldingRestException {
        final int userId = UserUtils.create(DummyDataGenerator.generateUser()).id();

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
        UserUtils.create(DummyDataGenerator.generateUser());

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
        UserUtils.create(DummyDataGenerator.generateUser());

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
        final UserRequest userToCreate = DummyDataGenerator.generateUser();
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
        final User createdUser = UserUtils.create(DummyDataGenerator.generateUser());
        final UserRequest userToUpdate = generateUserRequest(
            createdUser.foldingUserName(),
            createdUser.displayName(),
            "updatedPasskey123456789012345678",
            createdUser.category(),
            createdUser.profileLink(),
            createdUser.liveStatsLink(),
            createdUser.hardware().id(),
            createdUser.team().id(),
            createdUser.role().isCaptain()
        );
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
        final int userId = UserUtils.create(DummyDataGenerator.generateUser()).id();

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
        final UserRequest userToCreate = DummyDataGenerator.generateUser();
        StubbedFoldingEndpointUtils.enableUser(userToCreate);

        final HttpResponse<String> response = USER_REQUEST_SENDER.create(userToCreate, INVALID_USERNAME.userName(), INVALID_USERNAME.password());
        assertThat(response.statusCode())
            .as("Did not receive a 401_UNAUTHORIZED HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_UNAUTHORIZED);
    }

    @Test
    void whenCreatingUser_givenAuthentication_andAuthenticationHasInvalidPassword_thenRequestFails_andResponseHas401Status()
        throws FoldingRestException {
        final UserRequest userToCreate = DummyDataGenerator.generateUser();
        StubbedFoldingEndpointUtils.enableUser(userToCreate);

        final HttpResponse<String> response = USER_REQUEST_SENDER.create(userToCreate, INVALID_PASSWORD.userName(), INVALID_PASSWORD.password());
        assertThat(response.statusCode())
            .as("Did not receive a 401_UNAUTHORIZED HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_UNAUTHORIZED);
    }

    @Test
    void whenCreatingUser_givenAuthentication_andUserDoesNotHaveAdminRole_thenRequestFails_andResponseHas403Status()
        throws FoldingRestException {
        final UserRequest userToCreate = DummyDataGenerator.generateUser();
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
        final User createdUser = UserUtils.create(DummyDataGenerator.generateUser());

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
        final UserRequest user = DummyDataGenerator.generateUser();
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
        final User createdUser = UserUtils.create(DummyDataGenerator.generateUserWithLiveStatsLink("http://google.com"));

        final UserRequest userToUpdate = generateUserRequest(
            createdUser.foldingUserName(),
            createdUser.displayName(),
            createdUser.passkey(),
            createdUser.category(),
            createdUser.profileLink(),
            "",
            createdUser.hardware().id(),
            createdUser.team().id(),
            createdUser.role().isCaptain()
        );

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
        final Hardware hardware = HardwareUtils.create(DummyDataGenerator.generateHardware());
        final UserRequest userRequest = DummyDataGenerator.generateUserWithHardwareId(hardware.id());
        final User user = UserUtils.create(userRequest);
        final User initialUser = UserUtils.get(user.id());

        assertThat(initialUser.hardware())
            .as("Expected user to contain initial hardware")
            .isEqualTo(hardware);

        final HardwareRequest hardwareUpdateRequest = new HardwareRequest(
            "updatedHardwareName",
            hardware.displayName(),
            hardware.hardwareMake().toString(),
            hardware.hardwareType().toString(),
            hardware.multiplier(),
            hardware.averagePpd()
        );

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
        final Team team = TeamUtils.create(DummyDataGenerator.generateTeam());
        final UserRequest userRequest = DummyDataGenerator.generateUserWithTeamId(team.id());
        final User user = UserUtils.create(userRequest);
        final User initialUser = UserUtils.get(user.id());

        assertThat(initialUser.team())
            .as("Expected user to contain initial team")
            .isEqualTo(team);

        final TeamRequest teamUpdateRequest = new TeamRequest("updatedTeamName", team.teamDescription(), team.forumLink());

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
        final Optional<User> userWithId = findUserById(usersAfterUpdate, user.id());

        assertThat(userWithId.orElseThrow(() -> new AssertionError("Couldn't find updated user after team was updated: " + usersAfterUpdate)).team())
            .as("Expected user to contain updated team")
            .isEqualTo(updatedTeam);
    }

    @Test
    void whenCreatingUser_givenUserIsCaptain_andCaptainAlreadyExistsInTeam_thenUserBecomesCaptain_andOldUserIsRemovedAsCaptain()
        throws FoldingRestException {
        final User existingCaptain = UserUtils.create(DummyDataGenerator.generateCaptain());

        final User retrievedExistingCaptain = UserResponseParser.get(USER_REQUEST_SENDER.get(existingCaptain.id()));
        assertThat(retrievedExistingCaptain.role().isCaptain())
            .as("Expected existing user to be captain")
            .isTrue();

        final Hardware newHardware = HardwareUtils.create(DummyDataGenerator.generateHardwareFromCategory(Category.AMD_GPU));
        final User newCaptain = UserUtils.create(generateUserRequest(
            DummyDataGenerator.nextUserName(),
            "newUser",
            "DummyPasskey12345678901234567890",
            Category.AMD_GPU,
            null,
            null,
            newHardware.id(),
            existingCaptain.team().id(),
            true
        ));

        final User retrievedOldCaptain = UserResponseParser.get(USER_REQUEST_SENDER.get(existingCaptain.id()));
        assertThat(retrievedOldCaptain.role().isCaptain())
            .as("Expected original user to no longer be captain")
            .isFalse();

        final User retrievedNewCaptain = UserResponseParser.get(USER_REQUEST_SENDER.get(newCaptain.id()));
        assertThat(retrievedNewCaptain.role().isCaptain())
            .as("Expected new user to be captain")
            .isTrue();
    }

    @Test
    void whenUpdatingUser_givenUserIsCaptain_andCaptainAlreadyExistsInTeam_thenUserReplacesOldUserAsCaptain()
        throws FoldingRestException {
        final Hardware newHardware = HardwareUtils.create(DummyDataGenerator.generateHardwareFromCategory(Category.AMD_GPU));

        final User existingCaptain = UserUtils.create(DummyDataGenerator.generateCaptain());
        final User nonCaptain = UserUtils.create(generateUserRequest(
            DummyDataGenerator.nextUserName(),
            "newUser",
            "DummyPasskey12345678901234567890",
            Category.AMD_GPU,
            null,
            null,
            newHardware.id(),
            existingCaptain.team().id(),
            false
        ));

        final User retrievedExistingCaptain = UserResponseParser.get(USER_REQUEST_SENDER.get(existingCaptain.id()));
        assertThat(retrievedExistingCaptain.role().isCaptain())
            .as("Expected existing captain to be captain")
            .isTrue();
        final User retrievedExistingNonCaptain = UserResponseParser.get(USER_REQUEST_SENDER.get(nonCaptain.id()));
        assertThat(retrievedExistingNonCaptain.role().isCaptain())
            .as("Expected other user to not be captain")
            .isFalse();

        final User newCaptain = UserUtils.update(nonCaptain.id(), generateUserRequest(
            DummyDataGenerator.nextUserName(),
            "newUser",
            "DummyPasskey12345678901234567890",
            Category.AMD_GPU,
            null,
            null,
            newHardware.id(),
            existingCaptain.team().id(),
            true
        ));

        final User retrievedOldCaptain = UserResponseParser.get(USER_REQUEST_SENDER.get(existingCaptain.id()));
        assertThat(retrievedOldCaptain.role().isCaptain())
            .as("Expected original captain to no longer be captain")
            .isFalse();
        final User retrievedNewCaptain = UserResponseParser.get(USER_REQUEST_SENDER.get(newCaptain.id()));
        assertThat(retrievedNewCaptain.role().isCaptain())
            .as("Expected other user to be new captain")
            .isTrue();
    }

    @Test
    void whenUpdatingUser_givenUserIsCaptain_andUserIsChangingTeams_andCaptainAlreadyExistsInTeam_thenUserReplacesOldUserAsCaptain()
        throws FoldingRestException {

        final User firstTeamCaptain = UserUtils.create(DummyDataGenerator.generateCaptain());
        final User secondTeamCaptain = UserUtils.create(DummyDataGenerator.generateCaptain());

        final User retrievedFirstTeamCaptain = UserResponseParser.get(USER_REQUEST_SENDER.get(firstTeamCaptain.id()));
        assertThat(retrievedFirstTeamCaptain.role().isCaptain())
            .as("Expected user of first team to be captain")
            .isTrue();
        final User retrievedSecondTeamCaptain = UserResponseParser.get(USER_REQUEST_SENDER.get(secondTeamCaptain.id()));
        assertThat(retrievedSecondTeamCaptain.role().isCaptain())
            .as("Expected user of second team to be captain")
            .isTrue();

        final User firstUserMovingToSecondTeam = UserUtils.update(firstTeamCaptain.id(), generateUserRequest(
            firstTeamCaptain.foldingUserName(),
            firstTeamCaptain.displayName(),
            firstTeamCaptain.passkey(),
            Category.WILDCARD,
            null,
            null,
            firstTeamCaptain.hardware().id(),
            secondTeamCaptain.team().id(),
            firstTeamCaptain.role().isCaptain()
        ));

        final User retrievedSecondTeamNewCaptain = UserResponseParser.get(USER_REQUEST_SENDER.get(firstUserMovingToSecondTeam.id()));
        assertThat(retrievedSecondTeamNewCaptain.role().isCaptain())
            .as("Expected moved user to be captain of new team")
            .isTrue();
        final User retrievedSecondTeamOldCaptain = UserResponseParser.get(USER_REQUEST_SENDER.get(secondTeamCaptain.id()));
        assertThat(retrievedSecondTeamOldCaptain.role().isCaptain())
            .as("Expected old captain of team to no longer be captain")
            .isFalse();
    }

    @Test
    void whenCreatingUser_andContentTypeIsNotJson_thenResponse415Status() throws IOException, InterruptedException, FoldingRestException {
        final UserRequest userToCreate = DummyDataGenerator.generateUser();
        StubbedFoldingEndpointUtils.enableUser(userToCreate);

        final HttpRequest request = HttpRequest.newBuilder()
            .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(userToCreate)))
            .uri(URI.create(TestConstants.FOLDING_URL + "/users"))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.TEXT.contentTypeValue())
            .header(RestHeader.AUTHORIZATION.headerName(), encodeBasicAuthentication(ADMIN_USER.userName(), ADMIN_USER.password()))
            .build();

        final HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode())
            .as("Did not receive a 415_UNSUPPORTED_MEDIA_TYPE HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_UNSUPPORTED_TYPE);
    }

    private static Optional<User> findUserById(final Collection<User> users, final int id) {
        for (final User user : users) {
            if (user.id() == id) {
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }

    private static UserRequest generateUserRequest(final String foldingUserName,
                                                   final String displayName,
                                                   final String passkey,
                                                   final Category category,
                                                   final @Nullable String profileLink,
                                                   final @Nullable String liveStatsLink,
                                                   final int hardwareId,
                                                   final int teamId,
                                                   final boolean isCaptain) {
        return new UserRequest(
            foldingUserName,
            displayName,
            passkey,
            category.toString(),
            profileLink,
            liveStatsLink,
            hardwareId,
            teamId,
            isCaptain
        );
    }
}

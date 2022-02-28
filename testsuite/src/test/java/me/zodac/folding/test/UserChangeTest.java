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

package me.zodac.folding.test;

import static me.zodac.folding.rest.util.RestUtilConstants.HTTP_CLIENT;
import static me.zodac.folding.test.util.SystemCleaner.cleanSystemForComplexTests;
import static me.zodac.folding.test.util.TestAuthenticationData.ADMIN_USER;
import static me.zodac.folding.test.util.TestConstants.FOLDING_URL;
import static me.zodac.folding.test.util.TestGenerator.generateUser;
import static me.zodac.folding.test.util.rest.response.HttpResponseHeaderUtils.getTotalCount;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.tc.change.UserChange;
import me.zodac.folding.api.tc.change.UserChangeState;
import me.zodac.folding.client.java.request.UserChangeRequestSender;
import me.zodac.folding.client.java.response.UserChangeResponseParser;
import me.zodac.folding.client.java.response.UserResponseParser;
import me.zodac.folding.rest.api.exception.FoldingRestException;
import me.zodac.folding.rest.api.header.ContentType;
import me.zodac.folding.rest.api.header.RestHeader;
import me.zodac.folding.rest.api.tc.request.UserChangeRequest;
import me.zodac.folding.test.util.TestConstants;
import me.zodac.folding.test.util.rest.request.UserUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for the {@link User} REST endpoint at <code>/folding/change</code>.
 */
class UserChangeTest {

    private static final UserChangeRequestSender USER_CHANGE_REQUEST_SENDER = UserChangeRequestSender.createWithUrl(FOLDING_URL);
    private static final String DUMMY_LIVE_STATS_LINK = "https://www.google.com";

    @BeforeEach
    void setUp() throws FoldingRestException {
        cleanSystemForComplexTests();
    }

    @AfterAll
    static void tearDown() throws FoldingRestException {
        cleanSystemForComplexTests();
    }

    @Test
    void whenGettingAllUserChanges_givenNoneHaveBeenCreated_thenEmptyJsonResponseIsReturned_andHas200Status() throws FoldingRestException {
        final HttpResponse<String> response = USER_CHANGE_REQUEST_SENDER.getAll(ADMIN_USER.userName(), ADMIN_USER.password());
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final Collection<User> allUserChanges = UserResponseParser.getAll(response);
        final int xTotalCount = getTotalCount(response);

        assertThat(xTotalCount)
            .isEqualTo(allUserChanges.size());

        assertThat(allUserChanges)
            .isEmpty();
    }

    @Test
    void whenGettingAllUserChanges_givenSomeHaveBeenCreated_thenAllAreReturned_andHas200Status() throws FoldingRestException {
        final UserChange userChange = createUserChange();
        createUserChange();
        createUserChange();

        final HttpResponse<String> response = USER_CHANGE_REQUEST_SENDER.getAll(ADMIN_USER.userName(), ADMIN_USER.password());
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final Collection<UserChange> allUserChanges = UserChangeResponseParser.getAll(response);
        final int xTotalCount = getTotalCount(response);

        assertThat(xTotalCount)
            .isEqualTo(3);

        assertThat(allUserChanges)
            .hasSize(3);

        assertThat(allUserChanges.iterator().next())
            .isEqualTo(userChange);
    }

    @Test
    void whenCreatingUserChange_givenPayloadIsValid_thenTheCreatedUserChangeIsReturnedInResponse_andHasId_andResponseHas201Status()
        throws FoldingRestException {
        final User user = UserUtils.create(generateUser());

        final UserChangeRequest userChangeRequest = UserChangeRequest.builder()
            .userId(user.getId())
            .foldingUserName(user.getFoldingUserName())
            .existingPasskey(user.getPasskey())
            .passkey(user.getPasskey())
            .liveStatsLink(DUMMY_LIVE_STATS_LINK)
            .hardwareId(user.getHardware().getId())
            .immediate(true)
            .build();

        final HttpResponse<String> response = USER_CHANGE_REQUEST_SENDER.create(userChangeRequest);
        assertThat(response.statusCode())
            .as("Did not receive a 201_CREATED HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_CREATED);

        final UserChange userChange = UserChangeResponseParser.create(response);
        assertThat(userChange.getId())
            .as("Expected user change to contain an ID")
            .isNotZero();
        assertThat(userChange.getUser().getLiveStatsLink())
            .as("Expected user change to list user with the updated liveStatsLink")
            .isEqualTo(DUMMY_LIVE_STATS_LINK);
    }

    @Test
    void whenGetUserChange_givenChangeExists_thenChangeIsReturned_andResponseHas200Status() throws FoldingRestException {
        final UserChange userChange = createUserChange();

        final HttpResponse<String> response = USER_CHANGE_REQUEST_SENDER.get(userChange.getId(), ADMIN_USER.userName(), ADMIN_USER.password());
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final UserChange retrievedUserChange = UserChangeResponseParser.get(response);

        assertThat(retrievedUserChange)
            .isEqualTo(userChange);
    }

    @Test
    void whenRejectUserChange_givenChangeExists_thenChangeStateIsUpdated_andResponseHas200Status() throws FoldingRestException {
        final UserChange userChange = createUserChange();

        final HttpResponse<String> response = USER_CHANGE_REQUEST_SENDER.reject(userChange.getId(), ADMIN_USER.userName(), ADMIN_USER.password());
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final UserChange updatedUserChange = UserChangeResponseParser.update(response);
        assertThat(updatedUserChange.getState())
            .as("Expected state to be updated")
            .isEqualTo(UserChangeState.REJECTED);
    }

    @Test
    void whenApproveUserChangeNextMonth_givenChangeExists_thenChangeStateIsUpdated_andResponseHas200Status() throws FoldingRestException {
        final UserChange userChange = createUserChange();

        final HttpResponse<String> response =
            USER_CHANGE_REQUEST_SENDER.approveNextMonth(userChange.getId(), ADMIN_USER.userName(), ADMIN_USER.password());
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final UserChange updatedUserChange = UserChangeResponseParser.update(response);
        assertThat(updatedUserChange.getState())
            .as("Expected state to be updated")
            .isEqualTo(UserChangeState.APPROVED_NEXT_MONTH);
    }

    @Test
    void whenApproveUserChangeImmediately_givenChangeExists_thenChangeStateIsUpdated_andUserIsUpdated_andResponseHas200Status()
        throws FoldingRestException {
        final UserChange userChange = createUserChange();

        final User userBeforeChange = UserUtils.get(userChange.getUser().getId());
        assertThat(userBeforeChange.getLiveStatsLink())
            .as("Expected user's initial liveStatsLink to not be dummy value")
            .isNotEqualTo(DUMMY_LIVE_STATS_LINK);

        final HttpResponse<String> response =
            USER_CHANGE_REQUEST_SENDER.approveImmediately(userChange.getId(), ADMIN_USER.userName(), ADMIN_USER.password());
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final UserChange updatedUserChange = UserChangeResponseParser.update(response);
        assertThat(updatedUserChange.getState())
            .as("Expected state to be updated")
            .isEqualTo(UserChangeState.COMPLETED);

        final User userAfterChange = UserUtils.get(userChange.getUser().getId());
        assertThat(userAfterChange.getLiveStatsLink())
            .as("Expected user's liveStatsLink to be updated to dummy value")
            .isEqualTo(DUMMY_LIVE_STATS_LINK);
    }

    @Test
    void whenGetAllUserChangesWithState_givenMultipleChangesExist_thenOnlyChangesWithStateWillBeReturned_andResponseHas200Status()
        throws FoldingRestException {
        final UserChange userChange1 = createUserChange();
        final UserChange userChange2 = createUserChange();
        createUserChange();

        USER_CHANGE_REQUEST_SENDER.reject(userChange1.getId(), ADMIN_USER.userName(), ADMIN_USER.password());
        USER_CHANGE_REQUEST_SENDER.approveNextMonth(userChange2.getId(), ADMIN_USER.userName(), ADMIN_USER.password());

        final HttpResponse<String> response =
            USER_CHANGE_REQUEST_SENDER.getAll(List.of(UserChangeState.REJECTED, UserChangeState.APPROVED_NEXT_MONTH));
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final Collection<UserChange> allUserChanges = UserChangeResponseParser.getAll(response);
        final int xTotalCount = getTotalCount(response);

        assertThat(xTotalCount)
            .isEqualTo(2);

        assertThat(allUserChanges)
            .hasSize(2);
    }

    @Test
    void whenGetAllUserChangesWithState_givenNoChangesWithStateExist_thenNoneShallBeReturned_andResponseHas200Status() throws FoldingRestException {
        final UserChange userChange1 = createUserChange();
        final UserChange userChange2 = createUserChange();
        createUserChange();

        USER_CHANGE_REQUEST_SENDER.reject(userChange1.getId(), ADMIN_USER.userName(), ADMIN_USER.password());
        USER_CHANGE_REQUEST_SENDER.approveNextMonth(userChange2.getId(), ADMIN_USER.userName(), ADMIN_USER.password());

        final HttpResponse<String> response =
            USER_CHANGE_REQUEST_SENDER.getAll(List.of(UserChangeState.COMPLETED));
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final Collection<UserChange> allUserChanges = UserChangeResponseParser.getAll(response);
        final int xTotalCount = getTotalCount(response);

        assertThat(xTotalCount)
            .isZero();

        assertThat(allUserChanges)
            .isEmpty();
    }

    @Test
    void whenApproveUserChange_givenChangeHasPreviouslyBeenRejected_thenApprovalWillFail_andResponseHas400Status() throws FoldingRestException {
        final UserChange userChange = createUserChange();

        final HttpResponse<String> response = USER_CHANGE_REQUEST_SENDER.reject(userChange.getId(), ADMIN_USER.userName(), ADMIN_USER.password());
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final UserChange updatedUserChange = UserChangeResponseParser.update(response);
        assertThat(updatedUserChange.getState())
            .as("Expected state to be updated")
            .isEqualTo(UserChangeState.REJECTED);

        final HttpResponse<String> secondResponse =
            USER_CHANGE_REQUEST_SENDER.approveImmediately(userChange.getId(), ADMIN_USER.userName(), ADMIN_USER.password());
        assertThat(secondResponse.statusCode())
            .as("Did not receive a 400_BAD_REQUEST HTTP response: " + secondResponse.body())
            .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);
    }

    @Test
    void whenGetAllUserChanges_givenNoAuthentication_thenRequestFails_andResponseHas401Status()
        throws FoldingRestException, IOException, InterruptedException {
        createUserChange();

        final HttpRequest request = HttpRequest.newBuilder()
            .GET()
            .uri(URI.create(FOLDING_URL + "/changes"))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentType())
            .build();

        final HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode())
            .as("Did not receive a 401_UNAUTHORIZED HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_UNAUTHORIZED);
    }

    @Test
    void whenGetUserChange_givenNoAuthentication_thenRequestFails_andResponseHas401Status()
        throws FoldingRestException, IOException, InterruptedException {
        final UserChange userChange = createUserChange();

        final HttpRequest request = HttpRequest.newBuilder()
            .GET()
            .uri(URI.create(FOLDING_URL + "/changes/" + userChange.getId()))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentType())
            .build();

        final HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode())
            .as("Did not receive a 401_UNAUTHORIZED HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_UNAUTHORIZED);
    }

    @Test
    void whenRejectUserChange_givenNoAuthentication_thenRequestFails_andResponseHas401Status()
        throws FoldingRestException, IOException, InterruptedException {
        final UserChange userChange = createUserChange();

        final HttpRequest request = HttpRequest.newBuilder()
            .PUT(HttpRequest.BodyPublishers.noBody())
            .uri(URI.create(FOLDING_URL + "/changes/" + userChange.getId() + "/reject"))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentType())
            .build();

        final HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode())
            .as("Did not receive a 401_UNAUTHORIZED HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_UNAUTHORIZED);
    }

    @Test
    void whenApproveUserChangeImmediately_givenNoAuthentication_thenRequestFails_andResponseHas401Status()
        throws FoldingRestException, IOException, InterruptedException {
        final UserChange userChange = createUserChange();

        final HttpRequest request = HttpRequest.newBuilder()
            .PUT(HttpRequest.BodyPublishers.noBody())
            .uri(URI.create(FOLDING_URL + "/changes/" + userChange.getId() + "/approve/immediate"))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentType())
            .build();

        final HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode())
            .as("Did not receive a 401_UNAUTHORIZED HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_UNAUTHORIZED);
    }

    @Test
    void whenApproveUserChangeNextMonth_givenNoAuthentication_thenRequestFails_andResponseHas401Status()
        throws FoldingRestException, IOException, InterruptedException {
        final UserChange userChange = createUserChange();

        final HttpRequest request = HttpRequest.newBuilder()
            .PUT(HttpRequest.BodyPublishers.noBody())
            .uri(URI.create(FOLDING_URL + "/changes/" + userChange.getId() + "/approve/next"))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentType())
            .build();

        final HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode())
            .as("Did not receive a 401_UNAUTHORIZED HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_UNAUTHORIZED);
    }

    @Test
    void whenCreatingUserChange_givenPayloadPasskeyDoesNotMatchExistingUserPasskey_thenRequestFails_andResponseHas400Status()
        throws FoldingRestException {
        final User user = UserUtils.create(generateUser());

        final UserChangeRequest userChangeRequest = UserChangeRequest.builder()
            .userId(user.getId())
            .foldingUserName(user.getFoldingUserName())
            .existingPasskey("dummyPasskey")
            .passkey(user.getPasskey())
            .liveStatsLink(DUMMY_LIVE_STATS_LINK)
            .hardwareId(user.getHardware().getId())
            .immediate(true)
            .build();

        final HttpResponse<String> response = USER_CHANGE_REQUEST_SENDER.create(userChangeRequest);
        assertThat(response.statusCode())
            .as("Did not receive a 400_BAD_REQUEST HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);
    }

    @Test
    void whenCreatingUserChange_givenUserChangeAlreadyExists_thenRequestFails_andResponseHas400Status()
        throws FoldingRestException {
        final User user = UserUtils.create(generateUser());
        final UserChangeRequest userChangeRequest = generateUserChange(user);

        USER_CHANGE_REQUEST_SENDER.create(userChangeRequest);
        final HttpResponse<String> response = USER_CHANGE_REQUEST_SENDER.create(userChangeRequest);
        assertThat(response.statusCode())
            .as("Did not receive a 409_CONFLICT HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_CONFLICT);
    }

    @Test
    void whenCreatingUserChange_givenUserAlreadyHasSuppliedValues_thenRequestFails_andResponseHas400Status()
        throws FoldingRestException {
        final User user = UserUtils.create(generateUser());

        final UserChangeRequest userChangeRequest = UserChangeRequest.builder()
            .userId(user.getId())
            .foldingUserName(user.getFoldingUserName())
            .existingPasskey(user.getPasskey())
            .passkey(user.getPasskey())
            .liveStatsLink(user.getLiveStatsLink())
            .hardwareId(user.getHardware().getId())
            .immediate(true)
            .build();

        final HttpResponse<String> response = USER_CHANGE_REQUEST_SENDER.create(userChangeRequest);
        assertThat(response.statusCode())
            .as("Did not receive a 400_BAD_REQUEST HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);
    }

    @Test
    void whenGettingUserChange_givenNonExistingUserId_thenResponseHas404Status() throws FoldingRestException {
        final HttpResponse<String> response =
            USER_CHANGE_REQUEST_SENDER.get(TestConstants.NON_EXISTING_ID, ADMIN_USER.userName(), ADMIN_USER.password());

        assertThat(response.statusCode())
            .as("Did not receive a 404_NOT_FOUND HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_NOT_FOUND);

        assertThat(response.body())
            .as("Did not receive an empty JSON response: " + response.body())
            .isEmpty();
    }

    @Test
    void whenGettingUserChange_givenAnOutOfRangeUserId_thenResponseHas400Status() throws FoldingRestException {
        final HttpResponse<String> response =
            USER_CHANGE_REQUEST_SENDER.get(TestConstants.OUT_OF_RANGE_ID, ADMIN_USER.userName(), ADMIN_USER.password());

        assertThat(response.statusCode())
            .as("Did not receive a 400_BAD_REQUEST HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);

        assertThat(response.body())
            .as("Did not receive valid error message: " + response.body())
            .contains("out of range");
    }

    @Test
    void whenGetUserChange_givenChangeExists_thenChangeIsReturned_andPasskeyIsNotHidden_andResponseHas200Status() throws FoldingRestException {
        final UserChange userChange = createUserChange();

        final HttpResponse<String> response = USER_CHANGE_REQUEST_SENDER.get(userChange.getId(), ADMIN_USER.userName(), ADMIN_USER.password());
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final UserChange retrievedUserChange = UserChangeResponseParser.get(response);

        assertThat(retrievedUserChange)
            .isEqualTo(userChange);

        assertThat(retrievedUserChange.getUser().getPasskey())
            .as("Expected the passkey to not be masked")
            .doesNotContain("*");
    }

    @Test
    void whenGetAllUserChanges_givenChangesExists_thenChangesAreReturned_andPasskeyIsNotHidden_andResponseHas200Status() throws FoldingRestException {
        final UserChange userChange = createUserChange();

        final HttpResponse<String> response = USER_CHANGE_REQUEST_SENDER.getAll(ADMIN_USER.userName(), ADMIN_USER.password());
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final Collection<UserChange> allUserChanges = UserChangeResponseParser.getAll(response);
        final int xTotalCount = getTotalCount(response);

        assertThat(xTotalCount)
            .isEqualTo(1);

        assertThat(allUserChanges)
            .hasSize(1);

        final UserChange retrievedUserChange = allUserChanges.iterator().next();

        assertThat(retrievedUserChange)
            .isEqualTo(userChange);

        assertThat(retrievedUserChange.getUser().getPasskey())
            .as("Expected the passkey to not be masked")
            .doesNotContain("*");
    }

    @Test
    void whenGetAllUserChangesWithState_givenChangesExists_thenChangesAreReturned_andPasskeyIsHidden_andResponseHas200Status()
        throws FoldingRestException {
        final UserChange userChange1 = createUserChange();
        final UserChange userChange2 = createUserChange();
        createUserChange();

        USER_CHANGE_REQUEST_SENDER.reject(userChange1.getId(), ADMIN_USER.userName(), ADMIN_USER.password());
        USER_CHANGE_REQUEST_SENDER.approveNextMonth(userChange2.getId(), ADMIN_USER.userName(), ADMIN_USER.password());

        final HttpResponse<String> response =
            USER_CHANGE_REQUEST_SENDER.getAll(List.of(UserChangeState.REJECTED, UserChangeState.APPROVED_NEXT_MONTH));
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final Collection<UserChange> allUserChanges = UserChangeResponseParser.getAll(response);
        final int xTotalCount = getTotalCount(response);

        assertThat(xTotalCount)
            .isEqualTo(2);

        assertThat(allUserChanges)
            .hasSize(2);

        final UserChange retrievedUserChange = allUserChanges.iterator().next();

        final int lengthOfUnmaskedPasskey = 8;
        final String firstPartOfPasskey = retrievedUserChange.getUser().getPasskey().substring(0, lengthOfUnmaskedPasskey);
        final String secondPartOfPasskey = retrievedUserChange.getUser().getPasskey().substring(lengthOfUnmaskedPasskey);

        assertThat(firstPartOfPasskey)
            .as("Expected the first 8 characters of the passkey to be shown")
            .doesNotContain("*");

        assertThat(secondPartOfPasskey)
            .as("Expected the remaining 24 characters of the passkey to be masked")
            .doesNotContainPattern(Pattern.compile("[a-zA-Z]"));
    }

    private static UserChange createUserChange() throws FoldingRestException {
        final User user = UserUtils.create(generateUser());
        final UserChangeRequest userChangeRequest = generateUserChange(user);

        final HttpResponse<String> response = USER_CHANGE_REQUEST_SENDER.create(userChangeRequest);
        assertThat(response.statusCode())
            .as("Did not receive a 201_CREATED HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_CREATED);

        return UserChangeResponseParser.create(response);
    }

    private static UserChangeRequest generateUserChange(final User user) {
        return UserChangeRequest.builder()
            .userId(user.getId())
            .foldingUserName(user.getFoldingUserName())
            .existingPasskey(user.getPasskey())
            .passkey(user.getPasskey())
            .liveStatsLink(DUMMY_LIVE_STATS_LINK)
            .hardwareId(user.getHardware().getId())
            .immediate(true)
            .build();
    }
}

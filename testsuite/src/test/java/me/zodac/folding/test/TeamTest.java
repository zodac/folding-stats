/*
 * MIT License
 *
 * Copyright (c) 2021 zodac.me
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

import static me.zodac.folding.api.util.EncodingUtils.encodeBasicAuthentication;
import static me.zodac.folding.rest.util.RestUtilConstants.GSON;
import static me.zodac.folding.rest.util.RestUtilConstants.HTTP_CLIENT;
import static me.zodac.folding.test.util.SystemCleaner.cleanSystemForSimpleTests;
import static me.zodac.folding.test.util.TestAuthenticationData.ADMIN_USER;
import static me.zodac.folding.test.util.TestAuthenticationData.INVALID_PASSWORD;
import static me.zodac.folding.test.util.TestAuthenticationData.INVALID_USERNAME;
import static me.zodac.folding.test.util.TestAuthenticationData.READ_ONLY_USER;
import static me.zodac.folding.test.util.TestConstants.FOLDING_URL;
import static me.zodac.folding.test.util.TestGenerator.generateTeam;
import static me.zodac.folding.test.util.TestGenerator.nextTeamName;
import static me.zodac.folding.test.util.rest.request.TeamUtils.TEAM_REQUEST_SENDER;
import static me.zodac.folding.test.util.rest.request.TeamUtils.create;
import static me.zodac.folding.test.util.rest.response.HttpResponseHeaderUtils.getEntityTag;
import static me.zodac.folding.test.util.rest.response.HttpResponseHeaderUtils.getTotalCount;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collection;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.client.java.response.TeamResponseParser;
import me.zodac.folding.rest.api.exception.FoldingRestException;
import me.zodac.folding.rest.api.header.ContentType;
import me.zodac.folding.rest.api.header.RestHeader;
import me.zodac.folding.rest.api.tc.request.TeamRequest;
import me.zodac.folding.test.util.TestConstants;
import me.zodac.folding.test.util.TestGenerator;
import me.zodac.folding.test.util.rest.request.TeamUtils;
import me.zodac.folding.test.util.rest.request.UserUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * Tests for the {@link Team} REST endpoint at <code>/folding/teams</code>.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TeamTest {

    @BeforeAll
    static void setUp() throws FoldingRestException {
        cleanSystemForSimpleTests();
    }

    @AfterAll
    static void tearDown() throws FoldingRestException {
        cleanSystemForSimpleTests();
    }

    @Test
    @Order(1)
    void whenGettingAllTeams_givenNoTeamHasBeenCreated_thenEmptyJsonResponseIsReturned_andHas200Status() throws FoldingRestException {
        final HttpResponse<String> response = TEAM_REQUEST_SENDER.getAll();
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final Collection<Team> allTeams = TeamResponseParser.getAll(response);
        final int xTotalCount = getTotalCount(response);

        assertThat(xTotalCount)
            .isEqualTo(allTeams.size());

        assertThat(allTeams)
            .isEmpty();
    }

    @Test
    void whenCreatingTeam_givenPayloadIsValid_thenTheCreatedTeamIsReturnedInResponse_andHasId_andResponseHas201Status()
        throws FoldingRestException {
        final TeamRequest teamToCreate = generateTeam();

        final HttpResponse<String> response = TEAM_REQUEST_SENDER.create(teamToCreate, ADMIN_USER.userName(), ADMIN_USER.password());
        assertThat(response.statusCode())
            .as("Did not receive a 201_CREATED HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_CREATED);

        final Team actual = TeamResponseParser.create(response);
        assertThat(actual)
            .as("Did not receive created object as JSON response: " + response.body())
            .extracting("teamName", "teamDescription", "forumLink")
            .containsExactly(teamToCreate.getTeamName(), teamToCreate.getTeamDescription(), teamToCreate.getForumLink());
    }

    @Test
    void whenGettingTeam_givenValidTeamId_thenTeamIsReturned_andHas200Status() throws FoldingRestException {
        final int teamId = create(generateTeam()).getId();

        final HttpResponse<String> response = TEAM_REQUEST_SENDER.get(teamId);
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final Team team = TeamResponseParser.get(response);
        assertThat(team.getId())
            .as("Did not receive the expected team: " + response.body())
            .isEqualTo(teamId);
    }

    @Test
    void whenGettingTeam_givenValidTeamName_thenTeamIsReturned_andHas200Status() throws FoldingRestException {
        final String teamName = create(generateTeam()).getTeamName();

        final HttpResponse<String> response = TEAM_REQUEST_SENDER.get(teamName);
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final Team team = TeamResponseParser.get(response);
        assertThat(team.getTeamName())
            .as("Did not receive the expected team: " + response.body())
            .isEqualTo(teamName);
    }

    @Test
    void whenUpdatingTeam_givenValidTeamId_andValidPayload_thenUpdatedTeamIsReturned_andNoNewTeamIsCreated_andHas200Status()
        throws FoldingRestException {
        final Team createdTeam = create(generateTeam());
        final int initialSize = TeamUtils.getNumberOfTeams();

        final TeamRequest teamToUpdate = TeamRequest.builder()
            .teamName(createdTeam.getTeamName())
            .teamDescription("Updated description")
            .forumLink(createdTeam.getForumLink())
            .build();

        final HttpResponse<String> response =
            TEAM_REQUEST_SENDER.update(createdTeam.getId(), teamToUpdate, ADMIN_USER.userName(), ADMIN_USER.password());
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final Team actual = TeamResponseParser.update(response);
        assertThat(actual)
            .as("Did not receive created object as JSON response: " + response.body())
            .extracting("id", "teamName", "teamDescription", "forumLink")
            .containsExactly(createdTeam.getId(), teamToUpdate.getTeamName(), teamToUpdate.getTeamDescription(), teamToUpdate.getForumLink());

        final int allTeamsAfterUpdate = TeamUtils.getNumberOfTeams();
        assertThat(allTeamsAfterUpdate)
            .as("Expected no new team instances to be created")
            .isEqualTo(initialSize);
    }

    // Negative/alternative test cases

    @Test
    void whenDeletingTeam_givenValidTeamId_thenTeamIsDeleted_andHas200Status_andTeamCountIsReduced_andTeamCannotBeRetrievedAgain()
        throws FoldingRestException {
        final int teamId = create(generateTeam()).getId();
        final int initialSize = TeamUtils.getNumberOfTeams();

        final HttpResponse<Void> response = TEAM_REQUEST_SENDER.delete(teamId, ADMIN_USER.userName(), ADMIN_USER.password());
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final HttpResponse<String> getResponse = TEAM_REQUEST_SENDER.get(teamId);
        assertThat(getResponse.statusCode())
            .as("Was able to retrieve the team instance, despite deleting it")
            .isEqualTo(HttpURLConnection.HTTP_NOT_FOUND);

        final int newSize = TeamUtils.getNumberOfTeams();
        assertThat(newSize)
            .as("Get all response did not return the initial teams - deleted team")
            .isEqualTo(initialSize - 1);
    }

    @Test
    void whenCreatingTeam_givenTeamWithInvalidUrl_thenJsonResponseWithErrorIsReturned_andHas400Status() throws FoldingRestException {
        final TeamRequest team = TeamRequest.builder()
            .teamName(nextTeamName())
            .forumLink("invalidLink")
            .build();
        final HttpResponse<String> response = TEAM_REQUEST_SENDER.create(team, ADMIN_USER.userName(), ADMIN_USER.password());

        assertThat(response.statusCode())
            .as("Did not receive a 400_BAD_REQUEST HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);

        assertThat(response.body())
            .as("Did not receive expected error message in response")
            .contains("forumLink");
    }

    @Test
    void whenCreatingTeam_givenTeamWithTheNameAlreadyExists_thenA409ResponseIsReturned() throws FoldingRestException {
        final TeamRequest teamToCreate = generateTeam();
        final TeamRequest teamWithSameName = generateTeam();
        teamWithSameName.setTeamName(teamToCreate.getTeamName());

        TEAM_REQUEST_SENDER.create(teamToCreate, ADMIN_USER.userName(), ADMIN_USER.password());
        final HttpResponse<String> response = TEAM_REQUEST_SENDER.create(teamWithSameName, ADMIN_USER.userName(), ADMIN_USER.password());

        assertThat(response.statusCode())
            .as("Did not receive a 409_CONFLICT HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_CONFLICT);
    }

    @Test
    void whenGettingTeam_givenNonExistingTeamId_thenNoJsonResponseIsReturned_andHas404Status() throws FoldingRestException {
        final HttpResponse<String> response = TEAM_REQUEST_SENDER.get(TestConstants.NON_EXISTING_ID);

        assertThat(response.statusCode())
            .as("Did not receive a 404_NOT_FOUND HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_NOT_FOUND);

        assertThat(response.body())
            .as("Did not receive an empty JSON response: " + response.body())
            .isEmpty();
    }

    @Test
    void whenGettingTeam_givenNonExistingTeamName_thenNoJsonResponseIsReturned_andHas404Status() throws FoldingRestException {
        final HttpResponse<String> response = TEAM_REQUEST_SENDER.get("nonExistingName");

        assertThat(response.statusCode())
            .as("Did not receive a 404_NOT_FOUND HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_NOT_FOUND);

        assertThat(response.body())
            .as("Did not receive an empty JSON response: " + response.body())
            .isEmpty();
    }

    @Test
    void whenGettingTeam_givenOutOfRangeTeamId_thenResponseHas400Status() throws FoldingRestException {
        final HttpResponse<String> response = TEAM_REQUEST_SENDER.get(TestConstants.OUT_OF_RANGE_ID);

        assertThat(response.statusCode())
            .as("Did not receive a 400_BAD_REQUEST HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);

        assertThat(response.body())
            .as("Did not receive valid error message: " + response.body())
            .contains("out of range");
    }

    @Test
    void whenGettingTeam_givenInvalidTeamId_thenResponseHas400Status() throws IOException, InterruptedException {
        final HttpRequest request = HttpRequest.newBuilder()
            .GET()
            .uri(URI.create(FOLDING_URL + "/teams/" + TestConstants.INVALID_FORMAT_ID))
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
    void whenUpdatingTeam_givenNonExistingTeamId_thenNoJsonResponseIsReturned_andHas404Status() throws FoldingRestException {
        final TeamRequest updatedTeam = generateTeam();

        final HttpResponse<String> response =
            TEAM_REQUEST_SENDER.update(TestConstants.NON_EXISTING_ID, updatedTeam, ADMIN_USER.userName(), ADMIN_USER.password());
        assertThat(response.statusCode())
            .as("Did not receive a 404_NOT_FOUND HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_NOT_FOUND);

        assertThat(response.body())
            .as("Did not receive an empty JSON response: " + response.body())
            .isEmpty();
    }

    @Test
    void whenUpdatingTeam_givenOutOfRangeTeamId_thenResponseHas400Status() throws FoldingRestException {
        final TeamRequest updatedTeam = generateTeam();
        final HttpResponse<String> response =
            TEAM_REQUEST_SENDER.update(TestConstants.OUT_OF_RANGE_ID, updatedTeam, ADMIN_USER.userName(), ADMIN_USER.password());
        assertThat(response.statusCode())
            .as("Did not receive a 400_BAD_REQUEST HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);

        assertThat(response.body())
            .as("Did not receive valid error message: " + response.body())
            .contains("out of range");
    }

    @Test
    void whenUpdatingTeam_givenInvalidTeamId_thenNoJsonResponseIsReturned_andHas400Status()
        throws IOException, InterruptedException, FoldingRestException {
        final Team createdTeam = create(generateTeam());

        final TeamRequest teamToUpdate = TeamRequest.builder()
            .teamName(createdTeam.getTeamName())
            .teamDescription("Updated description")
            .forumLink(createdTeam.getForumLink())
            .build();

        final HttpRequest request = HttpRequest.newBuilder()
            .PUT(HttpRequest.BodyPublishers.ofString(GSON.toJson(teamToUpdate)))
            .uri(URI.create(FOLDING_URL + "/teams/" + TestConstants.INVALID_FORMAT_ID))
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
    void whenDeletingTeam_givenNonExistingTeamId_thenResponseHas404Status() throws FoldingRestException {
        final HttpResponse<Void> response = TEAM_REQUEST_SENDER.delete(TestConstants.NON_EXISTING_ID, ADMIN_USER.userName(), ADMIN_USER.password());

        assertThat(response.statusCode())
            .as("Did not receive a 404_NOT_FOUND HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_NOT_FOUND);
    }

    @Test
    void whenDeletingTeam_givenOutOfRangeTeamId_thenResponseHas400Status() throws FoldingRestException {
        final HttpResponse<Void> response = TEAM_REQUEST_SENDER.delete(TestConstants.OUT_OF_RANGE_ID, ADMIN_USER.userName(), ADMIN_USER.password());

        assertThat(response.statusCode())
            .as("Did not receive a 400_BAD_REQUEST HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);
    }

    @Test
    void whenDeletingTeam_givenInvalidTeamId_thenResponseHas400Status() throws IOException, InterruptedException {
        final HttpRequest request = HttpRequest.newBuilder()
            .DELETE()
            .uri(URI.create(FOLDING_URL + "/teams/" + TestConstants.INVALID_FORMAT_ID))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentType())
            .header(RestHeader.AUTHORIZATION.headerName(), encodeBasicAuthentication(ADMIN_USER.userName(), ADMIN_USER.password()))
            .build();

        final HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode())
            .as("Did not receive a 400_BAD_REQUEST HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);
    }

    @Test
    void whenUpdatingTeam_givenValidTeamId_andPayloadHasNoChanges_thenOriginalTeamIsReturned_andHas200Status() throws FoldingRestException {
        final Team createdTeam = create(generateTeam());

        final TeamRequest teamToUpdate = TeamRequest.builder()
            .teamName(createdTeam.getTeamName())
            .teamDescription(createdTeam.getTeamDescription())
            .forumLink(createdTeam.getForumLink())
            .build();

        final HttpResponse<String> updateResponse =
            TEAM_REQUEST_SENDER.update(createdTeam.getId(), teamToUpdate, ADMIN_USER.userName(), ADMIN_USER.password());

        assertThat(updateResponse.statusCode())
            .as("Did not receive a 200_OK HTTP response: " + updateResponse.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final Team actual = TeamResponseParser.update(updateResponse);
        assertThat(actual)
            .as("Did not receive the original team in response")
            .extracting("id", "teamName", "teamDescription", "forumLink")
            .containsExactly(createdTeam.getId(), createdTeam.getTeamName(), createdTeam.getTeamDescription(), createdTeam.getForumLink());
    }

    @Test
    void whenDeletingTeam_givenTheTeamIsLinkedToUser_thenResponseHas409Status() throws FoldingRestException {
        final int teamId = create(generateTeam()).getId();
        UserUtils.create(TestGenerator.generateUserWithTeamId(teamId));

        final HttpResponse<Void> deleteTeamResponse = TEAM_REQUEST_SENDER.delete(teamId, ADMIN_USER.userName(), ADMIN_USER.password());
        assertThat(deleteTeamResponse.statusCode())
            .as("Expected to fail due to a 409_CONFLICT: " + deleteTeamResponse)
            .isEqualTo(HttpURLConnection.HTTP_CONFLICT);
    }

    @Test
    void whenGettingTeamById_givenRequestUsesPreviousEntityTag_andTeamHasNotChanged_thenResponseHas304Status_andNoBody() throws FoldingRestException {
        final int teamId = create(generateTeam()).getId();

        final HttpResponse<String> response = TEAM_REQUEST_SENDER.get(teamId);
        assertThat(response.statusCode())
            .as("Expected first request to have a 200_OK HTTP response")
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final String eTag = getEntityTag(response);

        final HttpResponse<String> cachedResponse = TEAM_REQUEST_SENDER.get(teamId, eTag);
        assertThat(cachedResponse.statusCode())
            .as("Expected second request to have a 304_NOT_MODIFIED HTTP response")
            .isEqualTo(HttpURLConnection.HTTP_NOT_MODIFIED);

        assertThat(TeamResponseParser.get(cachedResponse))
            .as("Expected cached response to have the same content as the non-cached response")
            .isNull();
    }

    @Test
    void whenGettingAllTeams_givenRequestUsesPreviousEntityTag_andTeamsHaveNotChanged_thenResponseHas304Status_andNoBody()
        throws FoldingRestException {
        create(generateTeam());

        final HttpResponse<String> response = TEAM_REQUEST_SENDER.getAll();
        assertThat(response.statusCode())
            .as("Expected first GET request to have a 200_OK HTTP response")
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final String eTag = getEntityTag(response);

        final HttpResponse<String> cachedResponse = TEAM_REQUEST_SENDER.getAll(eTag);
        assertThat(cachedResponse.statusCode())
            .as("Expected second request to have a 304_NOT_MODIFIED HTTP response")
            .isEqualTo(HttpURLConnection.HTTP_NOT_MODIFIED);

        assertThat(TeamResponseParser.getAll(cachedResponse))
            .as("Expected cached response to have the same content as the non-cached response")
            .isNull();
    }

    @Test
    void whenCreatingTeam_givenNoAuthentication_thenRequestFails_andResponseHas401Status() throws FoldingRestException {
        final TeamRequest teamToCreate = generateTeam();

        final HttpResponse<String> response = TEAM_REQUEST_SENDER.create(teamToCreate);
        assertThat(response.statusCode())
            .as("Did not receive a 401_UNAUTHORIZED HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_UNAUTHORIZED);
    }

    @Test
    void whenUpdatingTeam_givenNoAuthentication_thenRequestFails_andResponseHas401Status() throws FoldingRestException {
        final Team createdTeam = create(generateTeam());

        final TeamRequest teamToUpdate = TeamRequest.builder()
            .teamName(createdTeam.getTeamName())
            .teamDescription("Updated description")
            .forumLink(createdTeam.getForumLink())
            .build();

        final HttpResponse<String> response = TEAM_REQUEST_SENDER.update(createdTeam.getId(), teamToUpdate);
        assertThat(response.statusCode())
            .as("Did not receive a 401_UNAUTHORIZED HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_UNAUTHORIZED);
    }

    @Test
    void whenDeletingTeam_givenNoAuthentication_thenRequestFails_andResponseHas401Status() throws FoldingRestException {
        final int teamId = create(generateTeam()).getId();

        final HttpResponse<Void> response = TEAM_REQUEST_SENDER.delete(teamId);
        assertThat(response.statusCode())
            .as("Did not receive a 401_UNAUTHORIZED HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_UNAUTHORIZED);
    }

    @Test
    void whenCreatingTeam_givenAuthentication_andAuthenticationHasInvalidUser_thenRequestFails_andResponseHas401Status()
        throws FoldingRestException {
        final TeamRequest teamToCreate = generateTeam();

        final HttpResponse<String> response = TEAM_REQUEST_SENDER.create(teamToCreate, INVALID_USERNAME.userName(), INVALID_USERNAME.password());
        assertThat(response.statusCode())
            .as("Did not receive a 401_UNAUTHORIZED HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_UNAUTHORIZED);
    }

    @Test
    void whenCreatingTeam_givenAuthentication_andAuthenticationHasInvalidPassword_thenRequestFails_andResponseHas401Status()
        throws FoldingRestException {
        final TeamRequest teamToCreate = generateTeam();

        final HttpResponse<String> response = TEAM_REQUEST_SENDER.create(teamToCreate, INVALID_PASSWORD.userName(), INVALID_PASSWORD.password());
        assertThat(response.statusCode())
            .as("Did not receive a 401_UNAUTHORIZED HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_UNAUTHORIZED);
    }

    @Test
    void whenCreatingTeam_givenAuthentication_andUserDoesNotHaveAdminRole_thenRequestFails_andResponseHas403Status()
        throws FoldingRestException {
        final TeamRequest teamToCreate = generateTeam();

        final HttpResponse<String> response = TEAM_REQUEST_SENDER.create(teamToCreate, READ_ONLY_USER.userName(), READ_ONLY_USER.password());
        assertThat(response.statusCode())
            .as("Did not receive a 403_FORBIDDEN HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_FORBIDDEN);
    }

    @Test
    void whenCreatingTeam_givenEmptyPayload_thenRequestFails_andResponseHas400Status() throws IOException, InterruptedException {
        final HttpRequest request = HttpRequest.newBuilder()
            .POST(HttpRequest.BodyPublishers.noBody())
            .uri(URI.create(FOLDING_URL + "/teams"))
            .header("Content-Type", "application/json")
            .header("Authorization", encodeBasicAuthentication(ADMIN_USER.userName(), ADMIN_USER.password()))
            .build();

        final HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode())
            .as("Did not receive a 400_BAD_REQUEST HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);
    }

    @Test
    void whenUpdatingTeam_givenEmptyPayload_thenRequestFails_andResponseHas400Status()
        throws FoldingRestException, IOException, InterruptedException {
        final int teamId = create(generateTeam()).getId();

        final HttpRequest request = HttpRequest.newBuilder()
            .PUT(HttpRequest.BodyPublishers.noBody())
            .uri(URI.create(FOLDING_URL + "/teams/" + teamId))
            .header("Content-Type", "application/json")
            .header("Authorization", encodeBasicAuthentication(ADMIN_USER.userName(), ADMIN_USER.password()))
            .build();

        final HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode())
            .as("Did not receive a 400_BAD_REQUEST HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);
    }

    @Test
    void whenCreatingTeam_andOptionalFieldIsEmptyString_thenValueShouldBeNullNotEmpty() throws FoldingRestException {
        final TeamRequest teamToCreate = TeamRequest.builder()
            .teamName(nextTeamName())
            .forumLink("")
            .build();

        final HttpResponse<String> response = TEAM_REQUEST_SENDER.create(teamToCreate, ADMIN_USER.userName(), ADMIN_USER.password());
        assertThat(response.statusCode())
            .as("Did not receive a 201_CREATED HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_CREATED);

        final int teamId = TeamResponseParser.create(response).getId();

        final Team actual = TeamUtils.get(teamId);
        assertThat(actual)
            .as("Empty optional value should not be returned: " + response.body())
            .extracting("forumLink")
            .isNull();
    }

    @Test
    void whenUpdatingTeam_andOptionalFieldIsEmptyString_thenValueShouldBeNullNotEmpty() throws FoldingRestException {
        final TeamRequest team = TeamRequest.builder()
            .teamName(nextTeamName())
            .forumLink("http://google.com")
            .build();

        final Team createdTeam = create(team);

        final TeamRequest teamToUpdate = TeamRequest.builder()
            .teamName(createdTeam.getTeamName())
            .teamDescription(createdTeam.getTeamDescription())
            .forumLink("")
            .build();

        final HttpResponse<String> response =
            TEAM_REQUEST_SENDER.update(createdTeam.getId(), teamToUpdate, ADMIN_USER.userName(), ADMIN_USER.password());
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final Team actual = TeamUtils.get(createdTeam.getId());
        assertThat(actual)
            .as("Empty optional value should not be returned: " + response.body())
            .extracting("forumLink")
            .isNull();
    }
}

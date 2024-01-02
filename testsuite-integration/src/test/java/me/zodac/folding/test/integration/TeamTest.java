/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2024 zodac.me
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
import static me.zodac.folding.test.integration.util.DummyDataGenerator.generateTeam;
import static me.zodac.folding.test.integration.util.DummyDataGenerator.generateTeamWithName;
import static me.zodac.folding.test.integration.util.DummyDataGenerator.nextTeamName;
import static me.zodac.folding.test.integration.util.SystemCleaner.cleanSystemForSimpleTests;
import static me.zodac.folding.test.integration.util.TestConstants.FOLDING_URL;
import static me.zodac.folding.test.integration.util.rest.request.TeamUtils.TEAM_REQUEST_SENDER;
import static me.zodac.folding.test.integration.util.rest.request.TeamUtils.create;
import static me.zodac.folding.test.integration.util.rest.response.HttpResponseHeaderUtils.getEntityTag;
import static me.zodac.folding.test.integration.util.rest.response.HttpResponseHeaderUtils.getTotalCount;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.client.java.response.TeamResponseParser;
import me.zodac.folding.rest.api.exception.FoldingRestException;
import me.zodac.folding.rest.api.header.ContentType;
import me.zodac.folding.rest.api.header.RestHeader;
import me.zodac.folding.rest.api.tc.request.TeamRequest;
import me.zodac.folding.test.integration.util.DummyDataGenerator;
import me.zodac.folding.test.integration.util.TestConstants;
import me.zodac.folding.test.integration.util.rest.request.TeamUtils;
import me.zodac.folding.test.integration.util.rest.request.UserUtils;
import me.zodac.folding.test.integration.util.rest.response.HttpResponseHeaderUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * Tests for the {@link Team} REST endpoint at {@code /folding/teams}.
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
    void whenGetAllTeams_givenNoTeamHasBeenCreated_thenEmptyJsonResponseIsReturned_andHas200Status() throws FoldingRestException {
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
    void whenGetAllTeams_givenTeamHasBeenCreated_thenAllAreReturned_andHas200Status_withCorsHeaders() throws FoldingRestException {
        final Team team = create(generateTeam());
        final HttpResponse<String> response = TEAM_REQUEST_SENDER.getAll();
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final Collection<Team> allTeams = TeamResponseParser.getAll(response);
        final int xTotalCount = getTotalCount(response);

        assertThat(xTotalCount)
            .isEqualTo(allTeams.size());

        assertThat(allTeams)
            .contains(team);

        final Map<String, List<String>> httpHeaders = response.headers().map();
        assertThat(httpHeaders)
            .containsAllEntriesOf(HttpResponseHeaderUtils.expectedCorsHeaders());
    }

    @Test
    void whenCreatingTeam_givenPayloadIsValid_thenTheCreatedTeamIsReturnedInResponse_andHasId_andHas201Status_withCorsHeaders()
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
            .containsExactly(teamToCreate.teamName(), teamToCreate.teamDescription(), teamToCreate.forumLink());

        final Map<String, List<String>> httpHeaders = response.headers().map();
        assertThat(httpHeaders)
            .containsAllEntriesOf(HttpResponseHeaderUtils.expectedCorsHeaders());
    }

    @Test
    void whenGetTeam_givenValidTeamId_thenTeamIsReturned_andHas200Status_withCorsHeaders() throws FoldingRestException {
        final int teamId = create(generateTeam()).id();

        final HttpResponse<String> response = TEAM_REQUEST_SENDER.get(teamId);
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final Team team = TeamResponseParser.get(response);
        assertThat(team.id())
            .as("Did not receive the expected team: " + response.body())
            .isEqualTo(teamId);

        final Map<String, List<String>> httpHeaders = response.headers().map();
        assertThat(httpHeaders)
            .containsAllEntriesOf(HttpResponseHeaderUtils.expectedCorsHeaders());
    }

    @Test
    void whenGetTeam_givenValidTeamName_thenTeamIsReturned_andHas200Status_withCorsHeaders() throws FoldingRestException {
        final String teamName = create(generateTeam()).teamName();

        final HttpResponse<String> response = TEAM_REQUEST_SENDER.get(teamName);
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final Team team = TeamResponseParser.get(response);
        assertThat(team.teamName())
            .as("Did not receive the expected team: " + response.body())
            .isEqualTo(teamName);

        final Map<String, List<String>> httpHeaders = response.headers().map();
        assertThat(httpHeaders)
            .containsAllEntriesOf(HttpResponseHeaderUtils.expectedCorsHeaders());
    }

    @Test
    void whenUpdatingTeam_givenValidTeamId_andValidPayload_thenUpdatedTeamIsReturned_andNoNewTeamIsCreated_andHas200Status_withCorsHeaders()
        throws FoldingRestException {
        final Team createdTeam = create(generateTeam());
        final int initialSize = TeamUtils.getNumberOfTeams();

        final TeamRequest teamToUpdate = new TeamRequest(createdTeam.teamName(), "Updated description", createdTeam.forumLink());

        final HttpResponse<String> response =
            TEAM_REQUEST_SENDER.update(createdTeam.id(), teamToUpdate, ADMIN_USER.userName(), ADMIN_USER.password());
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final Team actual = TeamResponseParser.update(response);
        assertThat(actual)
            .as("Did not receive created object as JSON response: " + response.body())
            .extracting("id", "teamName", "teamDescription", "forumLink")
            .containsExactly(createdTeam.id(), teamToUpdate.teamName(), teamToUpdate.teamDescription(), teamToUpdate.forumLink());

        final int allTeamsAfterUpdate = TeamUtils.getNumberOfTeams();
        assertThat(allTeamsAfterUpdate)
            .as("Expected no new team instances to be created")
            .isEqualTo(initialSize);

        final Map<String, List<String>> httpHeaders = response.headers().map();
        assertThat(httpHeaders)
            .containsAllEntriesOf(HttpResponseHeaderUtils.expectedCorsHeaders());
    }

    @Test
    void whenDeletingTeam_givenValidTeamId_thenTeamIsDeleted_andHas200Status_andTeamCountIsReduced_andTeamCannotBeRetrievedAgain_withCorsHeaders()
        throws FoldingRestException {
        final int teamId = create(generateTeam()).id();
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
            .as("Get all response did not return (initial teams - deleted team)")
            .isEqualTo(initialSize - 1);

        final Map<String, List<String>> httpHeaders = response.headers().map();
        assertThat(httpHeaders)
            .containsAllEntriesOf(HttpResponseHeaderUtils.expectedCorsHeaders());
    }

    // Negative/alternative test cases

    @Test
    void whenCreatingTeam_givenTeamWithInvalidUrl_thenJsonResponseWithErrorIsReturned_andHas400Status() throws FoldingRestException {
        final TeamRequest team = new TeamRequest(nextTeamName(), null, "invalidLink");
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
        final TeamRequest teamWithSameName = generateTeamWithName(teamToCreate.teamName());

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
    void whenGettingTeam_givenInvalidTeamId_thenResponseHas400Status() throws IOException, InterruptedException {
        final HttpRequest request = HttpRequest.newBuilder()
            .GET()
            .uri(URI.create(FOLDING_URL + "/teams/" + TestConstants.INVALID_FORMAT_ID))
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
    void whenUpdatingTeam_givenInvalidTeamId_thenNoJsonResponseIsReturned_andHas400Status()
        throws IOException, InterruptedException, FoldingRestException {
        final Team createdTeam = create(generateTeam());
        final TeamRequest teamToUpdate = new TeamRequest(createdTeam.teamName(), "Updated description", createdTeam.forumLink());

        final HttpRequest request = HttpRequest.newBuilder()
            .PUT(HttpRequest.BodyPublishers.ofString(GSON.toJson(teamToUpdate)))
            .uri(URI.create(FOLDING_URL + "/teams/" + TestConstants.INVALID_FORMAT_ID))
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
    void whenDeletingTeam_givenNonExistingTeamId_thenResponseHas404Status() throws FoldingRestException {
        final HttpResponse<Void> response = TEAM_REQUEST_SENDER.delete(TestConstants.NON_EXISTING_ID, ADMIN_USER.userName(), ADMIN_USER.password());

        assertThat(response.statusCode())
            .as("Did not receive a 404_NOT_FOUND HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_NOT_FOUND);
    }

    @Test
    void whenDeletingTeam_givenInvalidTeamId_thenResponseHas400Status() throws IOException, InterruptedException {
        final HttpRequest request = HttpRequest.newBuilder()
            .DELETE()
            .uri(URI.create(FOLDING_URL + "/teams/" + TestConstants.INVALID_FORMAT_ID))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentTypeValue())
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
        final TeamRequest teamToUpdate = new TeamRequest(createdTeam.teamName(), createdTeam.teamDescription(), createdTeam.forumLink());

        final HttpResponse<String> updateResponse =
            TEAM_REQUEST_SENDER.update(createdTeam.id(), teamToUpdate, ADMIN_USER.userName(), ADMIN_USER.password());

        assertThat(updateResponse.statusCode())
            .as("Did not receive a 200_OK HTTP response: " + updateResponse.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final Team actual = TeamResponseParser.update(updateResponse);
        assertThat(actual)
            .as("Did not receive the original team in response")
            .extracting("id", "teamName", "teamDescription", "forumLink")
            .containsExactly(createdTeam.id(), createdTeam.teamName(), createdTeam.teamDescription(), createdTeam.forumLink());
    }

    @Test
    void whenDeletingTeam_givenTheTeamIsLinkedToUser_thenResponseHas409Status() throws FoldingRestException {
        final int teamId = create(generateTeam()).id();
        UserUtils.create(DummyDataGenerator.generateUserWithTeamId(teamId));

        final HttpResponse<Void> deleteTeamResponse = TEAM_REQUEST_SENDER.delete(teamId, ADMIN_USER.userName(), ADMIN_USER.password());
        assertThat(deleteTeamResponse.statusCode())
            .as("Expected to fail due to a 409_CONFLICT: " + deleteTeamResponse)
            .isEqualTo(HttpURLConnection.HTTP_CONFLICT);
    }

    @Test
    void whenGettingTeamById_givenRequestUsesPreviousEntityTag_andTeamHasNotChanged_thenResponseHas304Status_andNoBody() throws FoldingRestException {
        final int teamId = create(generateTeam()).id();

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
    void whenCreatingTeam_givenNoAuthentication_thenRequestFails_andResponseHas401Status() throws IOException, InterruptedException {
        final TeamRequest teamToCreate = generateTeam();

        final HttpRequest request = HttpRequest.newBuilder()
            .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(teamToCreate)))
            .uri(URI.create(FOLDING_URL + "/teams"))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentTypeValue())
            .build();

        final HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode())
            .as("Did not receive a 401_UNAUTHORIZED HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_UNAUTHORIZED);
    }

    @Test
    void whenUpdatingTeam_givenNoAuthentication_thenRequestFails_andResponseHas401Status()
        throws FoldingRestException, IOException, InterruptedException {
        final Team createdTeam = create(generateTeam());
        final TeamRequest teamToUpdate = new TeamRequest(createdTeam.teamName(), "Updated description", createdTeam.forumLink());

        final HttpRequest request = HttpRequest.newBuilder()
            .PUT(HttpRequest.BodyPublishers.ofString(GSON.toJson(teamToUpdate)))
            .uri(URI.create(FOLDING_URL + "/teams/" + createdTeam.id()))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentTypeValue())
            .build();

        final HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

        assertThat(response.statusCode())
            .as("Did not receive a 401_UNAUTHORIZED HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_UNAUTHORIZED);
    }

    @Test
    void whenDeletingTeam_givenNoAuthentication_thenRequestFails_andResponseHas401Status()
        throws FoldingRestException, IOException, InterruptedException {
        final int teamId = create(generateTeam()).id();

        final HttpRequest request = HttpRequest.newBuilder()
            .DELETE()
            .uri(URI.create(FOLDING_URL + "/teams/" + teamId))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentTypeValue())
            .build();

        final HttpResponse<Void> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.discarding());
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
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentTypeValue())
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
        final int teamId = create(generateTeam()).id();

        final HttpRequest request = HttpRequest.newBuilder()
            .PUT(HttpRequest.BodyPublishers.noBody())
            .uri(URI.create(FOLDING_URL + "/teams/" + teamId))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.JSON.contentTypeValue())
            .header("Authorization", encodeBasicAuthentication(ADMIN_USER.userName(), ADMIN_USER.password()))
            .build();

        final HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode())
            .as("Did not receive a 400_BAD_REQUEST HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);
    }

    @Test
    void whenCreatingTeam_andOptionalFieldIsEmptyString_thenValueShouldBeNullNotEmpty() throws FoldingRestException {
        final TeamRequest teamToCreate = new TeamRequest(nextTeamName(), null, "");

        final HttpResponse<String> response = TEAM_REQUEST_SENDER.create(teamToCreate, ADMIN_USER.userName(), ADMIN_USER.password());
        assertThat(response.statusCode())
            .as("Did not receive a 201_CREATED HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_CREATED);

        final int teamId = TeamResponseParser.create(response).id();

        final Team actual = TeamUtils.get(teamId);
        assertThat(actual)
            .as("Empty optional value should not be returned: " + response.body())
            .extracting("forumLink")
            .isNull();
    }

    @Test
    void whenUpdatingTeam_andOptionalFieldIsEmptyString_thenValueShouldBeNullNotEmpty() throws FoldingRestException {
        final TeamRequest team = new TeamRequest(nextTeamName(), null, "http://google.com");
        final Team createdTeam = create(team);

        final TeamRequest teamToUpdate = new TeamRequest(createdTeam.teamName(), createdTeam.teamDescription(), "");

        final HttpResponse<String> response =
            TEAM_REQUEST_SENDER.update(createdTeam.id(), teamToUpdate, ADMIN_USER.userName(), ADMIN_USER.password());
        assertThat(response.statusCode())
            .as("Did not receive a 200_OK HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_OK);

        final Team actual = TeamUtils.get(createdTeam.id());
        assertThat(actual)
            .as("Empty optional value should not be returned: " + response.body())
            .extracting("forumLink")
            .isNull();
    }

    @Test
    void whenCreatingTeam_andContentTypeIsNotJson_thenResponse415Status() throws IOException, InterruptedException {
        final TeamRequest team = new TeamRequest(nextTeamName(), null, "http://google.com");

        final HttpRequest request = HttpRequest.newBuilder()
            .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(team)))
            .uri(URI.create(FOLDING_URL + "/teams"))
            .header(RestHeader.CONTENT_TYPE.headerName(), ContentType.TEXT.contentTypeValue())
            .header(RestHeader.AUTHORIZATION.headerName(), encodeBasicAuthentication(ADMIN_USER.userName(), ADMIN_USER.password()))
            .build();

        final HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode())
            .as("Did not receive a 415_UNSUPPORTED_MEDIA_TYPE HTTP response: " + response.body())
            .isEqualTo(HttpURLConnection.HTTP_UNSUPPORTED_TYPE);
    }
}

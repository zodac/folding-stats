package me.zodac.folding.test;

import me.zodac.folding.api.tc.Team;
import me.zodac.folding.client.java.response.TeamResponseParser;
import me.zodac.folding.rest.api.exception.FoldingRestException;
import me.zodac.folding.rest.api.tc.request.TeamRequest;
import me.zodac.folding.test.utils.TestConstants;
import me.zodac.folding.test.utils.TestGenerator;
import me.zodac.folding.test.utils.rest.request.TeamUtils;
import me.zodac.folding.test.utils.rest.request.UserUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static me.zodac.folding.api.utils.EncodingUtils.encodeBasicAuthentication;
import static me.zodac.folding.test.utils.SystemCleaner.cleanSystemForSimpleTests;
import static me.zodac.folding.test.utils.TestAuthenticationData.ADMIN_USER;
import static me.zodac.folding.test.utils.TestAuthenticationData.INVALID_PASSWORD;
import static me.zodac.folding.test.utils.TestAuthenticationData.INVALID_USERNAME;
import static me.zodac.folding.test.utils.TestAuthenticationData.READ_ONLY_USER;
import static me.zodac.folding.test.utils.TestConstants.FOLDING_URL;
import static me.zodac.folding.test.utils.TestConstants.HTTP_CLIENT;
import static me.zodac.folding.test.utils.TestGenerator.generateInvalidTeam;
import static me.zodac.folding.test.utils.TestGenerator.generateTeam;
import static me.zodac.folding.test.utils.TestGenerator.nextTeamName;
import static me.zodac.folding.test.utils.rest.request.TeamUtils.TEAM_REQUEST_SENDER;
import static me.zodac.folding.test.utils.rest.request.TeamUtils.create;
import static me.zodac.folding.test.utils.rest.response.HttpResponseHeaderUtils.getETag;
import static me.zodac.folding.test.utils.rest.response.HttpResponseHeaderUtils.getXTotalCount;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the {@link Team} REST endpoint at <code>/folding/teams</code>.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TeamTest {

    @BeforeAll
    static void setUp() throws FoldingRestException {
        cleanSystemForSimpleTests();
    }

    @Test
    @Order(1)
    void whenGettingAllTeams_givenNoTeamHasBeenCreated_thenAnEmptyJsonResponseIsReturned_andHasA200Status() throws FoldingRestException {
        final HttpResponse<String> response = TEAM_REQUEST_SENDER.getAll();
        assertThat(response.statusCode())
                .as("Did not receive a 200_OK HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_OK);

        final Collection<Team> allTeams = TeamResponseParser.getAll(response);
        final int xTotalCount = getXTotalCount(response);

        assertThat(xTotalCount)
                .isEqualTo(allTeams.size());

        assertThat(allTeams)
                .isEmpty();
    }

    @Test
    void whenCreatingTeam_givenPayloadIsValid_thenTheCreatedTeamIsReturnedInResponse_andHasId_andResponseHasA201StatusCode() throws FoldingRestException {
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
    void whenCreatingBatchOfTeams_givenPayloadIsValid_thenTheTeamsAreCreated_andResponseHasA200Status() throws FoldingRestException {
        final int initialSize = TeamUtils.getNumberOfTeams();

        final List<TeamRequest> batchOfTeams = List.of(
                generateTeam(),
                generateTeam(),
                generateTeam()
        );


        final HttpResponse<String> response = TEAM_REQUEST_SENDER.createBatchOf(batchOfTeams, ADMIN_USER.userName(), ADMIN_USER.password());
        assertThat(response.statusCode())
                .as("Did not receive a 200_OK HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_OK);

        final int newSize = TeamUtils.getNumberOfTeams();
        assertThat(newSize)
                .as("Get all response did not return the initial teams + new teams")
                .isEqualTo(initialSize + batchOfTeams.size());
    }

    @Test
    void whenGettingTeam_givenAValidTeamId_thenTeamIsReturned_andHasA200Status() throws FoldingRestException {
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
    void whenUpdatingTeam_givenAValidTeamId_andAValidPayload_thenUpdatedTeamIsReturned_andNoNewTeamIsCreated_andHasA200Status() throws FoldingRestException {
        final Team createdTeam = create(generateTeam());
        final int initialSize = TeamUtils.getNumberOfTeams();

        final TeamRequest teamToUpdate = TeamRequest.builder()
                .teamName(createdTeam.getTeamName())
                .teamDescription("Updated description")
                .forumLink(createdTeam.getForumLink())
                .build();

        final HttpResponse<String> response = TEAM_REQUEST_SENDER.update(createdTeam.getId(), teamToUpdate, ADMIN_USER.userName(), ADMIN_USER.password());
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

    @Test
    void whenDeletingTeam_givenAValidTeamId_thenTeamIsDeleted_andHasA200Status_andTeamCountIsReduced_andTeamCannotBeRetrievedAgain() throws FoldingRestException {
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

    // Negative/alternative test cases

    @Test
    void whenCreatingTeam_givenATeamWithInvalidUrl_thenJsonResponseWithErrorIsReturned_andHasA400Status() throws FoldingRestException {
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
    void whenGettingTeam_givenANonExistingTeamId_thenNoJsonResponseIsReturned_andHasA404Status() throws FoldingRestException {
        final HttpResponse<String> response = TEAM_REQUEST_SENDER.get(TestConstants.INVALID_ID);

        assertThat(response.statusCode())
                .as("Did not receive a 404_NOT_FOUND HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_NOT_FOUND);

        assertThat(response.body())
                .as("Did not receive an empty JSON response: " + response.body())
                .isEmpty();
    }

    @Test
    void whenUpdatingTeam_givenANonExistingTeamId_thenNoJsonResponseIsReturned_andHasA404Status() throws FoldingRestException {
        final TeamRequest updatedTeam = generateTeam();

        final HttpResponse<String> response = TEAM_REQUEST_SENDER.update(TestConstants.INVALID_ID, updatedTeam, ADMIN_USER.userName(), ADMIN_USER.password());
        assertThat(response.statusCode())
                .as("Did not receive a 404_NOT_FOUND HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_NOT_FOUND);

        assertThat(response.body())
                .as("Did not receive an empty JSON response: " + response.body())
                .isEmpty();
    }

    @Test
    void whenDeletingTeam_givenANonExistingTeamId_thenResponseHasA404Status() throws FoldingRestException {
        final HttpResponse<Void> response = TEAM_REQUEST_SENDER.delete(TestConstants.INVALID_ID, ADMIN_USER.userName(), ADMIN_USER.password());

        assertThat(response.statusCode())
                .as("Did not receive a 404_NOT_FOUND HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_NOT_FOUND);
    }

    @Test
    void whenUpdatingTeam_givenAValidTeamId_andPayloadHasNoChanges_thenOriginalTeamIsReturned_andHasA200Status() throws FoldingRestException {
        final Team createdTeam = create(generateTeam());

        final TeamRequest teamToUpdate = TeamRequest.builder()
                .teamName(createdTeam.getTeamName())
                .teamDescription(createdTeam.getTeamDescription())
                .forumLink(createdTeam.getForumLink())
                .build();

        final HttpResponse<String> updateResponse = TEAM_REQUEST_SENDER.update(createdTeam.getId(), teamToUpdate, ADMIN_USER.userName(), ADMIN_USER.password());

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
    void whenCreatingBatchOfTeams_givenPayloadIsPartiallyValid_thenOnlyValidTeamsAreCreated_andResponseHasA200Status() throws FoldingRestException {
        final int initialTeamsSize = TeamUtils.getNumberOfTeams();

        final List<TeamRequest> batchOfValidTeams = List.of(
                generateTeam(),
                generateTeam()
        );
        final List<TeamRequest> batchOfInvalidTeams = List.of(
                generateInvalidTeam(),
                generateInvalidTeam()
        );
        final List<TeamRequest> batchOfTeams = new ArrayList<>(batchOfValidTeams.size() + batchOfInvalidTeams.size());
        batchOfTeams.addAll(batchOfValidTeams);
        batchOfTeams.addAll(batchOfInvalidTeams);


        final HttpResponse<String> response = TEAM_REQUEST_SENDER.createBatchOf(batchOfTeams, ADMIN_USER.userName(), ADMIN_USER.password());
        assertThat(response.statusCode())
                .as("Did not receive a 200_OK HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_OK);

        final int newTeamsSize = TeamUtils.getNumberOfTeams();
        assertThat(newTeamsSize)
                .as("Get all response did not return the initial teams + new valid teams")
                .isEqualTo(initialTeamsSize + batchOfValidTeams.size());
    }

    @Test
    void whenCreatingBatchOfTeams_givenPayloadIsInvalid_thenResponseHasA400Status() throws FoldingRestException {
        final int initialTeamsSize = TeamUtils.getNumberOfTeams();

        final List<TeamRequest> batchOfInvalidTeams = List.of(
                generateInvalidTeam(),
                generateInvalidTeam()
        );

        final HttpResponse<String> response = TEAM_REQUEST_SENDER.createBatchOf(batchOfInvalidTeams, ADMIN_USER.userName(), ADMIN_USER.password());
        assertThat(response.statusCode())
                .as("Did not receive a 400_BAD_REQUEST HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);

        final int newHardwareSize = TeamUtils.getNumberOfTeams();
        assertThat(newHardwareSize)
                .as("Get all response did not return only the initial teams")
                .isEqualTo(initialTeamsSize);
    }

    @Test
    void whenDeletingTeam_givenTheTeamIsLinkedToAUser_thenResponseHasA409Status() throws FoldingRestException {
        final int teamId = create(generateTeam()).getId();
        UserUtils.create(TestGenerator.generateUserWithTeamId(teamId));

        final HttpResponse<Void> deleteTeamResponse = TEAM_REQUEST_SENDER.delete(teamId, ADMIN_USER.userName(), ADMIN_USER.password());
        assertThat(deleteTeamResponse.statusCode())
                .as("Expected to fail due to a 409_CONFLICT: " + deleteTeamResponse)
                .isEqualTo(HttpURLConnection.HTTP_CONFLICT);
    }

    @Test
    void whenGettingTeamById_givenRequestUsesPreviousETag_andTeamHasNotChanged_thenResponseHasA304Status_andNoBody() throws FoldingRestException {
        final int teamId = create(generateTeam()).getId();

        final HttpResponse<String> response = TEAM_REQUEST_SENDER.get(teamId);
        assertThat(response.statusCode())
                .as("Expected first request to have a 200_OK HTTP response")
                .isEqualTo(HttpURLConnection.HTTP_OK);

        final String eTag = getETag(response);

        final HttpResponse<String> cachedResponse = TEAM_REQUEST_SENDER.get(teamId, eTag);
        assertThat(cachedResponse.statusCode())
                .as("Expected second request to have a 304_NOT_MODIFIED HTTP response")
                .isEqualTo(HttpURLConnection.HTTP_NOT_MODIFIED);

        assertThat(TeamResponseParser.get(cachedResponse))
                .as("Expected cached response to have the same content as the non-cached response")
                .isNull();
    }

    @Test
    void whenGettingAllTeams_givenRequestUsesPreviousETag_andTeamsHaveNotChanged_thenResponseHasA304Status_andNoBody() throws FoldingRestException {
        create(generateTeam());

        final HttpResponse<String> response = TEAM_REQUEST_SENDER.getAll();
        assertThat(response.statusCode())
                .as("Expected first GET request to have a 200_OK HTTP response")
                .isEqualTo(HttpURLConnection.HTTP_OK);

        final String eTag = getETag(response);

        final HttpResponse<String> cachedResponse = TEAM_REQUEST_SENDER.getAll(eTag);
        assertThat(cachedResponse.statusCode())
                .as("Expected second request to have a 304_NOT_MODIFIED HTTP response")
                .isEqualTo(HttpURLConnection.HTTP_NOT_MODIFIED);

        assertThat(TeamResponseParser.getAll(cachedResponse))
                .as("Expected cached response to have the same content as the non-cached response")
                .isNull();
    }

    @Test
    void whenCreatingTeam_givenNoAuthentication_thenRequestFails_andResponseHasA401StatusCode() throws FoldingRestException {
        final TeamRequest teamToCreate = generateTeam();

        final HttpResponse<String> response = TEAM_REQUEST_SENDER.create(teamToCreate);
        assertThat(response.statusCode())
                .as("Did not receive a 401_UNAUTHORIZED HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_UNAUTHORIZED);
    }

    @Test
    void whenCreatingBatchOfTeams_givenNoAuthentication_thenRequestFails_andResponseHasA401StatusCode() throws FoldingRestException {
        final List<TeamRequest> batchOfTeams = List.of(
                generateTeam(),
                generateTeam(),
                generateTeam()
        );

        final HttpResponse<String> response = TEAM_REQUEST_SENDER.createBatchOf(batchOfTeams);
        assertThat(response.statusCode())
                .as("Did not receive a 401_UNAUTHORIZED HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_UNAUTHORIZED);
    }

    @Test
    void whenUpdatingTeam_givenNoAuthentication_thenRequestFails_andResponseHasA401StatusCode() throws FoldingRestException {
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
    void whenDeletingTeam_givenNoAuthentication_thenRequestFails_andResponseHasA401StatusCode() throws FoldingRestException {
        final int teamId = create(generateTeam()).getId();

        final HttpResponse<Void> response = TEAM_REQUEST_SENDER.delete(teamId);
        assertThat(response.statusCode())
                .as("Did not receive a 401_UNAUTHORIZED HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_UNAUTHORIZED);
    }

    @Test
    void whenCreatingTeam_givenAuthentication_andAuthenticationHasInvalidUser_thenRequestFails_andResponseHasA401StatusCode() throws FoldingRestException {
        final TeamRequest teamToCreate = generateTeam();

        final HttpResponse<String> response = TEAM_REQUEST_SENDER.create(teamToCreate, INVALID_USERNAME.userName(), INVALID_USERNAME.password());
        assertThat(response.statusCode())
                .as("Did not receive a 401_UNAUTHORIZED HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_UNAUTHORIZED);
    }

    @Test
    void whenCreatingTeam_givenAuthentication_andAuthenticationHasInvalidPassword_thenRequestFails_andResponseHasA401StatusCode() throws FoldingRestException {
        final TeamRequest teamToCreate = generateTeam();

        final HttpResponse<String> response = TEAM_REQUEST_SENDER.create(teamToCreate, INVALID_PASSWORD.userName(), INVALID_PASSWORD.password());
        assertThat(response.statusCode())
                .as("Did not receive a 401_UNAUTHORIZED HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_UNAUTHORIZED);
    }

    @Test
    void whenCreatingTeam_givenAuthentication_andUserDoesNotHaveAdminRole_thenRequestFails_andResponseHasA403StatusCode() throws FoldingRestException {
        final TeamRequest teamToCreate = generateTeam();

        final HttpResponse<String> response = TEAM_REQUEST_SENDER.create(teamToCreate, READ_ONLY_USER.userName(), READ_ONLY_USER.password());
        assertThat(response.statusCode())
                .as("Did not receive a 403_FORBIDDEN HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_FORBIDDEN);
    }

    @Test
    void whenCreatingTeam_givenEmptyPayload_thenRequestFails_andResponseHasA400StatusCode() throws IOException, InterruptedException {
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
    void whenUpdatingTeam_givenEmptyPayload_thenRequestFails_andResponseHasA400StatusCode() throws FoldingRestException, IOException, InterruptedException {
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
                .isEqualTo(null);
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

        final HttpResponse<String> response = TEAM_REQUEST_SENDER.update(createdTeam.getId(), teamToUpdate, ADMIN_USER.userName(), ADMIN_USER.password());
        assertThat(response.statusCode())
                .as("Did not receive a 200_OK HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_OK);

        final Team actual = TeamUtils.get(createdTeam.getId());
        assertThat(actual)
                .as("Empty optional value should not be returned: " + response.body())
                .extracting("forumLink")
                .isEqualTo(null);
    }

    @AfterAll
    static void tearDown() throws FoldingRestException {
        cleanSystemForSimpleTests();
    }
}

package me.zodac.folding.test;

import me.zodac.folding.api.tc.Category;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.client.java.response.TeamResponseParser;
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
import java.util.Map;
import java.util.Set;

import static me.zodac.folding.test.utils.SystemCleaner.cleanSystemForSimpleTests;
import static me.zodac.folding.test.utils.TeamUtils.TEAM_REQUEST_SENDER;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the {@link Team} REST endpoint at <code>/folding/teams</code>.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TeamTest {

    public static final Team DUMMY_TEAM = Team.createWithoutId("Dummy_Team", "Dummy Team", 1, Set.of(1), Collections.emptySet());

    @BeforeAll
    public static void setUp() throws FoldingRestException {
        cleanSystemForSimpleTests();
        HardwareUtils.createOrConflict(HardwareTest.DUMMY_HARDWARE);
        StubbedFoldingEndpointUtils.enableUser(UserTest.DUMMY_USER);
        UserUtils.createOrConflict(UserTest.DUMMY_USER);
    }

    @Test
    @Order(1)
    public void whenGettingAllTeams_givenNoTeamHasBeenCreated_thenAnEmptyJsonResponseIsReturned_andHasA200Status() throws FoldingRestException {
        final HttpResponse<String> response = TEAM_REQUEST_SENDER.getAll();
        assertThat(response.statusCode())
                .as("Did not receive a 200_OK HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_OK);

        final Collection<Team> allTeams = TeamResponseParser.getAll(response);
        final Map<String, List<String>> headers = response.headers().map();
        assertThat(headers)
                .containsKey("X-Total-Count");

        assertThat(headers.get("X-Total-Count").get(0))
                .isEqualTo(String.valueOf(allTeams.size()));

        assertThat(allTeams)
                .isEmpty();
    }

    @Test
    public void whenCreatingTeam_givenPayloadIsValid_thenTheCreatedTeamIsReturnedInResponse_andHasId_andResponseHasA201StatusCode() throws FoldingRestException {
        final Team teamToCreate = Team.createWithoutId("Dummy_Team1", "Dummy Team", 1, Set.of(1), Collections.emptySet());

        final HttpResponse<String> response = TEAM_REQUEST_SENDER.create(teamToCreate);
        assertThat(response.statusCode())
                .as("Did not receive a 201_CREATED HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_CREATED);

        final Team actual = TeamResponseParser.create(response);
        final Team expected = Team.updateWithId(actual.getId(), teamToCreate);
        assertThat(actual)
                .as("Did not receive created object as JSON response: " + response.body())
                .isEqualTo(expected);
    }

    @Test
    public void whenCreatingBatchOfTeams_givenPayloadIsValid_thenTheTeamsAreCreated_andResponseHasA200Status() throws FoldingRestException {
        final int initialSize = TeamUtils.getNumberOfTeams();

        final List<Team> batchOfTeams = List.of(
                Team.createWithoutId("Dummy_Team2", "Dummy Team", 1, Set.of(1), Collections.emptySet()),
                Team.createWithoutId("Dummy_Team3", "Dummy Team", 1, Set.of(1), Collections.emptySet()),
                Team.createWithoutId("Dummy_Team4", "Dummy Team", 1, Set.of(1), Collections.emptySet())
        );


        final HttpResponse<String> response = TEAM_REQUEST_SENDER.createBatchOf(batchOfTeams);
        assertThat(response.statusCode())
                .as("Did not receive a 200_OK HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_OK);

        final int newSize = TeamUtils.getNumberOfTeams();
        assertThat(newSize)
                .as("Get all response did not return the initial teams + new teams")
                .isEqualTo(initialSize + batchOfTeams.size());
    }

    @Test
    public void whenGettingTeam_givenAValidTeamId_thenTeamIsReturned_andHasA200Status() throws FoldingRestException {
        final Collection<Team> allTeams = TeamUtils.getAll();
        int teamId = allTeams.size();

        if (allTeams.isEmpty()) {
            teamId = TeamUtils.createOrConflict(DUMMY_TEAM).getId();
        }

        final HttpResponse<String> response = TEAM_REQUEST_SENDER.get(teamId);
        assertThat(response.statusCode())
                .as("Did not receive a 200_OK HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_OK);

        final Team team = TeamResponseParser.get(response);
        assertThat(team)
                .as("Did not receive the expected team: " + response.body())
                .extracting("id")
                .isEqualTo(teamId);
    }

    @Test
    public void whenUpdatingTeam_givenAValidTeamId_andAValidPayload_thenUpdatedTeamIsReturned_andNoNewTeamIsCreated_andHasA200Status() throws FoldingRestException {
        final Collection<Team> allTeams = TeamUtils.getAll();
        int teamId = allTeams.size();

        if (allTeams.isEmpty()) {
            teamId = TeamUtils.createOrConflict(DUMMY_TEAM).getId();
        }

        final Team updatedTeam = Team.updateWithId(teamId, DUMMY_TEAM);
        updatedTeam.setTeamDescription("Updated description");

        final HttpResponse<String> response = TEAM_REQUEST_SENDER.update(updatedTeam);
        assertThat(response.statusCode())
                .as("Did not receive a 200_OK HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_OK);

        final Team actual = TeamResponseParser.update(response);
        assertThat(actual)
                .as("Did not receive created object as JSON response: " + response.body())
                .isEqualTo(updatedTeam);


        final int allTeamsAfterUpdate = TeamUtils.getNumberOfTeams();
        assertThat(allTeamsAfterUpdate)
                .as("Expected no new team instances to be created")
                .isEqualTo(teamId);
    }

    @Test
    public void whenDeletingTeam_givenAValidTeamId_thenTeamIsDeleted_andHasA200Status_andTeamCountIsReduced_andTeamCannotBeRetrievedAgain() throws FoldingRestException {
        final Collection<Team> allTeams = TeamUtils.getAll();
        int teamId = allTeams.size();

        if (allTeams.isEmpty()) {
            teamId = TeamUtils.createOrConflict(DUMMY_TEAM).getId();
        }

        final HttpResponse<Void> response = TEAM_REQUEST_SENDER.delete(teamId);
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
                .isEqualTo(teamId - 1);
    }

    @Test
    public void whenRetiringAUserFromATeam_givenValidUserId_thenTeamActiveAndRetiredUsersAreUpdated_andResponseHasA200Status() throws FoldingRestException {
        final User captainUser = User.createWithoutId("Dummy_User1", "Dummy User1", "DummyPasskey1", Category.NVIDIA_GPU, 1, "", false);
        final User userToRetired = User.createWithoutId("Dummy_User2", "Dummy User2", "DummyPasskey2", Category.AMD_GPU, 1, "", false);
        StubbedFoldingEndpointUtils.enableUser(captainUser);
        StubbedFoldingEndpointUtils.enableUser(userToRetired);

        final int captainUserId = UserUtils.createOrConflict(captainUser).getId();
        final int userToRetireId = UserUtils.createOrConflict(userToRetired).getId();

        final Team team = Team.createWithoutId("Dummy_Team12", "Dummy Team", captainUserId, Set.of(captainUserId, userToRetireId), Collections.emptySet());
        final int teamId = TeamUtils.createOrConflict(team).getId();

        final HttpResponse<String> retireResponse = TEAM_REQUEST_SENDER.retireUser(teamId, userToRetireId);
        assertThat(retireResponse.statusCode())
                .as("Did not receive a 200_OK HTTP response: " + retireResponse.body())
                .isEqualTo(HttpURLConnection.HTTP_OK);


        final Team updatedTeam = TeamResponseParser.retireUser(retireResponse);

        assertThat(updatedTeam.getUserIds())
                .as("User ID for retired user should not be listed as active user for team")
                .doesNotContain(userToRetireId)
                .contains(captainUserId);

        assertThat(updatedTeam.getRetiredUserIds())
                .as("Retired user IDs should not be empty")
                .isNotEmpty();
    }

    @Test
    public void whenUnRetiringAUserToATeam_givenTheTeamIsTheOriginalTeamOfTheUser_thenTeamActiveAndRetiredUsersAreUpdated_andResponseHasA200Status() throws FoldingRestException {
        final User captainUser = User.createWithoutId("Dummy_User15", "Dummy User15", "DummyPasskey15", Category.NVIDIA_GPU, 1, "", false);
        final User userToRetired = User.createWithoutId("Dummy_User16", "Dummy User16", "DummyPasskey16", Category.AMD_GPU, 1, "", false);
        StubbedFoldingEndpointUtils.enableUser(captainUser);
        StubbedFoldingEndpointUtils.enableUser(userToRetired);

        final int captainUserId = UserUtils.createOrConflict(captainUser).getId();
        final int userToRetireId = UserUtils.createOrConflict(userToRetired).getId();

        final Team team = Team.createWithoutId("Dummy_Team13", "Dummy Team", captainUserId, Set.of(captainUserId, userToRetireId), Collections.emptySet());
        final int teamId = TeamUtils.createOrConflict(team).getId();
        final int retiredUserId = TeamUtils.retireUser(teamId, userToRetireId);

        final HttpResponse<String> unretireResponse = TEAM_REQUEST_SENDER.unretireUser(teamId, retiredUserId);
        assertThat(unretireResponse.statusCode())
                .as("Did not receive a 200_OK HTTP response: " + unretireResponse.body())
                .isEqualTo(HttpURLConnection.HTTP_OK);

        final Team updatedTeam = TeamResponseParser.unretireUser(unretireResponse);

        assertThat(updatedTeam.getUserIds())
                .as("User IDs should contain previously retired user ID")
                .contains(captainUserId, userToRetireId);

        assertThat(updatedTeam.getRetiredUserIds())
                .as("Retired user IDs should be empty")
                .isEmpty();
    }

    @Test
    public void whenUnRetiringAUserToATeam_givenTheTeamIsNotTheOriginalTeamOfTheUser_thenNewTeamActiveUsersAreUpdated_andOriginalTeamRetiredUsersAreNotUpdated_andResponseHasA200Status() throws FoldingRestException {
        final User captainUser = User.createWithoutId("Dummy_User5", "Dummy User5", "DummyPasskey5", Category.NVIDIA_GPU, 1, "", false);
        final User userToRetired = User.createWithoutId("Dummy_User6", "Dummy User6", "DummyPasskey6", Category.AMD_GPU, 1, "", false);
        StubbedFoldingEndpointUtils.enableUser(captainUser);
        StubbedFoldingEndpointUtils.enableUser(userToRetired);

        final int captainUserId = UserUtils.createOrConflict(captainUser).getId();
        final int userToRetireId = UserUtils.createOrConflict(userToRetired).getId();
        final Team originalTeam = Team.createWithoutId("Dummy_Team14", "Dummy Team", captainUserId, Set.of(captainUserId, userToRetireId), Collections.emptySet());
        final int originalTeamId = TeamUtils.createOrConflict(originalTeam).getId();

        final int retiredUserId = TeamUtils.retireUser(originalTeamId, userToRetireId);

        final User secondCaptainUser = User.createWithoutId("Dummy_User7", "Dummy User7", "DummyPasskey7", Category.NVIDIA_GPU, 1, "", false);
        StubbedFoldingEndpointUtils.enableUser(secondCaptainUser);
        final int secondCaptainUserId = UserUtils.createOrConflict(secondCaptainUser).getId();
        final Team newTeam = Team.createWithoutId("Dummy_Team15", "Dummy Team", secondCaptainUserId, Set.of(secondCaptainUserId), Collections.emptySet());
        final int newTeamId = TeamUtils.createOrConflict(newTeam).getId();

        final Team updatedNewTeam = TeamUtils.unretireUser(newTeamId, retiredUserId);
        assertThat(updatedNewTeam.getUserIds())
                .as("New team user IDs should contain previously retired user ID")
                .contains(secondCaptainUserId, userToRetireId);

        assertThat(updatedNewTeam.getRetiredUserIds())
                .as("New team should have no retired user IDs")
                .isEmpty();

        final Team originalTeamAfterUnretire = TeamUtils.get(originalTeamId);

        assertThat(originalTeamAfterUnretire.getUserIds())
                .as("Original team user IDs should only contain captain")
                .contains(captainUserId);

        assertThat(originalTeamAfterUnretire.getRetiredUserIds())
                .as("Original team should still have retried user ID")
                .isNotEmpty();
    }

    // Negative/alternative test cases

    @Test
    public void whenCreatingTeam_givenATeamWithInvalidCaptainUserId_thenJsonResponseWithErrorIsReturned_andHasA400Status() throws FoldingRestException {
        final Team team = Team.createWithoutId("Dummy_Team5", "Dummy Team", 0, Set.of(1), Collections.emptySet());
        final HttpResponse<String> response = TEAM_REQUEST_SENDER.create(team);

        assertThat(response.statusCode())
                .as("Did not receive a 400_BAD_REQUEST HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);

        assertThat(response.body())
                .as("Did not receive expected error message in response")
                .contains("captainUserId");
    }

    @Test
    public void whenCreatingTeam_givenTeamWithTheNameAlreadyExists_thenA409ResponseIsReturned() throws FoldingRestException {
        TEAM_REQUEST_SENDER.create(DUMMY_TEAM); // Send one request and ignore it (even if the user already exists, we can verify the conflict with the next one)
        final HttpResponse<String> response = TEAM_REQUEST_SENDER.create(DUMMY_TEAM);

        assertThat(response.statusCode())
                .as("Did not receive a 409_CONFLICT HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_CONFLICT);
    }

    @Test
    public void whenGettingTeam_givenANonExistingTeamId_thenNoJsonResponseIsReturned_andHasA404Status() throws FoldingRestException {
        final int invalidId = 99;
        final HttpResponse<String> response = TEAM_REQUEST_SENDER.get(invalidId);

        assertThat(response.statusCode())
                .as("Did not receive a 404_NOT_FOUND HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_NOT_FOUND);

        assertThat(response.body())
                .as("Did not receive an empty JSON response: " + response.body())
                .isEmpty();
    }

    @Test
    public void whenUpdatingTeam_givenANonExistingTeamId_thenNoJsonResponseIsReturned_andHasA404Status() throws FoldingRestException {
        final int invalidId = 99;
        final Team updatedTeam = Team.create(invalidId, "Dummy_Team6", "Dummy Team", 1, Set.of(1), Collections.emptySet());

        final HttpResponse<String> response = TEAM_REQUEST_SENDER.update(updatedTeam);
        assertThat(response.statusCode())
                .as("Did not receive a 404_NOT_FOUND HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_NOT_FOUND);

        assertThat(response.body())
                .as("Did not receive an empty JSON response: " + response.body())
                .isEmpty();
    }

    @Test
    public void whenDeletingTeam_givenANonExistingTeamId_thenResponseHasA404Status() throws FoldingRestException {
        final int invalidId = 99;
        final HttpResponse<Void> response = TEAM_REQUEST_SENDER.delete(invalidId);

        assertThat(response.statusCode())
                .as("Did not receive a 404_NOT_FOUND HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_NOT_FOUND);
    }

    @Test
    public void whenUpdatingTeam_givenAValidTeamId_andPayloadHasNoChanges_thenOriginalTeamIsReturned_andHasA200Status() throws FoldingRestException {
        final Team team = Team.createWithoutId("Dummy_Team6", "Dummy Team", 1, Set.of(1), Collections.emptySet());

        final int createdTeamId = TeamUtils.createOrConflict(team).getId();
        final Team teamWithId = Team.updateWithId(createdTeamId, team);

        final HttpResponse<String> updateResponse = TEAM_REQUEST_SENDER.update(teamWithId);

        assertThat(updateResponse.statusCode())
                .as("Did not receive a 200_OK HTTP response: " + updateResponse.body())
                .isEqualTo(HttpURLConnection.HTTP_OK);

        final Team actual = TeamResponseParser.update(updateResponse);

        assertThat(actual)
                .as("Did not receive the original team in response")
                .isEqualTo(teamWithId);
    }

    @Test
    public void whenCreatingBatchOfTeams_givenPayloadIsPartiallyValid_thenOnlyValidTeamsAreCreated_andResponseHasA200Status() throws FoldingRestException {
        final int initialTeamsSize = TeamUtils.getNumberOfTeams();

        final List<Team> batchOfValidTeams = List.of(
                Team.createWithoutId("Dummy_Team7", "Dummy Team", 1, Set.of(1), Collections.emptySet()),
                Team.createWithoutId("Dummy_Team8", "Dummy Team", 1, Set.of(1), Collections.emptySet())
        );
        final List<Team> batchOfInvalidTeams = List.of(
                Team.createWithoutId("Dummy_Team9", "Dummy Team", 0, Set.of(1), Collections.emptySet()),
                Team.createWithoutId("Dummy_Team10", "Dummy Team", 0, Set.of(1), Collections.emptySet())
        );
        final List<Team> batchOfTeams = new ArrayList<>(batchOfValidTeams.size() + batchOfInvalidTeams.size());
        batchOfTeams.addAll(batchOfValidTeams);
        batchOfTeams.addAll(batchOfInvalidTeams);


        final HttpResponse<String> response = TEAM_REQUEST_SENDER.createBatchOf(batchOfTeams);
        assertThat(response.statusCode())
                .as("Did not receive a 200_OK HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_OK);

        final int newTeamsSize = TeamUtils.getNumberOfTeams();
        assertThat(newTeamsSize)
                .as("Get all response did not return the initial teams + new valid teams")
                .isEqualTo(initialTeamsSize + batchOfValidTeams.size());
    }

    @Test
    public void whenCreatingBatchOfTeams_givenPayloadIsInvalid_thenResponseHasA400Status() throws FoldingRestException {
        final int initialTeamsSize = TeamUtils.getNumberOfTeams();

        final List<Team> batchOfInvalidTeams = List.of(
                Team.createWithoutId("Dummy_Team11", "Dummy Team", 0, Set.of(1), Collections.emptySet()),
                Team.createWithoutId("Dummy_Team12", "Dummy Team", 0, Set.of(1), Collections.emptySet())
        );

        final HttpResponse<String> response = TEAM_REQUEST_SENDER.createBatchOf(batchOfInvalidTeams);
        assertThat(response.statusCode())
                .as("Did not receive a 400_BAD_REQUEST HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);

        final int newHardwareSize = TeamUtils.getNumberOfTeams();
        assertThat(newHardwareSize)
                .as("Get all response did not return only the initial teams")
                .isEqualTo(initialTeamsSize);
    }

    @Test
    public void whenCreatingTeam_givenUsersExceedingPermittedAmountForACategory_thenTeamIsNotCreated_andResponseHasA400Status() throws FoldingRestException {
        final User firstUser = User.createWithoutId("Dummy_User17", "Dummy User17", "DummyPasskey17", Category.NVIDIA_GPU, 1, "", false);
        final User secondUser = User.createWithoutId("Dummy_User18", "Dummy User18", "DummyPasskey18", Category.NVIDIA_GPU, 1, "", false);
        StubbedFoldingEndpointUtils.enableUser(firstUser);
        StubbedFoldingEndpointUtils.enableUser(secondUser);

        final int firstUserId = UserUtils.createOrConflict(firstUser).getId();
        final int secondUserId = UserUtils.createOrConflict(secondUser).getId();

        final Team team = Team.createWithoutId("Dummy_Team13", "Dummy Team", firstUserId, Set.of(firstUserId, secondUserId), Collections.emptySet());

        final HttpResponse<String> response = TEAM_REQUEST_SENDER.create(team);
        assertThat(response.statusCode())
                .as("Did not receive a 400_BAD_REQUEST HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);

        assertThat(response.body())
                .as("Did not receive an error message specifying too many users for a specific category")
                .contains("category " + Category.NVIDIA_GPU.displayName());
    }

    @Test
    public void whenCreatingTeam_givenUsersExceedingTotalPermittedAmountForATeam_thenTeamIsNotCreated_andResponseHasA400Status() throws FoldingRestException {
        final User firstUser = User.createWithoutId("Dummy_User3", "Dummy User3", "DummyPasskey3", Category.NVIDIA_GPU, 1, "", false);
        final User secondUser = User.createWithoutId("Dummy_User4", "Dummy User4", "DummyPasskey4", Category.WILDCARD, 1, "", false);
        final User thirdUser = User.createWithoutId("Dummy_User5", "Dummy User5", "DummyPasskey4", Category.AMD_GPU, 1, "", false);
        final User fourthUser = User.createWithoutId("Dummy_User6", "Dummy User6", "DummyPasskey4", Category.NVIDIA_GPU, 1, "", false);
        StubbedFoldingEndpointUtils.enableUser(firstUser);
        StubbedFoldingEndpointUtils.enableUser(secondUser);
        StubbedFoldingEndpointUtils.enableUser(thirdUser);
        StubbedFoldingEndpointUtils.enableUser(fourthUser);

        final int firstUserId = UserUtils.createOrConflict(firstUser).getId();
        final int secondUserId = UserUtils.createOrConflict(secondUser).getId();
        final int thirdUserId = UserUtils.createOrConflict(thirdUser).getId();
        final int fourthUserId = UserUtils.createOrConflict(fourthUser).getId();

        final Team team = Team.createWithoutId("Dummy_Team13", "Dummy Team", firstUserId, Set.of(firstUserId, secondUserId, thirdUserId, fourthUserId), Collections.emptySet());

        final HttpResponse<String> response = TEAM_REQUEST_SENDER.create(team);
        assertThat(response.statusCode())
                .as("Did not receive a 400_BAD_REQUEST HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);

        assertThat(response.body())
                .as("Did not receive an error message specifying the team is too large")
                .contains("maximum permitted");
    }


    @Test
    public void whenRetiringAUserFromATeam_givenTheUserIsTheCaptain_thenUserCannotBeRetired_andResponseHasA400Status() throws FoldingRestException {
        final User captainUser = User.createWithoutId("Dummy_User8", "Dummy User8", "DummyPasskey8", Category.NVIDIA_GPU, 1, "", false);
        final User userToRetired = User.createWithoutId("Dummy_User9", "Dummy User9", "DummyPasskey9", Category.AMD_GPU, 1, "", false);
        StubbedFoldingEndpointUtils.enableUser(captainUser);
        StubbedFoldingEndpointUtils.enableUser(userToRetired);

        final int captainUserId = UserUtils.createOrConflict(captainUser).getId();
        final int userToRetireId = UserUtils.createOrConflict(userToRetired).getId();

        final Team team = Team.createWithoutId("Dummy_Team16", "Dummy Team", captainUserId, Set.of(captainUserId, userToRetireId), Collections.emptySet());

        final int teamId = TeamUtils.createOrConflict(team).getId();

        final HttpResponse<String> retireResponse = TEAM_REQUEST_SENDER.retireUser(teamId, captainUserId);
        assertThat(retireResponse.statusCode())
                .as("Did not receive a 400_BAD_REQUEST HTTP response: " + retireResponse.body())
                .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);
    }

    @Test
    public void whenRetiringAUserFromATeam_givenTheTeamIdIsInvalid_thenResponseHasA404Status() throws FoldingRestException {
        final int invalidTeamId = 99;
        final HttpResponse<String> response = TEAM_REQUEST_SENDER.retireUser(invalidTeamId, 1);
        assertThat(response.statusCode())
                .as("Did not receive a 404_NOT_FOUND HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_NOT_FOUND);
    }

    @Test
    public void whenRetiringAUserFromATeam_givenTheUserIdIsInvalid_thenResponseHasA400Status() throws FoldingRestException {
        final Team team = Team.createWithoutId("Dummy_Team17", "Dummy Team", 1, Set.of(1), Collections.emptySet());

        final int teamId = TeamUtils.createOrConflict(team).getId();
        final int invalidUserId = 99;

        final HttpResponse<String> retireResponse = TEAM_REQUEST_SENDER.retireUser(teamId, invalidUserId);
        assertThat(retireResponse.statusCode())
                .as("Did not receive a 400_BAD_REQUEST HTTP response: " + retireResponse.body())
                .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);
    }

    @Test
    public void whenUnRetiringAUserFromATeam_givenTheTeamIdIsInvalid_thenResponseHasA404Status() throws FoldingRestException {
        final int invalidTeamId = 99;
        final HttpResponse<String> response = TEAM_REQUEST_SENDER.unretireUser(invalidTeamId, 1);
        assertThat(response.statusCode())
                .as("Did not receive a 404_NOT_FOUND HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_NOT_FOUND);
    }

    @Test
    public void whenUnRetiringAUserFromATeam_givenTheUserIdIsInvalid_thenResponseHasA400Status() throws FoldingRestException {
        final Team team = Team.createWithoutId("Dummy_Team18", "Dummy Team", 1, Set.of(1), Collections.emptySet());

        final int teamId = TeamUtils.createOrConflict(team).getId();
        final int invalidUserId = 99;

        final HttpResponse<String> retireResponse = TEAM_REQUEST_SENDER.unretireUser(teamId, invalidUserId);
        assertThat(retireResponse.statusCode())
                .as("Did not receive a 400_BAD_REQUEST HTTP response: " + retireResponse.body())
                .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);
    }

    @Test
    public void whenUnRetiringAUserToATeam_givenTheTeamIsNotTheOriginalTeamOfTheUser_andTheNewTeamIsFull_thenTheUnRetirementShouldFail_andResponseHasA400Status() throws FoldingRestException {
        final User captainUser = User.createWithoutId("Dummy_User10", "Dummy User10", "DummyPasskey10", Category.NVIDIA_GPU, 1, "", false);
        final User userToRetired = User.createWithoutId("Dummy_User11", "Dummy User11", "DummyPasskey11", Category.AMD_GPU, 1, "", false);
        StubbedFoldingEndpointUtils.enableUser(captainUser);
        StubbedFoldingEndpointUtils.enableUser(userToRetired);

        final int captainUserId = UserUtils.createOrConflict(captainUser).getId();
        final int userToRetireId = UserUtils.createOrConflict(userToRetired).getId();
        final Team originalTeam = Team.createWithoutId("Dummy_Team18", "Dummy Team", captainUserId, Set.of(captainUserId, userToRetireId), Collections.emptySet());
        final int originalTeamId = TeamUtils.createOrConflict(originalTeam).getId();

        final int retiredUserId = TeamUtils.retireUser(originalTeamId, userToRetireId);

        final User secondCaptainUser = User.createWithoutId("Dummy_User12", "Dummy User12", "DummyPasskey12", Category.WILDCARD, 1, "", false);
        final User secondNVidiaUser = User.createWithoutId("Dummy_User13", "Dummy User13", "DummyPasskey13", Category.NVIDIA_GPU, 1, "", false);
        final User secondAmdUser = User.createWithoutId("Dummy_User14", "Dummy User14", "DummyPasskey14", Category.AMD_GPU, 1, "", false);
        StubbedFoldingEndpointUtils.enableUser(secondCaptainUser);
        StubbedFoldingEndpointUtils.enableUser(secondNVidiaUser);
        StubbedFoldingEndpointUtils.enableUser(secondAmdUser);
        final int secondCaptainUserId = UserUtils.createOrConflict(secondCaptainUser).getId();
        final int secondNVidiaUserId = UserUtils.createOrConflict(secondNVidiaUser).getId();
        final int secondAmdUserId = UserUtils.createOrConflict(secondAmdUser).getId();

        final Team newTeam = Team.createWithoutId("Dummy_Team19", "Dummy Team", secondCaptainUserId, Set.of(secondCaptainUserId, secondNVidiaUserId, secondAmdUserId), Collections.emptySet());
        final int newTeamId = TeamUtils.createOrConflict(newTeam).getId();

        final HttpResponse<String> unretireResponse = TEAM_REQUEST_SENDER.unretireUser(newTeamId, retiredUserId);
        assertThat(unretireResponse.statusCode())
                .as("Did not receive a 400_BAD_REQUEST HTTP response: " + unretireResponse.body())
                .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);
    }

    @AfterAll
    public static void tearDown() throws FoldingRestException {
        cleanSystemForSimpleTests();
    }
}

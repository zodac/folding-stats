package me.zodac.folding.test;

import me.zodac.folding.api.tc.Category;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.client.java.response.TeamResponseParser;
import me.zodac.folding.rest.api.exception.FoldingRestException;
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
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static me.zodac.folding.api.utils.EncodingUtils.encodeBasicAuthentication;
import static me.zodac.folding.test.utils.SystemCleaner.cleanSystemForSimpleTests;
import static me.zodac.folding.test.utils.TestAuthenticationData.ADMIN_USER;
import static me.zodac.folding.test.utils.TestAuthenticationData.INVALID_PASSWORD;
import static me.zodac.folding.test.utils.TestAuthenticationData.INVALID_USERNAME;
import static me.zodac.folding.test.utils.TestAuthenticationData.READ_ONLY_USER;
import static me.zodac.folding.test.utils.TestConstants.FOLDING_URL;
import static me.zodac.folding.test.utils.TestConstants.HTTP_CLIENT;
import static me.zodac.folding.test.utils.TestGenerator.generateTeam;
import static me.zodac.folding.test.utils.TestGenerator.generateTeamWithId;
import static me.zodac.folding.test.utils.TestGenerator.generateTeamWithUserIds;
import static me.zodac.folding.test.utils.TestGenerator.generateUser;
import static me.zodac.folding.test.utils.TestGenerator.generateUserWithCategory;
import static me.zodac.folding.test.utils.rest.request.TeamUtils.TEAM_REQUEST_SENDER;
import static me.zodac.folding.test.utils.rest.request.TeamUtils.createOrConflict;
import static me.zodac.folding.test.utils.rest.response.HttpResponseHeaderUtils.getETag;
import static me.zodac.folding.test.utils.rest.response.HttpResponseHeaderUtils.getXTotalCount;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the {@link Team} REST endpoint at <code>/folding/teams</code>.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TeamTest {

    @BeforeAll
    public static void setUp() throws FoldingRestException {
        cleanSystemForSimpleTests();
    }

    @Test
    @Order(1)
    public void whenGettingAllTeams_givenNoTeamHasBeenCreated_thenAnEmptyJsonResponseIsReturned_andHasA200Status() throws FoldingRestException {
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
    public void whenCreatingTeam_givenPayloadIsValid_thenTheCreatedTeamIsReturnedInResponse_andHasId_andResponseHasA201StatusCode() throws FoldingRestException {
        final Team teamToCreate = generateTeam();

        final HttpResponse<String> response = TEAM_REQUEST_SENDER.create(teamToCreate, ADMIN_USER.userName(), ADMIN_USER.password());
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
    public void whenGettingTeam_givenAValidTeamId_thenTeamIsReturned_andHasA200Status() throws FoldingRestException {
        final int teamId = TeamUtils.createOrConflict(generateTeam()).getId();

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
    public void whenUpdatingTeam_givenAValidTeamId_andAValidPayload_thenUpdatedTeamIsReturned_andNoNewTeamIsCreated_andHasA200Status() throws FoldingRestException {
        final int teamId = TeamUtils.createOrConflict(generateTeam()).getId();
        final int initialSize = TeamUtils.getNumberOfTeams();

        final Team updatedTeam = Team.updateWithId(teamId, TeamUtils.get(teamId));
        updatedTeam.setTeamDescription("Updated description");

        final HttpResponse<String> response = TEAM_REQUEST_SENDER.update(updatedTeam, ADMIN_USER.userName(), ADMIN_USER.password());
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
                .isEqualTo(initialSize);
    }

    @Test
    public void whenDeletingTeam_givenAValidTeamId_thenTeamIsDeleted_andHasA200Status_andTeamCountIsReduced_andTeamCannotBeRetrievedAgain() throws FoldingRestException {
        final int teamId = TeamUtils.createOrConflict(generateTeam()).getId();
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
    public void whenRetiringAUserFromATeam_givenValidUserId_thenTeamActiveAndRetiredUsersAreUpdated_andResponseHasA200Status() throws FoldingRestException {
        final User captainUser = generateUserWithCategory(Category.NVIDIA_GPU);
        final User userToRetire = generateUserWithCategory(Category.AMD_GPU);
        final int captainUserId = UserUtils.createOrConflict(captainUser).getId();
        final int userToRetireId = UserUtils.createOrConflict(userToRetire).getId();

        final Team team = generateTeamWithUserIds(captainUserId, userToRetireId);
        final int teamId = TeamUtils.createOrConflict(team).getId();

        final HttpResponse<String> retireResponse = TEAM_REQUEST_SENDER.retireUser(teamId, userToRetireId, ADMIN_USER.userName(), ADMIN_USER.password());
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
        final User captainUser = generateUserWithCategory(Category.NVIDIA_GPU);
        final User userToRetire = generateUserWithCategory(Category.AMD_GPU);
        final int captainUserId = UserUtils.createOrConflict(captainUser).getId();
        final int userToRetireId = UserUtils.createOrConflict(userToRetire).getId();

        final Team team = generateTeamWithUserIds(captainUserId, userToRetireId);
        final int teamId = TeamUtils.createOrConflict(team).getId();

        final int retiredUserId = TeamUtils.retireUser(teamId, userToRetireId);

        final HttpResponse<String> unretireResponse = TEAM_REQUEST_SENDER.unretireUser(teamId, retiredUserId, ADMIN_USER.userName(), ADMIN_USER.password());
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
        final User captainUser = generateUserWithCategory(Category.NVIDIA_GPU);
        final User userToRetire = generateUserWithCategory(Category.AMD_GPU);
        final int captainUserId = UserUtils.createOrConflict(captainUser).getId();
        final int userToRetireId = UserUtils.createOrConflict(userToRetire).getId();

        final Team originalTeam = generateTeamWithUserIds(captainUserId, userToRetireId);
        final int originalTeamId = TeamUtils.createOrConflict(originalTeam).getId();

        final int retiredUserId = TeamUtils.retireUser(originalTeamId, userToRetireId);

        final User secondCaptainUser = generateUser();
        final int secondCaptainUserId = UserUtils.createOrConflict(secondCaptainUser).getId();
        final Team newTeam = generateTeamWithUserIds(secondCaptainUserId);
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
                .as("Original team user IDs should only contain captain: " + originalTeamAfterUnretire)
                .contains(captainUserId);

        assertThat(originalTeamAfterUnretire.getRetiredUserIds())
                .as("Original team should still have retried user ID: " + originalTeamAfterUnretire)
                .isNotEmpty();
    }

    // Negative/alternative test cases

    @Test
    public void whenCreatingTeam_givenATeamWithInvalidCaptainUserId_thenJsonResponseWithErrorIsReturned_andHasA400Status() throws FoldingRestException {
        final Team team = generateTeamWithUserIds(0);
        final HttpResponse<String> response = TEAM_REQUEST_SENDER.create(team, ADMIN_USER.userName(), ADMIN_USER.password());

        assertThat(response.statusCode())
                .as("Did not receive a 400_BAD_REQUEST HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);

        assertThat(response.body())
                .as("Did not receive expected error message in response")
                .contains("captainUserId");
    }

    @Test
    public void whenCreatingTeam_givenTeamWithTheNameAlreadyExists_thenA409ResponseIsReturned() throws FoldingRestException {
        final Team teamToCreate = generateTeam();
        final Team teamWithSameName = generateTeam();
        teamWithSameName.setTeamName(teamToCreate.getTeamName());

        TEAM_REQUEST_SENDER.create(teamToCreate, ADMIN_USER.userName(), ADMIN_USER.password());
        final HttpResponse<String> response = TEAM_REQUEST_SENDER.create(teamWithSameName, ADMIN_USER.userName(), ADMIN_USER.password());

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
        final Team updatedTeam = generateTeamWithId(invalidId);

        final HttpResponse<String> response = TEAM_REQUEST_SENDER.update(updatedTeam, ADMIN_USER.userName(), ADMIN_USER.password());
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
        final HttpResponse<Void> response = TEAM_REQUEST_SENDER.delete(invalidId, ADMIN_USER.userName(), ADMIN_USER.password());

        assertThat(response.statusCode())
                .as("Did not receive a 404_NOT_FOUND HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_NOT_FOUND);
    }

    @Test
    public void whenUpdatingTeam_givenAValidTeamId_andPayloadHasNoChanges_thenOriginalTeamIsReturned_andHasA200Status() throws FoldingRestException {
        final Team team = generateTeam();
        final int createdTeamId = createOrConflict(team).getId();
        final Team teamWithId = Team.updateWithId(createdTeamId, team);

        final HttpResponse<String> updateResponse = TEAM_REQUEST_SENDER.update(teamWithId, ADMIN_USER.userName(), ADMIN_USER.password());

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
                generateTeam(),
                generateTeam()
        );
        final List<Team> batchOfInvalidTeams = List.of(
                generateTeamWithUserIds(0),
                generateTeamWithUserIds(0)
        );
        final List<Team> batchOfTeams = new ArrayList<>(batchOfValidTeams.size() + batchOfInvalidTeams.size());
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
    public void whenCreatingBatchOfTeams_givenPayloadIsInvalid_thenResponseHasA400Status() throws FoldingRestException {
        final int initialTeamsSize = TeamUtils.getNumberOfTeams();

        final List<Team> batchOfInvalidTeams = List.of(
                generateTeamWithUserIds(0),
                generateTeamWithUserIds(0)
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
    public void whenCreatingTeam_givenUsersExceedingPermittedAmountForACategory_thenTeamIsNotCreated_andResponseHasA400Status() throws FoldingRestException {
        final User firstUser = generateUserWithCategory(Category.NVIDIA_GPU);
        final User secondUser = generateUserWithCategory(Category.NVIDIA_GPU);
        final int firstUserId = UserUtils.createOrConflict(firstUser).getId();
        final int secondUserId = UserUtils.createOrConflict(secondUser).getId();

        final Team team = generateTeamWithUserIds(firstUserId, secondUserId);

        final HttpResponse<String> response = TEAM_REQUEST_SENDER.create(team, ADMIN_USER.userName(), ADMIN_USER.password());
        assertThat(response.statusCode())
                .as("Did not receive a 400_BAD_REQUEST HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);

        assertThat(response.body())
                .as("Did not receive an error message specifying too many users for a specific category")
                .contains("category " + Category.NVIDIA_GPU.displayName());
    }

    @Test
    public void whenCreatingTeam_givenUsersExceedingTotalPermittedAmountForATeam_thenTeamIsNotCreated_andResponseHasA400Status() throws FoldingRestException {
        final User firstUser = generateUserWithCategory(Category.NVIDIA_GPU);
        final User secondUser = generateUserWithCategory(Category.WILDCARD);
        final User thirdUser = generateUserWithCategory(Category.AMD_GPU);
        final User fourthUser = generateUserWithCategory(Category.NVIDIA_GPU);
        final int firstUserId = UserUtils.createOrConflict(firstUser).getId();
        final int secondUserId = UserUtils.createOrConflict(secondUser).getId();
        final int thirdUserId = UserUtils.createOrConflict(thirdUser).getId();
        final int fourthUserId = UserUtils.createOrConflict(fourthUser).getId();

        final Team team = generateTeamWithUserIds(firstUserId, secondUserId, thirdUserId, fourthUserId);

        final HttpResponse<String> response = TEAM_REQUEST_SENDER.create(team, ADMIN_USER.userName(), ADMIN_USER.password());
        assertThat(response.statusCode())
                .as("Did not receive a 400_BAD_REQUEST HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);

        assertThat(response.body())
                .as("Did not receive an error message specifying the team is too large")
                .contains("maximum permitted");
    }


    @Test
    public void whenRetiringAUserFromATeam_givenTheUserIsTheCaptain_thenUserCannotBeRetired_andResponseHasA400Status() throws FoldingRestException {
        final User captainUser = generateUserWithCategory(Category.NVIDIA_GPU);
        final User userToRetire = generateUserWithCategory(Category.AMD_GPU);
        final int captainUserId = UserUtils.createOrConflict(captainUser).getId();
        final int userToRetireId = UserUtils.createOrConflict(userToRetire).getId();

        final Team team = generateTeamWithUserIds(captainUserId, userToRetireId);
        final int teamId = TeamUtils.createOrConflict(team).getId();

        final HttpResponse<String> retireResponse = TEAM_REQUEST_SENDER.retireUser(teamId, captainUserId, ADMIN_USER.userName(), ADMIN_USER.password());
        assertThat(retireResponse.statusCode())
                .as("Did not receive a 400_BAD_REQUEST HTTP response: " + retireResponse.body())
                .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);
    }

    @Test
    public void whenRetiringAUserFromATeam_givenTheTeamIdIsInvalid_thenResponseHasA404Status() throws FoldingRestException {
        final int invalidTeamId = 99;
        final HttpResponse<String> response = TEAM_REQUEST_SENDER.retireUser(invalidTeamId, 1, ADMIN_USER.userName(), ADMIN_USER.password());
        assertThat(response.statusCode())
                .as("Did not receive a 404_NOT_FOUND HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_NOT_FOUND);
    }

    @Test
    public void whenRetiringAUserFromATeam_givenTheUserIdIsInvalid_thenResponseHasA400Status() throws FoldingRestException {
        final Team team = generateTeam();
        final int teamId = TeamUtils.createOrConflict(team).getId();
        final int invalidUserId = 99;

        final HttpResponse<String> retireResponse = TEAM_REQUEST_SENDER.retireUser(teamId, invalidUserId, ADMIN_USER.userName(), ADMIN_USER.password());
        assertThat(retireResponse.statusCode())
                .as("Did not receive a 400_BAD_REQUEST HTTP response: " + retireResponse.body())
                .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);
    }

    @Test
    public void whenUnRetiringAUserFromATeam_givenTheTeamIdIsInvalid_thenResponseHasA404Status() throws FoldingRestException {
        final int invalidTeamId = 99;
        final HttpResponse<String> response = TEAM_REQUEST_SENDER.unretireUser(invalidTeamId, 1, ADMIN_USER.userName(), ADMIN_USER.password());
        assertThat(response.statusCode())
                .as("Did not receive a 404_NOT_FOUND HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_NOT_FOUND);
    }

    @Test
    public void whenUnRetiringAUserFromATeam_givenTheUserIdIsInvalid_thenResponseHasA400Status() throws FoldingRestException {
        final Team team = generateTeam();
        final int teamId = TeamUtils.createOrConflict(team).getId();
        final int invalidUserId = 99;

        final HttpResponse<String> retireResponse = TEAM_REQUEST_SENDER.unretireUser(teamId, invalidUserId, ADMIN_USER.userName(), ADMIN_USER.password());
        assertThat(retireResponse.statusCode())
                .as("Did not receive a 400_BAD_REQUEST HTTP response: " + retireResponse.body())
                .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);
    }

    @Test
    public void whenUnRetiringAUserToATeam_givenTheTeamIsNotTheOriginalTeamOfTheUser_andTheNewTeamIsFull_thenTheUnRetirementShouldFail_andResponseHasA400Status() throws FoldingRestException {
        final User captainUser = generateUserWithCategory(Category.NVIDIA_GPU);
        final User userToRetire = generateUserWithCategory(Category.AMD_GPU);
        final int captainUserId = UserUtils.createOrConflict(captainUser).getId();
        final int userToRetireId = UserUtils.createOrConflict(userToRetire).getId();

        final Team originalTeam = generateTeamWithUserIds(captainUserId, userToRetireId);
        final int originalTeamId = TeamUtils.createOrConflict(originalTeam).getId();

        final int retiredUserId = TeamUtils.retireUser(originalTeamId, userToRetireId);

        final User secondTeamCaptain = generateUserWithCategory(Category.NVIDIA_GPU);
        final User secondTeamFirstUser = generateUserWithCategory(Category.AMD_GPU);
        final User secondTeamSecondUser = generateUserWithCategory(Category.WILDCARD);
        final int secondTeamCaptainId = UserUtils.createOrConflict(secondTeamCaptain).getId();
        final int secondTeamFirstUserId = UserUtils.createOrConflict(secondTeamFirstUser).getId();
        final int secondTeamSecondUserId = UserUtils.createOrConflict(secondTeamSecondUser).getId();

        final Team newTeam = generateTeamWithUserIds(secondTeamCaptainId, secondTeamFirstUserId, secondTeamSecondUserId);
        final int newTeamId = TeamUtils.createOrConflict(newTeam).getId();

        final HttpResponse<String> unretireResponse = TEAM_REQUEST_SENDER.unretireUser(newTeamId, retiredUserId, ADMIN_USER.userName(), ADMIN_USER.password());
        assertThat(unretireResponse.statusCode())
                .as("Did not receive a 400_BAD_REQUEST HTTP response: " + unretireResponse.body())
                .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);
    }

    @Test
    public void whenGettingTeamById_givenRequestUsesPreviousETag_andTeamHasNotChanged_thenResponseHasA304Status_andNoBody() throws FoldingRestException {
        final int teamId = TeamUtils.createOrConflict(generateTeam()).getId();

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
    public void whenGettingAllTeams_givenRequestUsesPreviousETag_andTeamsHaveNotChanged_thenResponseHasA304Status_andNoBody() throws FoldingRestException {
        TeamUtils.createOrConflict(generateTeam());

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
    public void whenCreatingTeam_givenNoAuthentication_thenRequestFails_andResponseHasA401StatusCode() throws FoldingRestException {
        final Team teamToCreate = generateTeam();

        final HttpResponse<String> response = TEAM_REQUEST_SENDER.create(teamToCreate);
        assertThat(response.statusCode())
                .as("Did not receive a 401_UNAUTHORIZED HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_UNAUTHORIZED);
    }

    @Test
    public void whenCreatingBatchOfTeams_givenNoAuthentication_thenRequestFails_andResponseHasA401StatusCode() throws FoldingRestException {
        final List<Team> batchOfTeams = List.of(
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
    public void whenUpdatingTeam_givenNoAuthentication_thenRequestFails_andResponseHasA401StatusCode() throws FoldingRestException {
        final int teamId = TeamUtils.createOrConflict(generateTeam()).getId();

        final Team updatedTeam = Team.updateWithId(teamId, TeamUtils.get(teamId));
        updatedTeam.setTeamDescription("Updated description");

        final HttpResponse<String> response = TEAM_REQUEST_SENDER.update(updatedTeam);
        assertThat(response.statusCode())
                .as("Did not receive a 401_UNAUTHORIZED HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_UNAUTHORIZED);
    }

    @Test
    public void whenDeletingTeam_givenNoAuthentication_thenRequestFails_andResponseHasA401StatusCode() throws FoldingRestException {
        final int teamId = TeamUtils.createOrConflict(generateTeam()).getId();

        final HttpResponse<Void> response = TEAM_REQUEST_SENDER.delete(teamId);
        assertThat(response.statusCode())
                .as("Did not receive a 401_UNAUTHORIZED HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_UNAUTHORIZED);
    }

    @Test
    public void whenRetiringAUserFromATeam_givenNoAuthentication_thenRequestFails_andResponseHasA401StatusCode() throws FoldingRestException {
        final User captainUser = generateUserWithCategory(Category.NVIDIA_GPU);
        final User userToRetire = generateUserWithCategory(Category.AMD_GPU);
        final int captainUserId = UserUtils.createOrConflict(captainUser).getId();
        final int userToRetireId = UserUtils.createOrConflict(userToRetire).getId();

        final Team team = generateTeamWithUserIds(captainUserId, userToRetireId);
        final int teamId = TeamUtils.createOrConflict(team).getId();

        final HttpResponse<String> response = TEAM_REQUEST_SENDER.retireUser(teamId, userToRetireId);
        assertThat(response.statusCode())
                .as("Did not receive a 401_UNAUTHORIZED HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_UNAUTHORIZED);
    }

    @Test
    public void whenUnRetiringAUserToATeam_givenNoAuthentication_thenRequestFails_andResponseHasA401StatusCode() throws FoldingRestException {
        final User captainUser = generateUserWithCategory(Category.NVIDIA_GPU);
        final User userToRetire = generateUserWithCategory(Category.AMD_GPU);
        final int captainUserId = UserUtils.createOrConflict(captainUser).getId();
        final int userToRetireId = UserUtils.createOrConflict(userToRetire).getId();

        final Team team = generateTeamWithUserIds(captainUserId, userToRetireId);
        final int teamId = TeamUtils.createOrConflict(team).getId();

        final int retiredUserId = TeamUtils.retireUser(teamId, userToRetireId);

        final HttpResponse<String> response = TEAM_REQUEST_SENDER.unretireUser(teamId, retiredUserId);
        assertThat(response.statusCode())
                .as("Did not receive a 401_UNAUTHORIZED HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_UNAUTHORIZED);
    }

    @Test
    public void whenCreatingTeam_givenAuthentication_andAuthenticationHasInvalidUser_thenRequestFails_andResponseHasA401StatusCode() throws FoldingRestException {
        final Team teamToCreate = generateTeam();

        final HttpResponse<String> response = TEAM_REQUEST_SENDER.create(teamToCreate, INVALID_USERNAME.userName(), INVALID_USERNAME.password());
        assertThat(response.statusCode())
                .as("Did not receive a 401_UNAUTHORIZED HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_UNAUTHORIZED);
    }

    @Test
    public void whenCreatingTeam_givenAuthentication_andAuthenticationHasInvalidPassword_thenRequestFails_andResponseHasA401StatusCode() throws FoldingRestException {
        final Team teamToCreate = generateTeam();

        final HttpResponse<String> response = TEAM_REQUEST_SENDER.create(teamToCreate, INVALID_PASSWORD.userName(), INVALID_PASSWORD.password());
        assertThat(response.statusCode())
                .as("Did not receive a 401_UNAUTHORIZED HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_UNAUTHORIZED);
    }

    @Test
    public void whenCreatingTeam_givenAuthentication_andUserDoesNotHaveAdminRole_thenRequestFails_andResponseHasA403StatusCode() throws FoldingRestException {
        final Team teamToCreate = generateTeam();

        final HttpResponse<String> response = TEAM_REQUEST_SENDER.create(teamToCreate, READ_ONLY_USER.userName(), READ_ONLY_USER.password());
        assertThat(response.statusCode())
                .as("Did not receive a 403_FORBIDDEN HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_FORBIDDEN);
    }

    @Test
    public void whenCreatingTeam_givenEmptyPayload_thenRequestFails_andResponseHasA400StatusCode() throws IOException, InterruptedException {
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
    public void whenUpdatingTeam_givenEmptyPayload_thenRequestFails_andResponseHasA400StatusCode() throws FoldingRestException, IOException, InterruptedException {
        final int teamId = TeamUtils.createOrConflict(generateTeam()).getId();
        final Team updatedTeam = Team.updateWithId(teamId, TeamUtils.get(teamId));
        updatedTeam.setTeamDescription("Updated description");

        final HttpRequest request = HttpRequest.newBuilder()
                .PUT(HttpRequest.BodyPublishers.noBody())
                .uri(URI.create(FOLDING_URL + "/teams/" + updatedTeam.getId()))
                .header("Content-Type", "application/json")
                .header("Authorization", encodeBasicAuthentication(ADMIN_USER.userName(), ADMIN_USER.password()))
                .build();

        final HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode())
                .as("Did not receive a 400_BAD_REQUEST HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST);
    }

    @Test
    public void whenCreatingUser_andOptionalFieldIsEmptyString_thenValueShouldBeNullNotEmpty() throws FoldingRestException {
        final int userId = UserUtils.createOrConflict(generateUser()).getId();
        final Team teamToCreate = Team.createWithoutId(
                "Dummy_Team_create_null",
                "Dummy Team",
                "",
                userId,
                Set.of(userId),
                Collections.emptySet());

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
    public void whenUpdatingUser_andOptionalFieldIsEmptyString_thenValueShouldBeNullNotEmpty() throws FoldingRestException {
        final int userId = UserUtils.createOrConflict(generateUser()).getId();
        final Team team = Team.createWithoutId(
                "Dummy_Team_update_null",
                "Dummy Team",
                "http://google.com",
                userId,
                Set.of(userId),
                Collections.emptySet());

        final Team teamToUpdate = TeamUtils.createOrConflict(team);
        final int teamId = teamToUpdate.getId();
        teamToUpdate.setForumLink("");

        final HttpResponse<String> response = TEAM_REQUEST_SENDER.update(teamToUpdate, ADMIN_USER.userName(), ADMIN_USER.password());
        assertThat(response.statusCode())
                .as("Did not receive a 200_OK HTTP response: " + response.body())
                .isEqualTo(HttpURLConnection.HTTP_OK);

        final Team actual = TeamUtils.get(teamId);
        assertThat(actual)
                .as("Empty optional value should not be returned: " + response.body())
                .extracting("forumLink")
                .isEqualTo(null);
    }

    @AfterAll
    public static void tearDown() throws FoldingRestException {
        cleanSystemForSimpleTests();
    }
}

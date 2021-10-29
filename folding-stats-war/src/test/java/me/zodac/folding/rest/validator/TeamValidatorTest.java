package me.zodac.folding.rest.validator;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.rest.api.tc.request.TeamRequest;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link TeamValidator}.
 */
class TeamValidatorTest {

    @Test
    void whenValidatingCreate_givenValidTeam_thenSuccessResponseIsReturned() {
        final TeamRequest team = TeamRequest.builder()
            .teamName("teamName")
            .teamDescription("teamDescription")
            .forumLink("http://www.google.com")
            .build();

        final ValidationResult<Team> response = TeamValidator.validateCreate(team, Collections.emptyList());

        assertThat(response.isFailure())
            .as("Expected validation to pass, instead failed with errors: " + response.getErrors())
            .isFalse();
    }

    @Test
    void whenValidatingCreate_givenNullTeam_thenFailureResponseIsReturned() {
        final ValidationResult<Team> response = TeamValidator.validateCreate(null, Collections.emptyList());

        assertThat(response.isFailure())
            .isTrue();

        assertThat(response.getErrors())
            .containsOnly("Payload is null");
    }

    @Test
    void whenValidatingCreate_givenTeamWithNullName_thenFailureResponseIsReturned() {
        final TeamRequest team = TeamRequest.builder()
            .teamName(null)
            .teamDescription("teamDescription")
            .forumLink("http://www.google.com")
            .build();

        final ValidationResult<Team> response = TeamValidator.validateCreate(team, Collections.emptyList());

        assertThat(response.isFailure())
            .isTrue();

        assertThat(response.getErrors())
            .containsOnly("Field 'teamName' must not be empty");
    }

    @Test
    void whenValidatingCreate_givenOtherTeamAlreadyExists_thenSuccessResponseIsReturned() {
        final Collection<Team> allTeams = List.of(Team.builder()
            .teamName("anotherName")
            .teamDescription("teamDescription")
            .forumLink("http://www.google.com")
            .build()
        );

        final TeamRequest team = TeamRequest.builder()
            .teamName("existingName")
            .teamDescription("teamDescription")
            .forumLink("http://www.google.com")
            .build();

        final ValidationResult<Team> response = TeamValidator.validateCreate(team, allTeams);

        assertThat(response.isFailure())
            .isFalse();
    }

    @Test
    void whenValidatingCreate_givenTeamWithNameAlreadyExists_thenFailureResponseIsReturned() {
        final Collection<Team> allTeams = List.of(Team.builder()
            .teamName("existingName")
            .teamDescription("teamDescription")
            .forumLink("http://www.google.com")
            .build()
        );

        final TeamRequest team = TeamRequest.builder()
            .teamName("existingName")
            .teamDescription("teamDescription")
            .forumLink("http://www.google.com")
            .build();

        final ValidationResult<Team> response = TeamValidator.validateCreate(team, allTeams);

        assertThat(response.isFailure())
            .isTrue();

        assertThat(response.getErrors())
            .containsOnly("Payload conflicts with an existing object on: [teamName]");
    }

    @Test
    void whenValidatingCreate_givenTeamWithNullDescription_thenSuccessResponseIsReturned() {
        final TeamRequest team = TeamRequest.builder()
            .teamName("teamName")
            .teamDescription(null)
            .forumLink("http://www.google.com")
            .build();

        final ValidationResult<Team> response = TeamValidator.validateCreate(team, Collections.emptyList());

        assertThat(response.isFailure())
            .isFalse();
    }

    @Test
    void whenValidatingCreate_givenTeamWithNullForumLink_thenSuccessResponseIsReturned() {
        final TeamRequest team = TeamRequest.builder()
            .teamName("teamName")
            .teamDescription("teamDescription")
            .forumLink(null)
            .build();

        final ValidationResult<Team> response = TeamValidator.validateCreate(team, Collections.emptyList());

        assertThat(response.isFailure())
            .isFalse();
    }

    @Test
    void whenValidatingCreate_givenTeamWithForumLinkThatIsNotValidUrl_thenFailureResponseIsReturned() {
        final TeamRequest team = TeamRequest.builder()
            .teamName("teamName")
            .teamDescription("teamDescription")
            .forumLink("invalidUrl")
            .build();

        final ValidationResult<Team> response = TeamValidator.validateCreate(team, Collections.emptyList());

        assertThat(response.isFailure())
            .isTrue();

        assertThat(response.getErrors())
            .containsOnly("Field 'forumLink' is not a valid link: 'invalidUrl'");
    }

    @Test
    void whenValidatingUpdate_givenValidTeam_thenSuccessResponseIsReturned() {
        final TeamRequest team = TeamRequest.builder()
            .teamName("teamName")
            .teamDescription("teamDescription")
            .forumLink("http://www.google.com")
            .build();

        final Team existingTeam = Team.builder()
            .teamName("teamName")
            .teamDescription("teamDescription2")
            .forumLink("http://www.google.com")
            .build();

        final ValidationResult<Team> response = TeamValidator.validateUpdate(team, existingTeam, Collections.emptyList());

        assertThat(response.isFailure())
            .as("Expected validation to pass, instead failed with errors: " + response.getErrors())
            .isFalse();
    }

    @Test
    void whenValidatingUpdate_givenNullTeam_thenFailureResponseIsReturned() {
        final Team existingTeam = Team.builder()
            .teamName("teamName")
            .teamDescription("teamDescription2")
            .forumLink("http://www.google.com")
            .build();

        final ValidationResult<Team> response = TeamValidator.validateUpdate(null, existingTeam, Collections.emptyList());

        assertThat(response.isFailure())
            .isTrue();

        assertThat(response.getErrors())
            .containsOnly("Payload is null");
    }

    @Test
    void whenValidatingUpdate_givenNullExistingTeam_thenFailureResponseIsReturned() {
        final TeamRequest team = TeamRequest.builder()
            .teamName("teamName")
            .teamDescription("teamDescription")
            .forumLink("http://www.google.com")
            .build();

        final ValidationResult<Team> response = TeamValidator.validateUpdate(team, null, Collections.emptyList());

        assertThat(response.isFailure())
            .isTrue();

        assertThat(response.getErrors())
            .containsOnly("Payload is null");
    }

    @Test
    void whenValidatingUpdate_givenTeamWithNullName_thenFailureResponseIsReturned() {
        final TeamRequest team = TeamRequest.builder()
            .teamName(null)
            .teamDescription("teamDescription")
            .forumLink("http://www.google.com")
            .build();

        final Team existingTeam = Team.builder()
            .teamName("teamName")
            .teamDescription("teamDescription")
            .forumLink("http://www.google.com")
            .build();

        final ValidationResult<Team> response = TeamValidator.validateUpdate(team, existingTeam, Collections.emptyList());

        assertThat(response.isFailure())
            .isTrue();

        assertThat(response.getErrors())
            .containsOnly("Field 'teamName' must not be empty");
    }

    @Test
    void whenValidatingUpdate_givenTeamWithNameAlreadyExists_thenFailureResponseIsReturned() {
        final TeamRequest team = TeamRequest.builder()
            .teamName("teamName")
            .teamDescription("teamDescription")
            .forumLink("http://www.google.com")
            .build();

        final Team existingTeam = Team.builder()
            .id(1)
            .teamName("differentName")
            .teamDescription("teamDescription")
            .forumLink("http://www.google.com")
            .build();

        final Collection<Team> allTeams = List.of(Team.builder()
            .id(20)
            .teamName("teamName")
            .teamDescription("teamDescription")
            .forumLink("http://www.google.com")
            .build()
        );

        final ValidationResult<Team> response = TeamValidator.validateUpdate(team, existingTeam, allTeams);

        assertThat(response.isFailure())
            .isTrue();

        assertThat(response.getErrors())
            .containsOnly("Payload conflicts with an existing object on: [teamName]");
    }

    @Test
    void whenValidatingUpdate_givenTeamWithNameAlreadyExists_andExistingTeamIsTheOneBeingUpdated_thenSuccessResponseIsReturned() {
        final TeamRequest team = TeamRequest.builder()
            .teamName("teamName")
            .teamDescription("teamDescription")
            .forumLink("http://www.google.com")
            .build();

        final Team existingTeam = Team.builder()
            .id(1)
            .teamName("teamName")
            .teamDescription("teamDescription")
            .forumLink("http://www.google.com")
            .build();

        final Collection<Team> allTeams = List.of(Team.builder()
            .id(1)
            .teamName("teamName")
            .teamDescription("teamDescription")
            .forumLink("http://www.google.com")
            .build()
        );

        final ValidationResult<Team> response = TeamValidator.validateUpdate(team, existingTeam, allTeams);

        assertThat(response.isFailure())
            .isFalse();
    }

    @Test
    void whenValidatingUpdate_givenTeamWithNullDescription_thenSuccessResponseIsReturned() {
        final TeamRequest team = TeamRequest.builder()
            .teamName("teamName")
            .teamDescription("teamDescription")
            .forumLink(null)
            .build();

        final Team existingTeam = Team.builder()
            .teamName("teamName")
            .teamDescription("teamDescription")
            .forumLink("http://www.google.com")
            .build();

        final ValidationResult<Team> response = TeamValidator.validateUpdate(team, existingTeam, Collections.emptyList());

        assertThat(response.isFailure())
            .isFalse();
    }

    @Test
    void whenValidatingUpdate_givenTeamWithNullForumLink_thenSuccessResponseIsReturned() {
        final TeamRequest team = TeamRequest.builder()
            .teamName("teamName")
            .teamDescription(null)
            .forumLink("http://www.google.com")
            .build();

        final Team existingTeam = Team.builder()
            .teamName("teamName")
            .teamDescription("teamDescription")
            .forumLink("http://www.google.com")
            .build();

        final ValidationResult<Team> response = TeamValidator.validateUpdate(team, existingTeam, Collections.emptyList());

        assertThat(response.isFailure())
            .isFalse();
    }

    @Test
    void whenValidatingUpdate_givenTeamWithForumLinkThatIsNotValidUrl_thenFailureResponseIsReturned() {
        final TeamRequest team = TeamRequest.builder()
            .teamName("teamName")
            .teamDescription("teamDescription")
            .forumLink("invalidUrl")
            .build();

        final Team existingTeam = Team.builder()
            .teamName("teamName")
            .teamDescription("teamDescription")
            .forumLink("http://www.google.com")
            .build();

        final ValidationResult<Team> response = TeamValidator.validateUpdate(team, existingTeam, Collections.emptyList());

        assertThat(response.isFailure())
            .isTrue();

        assertThat(response.getErrors())
            .containsOnly("Field 'forumLink' is not a valid link: 'invalidUrl'");
    }

    @Test
    void whenValidatingDelete_givenTeamThatIsNotBeingUsed_thenSuccessResponseIsReturned() {
        final Team existingTeam = Team.builder()
            .teamName("teamName")
            .teamDescription("teamDescription")
            .forumLink("http://www.google.com")
            .build();

        final ValidationResult<Team> response = TeamValidator.validateDelete(existingTeam, Collections.emptyList());

        assertThat(response.isFailure())
            .as("Expected validation to pass, instead failed with errors: " + response.getErrors())
            .isFalse();
    }

    @Test
    void whenValidatingDelete_givenTeamThatIsBeingUsedByUser_thenFailureResponseIsReturned() {
        final Team existingTeam = Team.builder()
            .id(1)
            .teamName("teamName")
            .teamDescription("teamDescription")
            .forumLink("http://www.google.com")
            .build();

        final Collection<User> allUsers = List.of(User.builder()
            .team(existingTeam)
            .build()
        );

        final ValidationResult<Team> response = TeamValidator.validateDelete(existingTeam, allUsers);

        assertThat(response.isFailure())
            .isTrue();
    }

    @Test
    void whenValidatingDelete_givenTeamExistsButIsNotBeingUsedByUser_thenSuccessResponseIsReturned() {
        final Team existingTeam = Team.builder()
            .id(1)
            .teamName("teamName")
            .teamDescription("teamDescription")
            .forumLink("http://www.google.com")
            .build();

        final Team userTeam = Team.builder()
            .id(2)
            .teamName("teamName2")
            .teamDescription("teamDescription")
            .forumLink("http://www.google.com")
            .build();

        final Collection<User> allUsers = List.of(User.builder()
            .team(userTeam)
            .build()
        );

        final ValidationResult<Team> response = TeamValidator.validateDelete(existingTeam, allUsers);

        assertThat(response.isFailure())
            .isFalse();
    }
}

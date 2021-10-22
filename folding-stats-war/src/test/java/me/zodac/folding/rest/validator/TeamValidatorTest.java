package me.zodac.folding.rest.validator;

import static org.assertj.core.api.Assertions.assertThat;

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

        final MockBusinessLogic mockBusinessLogic = MockBusinessLogic.create();
        final TeamValidator teamValidator = TeamValidator.create(mockBusinessLogic);
        final ValidationResult<Team> response = teamValidator.validateCreate(team);

        assertThat(response.isFailure())
            .as("Expected validation to pass, instead failed with errors: " + response.getErrors())
            .isFalse();
    }

    @Test
    void whenValidatingCreate_givenNullTeam_thenFailureResponseIsReturned() {
        final MockBusinessLogic mockBusinessLogic = MockBusinessLogic.create();
        final TeamValidator teamValidator = TeamValidator.create(mockBusinessLogic);
        final ValidationResult<Team> response = teamValidator.validateCreate(null);

        assertThat(response.isFailure())
            .isTrue();

        assertThat(response.getErrors())
            .contains("Payload is null");
    }

    @Test
    void whenValidatingCreate_givenTeamWithNullName_thenFailureResponseIsReturned() {
        final TeamRequest team = TeamRequest.builder()
            .teamName(null)
            .teamDescription("teamDescription")
            .forumLink("http://www.google.com")
            .build();

        final MockBusinessLogic mockBusinessLogic = MockBusinessLogic.create();
        final TeamValidator teamValidator = TeamValidator.create(mockBusinessLogic);
        final ValidationResult<Team> response = teamValidator.validateCreate(team);

        assertThat(response.isFailure())
            .isTrue();

        assertThat(response.getErrors())
            .contains("Field 'teamName' must not be empty");
    }

    @Test
    void whenValidatingCreate_givenTeamWithNameAlreadyExists_thenFailureResponseIsReturned() {
        final MockBusinessLogic mockBusinessLogic = MockBusinessLogic.create();
        mockBusinessLogic.createTeam(Team.builder()
            .teamName("existingName")
            .teamDescription("teamDescription")
            .forumLink("http://www.google.com")
            .build()
        );

        final TeamValidator teamValidator = TeamValidator.create(mockBusinessLogic);
        final TeamRequest team = TeamRequest.builder()
            .teamName("existingName")
            .teamDescription("teamDescription")
            .forumLink("http://www.google.com")
            .build();

        final ValidationResult<Team> response = teamValidator.validateCreate(team);

        assertThat(response.isFailure())
            .isTrue();

        assertThat(response.getErrors())
            .contains("Payload conflicts with an existing object on: [teamName]");
    }

    @Test
    void whenValidatingCreate_givenTeamWithNullDescription_thenSuccessResponseIsReturned() {
        final TeamRequest team = TeamRequest.builder()
            .teamName("teamName")
            .teamDescription(null)
            .forumLink("http://www.google.com")
            .build();

        final MockBusinessLogic mockBusinessLogic = MockBusinessLogic.create();
        final TeamValidator teamValidator = TeamValidator.create(mockBusinessLogic);
        final ValidationResult<Team> response = teamValidator.validateCreate(team);

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

        final MockBusinessLogic mockBusinessLogic = MockBusinessLogic.create();
        final TeamValidator teamValidator = TeamValidator.create(mockBusinessLogic);
        final ValidationResult<Team> response = teamValidator.validateCreate(team);

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

        final MockBusinessLogic mockBusinessLogic = MockBusinessLogic.create();
        final TeamValidator teamValidator = TeamValidator.create(mockBusinessLogic);
        final ValidationResult<Team> response = teamValidator.validateCreate(team);

        assertThat(response.isFailure())
            .isTrue();

        assertThat(response.getErrors())
            .contains("Field 'forumLink' is not a valid link: 'invalidUrl'");
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

        final MockBusinessLogic mockBusinessLogic = MockBusinessLogic.create();
        final TeamValidator teamValidator = TeamValidator.create(mockBusinessLogic);
        final ValidationResult<Team> response = teamValidator.validateUpdate(team, existingTeam);

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

        final MockBusinessLogic mockBusinessLogic = MockBusinessLogic.create();
        final TeamValidator teamValidator = TeamValidator.create(mockBusinessLogic);
        final ValidationResult<Team> response = teamValidator.validateUpdate(null, existingTeam);

        assertThat(response.isFailure())
            .isTrue();

        assertThat(response.getErrors())
            .contains("Payload is null");
    }

    @Test
    void whenValidatingUpdate_givenNullExistingTeam_thenFailureResponseIsReturned() {
        final TeamRequest team = TeamRequest.builder()
            .teamName("teamName")
            .teamDescription("teamDescription")
            .forumLink("http://www.google.com")
            .build();

        final MockBusinessLogic mockBusinessLogic = MockBusinessLogic.create();
        final TeamValidator teamValidator = TeamValidator.create(mockBusinessLogic);
        final ValidationResult<Team> response = teamValidator.validateUpdate(team, null);

        assertThat(response.isFailure())
            .isTrue();

        assertThat(response.getErrors())
            .contains("Payload is null");
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

        final MockBusinessLogic mockBusinessLogic = MockBusinessLogic.create();
        final TeamValidator teamValidator = TeamValidator.create(mockBusinessLogic);
        final ValidationResult<Team> response = teamValidator.validateUpdate(team, existingTeam);

        assertThat(response.isFailure())
            .isTrue();

        assertThat(response.getErrors())
            .contains("Field 'teamName' must not be empty");
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

        final MockBusinessLogic mockBusinessLogic = MockBusinessLogic.create();
        mockBusinessLogic.createTeam(Team.builder()
            .id(20)
            .teamName("teamName")
            .teamDescription("teamDescription")
            .forumLink("http://www.google.com")
            .build()
        );

        final TeamValidator teamValidator = TeamValidator.create(mockBusinessLogic);
        final ValidationResult<Team> response = teamValidator.validateUpdate(team, existingTeam);

        assertThat(response.isFailure())
            .isTrue();

        assertThat(response.getErrors())
            .contains("Payload conflicts with an existing object on: [teamName]");
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

        final MockBusinessLogic mockBusinessLogic = MockBusinessLogic.create();
        mockBusinessLogic.createTeam(Team.builder()
            .id(1)
            .teamName("teamName")
            .teamDescription("teamDescription")
            .forumLink("http://www.google.com")
            .build()
        );

        final TeamValidator teamValidator = TeamValidator.create(mockBusinessLogic);
        final ValidationResult<Team> response = teamValidator.validateUpdate(team, existingTeam);

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

        final MockBusinessLogic mockBusinessLogic = MockBusinessLogic.create();
        final TeamValidator teamValidator = TeamValidator.create(mockBusinessLogic);
        final ValidationResult<Team> response = teamValidator.validateUpdate(team, existingTeam);

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

        final MockBusinessLogic mockBusinessLogic = MockBusinessLogic.create();
        final TeamValidator teamValidator = TeamValidator.create(mockBusinessLogic);
        final ValidationResult<Team> response = teamValidator.validateUpdate(team, existingTeam);

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

        final MockBusinessLogic mockBusinessLogic = MockBusinessLogic.create();
        final TeamValidator teamValidator = TeamValidator.create(mockBusinessLogic);
        final ValidationResult<Team> response = teamValidator.validateUpdate(team, existingTeam);

        assertThat(response.isFailure())
            .isTrue();

        assertThat(response.getErrors())
            .contains("Field 'forumLink' is not a valid link: 'invalidUrl'");
    }

    @Test
    void whenValidatingDelete_givenTeamThatIsNotBeingUsed_thenSuccessResponseIsReturned() {
        final Team existingTeam = Team.builder()
            .teamName("teamName")
            .teamDescription("teamDescription")
            .forumLink("http://www.google.com")
            .build();

        final MockBusinessLogic mockBusinessLogic = MockBusinessLogic.create();
        final TeamValidator teamValidator = TeamValidator.create(mockBusinessLogic);
        final ValidationResult<Team> response = teamValidator.validateDelete(existingTeam);

        assertThat(response.isFailure())
            .as("Expected validation to pass, instead failed with errors: " + response.getErrors())
            .isFalse();
    }

    @Test
    void whenValidatingDelete_givenTeamThatIsBeingUsedByUser_thenFailureResponseIsReturned() {
        final Team existingTeam = Team.builder()
            .teamName("teamName")
            .teamDescription("teamDescription")
            .forumLink("http://www.google.com")
            .build();

        final User userUsingHardware = User.builder()
            .team(existingTeam)
            .build();

        final MockBusinessLogic mockBusinessLogic = MockBusinessLogic.create();
        mockBusinessLogic.createUser(userUsingHardware);
        final TeamValidator teamValidator = TeamValidator.create(mockBusinessLogic);
        final ValidationResult<Team> response = teamValidator.validateDelete(existingTeam);

        assertThat(response.isFailure())
            .isTrue();
    }
}

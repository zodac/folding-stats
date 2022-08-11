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

package me.zodac.folding.bean.tc.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

import java.util.List;
import me.zodac.folding.api.exception.ConflictException;
import me.zodac.folding.api.exception.UsedByException;
import me.zodac.folding.api.exception.ValidationException;
import me.zodac.folding.api.tc.Category;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.HardwareMake;
import me.zodac.folding.api.tc.HardwareType;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.bean.api.FoldingRepository;
import me.zodac.folding.rest.api.tc.request.TeamRequest;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link TeamValidator}.
 */
class TeamValidatorTest {

    private static int hardwareId = 1;
    private static int teamId = 1;
    private static int userId = 1;

    @Test
    void whenValidatingCreate_givenValidTeam_thenSuccessResponseIsReturned() {
        final TeamRequest team = TeamRequest.builder()
            .teamName("teamName")
            .teamDescription("teamDescription")
            .forumLink("https://www.google.com")
            .build();

        final FoldingRepository foldingRepository = new MockFoldingRepository();

        final TeamValidator teamValidator = new TeamValidator(foldingRepository);
        final Team response = teamValidator.create(team);

        assertThat(response)
            .as("Expected validation to pass")
            .isNotNull();
    }

    @Test
    void whenValidatingCreate_givenTeamWithNullName_thenFailureResponseIsReturned() {
        final TeamRequest team = TeamRequest.builder()
            .teamName(null)
            .teamDescription("teamDescription")
            .forumLink("https://www.google.com")
            .build();

        final FoldingRepository foldingRepository = new MockFoldingRepository();

        final TeamValidator teamValidator = new TeamValidator(foldingRepository);
        final ValidationException e = catchThrowableOfType(() -> teamValidator.create(team), ValidationException.class);
        assertThat(e.getValidationFailure().errors())
            .containsOnly("Field 'teamName' must not be empty");
    }

    @Test
    void whenValidatingCreate_givenOtherTeamAlreadyExists_thenSuccessResponseIsReturned() {
        final TeamRequest team = TeamRequest.builder()
            .teamName("existingName")
            .teamDescription("teamDescription")
            .forumLink("https://www.google.com")
            .build();

        final Team existingTeam = Team.create(
            teamId++,
            "anotherName",
            "teamDescription",
            "https://www.google.com"
        );

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createTeam(existingTeam);

        final TeamValidator teamValidator = new TeamValidator(foldingRepository);
        final Team response = teamValidator.create(team);

        assertThat(response)
            .as("Expected validation to pass")
            .isNotNull();
    }

    @Test
    void whenValidatingCreate_givenTeamWithNameAlreadyExists_thenFailureResponseIsReturned() {
        final TeamRequest team = TeamRequest.builder()
            .teamName("existingName")
            .teamDescription("teamDescription")
            .forumLink("https://www.google.com")
            .build();

        final Team existingTeam = Team.create(
            teamId++,
            "existingName",
            "teamDescription",
            "https://www.google.com"
        );

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createTeam(existingTeam);

        final TeamValidator teamValidator = new TeamValidator(foldingRepository);
        final ConflictException e = catchThrowableOfType(() -> teamValidator.create(team), ConflictException.class);
        assertThat(e.getConflictFailure().conflictingAttributes())
            .containsOnly("teamName");

        assertThat(e.getConflictFailure().conflictingObject())
            .isNotNull();
    }

    @Test
    void whenValidatingCreate_givenTeamWithNullDescription_thenSuccessResponseIsReturned() {
        final TeamRequest team = TeamRequest.builder()
            .teamName("teamName")
            .teamDescription(null)
            .forumLink("https://www.google.com")
            .build();

        final FoldingRepository foldingRepository = new MockFoldingRepository();

        final TeamValidator teamValidator = new TeamValidator(foldingRepository);
        final Team response = teamValidator.create(team);

        assertThat(response)
            .as("Expected validation to pass")
            .isNotNull();
    }

    @Test
    void whenValidatingCreate_givenTeamWithNullForumLink_thenSuccessResponseIsReturned() {
        final TeamRequest team = TeamRequest.builder()
            .teamName("teamName")
            .teamDescription("teamDescription")
            .forumLink(null)
            .build();

        final FoldingRepository foldingRepository = new MockFoldingRepository();

        final TeamValidator teamValidator = new TeamValidator(foldingRepository);
        final Team response = teamValidator.create(team);

        assertThat(response)
            .as("Expected validation to pass")
            .isNotNull();
    }

    @Test
    void whenValidatingCreate_givenTeamWithForumLinkThatIsNotValidUrl_thenFailureResponseIsReturned() {
        final TeamRequest team = TeamRequest.builder()
            .teamName("teamName")
            .teamDescription("teamDescription")
            .forumLink("invalidUrl")
            .build();

        final FoldingRepository foldingRepository = new MockFoldingRepository();

        final TeamValidator teamValidator = new TeamValidator(foldingRepository);
        final ValidationException e = catchThrowableOfType(() -> teamValidator.create(team), ValidationException.class);
        assertThat(e.getValidationFailure().errors())
            .containsOnly("Field 'forumLink' is not a valid link: 'invalidUrl'");
    }

    @Test
    void whenValidatingUpdate_givenValidTeam_thenSuccessResponseIsReturned() {
        final TeamRequest team = TeamRequest.builder()
            .teamName("teamName")
            .teamDescription("teamDescription")
            .forumLink("https://www.google.com")
            .build();

        final Team existingTeam = Team.create(
            teamId++,
            "teamName",
            "teamDescription2",
            "https://www.google.com"
        );

        final FoldingRepository foldingRepository = new MockFoldingRepository();

        final TeamValidator teamValidator = new TeamValidator(foldingRepository);
        final Team response = teamValidator.update(team, existingTeam);

        assertThat(response)
            .as("Expected validation to pass")
            .isNotNull();
    }

    @Test
    void whenValidatingUpdate_givenTeamWithNullName_thenFailureResponseIsReturned() {
        final TeamRequest team = TeamRequest.builder()
            .teamName(null)
            .teamDescription("teamDescription")
            .forumLink("https://www.google.com")
            .build();

        final Team existingTeam = generateTeam();

        final FoldingRepository foldingRepository = new MockFoldingRepository();

        final TeamValidator teamValidator = new TeamValidator(foldingRepository);
        final ValidationException e =
            catchThrowableOfType(() -> teamValidator.update(team, existingTeam), ValidationException.class);
        assertThat(e.getValidationFailure().errors())
            .containsOnly("Field 'teamName' must not be empty");
    }

    @Test
    void whenValidatingUpdate_givenTeamWithNameAlreadyExists_thenFailureResponseIsReturned() {
        final TeamRequest team = TeamRequest.builder()
            .teamName("teamName")
            .teamDescription("teamDescription")
            .forumLink("https://www.google.com")
            .build();

        final Team existingTeam = Team.create(
            1,
            "differentName",
            "teamDescription",
            "https://www.google.com"
        );

        final Team otherTeam = Team.create(
            20,
            "teamName",
            "teamDescription",
            "https://www.google.com"
        );

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createTeam(otherTeam);

        final TeamValidator teamValidator = new TeamValidator(foldingRepository);
        final ConflictException e = catchThrowableOfType(() -> teamValidator.update(team, existingTeam), ConflictException.class);
        assertThat(e.getConflictFailure().conflictingAttributes())
            .containsOnly("teamName");

        assertThat(e.getConflictFailure().conflictingObject())
            .isNotNull();
    }

    @Test
    void whenValidatingUpdate_givenTeamWithNameAlreadyExists_andExistingTeamIsTheOneBeingUpdated_thenSuccessResponseIsReturned() {
        final TeamRequest team = TeamRequest.builder()
            .teamName("teamName")
            .teamDescription("teamDescription")
            .forumLink("https://www.google.com")
            .build();

        final Team existingTeam = Team.create(
            1,
            "teamName",
            "teamDescription",
            "https://www.google.com"
        );

        final Team otherTeam = Team.create(
            1,
            "teamName",
            "teamDescription",
            "https://www.google.com"
        );

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createTeam(otherTeam);

        final TeamValidator teamValidator = new TeamValidator(foldingRepository);
        final Team response = teamValidator.update(team, existingTeam);

        assertThat(response)
            .as("Expected validation to pass")
            .isNotNull();
    }

    @Test
    void whenValidatingUpdate_givenTeamWithNullDescription_thenSuccessResponseIsReturned() {
        final TeamRequest team = TeamRequest.builder()
            .teamName("teamName")
            .teamDescription("teamDescription")
            .forumLink(null)
            .build();

        final Team existingTeam = generateTeam();

        final FoldingRepository foldingRepository = new MockFoldingRepository();

        final TeamValidator teamValidator = new TeamValidator(foldingRepository);
        final Team response = teamValidator.update(team, existingTeam);

        assertThat(response)
            .as("Expected validation to pass")
            .isNotNull();
    }

    @Test
    void whenValidatingUpdate_givenTeamWithNullForumLink_thenSuccessResponseIsReturned() {
        final TeamRequest team = TeamRequest.builder()
            .teamName("teamName")
            .teamDescription(null)
            .forumLink("https://www.google.com")
            .build();

        final Team existingTeam = generateTeam();

        final FoldingRepository foldingRepository = new MockFoldingRepository();

        final TeamValidator teamValidator = new TeamValidator(foldingRepository);
        final Team response = teamValidator.update(team, existingTeam);

        assertThat(response)
            .as("Expected validation to pass")
            .isNotNull();
    }

    @Test
    void whenValidatingUpdate_givenTeamWithForumLinkThatIsNotValidUrl_thenFailureResponseIsReturned() {
        final TeamRequest team = TeamRequest.builder()
            .teamName("teamName")
            .teamDescription("teamDescription")
            .forumLink("invalidUrl")
            .build();

        final Team existingTeam = generateTeam();

        final FoldingRepository foldingRepository = new MockFoldingRepository();

        final TeamValidator teamValidator = new TeamValidator(foldingRepository);
        final ValidationException e =
            catchThrowableOfType(() -> teamValidator.update(team, existingTeam), ValidationException.class);
        assertThat(e.getValidationFailure().errors())
            .containsOnly("Field 'forumLink' is not a valid link: 'invalidUrl'");
    }

    @Test
    void whenValidatingDelete_givenTeamThatIsNotBeingUsed_thenSuccessResponseIsReturned() {
        final Team existingTeam = generateTeam();

        final FoldingRepository foldingRepository = new MockFoldingRepository();

        final TeamValidator teamValidator = new TeamValidator(foldingRepository);
        final Team response = teamValidator.delete(existingTeam);

        assertThat(response)
            .as("Expected validation to pass")
            .isNotNull();
    }

    @Test
    void whenValidatingDelete_givenTeamThatIsBeingUsedByUser_thenFailureResponseIsReturned() {
        final Team existingTeam = Team.create(
            1,
            "teamName",
            "teamDescription",
            "https://www.google.com"
        );

        final User existingUser = User.create(
            userId++,
            "userName",
            "userName",
            "DummyPasskey12345678901234567890",
            Category.NVIDIA_GPU,
            "https://www.google.com",
            "https://www.google.com",
            generateHardware(),
            existingTeam,
            false
        );

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createUser(existingUser);

        final TeamValidator teamValidator = new TeamValidator(foldingRepository);
        final UsedByException e = catchThrowableOfType(() -> teamValidator.delete(existingTeam), UsedByException.class);
        final List<?> usedBy = List.of(e.getUsedByFailure().usedBy());
        assertThat(usedBy)
            .hasSize(1);

        assertThat(usedBy.get(0).toString())
            .contains("DummyPas************************")
            .doesNotContain("DummyPasskey12345678901234567890");
    }

    @Test
    void whenValidatingDelete_givenTeamExistsButIsNotBeingUsedByUser_thenSuccessResponseIsReturned() {
        final Team existingTeam = Team.create(
            1,
            "teamName",
            "teamDescription",
            "https://www.google.com"
        );

        final Team userTeam = Team.create(
            2,
            "teamName2",
            "teamDescription",
            "https://www.google.com"
        );

        final User existingUser = User.create(
            userId++,
            "userName",
            "userName",
            "DummyPasskey12345678901234567890",
            Category.NVIDIA_GPU,
            "https://www.google.com",
            "https://www.google.com",
            generateHardware(),
            userTeam,
            false
        );

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createUser(existingUser);

        final TeamValidator teamValidator = new TeamValidator(foldingRepository);
        final Team response = teamValidator.delete(existingTeam);

        assertThat(response)
            .as("Expected validation to pass")
            .isNotNull();
    }

    private static Team generateTeam() {
        return Team.create(
            teamId++,
            "teamName",
            "teamDescription",
            "https://www.google.com"
        );
    }

    private static Hardware generateHardware() {
        return Hardware.create(
            hardwareId++,
            "hardwareName",
            "displayName",
            HardwareMake.AMD,
            HardwareType.GPU,
            1.00D,
            1L
        );
    }
}

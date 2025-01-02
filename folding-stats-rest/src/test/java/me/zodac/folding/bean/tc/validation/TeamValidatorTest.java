/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2025 zodac.me
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
import me.zodac.folding.api.tc.Role;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.bean.api.FoldingRepository;
import me.zodac.folding.rest.api.tc.request.TeamRequest;
import org.checkerframework.checker.nullness.qual.Nullable;
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
        final TeamRequest team = generateTeamRequest("teamName", "teamDescription", "https://www.google.com");
        final FoldingRepository foldingRepository = new MockFoldingRepository();

        final TeamValidator teamValidator = new TeamValidator(foldingRepository);
        final Team response = teamValidator.create(team);

        assertThat(response)
            .as("Expected validation to pass")
            .isNotNull();
    }

    @Test
    void whenValidatingCreate_givenOtherTeamAlreadyExists_thenSuccessResponseIsReturned() {
        final TeamRequest team = generateTeamRequest("existingName", "teamDescription", "https://www.google.com");

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
        final TeamRequest team = generateTeamRequest("existingName", "teamDescription", "https://www.google.com");

        final Team existingTeam = Team.create(
            teamId++,
            "existingName",
            "teamDescription",
            "https://www.google.com"
        );

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createTeam(existingTeam);

        final TeamValidator teamValidator = new TeamValidator(foldingRepository);
        final ConflictException e = catchThrowableOfType(ConflictException.class, () -> teamValidator.create(team));
        assertThat(e.getConflictFailure().conflictingAttributes())
            .containsOnly("teamName");

        assertThat(e.getConflictFailure().conflictingObject())
            .isNotNull();
    }

    @Test
    void whenValidatingCreate_givenTeamWithNullDescription_thenSuccessResponseIsReturned() {
        final TeamRequest team = generateTeamRequest("teamName", null, "https://www.google.com");

        final FoldingRepository foldingRepository = new MockFoldingRepository();

        final TeamValidator teamValidator = new TeamValidator(foldingRepository);
        final Team response = teamValidator.create(team);

        assertThat(response)
            .as("Expected validation to pass")
            .isNotNull();
    }

    @Test
    void whenValidatingCreate_givenTeamWithNullForumLink_thenSuccessResponseIsReturned() {
        final TeamRequest team = generateTeamRequest("teamName", "teamDescription", null);

        final FoldingRepository foldingRepository = new MockFoldingRepository();

        final TeamValidator teamValidator = new TeamValidator(foldingRepository);
        final Team response = teamValidator.create(team);

        assertThat(response)
            .as("Expected validation to pass")
            .isNotNull();
    }

    @Test
    void whenValidatingCreate_givenTeamWithForumLinkThatIsNotValidUrl_thenFailureResponseIsReturned() {
        final TeamRequest team = generateTeamRequest("teamName", "teamDescription", "invalidUrl");
        final FoldingRepository foldingRepository = new MockFoldingRepository();

        final TeamValidator teamValidator = new TeamValidator(foldingRepository);
        final ValidationException e = catchThrowableOfType(ValidationException.class, () -> teamValidator.create(team));
        assertThat(e.getValidationFailure().errors())
            .containsOnly("Field 'forumLink' is not a valid link: 'invalidUrl'");
    }

    @Test
    void whenValidatingUpdate_givenValidTeam_thenSuccessResponseIsReturned() {
        final TeamRequest team = generateTeamRequest("teamName", "teamDescription", "https://www.google.com");
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
    void whenValidatingUpdate_givenTeamWithNameAlreadyExists_thenFailureResponseIsReturned() {
        final TeamRequest team = generateTeamRequest("teamName", "teamDescription", "https://www.google.com");

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
        final ConflictException e = catchThrowableOfType(ConflictException.class, () -> teamValidator.update(team, existingTeam));
        assertThat(e.getConflictFailure().conflictingAttributes())
            .containsOnly("teamName");

        assertThat(e.getConflictFailure().conflictingObject())
            .isNotNull();
    }

    @Test
    void whenValidatingUpdate_givenTeamWithNameAlreadyExists_andExistingTeamIsTheOneBeingUpdated_thenSuccessResponseIsReturned() {
        final TeamRequest team = generateTeamRequest("teamName", "teamDescription", "https://www.google.com");

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
        final TeamRequest team = generateTeamRequest("teamName", null, "https://www.google.com");
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
        final TeamRequest team = generateTeamRequest("teamName", "teamDescription", null);
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
        final TeamRequest team = generateTeamRequest("teamName", "teamDescription", "invalidUrl");
        final Team existingTeam = generateTeam();

        final FoldingRepository foldingRepository = new MockFoldingRepository();

        final TeamValidator teamValidator = new TeamValidator(foldingRepository);
        final ValidationException e = catchThrowableOfType(ValidationException.class, () -> teamValidator.update(team, existingTeam));
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
            Role.MEMBER
        );

        final FoldingRepository foldingRepository = new MockFoldingRepository();
        foldingRepository.createUser(existingUser);

        final TeamValidator teamValidator = new TeamValidator(foldingRepository);
        final UsedByException e = catchThrowableOfType(UsedByException.class, () -> teamValidator.delete(existingTeam));
        final List<?> usedBy = List.of(e.getUsedByFailure().usedBy());
        assertThat(usedBy)
            .hasSize(1);

        assertThat(usedBy.getFirst().toString())
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
            Role.MEMBER
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

    private static TeamRequest generateTeamRequest(final String teamName, final @Nullable String teamDescription, final @Nullable String forumLink) {
        return new TeamRequest(teamName, teamDescription, forumLink);
    }
}

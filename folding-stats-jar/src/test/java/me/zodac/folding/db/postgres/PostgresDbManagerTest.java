package me.zodac.folding.db.postgres;

import me.zodac.folding.api.db.DbManager;
import me.zodac.folding.api.db.SystemUserAuthentication;
import me.zodac.folding.api.exception.FoldingException;
import me.zodac.folding.api.tc.Category;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.OperatingSystem;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.tc.exception.HardwareNotFoundException;
import me.zodac.folding.api.tc.exception.NoStatsAvailableException;
import me.zodac.folding.api.tc.exception.TeamNotFoundException;
import me.zodac.folding.api.tc.exception.UserNotFoundException;
import me.zodac.folding.api.tc.stats.OffsetStats;
import me.zodac.folding.api.tc.stats.RetiredUserTcStats;
import me.zodac.folding.api.tc.stats.Stats;
import me.zodac.folding.api.tc.stats.UserStats;
import me.zodac.folding.api.tc.stats.UserTcStats;
import org.jooq.exception.DataAccessException;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.Collection;
import java.util.Optional;

import static me.zodac.folding.db.postgres.TestGenerator.nextHardwareName;
import static me.zodac.folding.db.postgres.TestGenerator.nextTeamName;
import static me.zodac.folding.db.postgres.TestGenerator.nextUserName;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link PostgresDbManager}.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PostgresDbManagerTest {

    private static final DbManager POSTGRES_DB_MANAGER = PostgresDbManager.create(TestDbConnectionPool.create());

    @Test
    @Order(1)
    void hardwareTest() throws FoldingException, HardwareNotFoundException {
        final Hardware hardware = generateHardware();
        final Hardware createdHardware = POSTGRES_DB_MANAGER.createHardware(hardware);
        assertThat(createdHardware.getId())
                .isNotEqualTo(Hardware.EMPTY_HARDWARE_ID);

        // Not explicitly handling this case, the validator should ensure no duplicate creates are attempted
        assertThatThrownBy(() -> POSTGRES_DB_MANAGER.createHardware(hardware))
                .isInstanceOf(DataAccessException.class);

        final Collection<Hardware> allHardware = POSTGRES_DB_MANAGER.getAllHardware();
        assertThat(allHardware)
                .hasSize(1);

        final Hardware retrievedHardware = POSTGRES_DB_MANAGER.getHardware(createdHardware.getId()).orElseThrow(() -> new HardwareNotFoundException(createdHardware.getId()));
        assertThat(retrievedHardware)
                .isEqualTo(createdHardware);

        final Hardware hardwareToUpdate = Hardware.builder()
                .id(retrievedHardware.getId())
                .hardwareName(retrievedHardware.getHardwareName())
                .displayName(retrievedHardware.getDisplayName())
                .operatingSystem(OperatingSystem.LINUX)
                .multiplier(retrievedHardware.getMultiplier())
                .build();

        POSTGRES_DB_MANAGER.updateHardware(hardwareToUpdate);
        final Hardware updatedHardware = POSTGRES_DB_MANAGER.getHardware(createdHardware.getId()).orElseThrow(() -> new HardwareNotFoundException(createdHardware.getId()));
        assertThat(updatedHardware)
                .isEqualTo(hardwareToUpdate);

        POSTGRES_DB_MANAGER.deleteHardware(createdHardware.getId());

        final Collection<Hardware> allHardwareAfterDelete = POSTGRES_DB_MANAGER.getAllHardware();
        assertThat(allHardwareAfterDelete)
                .isEmpty();
    }

    @Test
    @Order(2)
    void teamTest() throws FoldingException, TeamNotFoundException {
        final Team team = generateTeam();
        final Team createdTeam = POSTGRES_DB_MANAGER.createTeam(team);

        assertThat(createdTeam.getId())
                .isNotEqualTo(Team.EMPTY_TEAM_ID);

        // Not explicitly handling this case, the validator should ensure no duplicate creates are attempted
        assertThatThrownBy(() -> POSTGRES_DB_MANAGER.createTeam(team))
                .isInstanceOf(DataAccessException.class);

        final Collection<Team> allTeams = POSTGRES_DB_MANAGER.getAllTeams();
        assertThat(allTeams)
                .hasSize(1);

        final Team retrievedTeam = POSTGRES_DB_MANAGER.getTeam(createdTeam.getId()).orElseThrow(() -> new TeamNotFoundException(createdTeam.getId()));
        assertThat(retrievedTeam)
                .isEqualTo(createdTeam);

        final Team teamToUpdate = Team.builder()
                .id(retrievedTeam.getId())
                .teamName(retrievedTeam.getTeamName())
                .teamDescription("Updated description")
                .forumLink(retrievedTeam.getForumLink())
                .build();

        POSTGRES_DB_MANAGER.updateTeam(teamToUpdate);
        final Team updatedTeam = POSTGRES_DB_MANAGER.getTeam(createdTeam.getId()).orElseThrow(() -> new TeamNotFoundException(createdTeam.getId()));
        assertThat(updatedTeam)
                .isEqualTo(teamToUpdate);

        POSTGRES_DB_MANAGER.deleteTeam(createdTeam.getId());

        final Collection<Team> allTeamsAfterDelete = POSTGRES_DB_MANAGER.getAllTeams();
        assertThat(allTeamsAfterDelete)
                .isEmpty();
    }

    @Test
    @Order(3)
    void userTest() throws FoldingException, UserNotFoundException {
        final User user = generateUser();
        final User createdUser = POSTGRES_DB_MANAGER.createUser(user);

        assertThat(createdUser.getId())
                .isNotEqualTo(User.EMPTY_USER_ID);

        // Not explicitly handling this case, the validator should ensure no duplicate creates are attempted
        assertThatThrownBy(() -> POSTGRES_DB_MANAGER.createUser(user))
                .isInstanceOf(DataAccessException.class);

        final Collection<User> allUsers = POSTGRES_DB_MANAGER.getAllUsers();
        assertThat(allUsers)
                .hasSize(1);

        final User retrievedUser = POSTGRES_DB_MANAGER.getUser(createdUser.getId()).orElseThrow(() -> new UserNotFoundException(createdUser.getId()));
        assertThat(retrievedUser)
                .isEqualTo(createdUser);

        final User userToUpdate = User.builder()
                .id(retrievedUser.getId())
                .foldingUserName(retrievedUser.getFoldingUserName())
                .displayName(retrievedUser.getDisplayName())
                .passkey(retrievedUser.getPasskey())
                .category(Category.AMD_GPU)
                .profileLink(retrievedUser.getProfileLink())
                .liveStatsLink(retrievedUser.getLiveStatsLink())
                .hardware(retrievedUser.getHardware())
                .team(retrievedUser.getTeam())
                .userIsCaptain(retrievedUser.isUserIsCaptain())
                .build();

        POSTGRES_DB_MANAGER.updateUser(userToUpdate);
        final User updatedUser = POSTGRES_DB_MANAGER.getUser(createdUser.getId()).orElseThrow(() -> new UserNotFoundException(createdUser.getId()));
        assertThat(updatedUser)
                .isEqualTo(userToUpdate);

        POSTGRES_DB_MANAGER.deleteUser(createdUser.getId());

        final Collection<User> allUsersAfterDelete = POSTGRES_DB_MANAGER.getAllUsers();
        assertThat(allUsersAfterDelete)
                .isEmpty();
    }

    @Test
    void initialUserStatsTest() throws FoldingException {
        final int userId = createUser().getId();

        assertThatThrownBy(() -> POSTGRES_DB_MANAGER.getInitialStats(userId).orElseThrow(FoldingException::new))
                .isInstanceOf(FoldingException.class);

        final Stats newStats = Stats.create(100L, 5);
        POSTGRES_DB_MANAGER.persistInitialStats(UserStats.createWithoutTimestamp(userId, newStats));

        final UserStats userStatsAfterUpdate = POSTGRES_DB_MANAGER.getInitialStats(userId).orElseThrow(FoldingException::new);
        assertThat(userStatsAfterUpdate.getStats())
                .isEqualTo(newStats);
    }

    @Test
    void totalStatsTest() throws FoldingException {
        final int userId = createUser().getId();

        assertThatThrownBy(() -> POSTGRES_DB_MANAGER.getTotalStats(userId).orElseThrow(FoldingException::new))
                .isInstanceOf(FoldingException.class);

        final Stats newStats = Stats.create(100L, 5);
        POSTGRES_DB_MANAGER.persistTotalStats(UserStats.createWithoutTimestamp(userId, newStats));

        final UserStats userStatsAfterUpdate = POSTGRES_DB_MANAGER.getTotalStats(userId).orElseThrow(FoldingException::new);
        assertThat(userStatsAfterUpdate.getStats())
                .isEqualTo(newStats);
    }

    @Test
    void retiredUserStatsTest() throws FoldingException {
        final User userToRetire = createUser();
        final Team team = userToRetire.getTeam();

        assertThat(POSTGRES_DB_MANAGER.getRetiredUserStatsForTeam(team))
                .isEmpty();

        POSTGRES_DB_MANAGER.deleteUser(userToRetire.getId());

        final long points = 100L;
        final long multipliedPoints = 1_000L;
        final int units = 5;

        POSTGRES_DB_MANAGER.persistRetiredUserStats(team.getId(), userToRetire.getId(), userToRetire.getDisplayName(),
                UserTcStats.createWithoutTimestamp(userToRetire.getId(), points, multipliedPoints, units));

        final Collection<RetiredUserTcStats> retiredUserStatsForTeam = POSTGRES_DB_MANAGER.getRetiredUserStatsForTeam(team);

        assertThat(retiredUserStatsForTeam)
                .hasSize(1);

        final RetiredUserTcStats retiredUserTcStats = retiredUserStatsForTeam.iterator().next();

        assertThat(retiredUserTcStats.getPoints())
                .isEqualTo(points);
        assertThat(retiredUserTcStats.getMultipliedPoints())
                .isEqualTo(multipliedPoints);
        assertThat(retiredUserTcStats.getUnits())
                .isEqualTo(units);
    }

    @Test
    void offsetStatsTest() throws FoldingException {
        final int userId = createUser().getId();

        assertThat(POSTGRES_DB_MANAGER.getOffsetStats(userId))
                .isEmpty();

        final OffsetStats offsetStats = OffsetStats.create(100L, 1_000L, 5);
        POSTGRES_DB_MANAGER.addOffsetStats(userId, offsetStats);
        final Optional<OffsetStats> firstOffsetStats = POSTGRES_DB_MANAGER.getOffsetStats(userId);
        assertThat(firstOffsetStats)
                .isPresent();
        assertThat(firstOffsetStats.get())
                .isEqualTo(offsetStats);

        final OffsetStats overwriteOffsetStats = OffsetStats.create(500L, 5_000L, 25);
        POSTGRES_DB_MANAGER.addOffsetStats(userId, overwriteOffsetStats);
        final Optional<OffsetStats> secondOffsetStats = POSTGRES_DB_MANAGER.getOffsetStats(userId);
        assertThat(secondOffsetStats)
                .isPresent();
        assertThat(secondOffsetStats.get())
                .isEqualTo(overwriteOffsetStats);

        final OffsetStats additionalOffsetStats = OffsetStats.create(250L, 2_500L, 12);
        POSTGRES_DB_MANAGER.addOrUpdateOffsetStats(userId, additionalOffsetStats);

        final OffsetStats expectedOffsetStats = OffsetStats.create(
                overwriteOffsetStats.getPointsOffset() + additionalOffsetStats.getPointsOffset(),
                overwriteOffsetStats.getMultipliedPointsOffset() + additionalOffsetStats.getMultipliedPointsOffset(),
                overwriteOffsetStats.getUnitsOffset() + additionalOffsetStats.getUnitsOffset()
        );
        final Optional<OffsetStats> thirdOffsetStats = POSTGRES_DB_MANAGER.getOffsetStats(userId);
        assertThat(thirdOffsetStats)
                .isPresent();
        assertThat(thirdOffsetStats.get())
                .isEqualTo(expectedOffsetStats);

        final int secondUserId = createUser().getId();
        POSTGRES_DB_MANAGER.addOffsetStats(secondUserId, offsetStats);

        POSTGRES_DB_MANAGER.clearAllOffsetStats();

        assertThat(POSTGRES_DB_MANAGER.getOffsetStats(userId))
                .isEmpty();
        assertThat(POSTGRES_DB_MANAGER.getOffsetStats(secondUserId))
                .isEmpty();
    }

    @Test
    void hourlyTcStatsTest() throws FoldingException, NoStatsAvailableException {
        assertThat(POSTGRES_DB_MANAGER.isAnyHourlyTcStats())
                .isFalse();

        final int userId = createUser().getId();
        assertThatThrownBy(() -> POSTGRES_DB_MANAGER.getHourlyTcStats(userId).orElseThrow(() -> new NoStatsAvailableException("user", userId)))
                .isInstanceOf(NoStatsAvailableException.class);

        final long points = 100L;
        final long multipliedPoints = 1_000L;
        final int units = 5;
        final UserTcStats userTcStats = UserTcStats.createWithoutTimestamp(userId, points, multipliedPoints, units);
        POSTGRES_DB_MANAGER.persistHourlyTcStats(userTcStats);

        final UserTcStats retrievedUserTcStats = POSTGRES_DB_MANAGER.getHourlyTcStats(userId).orElseThrow(() -> new NoStatsAvailableException("user", userId));
        assertThat(retrievedUserTcStats)
                .isEqualTo(userTcStats);

        assertThat(POSTGRES_DB_MANAGER.isAnyHourlyTcStats())
                .isTrue();
    }

    @Test
    void validSystemUserTest() throws FoldingException {
        final SystemUserAuthentication invalidUserName = POSTGRES_DB_MANAGER.authenticateSystemUser("invalidUserName", "ADMIN_PASSWORD");
        assertThat(invalidUserName.isUserExists())
                .isFalse();
        assertThat(invalidUserName.isPasswordMatch())
                .isFalse();
        assertThat(invalidUserName.getUserRoles())
                .isEmpty();

        final SystemUserAuthentication invalidPassword = POSTGRES_DB_MANAGER.authenticateSystemUser("ADMIN_USERNAME", "invalidPassword");
        assertThat(invalidPassword.isUserExists())
                .isTrue();
        assertThat(invalidPassword.isPasswordMatch())
                .isFalse();
        assertThat(invalidPassword.getUserRoles())
                .isEmpty();

        final SystemUserAuthentication admin = POSTGRES_DB_MANAGER.authenticateSystemUser("ADMIN_USERNAME", "ADMIN_PASSWORD");
        assertThat(admin.isUserExists())
                .isTrue();
        assertThat(admin.isPasswordMatch())
                .isTrue();
        assertThat(admin.getUserRoles())
                .contains("admin");

        final SystemUserAuthentication readOnly = POSTGRES_DB_MANAGER.authenticateSystemUser("READ_ONLY_USERNAME", "READ_ONLY_PASSWORD");
        assertThat(readOnly.isUserExists())
                .isTrue();
        assertThat(readOnly.isPasswordMatch())
                .isTrue();
        assertThat(readOnly.getUserRoles())
                .contains("read-only");
    }

    private Hardware generateHardware() {
        return Hardware.createWithoutId(nextHardwareName(), "hardware", OperatingSystem.WINDOWS, 1.0D);
    }

    private Hardware createHardware() throws FoldingException {
        return POSTGRES_DB_MANAGER.createHardware(generateHardware());
    }

    private User generateUser() throws FoldingException {
        final Hardware hardware = createHardware();
        final Team team = createTeam();
        return User.createWithoutId(nextUserName(), "user", "passkey", Category.NVIDIA_GPU, "", "", hardware, team, true);
    }

    private User createUser() throws FoldingException {
        return POSTGRES_DB_MANAGER.createUser(generateUser());
    }

    private Team generateTeam() {
        return Team.createWithoutId(nextTeamName(), "team", "");
    }

    private Team createTeam() throws FoldingException {
        return POSTGRES_DB_MANAGER.createTeam(generateTeam());
    }
}

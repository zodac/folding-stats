package me.zodac.folding.db.postgres;

import me.zodac.folding.api.db.DbManager;
import me.zodac.folding.api.db.SystemUserAuthentication;
import me.zodac.folding.api.db.exception.FoldingConflictException;
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
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.Collection;

import static me.zodac.folding.db.postgres.TestGenerator.nextHardwareName;
import static me.zodac.folding.db.postgres.TestGenerator.nextTeamName;
import static me.zodac.folding.db.postgres.TestGenerator.nextUserName;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link PostgresDbManager}.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PostgresDbManagerTest {

    private static final DbManager POSTGRES_DB_MANAGER = PostgresDbManager.create(TestDbConnectionPool.create());

    @Test
    @Order(1)
    public void hardwareTest() throws FoldingException, FoldingConflictException, HardwareNotFoundException {
        final Hardware hardware = generateHardware();
        final Hardware createdHardware = POSTGRES_DB_MANAGER.createHardware(hardware);
        assertThat(createdHardware.getId())
                .isNotEqualTo(Hardware.EMPTY_HARDWARE_ID);

        final Collection<Hardware> allHardware = POSTGRES_DB_MANAGER.getAllHardware();
        assertThat(allHardware)
                .hasSize(1);

        final Hardware retrievedHardware = POSTGRES_DB_MANAGER.getHardware(createdHardware.getId());
        assertThat(retrievedHardware)
                .isEqualTo(createdHardware);

        createdHardware.setOperatingSystem(OperatingSystem.LINUX.displayName());

        POSTGRES_DB_MANAGER.updateHardware(createdHardware);
        final Hardware updatedHardware = POSTGRES_DB_MANAGER.getHardware(createdHardware.getId());
        assertThat(updatedHardware)
                .isEqualTo(createdHardware);

        POSTGRES_DB_MANAGER.deleteHardware(createdHardware.getId());

        final Collection<Hardware> allHardwareAfterDelete = POSTGRES_DB_MANAGER.getAllHardware();
        assertThat(allHardwareAfterDelete)
                .isEmpty();
    }

    @Test
    @Order(2)
    public void teamTest() throws FoldingException, FoldingConflictException, TeamNotFoundException {
        final Team team = generateTeam();
        final Team createdTeam = POSTGRES_DB_MANAGER.createTeam(team);

        assertThat(createdTeam.getId())
                .isNotEqualTo(Team.EMPTY_TEAM_ID);

        final Collection<Team> allTeams = POSTGRES_DB_MANAGER.getAllTeams();
        assertThat(allTeams)
                .hasSize(1);

        final Team retrievedTeam = POSTGRES_DB_MANAGER.getTeam(createdTeam.getId());
        assertThat(retrievedTeam)
                .isEqualTo(createdTeam);

        createdTeam.setTeamDescription("Updated description");

        POSTGRES_DB_MANAGER.updateTeam(createdTeam);
        final Team updatedTeam = POSTGRES_DB_MANAGER.getTeam(createdTeam.getId());
        assertThat(updatedTeam)
                .isEqualTo(createdTeam);

        POSTGRES_DB_MANAGER.deleteTeam(createdTeam.getId());

        final Collection<Team> allTeamsAfterDelete = POSTGRES_DB_MANAGER.getAllTeams();
        assertThat(allTeamsAfterDelete)
                .isEmpty();
    }

    @Test
    @Order(3)
    public void userTest() throws FoldingException, FoldingConflictException, UserNotFoundException {
        final User user = generateUser();
        final User createdUser = POSTGRES_DB_MANAGER.createUser(user);

        assertThat(createdUser.getId())
                .isNotEqualTo(User.EMPTY_USER_ID);

        final Collection<User> allUsers = POSTGRES_DB_MANAGER.getAllUsers();
        assertThat(allUsers)
                .hasSize(1);

        final User retrievedUser = POSTGRES_DB_MANAGER.getUser(createdUser.getId());
        assertThat(retrievedUser)
                .isEqualTo(createdUser);

        createdUser.setCategory(Category.AMD_GPU.displayName());

        POSTGRES_DB_MANAGER.updateUser(createdUser);
        final User updatedUser = POSTGRES_DB_MANAGER.getUser(createdUser.getId());
        assertThat(updatedUser)
                .isEqualTo(createdUser);

        POSTGRES_DB_MANAGER.deleteUser(createdUser.getId());

        final Collection<User> allUsersAfterDelete = POSTGRES_DB_MANAGER.getAllUsers();
        assertThat(allUsersAfterDelete)
                .isEmpty();
    }

    @Test
    public void initialUserStatsTest() throws FoldingConflictException, FoldingException {
        final int userId = createUser().getId();

        assertThatThrownBy(() -> POSTGRES_DB_MANAGER.getInitialStats(userId))
                .isInstanceOf(FoldingException.class);

        final Stats newStats = Stats.create(100L, 5);
        POSTGRES_DB_MANAGER.persistInitialStats(UserStats.createWithoutTimestamp(userId, newStats));

        final UserStats userStatsAfterUpdate = POSTGRES_DB_MANAGER.getInitialStats(userId);
        assertThat(userStatsAfterUpdate.getStats())
                .isEqualTo(newStats);
    }

    @Test
    public void totalStatsTest() throws FoldingConflictException, FoldingException {
        final int userId = createUser().getId();

        assertThatThrownBy(() -> POSTGRES_DB_MANAGER.getTotalStats(userId))
                .isInstanceOf(FoldingException.class);

        final Stats newStats = Stats.create(100L, 5);
        POSTGRES_DB_MANAGER.persistTotalStats(UserStats.createWithoutTimestamp(userId, newStats));

        final UserStats userStatsAfterUpdate = POSTGRES_DB_MANAGER.getTotalStats(userId);
        assertThat(userStatsAfterUpdate.getStats())
                .isEqualTo(newStats);
    }

    @Test
    public void retiredUserStatsTest() throws FoldingConflictException, FoldingException {
        final int invalidId = 9_999;
        assertThatThrownBy(() -> POSTGRES_DB_MANAGER.getRetiredUserStats(invalidId))
                .isInstanceOf(FoldingException.class);

        final User userToRetire = createUser();
        final int teamId = userToRetire.getTeamId();
        POSTGRES_DB_MANAGER.deleteUser(userToRetire.getId());

        final long points = 100L;
        final long multipliedPoints = 1_000L;
        final int units = 5;

        final int retiredUserId = POSTGRES_DB_MANAGER.persistRetiredUserStats(teamId, userToRetire.getId(), userToRetire.getDisplayName(),
                UserTcStats.createWithoutTimestamp(userToRetire.getId(), points, multipliedPoints, units));

        final RetiredUserTcStats retiredUserStats = POSTGRES_DB_MANAGER.getRetiredUserStats(retiredUserId);
        assertThat(retiredUserStats.getPoints())
                .isEqualTo(points);
        assertThat(retiredUserStats.getMultipliedPoints())
                .isEqualTo(multipliedPoints);
        assertThat(retiredUserStats.getUnits())
                .isEqualTo(units);
    }

    @Test
    public void offsetStatsTest() throws FoldingConflictException, FoldingException {
        final int userId = createUser().getId();

        assertThat(POSTGRES_DB_MANAGER.getOffsetStats(userId))
                .isEqualTo(OffsetStats.empty());

        final OffsetStats offsetStats = OffsetStats.create(100L, 1_000L, 5);
        POSTGRES_DB_MANAGER.addOffsetStats(userId, offsetStats);
        assertThat(POSTGRES_DB_MANAGER.getOffsetStats(userId))
                .isEqualTo(offsetStats);

        final OffsetStats overwriteOffsetStats = OffsetStats.create(500L, 5_000L, 25);
        POSTGRES_DB_MANAGER.addOffsetStats(userId, overwriteOffsetStats);
        assertThat(POSTGRES_DB_MANAGER.getOffsetStats(userId))
                .isEqualTo(overwriteOffsetStats);

        final OffsetStats additionalOffsetStats = OffsetStats.create(250L, 2_500L, 12);
        POSTGRES_DB_MANAGER.addOrUpdateOffsetStats(userId, additionalOffsetStats);

        final OffsetStats expectedOffsetStats = OffsetStats.create(
                overwriteOffsetStats.getPointsOffset() + additionalOffsetStats.getPointsOffset(),
                overwriteOffsetStats.getMultipliedPointsOffset() + additionalOffsetStats.getMultipliedPointsOffset(),
                overwriteOffsetStats.getUnitsOffset() + additionalOffsetStats.getUnitsOffset()
        );
        assertThat(POSTGRES_DB_MANAGER.getOffsetStats(userId))
                .isEqualTo(expectedOffsetStats);

        final int secondUserId = createUser().getId();
        POSTGRES_DB_MANAGER.addOffsetStats(secondUserId, offsetStats);

        POSTGRES_DB_MANAGER.clearAllOffsetStats();

        assertThat(POSTGRES_DB_MANAGER.getOffsetStats(userId))
                .isEqualTo(OffsetStats.empty());
        assertThat(POSTGRES_DB_MANAGER.getOffsetStats(secondUserId))
                .isEqualTo(OffsetStats.empty());
    }

    @Test
    public void hourlyTcStatsTest() throws FoldingConflictException, FoldingException, NoStatsAvailableException {
        assertThat(POSTGRES_DB_MANAGER.isAnyHourlyTcStats())
                .isFalse();

        final int userId = createUser().getId();
        assertThatThrownBy(() -> POSTGRES_DB_MANAGER.getHourlyTcStats(userId))
                .isInstanceOf(NoStatsAvailableException.class);

        final long points = 100L;
        final long multipliedPoints = 1_000L;
        final int units = 5;
        final UserTcStats userTcStats = UserTcStats.createWithoutTimestamp(userId, points, multipliedPoints, units);
        POSTGRES_DB_MANAGER.persistHourlyTcStats(userTcStats);

        final UserTcStats retrievedUserTcStats = POSTGRES_DB_MANAGER.getHourlyTcStats(userId);
        assertThat(retrievedUserTcStats)
                .isEqualTo(userTcStats);

        assertThat(POSTGRES_DB_MANAGER.isAnyHourlyTcStats())
                .isTrue();
    }

    @Test
    public void validSystemUserTest() throws FoldingException {
        final SystemUserAuthentication invalidUserName = POSTGRES_DB_MANAGER.isValidSystemUser("invalidUserName", "ADMIN_PASSWORD");
        assertThat(invalidUserName.isUserExists())
                .isFalse();
        assertThat(invalidUserName.isPasswordMatch())
                .isFalse();
        assertThat(invalidUserName.getUserRoles())
                .isEmpty();

        final SystemUserAuthentication invalidPassword = POSTGRES_DB_MANAGER.isValidSystemUser("ADMIN_USERNAME", "invalidPassword");
        assertThat(invalidPassword.isUserExists())
                .isTrue();
        assertThat(invalidPassword.isPasswordMatch())
                .isFalse();
        assertThat(invalidPassword.getUserRoles())
                .isEmpty();

        final SystemUserAuthentication admin = POSTGRES_DB_MANAGER.isValidSystemUser("ADMIN_USERNAME", "ADMIN_PASSWORD");
        assertThat(admin.isUserExists())
                .isTrue();
        assertThat(admin.isPasswordMatch())
                .isTrue();
        assertThat(admin.getUserRoles())
                .contains("admin");

        final SystemUserAuthentication readOnly = POSTGRES_DB_MANAGER.isValidSystemUser("READ_ONLY_USERNAME", "READ_ONLY_PASSWORD");
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

    private Hardware createHardware() throws FoldingConflictException, FoldingException {
        return POSTGRES_DB_MANAGER.createHardware(generateHardware());
    }

    private User generateUser() throws FoldingConflictException, FoldingException {
        final int hardwareId = createHardware().getId();
        final int teamId = createTeam().getId();
        return User.createWithoutId(nextUserName(), "user", "passkey", Category.NVIDIA_GPU, "", "", hardwareId, teamId, true);
    }

    private User createUser() throws FoldingConflictException, FoldingException {
        return POSTGRES_DB_MANAGER.createUser(generateUser());
    }

    private Team generateTeam() {
        return Team.createWithoutId(nextTeamName(), "team", "");
    }

    private Team createTeam() throws FoldingConflictException, FoldingException {
        return POSTGRES_DB_MANAGER.createTeam(generateTeam());
    }
}

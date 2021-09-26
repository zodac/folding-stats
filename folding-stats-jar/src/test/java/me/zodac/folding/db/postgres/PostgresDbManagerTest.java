package me.zodac.folding.db.postgres;

import static me.zodac.folding.db.postgres.TestGenerator.nextHardwareName;
import static me.zodac.folding.db.postgres.TestGenerator.nextTeamName;
import static me.zodac.folding.db.postgres.TestGenerator.nextUserName;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.Year;
import java.util.Collection;
import java.util.Optional;
import me.zodac.folding.api.UserAuthenticationResult;
import me.zodac.folding.api.db.DbManager;
import me.zodac.folding.api.tc.Category;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.tc.stats.OffsetStats;
import me.zodac.folding.api.tc.stats.RetiredUserTcStats;
import me.zodac.folding.api.tc.stats.UserStats;
import me.zodac.folding.api.tc.stats.UserTcStats;
import me.zodac.folding.api.utils.DateTimeUtils;
import org.jooq.exception.DataAccessException;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * Unit tests for {@link PostgresDbManager}.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PostgresDbManagerTest {

    private static final DbManager POSTGRES_DB_MANAGER = PostgresDbManager.create(TestDbConnectionPool.create());

    @Test
    @Order(1)
    void hardwareTest() {
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

        final Optional<Hardware> optionalRetrievedHardware = POSTGRES_DB_MANAGER.getHardware(createdHardware.getId());
        assertThat(optionalRetrievedHardware)
            .isPresent();

        final Hardware retrievedHardware = optionalRetrievedHardware.get();
        assertThat(retrievedHardware)
            .isEqualTo(createdHardware);

        final Hardware hardwareToUpdate = Hardware.builder()
            .id(retrievedHardware.getId())
            .hardwareName(retrievedHardware.getHardwareName())
            .displayName(retrievedHardware.getDisplayName())
            .multiplier(retrievedHardware.getMultiplier())
            .build();

        POSTGRES_DB_MANAGER.updateHardware(hardwareToUpdate);
        final Optional<Hardware> optionalUpdatedHardware = POSTGRES_DB_MANAGER.getHardware(createdHardware.getId());
        assertThat(optionalUpdatedHardware)
            .isPresent();

        final Hardware updatedHardware = optionalUpdatedHardware.get();
        assertThat(updatedHardware)
            .isEqualTo(hardwareToUpdate);

        POSTGRES_DB_MANAGER.deleteHardware(createdHardware.getId());

        final Collection<Hardware> allHardwareAfterDelete = POSTGRES_DB_MANAGER.getAllHardware();
        assertThat(allHardwareAfterDelete)
            .isEmpty();
    }

    @Test
    @Order(2)
    void teamTest() {
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

        final Optional<Team> optionalRetrievedTeam = POSTGRES_DB_MANAGER.getTeam(createdTeam.getId());
        assertThat(optionalRetrievedTeam)
            .isPresent();

        final Team retrievedTeam = optionalRetrievedTeam.get();
        assertThat(retrievedTeam)
            .isEqualTo(createdTeam);

        final Team teamToUpdate = Team.builder()
            .id(retrievedTeam.getId())
            .teamName(retrievedTeam.getTeamName())
            .teamDescription("Updated description")
            .forumLink(retrievedTeam.getForumLink())
            .build();

        POSTGRES_DB_MANAGER.updateTeam(teamToUpdate);
        final Optional<Team> optionalUpdatedTeam = POSTGRES_DB_MANAGER.getTeam(createdTeam.getId());
        assertThat(optionalUpdatedTeam)
            .isPresent();

        final Team updatedTeam = optionalUpdatedTeam.get();
        assertThat(updatedTeam)
            .isEqualTo(teamToUpdate);

        POSTGRES_DB_MANAGER.deleteTeam(createdTeam.getId());

        final Collection<Team> allTeamsAfterDelete = POSTGRES_DB_MANAGER.getAllTeams();
        assertThat(allTeamsAfterDelete)
            .isEmpty();
    }

    @Test
    @Order(3)
    void userTest() {
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

        final Optional<User> optionalRetrievedUser = POSTGRES_DB_MANAGER.getUser(createdUser.getId());
        assertThat(optionalRetrievedUser)
            .isPresent();

        final User retrievedUser = optionalRetrievedUser.get();
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
        final Optional<User> optionalUpdatedUser = POSTGRES_DB_MANAGER.getUser(createdUser.getId());
        assertThat(optionalUpdatedUser)
            .isPresent();

        final User updatedUser = optionalUpdatedUser.get();
        assertThat(updatedUser)
            .isEqualTo(userToUpdate);

        POSTGRES_DB_MANAGER.deleteUser(createdUser.getId());

        final Collection<User> allUsersAfterDelete = POSTGRES_DB_MANAGER.getAllUsers();
        assertThat(allUsersAfterDelete)
            .isEmpty();
    }

    @Test
    void initialUserStatsTest() {
        final int userId = createUser().getId();

        assertThat(POSTGRES_DB_MANAGER.getInitialStats(userId))
            .isEmpty();

        final long points = 100L;
        final int units = 10;
        POSTGRES_DB_MANAGER.persistInitialStats(UserStats.create(userId, DateTimeUtils.currentUtcTimestamp(), points, units));

        final Optional<UserStats> userStatsAfterUpdate = POSTGRES_DB_MANAGER.getInitialStats(userId);
        assertThat(userStatsAfterUpdate)
            .isPresent();
        assertThat(userStatsAfterUpdate.get().getPoints())
            .isEqualTo(points);
        assertThat(userStatsAfterUpdate.get().getUnits())
            .isEqualTo(units);
    }

    @Test
    void totalStatsTest() {
        final int userId = createUser().getId();

        assertThat(POSTGRES_DB_MANAGER.getTotalStats(userId))
            .isEmpty();

        final long points = 100L;
        final int units = 10;
        POSTGRES_DB_MANAGER.persistTotalStats(UserStats.create(userId, DateTimeUtils.currentUtcTimestamp(), points, units));

        final Optional<UserStats> userStatsAfterUpdate = POSTGRES_DB_MANAGER.getTotalStats(userId);
        assertThat(userStatsAfterUpdate)
            .isPresent();
        assertThat(userStatsAfterUpdate.get().getPoints())
            .isEqualTo(points);
        assertThat(userStatsAfterUpdate.get().getUnits())
            .isEqualTo(units);
    }

    @Test
    void retiredUserStatsTest() {
        final User userToRetire = createUser();
        final Team team = userToRetire.getTeam();

        assertThat(POSTGRES_DB_MANAGER.getAllRetiredUserStats())
            .isEmpty();

        POSTGRES_DB_MANAGER.deleteUser(userToRetire.getId());

        final long points = 100L;
        final long multipliedPoints = 1_000L;
        final int units = 5;

        POSTGRES_DB_MANAGER.persistRetiredUserStats(team.getId(), userToRetire.getId(), userToRetire.getDisplayName(),
            UserTcStats.createNow(userToRetire.getId(), points, multipliedPoints, units));

        final Collection<RetiredUserTcStats> retiredUserStatsForTeam = POSTGRES_DB_MANAGER.getAllRetiredUserStats();

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
    void offsetStatsTest() {
        final int userId = createUser().getId();

        assertThat(POSTGRES_DB_MANAGER.getOffsetStats(userId))
            .isEmpty();

        final OffsetStats offsetStats = OffsetStats.create(100L, 1_000L, 5);
        POSTGRES_DB_MANAGER.addOffsetStats(userId, offsetStats);
        final Optional<OffsetStats> firstOffsetStats = POSTGRES_DB_MANAGER.getOffsetStats(userId);
        assertThat(firstOffsetStats)
            .isPresent();

        final OffsetStats firstOffsetStatsActual = firstOffsetStats.get();
        assertThat(firstOffsetStatsActual)
            .isEqualTo(offsetStats);

        final OffsetStats overwriteOffsetStats = OffsetStats.create(500L, 5_000L, 25);
        POSTGRES_DB_MANAGER.addOffsetStats(userId, overwriteOffsetStats);
        final Optional<OffsetStats> secondOffsetStats = POSTGRES_DB_MANAGER.getOffsetStats(userId);
        assertThat(secondOffsetStats)
            .isPresent();

        final OffsetStats secondOffsetStatsActual = secondOffsetStats.get();
        assertThat(secondOffsetStatsActual)
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

        final OffsetStats thirdOffsetStatsActual = thirdOffsetStats.get();
        assertThat(thirdOffsetStatsActual)
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
    void hourlyTcStatsTest() {
        assertThat(POSTGRES_DB_MANAGER.isAnyHourlyTcStats())
            .isFalse();

        final int userId = createUser().getId();
        assertThat(POSTGRES_DB_MANAGER.getHourlyTcStats(userId))
            .isEmpty();

        final long points = 100L;
        final long multipliedPoints = 1_000L;
        final int units = 5;
        final UserTcStats userTcStats = UserTcStats.createNow(userId, points, multipliedPoints, units);
        POSTGRES_DB_MANAGER.persistHourlyTcStats(userTcStats);

        final Optional<UserTcStats> retrievedUserTcStats = POSTGRES_DB_MANAGER.getHourlyTcStats(userId);
        assertThat(retrievedUserTcStats)
            .isPresent();

        final UserTcStats actual = retrievedUserTcStats.get();
        assertThat(actual)
            .isEqualTo(userTcStats);

        assertThat(POSTGRES_DB_MANAGER.isAnyHourlyTcStats())
            .isTrue();
    }

    @Test
    void historicTest() {
        final int userId = createUser().getId();

        final Year year = Year.of(2020);
        final Month month = Month.APRIL;
        final int yesterday = 14;
        final int day = 15;

        assertThat(POSTGRES_DB_MANAGER.getHistoricStatsHourly(userId, yesterday, month, year))
            .isEmpty();
        assertThat(POSTGRES_DB_MANAGER.getHistoricStatsDaily(userId, month, year))
            .isEmpty();
        assertThat(POSTGRES_DB_MANAGER.getHistoricStatsMonthly(userId, year))
            .isEmpty();

        final long currentDayFirstPoints = 200L;
        final long currentDayFirstMultipliedPoints = 2_000L;
        final int currentDayFirstUnits = 10;
        final UserTcStats currentDayFirstUserTcStats = UserTcStats
            .create(userId, DateTimeUtils.getTimestampOf(year, month, yesterday, 0, 0, 0), currentDayFirstPoints, currentDayFirstMultipliedPoints,
                currentDayFirstUnits);
        POSTGRES_DB_MANAGER.persistHourlyTcStats(currentDayFirstUserTcStats);

        final long currentDaySecondPoints = 300L;
        final long currentDaySecondMultipliedPoints = 3_000L;
        final int currentDaySecondUnits = 15;
        final UserTcStats currentDaySecondUserTcStats = UserTcStats
            .create(userId, DateTimeUtils.getTimestampOf(year, month, yesterday, 1, 0, 0), currentDaySecondPoints, currentDaySecondMultipliedPoints,
                currentDaySecondUnits);
        POSTGRES_DB_MANAGER.persistHourlyTcStats(currentDaySecondUserTcStats);

        final long currentDayThirdPoints = 300L;
        final long currentDayThirdMultipliedPoints = 3_000L;
        final int currentDayThirdUnits = 15;
        final UserTcStats currentDayThirdUserTcStats = UserTcStats
            .create(userId, DateTimeUtils.getTimestampOf(year, month, day, 1, 0, 0), currentDayThirdPoints, currentDayThirdMultipliedPoints,
                currentDayThirdUnits);
        POSTGRES_DB_MANAGER.persistHourlyTcStats(currentDayThirdUserTcStats);

        assertThat(POSTGRES_DB_MANAGER.getHistoricStatsHourly(userId, yesterday, month, year))
            .isNotEmpty();
        assertThat(POSTGRES_DB_MANAGER.getHistoricStatsDaily(userId, month, year))
            .isNotEmpty();
        assertThat(POSTGRES_DB_MANAGER.getHistoricStatsMonthly(userId, year))
            .isNotEmpty();
    }

    @Test
    void validMonthlyResultTest() {
        final Year firstResultYear = Year.of(2020);
        final Month firstResultMonth = Month.APRIL;
        final String firstResult = "firstResult";
        final LocalDateTime firstResultUtcTimestamp = DateTimeUtils.getLocalDateTimeOf(firstResultYear, firstResultMonth);
        POSTGRES_DB_MANAGER.persistMonthlyResult(firstResult, firstResultUtcTimestamp);

        final Year secondResultYear = Year.of(2019);
        final Month secondResultMonth = Month.SEPTEMBER;
        final String secondResult = "secondResult";
        final LocalDateTime secondResultUtcTimestamp = DateTimeUtils.getLocalDateTimeOf(secondResultYear, secondResultMonth);
        POSTGRES_DB_MANAGER.persistMonthlyResult(secondResult, secondResultUtcTimestamp);

        final Optional<String> firstResultOutput = POSTGRES_DB_MANAGER.getMonthlyResult(firstResultMonth, firstResultYear);
        assertThat(firstResultOutput)
            .isPresent();
        assertThat(firstResultOutput.get())
            .isEqualTo(firstResult);

        final Optional<String> secondResultOutput = POSTGRES_DB_MANAGER.getMonthlyResult(secondResultMonth, secondResultYear);
        assertThat(secondResultOutput)
            .isPresent();
        assertThat(secondResultOutput.get())
            .isEqualTo(secondResult);

        final Optional<String> invalidResultOutput = POSTGRES_DB_MANAGER.getMonthlyResult(Month.JUNE, Year.of(1999));
        assertThat(invalidResultOutput)
            .isNotPresent();
    }

    @Test
    void validSystemUserTest() {
        final UserAuthenticationResult invalidUserName = POSTGRES_DB_MANAGER.authenticateSystemUser("invalidUserName", "ADMIN_PASSWORD");
        assertThat(invalidUserName.isUserExists())
            .isFalse();
        assertThat(invalidUserName.isPasswordMatch())
            .isFalse();
        assertThat(invalidUserName.getUserRoles())
            .isEmpty();

        final UserAuthenticationResult invalidPassword = POSTGRES_DB_MANAGER.authenticateSystemUser("ADMIN_USERNAME", "invalidPassword");
        assertThat(invalidPassword.isUserExists())
            .isTrue();
        assertThat(invalidPassword.isPasswordMatch())
            .isFalse();
        assertThat(invalidPassword.getUserRoles())
            .isEmpty();

        final UserAuthenticationResult admin = POSTGRES_DB_MANAGER.authenticateSystemUser("ADMIN_USERNAME", "ADMIN_PASSWORD");
        assertThat(admin.isUserExists())
            .isTrue();
        assertThat(admin.isPasswordMatch())
            .isTrue();
        assertThat(admin.getUserRoles())
            .contains("admin");

        final UserAuthenticationResult readOnly = POSTGRES_DB_MANAGER.authenticateSystemUser("READ_ONLY_USERNAME", "READ_ONLY_PASSWORD");
        assertThat(readOnly.isUserExists())
            .isTrue();
        assertThat(readOnly.isPasswordMatch())
            .isTrue();
        assertThat(readOnly.getUserRoles())
            .contains("read-only");
    }

    private Hardware generateHardware() {
        return Hardware.createWithoutId(nextHardwareName(), "hardware", 1.0D);
    }

    private Hardware createHardware() {
        return POSTGRES_DB_MANAGER.createHardware(generateHardware());
    }

    private User generateUser() {
        final Hardware hardware = createHardware();
        final Team team = createTeam();
        return User.createWithoutId(nextUserName(), "user", "passkey", Category.NVIDIA_GPU, "", "", hardware, team, true);
    }

    private User createUser() {
        return POSTGRES_DB_MANAGER.createUser(generateUser());
    }

    private Team generateTeam() {
        return Team.createWithoutId(nextTeamName(), "team", "");
    }

    private Team createTeam() {
        return POSTGRES_DB_MANAGER.createTeam(generateTeam());
    }
}

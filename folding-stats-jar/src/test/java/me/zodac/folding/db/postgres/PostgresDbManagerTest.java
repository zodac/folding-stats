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

package me.zodac.folding.db.postgres;

import static me.zodac.folding.db.postgres.TestGenerator.nextHardwareName;
import static me.zodac.folding.db.postgres.TestGenerator.nextTeamName;
import static me.zodac.folding.db.postgres.TestGenerator.nextUserName;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Month;
import java.time.Year;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import me.zodac.folding.api.UserAuthenticationResult;
import me.zodac.folding.api.tc.Category;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.HardwareMake;
import me.zodac.folding.api.tc.HardwareType;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.tc.change.UserChange;
import me.zodac.folding.api.tc.change.UserChangeState;
import me.zodac.folding.api.tc.result.MonthlyResult;
import me.zodac.folding.api.tc.stats.OffsetTcStats;
import me.zodac.folding.api.tc.stats.RetiredUserTcStats;
import me.zodac.folding.api.tc.stats.UserStats;
import me.zodac.folding.api.tc.stats.UserTcStats;
import me.zodac.folding.api.util.DateTimeConverterUtils;
import me.zodac.folding.rest.api.tc.TeamSummary;
import me.zodac.folding.rest.api.tc.UserSummary;
import me.zodac.folding.rest.api.tc.leaderboard.TeamLeaderboardEntry;
import me.zodac.folding.rest.api.tc.leaderboard.UserCategoryLeaderboardEntry;
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

    private static final PostgresDbManager POSTGRES_DB_MANAGER = PostgresDbManager.create(EmbeddedPostgresDataSource.create());

    @Test
    @Order(1)
    void hardwareTest() {
        final Hardware hardware = generateHardware();
        final Hardware createdHardware = POSTGRES_DB_MANAGER.createHardware(hardware);
        assertThat(createdHardware.id())
            .isNotEqualTo(Hardware.EMPTY_HARDWARE_ID);

        // Not explicitly handling this case, the validator should ensure no duplicate creates are attempted
        assertThatThrownBy(() -> POSTGRES_DB_MANAGER.createHardware(hardware))
            .isInstanceOf(DataAccessException.class);

        final Collection<Hardware> allHardware = POSTGRES_DB_MANAGER.getAllHardware();
        assertThat(allHardware)
            .hasSize(1);

        final Optional<Hardware> optionalRetrievedHardware = POSTGRES_DB_MANAGER.getHardware(createdHardware.id());
        assertThat(optionalRetrievedHardware)
            .isPresent();

        final Hardware retrievedHardware = optionalRetrievedHardware.get();
        assertThat(retrievedHardware)
            .isEqualTo(createdHardware);

        final Hardware hardwareToUpdate = Hardware.create(
            retrievedHardware.id(),
            retrievedHardware.hardwareName(),
            retrievedHardware.displayName(),
            retrievedHardware.hardwareMake(),
            retrievedHardware.hardwareType(),
            retrievedHardware.multiplier(),
            retrievedHardware.averagePpd()
        );

        POSTGRES_DB_MANAGER.updateHardware(hardwareToUpdate);
        final Optional<Hardware> optionalUpdatedHardware = POSTGRES_DB_MANAGER.getHardware(createdHardware.id());
        assertThat(optionalUpdatedHardware)
            .isPresent();

        final Hardware updatedHardware = optionalUpdatedHardware.get();
        assertThat(updatedHardware)
            .isEqualTo(hardwareToUpdate);

        POSTGRES_DB_MANAGER.deleteHardware(createdHardware.id());

        final Collection<Hardware> allHardwareAfterDelete = POSTGRES_DB_MANAGER.getAllHardware();
        assertThat(allHardwareAfterDelete)
            .isEmpty();
    }

    @Test
    @Order(2)
    void teamTest() {
        final Team team = generateTeam();
        final Team createdTeam = POSTGRES_DB_MANAGER.createTeam(team);

        assertThat(createdTeam.id())
            .isNotEqualTo(Team.EMPTY_TEAM_ID);

        // Not explicitly handling this case, the validator should ensure no duplicate creates are attempted
        assertThatThrownBy(() -> POSTGRES_DB_MANAGER.createTeam(team))
            .isInstanceOf(DataAccessException.class);

        final Collection<Team> allTeams = POSTGRES_DB_MANAGER.getAllTeams();
        assertThat(allTeams)
            .hasSize(1);

        final Optional<Team> optionalRetrievedTeam = POSTGRES_DB_MANAGER.getTeam(createdTeam.id());
        assertThat(optionalRetrievedTeam)
            .isPresent();

        final Team retrievedTeam = optionalRetrievedTeam.get();
        assertThat(retrievedTeam)
            .isEqualTo(createdTeam);

        final Team teamToUpdate = Team.create(
            retrievedTeam.id(),
            retrievedTeam.teamName(),
            "Updated description",
            retrievedTeam.forumLink()
        );

        POSTGRES_DB_MANAGER.updateTeam(teamToUpdate);
        final Optional<Team> optionalUpdatedTeam = POSTGRES_DB_MANAGER.getTeam(createdTeam.id());
        assertThat(optionalUpdatedTeam)
            .isPresent();

        final Team updatedTeam = optionalUpdatedTeam.get();
        assertThat(updatedTeam)
            .isEqualTo(teamToUpdate);

        POSTGRES_DB_MANAGER.deleteTeam(createdTeam.id());

        final Collection<Team> allTeamsAfterDelete = POSTGRES_DB_MANAGER.getAllTeams();
        assertThat(allTeamsAfterDelete)
            .isEmpty();
    }

    @Test
    @Order(3)
    void userTest() {
        final User user = generateUser();
        final User createdUser = POSTGRES_DB_MANAGER.createUser(user);

        assertThat(createdUser.id())
            .isNotEqualTo(User.EMPTY_USER_ID);

        // Not explicitly handling this case, the validator should ensure no duplicate creates are attempted
        assertThatThrownBy(() -> POSTGRES_DB_MANAGER.createUser(user))
            .isInstanceOf(DataAccessException.class);

        final Collection<User> allUsers = POSTGRES_DB_MANAGER.getAllUsers();
        assertThat(allUsers)
            .hasSize(1);

        final Optional<User> optionalRetrievedUser = POSTGRES_DB_MANAGER.getUser(createdUser.id());
        assertThat(optionalRetrievedUser)
            .isPresent();

        final User retrievedUser = optionalRetrievedUser.get();
        assertThat(retrievedUser)
            .isEqualTo(createdUser);

        final User userToUpdate = User.create(
            retrievedUser.id(),
            retrievedUser.foldingUserName(),
            retrievedUser.displayName(),
            retrievedUser.passkey(),
            Category.AMD_GPU,
            retrievedUser.profileLink(),
            retrievedUser.liveStatsLink(),
            retrievedUser.hardware(),
            retrievedUser.team(),
            retrievedUser.userIsCaptain()
        );

        POSTGRES_DB_MANAGER.updateUser(userToUpdate);
        final Optional<User> optionalUpdatedUser = POSTGRES_DB_MANAGER.getUser(createdUser.id());
        assertThat(optionalUpdatedUser)
            .isPresent();

        final User updatedUser = optionalUpdatedUser.get();
        assertThat(updatedUser)
            .isEqualTo(userToUpdate);

        POSTGRES_DB_MANAGER.deleteUser(createdUser.id());

        final Collection<User> allUsersAfterDelete = POSTGRES_DB_MANAGER.getAllUsers();
        assertThat(allUsersAfterDelete)
            .isEmpty();
    }

    @Test
    void initialUserStatsTest() {
        final int userId = createUser().id();

        assertThat(POSTGRES_DB_MANAGER.getInitialStats(userId))
            .isEmpty();

        final long points = 100L;
        final int units = 10;
        POSTGRES_DB_MANAGER.createInitialStats(UserStats.createNow(userId, points, units));

        final Optional<UserStats> userStatsAfterUpdate = POSTGRES_DB_MANAGER.getInitialStats(userId);
        assertThat(userStatsAfterUpdate)
            .isPresent();
        assertThat(userStatsAfterUpdate.get().points())
            .isEqualTo(points);
        assertThat(userStatsAfterUpdate.get().units())
            .isEqualTo(units);
    }

    @Test
    void totalStatsTest() {
        final int userId = createUser().id();

        assertThat(POSTGRES_DB_MANAGER.getTotalStats(userId))
            .isEmpty();

        final long points = 100L;
        final int units = 10;
        POSTGRES_DB_MANAGER.createTotalStats(UserStats.createNow(userId, points, units));

        final Optional<UserStats> userStatsAfterUpdate = POSTGRES_DB_MANAGER.getTotalStats(userId);
        assertThat(userStatsAfterUpdate)
            .isPresent();
        assertThat(userStatsAfterUpdate.get().points())
            .isEqualTo(points);
        assertThat(userStatsAfterUpdate.get().units())
            .isEqualTo(units);
    }

    @Test
    void retiredUserStatsTest() {
        final User userToRetire = createUser();
        final Team team = userToRetire.team();

        assertThat(POSTGRES_DB_MANAGER.getAllRetiredUserStats())
            .isEmpty();

        POSTGRES_DB_MANAGER.deleteUser(userToRetire.id());

        final long points = 100L;
        final long multipliedPoints = 1_000L;
        final int units = 5;

        final RetiredUserTcStats initialRetiredUserTcStats = RetiredUserTcStats.createWithoutId(team.id(), userToRetire.displayName(),
            UserTcStats.createNow(userToRetire.id(), points, multipliedPoints, units));
        POSTGRES_DB_MANAGER.createRetiredUserStats(initialRetiredUserTcStats);

        final Collection<RetiredUserTcStats> retiredUserStats = POSTGRES_DB_MANAGER.getAllRetiredUserStats();
        assertThat(retiredUserStats)
            .hasSize(1);

        final RetiredUserTcStats retiredUserTcStats = retiredUserStats.iterator().next();

        assertThat(retiredUserTcStats.points())
            .isEqualTo(points);
        assertThat(retiredUserTcStats.multipliedPoints())
            .isEqualTo(multipliedPoints);
        assertThat(retiredUserTcStats.units())
            .isEqualTo(units);

        POSTGRES_DB_MANAGER.deleteAllRetiredUserStats();
        final Collection<RetiredUserTcStats> retiredUserStatsAfterDelete = POSTGRES_DB_MANAGER.getAllRetiredUserStats();
        assertThat(retiredUserStatsAfterDelete)
            .isEmpty();
    }

    @Test
    void offsetStatsTest() {
        final int userId = createUser().id();

        assertThat(POSTGRES_DB_MANAGER.getOffsetStats(userId))
            .isEmpty();

        final OffsetTcStats offsetStats = OffsetTcStats.create(100L, 1_000L, 5);
        POSTGRES_DB_MANAGER.createOrUpdateOffsetStats(userId, offsetStats);
        final Optional<OffsetTcStats> firstOffsetStats = POSTGRES_DB_MANAGER.getOffsetStats(userId);
        assertThat(firstOffsetStats)
            .isPresent();

        final OffsetTcStats firstOffsetStatsActual = firstOffsetStats.get();
        assertThat(firstOffsetStatsActual)
            .isEqualTo(offsetStats);

        final OffsetTcStats additionalOffsetStats = OffsetTcStats.create(500L, 5_000L, 25);
        POSTGRES_DB_MANAGER.createOrUpdateOffsetStats(userId, additionalOffsetStats);
        final Optional<OffsetTcStats> secondOffsetStats = POSTGRES_DB_MANAGER.getOffsetStats(userId);
        assertThat(secondOffsetStats)
            .isPresent();

        final OffsetTcStats expectedOffsetStats = OffsetTcStats.create(
            offsetStats.getPointsOffset() + additionalOffsetStats.getPointsOffset(),
            offsetStats.getMultipliedPointsOffset() + additionalOffsetStats.getMultipliedPointsOffset(),
            offsetStats.getUnitsOffset() + additionalOffsetStats.getUnitsOffset()
        );
        final Optional<OffsetTcStats> thirdOffsetStats = POSTGRES_DB_MANAGER.getOffsetStats(userId);
        assertThat(thirdOffsetStats)
            .isPresent();

        final OffsetTcStats thirdOffsetStatsActual = thirdOffsetStats.get();
        assertThat(thirdOffsetStatsActual)
            .isEqualTo(expectedOffsetStats);

        final int secondUserId = createUser().id();
        POSTGRES_DB_MANAGER.createOrUpdateOffsetStats(secondUserId, offsetStats);

        POSTGRES_DB_MANAGER.deleteOffsetStats(userId);
        assertThat(POSTGRES_DB_MANAGER.getOffsetStats(userId))
            .isEmpty();

        POSTGRES_DB_MANAGER.deleteAllOffsetStats();
        assertThat(POSTGRES_DB_MANAGER.getOffsetStats(userId))
            .isEmpty();
        assertThat(POSTGRES_DB_MANAGER.getOffsetStats(secondUserId))
            .isEmpty();
    }

    @Test
    void hourlyTcStatsTest() {
        final int userId = createUser().id();
        assertThat(POSTGRES_DB_MANAGER.getHourlyTcStats(userId))
            .isEmpty();

        final long points = 100L;
        final long multipliedPoints = 1_000L;
        final int units = 5;
        final UserTcStats userTcStats = UserTcStats.createNow(userId, points, multipliedPoints, units);
        POSTGRES_DB_MANAGER.createHourlyTcStats(userTcStats);

        final Optional<UserTcStats> retrievedUserTcStats = POSTGRES_DB_MANAGER.getHourlyTcStats(userId);
        assertThat(retrievedUserTcStats)
            .isPresent();

        final UserTcStats actual = retrievedUserTcStats.get();
        assertThat(actual)
            .isEqualTo(userTcStats);
    }

    @Test
    void historicTest() {
        final int userId = createUser().id();

        final Year year = Year.of(2020);
        final Month month = Month.APRIL;
        final int yesterday = 14;
        final int day = 15;

        assertThat(POSTGRES_DB_MANAGER.getHistoricStatsHourly(userId, year, month, yesterday))
            .isEmpty();
        assertThat(POSTGRES_DB_MANAGER.getHistoricStatsDaily(userId, year, month))
            .isEmpty();
        assertThat(POSTGRES_DB_MANAGER.getHistoricStatsMonthly(userId, year))
            .isEmpty();

        final long currentDayFirstPoints = 200L;
        final long currentDayFirstMultipliedPoints = 2_000L;
        final int currentDayFirstUnits = 10;
        final UserTcStats currentDayFirstUserTcStats = UserTcStats
            .create(userId, DateTimeConverterUtils.getFirstTimestampOf(year, month, yesterday), currentDayFirstPoints,
                currentDayFirstMultipliedPoints, currentDayFirstUnits);
        POSTGRES_DB_MANAGER.createHourlyTcStats(currentDayFirstUserTcStats);

        final long currentDaySecondPoints = 300L;
        final long currentDaySecondMultipliedPoints = 3_000L;
        final int currentDaySecondUnits = 15;
        final UserTcStats currentDaySecondUserTcStats = UserTcStats
            .create(userId, DateTimeConverterUtils.getLastTimestampOf(year, month, yesterday), currentDaySecondPoints,
                currentDaySecondMultipliedPoints, currentDaySecondUnits);
        POSTGRES_DB_MANAGER.createHourlyTcStats(currentDaySecondUserTcStats);

        final long currentDayThirdPoints = 300L;
        final long currentDayThirdMultipliedPoints = 3_000L;
        final int currentDayThirdUnits = 15;
        final UserTcStats currentDayThirdUserTcStats = UserTcStats
            .create(userId, DateTimeConverterUtils.getFirstTimestampOf(year, month, day), currentDayThirdPoints, currentDayThirdMultipliedPoints,
                currentDayThirdUnits);
        POSTGRES_DB_MANAGER.createHourlyTcStats(currentDayThirdUserTcStats);

        assertThat(POSTGRES_DB_MANAGER.getHistoricStatsHourly(userId, year, month, yesterday))
            .isNotEmpty();
        assertThat(POSTGRES_DB_MANAGER.getHistoricStatsDaily(userId, year, month))
            .isNotEmpty();
        assertThat(POSTGRES_DB_MANAGER.getHistoricStatsMonthly(userId, year))
            .isNotEmpty();
    }

    @Test
    void monthlyResultTest() {
        final Year firstResultYear = Year.of(2020);
        final Month firstResultMonth = Month.APRIL;
        final MonthlyResult firstResult = MonthlyResult.create(
            List.of(
                TeamLeaderboardEntry.create(
                    TeamSummary.createWithDefaultRank(
                        generateTeam(),
                        "Captain1",
                        Collections.emptyList(),
                        Collections.emptyList()
                    ),
                    0L, 0L
                )
            ),
            Map.of(Category.AMD_GPU, List.of(
                    UserCategoryLeaderboardEntry.create(
                        UserSummary.createWithDefaultRank(
                            generateUser(),
                            0L,
                            0L,
                            0
                        ),
                        1, 0L, 0L
                    )
                ),
                Category.NVIDIA_GPU, Collections.emptyList(),
                Category.WILDCARD, Collections.emptyList()
            ),
            DateTimeConverterUtils.getLocalDateTimeOf(firstResultYear, firstResultMonth)
        );
        POSTGRES_DB_MANAGER.createMonthlyResult(firstResult);

        final Year secondResultYear = Year.of(2019);
        final Month secondResultMonth = Month.SEPTEMBER;
        final MonthlyResult secondResult = MonthlyResult.create(
            List.of(
                TeamLeaderboardEntry.create(
                    TeamSummary.createWithDefaultRank(
                        generateTeam(),
                        "Captain2",
                        Collections.emptyList(),
                        Collections.emptyList()
                    ),
                    0L, 0L
                )
            ),
            Map.of(Category.NVIDIA_GPU, List.of(
                    UserCategoryLeaderboardEntry.create(
                        UserSummary.createWithDefaultRank(
                            generateUser(),
                            0L,
                            0L,
                            0
                        ),
                        1, 0L, 0L
                    )
                ),
                Category.AMD_GPU, Collections.emptyList(),
                Category.WILDCARD, Collections.emptyList()
            ),
            DateTimeConverterUtils.getLocalDateTimeOf(secondResultYear, secondResultMonth)
        );
        POSTGRES_DB_MANAGER.createMonthlyResult(secondResult);

        final Optional<MonthlyResult> firstResultOutput = POSTGRES_DB_MANAGER.getMonthlyResult(firstResultMonth, firstResultYear);
        assertThat(firstResultOutput)
            .isPresent();
        assertThat(firstResultOutput.get())
            .isEqualTo(firstResult);

        final Optional<MonthlyResult> secondResultOutput = POSTGRES_DB_MANAGER.getMonthlyResult(secondResultMonth, secondResultYear);
        assertThat(secondResultOutput)
            .isPresent();
        assertThat(secondResultOutput.get())
            .isEqualTo(secondResult);

        final Optional<MonthlyResult> invalidResultOutput = POSTGRES_DB_MANAGER.getMonthlyResult(Month.JUNE, Year.of(1999));
        assertThat(invalidResultOutput)
            .isNotPresent();
    }

    @Test
    void systemUserTest() {
        final UserAuthenticationResult invalidUserName = POSTGRES_DB_MANAGER.authenticateSystemUser("invalidUserName", "ADMIN_PASSWORD");
        assertThat(invalidUserName.userExists())
            .isFalse();
        assertThat(invalidUserName.passwordMatch())
            .isFalse();
        assertThat(invalidUserName.userRoles())
            .isEmpty();

        final UserAuthenticationResult invalidPassword = POSTGRES_DB_MANAGER.authenticateSystemUser("ADMIN_USERNAME", "invalidPassword");
        assertThat(invalidPassword.userExists())
            .isTrue();
        assertThat(invalidPassword.passwordMatch())
            .isFalse();
        assertThat(invalidPassword.userRoles())
            .isEmpty();

        final UserAuthenticationResult admin = POSTGRES_DB_MANAGER.authenticateSystemUser("ADMIN_USERNAME", "ADMIN_PASSWORD");
        assertThat(admin.userExists())
            .isTrue();
        assertThat(admin.passwordMatch())
            .isTrue();
        assertThat(admin.userRoles())
            .contains("admin");

        final UserAuthenticationResult readOnly = POSTGRES_DB_MANAGER.authenticateSystemUser("READ_ONLY_USERNAME", "READ_ONLY_PASSWORD");
        assertThat(readOnly.userExists())
            .isTrue();
        assertThat(readOnly.passwordMatch())
            .isTrue();
        assertThat(readOnly.userRoles())
            .contains("read-only");
    }

    @Test
    void userChangeTest() {
        final UserChange userChange = generateUserChange();
        final UserChange createdUserChange = POSTGRES_DB_MANAGER.createUserChange(userChange);

        assertThat(createdUserChange.id())
            .isNotZero();

        final Collection<UserChange> allUserChanges = POSTGRES_DB_MANAGER.getAllUserChanges(UserChangeState.getAllValues(), 0L);
        assertThat(allUserChanges)
            .hasSize(1);

        final Optional<UserChange> optionalRetrievedUserChange = POSTGRES_DB_MANAGER.getUserChange(createdUserChange.id());
        assertThat(optionalRetrievedUserChange)
            .isPresent();

        final UserChange retrievedUserChange = optionalRetrievedUserChange.get();
        assertThat(retrievedUserChange)
            .isEqualTo(createdUserChange);

        final UserChange userChangeToUpdate = UserChange.updateWithState(UserChangeState.COMPLETED, createdUserChange);

        POSTGRES_DB_MANAGER.updateUserChange(userChangeToUpdate);
        final Optional<UserChange> optionalUpdatedUserChange = POSTGRES_DB_MANAGER.getUserChange(createdUserChange.id());
        assertThat(optionalUpdatedUserChange)
            .isPresent();

        final UserChange updatedUserChange = optionalUpdatedUserChange.get();
        assertThat(updatedUserChange)
            .isEqualTo(userChangeToUpdate);
    }

    private static Hardware generateHardware() {
        return Hardware.createWithoutId(nextHardwareName(), "hardware", HardwareMake.NVIDIA, HardwareType.GPU, 1.00D, 1L);
    }

    private static Hardware createHardware() {
        return POSTGRES_DB_MANAGER.createHardware(generateHardware());
    }

    private static User generateUser() {
        final Hardware hardware = createHardware();
        final Team team = createTeam();
        return User.createWithoutId(nextUserName(), "user", "passkey", Category.NVIDIA_GPU, "", "", hardware, team, true);
    }

    private static User createUser() {
        return POSTGRES_DB_MANAGER.createUser(generateUser());
    }

    private static Team generateTeam() {
        return Team.createWithoutId(nextTeamName(), "team", "");
    }

    private static Team createTeam() {
        return POSTGRES_DB_MANAGER.createTeam(generateTeam());
    }

    private static UserChange generateUserChange() {
        final User previousUser = createUser();
        final User newUser = createUser();
        return UserChange.createNow(previousUser, newUser, UserChangeState.REQUESTED_NOW);
    }
}

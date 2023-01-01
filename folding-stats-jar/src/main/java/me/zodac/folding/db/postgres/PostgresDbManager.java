/*
 * BSD Zero Clause License
 *
 * Copyright (c) 2021-2023 zodac.me
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

package me.zodac.folding.db.postgres;

import static me.zodac.folding.api.util.DateTimeConverterUtils.formatMonth;
import static me.zodac.folding.db.postgres.RecordConverter.GSON;
import static me.zodac.folding.db.postgres.gen.Routines.crypt;
import static me.zodac.folding.db.postgres.gen.tables.Hardware.HARDWARE;
import static me.zodac.folding.db.postgres.gen.tables.MonthlyResults.MONTHLY_RESULTS;
import static me.zodac.folding.db.postgres.gen.tables.RetiredUserStats.RETIRED_USER_STATS;
import static me.zodac.folding.db.postgres.gen.tables.SystemUsers.SYSTEM_USERS;
import static me.zodac.folding.db.postgres.gen.tables.Teams.TEAMS;
import static me.zodac.folding.db.postgres.gen.tables.UserChanges.USER_CHANGES;
import static me.zodac.folding.db.postgres.gen.tables.UserInitialStats.USER_INITIAL_STATS;
import static me.zodac.folding.db.postgres.gen.tables.UserOffsetTcStats.USER_OFFSET_TC_STATS;
import static me.zodac.folding.db.postgres.gen.tables.UserTcStatsHourly.USER_TC_STATS_HOURLY;
import static me.zodac.folding.db.postgres.gen.tables.UserTotalStats.USER_TOTAL_STATS;
import static me.zodac.folding.db.postgres.gen.tables.Users.USERS;
import static org.jooq.impl.DSL.day;
import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.hour;
import static org.jooq.impl.DSL.max;
import static org.jooq.impl.DSL.month;
import static org.jooq.impl.DSL.year;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.Year;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import javax.sql.DataSource;
import me.zodac.folding.api.UserAuthenticationResult;
import me.zodac.folding.api.db.DbManager;
import me.zodac.folding.api.tc.Hardware;
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
import me.zodac.folding.api.util.DateTimeUtils;
import me.zodac.folding.api.util.LoggerName;
import me.zodac.folding.rest.api.tc.historic.HistoricStats;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

/**
 * Implementation of {@link DbManager} for <b>PostgreSQL</b> databases.
 *
 * <p>
 * Uses <b>jOOQ</b> for code generation for the DB tables/schemas, rather than direct SQL queries. See existing methods for examples.
 *
 * @param dataSource the {@link DataSource} for this instance
 */
public record PostgresDbManager(DataSource dataSource) implements DbManager {

    private static final DateTimeUtils DATE_TIME_UTILS = DateTimeUtils.create();
    private static final Logger SQL_LOGGER = LogManager.getLogger(LoggerName.SQL.get());
    private static final int SINGLE_RESULT = 1;

    /**
     * Creates an instance of {@link PostgresDbManager}.
     *
     * @param dataSource the {@link DataSource} for this instance
     * @return the created {@link PostgresDbManager}
     */
    public static PostgresDbManager create(final DataSource dataSource) {
        return new PostgresDbManager(dataSource);
    }

    @Override
    public Hardware createHardware(final Hardware hardware) {
        return executeQuery(queryContext -> {
            final var query = queryContext
                .insertInto(HARDWARE)
                .columns(
                    HARDWARE.HARDWARE_NAME,
                    HARDWARE.DISPLAY_NAME,
                    HARDWARE.HARDWARE_MAKE,
                    HARDWARE.HARDWARE_TYPE,
                    HARDWARE.MULTIPLIER,
                    HARDWARE.AVERAGE_PPD
                )
                .values(
                    hardware.hardwareName(),
                    hardware.displayName(),
                    hardware.hardwareMake().toString(),
                    hardware.hardwareType().toString(),
                    BigDecimal.valueOf(hardware.multiplier()),
                    BigDecimal.valueOf(hardware.averagePpd())
                )
                .returning(HARDWARE.HARDWARE_ID);
            SQL_LOGGER.debug("Executing SQL: '{}'", query);

            final int hardwareId = query
                .fetch()
                .get(0)
                .getHardwareId();
            return Hardware.updateWithId(hardwareId, hardware);
        });
    }

    @Override
    public Collection<Hardware> getAllHardware() {
        return executeQuery(queryContext -> {
            final var query = queryContext
                .select()
                .from(HARDWARE)
                .orderBy(HARDWARE.HARDWARE_ID.asc());
            SQL_LOGGER.debug("Executing SQL: '{}'", query);

            return query
                .fetch()
                .into(HARDWARE)
                .stream()
                .map(RecordConverter::toHardware)
                .toList();
        });
    }

    @Override
    public Optional<Hardware> getHardware(final int hardwareId) {
        return executeQuery(queryContext -> {
            final var query = queryContext
                .select()
                .from(HARDWARE)
                .where(HARDWARE.HARDWARE_ID.equal(hardwareId));
            SQL_LOGGER.debug("Executing SQL: '{}'", query);

            return query
                .fetch()
                .into(HARDWARE)
                .stream()
                .map(RecordConverter::toHardware)
                .findAny();
        });
    }

    @Override
    public Hardware updateHardware(final Hardware hardwareToUpdate) {
        executeQuery(queryContext -> {
            final var query = queryContext
                .update(HARDWARE)
                .set(HARDWARE.HARDWARE_NAME, hardwareToUpdate.hardwareName())
                .set(HARDWARE.DISPLAY_NAME, hardwareToUpdate.displayName())
                .set(HARDWARE.HARDWARE_MAKE, hardwareToUpdate.hardwareMake().toString())
                .set(HARDWARE.HARDWARE_TYPE, hardwareToUpdate.hardwareType().toString())
                .set(HARDWARE.MULTIPLIER, BigDecimal.valueOf(hardwareToUpdate.multiplier()))
                .set(HARDWARE.AVERAGE_PPD, BigDecimal.valueOf(hardwareToUpdate.averagePpd()))
                .where(HARDWARE.HARDWARE_ID.equal(hardwareToUpdate.id()));
            SQL_LOGGER.debug("Executing SQL: '{}'", query);

            return query.execute();
        });

        // The DB makes no change to this object, so we simply return the provided one
        return hardwareToUpdate;
    }

    @Override
    public void deleteHardware(final int hardwareId) {
        executeQuery(queryContext -> {
            final var query = queryContext
                .deleteFrom(HARDWARE)
                .where(HARDWARE.HARDWARE_ID.equal(hardwareId));
            SQL_LOGGER.debug("Executing SQL: '{}'", query);

            return query.execute();
        });
    }

    @Override
    public Team createTeam(final Team team) {
        return executeQuery(queryContext -> {
            final var query = queryContext
                .insertInto(TEAMS)
                .columns(TEAMS.TEAM_NAME, TEAMS.TEAM_DESCRIPTION, TEAMS.FORUM_LINK)
                .values(team.teamName(), team.teamDescription(), team.forumLink())
                .returning(TEAMS.TEAM_ID);
            SQL_LOGGER.debug("Executing SQL: '{}'", query);

            final int teamId = query
                .fetch()
                .get(0)
                .getTeamId();
            return Team.updateWithId(teamId, team);
        });
    }

    @Override
    public Collection<Team> getAllTeams() {
        return executeQuery(queryContext -> {
            final var query = queryContext
                .select()
                .from(TEAMS)
                .orderBy(TEAMS.TEAM_ID.asc());
            SQL_LOGGER.debug("Executing SQL: '{}'", query);

            return query
                .fetch()
                .into(TEAMS)
                .stream()
                .map(RecordConverter::toTeam)
                .toList();
        });
    }

    @Override
    public Optional<Team> getTeam(final int teamId) {
        return executeQuery(queryContext -> {
            final var query = queryContext
                .select()
                .from(TEAMS)
                .where(TEAMS.TEAM_ID.equal(teamId));
            SQL_LOGGER.debug("Executing SQL: '{}'", query);

            return query
                .fetch()
                .into(TEAMS)
                .stream()
                .map(RecordConverter::toTeam)
                .findAny();
        });
    }

    @Override
    public Team updateTeam(final Team teamToUpdate) {
        executeQuery(queryContext -> {
            final var query = queryContext
                .update(TEAMS)
                .set(TEAMS.TEAM_NAME, teamToUpdate.teamName())
                .set(TEAMS.TEAM_DESCRIPTION, teamToUpdate.teamDescription())
                .set(TEAMS.FORUM_LINK, teamToUpdate.forumLink())
                .where(TEAMS.TEAM_ID.equal(teamToUpdate.id()));
            SQL_LOGGER.debug("Executing SQL: '{}'", query);

            return query.execute();
        });

        // The DB makes no change to this object, so we simply return the provided one
        return teamToUpdate;
    }

    @Override
    public void deleteTeam(final int teamId) {
        SQL_LOGGER.info("Deleting team {} from DB", teamId);
        executeQuery(queryContext -> {
            final var query = queryContext
                .deleteFrom(TEAMS)
                .where(TEAMS.TEAM_ID.equal(teamId));
            SQL_LOGGER.debug("Executing SQL: '{}'", query);

            return query.execute();
        });
    }

    @Override
    public User createUser(final User user) {
        return executeQuery(queryContext -> {
            final var query = queryContext
                .insertInto(USERS)
                .columns(
                    USERS.FOLDING_USERNAME,
                    USERS.DISPLAY_USERNAME,
                    USERS.PASSKEY,
                    USERS.CATEGORY,
                    USERS.PROFILE_LINK,
                    USERS.LIVE_STATS_LINK,
                    USERS.HARDWARE_ID,
                    USERS.TEAM_ID,
                    USERS.IS_CAPTAIN
                )
                .values(
                    user.foldingUserName(),
                    user.displayName(),
                    user.passkey(),
                    user.category().toString(),
                    user.profileLink(),
                    user.liveStatsLink(),
                    user.hardware().id(),
                    user.team().id(),
                    user.role().isCaptain()
                )
                .returning(USERS.USER_ID);
            SQL_LOGGER.debug("Executing SQL: '{}'", query);

            final int userId = query
                .fetch()
                .get(0)
                .getUserId();
            return User.updateWithId(userId, user);
        });
    }

    @Override
    public Collection<User> getAllUsers() {
        return executeQuery(queryContext -> {
            final var query = queryContext
                .select()
                .from(USERS)
                .leftJoin(HARDWARE)
                .on(USERS.HARDWARE_ID.equal(HARDWARE.HARDWARE_ID))
                .leftJoin(TEAMS)
                .on(USERS.TEAM_ID.equal(TEAMS.TEAM_ID))
                .orderBy(USERS.USER_ID.asc());
            SQL_LOGGER.debug("Executing SQL: '{}'", query);

            return query
                .fetch()
                .stream()
                .map(RecordConverter::toUser)
                .toList();
        });
    }

    @Override
    public Optional<User> getUser(final int userId) {
        return executeQuery(queryContext -> {
            final var query = queryContext
                .select()
                .from(USERS)
                .leftJoin(HARDWARE)
                .on(USERS.HARDWARE_ID.equal(HARDWARE.HARDWARE_ID))
                .leftJoin(TEAMS)
                .on(USERS.TEAM_ID.equal(TEAMS.TEAM_ID))
                .where(USERS.USER_ID.equal(userId));
            SQL_LOGGER.debug("Executing SQL: '{}'", query);

            return query
                .fetch()
                .stream()
                .map(RecordConverter::toUser)
                .findAny();
        });
    }

    @Override
    public User updateUser(final User userToUpdate) {
        executeQuery(queryContext -> {
            final var query = queryContext
                .update(USERS)
                .set(USERS.FOLDING_USERNAME, userToUpdate.foldingUserName())
                .set(USERS.DISPLAY_USERNAME, userToUpdate.displayName())
                .set(USERS.PASSKEY, userToUpdate.passkey())
                .set(USERS.CATEGORY, userToUpdate.category().toString())
                .set(USERS.PROFILE_LINK, userToUpdate.profileLink())
                .set(USERS.LIVE_STATS_LINK, userToUpdate.liveStatsLink())
                .set(USERS.HARDWARE_ID, userToUpdate.hardware().id())
                .set(USERS.TEAM_ID, userToUpdate.team().id())
                .set(USERS.IS_CAPTAIN, userToUpdate.role().isCaptain())
                .where(USERS.USER_ID.equal(userToUpdate.id()));
            SQL_LOGGER.debug("Executing SQL: '{}'", query);

            return query.execute();
        });

        // The DB makes no change to this object, so we simply return the provided one
        return userToUpdate;
    }

    @Override
    public void deleteUser(final int userId) {
        executeQuery(queryContext -> {
            final var query = queryContext
                .deleteFrom(USERS)
                .where(USERS.USER_ID.equal(userId));
            SQL_LOGGER.debug("Executing SQL: '{}'", query);

            return query.execute();
        });
    }

    @Override
    public UserChange createUserChange(final UserChange userChange) {
        return executeQuery(queryContext -> {
            final var query = queryContext
                .insertInto(USER_CHANGES)
                .columns(
                    USER_CHANGES.CREATED_UTC_TIMESTAMP,
                    USER_CHANGES.UPDATED_UTC_TIMESTAMP,
                    USER_CHANGES.USER_ID,
                    USER_CHANGES.PREVIOUS_USER,
                    USER_CHANGES.NEW_USER,
                    USER_CHANGES.STATE
                )
                .values(
                    userChange.createdUtcTimestamp(),
                    userChange.updatedUtcTimestamp(),
                    userChange.previousUser().id(),
                    GSON.toJson(userChange.previousUser()),
                    GSON.toJson(userChange.newUser()),
                    userChange.state().toString()
                )
                .returning(USER_CHANGES.USER_CHANGE_ID);
            SQL_LOGGER.debug("Executing SQL: '{}'", query);

            final int userChangeId = query
                .fetch()
                .get(0)
                .getUserChangeId();
            return UserChange.updateWithId(userChangeId, userChange);
        });
    }

    @Override
    public Collection<UserChange> getAllUserChanges(final Collection<UserChangeState> states, final long numberOfMonths) {
        return numberOfMonths == 0L
            ? getAllUserChangesWithState(states)
            : getAllUserChangesWithStateForPastMonths(states, numberOfMonths);
    }

    private List<UserChange> getAllUserChangesWithState(final Collection<UserChangeState> states) {
        return executeQuery(queryContext -> {
            final var query = queryContext
                .select()
                .from(USER_CHANGES)
                .where(USER_CHANGES.STATE.in(states))
                .orderBy(USER_CHANGES.USER_CHANGE_ID.asc());
            SQL_LOGGER.debug("Executing SQL: '{}'", query);

            return query
                .fetch()
                .into(USER_CHANGES)
                .stream()
                .map(RecordConverter::toUserChange)
                .toList();
        });
    }

    private List<UserChange> getAllUserChangesWithStateForPastMonths(final Collection<UserChangeState> states, final long numberOfMonths) {
        final LocalDateTime toTime = DATE_TIME_UTILS.currentUtcLocalDateTime();
        final LocalDateTime fromTime = toTime.minusMonths(numberOfMonths);

        return executeQuery(queryContext -> {
            final var query = queryContext
                .select()
                .from(USER_CHANGES)
                .where(USER_CHANGES.STATE.in(states))
                .and(USER_CHANGES.UPDATED_UTC_TIMESTAMP.between(fromTime).and(toTime))
                .orderBy(USER_CHANGES.USER_CHANGE_ID.asc());
            SQL_LOGGER.debug("Executing SQL: '{}'", query);

            return query
                .fetch()
                .into(USER_CHANGES)
                .stream()
                .map(RecordConverter::toUserChange)
                .toList();
        });
    }

    @Override
    public Optional<UserChange> getUserChange(final int userChangeId) {
        return executeQuery(queryContext -> {
            final var query = queryContext
                .select()
                .from(USER_CHANGES)
                .where(USER_CHANGES.USER_CHANGE_ID.equal(userChangeId));
            SQL_LOGGER.debug("Executing SQL: '{}'", query);

            return query
                .fetch()
                .into(USER_CHANGES)
                .stream()
                .map(RecordConverter::toUserChange)
                .findAny();
        });
    }

    @Override
    public UserChange updateUserChange(final UserChange userChangeToUpdate) {
        executeQuery(queryContext -> {
            final var query = queryContext
                .update(USER_CHANGES)
                .set(USER_CHANGES.CREATED_UTC_TIMESTAMP, userChangeToUpdate.createdUtcTimestamp())
                .set(USER_CHANGES.UPDATED_UTC_TIMESTAMP, userChangeToUpdate.updatedUtcTimestamp())
                .set(USER_CHANGES.PREVIOUS_USER, GSON.toJson(userChangeToUpdate.previousUser()))
                .set(USER_CHANGES.NEW_USER, GSON.toJson(userChangeToUpdate.newUser()))
                .set(USER_CHANGES.STATE, userChangeToUpdate.state().toString())
                .where(USER_CHANGES.USER_CHANGE_ID.equal(userChangeToUpdate.id()));
            SQL_LOGGER.debug("Executing SQL: '{}'", query);

            return query.execute();
        });

        // The DB makes no change to this object, so we simply return the provided one
        return userChangeToUpdate;
    }

    @Override
    public UserTcStats createHourlyTcStats(final UserTcStats userTcStats) {
        SQL_LOGGER.debug("Inserting TC stats for user ID: {}", userTcStats::userId);

        executeQuery(queryContext -> {
            final var query = queryContext
                .insertInto(USER_TC_STATS_HOURLY)
                .columns(
                    USER_TC_STATS_HOURLY.USER_ID,
                    USER_TC_STATS_HOURLY.UTC_TIMESTAMP,
                    USER_TC_STATS_HOURLY.TC_POINTS,
                    USER_TC_STATS_HOURLY.TC_POINTS_MULTIPLIED,
                    USER_TC_STATS_HOURLY.TC_UNITS
                )
                .values(
                    userTcStats.userId(),
                    DateTimeConverterUtils.toUtcLocalDateTime(userTcStats.timestamp()),
                    userTcStats.points(),
                    userTcStats.multipliedPoints(),
                    userTcStats.units()
                );
            SQL_LOGGER.debug("Executing SQL: '{}'", query);

            return query.execute();
        });

        // The DB makes no change to this object, so we simply return the provided one
        return userTcStats;
    }

    @Override
    public Optional<UserTcStats> getHourlyTcStats(final int userId) {
        return executeQuery(queryContext -> {
            final var query = queryContext
                .select(
                    USER_TC_STATS_HOURLY.USER_ID,
                    USER_TC_STATS_HOURLY.UTC_TIMESTAMP,
                    USER_TC_STATS_HOURLY.TC_POINTS,
                    USER_TC_STATS_HOURLY.TC_POINTS_MULTIPLIED,
                    USER_TC_STATS_HOURLY.TC_UNITS
                )
                .from(USER_TC_STATS_HOURLY)
                .where(USER_TC_STATS_HOURLY.USER_ID.equal(userId))
                .orderBy(USER_TC_STATS_HOURLY.UTC_TIMESTAMP.desc())
                .limit(SINGLE_RESULT);
            SQL_LOGGER.debug("Executing SQL: '{}'", query);

            return query
                .fetch()
                .into(USER_TC_STATS_HOURLY)
                .stream()
                .map(RecordConverter::toUserTcStats)
                .findAny();
        });
    }

    @Override
    public Collection<HistoricStats> getHistoricStatsHourly(final int userId, final Year year, final Month month, final int day) {
        SQL_LOGGER.info("Getting historic hourly user TC stats for {}/{}/{} for user {}", () -> year, () -> formatMonth(month),
            () -> day, () -> userId);

        final String selectSqlStatement = """
            SELECT
                MAX(utc_timestamp) AS hourly_timestamp,
                COALESCE(MAX(tc_points) - LAG(MAX(tc_points)) OVER (ORDER BY MIN(utc_timestamp)), 0) AS diff_points,
                COALESCE(MAX(tc_points_multiplied) - LAG(MAX(tc_points_multiplied)) OVER (ORDER BY MIN(utc_timestamp)), 0) AS diff_points_multiplied,
                COALESCE(MAX(tc_units) - LAG(MAX(tc_units)) OVER (ORDER BY MIN(utc_timestamp)), 0) AS diff_units
             FROM user_tc_stats_hourly
             WHERE
                utc_timestamp BETWEEN ? AND ?
             AND
                user_id = ?
             GROUP BY EXTRACT(HOUR FROM utc_timestamp)
             ORDER BY EXTRACT(HOUR FROM utc_timestamp) ASC;
            """;

        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(selectSqlStatement)) {

            preparedStatement.setTimestamp(1, DateTimeConverterUtils.getFirstTimestampOf(year, month, day));
            preparedStatement.setTimestamp(2, DateTimeConverterUtils.getLastTimestampOf(year, month, day));
            preparedStatement.setInt(3, userId);

            SQL_LOGGER.debug("Executing prepared statement: '{}'", preparedStatement);
            try (final ResultSet resultSet = preparedStatement.executeQuery()) {

                final Collection<HistoricStats> userStats = new ArrayList<>();

                // First entry will be zeroed, so we need to manually get the first hour's stats for the user
                if (resultSet.next()) {
                    final UserTcStats userTcStats = getTcStatsForFirstHourOfDay(userId, year, month, day);

                    userStats.add(
                        HistoricStats.create(
                            resultSet.getTimestamp("hourly_timestamp").toLocalDateTime(),
                            userTcStats.points(),
                            userTcStats.multipliedPoints(),
                            userTcStats.units()
                        )
                    );
                }

                // All remaining entries will be diff-ed from the previous entry
                while (resultSet.next()) {
                    userStats.add(
                        HistoricStats.create(
                            resultSet.getTimestamp("hourly_timestamp").toLocalDateTime(),
                            resultSet.getLong("diff_points"),
                            resultSet.getLong("diff_points_multiplied"),
                            resultSet.getInt("diff_units")
                        )
                    );
                }

                return userStats;
            }
        } catch (final DatabaseConnectionException e) {
            SQL_LOGGER.warn("Unable to get the stats for the first hour of {}/{}/{} for user {}", year, formatMonth(month), day,
                userId);
            throw e;
        } catch (final SQLException e) {
            throw new DatabaseConnectionException("Error opening connection to the DB", e);
        }
    }

    private UserTcStats getCurrentDayFirstHourTcStats(final int userId, final int day, final Month month, final Year year) {
        SQL_LOGGER.debug("Getting current day's first hour TC stats for user {} on {}/{}/{}", () -> userId, year::getValue, month::getValue,
            () -> day);

        return executeQuery(queryContext -> {
            final LocalDateTime start = DateTimeConverterUtils.getLocalDateTimeOf(year, month, day, 0, 0, 0);
            final LocalDateTime end = DateTimeConverterUtils.getLocalDateTimeOf(year, month, day, 0, 59, 59);

            final var query = queryContext
                .select(
                    max(USER_TC_STATS_HOURLY.USER_ID).as(USER_TC_STATS_HOURLY.USER_ID),
                    max(USER_TC_STATS_HOURLY.UTC_TIMESTAMP).as(USER_TC_STATS_HOURLY.UTC_TIMESTAMP),
                    max(USER_TC_STATS_HOURLY.TC_POINTS).as(USER_TC_STATS_HOURLY.TC_POINTS),
                    max(USER_TC_STATS_HOURLY.TC_POINTS_MULTIPLIED).as(USER_TC_STATS_HOURLY.TC_POINTS_MULTIPLIED),
                    max(USER_TC_STATS_HOURLY.TC_UNITS).as(USER_TC_STATS_HOURLY.TC_UNITS)
                )
                .from(USER_TC_STATS_HOURLY)
                .where(USER_TC_STATS_HOURLY.UTC_TIMESTAMP.between(start, end))
                .and(USER_TC_STATS_HOURLY.USER_ID.equal(userId))
                .groupBy(hour(USER_TC_STATS_HOURLY.UTC_TIMESTAMP))
                .orderBy(hour(USER_TC_STATS_HOURLY.UTC_TIMESTAMP).asc())
                .limit(SINGLE_RESULT);
            SQL_LOGGER.debug("Executing SQL: '{}'", query);

            return query
                .fetch()
                .into(USER_TC_STATS_HOURLY)
                .stream()
                .map(RecordConverter::toUserTcStats)
                .findAny()
                .orElse(UserTcStats.empty(userId));
        });
    }

    private UserTcStats getPreviousDayLastHourTcStats(final int userId, final int day, final Month month, final Year year) {
        SQL_LOGGER.debug("Getting previous day's last hour TC stats for user {} on {}/{}/{}", () -> userId, year::getValue, month::getValue,
            () -> day);

        return executeQuery(queryContext -> {
            final LocalDateTime start = DateTimeConverterUtils.getLocalDateTimeOf(year, month, day, 23, 0, 0);
            final LocalDateTime end = DateTimeConverterUtils.getLocalDateTimeOf(year, month, day, 23, 59, 59);

            final var query = queryContext
                .select(
                    max(USER_TC_STATS_HOURLY.USER_ID).as(USER_TC_STATS_HOURLY.USER_ID),
                    max(USER_TC_STATS_HOURLY.UTC_TIMESTAMP).as(USER_TC_STATS_HOURLY.UTC_TIMESTAMP),
                    max(USER_TC_STATS_HOURLY.TC_POINTS).as(USER_TC_STATS_HOURLY.TC_POINTS),
                    max(USER_TC_STATS_HOURLY.TC_POINTS_MULTIPLIED).as(USER_TC_STATS_HOURLY.TC_POINTS_MULTIPLIED),
                    max(USER_TC_STATS_HOURLY.TC_UNITS).as(USER_TC_STATS_HOURLY.TC_UNITS)
                )
                .from(USER_TC_STATS_HOURLY)
                .where(USER_TC_STATS_HOURLY.UTC_TIMESTAMP.between(start, end))
                .and(USER_TC_STATS_HOURLY.USER_ID.equal(userId))
                .groupBy(hour(USER_TC_STATS_HOURLY.UTC_TIMESTAMP))
                .orderBy(hour(USER_TC_STATS_HOURLY.UTC_TIMESTAMP).desc())
                .limit(SINGLE_RESULT);
            SQL_LOGGER.debug("Executing SQL: '{}'", query);

            return query
                .fetch()
                .into(USER_TC_STATS_HOURLY)
                .stream()
                .map(RecordConverter::toUserTcStats)
                .findAny()
                .orElse(UserTcStats.empty(userId));
        });
    }

    private UserTcStats getTcStatsForFirstHourOfDay(final int userId, final Year year, final Month month, final int day) {
        final UserTcStats firstHourTcStatsCurrentDay = getCurrentDayFirstHourTcStats(userId, day, month, year);

        final boolean isFirstDay = day == 1;
        final int previousDay = day - 1;
        final UserTcStats lastHourTcStatsPreviousDay =
            isFirstDay ? UserTcStats.empty(userId) : getPreviousDayLastHourTcStats(userId, previousDay, month, year);

        if (lastHourTcStatsPreviousDay.isEmpty()) {

            if (firstHourTcStatsCurrentDay.isEmpty()) {
                return UserTcStats.empty(userId);
            }

            // If no stats in previous day (meaning we are getting historic stats for the first day available),
            // we need to remove the initial points from the current day's points
            final UserStats initialStats = getInitialStats(userId).orElse(UserStats.empty());
            SQL_LOGGER.debug("Removing initial stats from current day's first hour stats: {} - {}", firstHourTcStatsCurrentDay, initialStats);

            // Since we didn't get any previous day's stats, we don't need to worry about the hardware multiplier having been changed
            // As a result, we will get the user's current hardware and use that multiplier
            final Optional<User> optionalUser = getUser(userId);

            if (optionalUser.isEmpty()) {
                SQL_LOGGER.warn("Could not find user with ID {}, returning empty stats for first hour of day", userId);
                return UserTcStats.empty(userId);
            }

            final User user = optionalUser.get();
            final Hardware hardware = user.hardware();
            final double hardwareMultiplier = hardware.multiplier();

            return UserTcStats.create(
                firstHourTcStatsCurrentDay.userId(),
                firstHourTcStatsCurrentDay.timestamp(),
                Math.max(0L, firstHourTcStatsCurrentDay.points() - initialStats.points()),
                Math.max(0L, firstHourTcStatsCurrentDay.multipliedPoints() - Math.round(hardwareMultiplier * initialStats.points())),
                Math.max(0, firstHourTcStatsCurrentDay.units() - initialStats.units())
            );
        }

        SQL_LOGGER.info("Removing previous day's last hour stats from current day's first hour stats: {} - {}", firstHourTcStatsCurrentDay,
            lastHourTcStatsPreviousDay);
        return UserTcStats.create(
            firstHourTcStatsCurrentDay.userId(),
            firstHourTcStatsCurrentDay.timestamp(),
            Math.max(0L, firstHourTcStatsCurrentDay.points() - lastHourTcStatsPreviousDay.points()),
            Math.max(0L, firstHourTcStatsCurrentDay.multipliedPoints() - lastHourTcStatsPreviousDay.multipliedPoints()),
            Math.max(0, firstHourTcStatsCurrentDay.units() - lastHourTcStatsPreviousDay.units())
        );
    }

    @Override
    public Collection<HistoricStats> getHistoricStatsDaily(final int userId, final Year year, final Month month) {
        SQL_LOGGER.info("Getting historic daily user TC stats for {}/{} for user {}", () -> formatMonth(month), () -> year, () -> userId);

        final String selectSqlStatement = """
            SELECT utc_timestamp::DATE AS daily_timestamp,
                COALESCE(MAX(tc_points) - LAG(MAX(tc_points)) OVER (ORDER BY MIN(utc_timestamp)), 0) AS diff_points,
                COALESCE(MAX(tc_points_multiplied) - LAG(MAX(tc_points_multiplied)) OVER (ORDER BY MIN(utc_timestamp)), 0) AS diff_points_multiplied,
                COALESCE(MAX(tc_units) - LAG(MAX(tc_units)) OVER (ORDER BY MIN(utc_timestamp)), 0) AS diff_units
             FROM user_tc_stats_hourly
             WHERE
                EXTRACT(MONTH FROM utc_timestamp) = ?
             AND
                EXTRACT(YEAR FROM utc_timestamp) = ?
             AND
                user_id = ?
             GROUP BY utc_timestamp::DATE
             ORDER BY utc_timestamp::DATE ASC;
            """;

        try (final Connection connection = dataSource.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(selectSqlStatement)) {

            preparedStatement.setInt(1, month.getValue());
            preparedStatement.setInt(2, year.getValue());
            preparedStatement.setInt(3, userId);

            SQL_LOGGER.debug("Executing prepared statement: '{}'", preparedStatement);
            try (final ResultSet resultSet = preparedStatement.executeQuery()) {

                final Collection<HistoricStats> userStats = new ArrayList<>();

                // First entry will be zeroed, so we need to manually get the first day's stats for the user
                if (resultSet.next()) {
                    final LocalDateTime localDateTime = resultSet.getTimestamp("daily_timestamp").toLocalDateTime();
                    final UserTcStats userTcStats = getTcStatsForFirstDayOfMonth(localDateTime, userId);

                    if (userTcStats.isEmpty()) {
                        SQL_LOGGER.warn("Error getting historic stats for first day of {} for user with ID {}", formatMonth(month),
                            userId);
                    } else {
                        userStats.add(
                            HistoricStats.create(
                                localDateTime,
                                userTcStats.points(),
                                userTcStats.multipliedPoints(),
                                userTcStats.units()
                            )
                        );
                    }
                }

                // All remaining entries will be diff-ed from the previous entry
                while (resultSet.next()) {
                    userStats.add(
                        HistoricStats.create(
                            resultSet.getTimestamp("daily_timestamp").toLocalDateTime(),
                            resultSet.getLong("diff_points"),
                            resultSet.getLong("diff_points_multiplied"),
                            resultSet.getInt("diff_units")
                        )
                    );
                }

                return userStats;
            }
        } catch (final SQLException e) {
            throw new DatabaseConnectionException("Error opening connection to the DB", e);
        }
    }

    @Override
    public Collection<HistoricStats> getHistoricStatsMonthly(final int userId, final Year year) {
        SQL_LOGGER.debug("Getting historic monthly user TC stats for {} for user {}", year, userId);

        return executeQuery(queryContext -> {
            final var query = queryContext
                .select(
                    max(USER_TC_STATS_HOURLY.UTC_TIMESTAMP).as(USER_TC_STATS_HOURLY.UTC_TIMESTAMP.getName()),
                    max(USER_TC_STATS_HOURLY.TC_POINTS).as(USER_TC_STATS_HOURLY.TC_POINTS.getName()),
                    max(USER_TC_STATS_HOURLY.TC_POINTS_MULTIPLIED).as(USER_TC_STATS_HOURLY.TC_POINTS_MULTIPLIED.getName()),
                    max(USER_TC_STATS_HOURLY.TC_UNITS).as(USER_TC_STATS_HOURLY.TC_UNITS.getName())
                )
                .from(USER_TC_STATS_HOURLY)
                .where(year(USER_TC_STATS_HOURLY.UTC_TIMESTAMP).equal(year.getValue()))
                .and(USER_TC_STATS_HOURLY.USER_ID.equal(userId))
                .groupBy(month(USER_TC_STATS_HOURLY.UTC_TIMESTAMP).cast(int.class))
                .orderBy(month(USER_TC_STATS_HOURLY.UTC_TIMESTAMP).cast(int.class).asc());
            SQL_LOGGER.debug("Executing SQL: '{}'", query);

            return query.fetch()
                .into(USER_TC_STATS_HOURLY)
                .stream()
                .map(RecordConverter::toHistoricStats)
                .toList();
        });
    }

    private UserTcStats getTcStatsForFirstDayOfMonth(final LocalDateTime localDateTime, final int userId) {
        SQL_LOGGER.debug("Getting TC stats for user {} on {}", userId, localDateTime);

        return executeQuery(queryContext -> {
            final var query = queryContext
                .select()
                .from(USER_TC_STATS_HOURLY)
                .where(day(USER_TC_STATS_HOURLY.UTC_TIMESTAMP).equal(localDateTime.getDayOfMonth()))
                .and(month(USER_TC_STATS_HOURLY.UTC_TIMESTAMP).equal(localDateTime.getMonth().getValue()))
                .and(year(USER_TC_STATS_HOURLY.UTC_TIMESTAMP).equal(localDateTime.getYear()))
                .and(USER_TC_STATS_HOURLY.USER_ID.equal(userId))
                .orderBy(hour(USER_TC_STATS_HOURLY.UTC_TIMESTAMP).desc())
                .limit(SINGLE_RESULT);
            SQL_LOGGER.debug("Executing SQL: '{}'", query);

            return query
                .fetch()
                .into(USER_TC_STATS_HOURLY)
                .stream()
                .map(RecordConverter::toUserTcStats)
                .findAny()
                .orElse(UserTcStats.empty(userId));
        });
    }

    @Override
    public UserStats createInitialStats(final UserStats userStats) {
        SQL_LOGGER.debug("Inserting initial stats for user {} to DB", userStats::userId);

        executeQuery(queryContext -> {
            final var query = queryContext
                .insertInto(USER_INITIAL_STATS)
                .columns(
                    USER_INITIAL_STATS.USER_ID,
                    USER_INITIAL_STATS.UTC_TIMESTAMP,
                    USER_INITIAL_STATS.INITIAL_POINTS,
                    USER_INITIAL_STATS.INITIAL_UNITS
                )
                .values(
                    userStats.userId(),
                    DateTimeConverterUtils.toUtcLocalDateTime(userStats.timestamp()),
                    userStats.points(),
                    userStats.units());
            SQL_LOGGER.debug("Executing SQL: '{}'", query);

            return query.execute();
        });

        return userStats;
    }

    @Override
    public Optional<UserStats> getInitialStats(final int userId) {
        SQL_LOGGER.debug("Getting initial stats for user ID: {}", userId);

        return executeQuery(queryContext -> {
            final var query = queryContext
                .select()
                .from(USER_INITIAL_STATS)
                .where(USER_INITIAL_STATS.USER_ID.equal(userId))
                .orderBy(USER_INITIAL_STATS.UTC_TIMESTAMP.desc())
                .limit(SINGLE_RESULT);
            SQL_LOGGER.debug("Executing SQL: '{}'", query);

            return query
                .fetch()
                .into(USER_INITIAL_STATS)
                .stream()
                .map(RecordConverter::toUserStats)
                .findAny();
        });
    }

    @Override
    public UserStats createTotalStats(final UserStats userStats) {
        SQL_LOGGER.debug("Inserting total stats for user ID {} to DB", userStats::userId);

        executeQuery(queryContext -> {
            final var query = queryContext
                .insertInto(USER_TOTAL_STATS)
                .columns(USER_TOTAL_STATS.USER_ID, USER_TOTAL_STATS.UTC_TIMESTAMP, USER_TOTAL_STATS.TOTAL_POINTS, USER_TOTAL_STATS.TOTAL_UNITS)
                .values(userStats.userId(), DateTimeConverterUtils.toUtcLocalDateTime(userStats.timestamp()), userStats.points(),
                    userStats.units());
            SQL_LOGGER.debug("Executing SQL: '{}'", query);

            return query.execute();
        });

        // The DB makes no change to this object, so we simply return the provided one
        return userStats;
    }

    @Override
    public Optional<UserStats> getTotalStats(final int userId) {
        SQL_LOGGER.debug("Getting total stats for user ID: {}", userId);

        return executeQuery(queryContext -> {
            final var query = queryContext
                .select()
                .from(USER_TOTAL_STATS)
                .where(USER_TOTAL_STATS.USER_ID.equal(userId))
                .orderBy(USER_TOTAL_STATS.UTC_TIMESTAMP.desc())
                .limit(SINGLE_RESULT);
            SQL_LOGGER.debug("Executing SQL: '{}'", query);

            return query
                .fetch()
                .into(USER_TOTAL_STATS)
                .stream()
                .map(RecordConverter::toUserStats)
                .findAny();
        });
    }

    @Override
    public OffsetTcStats createOrUpdateOffsetStats(final int userId, final OffsetTcStats offsetTcStats) {
        SQL_LOGGER.debug("Adding/updating offset stats for user {}", userId);

        return executeQuery(queryContext -> {
            final LocalDateTime currentUtcLocalDateTime = DATE_TIME_UTILS.currentUtcLocalDateTime();

            final var query = queryContext
                .insertInto(USER_OFFSET_TC_STATS)
                .columns(
                    USER_OFFSET_TC_STATS.USER_ID,
                    USER_OFFSET_TC_STATS.UTC_TIMESTAMP,
                    USER_OFFSET_TC_STATS.OFFSET_POINTS,
                    USER_OFFSET_TC_STATS.OFFSET_MULTIPLIED_POINTS,
                    USER_OFFSET_TC_STATS.OFFSET_UNITS
                )
                .values(
                    userId, currentUtcLocalDateTime,
                    offsetTcStats.pointsOffset(),
                    offsetTcStats.multipliedPointsOffset(),
                    offsetTcStats.unitsOffset()
                )
                .onConflict(USER_OFFSET_TC_STATS.USER_ID)
                .doUpdate()
                .set(USER_OFFSET_TC_STATS.UTC_TIMESTAMP, currentUtcLocalDateTime)
                .set(USER_OFFSET_TC_STATS.OFFSET_POINTS, USER_OFFSET_TC_STATS.OFFSET_POINTS.plus(offsetTcStats.pointsOffset()))
                .set(USER_OFFSET_TC_STATS.OFFSET_MULTIPLIED_POINTS,
                    USER_OFFSET_TC_STATS.OFFSET_MULTIPLIED_POINTS.plus(offsetTcStats.multipliedPointsOffset()))
                .set(USER_OFFSET_TC_STATS.OFFSET_UNITS, USER_OFFSET_TC_STATS.OFFSET_UNITS.plus(offsetTcStats.unitsOffset()))
                .returning();
            SQL_LOGGER.debug("Executing SQL: '{}'", query);

            return query
                .fetch()
                .into(USER_OFFSET_TC_STATS)
                .stream()
                .map(RecordConverter::toOffsetStats)
                .findAny()
                .orElse(OffsetTcStats.empty());
        });
    }

    @Override
    public Optional<OffsetTcStats> getOffsetStats(final int userId) {
        return executeQuery(queryContext -> {
            final var query = queryContext
                .select(USER_OFFSET_TC_STATS.OFFSET_POINTS, USER_OFFSET_TC_STATS.OFFSET_MULTIPLIED_POINTS, USER_OFFSET_TC_STATS.OFFSET_UNITS)
                .from(USER_OFFSET_TC_STATS)
                .where(USER_OFFSET_TC_STATS.USER_ID.equal(userId))
                .orderBy(USER_OFFSET_TC_STATS.UTC_TIMESTAMP.desc())
                .limit(SINGLE_RESULT);
            SQL_LOGGER.debug("Executing SQL: '{}'", query);

            return query
                .fetch()
                .into(USER_OFFSET_TC_STATS)
                .stream()
                .map(RecordConverter::toOffsetStats)
                .findAny();
        });
    }

    @Override
    public void deleteOffsetStats(final int userId) {
        executeQuery(queryContext -> {
            final var query = queryContext
                .deleteFrom(USER_OFFSET_TC_STATS)
                .where(USER_OFFSET_TC_STATS.USER_ID.equal(userId));
            SQL_LOGGER.debug("Executing SQL: '{}'", query);

            return query.execute();
        });
    }

    @Override
    public void deleteAllOffsetStats() {
        executeQuery(queryContext -> {
            final var query = queryContext
                .deleteFrom(USER_OFFSET_TC_STATS);
            SQL_LOGGER.debug("Executing SQL: '{}'", query);

            return query.execute();
        });
    }

    @Override
    public RetiredUserTcStats createRetiredUserStats(final RetiredUserTcStats retiredUserTcStats) {
        return executeQuery(queryContext -> {
            final LocalDateTime currentUtcLocalDateTime = DATE_TIME_UTILS.currentUtcLocalDateTime();

            final var query = queryContext
                .insertInto(RETIRED_USER_STATS)
                .columns(
                    RETIRED_USER_STATS.TEAM_ID,
                    RETIRED_USER_STATS.USER_ID,
                    RETIRED_USER_STATS.DISPLAY_USERNAME,
                    RETIRED_USER_STATS.UTC_TIMESTAMP,
                    RETIRED_USER_STATS.FINAL_POINTS,
                    RETIRED_USER_STATS.FINAL_MULTIPLIED_POINTS,
                    RETIRED_USER_STATS.FINAL_UNITS
                )
                .values(
                    retiredUserTcStats.teamId(),
                    retiredUserTcStats.userId(),
                    retiredUserTcStats.displayName(),
                    currentUtcLocalDateTime,
                    retiredUserTcStats.points(),
                    retiredUserTcStats.multipliedPoints(),
                    retiredUserTcStats.units()
                )
                .onConflict(RETIRED_USER_STATS.USER_ID)
                .doUpdate()
                .set(RETIRED_USER_STATS.TEAM_ID, retiredUserTcStats.teamId())
                .set(RETIRED_USER_STATS.UTC_TIMESTAMP, currentUtcLocalDateTime)
                .set(RETIRED_USER_STATS.DISPLAY_USERNAME, retiredUserTcStats.displayName())
                .set(RETIRED_USER_STATS.FINAL_POINTS, retiredUserTcStats.points())
                .set(RETIRED_USER_STATS.FINAL_MULTIPLIED_POINTS, retiredUserTcStats.multipliedPoints())
                .set(RETIRED_USER_STATS.FINAL_UNITS, retiredUserTcStats.units())
                .returning();
            SQL_LOGGER.debug("Executing SQL: '{}'", query);

            final int retiredUserId = query
                .fetch()
                .get(0)
                .getRetiredUserId();
            return RetiredUserTcStats.updateWithId(retiredUserId, retiredUserTcStats);
        });
    }

    @Override
    public Collection<RetiredUserTcStats> getAllRetiredUserStats() {
        return executeQuery(queryContext -> {
            final var query = queryContext
                .select()
                .from(RETIRED_USER_STATS)
                .orderBy(RETIRED_USER_STATS.RETIRED_USER_ID.asc());
            SQL_LOGGER.debug("Executing SQL: '{}'", query);

            return query
                .fetch()
                .into(RETIRED_USER_STATS)
                .stream()
                .map(RecordConverter::toRetiredUserStats)
                .toList();
        });
    }

    @Override
    public void deleteAllRetiredUserStats() {
        executeQuery(queryContext -> {
            final var query = queryContext
                .deleteFrom(RETIRED_USER_STATS);
            SQL_LOGGER.debug("Executing SQL: '{}'", query);

            return query.execute();
        });
    }

    @Override
    public MonthlyResult createMonthlyResult(final MonthlyResult monthlyResult) {
        SQL_LOGGER.debug("Persisting monthly result for {}/{}",
            () -> monthlyResult.utcTimestamp().getYear(),
            () -> formatMonth(monthlyResult.utcTimestamp().getMonth())
        );

        executeQuery(queryContext -> {
            final var query = queryContext
                .insertInto(MONTHLY_RESULTS)
                .columns(MONTHLY_RESULTS.UTC_TIMESTAMP, MONTHLY_RESULTS.JSON_RESULT)
                .values(monthlyResult.utcTimestamp(), GSON.toJson(monthlyResult));

            SQL_LOGGER.debug("Executing SQL: '{}'", query);

            return query.execute();
        });

        // The DB makes no change to this object, so we simply return the provided one
        return monthlyResult;
    }

    @Override
    public Optional<MonthlyResult> getMonthlyResult(final Month month, final Year year) {
        SQL_LOGGER.debug("Retrieving monthly result for {}/{}", () -> year, () -> formatMonth(month));

        return executeQuery(queryContext -> {
            final var query = queryContext
                .select(MONTHLY_RESULTS.JSON_RESULT)
                .from(MONTHLY_RESULTS)
                .where(year(MONTHLY_RESULTS.UTC_TIMESTAMP).equal(year.getValue()))
                .and(month(MONTHLY_RESULTS.UTC_TIMESTAMP).equal(month.getValue()))
                .orderBy(MONTHLY_RESULTS.UTC_TIMESTAMP.desc())
                .limit(SINGLE_RESULT);

            SQL_LOGGER.debug("Executing SQL: '{}'", query);

            return query
                .fetch()
                .into(MONTHLY_RESULTS)
                .stream()
                .map(RecordConverter::toMonthlyResult)
                .findAny();
        });
    }

    @Override
    public UserAuthenticationResult authenticateSystemUser(final String userName, final String password) {
        SQL_LOGGER.debug("Checking if supplied username '{}' and password is valid user, then returning roles", userName);

        return executeQuery(queryContext -> {
            final var query = queryContext
                .select(
                    field(crypt(password, SYSTEM_USERS.USER_PASSWORD_HASH.getValue(
                            queryContext
                                .select()
                                .from(SYSTEM_USERS)
                                .where(SYSTEM_USERS.USER_NAME.equalIgnoreCase(userName))
                                .fetch()
                                .into(SYSTEM_USERS)
                                .stream()
                                .findAny()
                                .orElse(SYSTEM_USERS.newRecord())
                        )
                    ).equal(SYSTEM_USERS.USER_PASSWORD_HASH)).as("is_password_match"),
                    SYSTEM_USERS.USER_NAME,
                    SYSTEM_USERS.ROLES
                )
                .from(SYSTEM_USERS)
                .where(SYSTEM_USERS.USER_NAME.equal(userName));
            SQL_LOGGER.debug("Executing SQL: '{}'", query);

            return query
                .fetch()
                .stream()
                .map(RecordConverter::toSystemUserAuthentication)
                .findAny()
                .orElse(UserAuthenticationResult.userDoesNotExist());
        });
    }

    private <T> T executeQuery(final Function<? super DSLContext, T> sqlQuery) {
        try (final Connection connection = dataSource.getConnection()) {
            final DSLContext queryContext = DSL.using(connection, SQLDialect.POSTGRES);
            return sqlQuery.apply(queryContext);
        } catch (final SQLException e) {
            throw new DatabaseConnectionException("Error closing DB connection", e);
        }
    }
}

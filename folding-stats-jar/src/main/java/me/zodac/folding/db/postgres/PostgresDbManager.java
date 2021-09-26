package me.zodac.folding.db.postgres;

import static java.util.stream.Collectors.toList;
import static me.zodac.folding.db.postgres.gen.Routines.crypt;
import static me.zodac.folding.db.postgres.gen.tables.Hardware.HARDWARE;
import static me.zodac.folding.db.postgres.gen.tables.MonthlyResults.MONTHLY_RESULTS;
import static me.zodac.folding.db.postgres.gen.tables.RetiredUserStats.RETIRED_USER_STATS;
import static me.zodac.folding.db.postgres.gen.tables.SystemUsers.SYSTEM_USERS;
import static me.zodac.folding.db.postgres.gen.tables.Teams.TEAMS;
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
import me.zodac.folding.api.UserAuthenticationResult;
import me.zodac.folding.api.db.DbConnectionPool;
import me.zodac.folding.api.db.DbManager;
import me.zodac.folding.api.exception.DatabaseConnectionException;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.tc.stats.OffsetStats;
import me.zodac.folding.api.tc.stats.RetiredUserTcStats;
import me.zodac.folding.api.tc.stats.UserStats;
import me.zodac.folding.api.tc.stats.UserTcStats;
import me.zodac.folding.api.utils.DateTimeUtils;
import me.zodac.folding.db.postgres.gen.tables.records.HardwareRecord;
import me.zodac.folding.db.postgres.gen.tables.records.TeamsRecord;
import me.zodac.folding.db.postgres.gen.tables.records.UsersRecord;
import me.zodac.folding.rest.api.tc.historic.HistoricStats;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jooq.DSLContext;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

/**
 * Implementation of {@link DbManager} for PostgreSQL databases.
 * <p>
 * Uses <b>jOOQ</b> for code generation for the DB tables/schemas, rather than direct SQL queries. See existing methods for examples.
 */
public final class PostgresDbManager implements DbManager {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final int SINGLE_RESULT = 1;

    private final DbConnectionPool dbConnectionPool;

    private PostgresDbManager(final DbConnectionPool dbConnectionPool) {
        this.dbConnectionPool = dbConnectionPool;
    }

    /**
     * Creates an instance of {@link PostgresDbManager}.
     *
     * @param dbConnectionPool the {@link DbConnectionPool} for this instance
     * @return the created {@link PostgresDbManager}
     */
    public static PostgresDbManager create(final DbConnectionPool dbConnectionPool) {
        return new PostgresDbManager(dbConnectionPool);
    }

    @Override
    public Hardware createHardware(final Hardware hardware) {
        return executeQuery(queryContext -> {
            final var query = queryContext
                .insertInto(HARDWARE)
                .columns(HARDWARE.HARDWARE_NAME, HARDWARE.DISPLAY_NAME, HARDWARE.HARDWARE_MAKE, HARDWARE.HARDWARE_TYPE, HARDWARE.AVERAGE_PPD,
                    HARDWARE.MULTIPLIER)
                .values(hardware.getHardwareName(), hardware.getDisplayName(),
                    "", "GPU", BigDecimal.valueOf(1.0D), // TODO: [zodac] Hardcoded for now until LARS automation implemented
                    BigDecimal.valueOf(hardware.getMultiplier()))
                .returning(HARDWARE.HARDWARE_ID);
            LOGGER.debug("Executing SQL: '{}'", query);

            final Result<HardwareRecord> hardwareRecordResult = query.fetch();
            final int hardwareId = hardwareRecordResult.get(0).getHardwareId();
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
            LOGGER.debug("Executing SQL: '{}'", query);

            return query
                .fetch()
                .into(HARDWARE)
                .stream()
                .map(RecordConverter::toHardware)
                .collect(toList());
        });
    }

    @Override
    public Optional<Hardware> getHardware(final int hardwareId) {
        return executeQuery(queryContext -> {
            final var query = queryContext
                .select()
                .from(HARDWARE)
                .where(HARDWARE.HARDWARE_ID.equal(hardwareId));
            LOGGER.debug("Executing SQL: '{}'", query);

            return query
                .fetch()
                .into(HARDWARE)
                .stream()
                .map(RecordConverter::toHardware)
                .findAny();
        });
    }

    @Override
    public void updateHardware(final Hardware hardware) {
        executeQuery(queryContext -> {
            final var query = queryContext
                .update(HARDWARE)
                .set(HARDWARE.HARDWARE_NAME, hardware.getHardwareName())
                .set(HARDWARE.DISPLAY_NAME, hardware.getDisplayName())
                .set(HARDWARE.MULTIPLIER, BigDecimal.valueOf(hardware.getMultiplier()))
                // TODO: [zodac] Hardcoded for now until LARS automation implemented
                .set(HARDWARE.HARDWARE_MAKE, "")
                .set(HARDWARE.HARDWARE_TYPE, "GPU")
                .set(HARDWARE.AVERAGE_PPD, BigDecimal.valueOf(1.0D))
                .where(HARDWARE.HARDWARE_ID.equal(hardware.getId()));
            LOGGER.debug("Executing SQL: '{}'", query);

            return query.execute();
        });
    }

    @Override
    public void deleteHardware(final int hardwareId) {
        executeQuery(queryContext -> {
            final var query = queryContext
                .deleteFrom(HARDWARE)
                .where(HARDWARE.HARDWARE_ID.equal(hardwareId));
            LOGGER.debug("Executing SQL: '{}'", query);

            return query.execute();
        });
    }

    @Override
    public Team createTeam(final Team team) {
        return executeQuery(queryContext -> {
            final var query = queryContext
                .insertInto(TEAMS)
                .columns(TEAMS.TEAM_NAME, TEAMS.TEAM_DESCRIPTION, TEAMS.FORUM_LINK)
                .values(team.getTeamName(), team.getTeamDescription(), team.getForumLink())
                .returning(TEAMS.TEAM_ID);
            LOGGER.debug("Executing SQL: '{}'", query);

            final Result<TeamsRecord> teamRecordResult = query.fetch();
            final int teamId = teamRecordResult.get(0).getTeamId();
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
            LOGGER.debug("Executing SQL: '{}'", query);

            return query
                .fetch()
                .into(TEAMS)
                .stream()
                .map(RecordConverter::toTeam)
                .collect(toList());
        });
    }

    @Override
    public Optional<Team> getTeam(final int teamId) {
        return executeQuery(queryContext -> {
            final var query = queryContext
                .select()
                .from(TEAMS)
                .where(TEAMS.TEAM_ID.equal(teamId));
            LOGGER.debug("Executing SQL: '{}'", query);

            return query
                .fetch()
                .into(TEAMS)
                .stream()
                .map(RecordConverter::toTeam)
                .findAny();
        });
    }

    @Override
    public void updateTeam(final Team team) {
        executeQuery(queryContext -> {
            final var query = queryContext
                .update(TEAMS)
                .set(TEAMS.TEAM_NAME, team.getTeamName())
                .set(TEAMS.TEAM_DESCRIPTION, team.getTeamDescription())
                .set(TEAMS.FORUM_LINK, team.getForumLink())
                .where(TEAMS.TEAM_ID.equal(team.getId()));
            LOGGER.debug("Executing SQL: '{}'", query);

            return query.execute();
        });
    }

    @Override
    public void deleteTeam(final int teamId) {
        LOGGER.debug("Deleting team {} from DB", teamId);
        executeQuery(queryContext -> {
            final var query = queryContext
                .deleteFrom(TEAMS)
                .where(TEAMS.TEAM_ID.equal(teamId));
            LOGGER.debug("Executing SQL: '{}'", query);

            return query.execute();
        });
    }

    @Override
    public User createUser(final User user) {
        return executeQuery(queryContext -> {
            final var query = queryContext
                .insertInto(USERS)
                .columns(USERS.FOLDING_USERNAME, USERS.DISPLAY_USERNAME, USERS.PASSKEY, USERS.CATEGORY, USERS.PROFILE_LINK, USERS.LIVE_STATS_LINK,
                    USERS.HARDWARE_ID, USERS.TEAM_ID, USERS.IS_CAPTAIN)
                .values(user.getFoldingUserName(), user.getDisplayName(), user.getPasskey(), user.getCategory().toString(), user.getProfileLink(),
                    user.getLiveStatsLink(), user.getHardware().getId(), user.getTeam().getId(), user.isUserIsCaptain())
                .returning(USERS.USER_ID);
            LOGGER.debug("Executing SQL: '{}'", query);

            final Result<UsersRecord> usersRecordResult = query.fetch();
            final int userId = usersRecordResult.get(0).getUserId();
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
            LOGGER.debug("Executing SQL: '{}'", query);

            return query
                .fetch()
                .stream()
                .map(RecordConverter::toUser)
                .collect(toList());
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
            LOGGER.debug("Executing SQL: '{}'", query);

            return query
                .fetch()
                .stream()
                .map(RecordConverter::toUser)
                .findAny();
        });
    }

    @Override
    public void updateUser(final User user) {
        executeQuery(queryContext -> {
            final var query = queryContext
                .update(USERS)
                .set(USERS.FOLDING_USERNAME, user.getFoldingUserName())
                .set(USERS.DISPLAY_USERNAME, user.getDisplayName())
                .set(USERS.PASSKEY, user.getPasskey())
                .set(USERS.CATEGORY, user.getCategory().toString())
                .set(USERS.PROFILE_LINK, user.getProfileLink())
                .set(USERS.LIVE_STATS_LINK, user.getLiveStatsLink())
                .set(USERS.HARDWARE_ID, user.getHardware().getId())
                .set(USERS.TEAM_ID, user.getTeam().getId())
                .set(USERS.IS_CAPTAIN, user.isUserIsCaptain())
                .where(USERS.USER_ID.equal(user.getId()));
            LOGGER.debug("Executing SQL: '{}'", query);

            return query.execute();
        });
    }

    @Override
    public void deleteUser(final int userId) {
        executeQuery(queryContext -> {
            final var query = queryContext
                .deleteFrom(USERS)
                .where(USERS.USER_ID.equal(userId));
            LOGGER.debug("Executing SQL: '{}'", query);

            return query.execute();
        });
    }

    @Override
    public void persistHourlyTcStats(final UserTcStats userTcStats) {
        LOGGER.debug("Inserting TC stats for user ID: {}", userTcStats::getUserId);

        executeQuery(queryContext -> {
            final var query = queryContext
                .insertInto(USER_TC_STATS_HOURLY)
                .columns(USER_TC_STATS_HOURLY.USER_ID, USER_TC_STATS_HOURLY.UTC_TIMESTAMP, USER_TC_STATS_HOURLY.TC_POINTS,
                    USER_TC_STATS_HOURLY.TC_POINTS_MULTIPLIED, USER_TC_STATS_HOURLY.TC_UNITS)
                .values(userTcStats.getUserId(), DateTimeUtils.toUtcLocalDateTime(userTcStats.getTimestamp()), userTcStats.getPoints(),
                    userTcStats.getMultipliedPoints(), userTcStats.getUnits());
            LOGGER.debug("Executing SQL: '{}'", query);

            return query.execute();
        });
    }

    @Override
    public Optional<UserTcStats> getHourlyTcStats(final int userId) {
        LOGGER.debug("Getting current TC stats for user {}", userId);

        return executeQuery(queryContext -> {
            final var query = queryContext
                .select(USER_TC_STATS_HOURLY.USER_ID, USER_TC_STATS_HOURLY.UTC_TIMESTAMP, USER_TC_STATS_HOURLY.TC_POINTS,
                    USER_TC_STATS_HOURLY.TC_POINTS_MULTIPLIED, USER_TC_STATS_HOURLY.TC_UNITS)
                .from(USER_TC_STATS_HOURLY)
                .where(USER_TC_STATS_HOURLY.USER_ID.equal(userId))
                .orderBy(USER_TC_STATS_HOURLY.UTC_TIMESTAMP.desc())
                .limit(SINGLE_RESULT);
            LOGGER.debug("Executing SQL: '{}'", query);

            return query
                .fetch()
                .into(USER_TC_STATS_HOURLY)
                .stream()
                .map(RecordConverter::toUserTcStats)
                .findAny();
        });
    }

    @Override
    public boolean isAnyHourlyTcStats() {
        LOGGER.debug("Checking if any TC stats exist in the DB");

        return executeQuery(queryContext -> {
            final var query = queryContext
                .selectCount()
                .from(USER_TC_STATS_HOURLY);
            LOGGER.debug("Executing SQL: '{}'", query);

            final Integer count = query.fetchOne(0, Integer.class);
            return count != null && count > 0;
        });
    }

    @Override
    public Collection<HistoricStats> getHistoricStatsHourly(final int userId, final int day, final Month month, final Year year) {
        LOGGER.debug("Getting historic hourly user TC stats for {}/{}/{} for user {}", () -> year, () -> DateTimeUtils.formatMonth(month), () -> day,
            () -> userId);

        final String selectSqlStatement = "SELECT MAX(utc_timestamp) AS hourly_timestamp, " +
            "COALESCE(MAX(tc_points) - LAG(MAX(tc_points)) OVER (ORDER BY MIN(utc_timestamp)), 0) AS diff_points, " +
            "COALESCE(MAX(tc_points_multiplied) - LAG(MAX(tc_points_multiplied)) OVER (ORDER BY MIN(utc_timestamp)), 0) AS diff_points_multiplied, " +
            "COALESCE(MAX(tc_units) - LAG(MAX(tc_units)) OVER (ORDER BY MIN(utc_timestamp)), 0) AS diff_units " +
            "FROM user_tc_stats_hourly " +
            "WHERE utc_timestamp BETWEEN ? AND ? " +
            "AND user_id = ? " +
            "GROUP BY EXTRACT(HOUR FROM utc_timestamp) " +
            "ORDER BY EXTRACT(HOUR FROM utc_timestamp) ASC";

        try (final Connection connection = dbConnectionPool.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(selectSqlStatement)) {

            preparedStatement.setTimestamp(1, DateTimeUtils.getTimestampOf(year, month, day, 0, 0, 0));
            preparedStatement.setTimestamp(2, DateTimeUtils.getTimestampOf(year, month, day, 23, 59, 59));
            preparedStatement.setInt(3, userId);

            LOGGER.debug("Executing prepared statement: '{}'", preparedStatement);
            try (final ResultSet resultSet = preparedStatement.executeQuery()) {

                final List<HistoricStats> userStats = new ArrayList<>();

                // First entry will be zeroed, so we need to manually get the first hour's stats for the user
                if (resultSet.next()) {
                    final UserTcStats userTcStats = getTcStatsForFirstHourOfDay(userId, day, month, year);

                    userStats.add(
                        HistoricStats.create(
                            resultSet.getTimestamp("hourly_timestamp").toLocalDateTime(),
                            userTcStats.getPoints(),
                            userTcStats.getMultipliedPoints(),
                            userTcStats.getUnits()
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
            LOGGER.warn("Unable to get the stats for the first hour of {}/{}/{} for user {}", year, DateTimeUtils.formatMonth(month), day, userId);
            throw e;
        } catch (final SQLException e) {
            throw new DatabaseConnectionException("Error opening connection to the DB", e);
        }
    }

    private UserTcStats getCurrentDayFirstHourTcStats(final int userId, final int day, final Month month, final Year year) {
        LOGGER.debug("Getting current day's first hour TC stats for user {} on {}/{}/{}", () -> userId, year::getValue, month::getValue, () -> day);

        return executeQuery(queryContext -> {
            final LocalDateTime start = DateTimeUtils.getLocalDateTimeOf(year, month, day, 0, 0, 0);
            final LocalDateTime end = DateTimeUtils.getLocalDateTimeOf(year, month, day, 0, 59, 59);

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
            LOGGER.debug("Executing SQL: '{}'", query);

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
        LOGGER.debug("Getting previous day's last hour TC stats for user {} on {}/{}/{}", () -> userId, year::getValue, month::getValue, () -> day);

        return executeQuery(queryContext -> {
            final LocalDateTime start = DateTimeUtils.getLocalDateTimeOf(year, month, day, 23, 0, 0);
            final LocalDateTime end = DateTimeUtils.getLocalDateTimeOf(year, month, day, 23, 59, 59);


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
            LOGGER.debug("Executing SQL: '{}'", query);

            return query
                .fetch()
                .into(USER_TC_STATS_HOURLY)
                .stream()
                .map(RecordConverter::toUserTcStats)
                .findAny()
                .orElse(UserTcStats.empty(userId));
        });
    }


    private UserTcStats getTcStatsForFirstHourOfDay(final int userId, final int day, final Month month, final Year year) {
        final UserTcStats firstHourTcStatsCurrentDay = getCurrentDayFirstHourTcStats(userId, day, month, year);

        final boolean isFirstDay = day == 1;
        final int previousDay = day - 1;
        final UserTcStats lastHourTcStatsPreviousDay =
            isFirstDay ? UserTcStats.empty(userId) : getPreviousDayLastHourTcStats(userId, previousDay, month, year);

        if (lastHourTcStatsPreviousDay.isEmpty()) {

            if (firstHourTcStatsCurrentDay.isEmpty()) {
                return UserTcStats.empty(userId);
            }


            // If no stats in previous day (meaning we are getting historic stats for the first day available), we need to remove the initial points from the current day's points
            final UserStats initialStats = getInitialStats(userId).orElse(UserStats.empty());
            LOGGER.debug("Removing initial stats from current day's first hour stats: {} - {}", firstHourTcStatsCurrentDay, initialStats);

            // Since we didn't get any previous day's stats, we don't need to worry about the hardware multiplier having been changed
            // As a result, we will get the user's current hardware and use that multiplier
            final Optional<User> optionalUser = getUser(userId);

            if (optionalUser.isEmpty()) {
                LOGGER.warn("Could not find user with ID {}, returning empty stats for first hour of day", userId);
                return UserTcStats.empty(userId);
            }

            final User user = optionalUser.get();
            final Hardware hardware = user.getHardware();
            final double hardwareMultiplier = hardware.getMultiplier();

            return UserTcStats.create(
                firstHourTcStatsCurrentDay.getUserId(),
                firstHourTcStatsCurrentDay.getTimestamp(),
                Math.max(0, firstHourTcStatsCurrentDay.getPoints() - initialStats.getPoints()),
                Math.max(0, firstHourTcStatsCurrentDay.getMultipliedPoints() - Math.round(hardwareMultiplier * initialStats.getPoints())),
                Math.max(0, firstHourTcStatsCurrentDay.getUnits() - initialStats.getUnits())
            );
        }

        LOGGER.debug("Removing previous day's last hour stats from current day's first hour stats: {} - {}", firstHourTcStatsCurrentDay,
            lastHourTcStatsPreviousDay);
        return UserTcStats.create(
            firstHourTcStatsCurrentDay.getUserId(),
            firstHourTcStatsCurrentDay.getTimestamp(),
            Math.max(0, firstHourTcStatsCurrentDay.getPoints() - lastHourTcStatsPreviousDay.getPoints()),
            Math.max(0, firstHourTcStatsCurrentDay.getMultipliedPoints() - lastHourTcStatsPreviousDay.getMultipliedPoints()),
            Math.max(0, firstHourTcStatsCurrentDay.getUnits() - lastHourTcStatsPreviousDay.getUnits())
        );
    }


    @Override
    public Collection<HistoricStats> getHistoricStatsDaily(final int userId, final Month month, final Year year) {
        LOGGER.debug("Getting historic daily user TC stats for {}/{} for user {}", () -> DateTimeUtils.formatMonth(month), () -> year, () -> userId);

        final String selectSqlStatement = "SELECT utc_timestamp::DATE AS daily_timestamp, " +
            "COALESCE(MAX(tc_points) - LAG(MAX(tc_points)) OVER (ORDER BY MIN(utc_timestamp)), 0) AS diff_points, " +
            "COALESCE(MAX(tc_points_multiplied) - LAG(MAX(tc_points_multiplied)) OVER (ORDER BY MIN(utc_timestamp)), 0) AS diff_points_multiplied, " +
            "COALESCE(MAX(tc_units) - LAG(MAX(tc_units)) OVER (ORDER BY MIN(utc_timestamp)), 0) AS diff_units " +
            "FROM user_tc_stats_hourly " +
            "WHERE EXTRACT(MONTH FROM utc_timestamp) = ? " +
            "AND EXTRACT(YEAR FROM utc_timestamp) = ? " +
            "AND user_id = ? " +
            "GROUP BY utc_timestamp::DATE " +
            "ORDER BY utc_timestamp::DATE ASC;";

        try (final Connection connection = dbConnectionPool.getConnection();
             final PreparedStatement preparedStatement = connection.prepareStatement(selectSqlStatement)) {

            preparedStatement.setInt(1, month.getValue());
            preparedStatement.setInt(2, year.getValue());
            preparedStatement.setInt(3, userId);

            LOGGER.debug("Executing prepared statement: '{}'", preparedStatement);
            try (final ResultSet resultSet = preparedStatement.executeQuery()) {

                final List<HistoricStats> userStats = new ArrayList<>();

                // First entry will be zeroed, so we need to manually get the first day's stats for the user
                if (resultSet.next()) {
                    final LocalDateTime localDateTime = resultSet.getTimestamp("daily_timestamp").toLocalDateTime();
                    final UserTcStats userTcStats = getTcStatsForFirstDayOfMonth(localDateTime, userId);

                    if (userTcStats.isEmpty()) {
                        LOGGER.warn("Error getting historic stats for first day of {} for user with ID {}", DateTimeUtils.formatMonth(month), userId);
                    } else {
                        userStats.add(
                            HistoricStats.create(
                                localDateTime,
                                userTcStats.getPoints(),
                                userTcStats.getMultipliedPoints(),
                                userTcStats.getUnits()
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
        LOGGER.debug("Getting historic monthly user TC stats for {} for user {}", year, userId);

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
            LOGGER.debug("Executing SQL: '{}'", query);

            return query.fetch()
                .into(USER_TC_STATS_HOURLY)
                .stream()
                .map(RecordConverter::toHistoricStats)
                .collect(toList());
        });
    }

    private UserTcStats getTcStatsForFirstDayOfMonth(final LocalDateTime localDateTime, final int userId) {
        LOGGER.debug("Getting TC stats for user {} on {}", userId, localDateTime);

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
            LOGGER.debug("Executing SQL: '{}'", query);

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
    public void persistInitialStats(final UserStats userStats) {
        LOGGER.debug("Inserting initial stats for user {} to DB", userStats::getUserId);

        executeQuery(queryContext -> {
            final var query = queryContext
                .insertInto(USER_INITIAL_STATS)
                .columns(USER_INITIAL_STATS.USER_ID, USER_INITIAL_STATS.UTC_TIMESTAMP, USER_INITIAL_STATS.INITIAL_POINTS,
                    USER_INITIAL_STATS.INITIAL_UNITS)
                .values(userStats.getUserId(), DateTimeUtils.toUtcLocalDateTime(userStats.getTimestamp()), userStats.getPoints(),
                    userStats.getUnits());
            LOGGER.debug("Executing SQL: '{}'", query);

            return query.execute();
        });
    }

    @Override
    public Optional<UserStats> getInitialStats(final int userId) {
        LOGGER.debug("Getting initial stats for user ID: {}", userId);

        return executeQuery(queryContext -> {
            final var query = queryContext
                .select()
                .from(USER_INITIAL_STATS)
                .where(USER_INITIAL_STATS.USER_ID.equal(userId))
                .orderBy(USER_INITIAL_STATS.UTC_TIMESTAMP.desc())
                .limit(SINGLE_RESULT);
            LOGGER.debug("Executing SQL: '{}'", query);

            return query
                .fetch()
                .into(USER_INITIAL_STATS)
                .stream()
                .map(RecordConverter::toUserStats)
                .findAny();
        });
    }

    @Override
    public void persistTotalStats(final UserStats userStats) {
        LOGGER.debug("Inserting total stats for user ID {} to DB", userStats::getUserId);

        executeQuery(queryContext -> {
            final var query = queryContext
                .insertInto(USER_TOTAL_STATS)
                .columns(USER_TOTAL_STATS.USER_ID, USER_TOTAL_STATS.UTC_TIMESTAMP, USER_TOTAL_STATS.TOTAL_POINTS, USER_TOTAL_STATS.TOTAL_UNITS)
                .values(userStats.getUserId(), DateTimeUtils.toUtcLocalDateTime(userStats.getTimestamp()), userStats.getPoints(),
                    userStats.getUnits());
            LOGGER.debug("Executing SQL: '{}'", query);

            return query.execute();
        });
    }

    @Override
    public Optional<UserStats> getTotalStats(final int userId) {
        LOGGER.debug("Getting total stats for user ID: {}", userId);

        return executeQuery(queryContext -> {
            final var query = queryContext
                .select()
                .from(USER_TOTAL_STATS)
                .where(USER_TOTAL_STATS.USER_ID.equal(userId))
                .orderBy(USER_TOTAL_STATS.UTC_TIMESTAMP.desc())
                .limit(SINGLE_RESULT);
            LOGGER.debug("Executing SQL: '{}'", query);

            return query
                .fetch()
                .into(USER_TOTAL_STATS)
                .stream()
                .map(RecordConverter::toUserStats)
                .findAny();
        });
    }

    @Override
    public void addOffsetStats(final int userId, final OffsetStats offsetStats) {
        LOGGER.debug("Adding offset stats for user {}", userId);

        executeQuery(queryContext -> {
            final LocalDateTime currentUtcLocalDateTime = DateTimeUtils.toUtcLocalDateTime(DateTimeUtils.currentUtcTimestamp());

            final var query = queryContext
                .insertInto(USER_OFFSET_TC_STATS)
                .columns(USER_OFFSET_TC_STATS.USER_ID, USER_OFFSET_TC_STATS.UTC_TIMESTAMP, USER_OFFSET_TC_STATS.OFFSET_POINTS,
                    USER_OFFSET_TC_STATS.OFFSET_MULTIPLIED_POINTS, USER_OFFSET_TC_STATS.OFFSET_UNITS)
                .values(userId, currentUtcLocalDateTime, offsetStats.getPointsOffset(), offsetStats.getMultipliedPointsOffset(),
                    offsetStats.getUnitsOffset())
                .onConflict(USER_OFFSET_TC_STATS.USER_ID)
                .doUpdate()
                .set(USER_OFFSET_TC_STATS.UTC_TIMESTAMP, currentUtcLocalDateTime)
                .set(USER_OFFSET_TC_STATS.OFFSET_POINTS, offsetStats.getPointsOffset())
                .set(USER_OFFSET_TC_STATS.OFFSET_MULTIPLIED_POINTS, offsetStats.getMultipliedPointsOffset())
                .set(USER_OFFSET_TC_STATS.OFFSET_UNITS, offsetStats.getUnitsOffset());
            LOGGER.debug("Executing SQL: '{}'", query);

            return query.execute();
        });
    }

    @Override
    public Optional<OffsetStats> addOrUpdateOffsetStats(final int userId, final OffsetStats offsetStats) {
        LOGGER.debug("Adding/updating offset stats for user {}", userId);

        return executeQuery(queryContext -> {
            final LocalDateTime currentUtcLocalDateTime = DateTimeUtils.toUtcLocalDateTime(DateTimeUtils.currentUtcTimestamp());

            final var query = queryContext
                .insertInto(USER_OFFSET_TC_STATS)
                .columns(USER_OFFSET_TC_STATS.USER_ID, USER_OFFSET_TC_STATS.UTC_TIMESTAMP, USER_OFFSET_TC_STATS.OFFSET_POINTS,
                    USER_OFFSET_TC_STATS.OFFSET_MULTIPLIED_POINTS, USER_OFFSET_TC_STATS.OFFSET_UNITS)
                .values(userId, currentUtcLocalDateTime, offsetStats.getPointsOffset(), offsetStats.getMultipliedPointsOffset(),
                    offsetStats.getUnitsOffset())
                .onConflict(USER_OFFSET_TC_STATS.USER_ID)
                .doUpdate()
                .set(USER_OFFSET_TC_STATS.UTC_TIMESTAMP, currentUtcLocalDateTime)
                .set(USER_OFFSET_TC_STATS.OFFSET_POINTS, USER_OFFSET_TC_STATS.OFFSET_POINTS.plus(offsetStats.getPointsOffset()))
                .set(USER_OFFSET_TC_STATS.OFFSET_MULTIPLIED_POINTS,
                    USER_OFFSET_TC_STATS.OFFSET_MULTIPLIED_POINTS.plus(offsetStats.getMultipliedPointsOffset()))
                .set(USER_OFFSET_TC_STATS.OFFSET_UNITS, USER_OFFSET_TC_STATS.OFFSET_UNITS.plus(offsetStats.getUnitsOffset()))
                .returning();
            LOGGER.debug("Executing SQL: '{}'", query);

            return query
                .fetch()
                .into(USER_OFFSET_TC_STATS)
                .stream()
                .map(RecordConverter::toOffsetStats)
                .findAny();
        });
    }

    @Override
    public Optional<OffsetStats> getOffsetStats(final int userId) {
        LOGGER.debug("Getting offset stats for user ID: {}", userId);

        return executeQuery(queryContext -> {
            final var query = queryContext
                .select(USER_OFFSET_TC_STATS.OFFSET_POINTS, USER_OFFSET_TC_STATS.OFFSET_MULTIPLIED_POINTS, USER_OFFSET_TC_STATS.OFFSET_UNITS)
                .from(USER_OFFSET_TC_STATS)
                .where(USER_OFFSET_TC_STATS.USER_ID.equal(userId))
                .orderBy(USER_OFFSET_TC_STATS.UTC_TIMESTAMP.desc())
                .limit(SINGLE_RESULT);
            LOGGER.debug("Executing SQL: '{}'", query);

            return query
                .fetch()
                .into(USER_OFFSET_TC_STATS)
                .stream()
                .map(RecordConverter::toOffsetStats)
                .findAny();
        });
    }

    @Override
    public void clearAllOffsetStats() {
        LOGGER.debug("Clearing offset stats for all users");

        executeQuery(queryContext -> {
            final var query = queryContext
                .deleteFrom(USER_OFFSET_TC_STATS);
            LOGGER.debug("Executing SQL: '{}'", query);

            return query.execute();
        });
    }

    @Override
    public int persistRetiredUserStats(final int teamId, final int userId, final String displayUserName, final UserTcStats retiredUserStats) {
        LOGGER.debug("Persisting retired user ID {} for team ID {}", userId, teamId);

        return executeQuery(queryContext -> {
            final LocalDateTime currentUtcLocalDateTime = DateTimeUtils.toUtcLocalDateTime(DateTimeUtils.currentUtcTimestamp());

            final var query = queryContext
                .insertInto(RETIRED_USER_STATS)
                .columns(RETIRED_USER_STATS.TEAM_ID, RETIRED_USER_STATS.USER_ID, RETIRED_USER_STATS.DISPLAY_USERNAME,
                    USER_OFFSET_TC_STATS.UTC_TIMESTAMP,
                    RETIRED_USER_STATS.FINAL_POINTS, RETIRED_USER_STATS.FINAL_MULTIPLIED_POINTS, RETIRED_USER_STATS.FINAL_UNITS)
                .values(teamId, userId, displayUserName, currentUtcLocalDateTime, retiredUserStats.getPoints(),
                    retiredUserStats.getMultipliedPoints(), retiredUserStats.getUnits())
                .onConflict(RETIRED_USER_STATS.USER_ID)
                .doUpdate()
                .set(RETIRED_USER_STATS.TEAM_ID, teamId)
                .set(RETIRED_USER_STATS.UTC_TIMESTAMP, currentUtcLocalDateTime)
                .set(RETIRED_USER_STATS.DISPLAY_USERNAME, displayUserName)
                .set(RETIRED_USER_STATS.FINAL_POINTS, retiredUserStats.getPoints())
                .set(RETIRED_USER_STATS.FINAL_MULTIPLIED_POINTS, retiredUserStats.getMultipliedPoints())
                .set(RETIRED_USER_STATS.FINAL_UNITS, retiredUserStats.getUnits())
                .returning();
            LOGGER.debug("Executing SQL: '{}'", query);

            return query
                .fetch()
                .into(RETIRED_USER_STATS)
                .get(0)
                .getRetiredUserId();
        });
    }

    @Override
    public Collection<RetiredUserTcStats> getAllRetiredUserStats() {
        LOGGER.debug("Getting all retired user stats");

        return executeQuery(queryContext -> {
            final var query = queryContext
                .select()
                .from(RETIRED_USER_STATS)
                .orderBy(RETIRED_USER_STATS.RETIRED_USER_ID.asc());
            LOGGER.debug("Executing SQL: '{}'", query);

            return query
                .fetch()
                .into(RETIRED_USER_STATS)
                .stream()
                .map(RecordConverter::toRetiredUserStats)
                .collect(toList());
        });
    }

    @Override
    public void deleteAllRetiredUserStats() {
        LOGGER.debug("Deleting all retired users");

        executeQuery(queryContext -> {
            final var query = queryContext
                .deleteFrom(RETIRED_USER_STATS);
            LOGGER.debug("Executing SQL: '{}'", query);

            return query.execute();
        });
    }

    @Override
    public void persistMonthlyResult(final String result, final LocalDateTime utcTimestamp) {
        LOGGER.debug("Persisting monthly result for {}/{}", utcTimestamp::getYear, () -> DateTimeUtils.formatMonth(utcTimestamp.getMonth()));

        executeQuery(queryContext -> {
            final var query = queryContext
                .insertInto(MONTHLY_RESULTS)
                .columns(MONTHLY_RESULTS.UTC_TIMESTAMP, MONTHLY_RESULTS.JSON_RESULT)
                .values(utcTimestamp, result);

            LOGGER.debug("Executing SQL: '{}'", query);

            return query.execute();
        });
    }

    @Override
    public Optional<String> getMonthlyResult(final Month month, final Year year) {
        LOGGER.debug("Retrieving monthly result for {}/{}", () -> year, () -> DateTimeUtils.formatMonth(month));

        return executeQuery(queryContext -> {
            final var query = queryContext
                .select(MONTHLY_RESULTS.JSON_RESULT)
                .from(MONTHLY_RESULTS)
                .where(year(MONTHLY_RESULTS.UTC_TIMESTAMP).equal(year.getValue()))
                .and(month(MONTHLY_RESULTS.UTC_TIMESTAMP).equal(month.getValue()))
                .orderBy(MONTHLY_RESULTS.UTC_TIMESTAMP.desc())
                .limit(SINGLE_RESULT);

            LOGGER.debug("Executing SQL: '{}'", query);

            return query
                .fetch()
                .into(MONTHLY_RESULTS)
                .stream()
                .map(RecordConverter::toMonthlyResults)
                .findAny();
        });
    }

    @Override
    public UserAuthenticationResult authenticateSystemUser(final String userName, final String password) {
        LOGGER.debug("Checking if supplied username '{}' and password is valid user, then returning roles", userName);

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
            LOGGER.debug("Executing SQL: '{}'", query);

            return query
                .fetch()
                .stream()
                .map(RecordConverter::toSystemUserAuthentication)
                .findAny()
                .orElse(UserAuthenticationResult.userDoesNotExist());
        });
    }

    private <T> T executeQuery(final Function<DSLContext, T> sqlQuery) {
        try (final Connection connection = dbConnectionPool.getConnection()) {
            final DSLContext queryContext = DSL.using(connection, SQLDialect.POSTGRES);
            return sqlQuery.apply(queryContext);
        } catch (final SQLException e) {
            throw new DatabaseConnectionException("Error closing connection", e);
        }
    }
}

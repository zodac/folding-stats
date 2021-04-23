package me.zodac.folding.bean;

import me.zodac.folding.StorageFacade;
import me.zodac.folding.api.exception.FoldingException;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.tc.stats.Stats;
import me.zodac.folding.api.tc.stats.UserStats;
import me.zodac.folding.api.tc.stats.UserTcStats;
import me.zodac.folding.api.utils.EnvironmentVariables;
import me.zodac.folding.cache.StatsCache;
import me.zodac.folding.db.DbManagerRetriever;
import me.zodac.folding.parsing.FoldingStatsParser;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.ScheduleExpression;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerService;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static me.zodac.folding.api.utils.NumberUtils.formatWithCommas;

// TODO: [zodac] Move this to an EJB module?
@Startup
@Singleton
public class TeamCompetitionStatsParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(TeamCompetitionStatsParser.class);

    // Default is to run every hour at 15 minutes past the hour
    private static final String STATS_PARSING_SCHEDULE_HOUR = EnvironmentVariables.get("STATS_PARSING_SCHEDULE_HOUR", "*");
    private static final String STATS_PARSING_SCHEDULE_MINUTE = EnvironmentVariables.get("STATS_PARSING_SCHEDULE_MINUTE", "15");
    private static final String STATS_PARSING_SCHEDULE_SECOND = EnvironmentVariables.get("STATS_PARSING_SCHEDULE_SECOND", "0");

    @EJB
    private StorageFacade storageFacade;

    @Resource
    private TimerService timerService;

    @PostConstruct
    public void init() {
        final ScheduleExpression schedule = new ScheduleExpression();
        schedule.hour(STATS_PARSING_SCHEDULE_HOUR);
        schedule.minute(STATS_PARSING_SCHEDULE_MINUTE);
        schedule.second(STATS_PARSING_SCHEDULE_SECOND);
        schedule.timezone("UTC");
        final Timer timer = timerService.createCalendarTimer(schedule);
        LOGGER.info("Starting TC stats parser with schedule: {}", timer.getSchedule());
    }

    @Timeout
    public void scheduledStatsParsing(final Timer timer) {
        LOGGER.trace("Timer fired at: {}", timer);
        getTcStatsForFoldingUsers();
    }

    public void manualStatsParsing() {
        LOGGER.debug("Manual stats parsing execution");
        getTcStatsForFoldingUsers();
    }

    private void getTcStatsForFoldingUsers() {
        LOGGER.info("Parsing TC Folding stats");

        try {
            final List<Team> tcTeams = storageFacade.getAllTeams();
            if (tcTeams.isEmpty()) {
                LOGGER.warn("No TC teams configured in system!");
                return;
            }

            final List<User> tcUsers = storageFacade.getUsersFromTeams(tcTeams);

            if (tcUsers.isEmpty()) {
                LOGGER.warn("No TC users configured in system!");
                return;
            }

            try {
                getStatsForUsers(tcUsers);
                LOGGER.info("Finished parsing");
                LOGGER.info("");
            } catch (final FoldingException e) {
                LOGGER.error("Error parsing TC stats", e.getCause());
            } catch (final Exception e) {
                LOGGER.error("Unexpected error parsing TC stats", e);
            }
        } catch (final FoldingException e) {
            LOGGER.warn("Unable to get teams!");
        }
    }

    private void getStatsForUsers(final List<User> users) throws FoldingException {
        final Map<Integer, User> userById = users.stream().collect(toMap(User::getId, user -> user));
        final Map<Integer, Hardware> hardwareById = storageFacade.getAllHardware().stream().collect(toMap(Hardware::getId, hardware -> hardware));
        final List<UserStats> stats = getTotalStatsForUsers(users);

        try {
            DbManagerRetriever.get().persistTotalUserStats(stats);
        } catch (final FoldingException e) {
            LOGGER.error("Error persisting total user stats", e.getCause());
        }


        final Map<Integer, Stats> initialStatsByUserId = storageFacade.getInitialUserStats(users.stream().map(User::getId).collect(toList()));


        // TODO: [zodac] Streams?
        final Map<Integer, Stats> offsetStatsByUserId = new HashMap<>(stats.size());

        // TODO: [zodac] We're reusing the users here from when the stats were triggered. There is a potential that an update was made to a user between the stats parsing
        //   started, and now. I'm ignoring it for now (laziness), but it could cause an inconsistency down the line. Perhaps block PUT/PATCH requests while stats are running?
        for (final User user : users) {
            // We may not have collected the stats for all users, so only get offsets for users whose points we are updating
            if (initialStatsByUserId.containsKey(user.getId())) {
                final Stats initialStats = initialStatsByUserId.get(user.getId());
                final long totalOffsetUnmultipliedPoints = user.getPointsOffset() + initialStats.getPoints();
                final int totalOffsetUnits = user.getUnitsOffset() + initialStats.getUnits();
                offsetStatsByUserId.put(user.getId(), Stats.create(totalOffsetUnmultipliedPoints, totalOffsetUnits));
            }
        }

        final List<UserTcStats> hourlyTcStatsForUsers = new ArrayList<>(stats.size());

        for (final UserStats totalStatsForUser : stats) {
            final int userId = totalStatsForUser.getUserId();
            LOGGER.debug("Calculating TC stats for {}", userId);
            if (!userById.containsKey(userId)) {
                LOGGER.warn("Unable to find user for ID: {}", userId);
                continue;
            }

            final User user = userById.get(userId);
            final int hardwareId = user.getHardwareId();
            if (!hardwareById.containsKey(hardwareId)) {
                LOGGER.warn("Unable to find hardware for user: {}", user);
                continue;
            }

            final Timestamp timestamp = totalStatsForUser.getTimestamp();
            final Stats offset = offsetStatsByUserId.get(userId);

            final long unmultipliedPoints = Math.max(0, totalStatsForUser.getPoints() - offset.getPoints());
            final int units = Math.max(0, totalStatsForUser.getUnits() - offset.getUnits());
            final Stats statsForUser = Stats.create(unmultipliedPoints, units);
            final UserTcStats tcStatsForUser = UserTcStats.createWithMultiplier(userId, timestamp, statsForUser, hardwareById.get(hardwareId).getMultiplier());

            LOGGER.info("{}: {} TC points | {} TC units", user.getFoldingUserName(), formatWithCommas(tcStatsForUser.getMultipliedPoints()), formatWithCommas(tcStatsForUser.getUnits()));
            hourlyTcStatsForUsers.add(tcStatsForUser);
        }

        try {
            DbManagerRetriever.get().persistHourlyTcUserStats(hourlyTcStatsForUsers);
        } catch (final FoldingException e) {
            LOGGER.error("Error persisting hourly TC stats", e.getCause());
        }
    }

    private List<UserStats> getTotalStatsForUsers(final List<User> users) {
        final List<UserStats> stats = new ArrayList<>(users.size());

        for (final User user : users) {
            if (StringUtils.isBlank(user.getPasskey())) {
                LOGGER.warn("Not parsing TC stats for user, missing passkey: {}", user);
                continue;
            }

            try {
                final UserStats userStats = FoldingStatsParser.getStatsForUser(user);
                stats.add(userStats);
                LOGGER.debug("{}: {} total points (unmultiplied) | {} total units", user.getFoldingUserName(), formatWithCommas(userStats.getPoints()), formatWithCommas(userStats.getUnits()));
                // TODO: [zodac] Go through StorageFacade for this
                StatsCache.get().addCurrentStats(user.getId(), userStats.getStats());
            } catch (final FoldingException e) {
                LOGGER.warn("Unable to get stats for user '{}/{}/{}'", user.getFoldingUserName(), user.getPasskey(), user.getFoldingTeamNumber(), e.getCause());
            }
        }
        return stats;
    }
}
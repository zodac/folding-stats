package me.zodac.folding.bean;

import me.zodac.folding.StorageFacade;
import me.zodac.folding.api.exception.FoldingException;
import me.zodac.folding.api.exception.UserNotFoundException;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.tc.stats.Stats;
import me.zodac.folding.api.tc.stats.UserStats;
import me.zodac.folding.api.tc.stats.UserStatsOffset;
import me.zodac.folding.api.tc.stats.UserTcStats;
import me.zodac.folding.api.utils.EnvironmentVariables;
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
import java.util.Collection;
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

    public void manualTcStatsParsing() {
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

            final Map<Integer, User> tcUserById = storageFacade.getActiveTcUsers(tcTeams);

            if (tcUserById.isEmpty()) {
                LOGGER.warn("No TC users configured in system!");
                return;
            }

            try {
                // TODO: [zodac] Scaling up, this should probably be done in separate threads (and async)
                updateTcStatsForUsers(tcUserById.values());
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

    public void updateTcStatsForUser(final int userId) throws FoldingException, UserNotFoundException {
        final User user = storageFacade.getUser(userId);
        updateTcStatsForUsers(List.of(user));
    }

    public void updateTcStatsForTeam(final Team team) throws FoldingException, UserNotFoundException {
        final List<User> users = new ArrayList<>();
        for (final int userId : team.getUserIds()) {
            users.add(storageFacade.getUser(userId));
        }
        updateTcStatsForUsers(users);
    }

    private void updateTcStatsForUsers(final Collection<User> users) throws FoldingException {
        final Map<Integer, User> userById = users.stream().collect(toMap(User::getId, user -> user));
        final Map<Integer, Double> hardwareMultiplierById = storageFacade.getAllHardware().stream().collect(toMap(Hardware::getId, Hardware::getMultiplier));
        final List<UserStats> stats = getTotalStatsForUsers(users);

        try {
            storageFacade.persistTotalUserStats(stats);
        } catch (final FoldingException e) {
            LOGGER.error("Error persisting total user stats", e.getCause());
            return;
        }

        final List<Integer> userIds = users.stream().map(User::getId).collect(toList());
        final Map<Integer, Stats> initialStatsByUserId = storageFacade.getInitialStatsForUsers(userIds);
        final Map<Integer, UserStatsOffset> statsOffsetsByUserId = storageFacade.getOffsetStatsForUsers(userIds);

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
            if (!hardwareMultiplierById.containsKey(hardwareId)) {
                LOGGER.warn("Unable to find hardware for user: {}", user);
                continue;
            }

            final Timestamp timestamp = totalStatsForUser.getTimestamp();
            final Stats offset = initialStatsByUserId.get(userId);

            final long unmultipliedPoints = Math.max(0, totalStatsForUser.getPoints() - offset.getPoints());
            final int units = Math.max(0, totalStatsForUser.getUnits() - offset.getUnits());
            final Stats statsForUser = Stats.create(unmultipliedPoints, units);

            final double hardwareMultiplier = hardwareMultiplierById.get(hardwareId);
            final UserTcStats tcStatsForUser = UserTcStats.createWithMultiplier(userId, timestamp, statsForUser, hardwareMultiplier);
            LOGGER.debug("{}: {} TC points (pre-offset) | {} TC units (pre-offset)", user.getFoldingUserName(), formatWithCommas(tcStatsForUser.getMultipliedPoints()), formatWithCommas(tcStatsForUser.getUnits()));

            final UserTcStats tcStatsForUserWithOffset = UserTcStats.updateWithOffsets(tcStatsForUser, statsOffsetsByUserId.get(userId), hardwareMultiplier);
            LOGGER.info("{}: {} TC points | {} TC units", user.getFoldingUserName(), formatWithCommas(tcStatsForUserWithOffset.getMultipliedPoints()), formatWithCommas(tcStatsForUserWithOffset.getUnits()));
            hourlyTcStatsForUsers.add(tcStatsForUserWithOffset);
        }

        try {
            storageFacade.persistHourlyTcUserStats(hourlyTcStatsForUsers);
        } catch (final FoldingException e) {
            LOGGER.error("Error persisting hourly TC stats", e.getCause());
        }
    }

    private List<UserStats> getTotalStatsForUsers(final Collection<User> users) {
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
            } catch (final FoldingException e) {
                LOGGER.warn("Unable to get stats for user '{}/{}'", user.getFoldingUserName(), user.getPasskey(), e.getCause());
            }
        }
        return stats;
    }
}
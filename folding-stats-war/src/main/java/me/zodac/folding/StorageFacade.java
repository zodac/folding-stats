package me.zodac.folding;


import me.zodac.folding.api.db.DbManager;
import me.zodac.folding.api.exception.FoldingConflictException;
import me.zodac.folding.api.exception.FoldingException;
import me.zodac.folding.api.exception.HardwareNotFoundException;
import me.zodac.folding.api.exception.NotFoundException;
import me.zodac.folding.api.exception.TeamNotFoundException;
import me.zodac.folding.api.exception.UserNotFoundException;
import me.zodac.folding.api.tc.Hardware;
import me.zodac.folding.api.tc.Team;
import me.zodac.folding.api.tc.User;
import me.zodac.folding.api.tc.stats.Stats;
import me.zodac.folding.api.tc.stats.UserStats;
import me.zodac.folding.api.tc.stats.UserTcStats;
import me.zodac.folding.cache.HardwareCache;
import me.zodac.folding.cache.StatsCache;
import me.zodac.folding.cache.TeamCache;
import me.zodac.folding.cache.UserCache;
import me.zodac.folding.db.DbManagerRetriever;
import me.zodac.folding.parsing.FoldingStatsParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Singleton;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

/**
 * In order to decouple the REST layer from the storage/persistence, we use this {@link StorageFacade} instead.
 * <p>
 * This way the {@link StorageFacade} is aware of caches or any other internal implementation, while the REST layer
 * does not need to know about them or any DBs being used.
 */
// TODO: [zodac] Should replace the cache miss warnings with some metrics instead?
@Singleton
public class StorageFacade {

    private static final Logger LOGGER = LoggerFactory.getLogger(StorageFacade.class);

    private final DbManager dbManager = DbManagerRetriever.get();
    private final TeamCache teamCache = TeamCache.get();
    private final UserCache userCache = UserCache.get();
    private final HardwareCache hardwareCache = HardwareCache.get();
    private final StatsCache statsCache = StatsCache.get();

    public Hardware createHardware(final Hardware hardware) throws FoldingException, FoldingConflictException {
        final Hardware hardwareWithId = dbManager.createHardware(hardware);
        hardwareCache.add(hardwareWithId);
        return hardwareWithId;
    }

    public Hardware getHardware(final int hardwareId) throws FoldingException, HardwareNotFoundException {
        try {
            return hardwareCache.get(hardwareId);
        } catch (final NotFoundException e) {
            LOGGER.debug("Unable to find hardware with ID {} in cache", hardwareId, e);
        }

        LOGGER.debug("Cache miss! Get hardware");
        // Should be no need to get anything from the DB (since it should have been added to the cache when created)
        // But adding this just in case we decide to add some cache eviction in future
        final Hardware hardwareFromDb = dbManager.getHardware(hardwareId);
        hardwareCache.add(hardwareFromDb);

        return hardwareFromDb;
    }

    public List<Hardware> getAllHardware() throws FoldingException {
        final List<Hardware> allHardware = hardwareCache.getAll();

        if (!allHardware.isEmpty()) {
            return allHardware;
        }

        LOGGER.debug("Cache miss! Get all hardware");
        // Should be no need to get anything from the DB (since it should have been added to the cache when created)
        // But adding this just in case we decide to add some cache eviction in future
        final List<Hardware> allHardwareFromDb = dbManager.getAllHardware();
        hardwareCache.addAll(allHardwareFromDb);
        return allHardwareFromDb;
    }

    public void updateHardware(final Hardware hardware) throws FoldingException, HardwareNotFoundException, FoldingConflictException {
        dbManager.updateHardware(hardware);
        hardwareCache.add(hardware);
    }

    public void deleteHardware(final int hardwareId) throws FoldingException, FoldingConflictException {
        hardwareCache.remove(hardwareId);
        dbManager.deleteHardware(hardwareId);
    }

    public User createUser(final User user) throws FoldingException, FoldingConflictException {
        final User userWithId = dbManager.createUser(user);
        userCache.add(userWithId);

        // When adding a new user, we should also configure the initial stats cache
        persistInitialUserStats(userWithId);

        return userWithId;
    }

    public void persistInitialUserStats(final User user) throws FoldingException {
        final UserStats currentUserStats = FoldingStatsParser.getStatsForUser(user);
        dbManager.persistInitialUserStats(currentUserStats);
        statsCache.addInitialStats(user.getId(), currentUserStats.getStats());
    }

    public Map<Integer, Stats> getInitialUserStats(final List<Integer> userIds) throws FoldingException {
        final Map<Integer, Stats> cachedInitialStats = new HashMap<>(userIds.size());
        for (final int userId : userIds) {
            final Optional<Stats> optionalStats = statsCache.getInitialStatsForUser(userId);
            optionalStats.ifPresent(stats -> cachedInitialStats.put(userId, stats));
        }

        if (cachedInitialStats.size() == userIds.size()) {
            return cachedInitialStats;
        }

        LOGGER.debug("Found {} cached initial stats for {} user IDs, checking DB instead", cachedInitialStats.size(), userIds.size());
        return dbManager.getInitialUserStats(userIds);
    }


    public User getUser(final int userId) throws FoldingException, UserNotFoundException {
        try {
            return userCache.get(userId);
        } catch (final NotFoundException e) {
            LOGGER.debug("Unable to find user with ID {} in cache", userId, e);
        }

        LOGGER.debug("Cache miss! Get user");
        // Should be no need to get anything from the DB (since it should have been added to the cache when created)
        // But adding this just in case we decide to add some cache eviction in future
        final User userFromDb = dbManager.getUser(userId);
        userCache.add(userFromDb);

        return userFromDb;
    }

    public List<User> getAllUsers() throws FoldingException {
        final List<User> allUsers = userCache.getAll();

        if (!allUsers.isEmpty()) {
            return allUsers;
        }

        LOGGER.debug("Cache miss! Get all users");
        // Should be no need to get anything from the DB (since it should have been added to the cache when created)
        // But adding this just in case we decide to add some cache eviction in future
        final List<User> allUsersFromDb = dbManager.getAllUsers();
        userCache.addAll(allUsersFromDb);
        return allUsersFromDb;
    }

    public void updateUser(final User updatedUser) throws FoldingException, UserNotFoundException, FoldingConflictException {
        final User existingUser = getUser(updatedUser.getId());
        dbManager.updateUser(updatedUser);
        userCache.add(updatedUser);

        // If a user is updated and their team, Folding username, hardware ID or passkey is changed, we need to update their initial offset again
        // The value should be: (new user info points - current TC points)
        // If the user had an offset, it should also be re-applied here, to negate the impact it has on current TC points
        // TODO: [zodac] When updating a user, KEEP the offsets. The offsets should be reset at the end of the month anyway, but should be incremented on update, not removed
        if (!existingUser.getPasskey().equalsIgnoreCase(updatedUser.getPasskey()) || !existingUser.getFoldingUserName().equalsIgnoreCase(updatedUser.getFoldingUserName())
                || existingUser.getFoldingTeamNumber() != updatedUser.getFoldingTeamNumber() || existingUser.getHardwareId() != updatedUser.getHardwareId()) {
            LOGGER.debug("User had state changes, recalculating initial stats");
            final UserStats updatedUserStats = FoldingStatsParser.getStatsForUser(updatedUser);
            final UserTcStats currentUserTcStats = getTcStatsForUser(updatedUser.getId());
            final UserStats newUserInitialStats = UserStats.create(updatedUser.getId(), updatedUserStats.getTimestamp(),
                    Stats.create(updatedUserStats.getPoints() - currentUserTcStats.getMultipliedPoints() - existingUser.getPointsOffset(), updatedUserStats.getUnits() - currentUserTcStats.getUnits() - existingUser.getUnitsOffset())
            );

            dbManager.persistInitialUserStats(newUserInitialStats);
            statsCache.addInitialStats(updatedUser.getId(), newUserInitialStats.getStats());
        }
    }

    public void deleteUser(final int userId) throws FoldingException, FoldingConflictException {
        hardwareCache.remove(userId);
        dbManager.deleteUser(userId);
    }

    public Team createTeam(final Team team) throws FoldingException, FoldingConflictException {
        final Team teamWithId = dbManager.createTeam(team);
        teamCache.add(teamWithId);
        return teamWithId;
    }

    public Team getTeam(final int teamId) throws FoldingException, TeamNotFoundException {
        try {
            return teamCache.get(teamId);
        } catch (final NotFoundException e) {
            LOGGER.debug("Unable to find team with ID {} in cache", teamId, e);
        }

        LOGGER.debug("Cache miss! Get team");
        // Should be no need to get anything from the DB (since it should have been added to the cache when created)
        // But adding this just in case we decide to add some cache eviction in future
        final Team teamFromDb = dbManager.getTeam(teamId);
        teamCache.add(teamFromDb);

        return teamFromDb;
    }

    public List<Team> getAllTeams() throws FoldingException {
        final List<Team> allTeams = teamCache.getAll();

        if (!allTeams.isEmpty()) {
            return allTeams;
        }

        LOGGER.debug("Cache miss! Get all teams");
        // Should be no need to get anything from the DB (since it should have been added to the cache when created)
        // But adding this just in case we decide to add some cache eviction in future
        final List<Team> allTeamsFromDb = dbManager.getAllTeams();
        teamCache.addAll(allTeamsFromDb);
        return allTeamsFromDb;
    }

    public void updateTeam(final Team team) throws FoldingException, TeamNotFoundException, FoldingConflictException {
        dbManager.updateTeam(team);
        teamCache.add(team);
    }

    public void deleteTeam(final int teamId) throws FoldingException, FoldingConflictException {
        hardwareCache.remove(teamId);
        dbManager.deleteTeam(teamId);
    }

    public List<User> getUsersFromTeams(final List<Team> teams) {
        return teams
                .stream()
                .map(Team::getUserIds)
                .flatMap(Collection::stream)
                .map(userId -> UserCache.get().getOrNull(userId))
                .filter(Objects::nonNull)
                .collect(toList());
    }

    public UserTcStats getTcStatsForUser(final int userId) throws UserNotFoundException, FoldingException {
        return dbManager.getCurrentTcStats(userId);
    }

    public Map<LocalDate, UserTcStats> getDailyUserTcStats(final int userId, final Month month, final Year year) throws FoldingException, UserNotFoundException {
        return dbManager.getDailyUserTcStats(userId, month, year);
    }
}

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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.stream.Collectors.toList;

/**
 * In order to decouple the REST layer from the storage/persistence, we use this {@link StorageFacade} instead.
 * <p>
 * This way the {@link StorageFacade} is aware of caches or any other internal implementation, while the REST layer
 * does not need to know about them or any DBs being used.
 */
@Singleton
public class StorageFacade {

    private static final Logger LOGGER = LoggerFactory.getLogger(StorageFacade.class);

    private final DbManager dbManager = DbManagerRetriever.get();
    private final TeamCache teamCache = TeamCache.get();
    private final UserCache userCache = UserCache.get();
    private final HardwareCache hardwareCache = HardwareCache.get();
    private final StatsCache statsCache = StatsCache.get();

    public Hardware createHardware(final Hardware hardware) throws FoldingException {
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

        // Should be no need to get anything from the DB (since it should have been added to the cache when created)
        // But adding this just in case we decide to add some cache eviction in future
        final List<Hardware> allHardwareFromDb = dbManager.getAllHardware();
        hardwareCache.addAll(allHardwareFromDb);
        return allHardwareFromDb;
    }

    public void updateHardware(final Hardware hardware) throws FoldingException, HardwareNotFoundException {
        dbManager.updateHardware(hardware);
        hardwareCache.add(hardware);
    }

    public void deleteHardware(final int hardwareId) throws FoldingException, FoldingConflictException {
        hardwareCache.remove(hardwareId);
        dbManager.deleteHardware(hardwareId);
    }

    public User createUser(final User user) throws FoldingException, NotFoundException {
        final User userWithId = dbManager.createUser(user);
        userCache.add(userWithId);

        // When adding a new user, we should also configure the TC stats cache
        final Hardware hardware = hardwareCache.get(user.getHardwareId());
        final Stats currentStats = FoldingStatsParser.getStatsForUser(user.getFoldingUserName(), user.getPasskey(), user.getFoldingTeamNumber(), hardware.getMultiplier());
        statsCache.addInitialStats(userWithId.getId(), currentStats);

        return userWithId;
    }

    public User getUser(final int userId) throws FoldingException, UserNotFoundException {
        try {
            return userCache.get(userId);
        } catch (final NotFoundException e) {
            LOGGER.debug("Unable to find user with ID {} in cache", userId, e);
        }

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

        // Should be no need to get anything from the DB (since it should have been added to the cache when created)
        // But adding this just in case we decide to add some cache eviction in future
        final List<User> allUsersFromDb = dbManager.getAllUsers();
        userCache.addAll(allUsersFromDb);
        return allUsersFromDb;
    }

    public void updateUser(final User user) throws FoldingException, UserNotFoundException {
        dbManager.updateUser(user);
        userCache.add(user);
    }

    public void deleteUser(final int userId) throws FoldingException, FoldingConflictException {
        hardwareCache.remove(userId);
        dbManager.deleteUser(userId);
    }

    public Team createTeam(final Team team) throws FoldingException {
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

        // Should be no need to get anything from the DB (since it should have been added to the cache when created)
        // But adding this just in case we decide to add some cache eviction in future
        final List<Team> allTeamsFromDb = dbManager.getAllTeams();
        teamCache.addAll(allTeamsFromDb);
        return allTeamsFromDb;
    }

    public void updateTeam(final Team team) throws FoldingException, TeamNotFoundException {
        dbManager.updateTeam(team);
        teamCache.add(team);
    }

    public void deleteTeam(final int teamId) throws FoldingException, FoldingConflictException {
        hardwareCache.remove(teamId);
        dbManager.deleteTeam(teamId);
    }

    public List<User> getUsersFromTeams() {
        try {
            return getAllTeams()
                    .stream()
                    .map(Team::getUserIds)
                    .flatMap(Collection::stream)
                    .map(userId -> UserCache.get().getOrNull(userId))
                    .filter(Objects::nonNull)
                    .collect(toList());
        } catch (final FoldingException e) {
            LOGGER.warn("Error retrieving users in teams", e.getCause());
            return Collections.emptyList();
        }
    }

    public Map<LocalDate, Stats> getDailyUserStats(final int userId, final Month month, final Year year) throws FoldingException, UserNotFoundException {
        return dbManager.getDailyUserStats(userId, month, year);
    }
}
